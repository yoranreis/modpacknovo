package br.com.caosarcana.mixin;

import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.RequirementResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ArmorItem.class)
public abstract class ArmorItemDispenserMixin {
    /**
     * Intercepta a tentativa de ejeção de armadura pelo dispenser ANTES de qualquer split(1) no ItemStack.
     * Se o alvo for um jogador que não cumpre os requisitos raciais/nível:
     * - Retorna false imediatamente.
     * - O dispenser executa DefaultDispenseItemBehavior (ejeção padrão vanilla como item dropado).
     * - Preserva a contagem exata da pilha no dispenser sem perda e sem duplicação.
     */
    @Inject(method = "dispenseArmor", at = @At("HEAD"), cancellable = true)
    private static void caosarcana$preventDispenserEquipToRestrictedPlayer(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }

        BlockPos targetPos = blockSource.getPos().relative(blockSource.getBlockState().getValue(DispenserBlock.FACING));
        List<LivingEntity> list = blockSource.getLevel().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(targetPos),
                EntitySelector.NO_SPECTATORS.and(new EntitySelector.MobCanWearArmorEntitySelector(itemStack))
        );

        if (!list.isEmpty()) {
            LivingEntity target = list.get(0);
            if (target instanceof Player player) {
                RequirementResult result = EquipmentRequirementRegistry.canEquip(player, itemStack);
                if (!result.isAllowed()) {
                    // Rejeita a transferência antes de split(1): o dispenser cospe o item com segurança
                    cir.setReturnValue(false);
                }
            }
        }
    }
}
