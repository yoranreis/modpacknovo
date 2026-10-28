package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** Basalt Shellback's colossal, boss-sized ancestor. */
public final class ColossoBasalticoEntity extends TerrasDaCinzaMob {
    private final ServerBossEvent bossEvent = new ServerBossEvent(
            getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private int chargeCooldown;
    private int chargeTicks;
    private int slamCooldown;
    private int slamWindup;
    private boolean hitDuringCharge;

    public ColossoBasalticoEntity(EntityType<? extends ColossoBasalticoEntity> entityType, Level level) {
        super(entityType, level);
        bossEvent.setName(getDisplayName());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 180.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 0.95D, false) {
            @Override
            protected int getAttackInterval() {
                return 48;
            }
        });
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 18.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel serverLevel) {
            bossEvent.setProgress(getHealth() / getMaxHealth());
            if (chargeCooldown > 0) chargeCooldown--;
            if (slamCooldown > 0) slamCooldown--;

            LivingEntity target = getTarget();
            if (target != null && target.isAlive()) {
                double distanceSqr = distanceToSqr(target);
                if (slamWindup > 0) {
                    slamWindup--;
                    getNavigation().stop();
                    setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
                    if (slamWindup == 0) performGroundSlam(serverLevel);
                } else if (slamCooldown == 0 && distanceSqr < 36.0D && chargeTicks == 0) {
                    slamCooldown = 150;
                    slamWindup = 18;
                    serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR,
                            SoundSource.HOSTILE, 1.0F, 0.75F);
                }

                if (chargeCooldown == 0 && chargeTicks == 0 && distanceSqr > 49.0D && distanceSqr < 144.0D
                        && slamWindup == 0 && slamCooldown == 0 && hasLineOfSight(target)) {
                    double dx = target.getX() - getX();
                    double dz = target.getZ() - getZ();
                    double length = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
                    setDeltaMovement(dx / length * 0.8D,
                            Math.max(0.05D, getDeltaMovement().y), dz / length * 0.8D);
                    hasImpulse = true;
                    chargeTicks = 8;
                    chargeCooldown = 130;
                    hitDuringCharge = false;
                    serverLevel.sendParticles(new BlockParticleOption(
                                    ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                            getX(), getY() + 0.25D, getZ(), 28, 0.75D, 0.2D, 0.75D, 0.12D);
                    serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ROAR,
                            SoundSource.HOSTILE, 1.6F, 0.55F);
                }

                if (chargeTicks > 0) {
                    chargeTicks--;
                    if (!hitDuringCharge && getBoundingBox().inflate(0.35D).intersects(target.getBoundingBox())) {
                        target.hurt(damageSources().mobAttack(this), 9.0F);
                        target.knockback(1.7D, getX() - target.getX(), getZ() - target.getZ());
                        hitDuringCharge = true;
                        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                                target.getX(), target.getY() + 0.5D, target.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
                        serverLevel.sendParticles(new BlockParticleOption(
                                        ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                                target.getX(), target.getY(), target.getZ(), 18, 0.5D, 0.3D, 0.5D, 0.1D);
                    }
                }
            }
        } else if (getDeltaMovement().horizontalDistanceSqr() > 0.003D && getRandom().nextInt(2) == 0) {
            level().addParticle(new BlockParticleOption(
                            ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                    getX() + (getRandom().nextDouble() - 0.5D) * 2.2D,
                    getY() + 0.1D,
                    getZ() + (getRandom().nextDouble() - 0.5D) * 2.2D,
                    0.0D, 0.035D, 0.0D);
        }
    }

    private void performGroundSlam(ServerLevel serverLevel) {
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ATTACK,
                SoundSource.HOSTILE, 1.5F, 0.5F);
        serverLevel.sendParticles(new BlockParticleOption(
                        ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                getX(), getY() + 0.15D, getZ(), 44, 1.0D, 0.25D, 1.0D, 0.16D);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                getX(), getY() + 0.3D, getZ(), 4, 1.2D, 0.1D, 1.2D, 0.0D);

        for (Player player : serverLevel.getEntitiesOfClass(
                Player.class, getBoundingBox().inflate(2.0D))) {
            double distance = distanceToSqr(player);
            if (distance <= 25.0D) {
                player.hurt(damageSources().mobAttack(this), 10.0F);
                player.knockback(1.35D, getX() - player.getX(), getZ() - player.getZ());
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(
                                ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()),
                        target.getX(), target.getY() + 0.4D, target.getZ(),
                        16, 0.4D, 0.25D, 0.4D, 0.08D);
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.RAVAGER_ATTACK,
                        SoundSource.HOSTILE, 1.2F, 0.65F);
            }
        }
        return hit;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        bossEvent.removeAllPlayers();
    }
}
