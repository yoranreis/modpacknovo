package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

public class EspiritoNevoaProjectile extends ThrowableItemProjectile {
    public EspiritoNevoaProjectile(EntityType<? extends EspiritoNevoaProjectile> type, Level level) {
        super(type, level);
    }

    public EspiritoNevoaProjectile(Level level, LivingEntity shooter) {
        super(CaosArcanaEntities.ESPIRITO_NEVOA_PROJECTILE, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.LIME_DYE;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
            target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), this.getOwner()); // 3s cegueira
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this.getOwner()); // 3s lentidão II

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                        target.getX(), target.getY() + 1.0D, target.getZ(),
                        12, 0.3D, 0.3D, 0.3D, 0.05D);
            }
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        result.getLocation().x, result.getLocation().y, result.getLocation().z,
                        8, 0.2D, 0.2D, 0.2D, 0.02D);
            }
            this.discard();
        }
    }
}
