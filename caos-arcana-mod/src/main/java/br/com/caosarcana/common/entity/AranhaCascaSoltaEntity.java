package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/**
 * Predadora ágil da Floresta Negra.
 * Camuflada com placas de casca solta sobre o corpo, dispara teias pegajosas de seiva
 * que desaceleram a presa e realiza botes rasantes de emboscada sem sofrer dano de queda.
 */
public final class AranhaCascaSoltaEntity extends Spider {
    private int webCooldown = 60;
    private int webWindup = 0;

    private int leapCooldown = 100;
    private int leapWindup = 0;
    private int inLeapTicks = 0;

    public AranhaCascaSoltaEntity(EntityType<? extends AranhaCascaSoltaEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            protected int getAttackInterval() {
                return 22;
            }
        });
        goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7D));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        // Imune a qualquer dano de queda ao saltar de árvores e desníveis
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

        if (level().isClientSide) {
            if (getRandom().nextInt(10) == 0) {
                level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                        getX() + (getRandom().nextDouble() - 0.5D) * 0.9D,
                        getY() + 0.2D + getRandom().nextDouble() * 0.4D,
                        getZ() + (getRandom().nextDouble() - 0.5D) * 0.9D,
                        0.0D, 0.005D, 0.0D);
            }
            return;
        }

        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (webCooldown > 0) webCooldown--;
        if (leapCooldown > 0) leapCooldown--;

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            webWindup = 0;
            leapWindup = 0;
            inLeapTicks = 0;
            return;
        }

        double distanceSqr = distanceToSqr(target);

        // 1. Manuseio do salto ativo
        if (inLeapTicks > 0) {
            inLeapTicks--;
            if (distanceSqr < 4.5D) {
                target.hurt(damageSources().mobAttack(this), 3.0F);
                target.knockback(0.4D, getX() - target.getX(), getZ() - target.getZ());
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_BREAK,
                        SoundSource.HOSTILE, 0.9F, 0.85F);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                        getX(), getY() + 0.3D, getZ(), 8, 0.25D, 0.25D, 0.25D, 0.05D
                );
                inLeapTicks = 0;
            }
        }

        // 2. Telegrafia e execução do Disparo de Teia
        if (webWindup > 0) {
            webWindup--;
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    getX(), getY() + 0.35D, getZ(), 3, 0.15D, 0.1D, 0.15D, 0.02D);

            if (webWindup == 0) {
                performWebSpit(target, serverLevel);
            }
            return;
        }

        // 3. Telegrafia e execução do Salto de Emboscada (Bote)
        if (leapWindup > 0) {
            leapWindup--;
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);

            if (leapWindup == 0) {
                performLeap(target, serverLevel);
            }
            return;
        }

        // 4. Início de Habilidades baseadas na distância e linha de visão
        if (hasLineOfSight(target)) {
            // Disparo de Teia: média distância (5 a 16 blocos)
            if (webCooldown == 0 && distanceSqr >= 25.0D && distanceSqr <= 256.0D) {
                webCooldown = 130;
                webWindup = 14; // ~0.7s de telegrafia
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT,
                        SoundSource.HOSTILE, 0.85F, 1.4F);
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SPIDER_AMBIENT,
                        SoundSource.HOSTILE, 0.75F, 0.8F);
                return;
            }

            // Bote de Emboscada: 3 a no máximo 12 blocos (distância máxima de 12 blocos)
            // Cooldown de pelo menos 10 segundos (200 ticks)
            if (leapCooldown == 0 && distanceSqr >= 9.0D && distanceSqr <= 144.0D) {
                leapCooldown = 200; // 10 segundos
                leapWindup = 10;    // ~0.5s de telegrafia
                serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SPIDER_HURT,
                        SoundSource.HOSTILE, 0.75F, 1.35F);
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                        getX(), getY() + 0.1D, getZ(), 6, 0.2D, 0.1D, 0.2D, 0.03D
                );
            }
        }
    }

    private void performWebSpit(LivingEntity target, ServerLevel serverLevel) {
        double dx = target.getX() - getX();
        double dy = target.getY(0.5D) - (getY() + 0.35D);
        double dz = target.getZ() - getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        TeiaDeCascaProjectile web = new TeiaDeCascaProjectile(level(), this);
        web.setPos(getX(), getY() + 0.35D, getZ());
        web.shoot(dx, dy + horizontalDist * 0.12D, dz, 1.25F, 2.0F);
        serverLevel.addFreshEntity(web);

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.CROSSBOW_SHOOT,
                SoundSource.HOSTILE, 0.8F, 1.5F);
        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_JUMP,
                SoundSource.HOSTILE, 0.7F, 1.2F);
    }

    private void performLeap(LivingEntity target, ServerLevel serverLevel) {
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dist = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double dy = target.getY() - getY();

        // Trajetória rasante e controlada: alcance máximo horizontal de 12 blocos
        double clampedDist = Math.min(12.0D, dist);
        double speed = Math.min(0.72D, 0.35D + clampedDist * 0.032D);
        double vx = (dx / dist) * speed;
        double vz = (dz / dist) * speed;

        // Altura vertical moderada (evita pular excessivamente alto)
        double vy;
        if (dy > 0.5D) {
            vy = Math.min(0.28D, dy * 0.16D + 0.18D);
        } else if (dy < -0.5D) {
            vy = Math.max(-0.15D, dy * 0.10D + 0.10D);
        } else {
            vy = 0.20D; // Arco baixo e rente ao solo
        }

        setDeltaMovement(vx, vy, vz);
        hasImpulse = true;
        inLeapTicks = 14;

        serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.HOSTILE, 0.85F, 0.75F);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget && level() instanceof ServerLevel serverLevel) {
            // Mordida aplica lentidão curta (30 ticks = 1.5s) pelo veneno viscoso de seiva
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 30, 0), this);
            serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                    getX(), getY() + 0.3D, getZ(), 6, 0.2D, 0.2D, 0.2D, 0.04D
            );
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT,
                    SoundSource.HOSTILE, 0.8F, 1.1F);
        }
        return hit;
    }
}
