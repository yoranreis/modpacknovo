package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.level.Level;

public final class EspiritoNevoaEntity extends PathfinderMob {
    private static final int MIST_COOLDOWN_TICKS = 200; // 10 segundos
    private static final double MIST_RANGE_SQR = 16.0D * 16.0D; // 16 blocos

    private int mistCooldown = 40;
    private int mistWindup = 0;

    public EspiritoNevoaEntity(EntityType<? extends EspiritoNevoaEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Flutua levemente pela névoa
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 2.5D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 1.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, false));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.7D));
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

        if (this.level().isClientSide) {
            // Partículas constantes de névoa ao redor do espírito
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + this.random.nextDouble() * 1.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.02D, 0.0D);
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.mistCooldown > 0) this.mistCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.mistWindup = 0;
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // Fase de telegrafia (windup de 1.5s / 30 ticks com fumaça adensando)
        if (this.mistWindup > 0) {
            this.mistWindup--;
            this.getNavigation().stop();
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    8, 0.6D, 0.3D, 0.6D, 0.01D);

            if (this.mistWindup == 0) {
                performMistPulse(serverLevel);
            }
            return;
        }

        // Condição para disparar o feitiço de névoa e poça no chão
        if (this.mistCooldown == 0 && distSqr <= MIST_RANGE_SQR && this.hasLineOfSight(target)) {
            this.mistCooldown = MIST_COOLDOWN_TICKS;
            this.mistWindup = 30; // 1.5 segundos de aviso prévio
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 0.8F, 1.2F);
        }
    }

    private void performMistPulse(ServerLevel serverLevel) {
        // Cria nuvem de poça no chão (estilo baforada do Ender Dragon)
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        cloud.setRadius(3.5F);
        cloud.setRadiusOnUse(-0.5F);
        cloud.setWaitTime(10);
        cloud.setDuration(100); // 5 segundos de duração no chão
        cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0)); // 3 segundos de cegueira
        cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // 3 segundos de lentidão II
        cloud.setOwner(this);
        this.level().addFreshEntity(cloud);

        // Som de explosão de névoa mágica
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.0F, 0.8F);

        // Aplicação em área imediata em entidades próximas
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(4.0D))) {
            if (entity != this) {
                entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
            }
        }
    }
}
