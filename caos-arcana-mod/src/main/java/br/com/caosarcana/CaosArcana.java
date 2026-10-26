package br.com.caosarcana;

import br.com.caosarcana.common.command.CaosArcanaCommands;
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

        ModMessages.registerC2SPackets();
        ModMessages.registerServerEvents();
        CaosArcanaCommands.register();

        LOGGER.info("[Caos Arcana] Modulos de Rede, Racas, Comandos e Persistencia carregados com sucesso.");
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
