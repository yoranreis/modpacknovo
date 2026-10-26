package br.com.caosarcana.common.equipment;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EquipmentRequirementRegistry {
    private static final Map<String, EquipmentRequirement> REQUIREMENTS = new ConcurrentHashMap<>();

    private static String getKey(ResourceLocation itemId, RequirementType type) {
        return itemId.toString() + "@" + type.name();
    }

    public static void register(EquipmentRequirement requirement) {
        if (requirement != null) {
            String key = getKey(requirement.getItemId(), requirement.getType());
            REQUIREMENTS.put(key, requirement);
        }
    }

    public static void unregister(ResourceLocation itemId, RequirementType type) {
        REQUIREMENTS.remove(getKey(itemId, type));
    }

    public static void clear() {
        REQUIREMENTS.clear();
    }

    public static Optional<EquipmentRequirement> get(ResourceLocation itemId, RequirementType type) {
        return Optional.ofNullable(REQUIREMENTS.get(getKey(itemId, type)));
    }

    public static RequirementResult check(Player player, ItemStack stack, RequirementType type) {
        if (stack == null || stack.isEmpty()) {
            return RequirementResult.allow();
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        EquipmentRequirement requirement = REQUIREMENTS.get(getKey(itemId, type));
        if (requirement == null) {
            return RequirementResult.allow();
        }

        return requirement.check(player, stack);
    }

    public static RequirementResult check(IRPGPlayerData rpgPlayer, ResourceLocation itemId, RequirementType type) {
        EquipmentRequirement requirement = REQUIREMENTS.get(getKey(itemId, type));
        if (requirement == null) {
            return RequirementResult.allow();
        }
        return requirement.check(rpgPlayer, ItemStack.EMPTY);
    }

    public static RequirementResult canEquip(Player player, ItemStack stack) {
        return check(player, stack, RequirementType.EQUIP);
    }

    public static RequirementResult canCraft(Player player, ItemStack stack) {
        return check(player, stack, RequirementType.CRAFT);
    }

    public static RequirementResult canTrade(Player player, ItemStack stack) {
        return check(player, stack, RequirementType.TRADE);
    }

    /**
     * Registra itens de teste PROVISÓRIOS para validar a infraestrutura de requisitos.
     * Estes itens são apenas para homologação e podem ser alterados ou removidos via clear().
     */
    public static void registerProvisionalTestItems() {
        // 1. Peitoral de Ferro: restrito para Orcs e Nível Geral 5 (Simula peitoral pesado de combate)
        register(new EquipmentRequirement(
                new ResourceLocation("minecraft", "iron_chestplate"),
                RequirementType.EQUIP,
                Set.of(Race.ORC),
                5,
                null,
                0,
                "§c[Restrição de Equipamento] Peitoral Pesado de Teste: Requer linhagem §eOrc§c e Nível Geral §e5§c."
        ));

        // 2. Botas Douradas: restrito para Felinos e Nível Geral 3 (Simula calçado leve/ágil)
        register(new EquipmentRequirement(
                new ResourceLocation("minecraft", "golden_boots"),
                RequirementType.EQUIP,
                Set.of(Race.FELINO),
                3,
                null,
                0,
                "§c[Restrição de Equipamento] Botas Ágeis de Teste: Requer linhagem §eFelino§c e Nível Geral §e3§c."
        ));

        // 3. Elmo de Diamante: restrito para Alto Elfo e Nível Geral 10 (Simula diadema místico)
        register(new EquipmentRequirement(
                new ResourceLocation("minecraft", "diamond_helmet"),
                RequirementType.EQUIP,
                Set.of(Race.ALTO_ELFO),
                10,
                null,
                0,
                "§c[Restrição de Equipamento] Diadema Arcano de Teste: Requer linhagem §eAlto Elfo§c e Nível Geral §e10§c."
        ));

        CaosArcana.LOGGER.info("[Caos Arcana] Requisitos centrais de equipamentos carregados (3 itens provisórios de teste registrados).");
    }
}
