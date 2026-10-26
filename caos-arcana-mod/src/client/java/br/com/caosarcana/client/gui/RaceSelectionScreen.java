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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

@Environment(EnvType.CLIENT)
public class RaceSelectionScreen extends Screen {
    private Race selectedRace = Race.ORC;
    private Button confirmButton;

    public RaceSelectionScreen() {
        super(Component.literal("Escolha de Raça - Caos Arcana"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int listX = centerX - 190;
        int startY = 65;
        int buttonWidth = 110;
        int buttonHeight = 24;

        Race[] availableRaces = new Race[]{Race.ORC, Race.FELINO, Race.ALTO_ELFO};

        for (int i = 0; i < availableRaces.length; i++) {
            Race race = availableRaces[i];
            int y = startY + (i * (buttonHeight + 8));

            this.addRenderableWidget(Button.builder(Component.literal(race.getDisplayName()), btn -> {
                this.selectedRace = race;
            }).bounds(listX, y, buttonWidth, buttonHeight).build());
        }

        int confirmY = this.height - 38;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, "§6§lCAOS ARCANA: CRIAÇÃO DE PERSONAGEM", centerX, 14, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Escolha sua linhagem no Server do Caos. Essa escolha definirá seu destino.", centerX, 28, 0xAAAAAA);

        int panelX = centerX - 65;
        int panelY = 48;
        int panelWidth = 265;
        int panelHeight = this.height - 92;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x88000000);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFF444444);

        if (selectedRace != null && selectedRace != Race.NONE) {
            int textY = panelY + 8;
            int textX = panelX + 10;
            int wrapWidth = panelWidth - 20;

            graphics.drawString(this.font, "§e§lRaça: §f" + selectedRace.getDisplayName(), textX, textY, 0xFFFFFF);
            textY += 12;

            graphics.drawString(this.font, "§8Estado: §7" + selectedRace.getDocumentationStatus(), textX, textY, 0xAAAAAA);
            textY += 14;

            List<FormattedCharSequence> descLines = font.split(Component.literal("§7" + selectedRace.getDescription()), wrapWidth);
            for (FormattedCharSequence line : descLines) {
                graphics.drawString(this.font, line, textX, textY, 0xFFFFFF);
                textY += 10;
            }
            textY += 8;

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
