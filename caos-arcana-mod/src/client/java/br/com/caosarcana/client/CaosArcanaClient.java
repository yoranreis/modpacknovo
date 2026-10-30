package br.com.caosarcana.client;

import br.com.caosarcana.client.gui.GrimoireStudyScreen;
import br.com.caosarcana.client.gui.ManaHudOverlay;
import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.client.gui.SpellSelectionScreen;
import br.com.caosarcana.client.model.AnciaoDasSombrasModel;
import br.com.caosarcana.client.model.AranhaCascaSoltaModel;
import br.com.caosarcana.client.model.AranhaDoGeloModel;
import br.com.caosarcana.client.model.CacadorDeGeloModel;
import br.com.caosarcana.client.model.CaoFumeganteModel;
import br.com.caosarcana.client.model.CarapacaBasalticaModel;
import br.com.caosarcana.client.model.CultistaOcultoModel;
import br.com.caosarcana.client.model.EscaravelhoSolarModel;
import br.com.caosarcana.client.model.EspiritoNevoaModel;
import br.com.caosarcana.client.model.FantasmaDoGeloModel;
import br.com.caosarcana.client.model.FaraoDoSolEternoModel;
import br.com.caosarcana.client.model.GolemDeAreiaModel;
import br.com.caosarcana.client.model.GolemDeGeloModel;
import br.com.caosarcana.client.model.GuardiaoDeObsidianaModel;
import br.com.caosarcana.client.model.GuardiaoMadeiraNegraModel;
import br.com.caosarcana.client.model.LordeDasMaresAbissaisModel;
import br.com.caosarcana.client.model.MedusaBioluminescenteModel;
import br.com.caosarcana.client.model.MineiroOcoModel;
import br.com.caosarcana.client.model.MumiaEsqueleticaModel;
import br.com.caosarcana.client.model.RastejanteDeCinzaModel;
import br.com.caosarcana.client.model.ReiDoGeloModel;
import br.com.caosarcana.client.model.SereiaDasProfundezasModel;
import br.com.caosarcana.client.model.TragadorDoAbismoModel;
import br.com.caosarcana.client.model.ViboraDasArenasModel;
import br.com.caosarcana.client.model.VigiaDasBrasasModel;
import br.com.caosarcana.client.renderer.AnciaoDasSombrasRenderer;
import br.com.caosarcana.client.renderer.AranhaCascaSoltaRenderer;
import br.com.caosarcana.client.renderer.AranhaDoGeloRenderer;
import br.com.caosarcana.client.renderer.CacadorDeGeloRenderer;
import br.com.caosarcana.client.renderer.CaoFumeganteRenderer;
import br.com.caosarcana.client.renderer.CarapacaBasalticaRenderer;
import br.com.caosarcana.client.renderer.ColossoBasalticoRenderer;
import br.com.caosarcana.client.renderer.CultistaOcultoRenderer;
import br.com.caosarcana.client.renderer.EscaravelhoSolarRenderer;
import br.com.caosarcana.client.renderer.EspiritoNevoaRenderer;
import br.com.caosarcana.client.renderer.FantasmaDoGeloRenderer;
import br.com.caosarcana.client.renderer.FaraoDoSolEternoRenderer;
import br.com.caosarcana.client.renderer.GolemDeAreiaRenderer;
import br.com.caosarcana.client.renderer.GolemDeGeloRenderer;
import br.com.caosarcana.client.renderer.GuardiaoDeObsidianaRenderer;
import br.com.caosarcana.client.renderer.GuardiaoMadeiraNegraRenderer;
import br.com.caosarcana.client.renderer.LordeDasMaresAbissaisRenderer;
import br.com.caosarcana.client.renderer.MedusaBioluminescenteRenderer;
import br.com.caosarcana.client.renderer.MineiroOcoRenderer;
import br.com.caosarcana.client.renderer.MumiaEsqueleticaRenderer;
import br.com.caosarcana.client.renderer.RastejanteDeCinzaRenderer;
import br.com.caosarcana.client.renderer.ReiDoGeloRenderer;
import br.com.caosarcana.client.renderer.SereiaDasProfundezasRenderer;
import br.com.caosarcana.client.renderer.TragadorDoAbismoRenderer;
import br.com.caosarcana.client.renderer.ViboraDasArenasRenderer;
import br.com.caosarcana.client.renderer.VigiaDasBrasasRenderer;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.entity.CaosArcanaEntities;
import br.com.caosarcana.common.entity.CaosArcanaSpawnEggs;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.SpawnEggItem;
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
        LOGGER.info("[Caos Arcana Client] Inicializando cliente, HUD de Mana, atalhos de feitiços, entidades e sincronização...");

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

        registerEntityRendering();
        registerClientPackets();

        // Registra o HUD Dinâmico de Mana (Barra azul discreta sobre a saúde com fade-out inteligente)
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
        EntityModelLayerRegistry.registerModelLayer(
                AranhaCascaSoltaModel.LAYER_LOCATION,
                AranhaCascaSoltaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ARANHA_CASCA_SOLTA,
                AranhaCascaSoltaRenderer::new
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.TEIA_DE_CASCA,
                context -> new ThrownItemRenderer<>(context, 1.0F, true)
        );
        EntityModelLayerRegistry.registerModelLayer(
                EspiritoNevoaModel.LAYER_LOCATION,
                EspiritoNevoaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ESPIRITO_NEVOA,
                EspiritoNevoaRenderer::new
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ESPIRITO_NEVOA_PROJECTILE,
                context -> new ThrownItemRenderer<>(context, 1.0F, true)
        );
        EntityModelLayerRegistry.registerModelLayer(
                CultistaOcultoModel.LAYER_LOCATION,
                CultistaOcultoModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.CULTISTA_OCULTO,
                CultistaOcultoRenderer::new
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.MALDICAO_OCULTA_PROJECTILE,
                context -> new ThrownItemRenderer<>(context, 1.0F, true)
        );
        EntityModelLayerRegistry.registerModelLayer(
                AnciaoDasSombrasModel.LAYER_LOCATION,
                AnciaoDasSombrasModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ANCIAO_DAS_SOMBRAS,
                AnciaoDasSombrasRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                EscaravelhoSolarModel.LAYER_LOCATION,
                EscaravelhoSolarModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ESCARAVELHO_SOLAR,
                EscaravelhoSolarRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                ViboraDasArenasModel.LAYER_LOCATION,
                ViboraDasArenasModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.VIBORA_DAS_ARENAS,
                ViboraDasArenasRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                GolemDeAreiaModel.LAYER_LOCATION,
                GolemDeAreiaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.GOLEM_DE_AREIA,
                GolemDeAreiaRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                MumiaEsqueleticaModel.LAYER_LOCATION,
                MumiaEsqueleticaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.MUMIA_ESQUELETICA,
                MumiaEsqueleticaRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                FaraoDoSolEternoModel.LAYER_LOCATION,
                FaraoDoSolEternoModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.FARAO_DO_SOL_ETERNO,
                FaraoDoSolEternoRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                TragadorDoAbismoModel.LAYER_LOCATION,
                TragadorDoAbismoModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.TRAGADOR_DO_ABISMO,
                TragadorDoAbismoRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                GuardiaoDeObsidianaModel.LAYER_LOCATION,
                GuardiaoDeObsidianaModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.GUARDIAO_DE_OBSIDIANA,
                GuardiaoDeObsidianaRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                MedusaBioluminescenteModel.LAYER_LOCATION,
                MedusaBioluminescenteModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.MEDUSA_BIOLUMINESCENTE,
                MedusaBioluminescenteRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                SereiaDasProfundezasModel.LAYER_LOCATION,
                SereiaDasProfundezasModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.SEREIA_DAS_PROFUNDEZAS,
                SereiaDasProfundezasRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                LordeDasMaresAbissaisModel.LAYER_LOCATION,
                LordeDasMaresAbissaisModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.LORDE_DAS_MARES_ABISSAIS,
                LordeDasMaresAbissaisRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                CacadorDeGeloModel.LAYER_LOCATION,
                CacadorDeGeloModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.CACADOR_DE_GELO,
                CacadorDeGeloRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                GolemDeGeloModel.LAYER_LOCATION,
                GolemDeGeloModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.GOLEM_DE_GELO,
                GolemDeGeloRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                AranhaDoGeloModel.LAYER_LOCATION,
                AranhaDoGeloModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.ARANHA_DO_GELO,
                AranhaDoGeloRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                FantasmaDoGeloModel.LAYER_LOCATION,
                FantasmaDoGeloModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.FANTASMA_DO_GELO,
                FantasmaDoGeloRenderer::new
        );
        EntityModelLayerRegistry.registerModelLayer(
                ReiDoGeloModel.LAYER_LOCATION,
                ReiDoGeloModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                CaosArcanaEntities.REI_DO_GELO,
                ReiDoGeloRenderer::new
        );
        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> ((SpawnEggItem) stack.getItem()).getColor(tintIndex),
                CaosArcanaSpawnEggs.ALL_EGGS
        );
    }

    private void registerClientPackets() {
        // Recebe ordem para abrir tela de seleção de raça
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.OPEN_RACE_SELECT_ID, (client, handler, buf, responseSender) -> {
            client.execute(() -> client.setScreen(new RaceSelectionScreen()));
        });

        // Recebe ordem para abrir tela de estudo de grimório
        ClientPlayNetworking.registerGlobalReceiver(ModMessages.OPEN_GRIMOIRE_STUDY_S2C_ID, (client, handler, buf, responseSender) -> {
            String spellId = buf.readUtf();
            client.execute(() -> client.setScreen(new GrimoireStudyScreen(spellId)));
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
