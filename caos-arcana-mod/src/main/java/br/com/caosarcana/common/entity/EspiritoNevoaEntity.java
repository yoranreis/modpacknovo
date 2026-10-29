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
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

public final class EspiritoNevoaEntity extends PathfinderMob {
    private static final int ATTACK_COOLDOWN_TICKS = 90; // ~4.5 segundos entre disparos
    private static final double MAX_ATTACK_RANGE = 14.0D;
    private static final double MAX_HEIGHT_ABOVE_GROUND = 4.0D;

    private int attackCooldown = 30;
    private int attackWindup = 0;

    public EspiritoNevoaEntity(EntityType<? extends EspiritoNevoaEntity> type, Level level) {
        super(type, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    // Mob fraco: 14 HP (7 corações), sem armadura, dano base baixo
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FLYING_SPEED, 0.35D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.ARMOR, 0.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Patrulha a área voando suavemente entre os obstáculos
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
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

    /**
     * Localiza a altura real do chão sólido abaixo da entidade
     */
    private double getGroundY() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
        int minY = Math.max(this.level().getMinBuildHeight(), this.getBlockY() - 24);
        for (int y = this.getBlockY(); y >= minY; y--) {
            pos.setY(y);
            if (!this.level().getBlockState(pos).isAir() && this.level().getBlockState(pos).blocksMotion()) {
                return y + 1.0D;
            }
        }
        return this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, this.blockPosition()).getY();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.setNoGravity(true);

        // Partículas constantes no cliente representando o olho verde brilhante e núcleo de alma
        if (this.level().isClientSide) {
            double eyeX = this.getX() + 0.15D;
            double eyeY = this.getY() + 0.95D;
            double eyeZ = this.getZ() - 0.15D;

            if (this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        eyeX + (this.random.nextDouble() - 0.5D) * 0.15D,
                        eyeY + (this.random.nextDouble() - 0.5D) * 0.15D,
                        eyeZ + (this.random.nextDouble() - 0.5D) * 0.15D,
                        0.0D, 0.01D, 0.0D);
            }

            // Névoa tênue saindo do corpo/peito
            if (this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.5D,
                        this.getY() + 0.4D + (this.random.nextDouble() - 0.5D) * 0.3D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.5D,
                        0.0D, 0.01D, 0.0D);
            }
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        LivingEntity target = this.getTarget();

        // CONTROLE ESTRITO DE ALTURA DE VOO (NUNCA MAIS DE 4 BLOCOS DE ALTURA)
        if (target != null && target.isAlive()) {
            double playerY = target.getY();
            double currentY = this.getY();
            double heightDiff = currentY - playerY;

            // Se subir mais do que 4 blocos acima do jogador, força queda imediata
            if (heightDiff > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.15D, this.getDeltaMovement().z);
            } else if (heightDiff > 2.8D) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.04D, this.getDeltaMovement().z);
            } else if (heightDiff < 1.0D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.04D, this.getDeltaMovement().z);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * 0.8D, this.getDeltaMovement().z);
            }

            // Manter distância tática de combate
            double distSqr = this.distanceToSqr(target.getX(), this.getY(), target.getZ());
            if (distSqr > 64.0D) {
                this.getNavigation().moveTo(target, 1.0D);
            } else if (distSqr < 6.25D) {
                double backX = this.getX() - (target.getX() - this.getX());
                double backZ = this.getZ() - (target.getZ() - this.getZ());
                this.getNavigation().moveTo(backX, this.getY(), backZ, 0.85D);
            }
        } else {
            // SEM ALVO: Patrulha a área respeitando o teto de 4 blocos acima do chão
            double groundY = getGroundY();
            double currentY = this.getY();
            double heightAboveGround = currentY - groundY;

            // Limite rígido de 4 blocos acima do chão
            if (heightAboveGround > MAX_HEIGHT_ABOVE_GROUND) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.16D, this.getDeltaMovement().z);
            } else if (heightAboveGround > 3.0D) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.05D, this.getDeltaMovement().z);
            } else if (heightAboveGround < 1.4D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.04D, this.getDeltaMovement().z);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().x, this.getDeltaMovement().y * 0.75D, this.getDeltaMovement().z);
            }

            // Trava rígida de subida sem alvo (impede qualquer subida infinita)
            if (this.getDeltaMovement().y > 0.06D) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.06D, this.getDeltaMovement().z);
            }
        }

        // CICLO DE ATAQUE MÁGICO TELEGRAFADO
        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (target == null || !target.isAlive()) {
            this.attackWindup = 0;
            return;
        }

        double distSqr = this.distanceToSqr(target);

        // Fase de telegrafia (windup de aviso antes de atirar)
        if (this.attackWindup > 0) {
            this.attackWindup--;
            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y * 0.5D, 0.0D);

            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY() + 0.6D, this.getZ(),
                    3, 0.2D, 0.2D, 0.2D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY() + 0.6D, this.getZ(),
                    2, 0.15D, 0.15D, 0.15D, 0.01D);

            if (this.attackWindup == 0) {
                performRangedAttack(target, serverLevel);
            }
            return;
        }

        if (this.attackCooldown == 0 && distSqr <= (MAX_ATTACK_RANGE * MAX_ATTACK_RANGE) && this.hasLineOfSight(target)) {
            this.attackCooldown = ATTACK_COOLDOWN_TICKS;
            this.attackWindup = 15; // ~0.75s de aviso antes de lançar

            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 0.7F, 1.3F);
        }
    }

    private void performRangedAttack(LivingEntity target, ServerLevel serverLevel) {
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.5D) - (this.getY() + 0.5D);
        double dz = target.getZ() - this.getZ();

        EspiritoNevoaProjectile projectile = new EspiritoNevoaProjectile(this.level(), this);
        projectile.setPos(this.getX(), this.getY() + 0.5D, this.getZ());
        projectile.shoot(dx, dy, dz, 0.95F, 1.2F);
        serverLevel.addFreshEntity(projectile);

        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.SOUL_ESCAPE, SoundSource.HOSTILE, 1.0F, 1.2F);
    }
}
