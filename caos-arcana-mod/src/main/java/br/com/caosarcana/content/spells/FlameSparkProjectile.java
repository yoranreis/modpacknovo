package br.com.caosarcana.content.spells;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * Projétil próprio do Caos Arcana para a magia Centelha Ígnea.
 * Encerrado estritamente na Server Thread no primeiro destes eventos:
 * 1. Impacto com entidade ou bloco (comportamento herdado com descarte imediato).
 * 2. 60 ticks de tempo de voo (3 segundos).
 * 3. 40 blocos de deslocamento acumulado desde a origem do disparo.
 */
public class FlameSparkProjectile extends SmallFireball {
    // Parâmetros provisórios de balanceamento
    public static final int MAX_LIFETIME_TICKS = 60;
    public static final double MAX_DISTANCE_BLOCKS = 40.0;
    public static final float BASE_DAMAGE = 5.0f;
    public static final int FIRE_SECONDS = 5;

    private Vec3 spawnPosition;
    private int flightTicks = 0;

    public FlameSparkProjectile(EntityType<? extends SmallFireball> entityType, Level level) {
        super(entityType, level);
        this.spawnPosition = this.position();
    }

    public FlameSparkProjectile(Level level, LivingEntity shooter, double xPower, double yPower, double zPower) {
        super(level, shooter, xPower, yPower, zPower);
        this.spawnPosition = shooter.position();
    }

    public FlameSparkProjectile(Level level, double x, double y, double z, double xPower, double yPower, double zPower) {
        super(level, x, y, z, xPower, yPower, zPower);
        this.spawnPosition = new Vec3(x, y, z);
    }

    public void setSpawnPosition(Vec3 pos) {
        this.spawnPosition = pos;
    }

    public Vec3 getSpawnPosition() {
        return this.spawnPosition;
    }

    public int getFlightTicks() {
        return this.flightTicks;
    }

    public double getDistanceFromSpawn() {
        if (this.spawnPosition == null) {
            return 0.0;
        }
        return this.position().distanceTo(this.spawnPosition);
    }

    public boolean isExpiredByTime() {
        return this.flightTicks >= MAX_LIFETIME_TICKS;
    }

    public boolean isExpiredByDistance() {
        return getDistanceFromSpawn() >= MAX_DISTANCE_BLOCKS;
    }

    /**
     * Aplica exatamente a mesma lógica que SmallFireball.onHitEntity executa nativamente:
     * - 5 de dano (BASE_DAMAGE)
     * - 5 segundos de fogo (FIRE_SECONDS)
     * - DamageSource fireball(this, owner)
     * - Reversão dos ticks de fogo caso o alvo recuse o dano (ex: imunidade a fogo)
     * - Efeitos de encantamento se o atacante for LivingEntity
     */
    public boolean applySpellImpact(Entity target) {
        if (target == null) return false;
        Entity owner = this.getOwner();
        int oldFireTicks = target.getRemainingFireTicks();

        boolean combustionFocus = false;
        boolean kineticFocus = false;
        if (owner instanceof net.minecraft.world.entity.player.Player player) {
            combustionFocus = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.hasCombustionFocus(player);
            kineticFocus = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.hasKineticFocus(player);
        }

        int fireSeconds = combustionFocus ? br.com.caosarcana.common.talent.TalentConfig.getCombustionFireSeconds() : FIRE_SECONDS;

        if (!kineticFocus) {
            target.setSecondsOnFire(fireSeconds);
        }

        DamageSource source = this.damageSources().fireball(this, owner);
        boolean hurt = owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                ? PyromancyProgressionHandler.applyPyromancyDamage(serverPlayer, target, source, BASE_DAMAGE)
                : target.hurt(source, BASE_DAMAGE);
        if (!hurt) {
            if (!kineticFocus) {
                target.setRemainingFireTicks(oldFireTicks);
            }
        } else {
            if (kineticFocus) {
                // Alternativa B: Substitui a combustão por impacto com empurrão
                Vec3 look = this.getDeltaMovement().normalize();
                if (look.lengthSqr() < 1.0e-4 && owner != null) {
                    look = owner.getLookAngle();
                }
                double knockback = br.com.caosarcana.common.talent.TalentConfig.getKineticKnockbackStrength();
                if (target instanceof LivingEntity livingTarget) {
                    livingTarget.knockback(knockback, -look.x, -look.z);
                } else {
                    target.push(look.x * knockback, 0.4, look.z * knockback);
                }
            }
            if (owner instanceof LivingEntity livingOwner) {
                this.doEnchantDamageEffects(livingOwner, target);
            }
        }
        return hurt;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (!super.canHitEntity(entity)) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner != null && owner.isAlliedTo(entity)) {
            return false;
        }
        return entity.isAlive();
    }

    private Entity findIntersectingEntity() {
        Vec3 pos = this.position();
        Vec3 delta = this.getDeltaMovement();
        Vec3 next = pos.add(delta);
        AABB checkArea = this.getBoundingBox().expandTowards(delta).inflate(0.35);

        List<Entity> list = this.level().getEntities(this, checkArea, this::canHitEntity);
        Entity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity e : list) {
            AABB box = e.getBoundingBox().inflate(0.15);
            boolean hit = box.contains(pos) || box.contains(next) || box.intersects(this.getBoundingBox());
            if (!hit) {
                Optional<Vec3> clip = box.clip(pos, next);
                hit = clip.isPresent();
            }
            if (hit) {
                double dist = e.distanceToSqr(this);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = e;
                }
            }
        }
        return closest;
    }

    @Override
    public void tick() {
        // Controle e encerramento estrito pelo lado do servidor
        if (!this.level().isClientSide()) {
            this.flightTicks++;

            if (this.spawnPosition == null) {
                this.spawnPosition = this.position();
            }

            // Encerrar no primeiro dos eventos: 60 ticks ou 40 blocos
            if (isExpiredByTime() || isExpiredByDistance()) {
                // Efeito sutil de dissipação ao expirar (sem dano, sem fogo e sem explosão)
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SMOKE,
                            this.getX(), this.getY(), this.getZ(),
                            6, 0.1, 0.1, 0.1, 0.02
                    );
                }
                // Remoção limpa; o servidor automaticamente envia pacote de remoção para os clientes
                this.discard();
                return;
            }

            // Verificação contínua de colisão por volume para impedir que o projétil passe através de entidades próximas
            Entity intersecting = findIntersectingEntity();
            if (intersecting != null) {
                applySpellImpact(intersecting);
                this.discard();
                return;
            }
        }

        super.tick();
    }
}
