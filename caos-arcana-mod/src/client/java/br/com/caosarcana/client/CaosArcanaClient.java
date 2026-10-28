package br.com.caosarcana.client;

import br.com.caosarcana.client.gui.ManaHudOverlay;
import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.client.model.CaoFumeganteModel;
import br.com.caosarcana.client.model.GuardiaoMadeiraNegraModel;
import br.com.caosarcana.client.model.CarapacaBasalticaModel;
import br.com.caosarcana.client.model.MineiroOcoModel;
import br.com.caosarcana.client.model.VigiaDasBrasasModel;
import br.com.caosarcana.client.model.RastejanteDeCinzaModel;
import br.com.caosarcana.client.renderer.CaoFumeganteRenderer;
import br.com.caosarcana.client.renderer.GuardiaoMadeiraNegraRenderer;
import br.com.caosarcana.client.renderer.CarapacaBasalticaRenderer;
import br.com.caosarcana.client.renderer.ColossoBasalticoRenderer;
import br.com.caosarcana.client.renderer.MineiroOcoRenderer;
import br.com.caosarcana.client.renderer.VigiaDasBrasasRenderer;
import br.com.caosarcana.client.renderer.RastejanteDeCinzaRenderer;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.entity.CaosArcanaEntities;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class CaosArcanaClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("caosarcana-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Caos Arcana Client] Inicializando cliente, HUD de Mana, inimigos e sincronização de dados...");
        registerEntityRendering();
        registerClientPackets();
        HudRenderCallback.EVENT.register(ManaHudOverlay::render);
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
        EntityModelLayerRegistry.registerModelLayer(
                CaoFumeganteModel.LAYER_LOCATION,
                CaoFumeganteModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.CAO_FUMEGANTE,
                CaoFumeganteRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                MineiroOcoModel.LAYER_LOCATION,
                MineiroOcoModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.MINEIRO_OCO,
                MineiroOcoRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                VigiaDasBrasasModel.LAYER_LOCATION,
                VigiaDasBrasasModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.VIGIA_DAS_BRASAS,
                VigiaDasBrasasRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                CarapacaBasalticaModel.LAYER_LOCATION,
                CarapacaBasalticaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.CARAPACA_BASALTICA,
                CarapacaBasalticaRenderer::new
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.COLOSSO_BASALTICO,
                ColossoBasalticoRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                GuardiaoMadeiraNegraModel.LAYER_LOCATION,
                GuardiaoMadeiraNegraModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.GUARDIAO_MADEIRA_NEGRA,
                GuardiaoMadeiraNegraRenderer::new
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

            client.execute(() -> {
                ClientRPGData.update(raceId, chosen, mana, maxMana, level);
                if (client.player instanceof IRPGPlayerData rpgPlayer) {
                    rpgPlayer.caosarcana$setRace(Race.fromId(raceId));
                    rpgPlayer.caosarcana$setChosenRace(chosen);
                    rpgPlayer.caosarcana$setMana(mana);
                    rpgPlayer.caosarcana$setBaseMaxMana(maxMana);
                    rpgPlayer.caosarcana$setGeneralLevel(level);
                }
                ManaHudOverlay.wakeUp();
            });
        });
    }
}
