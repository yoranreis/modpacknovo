package br.com.caosarcana.common.entity;

import br.com.caosarcana.CaosArcana;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.core.Registry;

public final class CaosArcanaEntities {
    public static final EntityType<RastejanteDeCinzaEntity> RASTEJANTE_DE_CINZA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("rastejante_de_cinza"),
                    EntityType.Builder.of(RastejanteDeCinzaEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.55F)
                            .clientTrackingRange(8)
                            .build("rastejante_de_cinza")
            );

    public static final EntityType<CaoFumeganteEntity> CAO_FUMEGANTE =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("cao_fumegante"),
                    EntityType.Builder.of(CaoFumeganteEntity::new, MobCategory.MONSTER)
                            .sized(0.95F, 1.5F)
                            .clientTrackingRange(8)
                            .build("cao_fumegante")
            );

    public static final EntityType<MineiroOcoEntity> MINEIRO_OCO =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("mineiro_oco"),
                    EntityType.Builder.of(MineiroOcoEntity::new, MobCategory.MONSTER)
                            .sized(0.75F, 1.55F)
                            .clientTrackingRange(8)
                            .build("mineiro_oco")
            );

    public static final EntityType<VigiaDasBrasasEntity> VIGIA_DAS_BRASAS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("vigia_das_brasas"),
                    EntityType.Builder.of(VigiaDasBrasasEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.9F)
                            .clientTrackingRange(8)
                            .build("vigia_das_brasas")
            );

    public static final EntityType<CarapacaBasalticaEntity> CARAPACA_BASALTICA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("carapaca_basaltica"),
                    EntityType.Builder.of(CarapacaBasalticaEntity::new, MobCategory.MONSTER)
                            .sized(1.35F, 1.4F)
                            .clientTrackingRange(8)
                            .build("carapaca_basaltica")
            );

    public static final EntityType<ColossoBasalticoEntity> COLOSSO_BASALTICO =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("colosso_basaltico"),
                    EntityType.Builder.of(ColossoBasalticoEntity::new, MobCategory.MONSTER)
                            .sized(3.8F, 3.2F)
                            .clientTrackingRange(12)
                            .build("colosso_basaltico")
            );

    private CaosArcanaEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                RASTEJANTE_DE_CINZA,
                RastejanteDeCinzaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                CAO_FUMEGANTE,
                CaoFumeganteEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                MINEIRO_OCO,
                MineiroOcoEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                VIGIA_DAS_BRASAS,
                VigiaDasBrasasEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                CARAPACA_BASALTICA,
                CarapacaBasalticaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                COLOSSO_BASALTICO,
                ColossoBasalticoEntity.createAttributes()
        );
        CaosArcana.LOGGER.info("[Caos Arcana] Inimigos das Terras da Cinza registrados.");
    }
}

