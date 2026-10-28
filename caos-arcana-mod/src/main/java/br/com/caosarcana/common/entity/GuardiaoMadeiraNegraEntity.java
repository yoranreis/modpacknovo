package br.com.caosarcana.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** A twisted woodland construct that disappears against old trees while still. */
public final class GuardiaoMadeiraNegraEntity extends PathfinderMob {
    private int stillTicks;
    private int camouflageCheckTicks;
    private int revealCooldown;

    public GuardiaoMadeiraNegraEntity(EntityType<? extends GuardiaoMadeiraNegraEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.65D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            protected int getAttackInterval() {
                return 26;
            }
        });
        goalSelector.addGoal(7, new RandomStrollGoal(this, 0.65D));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 10.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            if (isInvisible() && getRandom().nextInt(8) == 0) {
                level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                        getX() + (getRandom().nextDouble() - 0.5D) * 0.8D,
                        getY() + 0.4D + getRandom().nextDouble() * 1.0D,
                        getZ() + (getRandom().nextDouble() - 0.5D) * 0.8D,
                        0.0D, 0.005D, 0.0D);
            }
            return;
        }

        if (revealCooldown > 0) revealCooldown--;
        boolean moving = getDeltaMovement().horizontalDistanceSqr() > 0.0015D;
        boolean playerClose = getTarget() != null && distanceToSqr(getTarget()) < 64.0D;
        if (moving || playerClose || revealCooldown > 0) {
            stillTicks = 0;
            setInvisible(false);
            return;
        }

        if (++camouflageCheckTicks >= 10) {
            camouflageCheckTicks = 0;
            if (hasOldTreeNearby()) {
                stillTicks += 10;
                if (stillTicks >= 40) setInvisible(true);
            } else {
                stillTicks = 0;
                setInvisible(false);
            }
        }
    }

    private boolean hasOldTreeNearby() {
        int logs = 0;
        int leaves = 0;
        BlockPos origin = blockPosition();
        for (int x = -3; x <= 3; x++) {
            for (int y = 0; y <= 4; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    if (level().getBlockState(pos).is(BlockTags.LOGS)) logs++;
                    if (level().getBlockState(pos).is(BlockTags.LEAVES)) leaves++;
                    if (logs >= 3 && leaves >= 8) return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide) {
            revealCooldown = 100;
            stillTicks = 0;
            setInvisible(false);
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        setInvisible(false);
        revealCooldown = 100;
        boolean hit = super.doHurtTarget(target);
        if (hit && level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(new BlockParticleOption(
                            ParticleTypes.BLOCK, Blocks.DARK_OAK_WOOD.defaultBlockState()),
                    getX(), getY() + 0.6D, getZ(), 9, 0.22D, 0.28D, 0.22D, 0.06D);
            serverLevel.playSound(null, getX(), getY(), getZ(), SoundEvents.WOOD_HIT,
                    SoundSource.HOSTILE, 0.75F, 0.65F + getRandom().nextFloat() * 0.15F);
        }
        return hit;
    }
}
