package br.com.caosarcana.client.gui;

import br.com.caosarcana.client.ClientRPGData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RaceSelectionScreen extends Screen {
    private static Race currentPreviewRace = null;
    private Race selectedRace = Race.ORC;
    private Button confirmButton;

    public static Race getCurrentPreviewRace() {
        return currentPreviewRace;
    }

    public Race getSelectedRace() {
        return selectedRace;
    }

    public RaceSelectionScreen() {
        super(Component.literal("Escolha de Raça - Caos Arcana"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int listX = centerX - 250;
        int startY = 46;
        int buttonWidth = 110;
        int buttonHeight = 18;
        int spacing = 3;

        Race[] availableRaces = new Race[]{
                Race.HUMANO,
                Race.ELFO_FLORESTA,
                Race.ALTO_ELFO,
                Race.ORC,
                Race.FELINO,
                Race.NORDICO,
                Race.SANGUE_DAEDRA,
                Race.REDGUARD,
                Race.ARGONIANO,
                Race.VOIDMER
        };

        for (int i = 0; i < availableRaces.length; i++) {
            Race race = availableRaces[i];
            int y = startY + (i * (buttonHeight + spacing));

            this.addRenderableWidget(Button.builder(Component.literal(race.getDisplayName()), btn -> {
                this.selectedRace = race;
            }).bounds(listX, y, buttonWidth, buttonHeight).build());
        }

        int confirmY = this.height - 34;
        this.confirmButton = this.addRenderableWidget(Button.builder(
                Component.literal("CONFIRMAR ESCOLHA").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD),
                btn -> confirmSelection()
        ).bounds(centerX - 90, confirmY, 180, 24).build());
    }

    private void confirmSelection() {
        if (selectedRace != null && selectedRace != Race.NONE) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(selectedRace.getId());
            ClientPlayNetworking.send(ModMessages.SELECT_RACE_C2S_ID, buf);
            this.onClose();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return ClientRPGData.hasChosenRace();
    }

    @Override
    public void removed() {
        super.removed();
        currentPreviewRace = null;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        currentPreviewRace = this.selectedRace;
        this.renderBackground(graphics);

        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, "§6§lCAOS ARCANA: CRIAÇÃO DE PERSONAGEM", centerX, 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Escolha sua linhagem no Server do Caos. Essa escolha definirá seu destino.", centerX, 27, 0xAAAAAA);

        int panelY = 46;
        int panelHeight = this.height - 88;

        // 1. Painel Central: Visualização 3D do Personagem (Modelo e Skin)
        int previewBoxX = centerX - 130;
        int previewBoxWidth = 110;

        graphics.fill(previewBoxX, panelY, previewBoxX + previewBoxWidth, panelY + panelHeight, 0x88000000);
        graphics.renderOutline(previewBoxX, panelY, previewBoxWidth, panelHeight, 0xFF555555);

        graphics.drawCenteredString(this.font, "§ePrévia 3D", previewBoxX + (previewBoxWidth / 2), panelY + 8, 0xFFFFAA);
        graphics.drawCenteredString(this.font, "§8[" + selectedRace.getDisplayName() + "]", previewBoxX + (previewBoxWidth / 2), panelY + 20, 0x888888);

        // Renderiza o modelo 3D do jogador que segue o movimento do cursor do mouse
        if (this.minecraft != null && this.minecraft.player != null) {
            int entityX = previewBoxX + (previewBoxWidth / 2);
            int entityY = panelY + panelHeight - 25;
            int scale = Math.min(48, Math.max(30, panelHeight / 4));

            InventoryScreen.renderEntityInInventoryFollowsMouse(
                    graphics,
                    entityX,
                    entityY,
                    scale,
                    (float) entityX - mouseX,
                    (float) (entityY - 45) - mouseY,
                    this.minecraft.player
            );
        }

        // 2. Painel Direito: Detalhes, Vantagens e Fraquezas da Raça
        int detailsX = centerX - 10;
        int detailsWidth = 250;

        graphics.fill(detailsX, panelY, detailsX + detailsWidth, panelY + panelHeight, 0x88000000);
        graphics.renderOutline(detailsX, panelY, detailsWidth, panelHeight, 0xFF555555);

        if (selectedRace != null && selectedRace != Race.NONE) {
            int textY = panelY + 8;
            int textX = detailsX + 10;
            int wrapWidth = detailsWidth - 20;

            graphics.drawString(this.font, "§e§lRaça: §f" + selectedRace.getDisplayName(), textX, textY, 0xFFFFFF);
            textY += 12;

            graphics.drawString(this.font, "§8Estado: §7" + selectedRace.getDocumentationStatus(), textX, textY, 0xAAAAAA);
            textY += 13;

            List<FormattedCharSequence> descLines = font.split(Component.literal("§7" + selectedRace.getDescription()), wrapWidth);
            for (FormattedCharSequence line : descLines) {
                graphics.drawString(this.font, line, textX, textY, 0xFFFFFF);
                textY += 10;
            }
            textY += 7;

            if (!selectedRace.getActiveAdvantages().equals("Nenhuma")) {
                graphics.drawString(this.font, "§a§lVantagens Ativas (Funcionais):", textX, textY, 0x55FF55);
                textY += 11;
                List<FormattedCharSequence> advLines = font.split(Component.literal("§a• " + selectedRace.getActiveAdvantages()), wrapWidth);
                for (FormattedCharSequence line : advLines) {
                    graphics.drawString(this.font, line, textX, textY, 0xAAFFAA);
                    textY += 10;
                }
                textY += 6;
            }

            if (!selectedRace.getActiveDisadvantages().equals("Nenhuma")) {
                graphics.drawString(this.font, "§c§lPenalidades Ativas (Funcionais):", textX, textY, 0xFF5555);
                textY += 11;
                List<FormattedCharSequence> disLines = font.split(Component.literal("§c• " + selectedRace.getActiveDisadvantages()), wrapWidth);
                for (FormattedCharSequence line : disLines) {
                    graphics.drawString(this.font, line, textX, textY, 0xFFAAAA);
                    textY += 10;
                }
                textY += 6;
            }

            if (!selectedRace.getPartialTraits().equals("Nenhuma")) {
                graphics.drawString(this.font, "§6§lParcialmente Implementado:", textX, textY, 0xFFAA00);
                textY += 11;
                List<FormattedCharSequence> partialLines = font.split(Component.literal("§6• " + selectedRace.getPartialTraits()), wrapWidth);
                for (FormattedCharSequence line : partialLines) {
                    graphics.drawString(this.font, line, textX, textY, 0xFFDDAA);
                    textY += 10;
                }
                textY += 6;
            }

            if (!selectedRace.getPlannedTraits().equals("Nenhuma")) {
                graphics.drawString(this.font, "§8§lApenas Planejado (Sem efeito no momento):", textX, textY, 0x888888);
                textY += 11;
                List<FormattedCharSequence> planLines = font.split(Component.literal("§8• " + selectedRace.getPlannedTraits()), wrapWidth);
                for (FormattedCharSequence line : planLines) {
                    graphics.drawString(this.font, line, textX, textY, 0x777777);
                    textY += 10;
                }
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
