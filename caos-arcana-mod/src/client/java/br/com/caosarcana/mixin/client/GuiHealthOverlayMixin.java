package br.com.caosarcana.mixin.client;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiHealthOverlayMixin {

    @Shadow @Final private static ResourceLocation GUI_ICONS_LOCATION;

    /**
     * Limita o cálculo de layout de linhas do Minecraft a no máximo 20 de vida (1 linha de 10 corações).
     * Impede que corações extras de raças (como Orc com 24 HP) empurrem a barra de armadura ou a HUD para cima.
     */
    @Redirect(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float caosarcana$capMaxHealthToSingleRow(float a, float b) {
        float max = Math.max(a, b);
        return Math.min(20.0f, max);
    }

    /**
     * Renderiza os corações extras de raça sobrepondo os corações vermelhos da linha base na cor DOURADA.
     * Quando o jogador toma dano, o coração dourado desaparece primeiro, revelando o coração vermelho por baixo.
     */
    @Inject(method = "renderHearts", at = @At("TAIL"))
    private void caosarcana$renderGoldenOverlayHearts(GuiGraphics guiGraphics, Player player, int x, int y, int height,
                                                      int offsetHeartIndex, float maxHealth, int currentHealth,
                                                      int displayHealth, int absorption, boolean blinking,
                                                      CallbackInfo ci) {
        if (player != null && player.getHealth() > 20.0f) {
            float extraHp = player.getHealth() - 20.0f;
            int fullGoldHearts = (int) (extraHp / 2.0f);
            boolean halfGoldHeart = (extraHp % 2.0f > 0.0f);

            boolean hardcore = player.level().getLevelData().isHardcore();
            int v = hardcore ? 45 : 0;

            // U = 160 é o coração dourado cheio nativo do Minecraft (HeartType.ABSORBING)
            // U = 169 é o meio coração dourado nativo
            for (int i = 0; i < fullGoldHearts && i < 10; i++) {
                int heartY = y;
                if (i == offsetHeartIndex) {
                    heartY -= 2; // Acompanha animação nativa de salto na regeneração
                }
                guiGraphics.blit(GUI_ICONS_LOCATION, x + (i * 8), heartY, 160, v, 9, 9);
            }

            if (halfGoldHeart && fullGoldHearts < 10) {
                int heartY = y;
                if (fullGoldHearts == offsetHeartIndex) {
                    heartY -= 2;
                }
                guiGraphics.blit(GUI_ICONS_LOCATION, x + (fullGoldHearts * 8), heartY, 169, v, 9, 9);
            }
        }
    }
}
