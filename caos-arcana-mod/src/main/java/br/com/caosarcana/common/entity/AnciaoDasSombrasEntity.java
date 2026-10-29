package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.AreaEffectCloud;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Chefe da Floresta Negra: O Ancião das Sombras.
 * Colosso arbóreo ancestral com núcleo de alma verde-esmeralda e 3 fases de combate:
 * - Fase 1 (100% - 66%): Raízes Aprisionadoras telegrafadas no solo para limitar rotas de fuga.
 * - Fase 2 (66% - 33%): Sombras Duplicadas que flanqueiam e confundem o alvo.
 * - Fase 3 (< 33%): Tempestade de Folhas Venenosas que fecha partes da arena.
 */
public final class AnciaoDasSombrasEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(AnciaoDasSombrasEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST_STATE =
            SynchedEntityData.defineId(AnciaoDasSombrasEntity.class, EntityDataSerializers.INT);

    public static final int CAST_NONE = 0;
    public static final int CAST_ROOT_SNARE = 1;
    public static final int CAST_SHADOW_CLONES = 2;
    public static final int CAST_LEAF_STORM = 3;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.caosarcana.anciao_das_sombras"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int rootSnareCooldown = 80;
    private int rootSnareWindup = 0;
    private double snareTargetX;
    private double snareTargetY;
    private double snareTargetZ;

    private int shadowClonesCooldown = 140;
    private int shadowClonesWindup = 0;

    private int leafStormCooldown = 120;
    private int leafStormWindup = 0;

    public AnciaoDasSombrasEntity(EntityType<? extends AnciaoDasSombrasEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 50;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 220.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 8.0D);
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
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            protected int getAttackInterval() {
                return 36;
            }

            @Override
            protected double getAttackReachSqr(LivingEntity attackTarget) {
                return 9.5D + attackTarget.getBbWidth();
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.65D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 18.0F));
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
        if (source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            spawnClientAuraParticles();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float healthFraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthFraction);
        updatePhase(serverLevel, healthFraction);

        if (this.rootSnareCooldown > 0) this.rootSnareCooldown--;
        if (this.shadowClonesCooldown > 0) this.shadowClonesCooldown--;
        if (this.leafStormCooldown > 0) this.leafStormCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            resetWindups();
            return;
        }

        // Processa windup ativo (apenas uma habilidade telegrafada por vez)
        if (this.rootSnareWindup > 0) {
            tickRootSnareWindup(serverLevel);
            return;
        }
        if (this.shadowClonesWindup > 0) {
            tickShadowClonesWindup(serverLevel, target);
            return;
        }
        if (this.leafStormWindup > 0) {
            tickLeafStormWindup(serverLevel, target);
            return;
        }

        this.setCastState(CAST_NONE);
        double distSqr = this.distanceToSqr(target);
        int currentPhase = getPhase();

        // Prioridade 1 (Fase 3): Tempestade de Folhas Venenosas
        if (currentPhase >= 3 && this.leafStormCooldown == 0 && distSqr <= 196.0D) {
            this.leafStormCooldown = 180;
            this.leafStormWindup = 30; // 1.5s de aviso claro
            this.setCastState(CAST_LEAF_STORM);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.4F, 0.7F);
            return;
        }

        // Prioridade 2 (Fase 2+): Sombras Duplicadas
        if (currentPhase >= 2 && this.shadowClonesCooldown == 0 && distSqr <= 256.0D) {
            int activeShadows = serverLevel.getEntitiesOfClass(
                    EspiritoNevoaEntity.class,
                    this.getBoundingBox().inflate(24.0D)
            ).size();
            if (activeShadows < 3) {
                this.shadowClonesCooldown = 220;
                this.shadowClonesWindup = 24; // 1.2s de aviso
                this.setCastState(CAST_SHADOW_CLONES);
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.3F, 0.65F);
                return;
            }
        }

        // Prioridade 3 (Fase 1+): Raízes Aprisionadoras
        if (this.rootSnareCooldown == 0 && distSqr >= 9.0D && distSqr <= 256.0D && this.hasLineOfSight(target)) {
            this.rootSnareCooldown = currentPhase >= 2 ? 120 : 145;
            this.rootSnareWindup = 26; // 1.3s de telegrafia no chão
            this.snareTargetX = target.getX();
            this.snareTargetY = target.getY();
            this.snareTargetZ = target.getZ();
            this.setCastState(CAST_ROOT_SNARE);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.2F, 0.55F);
        }
    }

    private void spawnClientAuraParticles() {
        if (this.getRandom().nextInt(3) == 0) {
            this.level().addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.4D,
                    this.getY() + 1.4D + this.getRandom().nextDouble() * 1.2D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.4D,
                    0.0D, 0.02D, 0.0D
            );
        }
        if (this.getRandom().nextInt(4) == 0) {
            this.level().addParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.6D,
                    this.getY() + 1.2D + this.getRandom().nextDouble() * 1.5D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.6D,
                    0.0D, 0.03D, 0.0D
            );
        }
    }

    private void updatePhase(ServerLevel serverLevel, float healthFraction) {
        int currentPhase = getPhase();
        if (healthFraction <= 0.33F && currentPhase < 3) {
            setPhase(3);
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 1.6F, 0.55F);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.8D, this.getZ(), 45, 1.1D, 1.0D, 1.1D, 0.08D);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_LEAVES.defaultBlockState()),
                    this.getX(), this.getY() + 1.5D, this.getZ(), 40, 1.3D, 0.9D, 1.3D, 0.1D);
        } else if (healthFraction <= 0.66F && currentPhase < 2) {
            setPhase(2);
            this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.HOSTILE, 1.5F, 0.7F);
            serverLevel.sendParticles(ParticleTypes.SQUID_INK,
                    this.getX(), this.getY() + 1.6D, this.getZ(), 35, 1.0D, 0.9D, 1.0D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                    this.getX(), this.getY() + 1.6D, this.getZ(), 30, 0.9D, 0.8D, 0.9D, 0.06D);
        }
    }

    private void resetWindups() {
        this.rootSnareWindup = 0;
        this.shadowClonesWindup = 0;
        this.leafStormWindup = 0;
        this.setCastState(CAST_NONE);
    }

    private void tickRootSnareWindup(ServerLevel serverLevel) {
        this.rootSnareWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        // Anel telegrafado de 2.8 blocos ao redor da posição marcada no chão
        double radius = 2.8D;
        for (int i = 0; i < 12; i++) {
            double angle = (Math.PI * 2.0D * i) / 12.0D;
            double px = this.snareTargetX + Math.cos(angle) * radius;
            double pz = this.snareTargetZ + Math.sin(angle) * radius;
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                    px, this.snareTargetY + 0.1D, pz, 1, 0.08D, 0.05D, 0.08D, 0.02D
            );
            if (i % 2 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        px, this.snareTargetY + 0.25D, pz, 1, 0.05D, 0.05D, 0.05D, 0.01D
                );
            }
        }

        if (this.rootSnareWindup == 0) {
            performRootSnareEruption(serverLevel);
            this.setCastState(CAST_NONE);
        }
    }

    private void performRootSnareEruption(ServerLevel serverLevel) {
        serverLevel.playSound(null, this.snareTargetX, this.snareTargetY, this.snareTargetZ,
                SoundEvents.WARDEN_ATTACK_IMPACT, SoundSource.HOSTILE, 1.4F, 0.65F);
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.MANGROVE_ROOTS.defaultBlockState()),
                this.snareTargetX, this.snareTargetY + 0.5D, this.snareTargetZ,
                48, 1.4D, 0.6D, 1.4D, 0.15D
        );
        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.snareTargetX, this.snareTargetY + 0.4D, this.snareTargetZ,
                24, 1.2D, 0.4D, 1.2D, 0.04D
        );

        AABB snareBox = new AABB(
                this.snareTargetX - 2.8D, this.snareTargetY - 1.0D, this.snareTargetZ - 2.8D,
                this.snareTargetX + 2.8D, this.snareTargetY + 2.5D, this.snareTargetZ + 2.8D
        );
        List<LivingEntity> victims = serverLevel.getEntitiesOfClass(
                LivingEntity.class, snareBox,
                e -> e != this && !(e instanceof EspiritoNevoaEntity) && e.isAlive()
        );
        for (LivingEntity victim : victims) {
            double dx = victim.getX() - this.snareTargetX;
            double dz = victim.getZ() - this.snareTargetZ;
            if (dx * dx + dz * dz <= 2.8D * 2.8D) {
                victim.hurt(this.damageSources().mobAttack(this), 7.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 2), this);
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), this);
                victim.setDeltaMovement(0.0D, Math.min(0.0D, victim.getDeltaMovement().y), 0.0D);
            }
        }
    }

    private void tickShadowClonesWindup(ServerLevel serverLevel, LivingEntity target) {
        this.shadowClonesWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        serverLevel.sendParticles(
                ParticleTypes.SQUID_INK,
                this.getX(), this.getY() + 1.5D, this.getZ(),
                5, 0.8D, 0.9D, 0.8D, 0.03D
        );
        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 1.8D, this.getZ(),
                4, 0.7D, 0.6D, 0.7D, 0.02D
        );

        if (this.shadowClonesWindup == 0) {
            performShadowClones(serverLevel, target);
            this.setCastState(CAST_NONE);
        }
    }

    private void performShadowClones(ServerLevel serverLevel, LivingEntity target) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.4F, 0.6F);

        for (int i = -1; i <= 1; i += 2) {
            EspiritoNevoaEntity shadow = CaosArcanaEntities.ESPIRITO_NEVOA.create(serverLevel);
            if (shadow != null) {
                double offsetX = i * 2.8D + (this.getRandom().nextDouble() - 0.5D);
                double offsetZ = (this.getRandom().nextDouble() - 0.5D) * 2.5D;
                shadow.moveTo(
                        target.getX() + offsetX,
                        target.getY() + 1.2D,
                        target.getZ() + offsetZ,
                        this.getYRot(),
                        0.0F
                );
                shadow.setCustomName(Component.literal("Sombra do Ancião"));
                if (shadow.getAttribute(Attributes.MAX_HEALTH) != null) {
                    shadow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0D);
                    shadow.setHealth(8.0F);
                }
                shadow.setTarget(target);
                serverLevel.addFreshEntity(shadow);
                serverLevel.sendParticles(
                        ParticleTypes.SQUID_INK,
                        shadow.getX(), shadow.getY() + 0.5D, shadow.getZ(),
                        16, 0.35D, 0.45D, 0.35D, 0.04D
                );
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        shadow.getX(), shadow.getY() + 0.5D, shadow.getZ(),
                        12, 0.35D, 0.45D, 0.35D, 0.03D
                );
            }
        }
    }

    private void tickLeafStormWindup(ServerLevel serverLevel, LivingEntity target) {
        this.leafStormWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        double radius = 5.5D;
        for (int i = 0; i < 14; i++) {
            double angle = (Math.PI * 2.0D * i) / 14.0D + (this.tickCount * 0.15D);
            double px = this.getX() + Math.cos(angle) * radius;
            double pz = this.getZ() + Math.sin(angle) * radius;
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_LEAVES.defaultBlockState()),
                    px, this.getY() + 0.6D, pz, 1, 0.15D, 0.3D, 0.15D, 0.04D
            );
            if (i % 2 == 0) {
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        px, this.getY() + 0.8D, pz, 1, 0.1D, 0.2D, 0.1D, 0.02D
                );
            }
        }

        if (this.leafStormWindup == 0) {
            performLeafStorm(serverLevel, target);
            this.setCastState(CAST_NONE);
        }
    }

    private void performLeafStorm(ServerLevel serverLevel, LivingEntity target) {
        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5F, 0.55F);
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_LEAVES.defaultBlockState()),
                this.getX(), this.getY() + 1.5D, this.getZ(),
                75, 2.8D, 1.2D, 2.8D, 0.18D
        );
        serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                this.getX(), this.getY() + 1.5D, this.getZ(),
                35, 2.5D, 1.0D, 2.5D, 0.12D
        );

        // Dano em área no raio anunciado de 5.5 blocos
        for (LivingEntity entity : serverLevel.getEntitiesOfClass(
                LivingEntity.class, this.getBoundingBox().inflate(5.5D),
                e -> e != this && !(e instanceof EspiritoNevoaEntity) && e.isAlive())) {
            if (this.distanceToSqr(entity) <= 30.25D) {
                entity.hurt(this.damageSources().mobAttack(this), 8.0F);
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0), this);
                entity.knockback(1.1D, this.getX() - entity.getX(), this.getZ() - entity.getZ());
            }
        }

        // Cria 2 zonas de folhas venenosas que fecham partes da arena
        for (int i = 0; i < 2; i++) {
            double angle = this.getRandom().nextDouble() * Math.PI * 2.0D;
            double dist = 2.5D + this.getRandom().nextDouble() * 3.0D;
            double cx = target.getX() + Math.cos(angle) * dist;
            double cz = target.getZ() + Math.sin(angle) * dist;

            AreaEffectCloud cloud = new AreaEffectCloud(serverLevel, cx, target.getY(), cz);
            cloud.setOwner(this);
            cloud.setParticle(ParticleTypes.DRAGON_BREATH);
            cloud.setFixedColor(0x1E9E48);
            cloud.setRadius(2.4F);
            cloud.setDuration(110);
            cloud.setWaitTime(10);
            cloud.setRadiusPerTick(-0.008F);
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
            cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
            serverLevel.addFreshEntity(cloud);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0), this);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                        target.getX(), target.getY() + 0.6D, target.getZ(),
                        18, 0.4D, 0.3D, 0.4D, 0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        target.getX(), target.getY() + 0.8D, target.getZ(),
                        10, 0.35D, 0.3D, 0.35D, 0.04D
                );
            }
        }
        return hit;
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
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
