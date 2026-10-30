package br.com.caosarcana.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Caçador de Gelo — Emboscador alpino da Muralha de Gelo.
 * - Camuflagem Alpina: Torna-se semitransparente e imóvel próximo a neve/gelo para emboscar.
 * - Investida Gélida: Avança rapidamente causando dano e aplicando Lentidão III por 3 segundos.
 */
public final class CacadorDeGeloEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_CAMOUFLAGED =
            SynchedEntityData.defineId(CacadorDeGeloEntity.class, EntityDataSerializers.BOOLEAN);

    private int glacialChargeCooldown = 50;
    private int camouflageRecheckTimer = 0;

    public CacadorDeGeloEntity(EntityType<? extends CacadorDeGeloEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 34.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 6.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CAMOUFLAGED, false);
    }

    public boolean isCamouflaged() {
        return this.entityData.get(DATA_CAMOUFLAGED);
    }

    private void setCamouflaged(boolean camouflaged) {
        this.entityData.set(DATA_CAMOUFLAGED, camouflaged);
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FREEZING)) {
            return false;
        }
        this.setCamouflaged(false);
        return super.hurt(source, amount);
    }

    private boolean isNearSnowOrIce() {
        BlockPos center = this.blockPosition();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockState state = this.level().getBlockState(center.offset(dx, dy, dz));
                    if (state.is(BlockTags.SNOW) || state.is(BlockTags.ICE)
                            || state.is(Blocks.SNOW_BLOCK) || state.is(Blocks.SNOW)
                            || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.ICE)) {
                        return true;
                    }
                }
            }
        }
        return this.level().getBiome(center).value().coldEnoughToSnow(center);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        this.getY() + 0.5D + this.random.nextDouble() * 1.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        0.0D, -0.01D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.glacialChargeCooldown > 0) this.glacialChargeCooldown--;
        if (this.camouflageRecheckTimer > 0) this.camouflageRecheckTimer--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            if (this.camouflageRecheckTimer == 0) {
                this.camouflageRecheckTimer = 15;
                boolean canHide = isNearSnowOrIce();
                this.setCamouflaged(canHide);
                if (canHide) {
                    this.getNavigation().stop();
                }
            }
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // Permanece camuflado e imóvel preparando emboscada enquanto o alvo está entre 7 e 18 blocos
        if (distSqr > 49.0D && distSqr <= 324.0D && isNearSnowOrIce()) {
            this.setCamouflaged(true);
            this.getNavigation().stop();
            this.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        // Investida Gélida: bote rápido que revela o caçador e aplica Lentidão III por 3s
        if (this.glacialChargeCooldown == 0 && distSqr >= 9.0D && distSqr <= 64.0D && this.hasLineOfSight(target)) {
            this.glacialChargeCooldown = 85;
            this.setCamouflaged(false);
            Vec3 dash = target.position().subtract(this.position()).normalize().scale(0.95D);
            this.setDeltaMovement(dash.x, 0.25D, dash.z);
            this.hurtMarked = true;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.HOSTILE, 1.2F, 1.3F);
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.0D, this.getZ(),
                    16, 0.35D, 0.4D, 0.35D, 0.06D
            );
        } else if (distSqr <= 49.0D) {
            this.setCamouflaged(false);
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        this.setCamouflaged(false);
        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity victim) {
            // Lentidão III (Amplifier 2) por 3 segundos (60 ticks) + Congelamento
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2), this);
            victim.setTicksFrozen(Math.min(victim.getTicksRequiredToFreeze() + 60, victim.getTicksFrozen() + 90));
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        victim.getX(), victim.getY() + 1.0D, victim.getZ(),
                        12, 0.3D, 0.4D, 0.3D, 0.04D
                );
            }
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.LEATHER, 1 + this.random.nextInt(2 + looting));
        this.spawnAtLocation(Items.ARROW, 1 + this.random.nextInt(3 + looting));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.STRAY_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRAY_DEATH;
    }
}
