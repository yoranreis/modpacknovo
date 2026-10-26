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

public class ModMessages {
    public static final ResourceLocation OPEN_RACE_SELECT_ID = CaosArcana.id("open_race_select");
    public static final ResourceLocation SELECT_RACE_C2S_ID = CaosArcana.id("select_race");
    public static final ResourceLocation SYNC_RPG_DATA_S2C_ID = CaosArcana.id("sync_rpg_data");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SELECT_RACE_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String raceId = buf.readUtf();
            server.execute(() -> {
                if (player instanceof IRPGPlayerData rpgPlayer) {
                    // Validação estrita no servidor: rejeita re-seleção se já escolheu
                    if (!rpgPlayer.caosarcana$hasChosenRace()) {
                        Race race = Race.fromId(raceId);
                        // Validação estrita: rejeita escolhas inválidas ou nulas
                        if (race != Race.NONE) {
                            rpgPlayer.caosarcana$setRace(race);
                            rpgPlayer.caosarcana$setChosenRace(true);
                            race.applyModifiers(player);

                            syncPlayerData(player);

                            // Valida imediatamente se algum equipamento atual virou inválido
                            EquipmentValidationHandler.validateAndEnforce(player);

                            player.sendSystemMessage(Component.literal("§6[Caos Arcana] §aVocê escolheu a raça: §e" + race.getDisplayName() + "§a!"));
                            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                            CaosArcana.LOGGER.info("Jogador {} confirmou a raça: {}", player.getName().getString(), race.getDisplayName());
                        } else {
                            CaosArcana.LOGGER.warn("Jogador {} tentou selecionar uma raça inválida: {}", player.getName().getString(), raceId);
                        }
                    } else {
                        CaosArcana.LOGGER.warn("Jogador {} tentou re-selecionar raça após já ter confirmado.", player.getName().getString());
                    }
                }
            });
        });
    }

    public static void registerServerEvents() {
        // 1. Conexão ao servidor
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player instanceof IRPGPlayerData rpgPlayer) {
                // Reaplica modificadores raciais de forma idempotente
                if (rpgPlayer.caosarcana$hasChosenRace()) {
                    rpgPlayer.caosarcana$getRace().applyModifiers(player);
                }

                syncPlayerData(player);
                EquipmentValidationHandler.validateAndEnforce(player);

                // Se ainda não escolheu a raça, força abertura da tela
                if (!rpgPlayer.caosarcana$hasChosenRace()) {
                    server.execute(() -> {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, OPEN_RACE_SELECT_ID, buf);
                    });
                }
            }
        });

        // 2. Morte e Reaparecimento: Transfere dados persistentes do jogador antigo para o novo
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer instanceof IRPGPlayerData oldData && newPlayer instanceof IRPGPlayerData newData) {
                newData.caosarcana$setRace(oldData.caosarcana$getRace());
                newData.caosarcana$setChosenRace(oldData.caosarcana$hasChosenRace());
                newData.caosarcana$setMana(oldData.caosarcana$getMana());
                newData.caosarcana$setMaxMana(oldData.caosarcana$getMaxMana());
                newData.caosarcana$setGeneralLevel(oldData.caosarcana$getGeneralLevel());

                if (newData.caosarcana$hasChosenRace()) {
                    newData.caosarcana$getRace().applyModifiers(newPlayer);
                }

                syncPlayerData(newPlayer);
            }
        });

        // 3. Após Reaparecimento: sincroniza e valida equipamentos
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            syncPlayerData(newPlayer);
            EquipmentValidationHandler.validateAndEnforce(newPlayer);
        });

        // 4. Troca de Dimensão: garante sincronização e validação sem acumular bônus
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (player instanceof IRPGPlayerData rpgPlayer && rpgPlayer.caosarcana$hasChosenRace()) {
                rpgPlayer.caosarcana$getRace().applyModifiers(player);
                syncPlayerData(player);
                EquipmentValidationHandler.validateAndEnforce(player);
            }
        });

        // 5. Proteções anti-bypass enquanto o jogador não tiver escolhido raça
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                player.sendSystemMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de interagir com o mundo!"));
                return false;
            }
            return true;
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                player.sendSystemMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de iniciar combates!"));
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClientSide() && player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // Lembrete periódico no tick para jogadores sem raça
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 100 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player instanceof IRPGPlayerData rpg && !rpg.caosarcana$hasChosenRace()) {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, OPEN_RACE_SELECT_ID, buf);
                        player.sendSystemMessage(Component.literal("§6[Caos Arcana] Atenção: É obrigatório concluir a criação de personagem para jogar."));
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
