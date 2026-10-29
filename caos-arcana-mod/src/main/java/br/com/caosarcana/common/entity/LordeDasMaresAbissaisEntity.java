package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
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
 * Lorde das Marés Abissais — Chefão do Recife de Coral Abissal & Arquipélago.
 * - Domínio das Correntes: Cria redemoinhos e correntes marítimas que puxam o jogador ou o arremessam contra as bordas.
 * - Tridente Abissal: Dispara feixes de energia abissal e ondas de choque marinho em área.
 * - Invocações das Marés: Convoca Tragadores do Abismo para defendê-lo enquanto canaliza energia.
 */
public final class LordeDasMaresAbissaisEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(LordeDasMaresAbissaisEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST_STATE =
            SynchedEntityData.defineId(LordeDasMaresAbissaisEntity.class, EntityDataSerializers.INT);

    public static final int CAST_NONE = 0;
    public static final int CAST_TIDAL_CURRENTS = 1;
    public static final int CAST_ABYSSAL_TRIDENT = 2;
    public static final int CAST_SUMMON_TIDES = 3;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.caosarcana.lorde_das_mares_abissais"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int tidalCurrentsCooldown = 95;
    private int tidalCurrentsWindup = 0;
    private int tidalCurrentsActiveTicks = 0;

    private int abyssalTridentCooldown = 65;
    private int abyssalTridentWindup = 0;

    private int summonTidesCooldown = 140;
    private int summonTidesWindup = 0;

    public LordeDasMaresAbissaisEntity(EntityType<? extends LordeDasMaresAbissaisEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 75;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 360.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 10.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.1D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90D)
                .add(Attributes.FOLLOW_RANGE, 42.0D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PHASE, 1);
        this.entityData.define(DATA_CAST_STATE, CAST_NONE);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    private void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
    }

    public int getCastState() {
        return this.entityData.get(DATA_CAST_STATE);
    }

    private void setCastState(int state) {
        this.entityData.set(DATA_CAST_STATE, state);
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
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.05D, false) {
            @Override
            protected int getAttackInterval() {
                return 30;
            }
        });
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 20.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_DROWNING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            spawnClientAbyssalAura();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float healthFraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthFraction);
        updatePhase(serverLevel, healthFraction);

        if (this.tidalCurrentsCooldown > 0) this.tidalCurrentsCooldown--;
        if (this.abyssalTridentCooldown > 0) this.abyssalTridentCooldown--;
        if (this.summonTidesCooldown > 0) this.summonTidesCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            resetSkillStates();
            return;
        }

        if (this.tidalCurrentsWindup > 0 || this.tidalCurrentsActiveTicks > 0) {
            tickTidalCurrents(serverLevel);
            return;
        }
        if (this.abyssalTridentWindup > 0) {
            tickAbyssalTrident(serverLevel, target);
            return;
        }
        if (this.summonTidesWindup > 0) {
            tickSummonTides(serverLevel, target);
            return;
        }

        this.setCastState(CAST_NONE);
        double distSqr = this.distanceToSqr(target);

        // 1. Invocações das Marés: convoca Tragadores do Abismo enquanto canaliza energia
        if (this.summonTidesCooldown == 0 && distSqr <= 324.0D) {
            int activeDevourers = serverLevel.getEntitiesOfClass(
                    TragadorDoAbismoEntity.class,
                    this.getBoundingBox().inflate(24.0D)
            ).size();
            if (activeDevourers < 4) {
                this.summonTidesCooldown = getPhase() >= 2 ? 190 : 240;
                this.summonTidesWindup = 26;
                this.setCastState(CAST_SUMMON_TIDES);
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.5F, 0.65F);
                return;
            }
        }

        // 2. Domínio das Correntes: redemoinho que puxa e depois arremessa contra as bordas
        if (this.tidalCurrentsCooldown == 0 && distSqr >= 9.0D && distSqr <= 196.0D) {
            this.tidalCurrentsCooldown = 165;
            this.tidalCurrentsWindup = 20;
            this.setCastState(CAST_TIDAL_CURRENTS);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.CONDUIT_ACTIVATE, SoundSource.HOSTILE, 1.5F, 0.75F);
            return;
        }

        // 3. Tridente Abissal: feixe de energia abissal + onda de choque marinho em área
        if (this.abyssalTridentCooldown == 0 && distSqr >= 9.0D && distSqr <= 324.0D && this.hasLineOfSight(target)) {
            this.abyssalTridentCooldown = getPhase() >= 2 ? 95 : 125;
            this.abyssalTridentWindup = 22;
            this.setCastState(CAST_ABYSSAL_TRIDENT);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 1.3F, 1.2F);
        }
    }

    private void spawnClientAbyssalAura() {
        if (this.random.nextInt(2) == 0) {
            this.level().addParticle(
                    ParticleTypes.GLOW,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.5D,
                    this.getY() + 0.5D + this.random.nextDouble() * 2.4D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.5D,
                    0.0D, 0.03D, 0.0D
            );
        }
        if (this.random.nextInt(3) == 0) {
            this.level().addParticle(
                    ParticleTypes.NAUTILUS,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    this.getY() + 1.0D + this.random.nextDouble() * 1.6D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.4D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    private void updatePhase(ServerLevel serverLevel, float healthFraction) {
        if (healthFraction <= 0.60F && getPhase() < 2) {
            setPhase(2);
            this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            this.summonTidesCooldown = 0;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 1.5F, 0.7F);
            serverLevel.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    this.getX(), this.getY() + 1.6D, this.getZ(),
                    50, 1.4D, 1.2D, 1.4D, 0.08D
            );
            serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    this.getX(), this.getY() + 1.6D, this.getZ(),
                    45, 1.3D, 1.1D, 1.3D, 0.1D
            );
        }
    }

    private void resetSkillStates() {
        this.tidalCurrentsWindup = 0;
        this.tidalCurrentsActiveTicks = 0;
        this.abyssalTridentWindup = 0;
        this.summonTidesWindup = 0;
        this.setCastState(CAST_NONE);
    }

    private void tickTidalCurrents(ServerLevel serverLevel) {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        if (this.tidalCurrentsWindup > 0) {
            this.tidalCurrentsWindup--;
            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2.0D * i) / 10.0D + (this.tickCount * 0.25D);
                serverLevel.sendParticles(
                        ParticleTypes.NAUTILUS,
                        this.getX() + Math.cos(angle) * 3.8D,
                        this.getY() + 0.6D,
                        this.getZ() + Math.sin(angle) * 3.8D,
                        1, 0.1D, 0.2D, 0.1D, 0.04D
                );
            }
            if (this.tidalCurrentsWindup == 0) {
                this.tidalCurrentsActiveTicks = 44;
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.CONDUIT_ATTACK_TARGET, SoundSource.HOSTILE, 1.5F, 0.7F);
            }
            return;
        }

        if (this.tidalCurrentsActiveTicks > 0) {
            this.tidalCurrentsActiveTicks--;
            for (int i = 0; i < 14; i++) {
                double angle = (Math.PI * 2.0D * i) / 14.0D + (this.tickCount * 0.35D);
                double r = 1.6D + (i % 4) * 1.9D;
                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE_POP,
                        this.getX() + Math.cos(angle) * r,
                        this.getY() + 0.4D + (i % 3) * 0.6D,
                        this.getZ() + Math.sin(angle) * r,
                        2, 0.15D, 0.2D, 0.15D, 0.05D
                );
            }

            // Nos primeiros ticks puxa os jogadores para o redemoinho; no último tick arremessa contra as bordas
            boolean finalRepulse = (this.tidalCurrentsActiveTicks == 1);
            for (Player player : serverLevel.getEntitiesOfClass(
                    Player.class, this.getBoundingBox().inflate(11.0D),
                    p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
                if (finalRepulse) {
                    Vec3 away = player.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.35D);
                    player.setDeltaMovement(away.x, 0.42D, away.z);
                    player.hurtMarked = true;
                    player.hurt(this.damageSources().indirectMagic(this, this), 7.5F);
                } else {
                    Vec3 pull = this.position().subtract(player.position());
                    double dist = pull.horizontalDistance();
                    if (dist > 1.8D && dist <= 11.0D) {
                        Vec3 pullStep = pull.normalize().scale(0.15D);
                        player.setDeltaMovement(player.getDeltaMovement().add(pullStep.x, 0.02D, pullStep.z));
                        player.hurtMarked = true;
                    }
                }
            }

            if (finalRepulse) {
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 1.15F);
                serverLevel.sendParticles(
                        ParticleTypes.SPLASH,
                        this.getX(), this.getY() + 1.0D, this.getZ(),
                        60, 2.5D, 0.8D, 2.5D, 0.2D
                );
            }

            if (this.tidalCurrentsActiveTicks == 0) {
                this.setCastState(CAST_NONE);
            }
        }
    }

    private void tickAbyssalTrident(ServerLevel serverLevel, LivingEntity target) {
        this.abyssalTridentWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        Vec3 tridentTip = this.position().add(0.0D, 2.2D, 0.0D);
        serverLevel.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                tridentTip.x, tridentTip.y, tridentTip.z,
                5, 0.25D, 0.25D, 0.25D, 0.04D
        );
        serverLevel.sendParticles(
                ParticleTypes.GLOW,
                tridentTip.x, tridentTip.y, tridentTip.z,
                4, 0.2D, 0.2D, 0.2D, 0.02D
        );

        if (this.abyssalTridentWindup == 0) {
            this.setCastState(CAST_NONE);
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 beam = targetCenter.subtract(tridentTip);
            int steps = Math.max(6, (int) (beam.length() * 2.5D));
            for (int i = 0; i <= steps; i++) {
                Vec3 pt = tridentTip.add(beam.scale((double) i / (double) steps));
                serverLevel.sendParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
                if (i % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SONIC_BOOM, pt.x, pt.y, pt.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
                }
            }

            // Onda de choque marinho em área no ponto de impacto
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.TRIDENT_THUNDER, SoundSource.HOSTILE, 1.5F, 0.85F);
            serverLevel.sendParticles(
                    ParticleTypes.GLOW_SQUID_INK,
                    target.getX(), target.getY() + 0.6D, target.getZ(),
                    32, 1.8D, 0.6D, 1.8D, 0.08D
            );

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    target.getBoundingBox().inflate(3.2D),
                    e -> e != this && !(e instanceof TragadorDoAbismoEntity) && e.isAlive())) {
                victim.hurt(this.damageSources().indirectMagic(this, this), 9.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1), this);
            }
        }
    }

    private void tickSummonTides(ServerLevel serverLevel, LivingEntity target) {
        this.summonTidesWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        serverLevel.sendParticles(
                ParticleTypes.NAUTILUS,
                this.getX(), this.getY() + 1.6D, this.getZ(),
                6, 0.8D, 0.8D, 0.8D, 0.06D
        );

        if (this.summonTidesWindup == 0) {
            this.setCastState(CAST_NONE);
            this.heal(14.0F);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5F, 0.7F);

            for (int i = -1; i <= 1; i += 2) {
                TragadorDoAbismoEntity devourer = CaosArcanaEntities.TRAGADOR_DO_ABISMO.create(serverLevel);
                if (devourer != null) {
                    double sx = this.getX() + i * 2.8D;
                    double sz = this.getZ() + (this.random.nextDouble() - 0.5D) * 2.4D;
                    devourer.moveTo(sx, this.getY(), sz, this.getYRot(), 0.0F);
                    devourer.setCustomName(Component.literal("Servo das Marés"));
                    devourer.setTarget(target);
                    serverLevel.addFreshEntity(devourer);
                    serverLevel.sendParticles(
                            ParticleTypes.GLOW_SQUID_INK,
                            sx, this.getY() + 0.9D, sz,
                            24, 0.4D, 0.6D, 0.4D, 0.06D
                    );
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.HEART_OF_THE_SEA, 1);
        this.spawnAtLocation(Items.PRISMARINE_CRYSTALS, 4 + this.random.nextInt(4));
        this.spawnAtLocation(Items.GOLD_BLOCK, 1 + this.random.nextInt(2));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        this.bossEvent.removeAllPlayers();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ELDER_GUARDIAN_AMBIENT;
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
