package br.com.caosarcana.content.spells;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Grimório de Centelha Ígnea.
 * O livro serve para estudar e aprender permanentemente a magia.
 */
public class FlameSparkItem extends SpellGrimoireItem {
    public static final float MANA_COST = 20.0f;
    public static final int COOLDOWN_TICKS = 15; // 0.75 segundos

    public FlameSparkItem() {
        super(SpellCastHandler.CENTELHA_IGNEA_ID);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.literal("§6✦ Grimório: Centelha Ígnea ✦").withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§6Escola: §cPiromancia §7(Requisito: Nível 1)"));
        tooltip.add(Component.literal("§bCusto de Conjuração: §f20 Mana"));
        tooltip.add(Component.literal("§7Clique direito para abrir o estudo e gravar a magia permanentemente.").withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.literal("§8Após aprendida, a magia pode ser conjurada pela tecla [V] sem o livro.").withStyle(ChatFormatting.DARK_GRAY));
    }
}
