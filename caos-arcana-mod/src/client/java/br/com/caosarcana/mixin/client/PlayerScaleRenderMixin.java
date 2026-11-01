package br.com.caosarcana.mixin.client;

import br.com.caosarcana.client.ClientRPGData;
import br.com.caosarcana.client.gui.RaceSelectionScreen;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Protótipo Experimental de Escala Visual para Jogadores (Lote B).
 *
 * Restrito estritamente a PlayerRenderer com isolamento da matriz:
 * - A chamada ocorre dentro de LivingEntityRenderer.render(), entre poseStack.pushPose() e poseStack.popPose().
 * - Garante que apenas o modelo, armaduras (HumanoidArmorLayer) e itens empunhados (ItemInHandLayer)
 *   do jogador sofram a transformação, sem afetar o mundo, outras entidades ou modelos do auxiliar.
 * - Não altera câmera, hitbox, alcance de ataque ou regras físicas.
 */
@Environment(EnvType.CLIENT)
@Mixin(PlayerRenderer.class)
public abstract class PlayerScaleRenderMixin {

    @Inject(method = "scale(Lnet/minecraft/client/player/AbstractClientPlayer;Lcom/mojang/blaze3d/vertex/PoseStack;F)V", at = @At("TAIL"))
    private void caosarcana$applyRacialVisualScale(AbstractClientPlayer player, PoseStack poseStack, float partialTick, CallbackInfo ci) {
        if (player == null || poseStack == null) return;

        Race race = Race.NONE;

        // 1. Pré-visualização na tela de criação de personagem (RaceSelectionScreen)
        if (Minecraft.getInstance().screen instanceof RaceSelectionScreen) {
            Race preview = RaceSelectionScreen.getCurrentPreviewRace();
            if (preview != null && preview != Race.NONE) {
                race = preview;
            }
        }

        // 2. Se não estiver em prévia interativa na tela, obtém a raça confirmada
        if (race == Race.NONE) {
            if (player instanceof IRPGPlayerData rpgPlayer && rpgPlayer.caosarcana$hasChosenRace()) {
                race = rpgPlayer.caosarcana$getRace();
            } else if (player == Minecraft.getInstance().player && ClientRPGData.hasChosenRace()) {
                race = ClientRPGData.getCurrentRace();
            }
        }

        float visualScale = (race != null) ? race.getVisualScale() : 1.0f;
        if (visualScale > 0.0f && visualScale != 1.0f) {
            poseStack.scale(visualScale, visualScale, visualScale);
        }
    }
}
