package br.com.caosarcana.client.gui;

import br.com.caosarcana.client.ClientRPGData;
import br.com.caosarcana.common.data.IRPGPlayerData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ManaHudOverlay {
    private static long fullManaStartTime = -1L;
    private static float currentAlpha = 0.0f;

    public static void wakeUp() {
        currentAlpha = 1.0f;
        fullManaStartTime = -1L;
    }

    public static void render(GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.options.hideGui) {
            return;
        }

        Player player = client.player;
        if (player.isSpectator()) {
            return;
        }

        // Obtém dados sincronizados
        float mana;
        float maxMana;
        if (player instanceof IRPGPlayerData rpgPlayer && rpgPlayer.caosarcana$hasChosenRace()) {
            mana = rpgPlayer.caosarcana$getMana();
            maxMana = rpgPlayer.caosarcana$getMaxMana();
        } else if (ClientRPGData.hasChosenRace()) {
            mana = ClientRPGData.getMana();
            maxMana = ClientRPGData.getMaxMana();
        } else {
            return; // Sem raça escolhida, não renderiza
        }

        if (maxMana <= 0) {
            maxMana = 100.0f;
        }

        long now = System.currentTimeMillis();

        // Lógica de Fade-Out Inteligente
        // Só aparece quando a mana está baixa (< 100%).
        // Ao atingir 100%, aguarda 5 segundos e desaparece suavemente ao longo de 1.5s.
        if (mana < maxMana - 0.5f) {
            fullManaStartTime = -1L;
            currentAlpha = 1.0f; // Visível imediatamente
        } else {
            if (fullManaStartTime == -1L) {
                fullManaStartTime = now;
            }
            long elapsed = now - fullManaStartTime;
            if (elapsed < 5000L) {
                currentAlpha = 1.0f; // Permanece visível por 5 segundos a 100%
            } else if (elapsed < 6500L) {
                float progress = (elapsed - 5000L) / 1500.0f;
                currentAlpha = Math.max(0.0f, 1.0f - progress); // Fade out suave
            } else {
                currentAlpha = 0.0f; // Desapareceu completamente
            }
        }

        if (currentAlpha <= 0.01f) {
            return; // Invisível, não gasta processamento
        }

        int alpha = (int) (currentAlpha * 255.0f) & 0xFF;
        if (alpha <= 0) {
            return;
        }

        int screenWidth = guiGraphics.guiWidth();
        int screenHeight = guiGraphics.guiHeight();

        // Posicionamento discreto em cima da coluna de saúde (corações)
        int barWidth = 81; // Largura exata dos 10 corações do Minecraft
        int barHeight = 8;
        int x = (screenWidth / 2) - 91;

        // Calcula a altura dinâmica acima dos corações e armadura (corações extras sobrepõem na cor dourada sem subir linha)
        int baseY = screenHeight - 39;
        int armor = player.getArmorValue();
        if (armor > 0) {
            baseY -= 10;
        }
        int y = baseY - barHeight - 2;

        // Cores com canal Alpha multiplicado
        int colorBackground = (alpha << 24) | 0x111625; // Azul ardósia escuro medieval
        int colorBorder = (alpha << 24) | 0x334466;     // Borda metálica sutil
        int colorManaFill = (alpha << 24) | 0x1E88E5;   // Azul arcano vibrante
        int colorManaHighlight = (alpha << 24) | 0x42A5F5; // Brilho de topo da barra

        // 1. Fundo e Contorno
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, colorBackground);
        guiGraphics.renderOutline(x, y, barWidth, barHeight, colorBorder);

        // 2. Preenchimento de Mana
        float fillRatio = Math.max(0.0f, Math.min(1.0f, mana / maxMana));
        int innerWidth = barWidth - 2;
        int filledPixels = Math.round(innerWidth * fillRatio);

        if (filledPixels > 0) {
            int fillX1 = x + 1;
            int fillY1 = y + 1;
            int fillX2 = fillX1 + filledPixels;
            int fillY2 = y + barHeight - 1;

            // Linha superior com brilho mágico
            guiGraphics.fill(fillX1, fillY1, fillX2, fillY1 + 1, colorManaHighlight);
            // Corpo principal da barra
            guiGraphics.fill(fillX1, fillY1 + 1, fillX2, fillY2, colorManaFill);
        }

        // 3. Texto numérico centralizado: 23/100
        Font font = client.font;
        String text = (int) Math.ceil(mana) + "/" + (int) maxMana;
        int textWidth = font.width(text);
        int textX = x + (barWidth / 2) - (textWidth / 2);
        int textY = y + (barHeight / 2) - 4;

        int textColor = (alpha << 24) | 0xE0F7FA;
        guiGraphics.drawString(font, text, textX, textY, textColor, true);
    }
}
