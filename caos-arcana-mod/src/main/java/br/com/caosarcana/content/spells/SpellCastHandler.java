package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.content.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Centralizador de regras e validação no servidor para conjuração de magias.
 * Executa as 10 magias raciais exclusivas e as 10 magias universais do Caos Arcana.
 */
public class SpellCastHandler {
    public static final String CENTELHA_IGNEA_ID = "caosarcana:centelha_ignea";
    public static final String FAMILIAR_ESPECTRAL_ID = "caosarcana:familiar_espectral";

    public enum CastResult {
        SUCCESS,
        NO_RACE,
        NO_SPELL_SELECTED,
        SPELL_NOT_KNOWN,
        REQUIREMENT_NOT_MET,
        ON_COOLDOWN,
        INSUFFICIENT_MANA,
        UNKNOWN_SPELL
    }

    public static CastResult handleCast(ServerPlayer player, String rawSpellId) {
        if (!(player instanceof IRPGPlayerData rpgPlayer)) {
            return CastResult.UNKNOWN_SPELL;
        }

        String spellId = (rawSpellId != null) ? rawSpellId.trim().toLowerCase() : "";
        if (spellId.isEmpty()) {
            spellId = rpgPlayer.caosarcana$getSelectedSpell();
        }

        // 1. Validação de Raça escolhida
        if (!rpgPlayer.caosarcana$hasChosenRace()) {
            player.displayClientMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de canalizar magia!"), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return CastResult.NO_RACE;
        }

        // 2. Validação se há magia ativa
        if (spellId == null || spellId.isEmpty()) {
            player.displayClientMessage(Component.literal("§c[Caos Arcana] Nenhuma magia selecionada! Pressione [R] para selecionar uma magia aprendida."), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return CastResult.NO_SPELL_SELECTED;
        }

        // 3. Validação no SpellRegistry
        var spellOpt = SpellRegistry.get(spellId);
        if (spellOpt.isEmpty()) {
            player.displayClientMessage(Component.literal("§c[Caos Arcana] Magia desconhecida: " + rawSpellId), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return CastResult.UNKNOWN_SPELL;
        }

        SpellDefinition spell = spellOpt.get();

        // 4. Validação de Restrição Racial
        if (spell.isRacial() && !spell.canCast(rpgPlayer.caosarcana$getRace())) {
            player.displayClientMessage(Component.literal("§cEsta magia é exclusiva da linhagem " + spell.requiredRace().getDisplayName() + "!"), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return CastResult.REQUIREMENT_NOT_MET;
        }

        // 5. Validação de Conhecimento permanente
        if (!rpgPlayer.caosarcana$knowsSpell(spell.id())) {
            player.displayClientMessage(Component.literal("§cVocê ainda não aprendeu esta magia! Estude o grimório ou conclua a provação correspondente."), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.5f);
            return CastResult.SPELL_NOT_KNOWN;
        }

        // 6. Validação de Requisito de Escola de Magia
        if (!spell.school().equalsIgnoreCase("racas")) {
            int schoolLevel = rpgPlayer.caosarcana$getSchoolLevel(spell.school());
            if (schoolLevel < spell.minSchoolLevel()) {
                player.displayClientMessage(Component.literal("§cVocê não atende ao requisito de " + spell.schoolDisplayName() + " (Requer nível " + spell.minSchoolLevel() + ", atual: " + schoolLevel + ")!"), true);
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.5f);
                return CastResult.REQUIREMENT_NOT_MET;
            }
        }

        // 7. Validação de Recarga (Cooldown individual por feitiço no jogador)
        long gameTime = player.level().getGameTime();
        if (rpgPlayer.caosarcana$isSpellOnCooldown(spell.id(), gameTime)) {
            long remainingTicks = rpgPlayer.caosarcana$getSpellReadyTick(spell.id()) - gameTime;
            float seconds = remainingTicks / 20.0f;
            player.displayClientMessage(Component.literal(String.format("§c%s ainda está em recarga! Aguarde %.1fs.", spell.name(), seconds)), true);
            return CastResult.ON_COOLDOWN;
        }

        // 8. Tratamento atômico para Familiar Espectral (valida criação e inserção antes de cobrar mana/recarga ou substituir familiar)
        if (FAMILIAR_ESPECTRAL_ID.equalsIgnoreCase(spell.id())) {
            ServerLevel level = (ServerLevel) player.level();
            float cost = spell.manaCost();

            // 8.1. Validação prévia de Mana (sem consumir antecipadamente)
            if (rpgPlayer.caosarcana$getMana() < cost) {
                player.displayClientMessage(
                        Component.literal("§cMana insuficiente! §7(Requer: §b" + (int) cost + "§7 | Atual: §b" + (int) rpgPlayer.caosarcana$getMana() + "§7)"),
                        true
                );
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.9f, 1.5f);
                ModMessages.syncPlayerData(player);
                return CastResult.INSUFFICIENT_MANA;
            }

            // 8.2. Instanciação e configuração prévia da nova entidade
            Wolf candidateWolf = EntityType.WOLF.create(level);
            if (candidateWolf == null) {
                player.displayClientMessage(Component.literal("§c[Conjuração] Falha ao manifestar o Familiar Espectral no plano físico."), true);
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                return CastResult.REQUIREMENT_NOT_MET;
            }

            candidateWolf.setPos(player.getX() + 1.0, player.getY(), player.getZ() + 1.0);
            candidateWolf.tame(player);
            candidateWolf.setCustomName(Component.literal("§bEspírito Familiar de " + player.getName().getString()));
            candidateWolf.setCustomNameVisible(true);
            candidateWolf.addTag("caosarcana:spectral_familiar");
            candidateWolf.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0));

            // 8.3. Verificação estrita de sucesso na inserção no mundo antes de debitar ou substituir
            boolean inserted = level.addFreshEntity(candidateWolf);
            if (!inserted) {
                candidateWolf.discard();
                player.displayClientMessage(Component.literal("§c[Conjuração] Não foi possível materializar o Familiar Espectral neste local."), true);
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                return CastResult.REQUIREMENT_NOT_MET;
            }

            // 8.4. Inserção comprovadamente confirmada no mundo: efetiva a cobrança e substituição
            rpgPlayer.caosarcana$consumeMana(cost);
            rpgPlayer.caosarcana$setSpellCooldown(spell.id(), gameTime + spell.cooldownTicks());
            ModMessages.syncPlayerData(player);

            // Dissipa com segurança invocação anterior apenas após a confirmação da nova
            UUID oldFamiliarId = rpgPlayer.caosarcana$getActiveFamiliarId();
            if (oldFamiliarId != null) {
                for (ServerLevel sl : player.server.getAllLevels()) {
                    net.minecraft.world.entity.Entity oldEntity = sl.getEntity(oldFamiliarId);
                    if (oldEntity != null) {
                        sl.sendParticles(ParticleTypes.POOF, oldEntity.getX(), oldEntity.getY() + 0.5, oldEntity.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                        oldEntity.discard();
                        break;
                    }
                }
            }

            rpgPlayer.caosarcana$setActiveFamiliarId(candidateWolf.getUUID());

            level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WOLF_HOWL, SoundSource.PLAYERS, 1.0f, 1.1f);
            player.displayClientMessage(Component.literal("§d[Conjuração] §5Familiar Espectral invocado ao seu comando (Duração: 60s)!"), true);
            return CastResult.SUCCESS;
        }

        // 8. Validação e Consumo de Mana (demais magias)
        float cost = spell.manaCost();
        if (!rpgPlayer.caosarcana$consumeMana(cost)) {
            player.displayClientMessage(
                    Component.literal("§cMana insuficiente! §7(Requer: §b" + (int) cost + "§7 | Atual: §b" + (int) rpgPlayer.caosarcana$getMana() + "§7)"),
                    true
            );
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.9f, 1.5f);
            ModMessages.syncPlayerData(player);
            return CastResult.INSUFFICIENT_MANA;
        }

        // 9. Aplica Recarga individual e Sincroniza Mana
        rpgPlayer.caosarcana$setSpellCooldown(spell.id(), gameTime + spell.cooldownTicks());
        if (CENTELHA_IGNEA_ID.equals(spell.id())) {
            player.getCooldowns().addCooldown(ModItems.CENTELHA_IGNEA, spell.cooldownTicks());
        }
        ModMessages.syncPlayerData(player);

        // 10. Executa o efeito ativo da magia
        executeSpellEffect(player, rpgPlayer, spell);

        return CastResult.SUCCESS;
    }

    private static void executeSpellEffect(ServerPlayer player, IRPGPlayerData rpgPlayer, SpellDefinition spell) {
        ServerLevel level = (ServerLevel) player.level();
        String id = spell.id().toLowerCase();

        switch (id) {
            // ==========================================
            // MAGIAS UNIVERSAIS
            // ==========================================
            case "caosarcana:centelha_ignea" -> {
                Vec3 look = player.getLookAngle();
                double spawnX = player.getX() + look.x * 0.8;
                double spawnY = player.getEyeY() - 0.1 + look.y * 0.8;
                double spawnZ = player.getZ() + look.z * 0.8;

                FlameSparkProjectile projectile = new FlameSparkProjectile(level, player, look.x, look.y, look.z);
                projectile.setPos(spawnX, spawnY, spawnZ);
                level.addFreshEntity(projectile);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 1.3f);
                level.sendParticles(ParticleTypes.FLAME, spawnX, spawnY, spawnZ, 10, 0.1, 0.1, 0.1, 0.05);

                player.displayClientMessage(Component.literal("§6[Piromancia] §cCentelha Ígnea conjurada! §b(-" + (int) spell.manaCost() + " Mana)"), true);
            }
            case "caosarcana:geada_congelante" -> {
                Vec3 look = player.getLookAngle();
                Vec3 start = player.getEyePosition();
                double maxDist = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 16.0;
                Vec3 maxPoint = start.add(look.scale(maxDist));

                net.minecraft.world.phys.BlockHitResult blockHit = level.clip(new net.minecraft.world.level.ClipContext(
                        start, maxPoint,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                ));

                double effectiveDist = (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS)
                        ? Math.max(0.5, start.distanceTo(blockHit.getLocation()))
                        : maxDist;

                java.util.Set<LivingEntity> hitEntities = new java.util.HashSet<>();
                for (double d = 0.8; d <= effectiveDist; d += 0.8) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 4, 0.1, 0.1, 0.1, 0.02);

                    AABB hitBox = new AABB(point.x - 0.6, point.y - 0.6, point.z - 0.6, point.x + 0.6, point.y + 0.6, point.z + 0.6);
                    List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive() && !hitEntities.contains(e));
                    for (LivingEntity target : targets) {
                        // Verificação estrita de linha de visada (Line-of-Sight) contra superfícies sólidas para evitar atravessar portas/paredes
                        net.minecraft.world.phys.BlockHitResult los = level.clip(new net.minecraft.world.level.ClipContext(
                                start,
                                target.getEyePosition(),
                                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                net.minecraft.world.level.ClipContext.Fluid.NONE,
                                player
                        ));
                        if (los.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                            hitEntities.add(target);
                            target.hurt(player.damageSources().magic(), 5.0f);
                            target.setTicksFrozen(140); // Congela
                            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
                        }
                    }
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.4f);
                player.displayClientMessage(Component.literal("§b[Criomancia] §3Geada Congelante lançada! §b(-" + (int) spell.manaCost() + " Mana)"), true);
            }
            case "caosarcana:relampago_arcano" -> {
                Vec3 look = player.getLookAngle();
                Vec3 start = player.getEyePosition();
                double maxDist = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 24.0;
                Vec3 maxPoint = start.add(look.scale(maxDist));

                net.minecraft.world.phys.BlockHitResult blockHit = level.clip(new net.minecraft.world.level.ClipContext(
                        start, maxPoint,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                ));

                double effectiveDist = (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS)
                        ? Math.max(0.5, start.distanceTo(blockHit.getLocation()))
                        : maxDist;

                boolean hitTarget = false;
                for (double d = 1.0; d <= effectiveDist; d += 1.0) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 6, 0.15, 0.15, 0.15, 0.05);

                    if (!hitTarget) {
                        AABB hitBox = new AABB(point.x - 0.7, point.y - 0.7, point.z - 0.7, point.x + 0.7, point.y + 0.7, point.z + 0.7);
                        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive());
                        // Ordena pelo alvo estritamente mais próximo da origem do raio
                        targets.sort(Comparator.comparingDouble(e -> start.distanceToSqr(e.getEyePosition())));
                        for (LivingEntity target : targets) {
                            net.minecraft.world.phys.BlockHitResult los = level.clip(new net.minecraft.world.level.ClipContext(
                                    start,
                                    target.getEyePosition(),
                                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                                    player
                            ));
                            if (los.getType() == net.minecraft.world.phys.HitResult.Type.MISS) {
                                target.hurt(player.damageSources().lightningBolt(), 8.0f);
                                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                                hitTarget = true;
                                break;
                            }
                        }
                    }
                    if (hitTarget) {
                        // Relâmpago Arcano cessa percurso no primeiro alvo atingido
                        break;
                    }
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.7f, 1.5f);
                player.displayClientMessage(Component.literal("§e[Eletromancia] §6Relâmpago Arcano descarregado! §b(-" + (int) spell.manaCost() + " Mana)"), true);
            }
            case "caosarcana:toque_de_cura" -> {
                player.heal(6.0f); // Restaura 3 corações de vida
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 8, 0.3, 0.3, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.4f);
                player.displayClientMessage(Component.literal("§a[Restauração] §2Toque de Cura restaurou sua vitalidade! §b(-" + (int) spell.manaCost() + " Mana)"), true);
            }
            case "caosarcana:circulo_sagrado" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0));
                AABB area = player.getBoundingBox().inflate(6.0);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isInvertedHealAndHarm());
                for (LivingEntity undead : targets) {
                    undead.hurt(player.damageSources().magic(), 8.0f);
                    Vec3 push = undead.position().subtract(player.position()).normalize().scale(0.8);
                    undead.setDeltaMovement(push.x, 0.4, push.z);
                    undead.hurtMarked = true;
                }

                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.1, player.getZ(), 30, 2.5, 0.1, 2.5, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 1.2f);
                player.displayClientMessage(Component.literal("§e[Restauração] §6Círculo Sagrado ativado! Mortos-vivos repelidos!"), true);
            }
            case "caosarcana:corpo_de_carvalho" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 0)); // 45 segundos
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 0));
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.4, 0.6, 0.4, 0.1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§2[Alteração] §aCorpo de Carvalho enrijeceu sua pele por 45s!"), true);
            }
            case "caosarcana:luz_de_vela" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0)); // 60 segundos
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0));
                level.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 2.0, player.getZ(), 15, 0.3, 0.3, 0.3, 0.02);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
                player.displayClientMessage(Component.literal("§e[Alteração] §6Luz de Vela ilumina sua jornada por 60s!"), true);
            }
            case "caosarcana:espada_vinculada" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1)); // Força II por 60 segundos
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.5, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.9f, 1.3f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §bLâmina Vinculada de Oblivion forjada em seu espírito (+Força II)!"), true);
            }
            case "caosarcana:familiar_espectral" -> {
                // Invocação com ciclo atômico e validação prévia executada diretamente em castActiveSpell
            }
            case "caosarcana:manto_de_sombras" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0)); // 20s
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.4, 0.5, 0.4, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8f, 0.8f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dManto de Sombras encobre sua presença aos olhos do mundo!"), true);
            }

            // ==========================================
            // PODERES RACIAIS ESPECÍFICOS (10 RAÇAS)
            // ==========================================
            case "caosarcana:ira_berserker" -> { // Orc
                // 12s de fúria (Resistência I e Força I) com janela posterior de exaustão tática
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 240, 0));
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.5, player.getZ(), 10, 0.3, 0.3, 0.3, 0.0);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 1.0f, 0.9f);
                player.displayClientMessage(Component.literal("§c§l[Ira Berserker] §fFúria Orc ativada por 12s! (+Força I / +Resistência I)"), true);
            }
            case "caosarcana:olhos_da_noite" -> { // Khajiit
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0)); // 60s
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.2, 0.2, 0.2, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CAT_PURREOW, SoundSource.PLAYERS, 0.9f, 1.1f);
                player.displayClientMessage(Component.literal("§e§l[Olhos da Noite] §fPupilas felinas abertas sob a bênção das duas luas!"), true);
            }
            case "caosarcana:nobreza_arcana" -> { // Alto Elfo
                rpgPlayer.caosarcana$restoreMana(40.0f);
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
                level.sendParticles(ParticleTypes.WITCH, player.getX(), player.getY() + 1.2, player.getZ(), 20, 0.4, 0.5, 0.4, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 1.0f, 1.2f);
                player.displayClientMessage(Component.literal("§b§l[Nobreza Arcana] §fSurto místico altmer! 40 de Mana restaurados instantaneamente!"), true);
            }
            case "caosarcana:grito_de_batalha" -> { // Nórdico
                AABB area = player.getBoundingBox().inflate(8.0);
                List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
                for (LivingEntity e : enemies) {
                    Vec3 push = e.position().subtract(player.position()).normalize().scale(1.2);
                    e.setDeltaMovement(push.x, 0.45, push.z);
                    e.hurtMarked = true;
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 1));
                }
                level.sendParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("§9§l[Grito de Batalha] §fPela honra de Skyrim! Inimigos desorientados e repelidos!"), true);
            }
            case "caosarcana:ira_ancestral" -> { // Elfo Negro
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0)); // 20s
                AABB area = player.getBoundingBox().inflate(5.0);
                List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive());
                for (LivingEntity e : enemies) {
                    e.setSecondsOnFire(6);
                    e.hurt(player.damageSources().inFire(), 4.0f);
                }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 35, 1.5, 0.5, 1.5, 0.08);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§6§l[Ira Ancestral] §fO fogo e as cinzas de Morrowind incineram atacantes próximos!"), true);
            }
            case "caosarcana:comunhao_selvagem" -> { // Elfo da Floresta
                AABB area = player.getBoundingBox().inflate(12.0);
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area);
                for (Mob m : mobs) {
                    if (m.getTarget() == player) {
                        m.setTarget(null);
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.6, 0.4, 0.6, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.FOX_TELEPORT, SoundSource.PLAYERS, 0.9f, 1.1f);
                player.displayClientMessage(Component.literal("§a§l[Comunhão Selvagem] §fA harmonia de Valenwood acalma feras e agressores!"), true);
            }
            case "caosarcana:pele_de_dragao" -> { // Bretão
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
                rpgPlayer.caosarcana$restoreMana(25.0f);
                level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.5, 0.3, 0.1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§5§l[Pele de Dragão] §fEscudo feérico de High Rock absorveu impactos (+25 Mana)!"), true);
            }
            case "caosarcana:voz_do_imperador" -> { // Imperial
                AABB area = player.getBoundingBox().inflate(10.0);
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area);
                for (Mob m : mobs) {
                    m.setTarget(null);
                }
                player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, 300, 0)); // 15s calibrado
                level.sendParticles(ParticleTypes.NOTE, player.getX(), player.getY() + 1.5, player.getZ(), 12, 0.5, 0.5, 0.5, 0.0);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("§e§l[Voz do Imperador] §fAutoridade de Cyrodiil acalma o combate e atrai respeito!"), true);
            }
            case "caosarcana:impeto_de_adrenalina" -> { // Guarda Vermelho
                player.getFoodData().eat(8, 0.8f);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 1)); // Velocidade II (15s)
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 300, 1)); // Pressa II (15s)
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 18, 0.3, 0.5, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 1.2f);
                player.displayClientMessage(Component.literal("§4§l[Ímpeto de Adrenalina] §fVigor de Hammerfell reabastece seu fôlego e acelera seus golpes!"), true);
            }
            case "caosarcana:pele_de_hist" -> { // Argoniano
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1)); // Regeneração II (8s calibrado)
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 240, 0));
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 12, 0.3, 0.4, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.CONDUIT_AMBIENT_SHORT, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("§2§l[Pele de Hist] §fA bênção de Hist regenera suas feridas com o vigor dos pântanos!"), true);
            }
            default -> {
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.3, 0.4, 0.3, 0.1);
                player.displayClientMessage(Component.literal("§6[Caos Arcana] Magia " + spell.name() + " canalizada!"), true);
            }
        }
    }
}
