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
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
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

/**
 * Fantasma do Gelo — Espectro gélido da Muralha de Gelo.
 * - Intangibilidade Espectral: Fica intangível e imune a dano físico comum durante o deslocamento.
 * - Toque do Congelamento: Ataque corpo a corpo que ignora parcialmente a armadura e aplica Enfraquecimento II e Congelamento Severo.
 */
public final class FantasmaDoGeloEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_INTANGIBLE =
            SynchedEntityData.defineId(FantasmaDoGeloEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double MAX_HEIGHT_ABOVE_GROUND = 4.0D;

    private int intangibleCooldown = 70;
    private int intangibleTicks = 0;

    public FantasmaDoGeloEntity(EntityType<? extends FantasmaDoGeloEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
        this.xpReward = 14;
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
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.FLYING_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 26.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_INTANGIBLE, false);
    }

    public boolean isIntangible() {
        return this.entityData.get(DATA_INTANGIBLE);
    }

    private void setIntangible(boolean intangible) {
        this.entityData.set(DATA_INTANGIBLE, intangible);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, true));
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
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_FREEZING)) {
            return false;
        }
        // Intangibilidade Espectral: imune a dano físico comum durante o deslocamento etéreo
        if (this.isIntangible() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.is(DamageTypeTags.IS_FIRE)) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 0.9F, 1.4F);
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        10, 0.35D, 0.45D, 0.35D, 0.03D
                );
            }
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
                        ParticleTypes.SNOWFLAKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.3D + this.random.nextDouble() * 1.4D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.01D, 0.0D
                );
            }
            if (this.isIntangible() && this.random.nextInt(2) == 0) {
                this.level().addParticle(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.6D + this.random.nextDouble() * 0.9D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.intangibleCooldown > 0) this.intangibleCooldown--;
        if (this.intangibleTicks > 0) {
            this.intangibleTicks--;
            this.noPhysics = true;
            if (this.intangibleTicks == 0) {
                this.noPhysics = false;
                this.setIntangible(false);
            }
        } else {
            this.noPhysics = false;
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double heightDiff = this.getY() - target.getY();
            if (heightDiff > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.14D, this.getDeltaMovement().z);
            } else if (heightDiff < 0.5D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.05D, this.getDeltaMovement().z);
            }

            double distSqr = this.distanceToSqr(target);
            if (this.intangibleCooldown == 0 && distSqr >= 9.0D && distSqr <= 144.0D) {
                this.intangibleCooldown = 110;
                this.intangibleTicks = 35;
                this.setIntangible(true);
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.VEX_CHARGE, SoundSource.HOSTILE, 1.1F, 0.75F);
            }
        } else {
            double heightAboveGround = this.getY() - getGroundY();
            if (heightAboveGround > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.14D, this.getDeltaMovement().z);
            } else if (heightAboveGround < 1.2D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.04D, this.getDeltaMovement().z);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        // Sai da intangibilidade no instante do ataque
        this.intangibleTicks = 0;
        this.noPhysics = false;
        this.setIntangible(false);

        boolean hurt = super.doHurtTarget(entity);
        if (hurt && entity instanceof LivingEntity victim) {
            // Toque do Congelamento: dano mágico gélido que ignora armadura + Enfraquecimento II + Congelamento Severo
            victim.hurt(this.damageSources().indirectMagic(this, this), 3.5F);
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 90, 1), this);
            victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 110);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.PRISMARINE_CRYSTALS, 1 + this.random.nextInt(2 + looting));
        if (this.random.nextFloat() < 0.4F) {
            this.spawnAtLocation(Items.BLUE_ICE, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.VEX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }
}
