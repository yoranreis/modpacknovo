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

    private CaosArcanaEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(
                RASTEJANTE_DE_CINZA,
                RastejanteDeCinzaEntity.createAttributes()
        );
        CaosArcana.LOGGER.info("[Caos Arcana] Entidade Rastejante de Cinza registrada.");
    }
}

