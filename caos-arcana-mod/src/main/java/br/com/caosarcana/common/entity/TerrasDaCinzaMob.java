package br.com.caosarcana.common.entity;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/** Shared traits for creatures native to the Ashlands. */
public abstract class TerrasDaCinzaMob extends PathfinderMob {
    private static final float FIRE_DAMAGE_MULTIPLIER = 0.5F;

    protected TerrasDaCinzaMob(EntityType<? extends TerrasDaCinzaMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float adjustedAmount = source.is(DamageTypeTags.IS_FIRE)
                ? amount * FIRE_DAMAGE_MULTIPLIER
                : amount;
        return super.hurt(source, adjustedAmount);
    }
}
