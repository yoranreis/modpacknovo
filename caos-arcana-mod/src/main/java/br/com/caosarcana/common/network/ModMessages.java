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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class ModMessages {
    public static final ResourceLocation OPEN_RACE_SELECT_ID = CaosArcana.id("open_race_select");
    public static final ResourceLocation SELECT_RACE_C2S_ID = CaosArcana.id("select_race");
    public static final ResourceLocation SYNC_RPG_DATA_S2C_ID = CaosArcana.id("sync_rpg_data");
    public static final ResourceLocation CAST_SPELL_C2S_ID = CaosArcana.id("cast_spell");
    public static final ResourceLocation LEARN_SPELL_C2S_ID = CaosArcana.id("learn_spell");
    public static final ResourceLocation SELECT_SPELL_C2S_ID = CaosArcana.id("select_spell");
    public static final ResourceLocation OPEN_GRIMOIRE_STUDY_S2C_ID = CaosArcana.id("open_grimoire_study");

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

                    // Concede automaticamente o poder racial exclusivo da linhagem
                    br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(race).ifPresent(racialSpell -> {
                        rpgPlayer.caosarcana$learnSpell(racialSpell.id());
                        if (rpgPlayer.caosarcana$getSelectedSpell().isEmpty()) {
                            rpgPlayer.caosarcana$setSelectedSpell(racialSpell.id());
                        }
                    });

                    syncPlayerData(player);

                    // Valida imediatamente os equipamentos do jogador
                    EquipmentValidationHandler.validateAndEnforce(player);

                    player.sendSystemMessage(Component.literal("§6[Caos Arcana] §aVocê escolheu a raça: §e" + race.getDisplayName() + "§a!"));
                    player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                    CaosArcana.LOGGER.info("Jogador {} confirmou a raça: {}", player.getName().getString(), race.getDisplayName());
                }
            });
        });

        // Manipulador de solicitação de conjuração de magia via tecla
        ServerPlayNetworking.registerGlobalReceiver(CAST_SPELL_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String spellId = buf.readUtf(64);
            server.execute(() -> br.com.caosarcana.content.spells.SpellCastHandler.handleCast(player, spellId));
        });

        // Manipulador de estudo e gravação permanente de grimório
        ServerPlayNetworking.registerGlobalReceiver(LEARN_SPELL_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String spellId = buf.readUtf(64);
            server.execute(() -> br.com.caosarcana.content.spells.GrimoireStudyHandler.handleLearn(player, spellId));
        });

        // Manipulador de seleção de magia ativa
        ServerPlayNetworking.registerGlobalReceiver(SELECT_SPELL_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String spellId = buf.readUtf(64);
            server.execute(() -> {
                if (player instanceof IRPGPlayerData rpgPlayer) {
                    if (!rpgPlayer.caosarcana$hasChosenRace()) {
                        player.displayClientMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de gerenciar magias!"), true);
                        return;
                    }

                    String normalized = (spellId != null) ? spellId.trim().toLowerCase() : "";
                    if (!rpgPlayer.caosarcana$knowsSpell(normalized)) {
                        CaosArcana.LOGGER.warn("Jogador {} tentou selecionar magia desconhecida ou não aprendida: '{}'", player.getName().getString(), spellId);
                        player.displayClientMessage(Component.literal("§c[Caos Arcana] Você não conhece esta magia para selecioná-la!"), true);
                        player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                        syncPlayerData(player);
                        return;
                    }

                    rpgPlayer.caosarcana$setSelectedSpell(normalized);
                    syncPlayerData(player);

                    String spellName = br.com.caosarcana.content.spells.SpellRegistry.get(normalized)
                            .map(br.com.caosarcana.content.spells.SpellDefinition::name)
                            .orElse(normalized);

                    player.displayClientMessage(Component.literal("§6[Caos Arcana] Magia ativa: §e" + spellName), true);
                    player.playNotifySound(SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.6f, 1.2f);
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
                    // Garante que o jogador conheça o poder racial de sua raça
                    br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(rpgPlayer.caosarcana$getRace()).ifPresent(racialSpell -> {
                        if (!rpgPlayer.caosarcana$knowsSpell(racialSpell.id())) {
                            rpgPlayer.caosarcana$learnSpell(racialSpell.id());
                        }
                        if (rpgPlayer.caosarcana$getSelectedSpell().isEmpty()) {
                            rpgPlayer.caosarcana$setSelectedSpell(racialSpell.id());
                        }
                    });
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

        // 1.1. Desconexão do jogador: dissipa Familiar Espectral ativo
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player instanceof IRPGPlayerData rpgPlayer) {
                UUID familiarId = rpgPlayer.caosarcana$getActiveFamiliarId();
                if (familiarId != null) {
                    for (ServerLevel level : server.getAllLevels()) {
                        Entity entity = level.getEntity(familiarId);
                        if (entity != null) {
                            level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + 0.5, entity.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                            entity.discard();
                            break;
                        }
                    }
                    rpgPlayer.caosarcana$setActiveFamiliarId(null);
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

                // Transfere permanentemente todo o conhecimento arcano aprendido
                for (String spellId : oldData.caosarcana$getKnownSpells()) {
                    newData.caosarcana$learnSpell(spellId);
                    long ready = oldData.caosarcana$getSpellReadyTick(spellId);
                    if (ready > 0) {
                        newData.caosarcana$setSpellCooldown(spellId, ready);
                    }
                }
                newData.caosarcana$setSelectedSpell(oldData.caosarcana$getSelectedSpell());

                // Dissipa com segurança qualquer familiar invocado ativo na morte do conjurador
                UUID oldFamiliar = oldData.caosarcana$getActiveFamiliarId();
                if (oldFamiliar != null && oldPlayer.server != null) {
                    for (ServerLevel level : oldPlayer.server.getAllLevels()) {
                        Entity entity = level.getEntity(oldFamiliar);
                        if (entity != null) {
                            level.sendParticles(ParticleTypes.POOF, entity.getX(), entity.getY() + 0.5, entity.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                            entity.discard();
                            break;
                        }
                    }
                }
                newData.caosarcana$setActiveFamiliarId(null);

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

        // Regeneração de Mana e Passivas Raciais a cada 1 segundo (20 ticks) na thread do servidor
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
                        float currentMana = rpg.caosarcana$getMana();
                        float maxMana = rpg.caosarcana$getMaxMana();
                        if (currentMana < maxMana) {
                            float regen = rpg.caosarcana$getManaRegenPerSecond();
                            rpg.caosarcana$restoreMana(regen);
                            syncPlayerData(player);
                        }

                        // Passivas Raciais Calibradas (Equilíbrio e Sobrevivência)
                        Race race = rpg.caosarcana$getRace();
                        switch (race) {
                            case ARGONIANO -> {
                                // O fôlego 3x mais longo é gerenciado continuadamente a 1/3 de consumo via LivingEntityRPGMixin
                                // Impulso de nado apenas quando nadando ativamente (sem quebrar exploração marinha)
                                if (player.isSwimming()) {
                                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 40, 0, true, false, true));
                                }
                            }
                            case ELFO_NEGRO -> {
                                // Sangue das Cinzas: extingue chamas o dobro da velocidade sem conceder imunidade total à lava do Nether
                                if (player.isOnFire() && player.getRemainingFireTicks() > 20) {
                                    player.setRemainingFireTicks(player.getRemainingFireTicks() - 20);
                                }
                            }
                            case NORDICO -> {
                                // Resistência ao gelo de Skyrim: limpa congelamento ambiental
                                if (player.getTicksFrozen() > 0) {
                                    player.setTicksFrozen(0);
                                }
                            }
                            case ORC -> {
                                // Tenacidade de Sobrevivência: se estiver abaixo de 20% de HP (4 pontos), concede breve firmeza defensiva
                                if (player.getHealth() <= 4.0f && !player.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE)) {
                                    player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, false));
                                }
                            }
                            case ELFO_FLORESTA -> {
                                // Metabolismo Silvestre: acelera a cura de venenos em 50% sem anular o impacto inicial de poções e toxinas
                                if (player.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
                                    net.minecraft.world.effect.MobEffectInstance poison = player.getEffect(net.minecraft.world.effect.MobEffects.POISON);
                                    if (poison != null && poison.getDuration() > 40) {
                                        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.POISON, poison.getDuration() - 20, poison.getAmplifier(), poison.isAmbient(), poison.isVisible()));
                                    }
                                }
                            }
                            case GUARDA_VERMELHO -> {
                                // Fôlego Marcial: desacelera a perda de saturação em combate
                                if (player.getFoodData().needsFood() && server.getTickCount() % 120 == 0) {
                                    player.getFoodData().eat(1, 0.2f);
                                }
                            }
                            default -> {}
                        }
                    }
                }
            }
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

            java.util.Set<String> knownSpells = rpgPlayer.caosarcana$getKnownSpells();
            buf.writeInt(knownSpells.size());
            for (String spellId : knownSpells) {
                buf.writeUtf(spellId);
            }
            buf.writeUtf(rpgPlayer.caosarcana$getSelectedSpell() != null ? rpgPlayer.caosarcana$getSelectedSpell() : "");

            ServerPlayNetworking.send(player, SYNC_RPG_DATA_S2C_ID, buf);
        }
    }
}
