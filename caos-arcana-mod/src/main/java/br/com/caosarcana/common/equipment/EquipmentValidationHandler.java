package br.com.caosarcana.common.equipment;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.data.IRPGPlayerData;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

public class EquipmentValidationHandler {
    private static final EquipmentSlot[] CHECKED_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND
    };

    public static void register() {
        // 1. Intercepta tentativa de equipar com o botão direito na mão
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ArmorItem) {
                RequirementResult result = EquipmentRequirementRegistry.canEquip(player, stack);
                if (!result.isAllowed()) {
                    if (!world.isClientSide) {
                        player.sendSystemMessage(result.getDenialMessage());
                        if (player instanceof ServerPlayer sp) {
                            sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
                        }
                    }
                    return InteractionResultHolder.fail(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        });

        // 2. Validação contínua no servidor (cobre shift-click, arrastar em menu, ejetores/dispensers e troca de raça)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Executa a cada 5 ticks (4 vezes por segundo) para máxima responsividade com baixo custo de CPU
            if (server.getTickCount() % 5 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    validateAndEnforce(player);
                }
            }
        });
    }

    /**
     * Valida todos os slots de equipamento do jogador no servidor.
     * Se um item não atender aos requisitos raciais ou de nível:
     * - O item é desequipado imediatamente.
     * - É devolvido ao inventário do jogador.
     * - Se o inventário estiver 100% cheio, é dropado aos pés do jogador sem nunca ser destruído ou duplicado.
     * - Uma mensagem explicativa clara é enviada ao jogador.
     */
    public static void validateAndEnforce(ServerPlayer player) {
        if (player == null || !(player instanceof IRPGPlayerData)) {
            return;
        }

        for (EquipmentSlot slot : CHECKED_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            RequirementResult result = EquipmentRequirementRegistry.canEquip(player, stack);
            if (!result.isAllowed()) {
                // Remove o item do slot antes de devolver para evitar qualquer duplicação
                ItemStack copy = stack.copy();
                player.setItemSlot(slot, ItemStack.EMPTY);

                // Tenta adicionar ao inventário principal
                boolean returnedToInventory = player.getInventory().add(copy);

                if (!returnedToInventory) {
                    // Inventário cheio: dropa aos pés do jogador de forma segura
                    ItemEntity dropped = player.drop(copy, false, false);
                    if (dropped != null) {
                        dropped.setNoPickUpDelay();
                    }
                    player.sendSystemMessage(Component.literal("§6[Caos Arcana] Seu inventário estava lotado! O equipamento desequipado foi depositado com segurança aos seus pés."));
                }

                // Notifica o jogador do motivo do desequipamento
                player.sendSystemMessage(result.getDenialMessage());
                player.playNotifySound(SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.8f, 0.6f);
                player.inventoryMenu.broadcastChanges();

                CaosArcana.LOGGER.info("Item {} desequipado do jogador {} por violar requisitos: {}",
                        copy.getItem().getDescriptionId(), player.getName().getString(), result.getDenialMessage().getString());
            }
        }
    }
}
