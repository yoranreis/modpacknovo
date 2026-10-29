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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Faraó do Sol Eterno — Chefão do Deserto das Dunas Douradas.
 * - Raio Solar Incandescente: Canaliza o cajado para disparar um feixe de luz guiado que persegue o alvo.
 * - Redemoinho de Areia: Tempestade de vento localizada que puxa o jogador para o centro da arena.
 * - Invocação de Guardiões (Fase 2): Convoca ondas de Múmias Esqueléticas para regenerar o chefe e desviar o foco.
 */
public final class FaraoDoSolEternoEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(FaraoDoSolEternoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST_STATE =
            SynchedEntityData.defineId(FaraoDoSolEternoEntity.class, EntityDataSerializers.INT);

    public static final int CAST_NONE = 0;
    public static final int CAST_SOLAR_BEAM = 1;
    public static final int CAST_SAND_WHIRLWIND = 2;
    public static final int CAST_SUMMON_GUARDIANS = 3;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.caosarcana.farao_do_sol_eterno"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int solarBeamCooldown = 70;
    private int solarBeamWindup = 0;
    private int solarBeamActiveTicks = 0;
    private Vec3 guidedBeamPos = Vec3.ZERO;

    private int whirlwindCooldown = 130;
    private int whirlwindWindup = 0;
    private int whirlwindActiveTicks = 0;

    private int summonGuardiansCooldown = 160;
    private int summonGuardiansWindup = 0;

    public FaraoDoSolEternoEntity(EntityType<? extends FaraoDoSolEternoEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 65;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 340.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 9.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.9D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 9.0D);
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
        return MobType.UNDEAD;
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
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_FIRE)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            spawnClientSolarAura();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float healthFraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthFraction);
        updatePhase(serverLevel, healthFraction);

        // Na Fase 2, Múmias Esqueléticas vivas na arena canalizam regeneração contínua ao Faraó
        if (getPhase() >= 2 && this.tickCount % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
            List<MumiaEsqueleticaEntity> activeGuardians = serverLevel.getEntitiesOfClass(
                    MumiaEsqueleticaEntity.class,
                    this.getBoundingBox().inflate(24.0D),
                    LivingEntity::isAlive
            );
            if (!activeGuardians.isEmpty()) {
                this.heal(activeGuardians.size() * 1.5F);
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        this.getX(), this.getY() + 1.8D, this.getZ(),
                        6, 0.5D, 0.6D, 0.5D, 0.02D
                );
            }
        }

        if (this.solarBeamCooldown > 0) this.solarBeamCooldown--;
        if (this.whirlwindCooldown > 0) this.whirlwindCooldown--;
        if (this.summonGuardiansCooldown > 0) this.summonGuardiansCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            resetSkillStates();
            return;
        }

        // Processa habilidades ativas / windups
        if (this.solarBeamWindup > 0 || this.solarBeamActiveTicks > 0) {
            tickSolarBeam(serverLevel, target);
            return;
        }
        if (this.whirlwindWindup > 0 || this.whirlwindActiveTicks > 0) {
            tickSandWhirlwind(serverLevel);
            return;
        }
        if (this.summonGuardiansWindup > 0) {
            tickSummonGuardians(serverLevel, target);
            return;
        }

        this.setCastState(CAST_NONE);
        double distSqr = this.distanceToSqr(target);

        // 1. Invocação de Guardiões (Fase 2)
        if (getPhase() >= 2 && this.summonGuardiansCooldown == 0 && distSqr <= 324.0D) {
            int activeMummies = serverLevel.getEntitiesOfClass(
                    MumiaEsqueleticaEntity.class,
                    this.getBoundingBox().inflate(24.0D)
            ).size();
            if (activeMummies < 4) {
                this.summonGuardiansCooldown = 240;
                this.summonGuardiansWindup = 26;
                this.setCastState(CAST_SUMMON_GUARDIANS);
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.5F, 0.7F);
                return;
            }
        }

        // 2. Redemoinho de Areia (puxa o jogador para o centro da arena)
        if (this.whirlwindCooldown == 0 && distSqr >= 16.0D && distSqr <= 196.0D) {
            this.whirlwindCooldown = 190;
            this.whirlwindWindup = 20;
            this.setCastState(CAST_SAND_WHIRLWIND);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BREEZE_IDLE_GROUND, SoundSource.HOSTILE, 1.4F, 0.65F);
            return;
        }

        // 3. Raio Solar Incandescente (feixe de luz guiado que persegue o alvo)
        if (this.solarBeamCooldown == 0 && distSqr >= 9.0D && distSqr <= 324.0D && this.hasLineOfSight(target)) {
            this.solarBeamCooldown = 140;
            this.solarBeamWindup = 22;
            this.setCastState(CAST_SOLAR_BEAM);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.4F, 1.25F);
        }
    }

    private void spawnClientSolarAura() {
        if (this.getRandom().nextInt(2) == 0) {
            this.level().addParticle(
                    ParticleTypes.WAX_ON,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.3D,
                    this.getY() + 0.5D + this.getRandom().nextDouble() * 2.2D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.3D,
                    0.0D, 0.03D, 0.0D
            );
        }
        if (this.getRandom().nextInt(4) == 0) {
            this.level().addParticle(
                    ParticleTypes.FLAME,
                    this.getX() + (this.getRandom().nextDouble() - 0.5D) * 1.1D,
                    this.getY() + 1.2D + this.getRandom().nextDouble() * 1.4D,
                    this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 1.1D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    private void updatePhase(ServerLevel serverLevel, float healthFraction) {
        if (healthFraction <= 0.60F && getPhase() < 2) {
            setPhase(2);
            this.bossEvent.setColor(BossEvent.BossBarColor.RED);
            this.summonGuardiansCooldown = 0; // Dispara a primeira onda de Guardiões na entrada da Fase 2
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.2F, 0.85F);
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX(), this.getY() + 1.6D, this.getZ(),
                    50, 1.2D, 1.1D, 1.2D, 0.08D
            );
            serverLevel.sendParticles(
                    ParticleTypes.TOTEM_OF_UNDYING,
                    this.getX(), this.getY() + 1.6D, this.getZ(),
                    40, 1.1D, 1.0D, 1.1D, 0.1D
            );
        }
    }

    private void resetSkillStates() {
        this.solarBeamWindup = 0;
        this.solarBeamActiveTicks = 0;
        this.whirlwindWindup = 0;
        this.whirlwindActiveTicks = 0;
        this.summonGuardiansWindup = 0;
        this.setCastState(CAST_NONE);
    }

    private void tickSolarBeam(ServerLevel serverLevel, LivingEntity target) {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        Vec3 staffTip = this.position().add(0.0D, 2.35D, 0.0D);

        if (this.solarBeamWindup > 0) {
            this.solarBeamWindup--;
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    staffTip.x, staffTip.y, staffTip.z,
                    4, 0.25D, 0.25D, 0.25D, 0.04D
            );
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    staffTip.x, staffTip.y, staffTip.z,
                    3, 0.2D, 0.2D, 0.2D, 0.02D
            );

            if (this.solarBeamWindup == 0) {
                this.solarBeamActiveTicks = 36;
                Vec3 dir = target.getEyePosition().subtract(staffTip).normalize();
                this.guidedBeamPos = staffTip.add(dir.scale(2.5D));
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 1.5F, 0.8F);
            }
            return;
        }

        if (this.solarBeamActiveTicks > 0) {
            this.solarBeamActiveTicks--;
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 toTarget = targetCenter.subtract(this.guidedBeamPos);
            double dist = toTarget.length();
            if (dist > 0.05D) {
                this.guidedBeamPos = this.guidedBeamPos.add(toTarget.normalize().scale(Math.min(dist, 0.52D)));
            }

            // Desenha o feixe solar entre a ponta do cajado e o ponto guiado
            Vec3 beamVec = this.guidedBeamPos.subtract(staffTip);
            int steps = Math.max(4, (int) (beamVec.length() * 2.5D));
            for (int i = 0; i <= steps; i++) {
                double t = (double) i / (double) steps;
                Vec3 pt = staffTip.add(beamVec.scale(t));
                if (i % 2 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, pt.x, pt.y, pt.z, 1, 0.04D, 0.04D, 0.04D, 0.0D);
                }
            }
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.guidedBeamPos.x, this.guidedBeamPos.y, this.guidedBeamPos.z,
                    5, 0.25D, 0.25D, 0.25D, 0.03D
            );

            // Verifica impacto do feixe guiado
            AABB hitBox = new AABB(
                    this.guidedBeamPos.x - 0.9D, this.guidedBeamPos.y - 0.9D, this.guidedBeamPos.z - 0.9D,
                    this.guidedBeamPos.x + 0.9D, this.guidedBeamPos.y + 0.9D, this.guidedBeamPos.z + 0.9D
            );
            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class, hitBox,
                    e -> e != this && !(e instanceof MumiaEsqueleticaEntity) && e.isAlive())) {
                if (this.solarBeamActiveTicks % 6 == 0) {
                    victim.hurt(this.damageSources().indirectMagic(this, this), 6.0F);
                    victim.setSecondsOnFire(4);
                }
            }

            if (this.solarBeamActiveTicks == 0) {
                this.setCastState(CAST_NONE);
            }
        }
    }

    private void tickSandWhirlwind(ServerLevel serverLevel) {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        if (this.whirlwindWindup > 0) {
            this.whirlwindWindup--;
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2.0D * i) / 8.0D + (this.tickCount * 0.2D);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        this.getX() + Math.cos(angle) * 3.5D,
                        this.getY() + 0.5D,
                        this.getZ() + Math.sin(angle) * 3.5D,
                        1, 0.1D, 0.2D, 0.1D, 0.04D
                );
            }
            if (this.whirlwindWindup == 0) {
                this.whirlwindActiveTicks = 50; // 2.5s de vórtice puxando o jogador
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.HOSTILE, 1.5F, 0.7F);
            }
            return;
        }

        if (this.whirlwindActiveTicks > 0) {
            this.whirlwindActiveTicks--;
            for (int i = 0; i < 12; i++) {
                double angle = (Math.PI * 2.0D * i) / 12.0D + (this.tickCount * 0.35D);
                double r = 1.5D + (i % 4) * 1.8D;
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        this.getX() + Math.cos(angle) * r,
                        this.getY() + 0.3D + (i % 3) * 0.6D,
                        this.getZ() + Math.sin(angle) * r,
                        1, 0.15D, 0.25D, 0.15D, 0.08D
                );
            }

            // Puxa jogadores num raio de 10 blocos para o centro da arena
            for (Player player : serverLevel.getEntitiesOfClass(
                    Player.class, this.getBoundingBox().inflate(10.0D),
                    p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
                Vec3 pull = this.position().subtract(player.position());
                double dist = pull.horizontalDistance();
                if (dist > 1.5D && dist <= 10.0D) {
                    Vec3 pullStep = pull.normalize().scale(0.14D);
                    player.setDeltaMovement(player.getDeltaMovement().add(pullStep.x, 0.02D, pullStep.z));
                    player.hurtMarked = true;
                }
            }

            if (this.whirlwindActiveTicks == 0) {
                this.setCastState(CAST_NONE);
            }
        }
    }

    private void tickSummonGuardians(ServerLevel serverLevel, LivingEntity target) {
        this.summonGuardiansWindup--;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);

        serverLevel.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                this.getX(), this.getY() + 1.6D, this.getZ(),
                4, 0.8D, 0.8D, 0.8D, 0.05D
        );

        if (this.summonGuardiansWindup == 0) {
            this.setCastState(CAST_NONE);
            // Regenera o Faraó e invoca 2 Múmias Esqueléticas Guardiãs
            this.heal(16.0F);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.5F, 0.75F);

            for (int i = -1; i <= 1; i += 2) {
                MumiaEsqueleticaEntity mummy = CaosArcanaEntities.MUMIA_ESQUELETICA.create(serverLevel);
                if (mummy != null) {
                    double sx = this.getX() + i * 2.6D;
                    double sz = this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 2.4D;
                    mummy.moveTo(sx, this.getY(), sz, this.getYRot(), 0.0F);
                    mummy.setCustomName(Component.literal("Guardião do Faraó"));
                    mummy.setTarget(target);
                    serverLevel.addFreshEntity(mummy);
                    serverLevel.sendParticles(
                            new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                            sx, this.getY() + 0.8D, sz,
                            24, 0.4D, 0.6D, 0.4D, 0.08D
                    );
                }
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.GOLD_BLOCK, 1 + this.random.nextInt(2));
        this.spawnAtLocation(Items.TOTEM_OF_UNDYING, 1);
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
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BLAZE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
