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
    private static final int ATTACK_COOLDOWN_TICKS = 200; // 10 segundos
    private int attackCooldown = 40;
    private int windupTicks = 0;

    public EspiritoNevoaEntity(EntityType<? extends EspiritoNevoaEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false; // Tem física, mas não gravidade (flutua)
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 60, 16.0F));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
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
        this.setNoGravity(true); // Garante que flutue sempre no ar

        // Movimento de flutuação vertical suave
        Vec3 velocity = this.getDeltaMovement();
        if (!this.onGround() && velocity.y < 0.0D) {
            this.setDeltaMovement(velocity.x, velocity.y * 0.6D, velocity.z);
        }

        if (this.level().isClientSide) {
            // Partículas de fumaça e brilho verde espectral ao redor
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 0.5D + this.random.nextDouble() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.01D, 0.0D);
            }
            return;
        }

        if (this.attackCooldown > 0) this.attackCooldown--;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            // Mantém-se pairando um pouco acima do alvo / altura moderada
            double targetHoverY = target.getY() + 1.5D;
            if (this.getY() < targetHoverY) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.03D, 0.0D));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - this.getY(0.65D);
        double dz = target.getZ() - this.getZ();

        EspiritoNevoaProjectile projectile = new EspiritoNevoaProjectile(this.level(), this);
        projectile.setPos(this.getX(), this.getY(0.65D), this.getZ());
        projectile.shoot(dx, dy, dz, 1.15F, 1.0F);
        serverLevel.addFreshEntity(projectile);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.0F, 0.9F);
    }
}
