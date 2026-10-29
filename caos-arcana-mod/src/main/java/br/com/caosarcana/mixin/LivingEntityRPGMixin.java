package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRPGMixin {

    /**
     * Passiva Anfíbia do Argoniano:
     * Reduz o consumo de ar para exatamente 1/3 do valor natural através da preservação
     * contínua da reserva em 2 a cada 3 ticks de simulação submersa.
     * Preserva integralmente a taxa de recuperação em superfície e compatibilidade
     * com encantamentos de Respiração e efeito de Respiração Aquática.
     */
    @Inject(method = "decreaseAirSupply", at = @At("HEAD"), cancellable = true)
    private void caosarcana$handleArgonianAirSupply(int air, CallbackInfoReturnable<Integer> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
            if (rpg.caosarcana$getRace() == Race.ARGONIANO) {
                if (entity.tickCount % 3 != 0) {
                    cir.setReturnValue(air);
                }
            }
        }
    }

    /**
     * Impede qualquer drop de itens de loot do Familiar Espectral.
     * Evita exploits de farm automatizado com entidades invocadas.
     */
    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventSpectralLoot(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getTags().contains("caosarcana:spectral_familiar")) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Impede qualquer drop de esferas de experiência (XP) do Familiar Espectral.
     */
    @Inject(method = "shouldDropExperience", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventSpectralExperience(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.getTags().contains("caosarcana:spectral_familiar")) {
            cir.setReturnValue(false);
        }
    }
}
