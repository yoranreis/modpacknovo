package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Passiva de Neutralidade de Mobs Nativos do Nether para Sangue-Daedra / Elfo Negro:
 * Blazes, Ghasts, Magma Cubes, Wither Skeletons, Piglins e Zombified Piglins
 * tornam-se passivos por padrão, atacando apenas caso o jogador os agrida primeiro.
 */
@Mixin(Mob.class)
public abstract class MobRPGMixin {

    @Unique
    private static boolean caosarcana$isNetherNative(Mob mob) {
        return mob instanceof Blaze ||
               mob instanceof Ghast ||
               mob instanceof MagmaCube ||
               mob instanceof WitherSkeleton ||
               mob instanceof AbstractPiglin ||
               mob instanceof ZombifiedPiglin ||
               mob instanceof Hoglin;
    }

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void caosarcana$cancelPassiveNetherTargeting(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (target instanceof Player player && caosarcana$isNetherNative(self)) {
            if (player instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
                if (rpg.caosarcana$getRace() == Race.SANGUE_DAEDRA) {
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void caosarcana$clearProtectedNetherTarget(CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (caosarcana$isNetherNative(self)) {
            LivingEntity target = self.getTarget();
            if (target instanceof Player player && player instanceof IRPGPlayerData rpg && rpg.caosarcana$hasChosenRace()) {
                if (rpg.caosarcana$getRace() == Race.SANGUE_DAEDRA) {
                    self.setTarget(null);
                }
            }
        }
    }
}
