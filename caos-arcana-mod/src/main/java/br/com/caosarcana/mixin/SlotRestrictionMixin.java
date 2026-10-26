package br.com.caosarcana.mixin;

import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.RequirementResult;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotRestrictionMixin {
    @Shadow @Final public Container container;
    @Shadow public abstract int getContainerSlot();

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void caosarcana$preventPlacingRestrictedArmor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this.container instanceof Inventory inventory) {
            int containerSlot = this.getContainerSlot();
            // Índices 36 a 39 no inventário vanilla representam os 4 slots de armadura (Pés, Pernas, Peitoral, Cabeça)
            if (containerSlot >= 36 && containerSlot <= 39 && !stack.isEmpty()) {
                RequirementResult result = EquipmentRequirementRegistry.canEquip(inventory.player, stack);
                if (!result.isAllowed()) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
