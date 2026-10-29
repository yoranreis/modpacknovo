package br.com.caosarcana.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class CultistaOcultoEntity extends PathfinderMob {
    private static final int CAST_COOLDOWN_TICKS = 60;   // 3s entre disparos
    private static final int SUMMON_COOLDOWN_TICKS = 260; // 13s entre invocações
    private static final double MAX_CAST_RANGE = 18.0D;
    private static final double MIN_SAFE_DISTANCE = 6.0D;

    private int castCooldown = 40;
    private int castWindup = 0;

    private int summonCooldown = 120;
    private int summonWindup = 0;

    public CultistaOcultoEntity(EntityType<? extends CultistaOcultoEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 22.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 12.0F));
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

        // Efeitos visuais no cliente: brilho da máscara e do orbe do cajado
        if (this.level().isClientSide) {
            // Brilho na máscara de crânio
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                        this.getY() + 1.6D + (this.random.nextDouble() - 0.5D) * 0.2D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                        0.0D, 0.01D, 0.0D);
            }
            // Brilho no topo do cajado (mão direita erguida)
            if (this.random.nextInt(2) == 0) {
                double staffX = this.getX() - 0.45D;
                double staffY = this.getY() + 1.8D;
                double staffZ = this.getZ() - 0.25D;
                this.level().addParticle(ParticleTypes.SCULK_SOUL,
                        staffX + (this.random.nextDouble() - 0.5D) * 0.15D,
                        staffY + (this.random.nextDouble() - 0.5D) * 0.15D,
                        staffZ + (this.random.nextDouble() - 0.5D) * 0.15D,
                        0.0D, 0.02D, 0.0D);
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (this.castCooldown > 0) this.castCooldown--;
        if (this.summonCooldown > 0) this.summonCooldown--;

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            this.castWindup = 0;
            this.summonWindup = 0;
            return;
        }

        double distSqr = this.distanceToSqr(target);
        double dist = Math.sqrt(distSqr);

        // 1. Manutenção de distância tática (recua se o jogador chegar muito perto)
        if (dist < MIN_SAFE_DISTANCE) {
            double awayX = this.getX() - (target.getX() - this.getX());
            double awayZ = this.getZ() - (target.getZ() - this.getZ());
            this.getNavigation().moveTo(awayX, this.getY(), awayZ, 1.25D);
        } else if (dist > 14.0D) {
            this.getNavigation().moveTo(target, 1.0D);
        }

        // 2. FASE DE CONJURAÇÃO DE INVOCAÇÃO (Suporte)
        if (this.summonWindup > 0) {
            this.summonWindup--;
            this.getNavigation().stop();
            // Partículas concentrando-se no orbe do cajado
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 2.0D, this.getZ(),
                    4, 0.3D, 0.3D, 0.3D, 0.02D);
            if (this.summonWindup == 0) {
                performSummon(serverLevel);
            }
            return;
        }

        // 3. FASE DE CONJURAÇÃO DE RAJADA DE MALDIÇÃO (Ataque)
        if (this.castWindup > 0) {
            this.castWindup--;
            this.getNavigation().stop();
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() - 0.4D, this.getY() + 1.8D, this.getZ(),
                    2, 0.15D, 0.15D, 0.15D, 0.01D);
            if (this.castWindup == 0) {
                performCast(target, serverLevel);
            }
            return;
        }

        // Gatilho de Invocação: se cooldown zerado e menos de 2 espíritos ativos por perto
        if (this.summonCooldown == 0 && distSqr <= (MAX_CAST_RANGE * MAX_CAST_RANGE) && this.hasLineOfSight(target)) {
            AABB searchBox = this.getBoundingBox().inflate(16.0D);
            List<EspiritoNevoaEntity> nearbySpirits = serverLevel.getEntitiesOfClass(EspiritoNevoaEntity.class, searchBox);
            if (nearbySpirits.size() < 2) {
                this.summonCooldown = SUMMON_COOLDOWN_TICKS;
                this.summonWindup = 22; // ~1.1s de telegrafia
                serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0F, 0.9F);
                return;
            }
        }

        // Gatilho de Ataque com Projétil
        if (this.castCooldown == 0 && distSqr <= (MAX_CAST_RANGE * MAX_CAST_RANGE) && this.hasLineOfSight(target)) {
            this.castCooldown = CAST_COOLDOWN_TICKS;
            this.castWindup = 12; // ~0.6s de aviso
            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 0.8F, 1.1F);
        }
    }

    private void performCast(LivingEntity target, ServerLevel serverLevel) {
        double dx = target.getX() - (this.getX() - 0.4D);
        double dy = target.getY(0.5D) - (this.getY() + 1.6D);
        double dz = target.getZ() - this.getZ();

        MaldicaoOcultaProjectile projectile = new MaldicaoOcultaProjectile(this.level(), this);
        projectile.setPos(this.getX() - 0.4D, this.getY() + 1.6D, this.getZ());
        projectile.shoot(dx, dy, dz, 1.15F, 1.0F);
        serverLevel.addFreshEntity(projectile);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITCH_THROW, SoundSource.HOSTILE, 1.0F, 1.1F);
    }

    private void performSummon(ServerLevel serverLevel) {
        // Invoca 1 espírito da névoa nas proximidades para flutuar e atacar o alvo
        BlockPos spawnPos = this.blockPosition().offset(
                this.random.nextInt(5) - 2,
                1,
                this.random.nextInt(5) - 2
        );

        EspiritoNevoaEntity spirit = CaosArcanaEntities.ESPIRITO_NEVOA.create(serverLevel);
        if (spirit != null) {
            spirit.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, this.getYRot(), 0.0F);
            spirit.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spawnPos), MobSpawnType.MOB_SUMMONED, null, null);
            if (this.getTarget() != null) {
                spirit.setTarget(this.getTarget());
            }
            serverLevel.addFreshEntity(spirit);

            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D,
                    15, 0.4D, 0.4D, 0.4D, 0.03D);
            serverLevel.playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(),
                    SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
}
