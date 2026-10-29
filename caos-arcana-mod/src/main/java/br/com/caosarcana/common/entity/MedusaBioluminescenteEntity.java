package br.com.caosarcana.common.entity;

import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Medusa Bioluminescente — Água-viva flutuante translúcida do Recife de Coral Abissal.
 * - Pulso Eletromagnético: Choque em área que aplica lentidão e drena o oxigênio subaquático.
 * - Fio Paralisante: Ataque à distância que prende o jogador no lugar por breves segundos.
 */
public final class MedusaBioluminescenteEntity extends PathfinderMob {
    private static final double MAX_HEIGHT_ABOVE_GROUND = 4.2D;

    private int empPulseCooldown = 55;
    private int paralyzingThreadCooldown = 70;

    public MedusaBioluminescenteEntity(EntityType<? extends MedusaBioluminescenteEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 18, true);
        this.setNoGravity(true);
        this.xpReward = 10;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FLYING_SPEED, 0.36D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ARMOR, 1.0D);
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
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 14.0F));
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
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    private double getGroundY() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        int minY = Math.max(this.level().getMinBuildHeight(), this.getBlockY() - 24);
        for (int y = this.getBlockY(); y >= minY; y--) {
            pos.setY(y);
            if (!this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos).blocksMotion()) {
                return y + 1.0D;
            }
        }
        return this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.blockPosition()).getY();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);

        if (this.level().isClientSide) {
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(
                        ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.3D + this.random.nextDouble() * 1.1D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.02D, 0.0D
                );
            }
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(
                        ParticleTypes.ELECTRIC_SPARK,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.4D + this.random.nextDouble() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D, -0.01D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.empPulseCooldown > 0) this.empPulseCooldown--;
        if (this.paralyzingThreadCooldown > 0) this.paralyzingThreadCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double heightDiff = this.getY() - target.getY();
            if (heightDiff > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.14D, this.getDeltaMovement().z);
            } else if (heightDiff < 0.8D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.05D, this.getDeltaMovement().z);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * 0.8D, this.getDeltaMovement().z);
            }

            double distSqr = this.distanceToSqr(target);
            if (distSqr > 49.0D) {
                this.getNavigation().moveTo(target, 1.0D);
            }

            // 1. Pulso Eletromagnético em área: desacelera e drena oxigênio
            if (this.empPulseCooldown == 0 && distSqr <= 30.25D) {
                this.empPulseCooldown = 95;
                triggerEmpPulse(serverLevel);
                return;
            }

            // 2. Fio Paralisante à distância: prende o jogador no lugar por breves segundos
            if (this.paralyzingThreadCooldown == 0 && distSqr >= 9.0D && distSqr <= 169.0D && this.hasLineOfSight(target)) {
                this.paralyzingThreadCooldown = 85;
                fireParalyzingThread(serverLevel, target);
            }
        } else {
            double heightAboveGround = this.getY() - getGroundY();
            if (heightAboveGround > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.14D, this.getDeltaMovement().z);
            } else if (heightAboveGround < 1.3D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.04D, this.getDeltaMovement().z);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * 0.75D, this.getDeltaMovement().z);
            }
        }
    }

    private void triggerEmpPulse(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, 1.25F, 1.35F);
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                this.getX(), this.getY() + 0.9D, this.getZ(),
                40, 1.8D, 1.1D, 1.8D, 0.08D
        );
        serverLevel.sendParticles(
                ParticleTypes.GLOW,
                this.getX(), this.getY() + 0.9D, this.getZ(),
                24, 1.6D, 0.9D, 1.6D, 0.05D
        );

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(5.5D),
                e -> e != this && !(e instanceof MedusaBioluminescenteEntity) && e.isAlive())) {
            victim.hurt(this.damageSources().indirectMagic(this, this), 4.5F);
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 75, 1), this);
            // Drena oxigênio rapidamente
            victim.setAirSupply(Math.max(-20, victim.getAirSupply() - 90));
        }
    }

    private void fireParalyzingThread(ServerLevel serverLevel, LivingEntity target) {
        Vec3 start = this.position().add(0.0D, 0.55D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
        Vec3 diff = end.subtract(start);
        int steps = Math.max(6, (int) (diff.length() * 3.0D));
        for (int i = 0; i <= steps; i++) {
            Vec3 pt = start.add(diff.scale((double) i / (double) steps));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pt.x, pt.y, pt.z, 1, 0.03D, 0.03D, 0.03D, 0.0D);
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
            }
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GLOW_SQUID_SQUIRT, SoundSource.HOSTILE, 1.2F, 1.4F);

        target.hurt(this.damageSources().indirectMagic(this, this), 3.5F);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 4), this);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hurtMarked = true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.GLOW_INK_SAC, 1 + this.random.nextInt(2 + looting));
        if (this.random.nextFloat() < 0.4F) {
            this.spawnAtLocation(Items.PRISMARINE_CRYSTALS, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.GLOW_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.GLOW_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GLOW_SQUID_DEATH;
    }
}
