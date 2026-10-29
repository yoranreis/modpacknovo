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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Tragador do Abismo — Predador abissal do Recife de Coral Abissal.
 * - Sucção Abissal: Puxa o jogador em direção à sua bocarra circular com uma corrente de água.
 * - Investida Oculta: Move-se com extrema rapidez em áreas escuras ou na água, causando dano perfurante.
 */
public final class TragadorDoAbismoEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_SUCTION_ACTIVE =
            SynchedEntityData.defineId(TragadorDoAbismoEntity.class, EntityDataSerializers.BOOLEAN);

    private int suctionCooldown = 50;
    private int suctionTicks = 0;
    private int lungeCooldown = 45;

    public TragadorDoAbismoEntity(EntityType<? extends TragadorDoAbismoEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 12;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.6D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.35D)
                .add(Attributes.FOLLOW_RANGE, 26.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SUCTION_ACTIVE, false);
    }

    public boolean isSuctionActive() {
        return this.entityData.get(DATA_SUCTION_ACTIVE);
    }

    private void setSuctionActive(boolean active) {
        this.entityData.set(DATA_SUCTION_ACTIVE, active);
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    private boolean isInAbyssalConditions() {
        return this.isInWaterOrBubble() || this.level().getMaxLocalRawBrightness(this.blockPosition()) <= 7;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        this.getY() + 0.5D + this.random.nextDouble() * 1.1D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        0.0D, 0.02D, 0.0D
                );
            }
            if (this.isSuctionActive()) {
                for (int i = 0; i < 3; i++) {
                    double angle = (this.tickCount * 0.35D) + i * (Math.PI * 2.0D / 3.0D);
                    double ox = Math.cos(angle) * 1.4D;
                    double oz = Math.sin(angle) * 1.4D;
                    this.level().addParticle(
                            ParticleTypes.NAUTILUS,
                            this.getX() + ox,
                            this.getY() + 1.15D,
                            this.getZ() + oz,
                            -ox * 0.15D, 0.02D, -oz * 0.15D
                    );
                }
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.suctionCooldown > 0) this.suctionCooldown--;
        if (this.lungeCooldown > 0) this.lungeCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            if (this.suctionTicks > 0) {
                this.suctionTicks = 0;
                this.setSuctionActive(false);
            }
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // 1. Sucção Abissal ativa: puxa o alvo em direção à bocarra circular
        if (this.suctionTicks > 0) {
            this.suctionTicks--;
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);

            Vec3 pullVec = this.position().add(0.0D, 0.5D, 0.0D).subtract(target.position());
            double dist = pullVec.length();
            if (dist > 1.1D && dist <= 11.0D && this.hasLineOfSight(target)) {
                Vec3 pullStep = pullVec.normalize().scale(0.13D);
                target.setDeltaMovement(target.getDeltaMovement().add(pullStep.x, 0.02D, pullStep.z));
                target.hurtMarked = true;

                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE_POP,
                        target.getX(), target.getY() + 0.8D, target.getZ(),
                        3, 0.25D, 0.25D, 0.25D, 0.03D
                );
            }

            if (this.suctionTicks == 0) {
                this.setSuctionActive(false);
            }
            return;
        }

        // Inicia Sucção Abissal quando o alvo está a média distância
        if (this.suctionCooldown == 0 && distSqr >= 6.25D && distSqr <= 100.0D && this.hasLineOfSight(target)) {
            this.suctionCooldown = 110;
            this.suctionTicks = 28;
            this.setSuctionActive(true);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.2F, 0.65F);
            return;
        }

        // 2. Investida Oculta: bote veloz em áreas escuras ou submersas
        if (this.lungeCooldown == 0 && isInAbyssalConditions() && distSqr >= 9.0D && distSqr <= 81.0D && this.hasLineOfSight(target)) {
            this.lungeCooldown = 75;
            Vec3 dash = target.position().subtract(this.position()).normalize().scale(0.85D);
            this.setDeltaMovement(dash.x, Math.max(0.22D, dash.y * 0.4D), dash.z);
            this.hurtMarked = true;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.DROWNED_SHOOT, SoundSource.HOSTILE, 1.1F, 0.75F);
            serverLevel.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    this.getX(), this.getY() + 0.9D, this.getZ(),
                    10, 0.3D, 0.4D, 0.3D, 0.04D
            );
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity victim) {
            // Dano perfurante abissal adicional que atravessa parte da armadura
            float bonusPiercing = isInAbyssalConditions() ? 3.5F : 2.0F;
            victim.hurt(this.damageSources().indirectMagic(this, this), bonusPiercing);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.GLOW,
                        victim.getX(), victim.getY() + 1.0D, victim.getZ(),
                        8, 0.25D, 0.3D, 0.25D, 0.03D
                );
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.PRISMARINE_SHARD, 1 + this.random.nextInt(2 + looting));
        if (this.random.nextFloat() < 0.45F) {
            this.spawnAtLocation(Items.GLOW_INK_SAC, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.DROWNED_AMBIENT_WATER;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GUARDIAN_DEATH;
    }
}
