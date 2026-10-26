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

        // Botão de Confirmação
        int confirmY = this.height - 42;
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

        // Título Principal
        graphics.drawCenteredString(this.font, "§6§lCAOS ARCANA: CRIAÇÃO DE PERSONAGEM", centerX, 18, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§7Escolha sua linhagem no Server do Caos. Essa escolha definirá seu destino.", centerX, 34, 0xAAAAAA);

        // Painel de Detalhes da Raça Selecionada
        int panelX = centerX - 60;
        int panelY = 60;
        int panelWidth = 250;
        int panelHeight = this.height - 115;

        // Fundo do painel
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x88000000);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFF444444);

        if (selectedRace != null) {
            int textY = panelY + 12;
            int textX = panelX + 12;
            int wrapWidth = panelWidth - 24;

            // Nome da Raça
            graphics.drawString(this.font, "§e§lRaça: " + selectedRace.getDisplayName(), textX, textY, 0xFFFFFF);
            textY += 18;

            // Descrição da Raça
            List<FormattedCharSequence> descLines = font.split(Component.literal("§7" + selectedRace.getDescription()), wrapWidth);
            for (FormattedCharSequence line : descLines) {
                graphics.drawString(this.font, line, textX, textY, 0xFFFFFF);
                textY += 11;
            }
            textY += 10;

            // Vantagens
            graphics.drawString(this.font, "§a§lVantagens Raciais:", textX, textY, 0x55FF55);
            textY += 13;
            List<FormattedCharSequence> advLines = font.split(Component.literal("§a• " + selectedRace.getAdvantages()), wrapWidth);
            for (FormattedCharSequence line : advLines) {
                graphics.drawString(this.font, line, textX, textY, 0xAAFFAA);
                textY += 11;
            }
            textY += 10;

            // Desvantagens
            graphics.drawString(this.font, "§c§lPenalidades & Fraquezas:", textX, textY, 0xFF5555);
            textY += 13;
            List<FormattedCharSequence> disLines = font.split(Component.literal("§c• " + selectedRace.getDisadvantages()), wrapWidth);
            for (FormattedCharSequence line : disLines) {
                graphics.drawString(this.font, line, textX, textY, 0xFFAAAA);
                textY += 11;
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
