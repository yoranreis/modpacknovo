package br.com.caosarcana.content.loot;

import br.com.caosarcana.content.ModItems;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.Set;

/**
 * Injeta grimórios de magias e moedas físicas nos baús de dungeons e estruturas do mundo.
 */
public class DungeonLootHandler {
    private static final Set<ResourceLocation> ADVANCED_DUNGEON_CHESTS = Set.of(
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.DESERT_PYRAMID,
            BuiltInLootTables.JUNGLE_TEMPLE,
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.NETHER_BRIDGE,
            BuiltInLootTables.ANCIENT_CITY,
            BuiltInLootTables.WOODLAND_MANSION
    );

    public static void register() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (!source.isBuiltin()) {
                return;
            }

            // 1. Piscina de Moedas comum para todas as dungeons registradas
            if (id.equals(BuiltInLootTables.SIMPLE_DUNGEON) || ADVANCED_DUNGEON_CHESTS.contains(id)) {
                LootPool.Builder coinPool = LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .add(LootItem.lootTableItem(ModItems.MOEDA_COBRE).setWeight(50)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 24))))
                        .add(LootItem.lootTableItem(ModItems.MOEDA_PRATA).setWeight(20)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 8))))
                        .add(LootItem.lootTableItem(ModItems.MOEDA_OURO).setWeight(5)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));

                tableBuilder.pool(coinPool.build());
            }

            // 2. CONFIGURAÇÃO PROVISÓRIA DO PROTÓTIPO: Primeira Jornada Jogável
            // Na Dungeon Simples Vanilla (minecraft:chests/simple_dungeon), garante exatamente 1 Grimório da Centelha Ígnea por baú.
            // Os grimórios avançados são excluídos desta tabela inicial para manter a dificuldade e foco da progressão de novato.
            if (id.equals(BuiltInLootTables.SIMPLE_DUNGEON)) {
                LootPool.Builder guaranteedSparkPool = LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(LootItem.lootTableItem(ModItems.CENTELHA_IGNEA));

                tableBuilder.pool(guaranteedSparkPool.build());
            }

            // 3. Distribuição de Grimórios nas demais estruturas (mantém Centelha e grimórios avançados aleatórios)
            if (ADVANCED_DUNGEON_CHESTS.contains(id)) {
                LootPool.Builder grimoirePool = LootPool.lootPool()
                        .setRolls(UniformGenerator.between(0, 1))
                        .add(LootItem.lootTableItem(ModItems.CENTELHA_IGNEA).setWeight(10))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_GEADA).setWeight(10))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_RELAMPAGO).setWeight(8))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_CURA).setWeight(10))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_CIRCULO_SAGRADO).setWeight(6))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_CARVALHO).setWeight(10))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_LUZ).setWeight(12))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_ESPADA).setWeight(8))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_FAMILIAR).setWeight(5))
                        .add(LootItem.lootTableItem(ModItems.GRIMORIO_SOMBRAS).setWeight(6));

                tableBuilder.pool(grimoirePool.build());
            }
        });
    }
}
