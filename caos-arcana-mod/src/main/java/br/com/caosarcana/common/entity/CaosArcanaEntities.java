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
                            .sized(2.8F, 3.2F)
                            .clientTrackingRange(12)
                            .build("colosso_basaltico")
            );

    public static final EntityType<GuardiaoMadeiraNegraEntity> GUARDIAO_MADEIRA_NEGRA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("guardiao_madeira_negra"),
                    EntityType.Builder.of(GuardiaoMadeiraNegraEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 1.5F)
                            .clientTrackingRange(8)
                            .build("guardiao_madeira_negra")
            );

    public static final EntityType<AranhaCascaSoltaEntity> ARANHA_CASCA_SOLTA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("aranha_casca_solta"),
                    EntityType.Builder.of(AranhaCascaSoltaEntity::new, MobCategory.MONSTER)
                            .sized(1.2F, 0.75F)
                            .clientTrackingRange(8)
                            .build("aranha_casca_solta")
            );

    public static final EntityType<TeiaDeCascaProjectile> TEIA_DE_CASCA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("teia_de_casca"),
                    EntityType.Builder.<TeiaDeCascaProjectile>of(TeiaDeCascaProjectile::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("teia_de_casca")
            );

    public static final EntityType<EspiritoNevoaEntity> ESPIRITO_NEVOA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("espirito_nevoa"),
                    EntityType.Builder.of(EspiritoNevoaEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.2F)
                            .clientTrackingRange(8)
                            .build("espirito_nevoa")
            );

    public static final EntityType<EspiritoNevoaProjectile> ESPIRITO_NEVOA_PROJECTILE =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("espirito_nevoa_projectile"),
                    EntityType.Builder.<EspiritoNevoaProjectile>of(EspiritoNevoaProjectile::new, MobCategory.MISC)
                            .sized(0.3F, 0.3F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("espirito_nevoa_projectile")
            );

    public static final EntityType<CultistaOcultoEntity> CULTISTA_OCULTO =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("cultista_oculto"),
                    EntityType.Builder.of(CultistaOcultoEntity::new, MobCategory.MONSTER)
                            .sized(0.75F, 1.95F)
                            .clientTrackingRange(8)
                            .build("cultista_oculto")
            );

    public static final EntityType<MaldicaoOcultaProjectile> MALDICAO_OCULTA_PROJECTILE =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("maldicao_oculta_projectile"),
                    EntityType.Builder.<MaldicaoOcultaProjectile>of(MaldicaoOcultaProjectile::new, MobCategory.MISC)
                            .sized(0.35F, 0.35F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("maldicao_oculta_projectile")
            );

    public static final EntityType<AnciaoDasSombrasEntity> ANCIAO_DAS_SOMBRAS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("anciao_das_sombras"),
                    EntityType.Builder.of(AnciaoDasSombrasEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 3.1F)
                            .clientTrackingRange(12)
                            .build("anciao_das_sombras")
            );

    public static final EntityType<EscaravelhoSolarEntity> ESCARAVELHO_SOLAR =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("escaravelho_solar"),
                    EntityType.Builder.of(EscaravelhoSolarEntity::new, MobCategory.MONSTER)
                            .sized(1.15F, 0.9F)
                            .clientTrackingRange(8)
                            .build("escaravelho_solar")
            );

    public static final EntityType<ViboraDasArenasEntity> VIBORA_DAS_ARENAS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("vibora_das_arenas"),
                    EntityType.Builder.of(ViboraDasArenasEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 1.05F)
                            .clientTrackingRange(8)
                            .build("vibora_das_arenas")
            );

    public static final EntityType<GolemDeAreiaEntity> GOLEM_DE_AREIA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("golem_de_areia"),
                    EntityType.Builder.of(GolemDeAreiaEntity::new, MobCategory.MONSTER)
                            .sized(1.45F, 2.75F)
                            .clientTrackingRange(10)
                            .build("golem_de_areia")
            );

    public static final EntityType<MumiaEsqueleticaEntity> MUMIA_ESQUELETICA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("mumia_esqueletica"),
                    EntityType.Builder.of(MumiaEsqueleticaEntity::new, MobCategory.MONSTER)
                            .sized(0.65F, 1.95F)
                            .clientTrackingRange(8)
                            .build("mumia_esqueletica")
            );

    public static final EntityType<FaraoDoSolEternoEntity> FARAO_DO_SOL_ETERNO =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("farao_do_sol_eterno"),
                    EntityType.Builder.of(FaraoDoSolEternoEntity::new, MobCategory.MONSTER)
                            .sized(1.15F, 2.75F)
                            .clientTrackingRange(12)
                            .build("farao_do_sol_eterno")
            );

    public static final EntityType<TragadorDoAbismoEntity> TRAGADOR_DO_ABISMO =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("tragador_do_abismo"),
                    EntityType.Builder.of(TragadorDoAbismoEntity::new, MobCategory.MONSTER)
                            .sized(0.85F, 1.95F)
                            .clientTrackingRange(8)
                            .build("tragador_do_abismo")
            );

    public static final EntityType<GuardiaoDeObsidianaEntity> GUARDIAO_DE_OBSIDIANA =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("guardiao_de_obsidiana"),
                    EntityType.Builder.of(GuardiaoDeObsidianaEntity::new, MobCategory.MONSTER)
                            .sized(1.15F, 2.35F)
                            .clientTrackingRange(8)
                            .build("guardiao_de_obsidiana")
            );

    public static final EntityType<MedusaBioluminescenteEntity> MEDUSA_BIOLUMINESCENTE =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("medusa_bioluminescente"),
                    EntityType.Builder.of(MedusaBioluminescenteEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 1.45F)
                            .clientTrackingRange(8)
                            .build("medusa_bioluminescente")
            );

    public static final EntityType<SereiaDasProfundezasEntity> SEREIA_DAS_PROFUNDEZAS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("sereia_das_profundezas"),
                    EntityType.Builder.of(SereiaDasProfundezasEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 1.95F)
                            .clientTrackingRange(8)
                            .build("sereia_das_profundezas")
            );

    public static final EntityType<LordeDasMaresAbissaisEntity> LORDE_DAS_MARES_ABISSAIS =
            Registry.register(
                    BuiltInRegistries.ENTITY_TYPE,
                    CaosArcana.id("lorde_das_mares_abissais"),
                    EntityType.Builder.of(LordeDasMaresAbissaisEntity::new, MobCategory.MONSTER)
                            .sized(1.45F, 2.95F)
                            .clientTrackingRange(12)
                            .build("lorde_das_mares_abissais")
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
        FabricDefaultAttributeRegistry.register(
                GUARDIAO_MADEIRA_NEGRA,
                GuardiaoMadeiraNegraEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                ARANHA_CASCA_SOLTA,
                AranhaCascaSoltaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                ESPIRITO_NEVOA,
                EspiritoNevoaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                CULTISTA_OCULTO,
                CultistaOcultoEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                ANCIAO_DAS_SOMBRAS,
                AnciaoDasSombrasEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                ESCARAVELHO_SOLAR,
                EscaravelhoSolarEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                VIBORA_DAS_ARENAS,
                ViboraDasArenasEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                GOLEM_DE_AREIA,
                GolemDeAreiaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                MUMIA_ESQUELETICA,
                MumiaEsqueleticaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                FARAO_DO_SOL_ETERNO,
                FaraoDoSolEternoEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                TRAGADOR_DO_ABISMO,
                TragadorDoAbismoEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                GUARDIAO_DE_OBSIDIANA,
                GuardiaoDeObsidianaEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                MEDUSA_BIOLUMINESCENTE,
                MedusaBioluminescenteEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                SEREIA_DAS_PROFUNDEZAS,
                SereiaDasProfundezasEntity.createAttributes()
        );
        FabricDefaultAttributeRegistry.register(
                LORDE_DAS_MARES_ABISSAIS,
                LordeDasMaresAbissaisEntity.createAttributes()
        );
        CaosArcanaSpawnEggs.register();
        CaosArcana.LOGGER.info("[Caos Arcana] Inimigos e entidades registrados com sucesso.");
    }
}
