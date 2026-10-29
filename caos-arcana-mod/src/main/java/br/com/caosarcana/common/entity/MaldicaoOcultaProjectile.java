package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class MaldicaoOcultaProjectile extends ThrowableItemProjectile {

    public MaldicaoOcultaProjectile(EntityType<? extends MaldicaoOcultaProjectile> type, Level level) {
        super(type, level);
    }

    public MaldicaoOcultaProjectile(Level level, LivingEntity shooter) {
        super(CaosArcanaEntities.MALDICAO_OCULTA_PROJECTILE, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ENDER_EYE; // Orbe místico com aparência de olho/energia verde
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // Rastro contínuo de almas e faíscas verdes
            this.level().addParticle(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, 0.02D, 0.0D);
            if (this.random.nextBoolean()) {
                this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.25D,
                        this.getY() + (this.random.nextDouble() - 0.5D) * 0.25D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.25D,
                        0.0D, 0.01D, 0.0D);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 3.5F);
            // Aplica Maldição: Fraqueza I e Lentidão I por 4 segundos
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), this.getOwner());
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0), this.getOwner());

            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SCULK_SOUL,
                        target.getX(), target.getY() + 1.0D, target.getZ(),
                        12, 0.3D, 0.3D, 0.3D, 0.03D);
                sl.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.WITCH_THROW, SoundSource.HOSTILE, 0.8F, 0.8F);
            }
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SCULK_SOUL,
                        result.getLocation().x, result.getLocation().y + 0.1D, result.getLocation().z,
                        8, 0.25D, 0.15D, 0.25D, 0.02D);
            }
            this.discard();
        }
    }
}
