package br.com.caosarcana.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * Múmia Esquelética — Mob Hostil / Combatente Ágil do Deserto das Dunas Douradas.
 * - Fúria do Deserto: Aumenta velocidade de movimento e ataque ao avistar o jogador diretamente.
 * - Maldição da Desidratação: Acelera o consumo da barra de fome do jogador durante o combate ativo.
 */
public final class MumiaEsqueleticaEntity extends PathfinderMob {
    private static final EntityDataAccessor<Boolean> DATA_DESERT_FURY =
            SynchedEntityData.defineId(MumiaEsqueleticaEntity.class, EntityDataSerializers.BOOLEAN);

    public MumiaEsqueleticaEntity(EntityType<? extends MumiaEsqueleticaEntity> entityType, Level level) {
        super(entityType, level);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.12F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 4.5D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.ARMOR, 3.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_DESERT_FURY, false);
    }

    public boolean isInDesertFury() {
        return this.entityData.get(DATA_DESERT_FURY);
    }

    private void setDesertFury(boolean fury) {
        this.entityData.set(DATA_DESERT_FURY, fury);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15D, false) {
            @Override
            protected int getAttackInterval() {
                // Ataque acelerado durante Fúria do Deserto
                return isInDesertFury() ? 13 : 20;
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.STONE_SWORD));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable net.minecraft.nbt.CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
        this.populateDefaultEquipmentSlots(level.getRandom(), difficulty);
        return data;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            if (isInDesertFury() && this.getRandom().nextInt(3) == 0) {
                this.level().addParticle(
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState()),
                        this.getX() + (this.getRandom().nextDouble() - 0.5D) * 0.6D,
                        this.getY() + 0.9D + (this.getRandom().nextDouble() - 0.5D) * 0.6D,
                        this.getZ() + (this.getRandom().nextDouble() - 0.5D) * 0.6D,
                        0.0D, 0.02D, 0.0D
                );
            }
            return;
        }

        LivingEntity target = this.getTarget();
        boolean canSeeTarget = target != null && target.isAlive() && this.hasLineOfSight(target);
        setDesertFury(canSeeTarget);

        if (canSeeTarget) {
            // Fúria do Deserto: velocidade aumentada ao avistar o jogador diretamente
            if (!this.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, false, false));
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingTarget) {
            // Maldição da Desidratação: Acelera o consumo da barra de fome do jogador
            livingTarget.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 2), this);
            if (livingTarget instanceof Player player) {
                player.causeFoodExhaustion(3.5F);
            }
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                        target.getX(), target.getY() + 0.8D, target.getZ(),
                        12, 0.3D, 0.35D, 0.3D, 0.06D
                );
            }
        }
        return hit;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(Items.BONE, 1 + this.random.nextInt(2 + looting));
        this.spawnAtLocation(Items.ROTTEN_FLESH, 1 + this.random.nextInt(2));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
