package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Escaravelho Solar — Mob Hostil / Enxame do Deserto das Dunas Douradas.
 * - Reflexão Térmica: Reflete 15% de dano à distância/mágico sob luz solar direta.
 * - Investida em Bando: Ataca coletivamente em enxame com mordidas rápidas para desestabilizar o jogador.
 */
public final class EscaravelhoSolarEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_SOLAR =
            SynchedEntityData.defineId(EscaravelhoSolarEntity.class, EntityDataSerializers.BOOLEAN);

    private int solarChargeCooldown = 60;
    private int solarChargeWindup = 0;
    private int lungeTicks = 0;
    private boolean hitDuringLunge = false;

    public EscaravelhoSolarEntity(EntityType<? extends EscaravelhoSolarEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.31D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.45D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 6.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGING_SOLAR, false);
    }

    public boolean isChargingSolar() {
        return this.entityData.get(DATA_CHARGING_SOLAR);
    }

    private void setChargingSolar(boolean charging) {
        this.entityData.set(DATA_CHARGING_SOLAR, charging);
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Mordidas rápidas em enxame (attack interval reduzido para 14 ticks)
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            protected int getAttackInterval() {
                return 14;
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(EscaravelhoSolarEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean isExposedToDirectSunlight() {
        return this.level().isDay()
                && !this.level().isRaining()
                && this.level().canSeeSky(this.blockPosition());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurtResult = super.hurt(source, amount);
        if (hurtResult && !this.level().isClientSide && isExposedToDirectSunlight()) {
            boolean isRangedOrMagic = source.is(DamageTypeTags.IS_PROJECTILE)
                    || source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                    || source.is(DamageTypes.MAGIC)
                    || source.is(DamageTypes.INDIRECT_MAGIC)
                    || source.isIndirect();

            if (isRangedOrMagic && source.getEntity() instanceof LivingEntity attacker && attacker != this) {
                float reflected = amount * 0.15F;
                if (reflected > 0.05F) {
                    attacker.hurt(this.damageSources().thorns(this), reflected);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.WAX_ON,
                                this.getX(), this.getY() + 0.55D, this.getZ(),
                                12, 0.35D, 0.25D, 0.35D, 0.06D
                        );
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.HOSTILE, 1.0F, 1.35F);
                    }
                }
            }
        }
        return hurtResult;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (isExposedToDirectSunlight() && this.getRandom().nextInt(4) == 0) {
                this.level().addParticle(
                        ParticleTypes.WAX_ON,
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.9D,
                        this.getY() + 0.35D + this.getRandom().nextDouble() * 0.45D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.9D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.solarChargeCooldown > 0) this.solarChargeCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.solarChargeWindup = 0;
            this.lungeTicks = 0;
            this.setChargingSolar(false);
            return;
        }

        // Fase 1: Windup da Investida em Bando
        if (this.solarChargeWindup > 0) {
            this.solarChargeWindup--;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            serverLevel.sendParticles(
                    ParticleTypes.WAX_ON,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    3, 0.3D, 0.2D, 0.3D, 0.04D
            );

            if (this.solarChargeWindup == 0) {
                this.setChargingSolar(false);
                launchSwarmCharge(serverLevel, target);
            }
            return;
        }

        // Fase 2: Investida ativa
        if (this.lungeTicks > 0) {
            this.lungeTicks--;
            if (!this.hitDuringLunge && this.getBoundingBox().inflate(0.28D).intersects(target.getBoundingBox())) {
                this.hitDuringLunge = true;
                target.hurt(this.damageSources().mobAttack(this), 5.0F);
                target.knockback(0.65D, this.getX() - target.getX(), this.getZ() - target.getZ());
                serverLevel.sendParticles(
                        ParticleTypes.CRIT,
                        target.getX(), target.getY() + 0.5D, target.getZ(),
                        10, 0.3D, 0.3D, 0.3D, 0.05D
                );
            }
            return;
        }

        double distSqr = this.distanceToSqr(target);
        if (this.solarChargeCooldown == 0 && distSqr >= 6.0D && distSqr <= 49.0D && this.hasLineOfSight(target)) {
            triggerPackCharge(serverLevel, target);
        }
    }

    public void triggerPackCharge(ServerLevel serverLevel, LivingEntity target) {
        this.solarChargeCooldown = 110;
        this.solarChargeWindup = 14;
        this.setChargingSolar(true);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SILVERFISH_AMBIENT, SoundSource.HOSTILE, 1.1F, 0.85F);

        // Sincroniza outros Escaravelhos Solares próximos para Investida em Bando
        List<EscaravelhoSolarEntity> pack = serverLevel.getEntitiesOfClass(
                EscaravelhoSolarEntity.class,
                this.getBoundingBox().inflate(10.0D),
                other -> other != this && other.isAlive() && other.solarChargeWindup == 0 && other.lungeTicks == 0
        );
        for (EscaravelhoSolarEntity ally : pack) {
            if (ally.getTarget() == null) {
                ally.setTarget(target);
            }
            if (ally.solarChargeCooldown <= 40) {
                ally.solarChargeCooldown = 110;
                ally.solarChargeWindup = 14;
                ally.setChargingSolar(true);
            }
        }
    }

    private void launchSwarmCharge(ServerLevel serverLevel, LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        this.setDeltaMovement(
                (dx / len) * 0.88D,
                Math.max(0.16D, this.getDeltaMovement().y),
                (dz / len) * 0.88D
        );
        this.hasImpulse = true;
        this.lungeTicks = 9;
        this.hitDuringLunge = false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        int count = 1 + this.random.nextInt(2 + looting);
        this.spawnAtLocation(Items.GOLD_NUGGET, count);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WAX_ON,
                    target.getX(), target.getY() + 0.5D, target.getZ(),
                    6, 0.25D, 0.25D, 0.25D, 0.04D
            );
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SHULKER_HURT_CLOSED;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }
}
