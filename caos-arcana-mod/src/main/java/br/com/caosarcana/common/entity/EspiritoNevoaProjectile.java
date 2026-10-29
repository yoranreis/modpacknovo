package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
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

    private void createMistCloud(double x, double y, double z) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // Cria área de efeito no chão estilo baforada do Ender Dragon
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
            cloud.setRadius(3.0F);
            cloud.setRadiusOnUse(-0.4F);
            cloud.setWaitTime(5);
            cloud.setDuration(80); // 4 segundos de duração no chão
            cloud.setRadiusPerTick(-cloud.getRadius() / (float)cloud.getDuration());
            cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0)); // 3s cegueira
            cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1)); // 3s lentidão II

            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                cloud.setOwner(livingOwner);
            }

            this.level().addFreshEntity(cloud);

            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                    x, y + 0.2D, z, 15, 0.4D, 0.2D, 0.4D, 0.03D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
            createMistCloud(target.getX(), target.getY(), target.getZ());
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            createMistCloud(result.getLocation().x, result.getLocation().y, result.getLocation().z);
            this.discard();
        }
    }
}
