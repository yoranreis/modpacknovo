package br.com.caosarcana.client;

import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.client.model.RastejanteDeCinzaModel;
import br.com.caosarcana.client.renderer.RastejanteDeCinzaRenderer;
import br.com.caosarcana.common.entity.CaosArcanaEntities;
import br.com.caosarcana.common.network.ModMessages;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class CaosArcanaClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("caosarcana-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Caos Arcana Client] Inicializando cliente, interface e sincronização de dados...");
        registerEntityRendering();
        registerClientPackets();
    }

    private void registerEntityRendering() {
        EntityModelLayerRegistry.registerModelLayer(
                RastejanteDeCinzaModel.LAYER_LOCATION,
                RastejanteDeCinzaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.RASTEJANTE_DE_CINZA,
                RastejanteDeCinzaRenderer::new
        );
    }

    private void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.OPEN_RACE_SELECT_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new RaceSelectionScreen()));
        });

        ClientPlayNetworking.registerGlobalReceiver(ModMessages.SYNC_RPG_DATA_S2C_ID, (client, handler, buf, responseSender) -> {
            String raceId = buf.readUtf();
            boolean chosen = buf.readBoolean();
            float mana = buf.readFloat();
            float maxMana = buf.readFloat();
            int level = buf.readInt();

            client.execute(() -> ClientRPGData.update(raceId, chosen, mana, maxMana, level));
        });
    }
}

