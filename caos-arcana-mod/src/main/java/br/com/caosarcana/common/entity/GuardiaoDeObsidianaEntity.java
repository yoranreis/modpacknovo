package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.PathfinderMob;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Guardião de Obsidiana — Construto vulcânico abissal do Recife de Coral Abissal.
 * - Cortina de Vapor: Ao ser atingido, libera uma nuvem densa de vapor que causa cegueira curta e queimadura leve.
 * - Resistência Vulcânica: Imune a fogo e reduz significativamente o dano físico recebido pela frente.
 */
public final class GuardiaoDeObsidianaEntity extends PathfinderMob {
    private int steamCurtainCooldown = 0;

    public GuardiaoDeObsidianaEntity(EntityType<? extends GuardiaoDeObsidianaEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 15;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 46.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 7.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    public boolean fireImmune() {
        return true;
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(
                        ParticleTypes.LAVA,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        this.getY() + 0.8D + this.random.nextDouble() * 0.9D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        0.0D, 0.02D, 0.0D
                );
            }
            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(
                        ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        this.getY() + 1.1D + this.random.nextDouble() * 0.8D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.04D, 0.0D
                );
            }
            return;
        }

        if (this.steamCurtainCooldown > 0) {
            this.steamCurtainCooldown--;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }

        // Resistência Vulcânica: reduz em 40% o dano físico recebido pela frente
        Entity attacker = source.getEntity();
        if (attacker != null && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            Vec3 forward = Vec3.directionFromRotation(0.0F, this.getYRot()).normalize();
            Vec3 toAttacker = attacker.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            if (forward.dot(toAttacker) > 0.15D) {
                amount *= 0.60F;
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.55F, 1.55F);
                    serverLevel.sendParticles(
                            ParticleTypes.CRIT,
                            this.getX(), this.getY() + 1.2D, this.getZ(),
                            8, 0.35D, 0.35D, 0.35D, 0.05D
                    );
                }
            }
        }

        boolean hurt = super.hurt(source, amount);
        if (hurt && this.isAlive() && this.steamCurtainCooldown == 0 && this.level() instanceof ServerLevel serverLevel) {
            triggerSteamCurtain(serverLevel);
        }
        return hurt;
    }

    private void triggerSteamCurtain(ServerLevel serverLevel) {
        this.steamCurtainCooldown = 75;
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.HOSTILE, 1.3F, 0.8F);
        serverLevel.sendParticles(
                ParticleTypes.CLOUD,
                this.getX(), this.getY() + 1.1D, this.getZ(),
                36, 1.6D, 0.9D, 1.6D, 0.04D
        );
        serverLevel.sendParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                this.getX(), this.getY() + 1.0D, this.getZ(),
                18, 1.3D, 0.8D, 1.3D, 0.02D
        );
        serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(), this.getY() + 1.0D, this.getZ(),
                14, 1.1D, 0.6D, 1.1D, 0.03D
        );

        for (LivingEntity nearby : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(4.0D),
                e -> e != this && !(e instanceof GuardiaoDeObsidianaEntity) && e.isAlive())) {
            nearby.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 55, 0), this);
            nearby.setSecondsOnFire(3);
            nearby.hurt(this.damageSources().onFire(), 2.5F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.OBSIDIAN, 1 + this.random.nextInt(2 + looting));
        if (this.random.nextFloat() < 0.5F) {
            this.spawnAtLocation(Items.MAGMA_CREAM, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.BLAZE_AMBIENT;
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
