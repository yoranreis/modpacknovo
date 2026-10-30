package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Golem de Gelo — Tanque maciço da Muralha de Gelo.
 * - Aura de Arrepiar: Causa dano de congelamento passivo e lento a quem permanecer a menos de 3 blocos.
 * - Esmagamento Sísmico: Bate no chão quebrando gelo, lançando detritos e causando dano em área com repulsão.
 */
public final class GolemDeGeloEntity extends PathfinderMob {
    private int seismicSmashCooldown = 80;

    public GolemDeGeloEntity(EntityType<? extends GolemDeGeloEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 18;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 56.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 8.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FREEZING) || source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 1.4D,
                        this.getY() + 0.5D + this.random.nextDouble() * 1.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 1.4D,
                        0.0D, -0.02D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.seismicSmashCooldown > 0) {
            this.seismicSmashCooldown--;
        }

        // 1. Aura de Arrepiar: dano de congelamento passivo a quem permanecer a menos de 3 blocos
        if (this.tickCount % 20 == 0) {
            for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    this.getBoundingBox().inflate(3.0D),
                    e -> e != this && !(e instanceof GolemDeGeloEntity) && e.isAlive())) {
                nearby.setTicksFrozen(Math.min(nearby.getTicksRequiredToFreeze() + 60, nearby.getTicksFrozen() + 55));
                nearby.hurt(this.damageSources().freeze(), 1.5F);
                nearby.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this);
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        nearby.getX(), nearby.getY() + 1.0D, nearby.getZ(),
                        6, 0.25D, 0.35D, 0.25D, 0.02D
                );
            }
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        // 2. Esmagamento Sísmico: bate no chão quebrando gelo e lançando detritos com repulsão
        double distSqr = this.distanceToSqr(target);
        if (this.seismicSmashCooldown == 0 && distSqr <= 20.25D && this.onGround()) {
            this.seismicSmashCooldown = 115;
            performSeismicSmash(serverLevel);
        }
    }

    private void performSeismicSmash(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.1F, 1.25F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.4F, 0.65F);

        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                this.getX(), this.getY() + 0.2D, this.getZ(),
                48, 1.8D, 0.4D, 1.8D, 0.15D
        );
        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY() + 0.5D, this.getZ(),
                32, 1.8D, 0.5D, 1.8D, 0.08D
        );

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(4.5D),
                e -> e != this && !(e instanceof GolemDeGeloEntity) && e.isAlive())) {
            victim.hurt(this.damageSources().mobAttack(this), 8.0F);
            Vec3 knock = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.1D);
            victim.push(knock.x, 0.42D, knock.z);
            victim.hurtMarked = true;
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.PACKED_ICE, 2 + this.random.nextInt(3 + looting));
        if (this.random.nextFloat() < 0.45F) {
            this.spawnAtLocation(Items.BLUE_ICE, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }
}
