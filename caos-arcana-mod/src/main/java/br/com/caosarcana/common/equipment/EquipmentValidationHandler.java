package br.com.caosarcana.common.equipment;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.data.IRPGPlayerData;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentValidationHandler {
    private static final EquipmentSlot[] CHECKED_ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND
    };

    /**
     * Cache de controle de taxa (throttle) de feedback visual e sonoro por jogador (UUID -> timestamp ms).
     * Evita spam em múltiplos pacotes de ataque, múltiplos alvos em ataques de área (cleave) ou cliques rápidos.
     */
    private static final Map<UUID, Long> LAST_ATTACK_DENIAL_FEEDBACK = new ConcurrentHashMap<>();

    /**
     * Registra listeners de validação e entrega.
     * TODAS as operações aqui registradas são executadas estritamente na THREAD PRINCIPAL DO SERVIDOR (Server Thread),
     * garantindo thread-safety total sobre inventários, atributos e entidades.
     */
    public static void register() {
        // 1. Intercepta imediatamente o clique direito do mouse (UseItem) na mão
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

        // 2. Watchdog de segurança síncrono no fim do tick do servidor (executado a cada 20 ticks / 1 segundo)
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    validateAndEnforce(player);
                    processPendingDeliveries(player);
                }
            }
        });
    }

    /**
     * Valida de forma síncrona todos os slots de armadura do jogador.
     * Executado sempre na Thread do Servidor (Server Thread).
     */
    public static void validateAndEnforce(ServerPlayer player) {
        if (player == null || !(player instanceof IRPGPlayerData rpgPlayer)) {
            return;
        }

        for (EquipmentSlot slot : CHECKED_ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            // Para a mão secundária (offhand), armas manuais (espadas, machados, etc.) NUNCA devem ser
            // desequipadas compulsoriamente do slot. Preserva transporte, armazenamento e comércio.
            // Apenas armaduras reais ou escudos sofrem auto-desequipamento de offhand.
            // A restrição de armas é aplicada estritamente no momento do ataque (Player.attack).
            if (slot == EquipmentSlot.OFFHAND && !(stack.getItem() instanceof ArmorItem || stack.getItem() instanceof ShieldItem)) {
                continue;
            }

            RequirementResult result = EquipmentRequirementRegistry.canEquip(player, stack);
            if (!result.isAllowed()) {
                // 1. Clona a peça com todos os seus dados e NBT (isolamento total de referência)
                ItemStack itemToPreserve = stack.copy();

                // 2. Desocupa imediatamente o slot no jogador
                player.setItemSlot(slot, ItemStack.EMPTY);

                // 3. Tenta devolver ao inventário do jogador (Inventory.add muta itemToPreserve in-place)
                player.getInventory().add(itemToPreserve);

                if (!itemToPreserve.isEmpty()) {
                    // Apenas o restante não inserido vai para a custódia segura
                    // NUNCA dropa no chão, NUNCA deleta, NUNCA entrega a outro jogador
                    rpgPlayer.caosarcana$addPendingDelivery(itemToPreserve);
                    player.sendSystemMessage(Component.literal("§6[Caos Arcana] §cSeu inventário está lotado! O equipamento desequipado (§e" 
                            + itemToPreserve.getHoverName().getString() 
                            + " §7x" + itemToPreserve.getCount() 
                            + "§c) foi guardado em sua custódia segura. Libere espaço no inventário para recebê-lo."));
                } else {
                    player.sendSystemMessage(Component.literal("§6[Caos Arcana] §cEquipamento desequipado e devolvido ao inventário: §e" + stack.getHoverName().getString()));
                }

                // 4. Envia mensagem formal de restrição e som de feedback
                player.sendSystemMessage(result.getDenialMessage());
                player.playNotifySound(SoundEvents.ARMOR_EQUIP_GENERIC, SoundSource.PLAYERS, 0.8f, 0.6f);
                player.inventoryMenu.broadcastChanges();

                CaosArcana.LOGGER.info("Item {} desequipado do jogador {} por violar requisitos: {}",
                        itemToPreserve.getItem().getDescriptionId(), player.getName().getString(), result.getDenialMessage().getString());
            }
        }
    }

    /**
     * Entrega itens da custódia segura do jogador assim que houver espaço no inventário.
     * Suporta devolução parcial sem duplicação ou perda. Executado estritamente na Server Thread.
     */
    public static void processPendingDeliveries(ServerPlayer player) {
        if (player == null || !(player instanceof IRPGPlayerData rpgPlayer)) {
            return;
        }

        List<ItemStack> pending = rpgPlayer.caosarcana$getPendingDeliveries();
        if (pending.isEmpty()) {
            return;
        }

        Iterator<ItemStack> iterator = pending.iterator();
        int deliveredCount = 0;

        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            if (stack.isEmpty()) {
                iterator.remove();
                continue;
            }

            // Tenta adicionar ao inventário principal (Inventory.add consome unidades do stack diretamente)
            player.getInventory().add(stack);

            // Verifica o estado real do stack após a inserção (em vez de confiar apenas em booleano)
            if (stack.isEmpty()) {
                iterator.remove();
                deliveredCount++;
                player.sendSystemMessage(Component.literal("§a[Caos Arcana] Equipamento recuperado da custódia segura: §e" + stack.getHoverName().getString()));
            } else {
                // Se o stack ainda tiver unidades restantes, significa que o inventário lotou durante a inserção parcial.
                // O stack na lista agora armazena apenas a sobra não inserida. Interrompe a entrega.
                break;
            }
        }

        if (deliveredCount > 0) {
            player.playNotifySound(SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            player.inventoryMenu.broadcastChanges();
        }
    }

    /**
     * Resolve a arma real de ataque do jogador de forma estritamente segura no servidor.
     * Caso o Better Combat esteja instalado, utiliza introspecção (reflexão segura sem dependência dura de compilação)
     * para consultar o ataque corrente (getCurrentAttack() / AttackHand) resolvido pelo servidor, garantindo
     * diferenciação exata entre mão principal e mão secundária (dual wielding).
     * Caso contrário, ou em falha de reflexão, recorre à arma da mão principal do vanilla.
     */
    public static ItemStack resolveAttackingWeapon(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }

        if (FabricLoader.getInstance().isModLoaded("bettercombat")) {
            try {
                Method getCurrentAttackMethod = player.getClass().getMethod("getCurrentAttack");
                Object attackHand = getCurrentAttackMethod.invoke(player);
                if (attackHand != null) {
                    Method itemStackMethod = attackHand.getClass().getMethod("itemStack");
                    Object stackObj = itemStackMethod.invoke(attackHand);
                    if (stackObj instanceof ItemStack is && !is.isEmpty()) {
                        return is;
                    }
                }
            } catch (Throwable ignored) {
                // Fallback seguro caso a chamada de Better Combat não retorne objeto válido
            }
        }

        return player.getMainHandItem();
    }

    /**
     * Emite feedback de negação com controle de taxa (throttle de 1.5s).
     * Evita repetição excessiva em ataques de múltiplos alvos (cleave) ou múltiplos pacotes C2S.
     */
    public static void notifyAttackDenied(Player player, RequirementResult result) {
        if (player == null || result == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Long lastTime = LAST_ATTACK_DENIAL_FEEDBACK.get(player.getUUID());
        if (lastTime == null || now - lastTime > 1500L) {
            LAST_ATTACK_DENIAL_FEEDBACK.put(player.getUUID(), now);
            player.sendSystemMessage(result.getDenialMessage());
            if (player instanceof ServerPlayer sp) {
                sp.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
        }
    }
}
