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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
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
 * Sereia das Profundezas — Criatura mística do Recife de Coral Abissal.
 * - Canto Desorientador: Emite ondas sonoras que causam náusea e invertem/desviam o impulso do jogador.
 * - Projéteis Sônicos: Dispara rajadas de energia sônica concentrada à distância.
 */
public final class SereiaDasProfundezasEntity extends PathfinderMob {
    private int sirenSongCooldown = 70;
    private int sonicProjectileCooldown = 55;

    public SereiaDasProfundezasEntity(EntityType<? extends SereiaDasProfundezasEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 14;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 5.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 26.0D)
                .add(Attributes.ARMOR, 3.0D);
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.85D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 14.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(
                        ParticleTypes.GLOW,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        this.getY() + 0.6D + this.random.nextDouble() * 1.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.7D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.sirenSongCooldown > 0) this.sirenSongCooldown--;
        if (this.sonicProjectileCooldown > 0) this.sonicProjectileCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // 1. Canto Desorientador: ondas sonoras que aplicam Náusea e invertem o movimento do alvo
        if (this.sirenSongCooldown == 0 && distSqr <= 81.0D && this.hasLineOfSight(target)) {
            this.sirenSongCooldown = 135;
            performDisorientingSong(serverLevel);
            return;
        }

        // 2. Projéteis Sônicos: rajada de energia sônica concentrada à distância
        if (this.sonicProjectileCooldown == 0 && distSqr >= 12.25D && distSqr <= 196.0D && this.hasLineOfSight(target)) {
            this.sonicProjectileCooldown = 75;
            fireSonicProjectile(serverLevel, target);
        }
    }

    private void performDisorientingSong(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM, SoundSource.HOSTILE, 1.5F, 0.65F);
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 0.9F, 1.2F);

        for (int i = 0; i < 10; i++) {
            double angle = (Math.PI * 2.0D * i) / 10.0D;
            serverLevel.sendParticles(
                    ParticleTypes.NOTE,
                    this.getX() + Math.cos(angle) * 1.5D,
                    this.getY() + 1.45D,
                    this.getZ() + Math.sin(angle) * 1.5D,
                    1, 0.1D, 0.1D, 0.1D, (i % 6) / 6.0D
            );
        }

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(9.0D),
                e -> e != this && !(e instanceof SereiaDasProfundezasEntity) && e.isAlive())) {
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 130, 0), this);
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this);

            // Inverte e desvia o impulso atual do jogador para simular controles invertidos
            Vec3 currentVel = victim.getDeltaMovement();
            Vec3 tangential = this.position().subtract(victim.position()).normalize().yRot((float) (Math.PI * 0.5D)).scale(0.32D);
            victim.setDeltaMovement(-currentVel.x * 1.25D + tangential.x, 0.12D, -currentVel.z * 1.25D + tangential.z);
            victim.hurtMarked = true;
        }
    }

    private void fireSonicProjectile(ServerLevel serverLevel, LivingEntity target) {
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);
        Vec3 start = this.position().add(0.0D, 1.35D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
        Vec3 diff = end.subtract(start);
        int steps = Math.max(5, (int) (diff.length() * 1.8D));

        for (int i = 0; i <= steps; i++) {
            Vec3 pt = start.add(diff.scale((double) i / (double) steps));
            if (i % 2 == 0) {
                serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pt.x, pt.y, pt.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
            serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, pt.x, pt.y, pt.z, 2, 0.12D, 0.12D, 0.12D, 0.02D);
        }

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 0.85F, 1.35F);

        target.hurt(this.damageSources().sonicBoom(this), 6.0F);
        Vec3 push = diff.normalize().scale(0.45D);
        target.push(push.x, 0.18D, push.z);
        target.hurtMarked = true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.PRISMARINE_CRYSTALS, 1 + this.random.nextInt(2 + looting));
        if (this.random.nextFloat() < 0.35F) {
            this.spawnAtLocation(Items.NAUTILUS_SHELL, 1);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ELDER_GUARDIAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }
}
