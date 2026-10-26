package br.com.caosarcana.common.network;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.equipment.EquipmentValidationHandler;
import br.com.caosarcana.common.race.Race;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

public class ModMessages {
    public static final ResourceLocation OPEN_RACE_SELECT_ID = CaosArcana.id("open_race_select");
    public static final ResourceLocation SELECT_RACE_C2S_ID = CaosArcana.id("select_race");
    public static final ResourceLocation SYNC_RPG_DATA_S2C_ID = CaosArcana.id("sync_rpg_data");

    public static void registerC2SPackets() {
        // Manipulador estrito executado na Server Thread
        ServerPlayNetworking.registerGlobalReceiver(SELECT_RACE_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String raceId = buf.readUtf(64); // Limita tamanho para evitar flood/buffer overflow
            server.execute(() -> {
                if (player instanceof IRPGPlayerData rpgPlayer) {
                    // 1. Validação estrita: Rejeita se já escolheu
                    if (rpgPlayer.caosarcana$hasChosenRace()) {
                        CaosArcana.LOGGER.warn("Tentativa bloqueada: Jogador {} tentou re-selecionar raça após já ter confirmado.", player.getName().getString());
                        return;
                    }

                    // 2. Validação estrita: Rejeita ID inválido ou nulo
                    Race race = Race.fromId(raceId);
                    if (race == Race.NONE) {
                        CaosArcana.LOGGER.warn("Tentativa bloqueada: Jogador {} tentou enviar uma raça inválida: '{}'", player.getName().getString(), raceId);
                        return;
                    }

                    // 3. Aplica a escolha de forma segura
                    rpgPlayer.caosarcana$setRace(race);
                    rpgPlayer.caosarcana$setChosenRace(true);
                    race.applyModifiers(player);

                    syncPlayerData(player);

                    // Valida imediatamente os equipamentos do jogador
                    EquipmentValidationHandler.validateAndEnforce(player);

                    player.sendSystemMessage(Component.literal("§6[Caos Arcana] §aVocê escolheu a raça: §e" + race.getDisplayName() + "§a!"));
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                    CaosArcana.LOGGER.info("Jogador {} confirmou a raça: {}", player.getName().getString(), race.getDisplayName());
                }
            });
        });
    }

    public static void registerServerEvents() {
        // 1. Conexão ao servidor (Server Thread)
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player instanceof IRPGPlayerData rpgPlayer) {
                if (rpgPlayer.caosarcana$hasChosenRace()) {
                    rpgPlayer.caosarcana$getRace().applyModifiers(player);
                }

                syncPlayerData(player);
                EquipmentValidationHandler.validateAndEnforce(player);
                EquipmentValidationHandler.processPendingDeliveries(player);

                // Se ainda não escolheu a raça, força abertura da tela
                if (!rpgPlayer.caosarcana$hasChosenRace()) {
                    server.execute(() -> {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, OPEN_RACE_SELECT_ID, buf);
                    });
                }
            }
        });

        // 2. Morte e Reaparecimento: Transfere dados persistentes e custódia segura
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer instanceof IRPGPlayerData oldData && newPlayer instanceof IRPGPlayerData newData) {
                newData.caosarcana$setRace(oldData.caosarcana$getRace());
                newData.caosarcana$setChosenRace(oldData.caosarcana$hasChosenRace());
                newData.caosarcana$setMana(oldData.caosarcana$getMana());
                newData.caosarcana$setBaseMaxMana(oldData.caosarcana$getBaseMaxMana());
                newData.caosarcana$setGeneralLevel(oldData.caosarcana$getGeneralLevel());

                // Transfere integralmente a fila de custódia segura sem perder nenhum item
                newData.caosarcana$clearPendingDeliveries();
                for (ItemStack pending : oldData.caosarcana$getPendingDeliveries()) {
                    newData.caosarcana$addPendingDelivery(pending);
                }

                if (newData.caosarcana$hasChosenRace()) {
                    newData.caosarcana$getRace().applyModifiers(newPlayer);
                }

                syncPlayerData(newPlayer);
            }
        });

        // 3. Após Reaparecimento: sincroniza, valida equipamentos e entrega custódia
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            syncPlayerData(newPlayer);
            EquipmentValidationHandler.validateAndEnforce(newPlayer);
            EquipmentValidationHandler.processPendingDeliveries(newPlayer);
        });

        // 4. Troca de Dimensão: garante sincronização e validação sem acumular bônus
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (player instanceof IRPGPlayerData rpgPlayer && rpgPlayer.caosarcana$hasChosenRace()) {
                rpgPlayer.caosarcana$getRace().applyModifiers(player);
                syncPlayerData(player);
                EquipmentValidationHandler.validateAndEnforce(player);
                EquipmentValidationHandler.processPendingDeliveries(player);
            }
        });

        // 5. Proteções no servidor enquanto o jogador não tiver escolhido raça
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                player.sendSystemMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de quebrar blocos!"));
                return false;
            }
            return true;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                player.sendSystemMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de atacar criaturas!"));
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                player.sendSystemMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de interagir com blocos ou recipientes!"));
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });

        // Lembrete periódico e re-abertura forçada na tela para jogadores sem raça
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 60 == 0) { // A cada 3 segundos
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, OPEN_RACE_SELECT_ID, buf);
                        player.displayClientMessage(Component.literal("§6[Caos Arcana] Escolha sua raça para desbloquear suas ações no mundo!"), true);
                    }
                }
            }
        });
    }

    public static void syncPlayerData(ServerPlayer player) {
        if (player instanceof IRPGPlayerData rpgPlayer) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(rpgPlayer.caosarcana$getRace().getId());
            buf.writeBoolean(rpgPlayer.caosarcana$hasChosenRace());
            buf.writeFloat(rpgPlayer.caosarcana$getMana());
            buf.writeFloat(rpgPlayer.caosarcana$getMaxMana());
            buf.writeInt(rpgPlayer.caosarcana$getGeneralLevel());

            ServerPlayNetworking.send(player, SYNC_RPG_DATA_S2C_ID, buf);
        }
    }
}
