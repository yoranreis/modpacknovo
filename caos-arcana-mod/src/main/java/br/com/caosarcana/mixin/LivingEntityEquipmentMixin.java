package br.com.caosarcana.mixin;

import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.RequirementResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityEquipmentMixin {
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventProgrammaticOrDispenserEquip(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        // Intercepta tentativas de ejetores/dispensers ou chamadas diretas de equipar no servidor
        if ((Object) this instanceof ServerPlayer player && slot.getType() == EquipmentSlot.Type.ARMOR && !stack.isEmpty()) {
            RequirementResult result = EquipmentRequirementRegistry.canEquip(player, stack);
            if (!result.isAllowed()) {
                ci.cancel();
            }
        }
    }
}
