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
import java.util.Optional;
import java.util.UUID;

/**
 * Centralizador de regras e validação no servidor para conjuração de magias.
 * Executa os poderes raciais exclusivos e as magias universais registradas do Caos Arcana.
 */
public class SpellCastHandler {
    public static final String CENTELHA_IGNEA_ID = "caosarcana:centelha_ignea";

    public static String formatManaAmount(float amount) {
        return new java.math.BigDecimal(Float.toString(amount))
                .stripTrailingZeros()
                .toPlainString();
    }
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
        if (CENTELHA_IGNEA_ID.equals(spell.id()) && br.com.caosarcana.common.talent.CaosArcanaTalentBridge.hasRootUnlocked(player)) {
            cost = br.com.caosarcana.common.talent.TalentConfig.getRootManaCost();
        }
        if (!rpgPlayer.caosarcana$consumeMana(cost)) {
            player.displayClientMessage(
                    Component.literal("§cMana insuficiente! §7(Requer: §b" + formatManaAmount(cost) + "§7 | Atual: §b" + formatManaAmount(rpgPlayer.caosarcana$getMana()) + "§7)"),
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
                Vec3 eyePos = player.getEyePosition();

                // Trecho exato entre a origem dos olhos e o ponto de nascimento (0.35 blocos)
                double spawnDist = 0.35;
                Vec3 spawnPos = eyePos.add(look.scale(spawnDist));

                // 1. Respeita o primeiro obstáculo sólido no trecho inicial
                net.minecraft.world.phys.BlockHitResult blockHit = level.clip(new net.minecraft.world.level.ClipContext(
                        eyePos, spawnPos,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE,
                        player
                ));

                Vec3 rayEnd = (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) ? blockHit.getLocation() : spawnPos;
                double maxDistSqr = eyePos.distanceToSqr(rayEnd);

                // 2. Seleciona o primeiro alvo válido considerando interseção, contenção ou sobreposição da hitbox
                AABB sweepBox = new AABB(eyePos, rayEnd).inflate(0.5);
                List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, sweepBox,
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player));

                LivingEntity closestEntity = null;
                double closestIntersectionDistSqr = maxDistSqr;

                for (LivingEntity candidate : candidates) {
                    AABB targetBox = candidate.getBoundingBox().inflate(0.15);
                    boolean hit = false;
                    double distSqr = Double.MAX_VALUE;

                    if (targetBox.contains(eyePos)) {
                        hit = true;
                        distSqr = 0.0;
                    } else if (targetBox.contains(rayEnd)) {
                        hit = true;
                        distSqr = 0.0;
                    } else {
                        Optional<Vec3> clip = targetBox.clip(eyePos, rayEnd);
                        if (clip.isPresent()) {
                            hit = true;
                            distSqr = eyePos.distanceToSqr(clip.get());
                        } else if (targetBox.intersects(new AABB(eyePos, rayEnd))) {
                            hit = true;
                            distSqr = eyePos.distanceToSqr(candidate.position());
                        }
                    }

                    if (hit && distSqr <= closestIntersectionDistSqr) {
                        closestIntersectionDistSqr = distSqr;
                        closestEntity = candidate;
                    }
                }

                // Instancia o projétil para compartilhamento integral da lógica de dano e autoria
                FlameSparkProjectile projectile = new FlameSparkProjectile(level, player, look.x, look.y, look.z);
                projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

                if (closestEntity != null) {
                    // Impacto no trecho imediato: aplica exatamente a mesma lógica (5 dano, 5s fogo, fireball) e descarta o projétil
                    projectile.applySpellImpact(closestEntity);
                    projectile.discard();

                    level.sendParticles(ParticleTypes.FLAME, closestEntity.getX(), closestEntity.getY() + closestEntity.getBbHeight() * 0.5, closestEntity.getZ(), 10, 0.1, 0.1, 0.1, 0.05);
                    level.playSound(null, closestEntity.getX(), closestEntity.getY(), closestEntity.getZ(),
                            SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 0.9f, 1.2f);
                } else if (blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS) {
                    // Colisão com obstáculo sólido no trecho inicial: encerra sem atravessar parede
                    projectile.discard();
                    level.sendParticles(ParticleTypes.SMOKE, blockHit.getLocation().x, blockHit.getLocation().y, blockHit.getLocation().z, 6, 0.05, 0.05, 0.05, 0.02);
                } else {
                    // Trajetória desimpedida: dispara o projétil físico
                    level.addFreshEntity(projectile);
                    level.sendParticles(ParticleTypes.FLAME, spawnPos.x, spawnPos.y, spawnPos.z, 8, 0.04, 0.04, 0.04, 0.02);
                }

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 1.3f);

                float effectiveCost = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.hasRootUnlocked(player) ? br.com.caosarcana.common.talent.TalentConfig.getRootManaCost() : spell.manaCost();
                player.displayClientMessage(Component.literal("§6[Piromancia] §cCentelha Ígnea conjurada! §b(-" + formatManaAmount(effectiveCost) + " Mana)"), true);
            }
            case "caosarcana:bola_de_fogo" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double maxDist = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 24.0;
                Vec3 end = start.add(look.scale(maxDist));
                var blockHit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                double effectiveDist = blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS
                        ? start.distanceTo(blockHit.getLocation()) : maxDist;
                LivingEntity primary = null;
                double best = effectiveDist * effectiveDist;
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,
                        new AABB(start, start.add(look.scale(effectiveDist))).inflate(1.0),
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
                    var hit = target.getBoundingBox().inflate(0.2).clip(start, start.add(look.scale(effectiveDist)));
                    if (hit.isPresent()) {
                        double distance = start.distanceToSqr(hit.get());
                        if (distance < best) { best = distance; primary = target; }
                    }
                }
                Vec3 impact = primary != null ? primary.position() : start.add(look.scale(effectiveDist));
                for (double d = 0.8; d <= Math.max(0.8, start.distanceTo(impact)); d += 0.8) {
                    Vec3 p = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 5, 0.12, 0.12, 0.12, 0.02);
                }
                if (primary != null) {
                    final LivingEntity impactTarget = primary;
                    impactTarget.hurt(player.damageSources().inFire(), br.com.caosarcana.common.talent.TalentConfig.getFireballDamage());
                    impactTarget.setSecondsOnFire(br.com.caosarcana.common.talent.TalentConfig.getFireballFireSeconds());
                    for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class,
                            impactTarget.getBoundingBox().inflate(2.0), e -> e != player && e != impactTarget && e.isAlive() && !e.isAlliedTo(player))) {
                        nearby.hurt(player.damageSources().inFire(), br.com.caosarcana.common.talent.TalentConfig.getFireballDamage() / 2.0f);
                        nearby.setSecondsOnFire(2);
                    }
                }
                level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y + 0.5, impact.z, 30, 0.6, 0.5, 0.6, 0.08);
                level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.8f, 1.2f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cBola de Fogo explodiu! §b(-" + formatManaAmount(spell.manaCost()) + " Mana)"), true);
            }
            case "caosarcana:manto_de_chamas" -> {
                int cloakDuration = br.com.caosarcana.common.talent.TalentConfig.getFlameCloakDurationSeconds();
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, cloakDuration * 20, 0));
                AABB area = player.getBoundingBox().inflate(br.com.caosarcana.common.talent.TalentConfig.getFlameCloakRadius());
                for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
                    enemy.hurt(player.damageSources().inFire(), br.com.caosarcana.common.talent.TalentConfig.getFlameCloakDamage());
                    enemy.setSecondsOnFire(br.com.caosarcana.common.talent.TalentConfig.getFlameCloakFireSeconds());
                }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 40, 2.0, 1.0, 2.0, 0.08);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cManto de Chamas ativo por " + cloakDuration + "s!"), true);
            }
            case "caosarcana:muralha_de_chamas" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double length = Math.min(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 12.0,
                        br.com.caosarcana.common.talent.TalentConfig.getFlameWallLength());
                java.util.Set<LivingEntity> affected = new java.util.HashSet<>();
                for (double d = 1.0; d <= length; d += 1.0) {
                    Vec3 center = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.FLAME, center.x, center.y, center.z, 12, 0.9, 0.7, 0.9, 0.03);
                    for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(1.3),
                            e -> e != player && e.isAlive() && !e.isAlliedTo(player) && !affected.contains(e))) {
                        affected.add(enemy);
                        enemy.hurt(player.damageSources().inFire(), br.com.caosarcana.common.talent.TalentConfig.getFlameWallDamage());
                        enemy.setSecondsOnFire(br.com.caosarcana.common.talent.TalentConfig.getFlameWallFireSeconds());
                    }
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0f, 0.7f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cMuralha de Chamas projetada!"), true);
            }
            case "caosarcana:dardo_de_fogo" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double maxDist = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 32.0;
                Vec3 end = start.add(look.scale(maxDist));
                var blockHit = level.clip(new net.minecraft.world.level.ClipContext(start, end,
                        net.minecraft.world.level.ClipContext.Block.COLLIDER,
                        net.minecraft.world.level.ClipContext.Fluid.NONE, player));
                double effective = blockHit.getType() != net.minecraft.world.phys.HitResult.Type.MISS ? start.distanceTo(blockHit.getLocation()) : maxDist;
                LivingEntity target = null;
                double best = effective * effective;
                for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, start.add(look.scale(effective))).inflate(0.6),
                        e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
                    var hit = candidate.getBoundingBox().inflate(0.15).clip(start, start.add(look.scale(effective)));
                    if (hit.isPresent() && start.distanceToSqr(hit.get()) < best) { best = start.distanceToSqr(hit.get()); target = candidate; }
                }
                Vec3 impact = target != null ? target.position() : start.add(look.scale(effective));
                level.sendParticles(ParticleTypes.FLAME, impact.x, impact.y + 0.5, impact.z, 18, 0.25, 0.25, 0.25, 0.04);
                if (target != null) { target.hurt(player.damageSources().inFire(), 6.0f); target.setSecondsOnFire(3); }
                level.playSound(null, impact.x, impact.y, impact.z, SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 1.5f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cDardo de Fogo lançado!"), true);
            }
            case "caosarcana:incinerar" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double maxDist = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 30.0;
                LivingEntity target = null;
                for (double d = 1.0; d <= maxDist && target == null; d += 0.75) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 8, 0.2, 0.2, 0.2, 0.03);
                    List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(0.8), e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                    if (!hits.isEmpty()) target = hits.get(0);
                }
                if (target != null) { target.hurt(player.damageSources().inFire(), 14.0f); target.setSecondsOnFire(8); }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_HURT, SoundSource.PLAYERS, 1.0f, 0.6f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cIncinerar canalizado!"), true);
            }
            case "caosarcana:tempestade_de_fogo" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 8.0);
                List<LivingEntity> enemies = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                for (LivingEntity enemy : enemies) { enemy.hurt(player.damageSources().inFire(), 12.0f); enemy.setSecondsOnFire(6); }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 100, 4.0, 1.2, 4.0, 0.1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.7f);
                player.displayClientMessage(Component.literal("§6[Piromancia] §cTempestade de Fogo desencadeada!"), true);
            }
            case "caosarcana:dardo_de_gelo" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double range = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 28.0;
                Vec3 end = start.add(look.scale(range));
                LivingEntity target = null;
                for (double d = 0.8; d <= range && target == null; d += 0.8) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 5, 0.12, 0.12, 0.12, 0.02);
                    List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(0.65), e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                    if (!hits.isEmpty()) target = hits.get(0);
                }
                if (target != null) { target.hurt(player.damageSources().magic(), 6.0f); target.setTicksFrozen(100); target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1)); }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.6f);
                player.displayClientMessage(Component.literal("§b[Criomancia] §fDardo de Gelo lançado!"), true);
            }
            case "caosarcana:espiga_de_gelo" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double range = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 24.0;
                LivingEntity target = null;
                for (double d = 0.8; d <= range && target == null; d += 0.8) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 8, 0.18, 0.18, 0.18, 0.02);
                    List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(0.8), e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                    if (!hits.isEmpty()) target = hits.get(0);
                }
                if (target != null) { target.hurt(player.damageSources().magic(), 10.0f); target.setTicksFrozen(180); target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3)); }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§b[Criomancia] §fEspiga de Gelo perfurou a linha!"), true);
            }
            case "caosarcana:nevasca" -> {
                double radius = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 8.0;
                for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
                    enemy.hurt(player.damageSources().magic(), 9.0f);
                    enemy.setTicksFrozen(220);
                    enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2));
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0, player.getZ(), 120, radius * 0.5, 1.5, radius * 0.5, 0.03);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§b[Criomancia] §fNevasca congelante invocada!"), true);
            }
            case "caosarcana:faisca_eletrica" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double range = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 24.0;
                LivingEntity target = null;
                for (double d = 0.8; d <= range && target == null; d += 0.8) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 6, 0.12, 0.12, 0.12, 0.03);
                    List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(0.65), e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                    if (!hits.isEmpty()) target = hits.get(0);
                }
                if (target != null) { target.hurt(player.damageSources().lightningBolt(), 5.0f); target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0)); }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 1.8f);
                player.displayClientMessage(Component.literal("§e[Eletromancia] §fFaísca Elétrica descarregada!"), true);
            }
            case "caosarcana:corrente_de_relampago" -> {
                Vec3 start = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                double range = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 20.0;
                LivingEntity primary = null;
                for (double d = 0.8; d <= range && primary == null; d += 0.8) {
                    Vec3 point = start.add(look.scale(d));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z, 8, 0.15, 0.15, 0.15, 0.03);
                    List<LivingEntity> hits = level.getEntitiesOfClass(LivingEntity.class, new AABB(point, point).inflate(0.75), e -> e != player && e.isAlive() && !e.isAlliedTo(player));
                    if (!hits.isEmpty()) primary = hits.get(0);
                }
                if (primary != null) {
                    final LivingEntity chainTarget = primary;
                    chainTarget.hurt(player.damageSources().lightningBolt(), 8.0f);
                    for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, chainTarget.getBoundingBox().inflate(4.0), e -> e != player && e != chainTarget && e.isAlive() && !e.isAlliedTo(player))) {
                        nearby.hurt(player.damageSources().lightningBolt(), 4.0f);
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, nearby.getX(), nearby.getY() + 0.8, nearby.getZ(), 12, 0.2, 0.3, 0.2, 0.05);
                    }
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.9f, 1.2f);
                player.displayClientMessage(Component.literal("§e[Eletromancia] §fCorrente de Relâmpago encadeada!"), true);
            }
            case "caosarcana:tempestade_de_raios" -> {
                double radius = spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 10.0;
                for (LivingEntity enemy : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive() && !e.isAlliedTo(player))) {
                    enemy.hurt(player.damageSources().lightningBolt(), 12.0f);
                    enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 1));
                    level.sendParticles(ParticleTypes.ELECTRIC_SPARK, enemy.getX(), enemy.getY() + 1.0, enemy.getZ(), 20, 0.25, 0.5, 0.25, 0.08);
                }
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 100, radius * 0.5, 1.2, radius * 0.5, 0.08);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0f, 0.8f);
                player.displayClientMessage(Component.literal("§e[Eletromancia] §fTempestade de Raios invocada!"), true);
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

            case "caosarcana:cura_basica" -> {
                player.heal(4.0f);
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.3, 0.4, 0.3, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.6f);
                player.displayClientMessage(Component.literal("§a[Restauração] §fCura Básica restaurou sua vitalidade!"), true);
            }
            case "caosarcana:cura_rapida" -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 18, 0.4, 0.5, 0.4, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.4f);
                player.displayClientMessage(Component.literal("§a[Restauração] §fCura Rápida aplicada por 6s!"), true);
            }
            case "caosarcana:barreira_menor" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 160, 0));
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 25, 1.0, 1.0, 1.0, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.8f, 1.3f);
                player.displayClientMessage(Component.literal("§a[Restauração] §fBarreira Menor protegeu você por 8s!"), true);
            }
            case "caosarcana:armadura_de_pedra" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 900, 0));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 900, 0));
                level.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.5, 0.8, 0.5, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_IRON, SoundSource.PLAYERS, 1.0f, 0.9f);
                player.displayClientMessage(Component.literal("§2[Alteração] §fArmadura de Pedra reforçou seu corpo por 45s!"), true);
            }
            case "caosarcana:luz_arcana" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 1200, 0));
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.5, player.getZ(), 30, 0.5, 0.8, 0.5, 0.06);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.6f);
                player.displayClientMessage(Component.literal("§e[Alteração] §fLuz Arcana revelou o caminho por 60s!"), true);
            }
            case "caosarcana:respiracao_aquatica" -> {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 1200, 0));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
                level.sendParticles(ParticleTypes.BUBBLE, player.getX(), player.getY() + 1.0, player.getZ(), 25, 0.4, 0.5, 0.4, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_SPLASH, SoundSource.PLAYERS, 0.8f, 1.2f);
                player.displayClientMessage(Component.literal("§b[Alteração] §fRespiração Aquática ativa por 60s!"), true);
            }
            case "caosarcana:machado_vinculado" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 1));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 360, 0));
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 22, 0.3, 0.5, 0.3, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 0.8f, 1.1f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §fMachado Vinculado fortaleceu seus golpes por 18s!"), true);
            }
            case "caosarcana:arco_vinculado" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 360, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 360, 1));
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), 25, 0.4, 0.6, 0.4, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 0.9f, 1.3f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §fArco Vinculado guiou seus disparos por 18s!"), true);
            }
            case "caosarcana:reanimar_morto_vivo" -> {
                net.minecraft.world.entity.monster.Zombie zombie = EntityType.ZOMBIE.create(level);
                if (zombie != null) {
                    zombie.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0f);
                    zombie.setPersistenceRequired();
                    zombie.setCustomName(Component.literal("Servo Reanimado"));
                    zombie.setCustomNameVisible(true);
                    level.addFreshEntity(zombie);
                }
                level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 0.5, player.getZ(), 30, 0.7, 0.5, 0.7, 0.06);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ZOMBIE_AMBIENT, SoundSource.PLAYERS, 0.9f, 0.7f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §fUm Servo Reanimado surgiu ao seu lado!"), true);
            }
            case "caosarcana:clarividencia" -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.5, player.getZ(), 30, 1.5, 1.0, 1.5, 0.05);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.8f, 1.5f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dClarividência revelou o caminho por 30s!"), true);
            }
            case "caosarcana:silencio" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 300, 0));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 0));
                level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 22, 0.4, 0.5, 0.4, 0.03);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.3f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dSilêncio encobriu sua presença por 15s!"), true);
            }
            case "caosarcana:furia" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 10.0);
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area, Mob::isAlive);
                for (Mob mob : mobs) { mob.setTarget(player); mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0)); }
                level.sendParticles(ParticleTypes.ANGRY_VILLAGER, player.getX(), player.getY() + 1.2, player.getZ(), 25, 2.5, 0.8, 2.5, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.RAVAGER_ROAR, SoundSource.PLAYERS, 0.8f, 1.4f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dFúria provocou " + mobs.size() + " criatura(s) próximas!"), true);
            }
            case "caosarcana:calma" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 8.0);
                List<Mob> mobs = level.getEntitiesOfClass(Mob.class, area, Mob::isAlive);
                for (Mob mob : mobs) {
                    mob.setTarget(null);
                    mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
                }
                level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.2, player.getZ(), 18, 2.0, 0.8, 2.0, 0.03);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.PLAYERS, 0.8f, 1.0f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dCalma pacificou " + mobs.size() + " criatura(s) próximas!"), true);
            }
            case "caosarcana:invisibilidade" -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 600, 0));
                level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.4, 0.7, 0.4, 0.03);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.0f);
                player.displayClientMessage(Component.literal("§5[Ilusão] §dInvisibilidade ativa por 30s!"), true);
            }
            case "caosarcana:circulo_de_protecao" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 6.0);
                List<LivingEntity> undead = level.getEntitiesOfClass(LivingEntity.class, area,
                        e -> e.isAlive() && e.getMobType() == net.minecraft.world.entity.MobType.UNDEAD);
                for (LivingEntity entity : undead) {
                    entity.hurt(player.damageSources().magic(), 4.0f);
                    Vec3 push = entity.position().subtract(player.position()).normalize().scale(0.8);
                    entity.push(push.x, 0.35, push.z);
                }
                level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 0.2, player.getZ(), 36, 3.0, 0.1, 3.0, 0.02);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.2f);
                player.displayClientMessage(Component.literal("§e[Restauração] §fCírculo de Proteção repeliu " + undead.size() + " morto(s)-vivo(s)!"), true);
            }
            case "caosarcana:barreira_maior" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 400, 1));
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.8, 1.0, 0.8, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 0.9f);
                player.displayClientMessage(Component.literal("§e[Restauração] §fBarreira Maior ativa por 20s!"), true);
            }
            case "caosarcana:armadura_de_carvalho" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 320, 0));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 320, 0));
                level.sendParticles(ParticleTypes.COMPOSTER, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.5, 0.8, 0.5, 0.03);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WOOD_PLACE, SoundSource.PLAYERS, 0.8f, 0.8f);
                player.displayClientMessage(Component.literal("§2[Alteração] §fArmadura de Carvalho fortaleceu seu corpo por 16s!"), true);
            }
            case "caosarcana:armadura_de_ferro" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
                level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.6, 0.8, 0.6, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.4f, 1.5f);
                player.displayClientMessage(Component.literal("§2[Alteração] §fArmadura de Ferro ativa por 30s!"), true);
            }
            case "caosarcana:armadura_de_ebano" -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 800, 2));
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 800, 2));
                level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.7, 1.0, 0.7, 0.06);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARMOR_EQUIP_NETHERITE, SoundSource.PLAYERS, 0.8f, 0.8f);
                player.displayClientMessage(Component.literal("§2[Alteração] §fArmadura de Ébano ativa por 40s!"), true);
            }
            case "caosarcana:telecinese" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 16.0);
                LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive()).stream()
                        .min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
                if (target != null) {
                    Vec3 push = target.position().subtract(player.position()).normalize().scale(1.2);
                    target.push(push.x, 0.55, push.z);
                    target.hurtMarked = true;
                }
                level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.0, player.getZ(), 30, 1.0, 0.8, 1.0, 0.05);
                player.displayClientMessage(Component.literal("§2[Alteração] §fTelecinese " + (target == null ? "não encontrou alvo." : "arremessou " + target.getName().getString() + "!")), true);
            }
            case "caosarcana:paralisia_de_toque" -> {
                AABB area = player.getBoundingBox().inflate(spell.maxDistanceBlocks() > 0 ? spell.maxDistanceBlocks() : 8.0);
                LivingEntity target = level.getEntitiesOfClass(LivingEntity.class, area, e -> e != player && e.isAlive()).stream()
                        .min(java.util.Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
                if (target != null) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 10));
                    target.setDeltaMovement(Vec3.ZERO);
                }
                level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 26, 0.8, 0.8, 0.8, 0.04);
                player.displayClientMessage(Component.literal("§2[Alteração] §fParalisia " + (target == null ? "não encontrou alvo." : "imobilizou " + target.getName().getString() + " por 5s!")), true);
            }
            case "caosarcana:invocar_atronach_de_chamas" -> {
                Mob summon = EntityType.BLAZE.create(level);
                if (summon != null) {
                    summon.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0f);
                    summon.setPersistenceRequired();
                    summon.setCustomName(Component.literal("Atronach de Chamas"));
                    summon.setCustomNameVisible(true);
                    level.addFreshEntity(summon);
                }
                level.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.8, player.getZ(), 35, 0.8, 0.7, 0.8, 0.06);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.8f, 0.8f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §fUm Atronach de Chamas foi invocado por 60s!"), true);
            }
            case "caosarcana:invocar_atronach_de_gelo" -> {
                Mob summon = EntityType.STRAY.create(level);
                if (summon != null) {
                    summon.moveTo(player.getX() + 1.5, player.getY(), player.getZ() + 1.5, player.getYRot(), 0.0f);
                    summon.setPersistenceRequired();
                    summon.setCustomName(Component.literal("Atronach de Gelo"));
                    summon.setCustomNameVisible(true);
                    level.addFreshEntity(summon);
                }
                level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.8, player.getZ(), 35, 0.8, 0.7, 0.8, 0.04);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.STRAY_AMBIENT, SoundSource.PLAYERS, 0.8f, 0.8f);
                player.displayClientMessage(Component.literal("§9[Conjuração] §fUm Atronach de Gelo foi invocado por 60s!"), true);
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
