package br.com.caosarcana.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Víbora das Arenas — Mob Hostil / Emboscada do Deserto das Dunas Douradas.
 * - Emboscada Subterrânea: Oculta-se sob blocos de areia até o jogador aproximar-se a menos de 3 blocos.
 * - Mordida Paralisante: Aplica Lentidão IV + Veneno por 4 segundos (80 ticks).
 */
public final class ViboraDasArenasEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_BURROWED =
            SynchedEntityData.defineId(ViboraDasArenasEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double AMBUSH_TRIGGER_DIST_SQR = 9.0D; // 3 blocos
    private int idleTicksForBurrow = 0;
    private int burrowCooldown = 0;

    public ViboraDasArenasEntity(EntityType<? extends ViboraDasArenasEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 4.5D)
                .add(Attributes.FOLLOW_RANGE, 20.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BURROWED, false);
    }

    public boolean isBurrowed() {
        return this.entityData.get(DATA_BURROWED);
    }

    private void setBurrowedState(boolean burrowed) {
        this.entityData.set(DATA_BURROWED, burrowed);
        this.setInvisible(burrowed);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.75D) {
            @Override
            public boolean canUse() {
                return !isBurrowed() && super.canUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (isBurrowed() && this.getRandom().nextInt(6) == 0) {
                this.level().addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                        this.getY() + 0.08D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.7D,
                        0.0D, 0.0D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.burrowCooldown > 0) {
            this.burrowCooldown--;
        }

        Player nearestPlayer = serverLevel.getNearestPlayer(this, 16.0D);
        double playerDistSqr = nearestPlayer != null ? this.distanceToSqr(nearestPlayer) : Double.MAX_VALUE;

        if (isBurrowed()) {
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

            // Emerge em emboscada quando o jogador chega a menos de 3 blocos
            if (nearestPlayer != null && !nearestPlayer.isSpectator() && !nearestPlayer.isCreative()
                    && playerDistSqr <= AMBUSH_TRIGGER_DIST_SQR) {
                emergeAndStrike(serverLevel, nearestPlayer);
            } else if (!isOnSandOrDesertGround()) {
                setBurrowedState(false);
            }
            return;
        }

        // Entra em Emboscada Subterrânea se estiver sobre areia/arenito e sem jogador a menos de 5 blocos
        if (this.burrowCooldown == 0 && isOnSandOrDesertGround() && playerDistSqr > 25.0D
                && this.getDeltaMovement().horizontalDistanceSqr() < 0.002D) {
            this.idleTicksForBurrow++;
            if (this.idleTicksForBurrow >= 35) {
                setBurrowedState(true);
                this.setTarget(null);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        this.getX(), this.getY() + 0.15D, this.getZ(),
                        20, 0.4D, 0.1D, 0.4D, 0.06D
                );
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 0.9F, 0.85F);
            }
        } else {
            this.idleTicksForBurrow = 0;
        }
    }

    private boolean isOnSandOrDesertGround() {
        BlockPos below = this.blockPosition().below();
        BlockState state = this.level().getBlockState(below);
        return state.is(BlockTags.SAND)
                || state.is(Blocks.SANDSTONE)
                || state.is(Blocks.CUT_SANDSTONE)
                || state.is(Blocks.CHISELED_SANDSTONE)
                || state.is(Blocks.SMOOTH_SANDSTONE)
                || state.is(Blocks.RED_SANDSTONE)
                || state.is(BlockTags.TERRACOTTA);
    }

    private void emergeAndStrike(ServerLevel serverLevel, LivingEntity target) {
        setBurrowedState(false);
        this.idleTicksForBurrow = 0;
        this.burrowCooldown = 160;
        this.setTarget(target);

        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                this.getX(), this.getY() + 0.3D, this.getZ(),
                36, 0.45D, 0.25D, 0.45D, 0.12D
        );
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 1.2F, 0.75F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.1F, 1.35F);

        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        this.setDeltaMovement((dx / len) * 0.75D, 0.32D, (dz / len) * 0.75D);
        this.hasImpulse = true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isBurrowed() && this.level() instanceof ServerLevel serverLevel) {
            setBurrowedState(false);
            this.burrowCooldown = 140;
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY() + 0.25D, this.getZ(),
                    24, 0.4D, 0.2D, 0.4D, 0.1D
            );
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            // Mordida Paralisante: Lentidão IV (amp 3) + Veneno por 4 segundos (80 ticks)
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 3), this);
            livingTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 1.0F, 1.4F);
            }
        }
        return hit;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }
}
