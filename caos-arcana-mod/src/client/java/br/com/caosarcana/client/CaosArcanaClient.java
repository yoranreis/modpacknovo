package br.com.caosarcana.client;

import br.com.caosarcana.client.gui.GrimoireStudyScreen;
import br.com.caosarcana.client.gui.ManaHudOverlay;
import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.client.gui.SpellSelectionScreen;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class CaosArcanaClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("caosarcana-client");

    public static KeyMapping castSpellKey;
    public static KeyMapping openSpellMenuKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[Caos Arcana Client] Inicializando cliente, HUD de Mana, atalhos de feitiços e sincronização...");

        // Registra a tecla de conjuração da magia ativa (Padrão: V)
        castSpellKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.caosarcana.cast_spell",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "category.caosarcana.spells"
        ));

        // Registra a tecla de abertura do Grimório de Seleção de Magias (Padrão: R)
        openSpellMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.caosarcana.open_spell_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.caosarcana.spells"
        ));

        // Listener de cliques de teclas no cliente
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (castSpellKey.consumeClick()) {
                if (client.player != null) {
                    String activeSpell = ClientRPGData.getSelectedSpell();
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeUtf(activeSpell != null ? activeSpell : "");
                    ClientPlayNetworking.send(ModMessages.CAST_SPELL_C2S_ID, buf);
                }
            }

            while (openSpellMenuKey.consumeClick()) {
                if (client.player != null && client.screen == null) {
                    client.setScreen(new SpellSelectionScreen());
                }
            }
        });

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

        // Recebe ordem para abrir tela de estudo de grimório
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.OPEN_GRIMOIRE_STUDY_S2C_ID, (client, handler, buf, responseSender) -> {
            String spellId = buf.readUtf();
            client.execute(() -> {
                client.setScreen(new GrimoireStudyScreen(spellId));
            });
        });

        // Recebe sincronização dos dados de RPG do jogador
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.SYNC_RPG_DATA_S2C_ID, (client, handler, buf, responseSender) -> {
            String raceId = buf.readUtf();
            boolean chosen = buf.readBoolean();
            float mana = buf.readFloat();
            float maxMana = buf.readFloat();
            int level = buf.readInt();

            int spellCount = buf.readInt();
            Set<String> spells = new HashSet<>();
            for (int i = 0; i < spellCount; i++) {
                spells.add(buf.readUtf());
            }

            String selectedSpell = buf.readUtf();

            client.execute(() -> {
                ClientRPGData.update(raceId, chosen, mana, maxMana, level, spells, selectedSpell);
                if (client.player instanceof IRPGPlayerData rpgPlayer) {
                    rpgPlayer.caosarcana$setRace(Race.fromId(raceId));
                    rpgPlayer.caosarcana$setChosenRace(chosen);
                    rpgPlayer.caosarcana$setMana(mana);
                    rpgPlayer.caosarcana$setBaseMaxMana(maxMana);
                    rpgPlayer.caosarcana$setGeneralLevel(level);
                    for (String sp : spells) {
                        rpgPlayer.caosarcana$learnSpell(sp);
                    }
                    rpgPlayer.caosarcana$setSelectedSpell(selectedSpell);
                }
                ManaHudOverlay.wakeUp();
            });
        });
    }
}
