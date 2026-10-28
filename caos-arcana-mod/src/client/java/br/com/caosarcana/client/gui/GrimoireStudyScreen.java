package br.com.caosarcana.client.gui;

import br.com.caosarcana.client.ClientRPGData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.content.spells.SpellDefinition;
import br.com.caosarcana.content.spells.SpellRegistry;
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

import java.util.List;
import java.util.Optional;

/**
 * Interface gráfica para estudo de grimórios e confirmação de aprendizado de magias.
 * Suporta dinamicamente qualquer magia registrada no SpellRegistry.
 */
@Environment(EnvType.CLIENT)
public class GrimoireStudyScreen extends Screen {
    private final String spellId;
    private final int boxWidth = 280;
    private final int boxHeight = 210;

    public GrimoireStudyScreen(String spellId) {
        super(Component.literal("Estudo de Grimório Arcano"));
        this.spellId = spellId;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int top = centerY - (boxHeight / 2);

        boolean alreadyKnown = ClientRPGData.knowsSpell(this.spellId);

        if (alreadyKnown) {
            // Se já conhece, exibe botão informativo desabilitado e botão para fechar
            this.addRenderableWidget(Button.builder(
                    Component.literal("✓ Conhecimento Dominado").withStyle(ChatFormatting.DARK_GREEN),
                    btn -> {}
            ).bounds(centerX - 100, top + 172, 130, 22).build()).active = false;

            this.addRenderableWidget(Button.builder(
                    Component.literal("Fechar"),
                    btn -> this.onClose()
            ).bounds(centerX + 35, top + 172, 65, 22).build());
        } else {
            // Botão para confirmar o aprendizado no servidor
            this.addRenderableWidget(Button.builder(
                    Component.literal("Gravar Conhecimento").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    btn -> {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        buf.writeUtf(this.spellId);
                        ClientPlayNetworking.send(ModMessages.LEARN_SPELL_C2S_ID, buf);
                        this.onClose();
                    }
            ).bounds(centerX - 110, top + 172, 135, 22).build());

            // Botão para cancelar estudo
            this.addRenderableWidget(Button.builder(
                    Component.literal("Cancelar"),
                    btn -> this.onClose()
            ).bounds(centerX + 30, top + 172, 80, 22).build());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int left = centerX - (boxWidth / 2);
        int top = centerY - (boxHeight / 2);

        // Moldura arcana estilizada (Fundo pergaminho escuro com borda dourada)
        graphics.fill(left, top, left + boxWidth, top + boxHeight, 0xFA15100B);
        graphics.fill(left + 2, top + 2, left + boxWidth - 2, top + boxHeight - 2, 0xF51E1710);
        graphics.renderOutline(left, top, boxWidth, boxHeight, 0xFFC69E58);
        graphics.renderOutline(left + 2, top + 2, boxWidth - 4, boxHeight - 4, 0x888B6E38);

        Optional<SpellDefinition> spellOpt = SpellRegistry.get(this.spellId);
        String spellName = spellOpt.map(SpellDefinition::name).orElse(this.spellId);
        String schoolName = spellOpt.map(SpellDefinition::schoolDisplayName).orElse("Arcano");
        int reqLevel = spellOpt.map(SpellDefinition::minSchoolLevel).orElse(1);
        int manaCost = spellOpt.map(s -> (int) s.manaCost()).orElse(20);
        int cooldown = spellOpt.map(SpellDefinition::cooldownTicks).orElse(20);
        double maxDist = spellOpt.map(SpellDefinition::maxDistanceBlocks).orElse(0.0);
        String desc = spellOpt.map(SpellDefinition::description).orElse("Escrituras arcanas antigas.");

        // Títulos
        graphics.drawCenteredString(this.font, "§6§l✦ Estudo de Grimório Arcano ✦", centerX, top + 12, 0xFFFFFF);
        graphics.drawCenteredString(this.font, "§c§l" + spellName, centerX, top + 28, 0xFF5555);

        // Divisória decorativa
        graphics.fill(left + 15, top + 42, left + boxWidth - 15, top + 43, 0xFF7D5F31);

        // Requisitos e Atributos da Magia
        int textX = left + 16;
        graphics.drawString(this.font, "§6Escola: §e" + schoolName + " §7(Requisito: §aNível " + reqLevel + "§7)", textX, top + 48, 0xFFFFFF, false);
        graphics.drawString(this.font, "§bCusto de Conjuração: §f" + manaCost + " Mana", textX, top + 62, 0xFFFFFF, false);
        graphics.drawString(this.font, "§7Tempo de Recarga: §f" + cooldown + " ticks §7(" + String.format("%.1fs", cooldown / 20.0f) + ")", textX, top + 76, 0xFFFFFF, false);
        String distStr = maxDist > 0 ? (int) maxDist + " blocos" : "Pessoal / Instantâneo";
        graphics.drawString(this.font, "§7Alcance Limite: §f" + distStr, textX, top + 90, 0xFFFFFF, false);

        // Descrição e Efeito (quebra em até 2 linhas)
        graphics.fill(left + 15, top + 104, left + boxWidth - 15, top + 105, 0x447D5F31);
        graphics.drawString(this.font, "§eDescrição do Efeito:", textX, top + 110, 0xFFFFFF, false);

        List<net.minecraft.util.FormattedCharSequence> wrappedLines = this.font.split(Component.literal("§7" + desc), boxWidth - 32);
        int lineY = top + 122;
        for (int i = 0; i < wrappedLines.size() && i < 3; i++) {
            graphics.drawString(this.font, wrappedLines.get(i), textX + 4, lineY, 0xAAAAAA, false);
            lineY += 11;
        }

        // Status de Conhecimento
        boolean alreadyKnown = ClientRPGData.knowsSpell(this.spellId);
        if (alreadyKnown) {
            graphics.drawString(this.font, "§a✓ Esta magia já está gravada na sua alma.", textX, top + 158, 0x55FF55, false);
        } else {
            graphics.drawString(this.font, "§6Grave a magia para conjurar pela tecla [V].", textX, top + 158, 0xFFAA00, false);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
