package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

/** Projétil viscoso disparado pela Aranha de Casca Solta que aplica lentidão e seiva na vítima. */
public class TeiaDeCascaProjectile extends ThrowableItemProjectile {
    public TeiaDeCascaProjectile(EntityType<? extends TeiaDeCascaProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public TeiaDeCascaProjectile(Level level, LivingEntity shooter) {
        super(CaosArcanaEntities.TEIA_DE_CASCA, shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.COBWEB;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        if (entity instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.0F);
            // Aplica Lentidão II por 4 segundos (80 ticks)
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), this.getOwner());
            playImpactEffects();
        }
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        playImpactEffects();
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    private void playImpactEffects() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY(), getZ(), 10, 0.25D, 0.25D, 0.25D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, getX(), getY(), getZ(), 8, 0.3D, 0.3D, 0.3D, 0.02D);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.SLIME_BLOCK_PLACE, SoundSource.HOSTILE, 0.9F, 0.7F);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT, SoundSource.HOSTILE, 0.7F, 1.3F);
        }
    }
}
