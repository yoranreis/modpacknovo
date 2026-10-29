package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        return Items.ENDER_EYE; // Orbe mágico de energia sombria/verde
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            // Rastro contínuo de magia e ectoplasma verde
            this.level().addParticle(ParticleTypes.SCULK_SOUL,
                    this.getX(), this.getY(), this.getZ(),
                    0.0D, 0.02D, 0.0D);
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                    this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    this.getY() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D,
                    0.0D, 0.01D, 0.0D);
        }
    }

    private void createMistCloud(double x, double y, double z) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            // Cria a poça/nuvem de névoa no chão similar ao bafo do dragão (AreaEffectCloud)
            AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
            cloud.setRadius(2.6F);
            cloud.setRadiusOnUse(-0.25F);
            cloud.setWaitTime(0);
            cloud.setDuration(90); // ~4.5 segundos antes de sumir
            cloud.setRadiusPerTick(-cloud.getRadius() / 90.0F);
            cloud.setParticle(ParticleTypes.DRAGON_BREATH); // Bafo do dragão
            cloud.setFixedColor(0x2CE574); // Verde espectral

            // Efeitos solicitados pelo usuário: Lentidão e Cegueira
            cloud.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 70, 0));
            cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 70, 1));

            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                cloud.setOwner(livingOwner);
            }

            this.level().addFreshEntity(cloud);

            // Partículas adicionais de impacto de névoa
            serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y + 0.15D, z, 14, 0.35D, 0.1D, 0.35D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.SCULK_SOUL,
                    x, y + 0.2D, z, 8, 0.3D, 0.15D, 0.3D, 0.02D);

            // Som de impacto de magia espectral estourando no chão
            serverLevel.playSound(null, x, y, z,
                    SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.HOSTILE, 0.8F, 1.4F);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            if (result.getEntity() instanceof LivingEntity target) {
                target.hurt(this.damageSources().thrown(this, this.getOwner()), 2.5F);
                createMistCloud(target.getX(), target.getY(), target.getZ());
            }
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
