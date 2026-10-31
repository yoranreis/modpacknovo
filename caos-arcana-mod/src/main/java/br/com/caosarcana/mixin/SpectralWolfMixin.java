package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Wolf.class)
public abstract class SpectralWolfMixin {

    @Unique
    private int caosarcana$spectralLifetime = 1200;

    /**
     * Ciclo de vida estrito do Familiar Espectral:
     * - Duração máxima de 1200 ticks de simulação (60s).
     * - Dissipação imediata ao expirar, morrer ou desconectar o conjurador.
     * - Auto-anulação ao acordar em chunk caso o conjurador esteja offline ou tenha invocado outro familiar.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void caosarcana$handleSpectralLifecycle(CallbackInfo ci) {
        Wolf wolf = (Wolf) (Object) this;
        if (!wolf.getTags().contains("caosarcana:spectral_familiar")) {
            return;
        }

        if (!wolf.level().isClientSide()) {
            this.caosarcana$spectralLifetime--;
            if (this.caosarcana$spectralLifetime <= 0) {
                if (wolf.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.POOF, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                }
                wolf.discard();
                return;
            }

            LivingEntity owner = wolf.getOwner();
            if (owner == null || !owner.isAlive()) {
                if (wolf.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.POOF, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                }
                wolf.discard();
                return;
            }

            if (owner instanceof IRPGPlayerData rpgPlayer) {
                UUID activeId = rpgPlayer.caosarcana$getActiveFamiliarId();
                if (activeId == null || !activeId.equals(wolf.getUUID())) {
                    if (wolf.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.POOF, wolf.getX(), wolf.getY() + 0.5, wolf.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
                    }
                    wolf.discard();
                }
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$saveSpectralLifetime(CompoundTag compound, CallbackInfo ci) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.getTags().contains("caosarcana:spectral_familiar")) {
            compound.putInt("CaosArcanaSpectralLifetime", this.caosarcana$spectralLifetime);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$readSpectralLifetime(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("CaosArcanaSpectralLifetime")) {
            this.caosarcana$spectralLifetime = compound.getInt("CaosArcanaSpectralLifetime");
        }
    }

    /**
     * Impede qualquer reprodução/cruzamento com o Familiar Espectral.
     */
    @Inject(method = "canMate", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventSpectralMating(Animal other, CallbackInfoReturnable<Boolean> cir) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.getTags().contains("caosarcana:spectral_familiar")) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Impede alimentação para reprodução de familiares espectrais.
     */
    @Inject(method = "isFood", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventSpectralFood(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Wolf wolf = (Wolf) (Object) this;
        if (wolf.getTags().contains("caosarcana:spectral_familiar")) {
            cir.setReturnValue(false);
        }
    }
}
