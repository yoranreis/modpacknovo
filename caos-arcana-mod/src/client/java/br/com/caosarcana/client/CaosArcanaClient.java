package br.com.caosarcana.client;

import br.com.caosarcana.client.gui.ManaHudOverlay;
import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class CaosArcanaClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("caosarcana-client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Caos Arcana Client] Inicializando cliente, HUD de Mana e sincronização de dados...");

        registerClientPackets();

        // Registra o HUD Dinâmico de Mana (Barra azul discreta sobre a saúde com fade-out inteligente)
        HudRenderCallback.EVENT.register(ManaHudOverlay::render);
    }

    private void registerClientPackets() {
        // Recebe ordem para abrir tela de seleção de raça
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.OPEN_RACE_SELECT_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> {
                client.setScreen(new RaceSelectionScreen());
            });
        });

        // Recebe sincronização dos dados de RPG do jogador
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
