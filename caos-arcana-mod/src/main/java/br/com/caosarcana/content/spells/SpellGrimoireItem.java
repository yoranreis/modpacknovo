package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Item genérico de Grimório Arcano.
 * Permite estudar e gravar permanentemente a magia contida em suas páginas místicas.
 */
public class SpellGrimoireItem extends Item {
    private final String spellId;

    public SpellGrimoireItem(String spellId) {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
        this.spellId = spellId;
    }

    public String getSpellId() {
        return spellId;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        if (player instanceof ServerPlayer serverPlayer && serverPlayer instanceof IRPGPlayerData rpgPlayer) {
            // 1. Exige ter escolhido uma raça para estudar
            if (!rpgPlayer.caosarcana$hasChosenRace()) {
                serverPlayer.displayClientMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de estudar grimórios!"), true);
                serverPlayer.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                return InteractionResultHolder.fail(stack);
            }

            // 2. Abre a tela de estudo no cliente
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(this.spellId);
            ServerPlayNetworking.send(serverPlayer, ModMessages.OPEN_GRIMOIRE_STUDY_S2C_ID, buf);

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        Optional<SpellDefinition> spellOpt = SpellRegistry.get(this.spellId);
        if (spellOpt.isPresent()) {
            SpellDefinition spell = spellOpt.get();
            tooltip.add(Component.literal("§6✦ Grimório Arcano ✦").withStyle(ChatFormatting.BOLD));
            tooltip.add(Component.literal("§eFeitiço Contido: §f" + spell.name()));
            tooltip.add(Component.literal("§7Escola: §b" + spell.schoolDisplayName() + " §7(Nível " + spell.minSchoolLevel() + ")"));
            tooltip.add(Component.literal("§7Custo de Mana: §b" + (int) spell.manaCost()));
            tooltip.add(Component.literal("§8[Clique com o Botão Direito para estudar este tomo]"));
        } else {
            tooltip.add(Component.literal("§7Um tomo antigo com runas indecifráveis."));
        }
    }
}
