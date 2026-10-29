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

/**
 * Registro centralizado dos Ovos de Invocação (Spawn Eggs) de todos os mobs do Caos Arcana.
 */
public final class CaosArcanaSpawnEggs {
    // 1. Terras da Cinza
    public static final SpawnEggItem RASTEJANTE_DE_CINZA_SPAWN_EGG = registerEgg(
            "rastejante_de_cinza_spawn_egg", CaosArcanaEntities.RASTEJANTE_DE_CINZA, 0x3A3532, 0xFF6A1A);
    public static final SpawnEggItem CAO_FUMEGANTE_SPAWN_EGG = registerEgg(
            "cao_fumegante_spawn_egg", CaosArcanaEntities.CAO_FUMEGANTE, 0x26211F, 0xF2541B);
    public static final SpawnEggItem MINEIRO_OCO_SPAWN_EGG = registerEgg(
            "mineiro_oco_spawn_egg", CaosArcanaEntities.MINEIRO_OCO, 0x4A443F, 0xF5B841);
    public static final SpawnEggItem VIGIA_DAS_BRASAS_SPAWN_EGG = registerEgg(
            "vigia_das_brasas_spawn_egg", CaosArcanaEntities.VIGIA_DAS_BRASAS, 0x2E2422, 0xFF7B24);
    public static final SpawnEggItem CARAPACA_BASALTICA_SPAWN_EGG = registerEgg(
            "carapaca_basaltica_spawn_egg", CaosArcanaEntities.CARAPACA_BASALTICA, 0x3E3D42, 0xE85D22);
    public static final SpawnEggItem COLOSSO_BASALTICO_SPAWN_EGG = registerEgg(
            "colosso_basaltico_spawn_egg", CaosArcanaEntities.COLOSSO_BASALTICO, 0x1F1E22, 0xFF3B14);

    // 2. Floresta Negra
    public static final SpawnEggItem GUARDIAO_MADEIRA_NEGRA_SPAWN_EGG = registerEgg(
            "guardiao_madeira_negra_spawn_egg", CaosArcanaEntities.GUARDIAO_MADEIRA_NEGRA, 0x2E2319, 0x2EE678);
    public static final SpawnEggItem ARANHA_CASCA_SOLTA_SPAWN_EGG = registerEgg(
            "aranha_casca_solta_spawn_egg", CaosArcanaEntities.ARANHA_CASCA_SOLTA, 0x3B2B1E, 0x52B788);
    public static final SpawnEggItem ESPIRITO_NEVOA_SPAWN_EGG = registerEgg(
            "espirito_nevoa_spawn_egg", CaosArcanaEntities.ESPIRITO_NEVOA, 0x16382C, 0x64FFAA);
    public static final SpawnEggItem CULTISTA_OCULTO_SPAWN_EGG = registerEgg(
            "cultista_oculto_spawn_egg", CaosArcanaEntities.CULTISTA_OCULTO, 0x1E2D1B, 0x38FF82);
    public static final SpawnEggItem ANCIAO_DAS_SOMBRAS_SPAWN_EGG = registerEgg(
            "anciao_das_sombras_spawn_egg", CaosArcanaEntities.ANCIAO_DAS_SOMBRAS, 0x1A1511, 0x00FF7A);

    // 3. Deserto das Dunas Douradas
    public static final SpawnEggItem ESCARAVELHO_SOLAR_SPAWN_EGG = registerEgg(
            "escaravelho_solar_spawn_egg", CaosArcanaEntities.ESCARAVELHO_SOLAR, 0x6E522E, 0xFFE688);
    public static final SpawnEggItem VIBORA_DAS_ARENAS_SPAWN_EGG = registerEgg(
            "vibora_das_arenas_spawn_egg", CaosArcanaEntities.VIBORA_DAS_ARENAS, 0xD4B886, 0x4CD4C8);
    public static final SpawnEggItem GOLEM_DE_AREIA_SPAWN_EGG = registerEgg(
            "golem_de_areia_spawn_egg", CaosArcanaEntities.GOLEM_DE_AREIA, 0xDFC89A, 0x7A8254);
    public static final SpawnEggItem MUMIA_ESQUELETICA_SPAWN_EGG = registerEgg(
            "mumia_esqueletica_spawn_egg", CaosArcanaEntities.MUMIA_ESQUELETICA, 0xD2BC90, 0x204494);
    public static final SpawnEggItem FARAO_DO_SOL_ETERNO_SPAWN_EGG = registerEgg(
            "farao_do_sol_eterno_spawn_egg", CaosArcanaEntities.FARAO_DO_SOL_ETERNO, 0x1D3C8C, 0xF2C64E);

    // 4. Recife de Coral Abissal & Arquipélago
    public static final SpawnEggItem TRAGADOR_DO_ABISMO_SPAWN_EGG = registerEgg(
            "tragador_do_abismo_spawn_egg", CaosArcanaEntities.TRAGADOR_DO_ABISMO, 0x122A3A, 0x00F5FF);
    public static final SpawnEggItem GUARDIAO_DE_OBSIDIANA_SPAWN_EGG = registerEgg(
            "guardiao_de_obsidiana_spawn_egg", CaosArcanaEntities.GUARDIAO_DE_OBSIDIANA, 0x261C38, 0xFF6914);
    public static final SpawnEggItem MEDUSA_BIOLUMINESCENTE_SPAWN_EGG = registerEgg(
            "medusa_bioluminescente_spawn_egg", CaosArcanaEntities.MEDUSA_BIOLUMINESCENTE, 0x4BE1FF, 0xFF82EB);
    public static final SpawnEggItem SEREIA_DAS_PROFUNDEZAS_SPAWN_EGG = registerEgg(
            "sereia_das_profundezas_spawn_egg", CaosArcanaEntities.SEREIA_DAS_PROFUNDEZAS, 0x225880, 0x50FAFF);
    public static final SpawnEggItem LORDE_DAS_MARES_ABISSAIS_SPAWN_EGG = registerEgg(
            "lorde_das_mares_abissais_spawn_egg", CaosArcanaEntities.LORDE_DAS_MARES_ABISSAIS, 0x164460, 0xEEBE3E);

    public static final SpawnEggItem[] ALL_EGGS = new SpawnEggItem[]{
            RASTEJANTE_DE_CINZA_SPAWN_EGG,
            CAO_FUMEGANTE_SPAWN_EGG,
            MINEIRO_OCO_SPAWN_EGG,
            VIGIA_DAS_BRASAS_SPAWN_EGG,
            CARAPACA_BASALTICA_SPAWN_EGG,
            COLOSSO_BASALTICO_SPAWN_EGG,
            GUARDIAO_MADEIRA_NEGRA_SPAWN_EGG,
            ARANHA_CASCA_SOLTA_SPAWN_EGG,
            ESPIRITO_NEVOA_SPAWN_EGG,
            CULTISTA_OCULTO_SPAWN_EGG,
            ANCIAO_DAS_SOMBRAS_SPAWN_EGG,
            ESCARAVELHO_SOLAR_SPAWN_EGG,
            VIBORA_DAS_ARENAS_SPAWN_EGG,
            GOLEM_DE_AREIA_SPAWN_EGG,
            MUMIA_ESQUELETICA_SPAWN_EGG,
            FARAO_DO_SOL_ETERNO_SPAWN_EGG,
            TRAGADOR_DO_ABISMO_SPAWN_EGG,
            GUARDIAO_DE_OBSIDIANA_SPAWN_EGG,
            MEDUSA_BIOLUMINESCENTE_SPAWN_EGG,
            SEREIA_DAS_PROFUNDEZAS_SPAWN_EGG,
            LORDE_DAS_MARES_ABISSAIS_SPAWN_EGG
    };

    private CaosArcanaSpawnEggs() {
    }

    private static <T extends Mob> SpawnEggItem registerEgg(
            String id, EntityType<T> entityType, int primaryColor, int secondaryColor) {
        return Registry.register(
                BuiltInRegistries.ITEM,
                CaosArcana.id(id),
                new SpawnEggItem(entityType, primaryColor, secondaryColor, new Item.Properties())
        );
    }

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            for (SpawnEggItem egg : ALL_EGGS) {
                entries.accept(egg);
            }
        });
        CaosArcana.LOGGER.info("[Caos Arcana] {} ovos de invocação registrados com sucesso.", ALL_EGGS.length);
    }
}
