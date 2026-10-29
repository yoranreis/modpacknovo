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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.level.Level;

/**
 * Escaravelho Solar — Inimigo encouraçado do Deserto das Dunas Douradas.
 * Carapaça dourada resistente e investida solar telegrafada com chifres.
 */
public final class EscaravelhoSolarEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_CHARGING_SOLAR =
            SynchedEntityData.defineId(EscaravelhoSolarEntity.class, EntityDataSerializers.BOOLEAN);

    private int solarChargeCooldown = 70;
    private int solarChargeWindup = 0;
    private int lungeTicks = 0;
    private boolean hitDuringLunge = false;

    public EscaravelhoSolarEntity(EntityType<? extends EscaravelhoSolarEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 22.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE)) {
            return super.hurt(source, amount * 0.5F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.getRandom().nextInt(5) == 0) {
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

        // Fase 1: Windup telegrafado (carapaça brilhando antes da investida)
        if (this.solarChargeWindup > 0) {
            this.solarChargeWindup--;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            serverLevel.sendParticles(
                    ParticleTypes.WAX_ON,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    4, 0.35D, 0.25D, 0.35D, 0.05D
            );
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.45D, this.getZ(),
                    2, 0.25D, 0.15D, 0.25D, 0.02D
            );

            if (this.solarChargeWindup == 0) {
                this.setChargingSolar(false);
                launchSolarHornCharge(serverLevel, target);
            }
            return;
        }

        // Fase 2: Investida com os chifres dourados em andamento
        if (this.lungeTicks > 0) {
            this.lungeTicks--;
            if (!this.hitDuringLunge && this.getBoundingBox().inflate(0.3D).intersects(target.getBoundingBox())) {
                this.hitDuringLunge = true;
                target.hurt(this.damageSources().mobAttack(this), 6.5F);
                target.knockback(0.95D, this.getX() - target.getX(), this.getZ() - target.getZ());
                target.setSecondsOnFire(3);
                target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), this);
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        target.getX(), target.getY() + 0.6D, target.getZ(),
                        16, 0.35D, 0.35D, 0.35D, 0.06D
                );
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.7F, 1.6F);
            }
            return;
        }

        double distSqr = this.distanceToSqr(target);
        if (this.solarChargeCooldown == 0 && distSqr >= 9.0D && distSqr <= 64.0D && this.hasLineOfSight(target)) {
            this.solarChargeCooldown = 130;
            this.solarChargeWindup = 20; // 1.0s de telegrafia
            this.setChargingSolar(true);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 1.4F, 1.1F);
        }
    }

    private void launchSolarHornCharge(ServerLevel serverLevel, LivingEntity target) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        this.setDeltaMovement(
                (dx / len) * 0.95D,
                Math.max(0.18D, this.getDeltaMovement().y),
                (dz / len) * 0.95D
        );
        this.hasImpulse = true;
        this.lungeTicks = 10;
        this.hitDuringLunge = false;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.0F, 1.2F);
        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(), this.getY() + 0.35D, this.getZ(),
                14, 0.35D, 0.2D, 0.35D, 0.05D
        );
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.WAX_ON,
                    target.getX(), target.getY() + 0.5D, target.getZ(),
                    8, 0.25D, 0.25D, 0.25D, 0.04D
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
