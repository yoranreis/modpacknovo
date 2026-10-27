package br.com.caosarcana.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;

public final class CaoFumeganteEntity extends PathfinderMob {
    public CaoFumeganteEntity(EntityType<? extends CaoFumeganteEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.34D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25D, false));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.95D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            return;
        }

        boolean moving = getDeltaMovement().horizontalDistanceSqr() > 0.0025D;
        if (getRandom().nextInt(moving ? 4 : 8) == 0) {
            double x = getX() + (getRandom().nextDouble() - 0.5D) * getBbWidth() * 0.75D;
            double y = getY() + 0.2D + getRandom().nextDouble() * getBbHeight() * 0.7D;
            double z = getZ() + (getRandom().nextDouble() - 0.5D) * getBbWidth() * 0.75D;
            level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.015D, 0.0D);

            if (moving && getRandom().nextInt(3) == 0) {
                level().addParticle(ParticleTypes.ASH, x, y, z, 0.0D, 0.01D, 0.0D);
            }
        }

        if (getRandom().nextInt(28) == 0) {
            double x = getX() + (getRandom().nextDouble() - 0.5D) * getBbWidth() * 0.6D;
            double y = getY() + 0.35D + getRandom().nextDouble() * getBbHeight() * 0.5D;
            double z = getZ() + (getRandom().nextDouble() - 0.5D) * getBbWidth() * 0.6D;
            level().addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.01D, 0.0D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.LARGE_SMOKE,
                    getX(), getY() + 0.35D, getZ(),
                    5, 0.16D, 0.08D, 0.16D, 0.01D
            );
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    getX(), getY() + 0.35D, getZ(),
                    2, 0.1D, 0.08D, 0.1D, 0.01D
            );
        }
        return hit;
    }
}
