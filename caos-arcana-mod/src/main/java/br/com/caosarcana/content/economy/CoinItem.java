package br.com.caosarcana.content.economy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Moedas físicas do sistema econômico do Caos Arcana.
 * Não são mineráveis de blocos de minério comuns (para evitar inflação vanilla).
 * Encontradas em baús de dungeons, ruínas, recompensas de provações e comércio entre jogadores e NPCs.
 */
public class CoinItem extends Item {
    public enum CoinType {
        COBRE("Moeda de Cobre", 1, Rarity.COMMON, "§6Moeda de Cobre Antiga", "Base do comércio diário e tavernas. (100 Cobre = 1 Prata)"),
        PRATA("Moeda de Prata", 100, Rarity.UNCOMMON, "§fMoeda de Prata Imperial", "Cunhagem nobre para trocas de caravanas. (100 Prata = 1 Ouro)"),
        OURO("Septim de Ouro", 10000, Rarity.RARE, "§eSeptim de Ouro Maciço", "A mais alta moeda do império, aceita por mestres arcanos.");

        private final String displayName;
        private final int valueInCopper;
        private final Rarity rarity;
        private final String title;
        private final String lore;

        CoinType(String displayName, int valueInCopper, Rarity rarity, String title, String lore) {
            this.displayName = displayName;
            this.valueInCopper = valueInCopper;
            this.rarity = rarity;
            this.title = title;
            this.lore = lore;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getValueInCopper() {
            return valueInCopper;
        }

        public Rarity getRarity() {
            return rarity;
        }

        public String getTitle() {
            return title;
        }

        public String getLore() {
            return lore;
        }
    }

    private final CoinType coinType;

    public CoinItem(CoinType coinType) {
        super(new Item.Properties().stacksTo(64).rarity(coinType.getRarity()));
        this.coinType = coinType;
    }

    public CoinType getCoinType() {
        return coinType;
    }

    public int getValueInCopper() {
        return coinType.getValueInCopper();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.literal(coinType.getTitle()).withStyle(ChatFormatting.BOLD));
        tooltip.add(Component.literal("§7" + coinType.getLore()));
        tooltip.add(Component.literal("§8Valor Econômico: §6" + coinType.getValueInCopper() + " §8Cobre"));
    }
}
