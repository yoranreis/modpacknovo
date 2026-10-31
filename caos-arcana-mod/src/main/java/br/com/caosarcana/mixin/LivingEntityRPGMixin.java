package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityRPGMixin {

    @Unique
    private static boolean caosarcana$isNetherNativeEntity(LivingEntity entity) {
        return entity instanceof Blaze ||
               entity instanceof Ghast ||
               entity instanceof MagmaCube ||
               entity instanceof WitherSkeleton ||
               entity instanceof AbstractPiglin ||
               entity instanceof ZombifiedPiglin ||
               entity instanceof Hoglin;
    }

    /**
     * Neutralidade Absoluta de Mobs Nativos do Nether para Sangue-Daedra:
     * Impede que monstros do Nether considerem o jogador um alvo atacável, mesmo se agredidos ou provocados.
     * Cobre tanto o overload de condição quanto o overload direto de entidade chamado pelo Sensor e TargetingConditions.
     */
    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void caosarcana$netherMobNeutralityDirect(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (caosarcana$isNetherNativeEntity(self) && target instanceof Player player) {
            if (player instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
                if (rpg.caosarcana$getRace() == Race.SANGUE_DAEDRA) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/ai/targeting/TargetingConditions;)Z", at = @At("HEAD"), cancellable = true)
    private void caosarcana$netherMobNeutralityConditions(LivingEntity target, TargetingConditions conditions, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (caosarcana$isNetherNativeEntity(self) && target instanceof Player player) {
            if (player instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
                if (rpg.caosarcana$getRace() == Race.SANGUE_DAEDRA) {
                    cir.setReturnValue(false);
                }
            }
        }
    }

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
