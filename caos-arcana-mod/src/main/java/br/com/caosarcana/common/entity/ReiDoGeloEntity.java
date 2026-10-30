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

/**
 * O Rei do Gelo — Chefão da Muralha de Gelo.
 * - Fase 1 (Domínio do Solo): Erupções de Gelo em linha + Pisão Colossal em área.
 * - Fase 2 (Artilharia Pesada e Clima): Bombardeio de Neve explosivo + Nevasca Violenta que reduz visibilidade/velocidade e cura o Rei.
 * - Fase 3 (Fúria Final): Estátua de Gelo que aprisiona o jogador em gelo maciço antes do golpe final.
 */
public final class ReiDoGeloEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(ReiDoGeloEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CAST_STATE =
            SynchedEntityData.defineId(ReiDoGeloEntity.class, EntityDataSerializers.INT);

    public static final int CAST_NONE = 0;
    public static final int CAST_ICE_ERUPTION = 1;
    public static final int CAST_COLOSSAL_STOMP = 2;
    public static final int CAST_SNOW_BOMBARDMENT = 3;
    public static final int CAST_VIOLENT_BLIZZARD = 4;
    public static final int CAST_ICE_STATUE = 5;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.caosarcana.rei_do_gelo"),
            BossEvent.BossBarColor.BLUE,
            BossEvent.BossBarOverlay.NOTCHED_10
    );

    private int iceEruptionCooldown = 65;
    private int iceEruptionWindup = 0;

    private int colossalStompCooldown = 90;
    private int colossalStompWindup = 0;

    private int snowBombardmentCooldown = 110;
    private int snowBombardmentWindup = 0;

    private int blizzardCooldown = 140;
    private int blizzardActiveTicks = 0;

    private int iceStatueCooldown = 160;
    private int iceStatueWindup = 0;

    public ReiDoGeloEntity(EntityType<? extends ReiDoGeloEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 85;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 380.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 11.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.3D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.92D)
                .add(Attributes.FOLLOW_RANGE, 42.0D)
                .add(Attributes.ARMOR, 11.0D);
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
    public boolean canFreeze() {
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
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.65D));
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
        if (source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypeTags.IS_FREEZING)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            spawnClientGlacialAura();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        float healthFraction = this.getHealth() / this.getMaxHealth();
        this.bossEvent.setProgress(healthFraction);
        updatePhase(serverLevel, healthFraction);

        if (this.iceEruptionCooldown > 0) this.iceEruptionCooldown--;
        if (this.colossalStompCooldown > 0) this.colossalStompCooldown--;
        if (this.snowBombardmentCooldown > 0) this.snowBombardmentCooldown--;
        if (this.blizzardCooldown > 0) this.blizzardCooldown--;
        if (this.iceStatueCooldown > 0) this.iceStatueCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            resetSkillStates();
            return;
        }

        if (this.iceEruptionWindup > 0) {
            tickIceEruption(serverLevel, target);
            return;
        }
        if (this.colossalStompWindup > 0) {
            tickColossalStomp(serverLevel);
            return;
        }
        if (this.snowBombardmentWindup > 0) {
            tickSnowBombardment(serverLevel, target);
            return;
        }
        if (this.blizzardActiveTicks > 0) {
            tickViolentBlizzard(serverLevel);
            return;
        }
        if (this.iceStatueWindup > 0) {
            tickIceStatue(serverLevel, target);
            return;
        }

        this.setCastState(CAST_NONE);
        double distSqr = this.distanceToSqr(target);

        // Fase 3: Estátua de Gelo (aprisiona o jogador em cubo de gelo antes do golpe final)
        if (getPhase() >= 3 && this.iceStatueCooldown == 0 && distSqr <= 196.0D && this.hasLineOfSight(target)) {
            this.iceStatueCooldown = 210;
            this.iceStatueWindup = 28;
            this.setCastState(CAST_ICE_STATUE);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.5F, 0.65F);
            return;
        }

        // Fase 2+: Nevasca Violenta (reduz visibilidade e velocidade, curando o Rei lentamente)
        if (getPhase() >= 2 && this.blizzardCooldown == 0 && distSqr <= 256.0D) {
            this.blizzardCooldown = 220;
            this.blizzardActiveTicks = 60;
            this.setCastState(CAST_VIOLENT_BLIZZARD);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.5F, 0.6F);
            return;
        }

        // Fase 2+: Bombardeio de Neve (lança blocos de neve/gelo que explodem em áreas gélidas)
        if (getPhase() >= 2 && this.snowBombardmentCooldown == 0 && distSqr >= 16.0D && distSqr <= 324.0D) {
            this.snowBombardmentCooldown = 130;
            this.snowBombardmentWindup = 20;
            this.setCastState(CAST_SNOW_BOMBARDMENT);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.4F, 0.75F);
            return;
        }

        // Fase 1+: Pisão Colossal (ataque corpo a corpo sísmico em área)
        if (this.colossalStompCooldown == 0 && distSqr <= 25.0D) {
            this.colossalStompCooldown = 110;
            this.colossalStompWindup = 18;
            this.setCastState(CAST_COLOSSAL_STOMP);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 1.2F, 0.6F);
            return;
        }

        // Fase 1+: Erupções de Gelo (estacas afiadas surgem do chão em onda de choque)
        if (this.iceEruptionCooldown == 0 && distSqr >= 9.0D && distSqr <= 256.0D && this.hasLineOfSight(target)) {
            this.iceEruptionCooldown = 95;
            this.iceEruptionWindup = 20;
            this.setCastState(CAST_ICE_ERUPTION);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.3F, 0.75F);
        }
    }

    private void spawnClientGlacialAura() {
        if (this.random.nextInt(2) == 0) {
            this.level().addParticle(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.6D,
                    this.getY() + 0.5D + this.random.nextDouble() * 2.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.6D,
                    0.0D, -0.02D, 0.0D
            );
        }
        if (this.random.nextInt(4) == 0) {
            this.level().addParticle(
                    ParticleTypes.END_ROD,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 1.2D,
                    this.getY() + 1.5D + this.random.nextDouble() * 1.5D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 1.2D,
                    0.0D, 0.02D, 0.0D
            );
        }
    }

    private void updatePhase(ServerLevel serverLevel, float healthFraction) {
        if (healthFraction <= 0.30F && getPhase() < 3) {
            setPhase(3);
            this.bossEvent.setColor(BossEvent.BossBarColor.PURPLE);
            this.iceStatueCooldown = 0;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.4F, 1.15F);
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.8D, this.getZ(),
                    65, 1.5D, 1.4D, 1.5D, 0.1D
            );
        } else if (healthFraction <= 0.65F && getPhase() < 2) {
            setPhase(2);
            this.bossEvent.setColor(BossEvent.BossBarColor.WHITE);
            this.blizzardCooldown = 0;
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.4F, 0.75F);
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.6D, this.getZ(),
                    50, 1.4D, 1.2D, 1.4D, 0.08D
            );
        }
    }

    private void resetSkillStates() {
        this.iceEruptionWindup = 0;
        this.colossalStompWindup = 0;
        this.snowBombardmentWindup = 0;
        this.blizzardActiveTicks = 0;
        this.iceStatueWindup = 0;
        this.setCastState(CAST_NONE);
    }

    private void tickIceEruption(ServerLevel serverLevel, LivingEntity target) {
        this.iceEruptionWindup--;
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.iceEruptionWindup == 0) {
            this.setCastState(CAST_NONE);
            Vec3 dir = target.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize();
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 0.6F);

            for (int step = 2; step <= 14; step += 2) {
                Vec3 spikePos = this.position().add(dir.scale(step));
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                        spikePos.x, spikePos.y + 0.8D, spikePos.z,
                        22, 0.45D, 0.9D, 0.45D, 0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.SNOWFLAKE,
                        spikePos.x, spikePos.y + 1.0D, spikePos.z,
                        12, 0.4D, 0.8D, 0.4D, 0.04D
                );

                AABB hitBox = new AABB(
                        spikePos.x - 1.3D, spikePos.y - 1.0D, spikePos.z - 1.3D,
                        spikePos.x + 1.3D, spikePos.y + 2.5D, spikePos.z + 1.3D
                );
                for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                        LivingEntity.class, hitBox, e -> e != this && e.isAlive())) {
                    victim.hurt(this.damageSources().indirectMagic(this, this), 8.5F);
                    victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 70);
                    victim.push(0.0D, 0.35D, 0.0D);
                    victim.hurtMarked = true;
                }
            }
        }
    }

    private void tickColossalStomp(ServerLevel serverLevel) {
        this.colossalStompWindup--;
        this.getNavigation().stop();

        if (this.colossalStompWindup == 0) {
            this.setCastState(CAST_NONE);
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.4F, 0.85F);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                    this.getX(), this.getY() + 0.3D, this.getZ(),
                    65, 2.5D, 0.5D, 2.5D, 0.18D
            );

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class, this.getBoundingBox().inflate(5.2D),
                    e -> e != this && e.isAlive())) {
                victim.hurt(this.damageSources().mobAttack(this), 11.0F);
                Vec3 away = victim.position().subtract(this.position()).multiply(1.0D, 0.0D, 1.0D).normalize().scale(1.3D);
                victim.push(away.x, 0.48D, away.z);
                victim.hurtMarked = true;
            }
        }
    }

    private void tickSnowBombardment(ServerLevel serverLevel, LivingEntity target) {
        this.snowBombardmentWindup--;
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (this.snowBombardmentWindup == 0) {
            this.setCastState(CAST_NONE);
            Vec3 impact = target.position();
            serverLevel.playSound(null, impact.x, impact.y, impact.z,
                    SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2F, 1.2F);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SNOW_BLOCK.defaultBlockState()),
                    impact.x, impact.y + 0.8D, impact.z,
                    55, 2.2D, 0.9D, 2.2D, 0.15D
            );
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    impact.x, impact.y + 0.8D, impact.z,
                    40, 2.2D, 0.8D, 2.2D, 0.08D
            );

            for (LivingEntity victim : serverLevel.getEntitiesOfClass(
                    LivingEntity.class, target.getBoundingBox().inflate(3.5D),
                    e -> e != this && e.isAlive())) {
                victim.hurt(this.damageSources().freeze(), 8.0F);
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 2), this);
                victim.setTicksFrozen(victim.getTicksRequiredToFreeze() + 100);
            }
        }
    }

    private void tickViolentBlizzard(ServerLevel serverLevel) {
        this.blizzardActiveTicks--;
        this.getNavigation().stop();

        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2.0D * i) / 16.0D + (this.tickCount * 0.3D);
            double r = 2.0D + (i % 4) * 2.2D;
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    this.getX() + Math.cos(angle) * r,
                    this.getY() + 0.5D + (i % 3) * 0.8D,
                    this.getZ() + Math.sin(angle) * r,
                    2, 0.2D, 0.3D, 0.2D, 0.06D
            );
        }

        // Cura o Rei lentamente durante a nevasca e aplica cegueira/lentidão aos jogadores
        if (this.blizzardActiveTicks % 15 == 0) {
            this.heal(4.5F);
            for (Player player : serverLevel.getEntitiesOfClass(
                    Player.class, this.getBoundingBox().inflate(10.0D),
                    p -> p.isAlive() && !p.isSpectator() && !p.isCreative())) {
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0), this);
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 1), this);
                player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 80, player.getTicksFrozen() + 45));
            }
        }

        if (this.blizzardActiveTicks == 0) {
            this.setCastState(CAST_NONE);
        }
    }

    private void tickIceStatue(ServerLevel serverLevel, LivingEntity target) {
        this.iceStatueWindup--;
        this.getNavigation().stop();
        this.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Envolve o alvo em um cubo denso de partículas de gelo durante o carregamento
        serverLevel.sendParticles(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                target.getX(), target.getY() + 1.0D, target.getZ(),
                10, 0.6D, 1.0D, 0.6D, 0.02D
        );
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 5), this);
        target.setTicksFrozen(target.getTicksRequiredToFreeze() + 120);
        target.setDeltaMovement(0.0D, Math.min(0.0D, target.getDeltaMovement().y), 0.0D);
        target.hurtMarked = true;

        if (this.iceStatueWindup == 0) {
            this.setCastState(CAST_NONE);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.5F, 0.55F);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BLUE_ICE.defaultBlockState()),
                    target.getX(), target.getY() + 1.0D, target.getZ(),
                    45, 0.8D, 1.1D, 0.8D, 0.15D
            );
            target.hurt(this.damageSources().freeze(), 10.5F);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.BLUE_ICE, 4 + this.random.nextInt(4));
        this.spawnAtLocation(Items.DIAMOND, 2 + this.random.nextInt(2 + looting));
        this.spawnAtLocation(Items.NETHERITE_SCRAP, 1);
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
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
