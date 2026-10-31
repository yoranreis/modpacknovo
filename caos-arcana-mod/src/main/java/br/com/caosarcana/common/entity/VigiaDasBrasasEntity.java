package br.com.caosarcana.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;

/** A fire-wreathed sentinel that guards the Ashlands with ember shots. */
public final class VigiaDasBrasasEntity extends TerrasDaCinzaMob implements RangedAttackMob {
    public VigiaDasBrasasEntity(EntityType<? extends VigiaDasBrasasEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 1.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 35, 55, 14.0F));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.65D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double dx = target.getX() - getX();
        double dy = target.getY(0.5D) - getY(0.5D);
        double dz = target.getZ() - getZ();
        SmallFireball ember = new SmallFireball(level(), this, dx, dy, dz);
        ember.setPos(getX(), getY(0.72D), getZ());
        serverLevel.addFreshEntity(ember);
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.BLAZE_SHOOT,
                SoundSource.HOSTILE, 0.65F, 0.75F + getRandom().nextFloat() * 0.15F);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide) {
            return;
        }

        if (getRandom().nextInt(2) == 0) {
            double x = getX() + (getRandom().nextDouble() - 0.5D) * 0.7D;
            double y = getY() + 0.25D + getRandom().nextDouble() * 1.25D;
            double z = getZ() + (getRandom().nextDouble() - 0.5D) * 0.7D;
            level().addParticle(net.minecraft.core.particles.ParticleTypes.SMOKE,
                    x, y, z, 0.0D, 0.012D, 0.0D);
            if (getRandom().nextInt(3) == 0) {
                level().addParticle(net.minecraft.core.particles.ParticleTypes.FLAME,
                        x, y, z, 0.0D, 0.018D, 0.0D);
            }
        }
    }
}
