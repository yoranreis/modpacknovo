package br.com.caosarcana.common.entity;

import br.com.caosarcana.CaosArcana;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

/** Spawn eggs for the Ashlands entities available in this isolated integration. */
public final class CaosArcanaSpawnEggs {
    public static final SpawnEggItem RASTEJANTE_DE_CINZA_SPAWN_EGG = registerEgg("rastejante_de_cinza_spawn_egg", CaosArcanaEntities.RASTEJANTE_DE_CINZA, 0x3A3532, 0xFF6A1A);
    public static final SpawnEggItem CAO_FUMEGANTE_SPAWN_EGG = registerEgg("cao_fumegante_spawn_egg", CaosArcanaEntities.CAO_FUMEGANTE, 0x26211F, 0xF2541B);
    public static final SpawnEggItem MINEIRO_OCO_SPAWN_EGG = registerEgg("mineiro_oco_spawn_egg", CaosArcanaEntities.MINEIRO_OCO, 0x4A443F, 0xF5B841);
    public static final SpawnEggItem VIGIA_DAS_BRASAS_SPAWN_EGG = registerEgg("vigia_das_brasas_spawn_egg", CaosArcanaEntities.VIGIA_DAS_BRASAS, 0x2E2422, 0xFF7B24);
    public static final SpawnEggItem CARAPACA_BASALTICA_SPAWN_EGG = registerEgg("carapaca_basaltica_spawn_egg", CaosArcanaEntities.CARAPACA_BASALTICA, 0x3E3D42, 0xE85D22);
    public static final SpawnEggItem COLOSSO_BASALTICO_SPAWN_EGG = registerEgg("colosso_basaltico_spawn_egg", CaosArcanaEntities.COLOSSO_BASALTICO, 0x1F1E22, 0xFF3B14);

    public static final SpawnEggItem[] ALL_EGGS = {
            RASTEJANTE_DE_CINZA_SPAWN_EGG, CAO_FUMEGANTE_SPAWN_EGG, MINEIRO_OCO_SPAWN_EGG,
            VIGIA_DAS_BRASAS_SPAWN_EGG, CARAPACA_BASALTICA_SPAWN_EGG, COLOSSO_BASALTICO_SPAWN_EGG
    };

    private CaosArcanaSpawnEggs() {}

    private static <T extends Mob> SpawnEggItem registerEgg(String id, EntityType<T> entityType, int primaryColor, int secondaryColor) {
        return Registry.register(BuiltInRegistries.ITEM, CaosArcana.id(id),
                new SpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties()));
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            for (SpawnEggItem egg : ALL_EGGS) entries.accept(egg);
        });
        CaosArcana.LOGGER.info("[Caos Arcana] {} ovos de invocação das Terras da Cinza registrados.", ALL_EGGS.length);
    }
}
