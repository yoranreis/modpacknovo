package br.com.caosarcana;

import br.com.caosarcana.common.command.CaosArcanaCommands;
import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.EquipmentValidationHandler;
import br.com.caosarcana.common.entity.CaosArcanaEntities;
import br.com.caosarcana.common.network.ModMessages;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaosArcana implements ModInitializer {
    public static final String MOD_ID = "caosarcana";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Caos Arcana] Inicializando sistemas RPG do Server do Caos...");

        CaosArcanaEntities.register();
        ModMessages.registerC2SPackets();
        ModMessages.registerServerEvents();
        CaosArcanaCommands.register();

        EquipmentRequirementRegistry.registerProvisionalTestItems();
        EquipmentValidationHandler.register();

        LOGGER.info("[Caos Arcana] Sistemas RPG e entidade inicial carregados.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
