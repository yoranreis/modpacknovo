package br.com.caosarcana.mixin;

import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.RequirementResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.inventory.InventoryMenu$1")
public abstract class InventoryArmorSlotMixin {
    @Shadow @Final Player val$owner;
    @Shadow @Final EquipmentSlot val$slot;

    /**
     * Intercepta a implementação REAL de mayPlace nos 4 slots de armadura do inventário de sobrevivência (InventoryMenu$1).
     * Como InventoryMenu$1 sobrescreve Slot.mayPlace sem chamar super.mayPlace,
     * esta injeção na subclasse garante o bloqueio eficaz para:
     * - Arrastar com o mouse (Drag & Drop)
     * - Shift-Click (Quick Move)
     * - Troca rápida por teclas numéricas da hotbar (1-9)
     */
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void caosarcana$checkArmorSlotMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack != null && !stack.isEmpty()) {
            RequirementResult result = EquipmentRequirementRegistry.canEquip(this.val$owner, stack);
            if (!result.isAllowed()) {
                cir.setReturnValue(false);
            }
        }
    }
}
