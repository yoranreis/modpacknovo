package br.com.caosarcana.common.command;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.EquipmentValidationHandler;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import br.com.caosarcana.content.spells.SpellDefinition;
import br.com.caosarcana.content.spells.SpellRegistry;
import br.com.caosarcana.content.trials.ArcaneTrialRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.Optional;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CaosArcanaCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CaosArcanaCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("caos")
            .then(Commands.literal("status")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        Race race = rpgPlayer.caosarcana$getRace();
                        double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                        double maxHp = player.getMaxHealth();
                        double attack = player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                        double armor = player.getAttributeValue(Attributes.ARMOR);

                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Caos Arcana: Status do Personagem] ---"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eRaça Atual: §f" + race.getDisplayName() + " §7(" + race.getDocumentationStatus() + ")"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eNível Geral: §f" + rpgPlayer.caosarcana$getGeneralLevel()), false);
                        int piroLvl = rpgPlayer.caosarcana$getSchoolLevel("piromancia");
                        int piroXp = rpgPlayer.caosarcana$getSchoolXp("piromancia");
                        int piroNeeded = br.com.caosarcana.content.spells.PyromancyProgressionHandler.getXpNeededForLevel(piroLvl);
                        String piroProgStr = (piroLvl >= 100) ? "§6MAX" : (piroXp + "/" + piroNeeded + " XP");
                        ctx.getSource().sendSuccess(() -> Component.literal("§cPiromancia: §fNível " + piroLvl + " §7(" + piroProgStr + "§7)"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§bMana: §f" + String.format("%.0f/%.0f", rpgPlayer.caosarcana$getMana(), rpgPlayer.caosarcana$getMaxMana())), false);
                        java.util.Set<String> spells = rpgPlayer.caosarcana$getKnownSpells();
                        ctx.getSource().sendSuccess(() -> Component.literal("§dMagias Conhecidas: §f" + (spells.isEmpty() ? "§7Nenhuma" : String.join(", ", spells))), false);
                        String selected = rpgPlayer.caosarcana$getSelectedSpell();
                        ctx.getSource().sendSuccess(() -> Component.literal("§dMagia Ativa: §f" + (selected == null || selected.isEmpty() ? "§7Nenhuma" : selected)), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§aVantagens Declaradas: §7" + race.getActiveAdvantages()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§cPenalidades Declaradas: §7" + race.getActiveDisadvantages()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Atributos Reais Medidos no Jogo] ---"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format("§f• Velocidade: §e%.3f §7(Base Vanilla: 0.100 | Felino: 0.115 | Orc: 0.090)", speed)), false);
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format("§f• Vida Máxima: §e%.1f §7(Base Vanilla: 20.0 = 10 corações)", maxHp)), false);
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format("§f• Dano de Ataque: §e%.1f §7(Base Vanilla: 1.0)", attack)), false);
                        ctx.getSource().sendSuccess(() -> Component.literal(String.format("§f• Armadura Natural: §e%.1f §7(Base Vanilla: 0.0)", armor)), false);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("resetrace")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        rpgPlayer.caosarcana$getRace().removeModifiers(player);
                        rpgPlayer.caosarcana$setRace(Race.NONE);
                        rpgPlayer.caosarcana$setChosenRace(false);

                        ModMessages.syncPlayerData(player);
                        EquipmentValidationHandler.validateAndEnforce(player);

                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, ModMessages.OPEN_RACE_SELECT_ID, buf);

                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Raça resetada com sucesso. Modificadores antigos removidos, equipamentos incompatíveis desequipados e tela reaberta."), true);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("setrace")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("race", StringArgumentType.word())
                    .executes(ctx -> {
                        String raceName = StringArgumentType.getString(ctx, "race");
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (player instanceof IRPGPlayerData rpgPlayer) {
                            Race race = Race.fromId(raceName);
                            if (race == null || race == Race.NONE) {
                                ctx.getSource().sendFailure(Component.literal("§cRaça desconhecida: " + raceName));
                                return 0;
                            }
                            rpgPlayer.caosarcana$getRace().removeModifiers(player);
                            rpgPlayer.caosarcana$setRace(race);
                            rpgPlayer.caosarcana$setChosenRace(true);
                            race.applyModifiers(player);

                            // Expurgar magias raciais antigas de outras linhagens
                            java.util.List<String> oldRacialSpells = new java.util.ArrayList<>();
                            for (String known : rpgPlayer.caosarcana$getKnownSpells()) {
                                br.com.caosarcana.content.spells.SpellRegistry.get(known).ifPresent(def -> {
                                    if (def.isRacial() && def.requiredRace() != race) {
                                        oldRacialSpells.add(known);
                                    }
                                });
                            }
                            for (String rm : oldRacialSpells) {
                                rpgPlayer.caosarcana$unlearnSpell(rm);
                            }

                            // Concede o feitiço da nova raça
                            br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(race).ifPresent(racialSpell -> {
                                rpgPlayer.caosarcana$learnSpell(racialSpell.id());
                                if (rpgPlayer.caosarcana$getSelectedSpell().isEmpty() || oldRacialSpells.contains(rpgPlayer.caosarcana$getSelectedSpell())) {
                                    rpgPlayer.caosarcana$setSelectedSpell(racialSpell.id());
                                }
                            });

                            ModMessages.syncPlayerData(player);
                            EquipmentValidationHandler.validateAndEnforce(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Raça alterada para §e" + race.getDisplayName() + "§a."), true);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("setlevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int newLevel = IntegerArgumentType.getInteger(ctx, "level");
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (player instanceof IRPGPlayerData rpgPlayer) {
                            rpgPlayer.caosarcana$setGeneralLevel(newLevel);
                            ModMessages.syncPlayerData(player);
                            EquipmentValidationHandler.validateAndEnforce(player);

                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Nível geral alterado para §e" + newLevel + "§a. Requisitos revalidados."), true);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("setschool")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("school", StringArgumentType.word())
                    .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> {
                            String rawSchool = StringArgumentType.getString(ctx, "school").toLowerCase().trim();
                            int newLevel = IntegerArgumentType.getInteger(ctx, "level");
                            ServerPlayer player = ctx.getSource().getPlayerOrException();

                            java.util.Set<String> validSchools = java.util.Set.of(
                                "piromancia", "criomancia", "eletromancia", "restauracao", "alteracao", "conjuracao", "ilusao"
                            );
                            if (!validSchools.contains(rawSchool)) {
                                ctx.getSource().sendFailure(Component.literal("§c[Caos Arcana] Escola inválida: " + rawSchool + ". Escolas válidas: " + String.join(", ", validSchools)));
                                return 0;
                            }

                            if (player instanceof IRPGPlayerData rpgPlayer) {
                                rpgPlayer.caosarcana$setSchoolLevel(rawSchool, newLevel);
                                rpgPlayer.caosarcana$setSchoolXp(rawSchool, 0);
                                ModMessages.syncPlayerData(player);

                                if ("piromancia".equals(rawSchool)) {
                                    br.com.caosarcana.content.spells.PyromancyProgressionHandler.checkAndGrantMilestones(player);
                                }

                                ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Escola §e" + rawSchool + "§a definida para o Nível §e" + newLevel + "§a (XP resetado para 0)."), true);
                            }
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("setmana")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("amount", com.mojang.brigadier.arguments.FloatArgumentType.floatArg(0.0f, 500.0f))
                    .executes(ctx -> {
                        float amount = com.mojang.brigadier.arguments.FloatArgumentType.getFloat(ctx, "amount");
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (player instanceof IRPGPlayerData rpgPlayer) {
                            rpgPlayer.caosarcana$setMana(amount);
                            ModMessages.syncPlayerData(player);
                            ctx.getSource().sendSuccess(() -> Component.literal(String.format("§a[Caos Arcana] Mana alterada para §b%.0f/%.0f§a.", rpgPlayer.caosarcana$getMana(), rpgPlayer.caosarcana$getMaxMana())), true);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("checkequipment")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    EquipmentValidationHandler.validateAndEnforce(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Verificação de equipamentos concluída no servidor."), false);
                    return 1;
                })
            )
            .then(Commands.literal("spells")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Caos Arcana: Conhecimento Arcano] ---"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eRaça: §f" + rpgPlayer.caosarcana$getRace().getDisplayName()), false);
                        SpellRegistry.getRacialSpellFor(rpgPlayer.caosarcana$getRace()).ifPresent(racial ->
                            ctx.getSource().sendSuccess(() -> Component.literal("§dPoder Racial: §f" + racial.name() + " §7(" + racial.id() + ")"), false)
                        );
                        String selected = rpgPlayer.caosarcana$getSelectedSpell();
                        ctx.getSource().sendSuccess(() -> Component.literal("§aMagia Ativa [V]: §f" + (selected.isEmpty() ? "§7Nenhuma" : selected)), false);
                        java.util.Set<String> known = rpgPlayer.caosarcana$getKnownSpells();
                        ctx.getSource().sendSuccess(() -> Component.literal("§bMagias Conhecidas (" + known.size() + "): §f" + (known.isEmpty() ? "§7Nenhuma" : String.join(", ", known))), false);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("learn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("spell", StringArgumentType.string())
                    .executes(ctx -> {
                        String spellId = StringArgumentType.getString(ctx, "spell");
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (player instanceof IRPGPlayerData rpgPlayer) {
                            Optional<SpellDefinition> spellOpt = SpellRegistry.get(spellId);
                            if (spellOpt.isEmpty()) {
                                ctx.getSource().sendFailure(Component.literal("§cMagia não encontrada: " + spellId));
                                return 0;
                            }
                            SpellDefinition spell = spellOpt.get();
                            rpgPlayer.caosarcana$learnSpell(spell.id());
                            if (rpgPlayer.caosarcana$getSelectedSpell().isEmpty()) {
                                rpgPlayer.caosarcana$setSelectedSpell(spell.id());
                            }
                            ModMessages.syncPlayerData(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Magia §e" + spell.name() + " §aensinada com sucesso para " + player.getName().getString()), true);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("provacao")
                .then(Commands.literal("listar")
                    .executes(ctx -> {
                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Provações Arcanas Disponíveis] ---"), false);
                        for (ArcaneTrialRegistry.Trial trial : ArcaneTrialRegistry.getAll()) {
                            Optional<SpellDefinition> spell = SpellRegistry.get(trial.rewardSpellId());
                            String spellName = spell.map(SpellDefinition::name).orElse(trial.rewardSpellId());
                            ctx.getSource().sendSuccess(() -> Component.literal("§e• §6" + trial.name() + " §7(ID: §f" + trial.id() + "§7)"), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("  §8Escola: §b" + trial.school() + " §7(Req: Nível " + trial.requiredLevel() + ") | §aRecompensa: §e" + spellName), false);
                            ctx.getSource().sendSuccess(() -> Component.literal("  §7Instrução: §f" + trial.trialInstructions()), false);
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("concluir")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("trial_id", StringArgumentType.word())
                        .executes(ctx -> {
                            String trialId = StringArgumentType.getString(ctx, "trial_id");
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            boolean ok = ArcaneTrialRegistry.completeTrial(player, trialId);
                            if (ok) {
                                ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Provação §e" + trialId + " §aconcluída para " + player.getName().getString() + "!"), true);
                            } else {
                                ctx.getSource().sendFailure(Component.literal("§cProvação não encontrada: " + trialId));
                            }
                            return ok ? 1 : 0;
                        })
                    )
                )
            )
            .then(Commands.literal("testweapon")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("enable")
                    .executes(ctx -> {
                        EquipmentRequirementRegistry.setExperimentalWeaponRuleActive(true);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Regra experimental de arma de teste ativada: minecraft:iron_sword permitida somente para Orc (Nível >= 3)."), true);
                        return 1;
                    })
                )
                .then(Commands.literal("disable")
                    .executes(ctx -> {
                        EquipmentRequirementRegistry.setExperimentalWeaponRuleActive(false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Regra experimental de arma de teste desativada."), true);
                        return 1;
                    })
                )
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        boolean active = EquipmentRequirementRegistry.isExperimentalWeaponRuleActive();
                        ctx.getSource().sendSuccess(() -> Component.literal("§6[Caos Arcana] Regra experimental de arma: " + (active ? "§aATIVADA (Orc Nível >= 3)" : "§cDESATIVADA")), false);
                        return 1;
                    })
                )
            )
            .then(Commands.literal("testattack")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.entity())
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        Entity target = EntityArgument.getEntity(ctx, "target");
                        player.attack(target);
                        ctx.getSource().sendSuccess(() -> Component.literal("§7[Caos Arcana] Ataque padrão executado contra " + target.getName().getString()), true);
                        return 1;
                    })
                    .then(Commands.argument("combo", IntegerArgumentType.integer(0, 10))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            Entity target = EntityArgument.getEntity(ctx, "target");
                            int combo = IntegerArgumentType.getInteger(ctx, "combo");
                            if (FabricLoader.getInstance().isModLoaded("bettercombat")) {
                                try {
                                    java.lang.reflect.Method setComboMethod = player.getClass().getMethod("setComboCount", int.class);
                                    setComboMethod.invoke(player, combo);
                                } catch (Throwable ignored) {}
                            }
                            player.attack(target);
                            ctx.getSource().sendSuccess(() -> Component.literal("§7[Caos Arcana] Ataque Better Combat (combo " + combo + ") executado contra " + target.getName().getString()), true);
                            return 1;
                        })
                    )
                )
            )
            .then(Commands.literal("talent")
                .then(Commands.literal("open")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        boolean ok = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.openScreen(player);
                        if (!ok) {
                            ctx.getSource().sendFailure(Component.literal("§c[Caos Arcana] Pufferfish's Skills não está ativo no servidor."));
                            return 0;
                        }
                        return 1;
                    })
                )
                .then(Commands.literal("points")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                            .executes(ctx -> {
                                ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                int amount = IntegerArgumentType.getInteger(ctx, "amount");
                                boolean ok = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.addPoints(target, amount);
                                if (ok) {
                                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Concedidos " + amount + " ponto(s) de talento de Piromancia para " + target.getName().getString()), true);
                                    return 1;
                                } else {
                                    ctx.getSource().sendFailure(Component.literal("§c[Caos Arcana] Pufferfish's Skills não está ativo ou falhou ao conceder pontos."));
                                    return 0;
                                }
                            })
                        )
                    )
                )
                .then(Commands.literal("reset")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                            boolean ok = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.resetTalents(target);
                            if (ok) {
                                ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Talentos de Piromancia resetados para " + target.getName().getString() + " (magias aprendidas preservadas)."), true);
                                return 1;
                            } else {
                                ctx.getSource().sendFailure(Component.literal("§c[Caos Arcana] Pufferfish's Skills não está ativo ou falhou ao resetar talentos."));
                                return 0;
                            }
                        })
                    )
                )
            )
        );
    }
}
