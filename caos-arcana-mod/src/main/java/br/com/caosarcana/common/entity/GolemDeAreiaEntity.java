package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
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

/**
 * Golem de Areia — Mob Hostil / Tanque do Deserto das Dunas Douradas.
 * - Desmoronamento Tático: Desfaz-se temporariamente em poeira ao receber dano de alto impacto,
 *   esquivando-se e reposicionando-se.
 * - Soco Sísmico: Ataque em área (AoE) no chão que causa repulsão (knockback) severa.
 */
public final class GolemDeAreiaEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_SEISMIC_WINDUP =
            SynchedEntityData.defineId(GolemDeAreiaEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CRUMBLED =
            SynchedEntityData.defineId(GolemDeAreiaEntity.class, EntityDataSerializers.BOOLEAN);

    private int seismicSlamCooldown = 100;
    private int seismicSlamWindup = 0;

    private int crumbleCooldown = 0;
    private int crumbledTicks = 0;

    public GolemDeAreiaEntity(EntityType<? extends GolemDeAreiaEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 46.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.1D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 7.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SEISMIC_WINDUP, false);
        this.entityData.define(DATA_CRUMBLED, false);
    }

    public boolean isSeismicWindup() {
        return this.entityData.get(DATA_SEISMIC_WINDUP);
    }

    private void setSeismicWindup(boolean windup) {
        this.entityData.set(DATA_SEISMIC_WINDUP, windup);
    }

    public boolean isCrumbled() {
        return this.entityData.get(DATA_CRUMBLED);
    }

    private void setCrumbled(boolean crumbled) {
        this.entityData.set(DATA_CRUMBLED, crumbled);
        this.setInvisible(crumbled);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            protected int getAttackInterval() {
                return 34;
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
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
        if (isCrumbled() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        boolean damaged = super.hurt(source, amount);
        if (damaged && !this.level().isClientSide && this.isAlive()
                && amount >= 5.5F && this.crumbleCooldown == 0 && !isCrumbled()
                && this.level() instanceof ServerLevel serverLevel) {
            triggerTacticalCrumble(serverLevel, source.getEntity());
        }
        return damaged;
    }

    private void triggerTacticalCrumble(ServerLevel serverLevel, Entity attacker) {
        this.crumbleCooldown = 180;
        this.crumbledTicks = 22; // 1.1s em forma de poeira
        this.seismicSlamWindup = 0;
        this.setSeismicWindup(false);
        this.setCrumbled(true);

        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                this.getX(), this.getY() + 1.0D, this.getZ(),
                45, 0.6D, 0.8D, 0.6D, 0.12D
        );
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SAND_BREAK, SoundSource.HOSTILE, 1.3F, 0.65F);

        // Reposicionamento tático lateral/traseiro em nuvem de areia
        double angle = this.getRandom().nextDouble() * Math.PI * 2.0D;
        double vx = Math.cos(angle) * 0.65D;
        double vz = Math.sin(angle) * 0.65D;
        if (attacker != null) {
            double dx = this.getX() - attacker.getX();
            double dz = this.getZ() - attacker.getZ();
            double len = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
            vx = (-dz / len) * (this.getRandom().nextBoolean() ? 0.65D : -0.65D);
            vz = (dx / len) * (this.getRandom().nextBoolean() ? 0.65D : -0.65D);
        }
        this.setDeltaMovement(vx, 0.15D, vz);
        this.hasImpulse = true;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (this.getDeltaMovement().horizontalDistanceSqr() > 0.002D && this.getRandom().nextInt(3) == 0) {
                this.level().addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.1D,
                        this.getY() + 0.2D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.1D,
                        0.0D, 0.0D, 0.0D
                );
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (this.seismicSlamCooldown > 0) this.seismicSlamCooldown--;
        if (this.crumbleCooldown > 0) this.crumbleCooldown--;

        if (this.crumbledTicks > 0) {
            this.crumbledTicks--;
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                    this.getX(), this.getY() + 0.5D, this.getZ(),
                    8, 0.45D, 0.35D, 0.45D, 0.05D
            );
            if (this.crumbledTicks == 0) {
                this.setCrumbled(false);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        32, 0.55D, 0.75D, 0.55D, 0.1D
                );
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.IRON_GOLEM_REPAIR, SoundSource.HOSTILE, 1.1F, 0.65F);
            }
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.seismicSlamWindup = 0;
            this.setSeismicWindup(false);
            return;
        }

        if (this.seismicSlamWindup > 0) {
            this.seismicSlamWindup--;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                    this.getX(), this.getY() + 0.2D, this.getZ(),
                    6, 0.8D, 0.15D, 0.8D, 0.06D
            );

            if (this.seismicSlamWindup == 0) {
                this.setSeismicWindup(false);
                performSeismicSlam(serverLevel);
            }
            return;
        }

        double distSqr = this.distanceToSqr(target);
        if (this.seismicSlamCooldown == 0 && distSqr <= 20.25D) {
            this.seismicSlamCooldown = 140;
            this.seismicSlamWindup = 20; // 1.0s de telegrafia erguendo os braços
            this.setSeismicWindup(true);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.IRON_GOLEM_ATTACK, SoundSource.HOSTILE, 1.2F, 0.55F);
        }
    }

    private void performSeismicSlam(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.1F, 0.75F);
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SANDSTONE.defaultBlockState()),
                this.getX(), this.getY() + 0.25D, this.getZ(),
                50, 1.8D, 0.3D, 1.8D, 0.16D
        );
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                this.getX(), this.getY() + 0.35D, this.getZ(),
                40, 2.0D, 0.4D, 2.0D, 0.12D
        );

        for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(4.5D),
                e -> e != this && !(e instanceof GolemDeAreiaEntity) && e.isAlive())) {
            if (this.distanceToSqr(victim) <= 20.25D) {
                victim.hurt(this.damageSources().mobAttack(this), 8.0F);
                // Repulsão (knockback) severa conforme GDD
                victim.knockback(1.75D, this.getX() - victim.getX(), this.getZ() - victim.getZ());
                victim.setDeltaMovement(victim.getDeltaMovement().add(0.0D, 0.32D, 0.0D));
                victim.hurtMarked = true;
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.CUT_SANDSTONE, 1 + this.random.nextInt(2 + looting));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_STEP;
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
