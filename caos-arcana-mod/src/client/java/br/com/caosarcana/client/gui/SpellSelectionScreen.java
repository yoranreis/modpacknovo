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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface gráfica de seleção de magia ativa do Caos Arcana.
 * Apresenta somente magias aprendidas e permite definir a magia canalizada pela tecla de disparo [V].
 */
@Environment(EnvType.CLIENT)
public class SpellSelectionScreen extends Screen {
    private final int boxWidth = 340;
    private final int boxHeight = 220;
    private String inspectedSpellId = "";
    private Button selectActiveButton;

    public SpellSelectionScreen() {
        super(Component.literal("Grimório de Magias Aprendidas"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int left = centerX - (boxWidth / 2);
        int top = centerY - (boxHeight / 2);

        List<String> learnedSpells = new ArrayList<>(ClientRPGData.getKnownSpells());
        String currentActive = ClientRPGData.getSelectedSpell();

        // Se houver magias e nenhuma inspecionada, define a ativa (ou a primeira)
        if (!learnedSpells.isEmpty()) {
            if (inspectedSpellId.isEmpty() || !learnedSpells.contains(inspectedSpellId)) {
                inspectedSpellId = (!currentActive.isEmpty() && learnedSpells.contains(currentActive))
                        ? currentActive
                        : learnedSpells.get(0);
            }
        }

        if (learnedSpells.isEmpty()) {
            // Estado vazio: botão único de fechar
            this.addRenderableWidget(Button.builder(
                    Component.literal("Fechar"),
                    btn -> this.onClose()
            ).bounds(centerX - 40, top + boxHeight - 34, 80, 20).build());
            return;
        }

        // Coluna da esquerda: botões para listar magias aprendidas
        int listX = left + 12;
        int listStartY = top + 36;
        int itemBtnWidth = 118;
        int itemBtnHeight = 22;

        for (int i = 0; i < learnedSpells.size() && i < 7; i++) {
            String spellId = learnedSpells.get(i);
            Optional<SpellDefinition> defOpt = SpellRegistry.get(spellId);
            String name = defOpt.map(SpellDefinition::name).orElse(spellId);
            boolean isRacial = defOpt.map(SpellDefinition::isRacial).orElse(false);

            boolean isActive = spellId.equalsIgnoreCase(ClientRPGData.getSelectedSpell());
            Component btnTitle = isActive
                    ? Component.literal("★ " + name).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    : (isRacial ? Component.literal("✦ " + name).withStyle(ChatFormatting.AQUA) : Component.literal(name));

            int btnY = listStartY + (i * (itemBtnHeight + 2));
            this.addRenderableWidget(Button.builder(btnTitle, btn -> {
                this.inspectedSpellId = spellId;
                this.rebuildWidgets();
            }).bounds(listX, btnY, itemBtnWidth, itemBtnHeight).build());
        }

        // Painel da direita: botão para selecionar como ativa
        int rightColX = left + 140;
        int actionY = top + boxHeight - 34;

        boolean isInspectedActive = inspectedSpellId.equalsIgnoreCase(ClientRPGData.getSelectedSpell());

        if (isInspectedActive) {
            this.selectActiveButton = this.addRenderableWidget(Button.builder(
                    Component.literal("✓ Magia Ativa").withStyle(ChatFormatting.DARK_GREEN),
                    btn -> {}
            ).bounds(rightColX, actionY, 115, 20).build());
            this.selectActiveButton.active = false;
        } else {
            this.selectActiveButton = this.addRenderableWidget(Button.builder(
                    Component.literal("Definir Ativa").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                    btn -> {
                        if (!inspectedSpellId.isEmpty()) {
                            FriendlyByteBuf buf = PacketByteBufs.create();
                            buf.writeUtf(inspectedSpellId);
                            ClientPlayNetworking.send(ModMessages.SELECT_SPELL_C2S_ID, buf);
                            ClientRPGData.setSelectedSpell(inspectedSpellId);
                            this.rebuildWidgets();
                        }
                    }
            ).bounds(rightColX, actionY, 115, 20).build());
        }

        // Botão para fechar no canto inferior direito
        this.addRenderableWidget(Button.builder(
                Component.literal("Fechar"),
                btn -> this.onClose()
        ).bounds(rightColX + 120, actionY, 65, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int left = centerX - (boxWidth / 2);
        int top = centerY - (boxHeight / 2);

        // Moldura Arcana
        graphics.fill(left, top, left + boxWidth, top + boxHeight, 0xFA15100B);
        graphics.fill(left + 2, top + 2, left + boxWidth - 2, top + boxHeight - 2, 0xF51E1710);
        graphics.renderOutline(left, top, boxWidth, boxHeight, 0xFFC69E58);
        graphics.renderOutline(left + 2, top + 2, boxWidth - 4, boxHeight - 4, 0x888B6E38);

        List<String> learnedSpells = new ArrayList<>(ClientRPGData.getKnownSpells());

        if (learnedSpells.isEmpty()) {
            // Renderização do Estado Vazio
            graphics.drawCenteredString(this.font, "§6§l✦ Grimório de Magias Pessoais ✦", centerX, top + 16, 0xFFFFFF);
            graphics.fill(left + 20, top + 34, left + boxWidth - 20, top + 35, 0xFF7D5F31);

            graphics.drawCenteredString(this.font, "§c§lNenhuma Magia Aprendida", centerX, centerY - 25, 0xFF5555);
            graphics.drawCenteredString(this.font, "§7Seu personagem ainda não memorizou nenhum feitiço.", centerX, centerY - 8, 0xAAAAAA);
            graphics.drawCenteredString(this.font, "§7Encontre e estude grimórios arcanos pelo mundo", centerX, centerY + 6, 0xAAAAAA);
            graphics.drawCenteredString(this.font, "§8para gravar novos poderes permanentemente.", centerX, centerY + 20, 0x888888);

            super.render(graphics, mouseX, mouseY, delta);
            return;
        }

        // Título Principal
        graphics.drawCenteredString(this.font, "§6§l✦ Grimório de Magias Aprendidas ✦", centerX, top + 12, 0xFFFFFF);
        graphics.fill(left + 15, top + 26, left + boxWidth - 15, top + 27, 0xFF7D5F31);

        // Divisória vertical entre a lista e os detalhes
        int divX = left + 135;
        graphics.fill(divX, top + 32, divX + 1, top + boxHeight - 40, 0xFF7D5F31);

        // Detalhes da Magia Selecionada na Coluna da Direita
        int detailsX = left + 142;
        Optional<SpellDefinition> spellOpt = SpellRegistry.get(inspectedSpellId);

        if (spellOpt.isPresent()) {
            SpellDefinition spell = spellOpt.get();

            boolean isActive = spell.id().equalsIgnoreCase(ClientRPGData.getSelectedSpell());
            String titleText = isActive ? "§6★ §c§l" + spell.name() + " §7(Ativa)" : "§c§l" + spell.name();
            graphics.drawString(this.font, titleText, detailsX, top + 34, 0xFFFFFF, false);

            graphics.drawString(this.font, "§6Escola: §e" + spell.schoolDisplayName() + " §7(Nível " + spell.minSchoolLevel() + ")", detailsX, top + 48, 0xFFFFFF, false);
            graphics.drawString(this.font, "§bCusto: §f" + (int) spell.manaCost() + " Mana", detailsX, top + 62, 0xFFFFFF, false);
            graphics.drawString(this.font, "§7Recarga: §f" + spell.cooldownTicks() + " ticks §7(" + String.format("%.1fs", spell.cooldownTicks() / 20.0f) + ")", detailsX, top + 76, 0xFFFFFF, false);
            String distStr = spell.maxDistanceBlocks() > 0 ? (int) spell.maxDistanceBlocks() + " blocos" : "Pessoal / Instantâneo";
            graphics.drawString(this.font, "§7Alcance: §f" + distStr, detailsX, top + 90, 0xFFFFFF, false);

            graphics.fill(detailsX, top + 104, left + boxWidth - 14, top + 105, 0x447D5F31);
            graphics.drawString(this.font, "§eDescrição do Efeito:", detailsX, top + 109, 0xFFFFFF, false);

            // Quebra e renderização limpa da descrição
            int descY = top + 122;
            List<net.minecraft.util.FormattedCharSequence> lines = this.font.split(Component.literal(spell.description()), boxWidth - 156);
            for (int i = 0; i < lines.size() && i < 4; i++) {
                graphics.drawString(this.font, lines.get(i), detailsX, descY + (i * 11), 0xAAAAAA, false);
            }
        } else {
            graphics.drawString(this.font, "§7Selecione uma magia da lista.", detailsX, top + 50, 0x888888, false);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
