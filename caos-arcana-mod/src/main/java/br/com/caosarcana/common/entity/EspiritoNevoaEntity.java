package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class EspiritoNevoaEntity extends PathfinderMob implements RangedAttackMob {
    private static final int ATTACK_COOLDOWN_TICKS = 140; // 7 segundos
    private int attackCooldown = 20;

    public EspiritoNevoaEntity(EntityType<? extends EspiritoNevoaEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 12.0D) // Alcance de visão restrito a 12 blocos
                .add(Attributes.ARMOR, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Ranged attack mantendo-se a curta distância (maxRange = 6.0F)
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 6.0F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.5D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);

        // Mantém-se voando a no máximo 2 a 4 blocos de altura do chão/alvo
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            double desiredY = target.getY() + 2.0D; // 2 blocos acima do jogador
            double currentY = this.getY();
            if (currentY < desiredY) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.04D, 0.0D));
            } else if (currentY > desiredY + 1.5D) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
            }

            // Se estiver muito longe (> 5 blocos horizontalmente), aproxima-se
            double distSqr = this.distanceToSqr(target.getX(), this.getY(), target.getZ());
            if (distSqr > 25.0D) {
                this.getNavigation().moveTo(target, 1.1D);
            }
        }

        if (this.level().isClientSide) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.3D + this.random.nextDouble() * 0.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.6D,
                        0.0D, 0.01D, 0.0D);
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getY(0.5D);
        double dz = target.getZ() - this.getZ();

        EspiritoNevoaProjectile projectile = new EspiritoNevoaProjectile(this.level(), this);
        projectile.setPos(this.getX(), this.getY(0.5D), this.getZ());
        projectile.shoot(dx, dy, dz, 1.0F, 0.5F);
        serverLevel.addFreshEntity(projectile);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 0.9F, 1.1F);
    }
}
