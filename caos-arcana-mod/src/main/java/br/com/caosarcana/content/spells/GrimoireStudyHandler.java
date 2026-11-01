package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.content.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * Manipulador no servidor para validação e gravação permanente do estudo de grimórios.
 * Suporta dinamicamente qualquer grimório e magia registrada.
 */
public class GrimoireStudyHandler {

    public enum StudyResult {
        SUCCESS,
        NO_RACE,
        NOT_HOLDING_BOOK,
        REQUIREMENT_NOT_MET,
        ALREADY_KNOWN,
        UNKNOWN_SPELL
    }

    public static StudyResult handleLearn(ServerPlayer player, String rawSpellId) {
        if (!(player instanceof IRPGPlayerData rpgPlayer)) {
            return StudyResult.UNKNOWN_SPELL;
        }

        String spellId = (rawSpellId != null) ? rawSpellId.trim().toLowerCase() : "";

        // 1. Exige ter escolhido uma raça
        if (!rpgPlayer.caosarcana$hasChosenRace()) {
            player.displayClientMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de estudar grimórios!"), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return StudyResult.NO_RACE;
        }

        // 2. Validação no SpellRegistry
        Optional<SpellDefinition> opt = SpellRegistry.get(spellId);
        if (opt.isEmpty()) {
            player.displayClientMessage(Component.literal("§c[Caos Arcana] Magia inválida para estudo: " + rawSpellId), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return StudyResult.UNKNOWN_SPELL;
        }

        SpellDefinition spell = opt.get();

        // 3. Validação de restrição racial
        if (spell.isRacial() && !spell.canCast(rpgPlayer.caosarcana$getRace())) {
            player.displayClientMessage(Component.literal("§c[Grimório] As escrituras deste tomo são exclusivas da linhagem " + spell.requiredRace().getDisplayName() + "!"), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return StudyResult.REQUIREMENT_NOT_MET;
        }

        // 4. Validação de posse do grimório na mão principal ou secundária
        boolean holdingBook = isHoldingGrimoireFor(player, spellId);
        if (!holdingBook) {
            player.displayClientMessage(Component.literal("§c[Grimório] Você precisa estar segurando o grimório correspondente para aprender suas runas!"), true);
            player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
            return StudyResult.NOT_HOLDING_BOOK;
        }

        // 5. Validação do requisito de escola
        if (!spell.school().equalsIgnoreCase("racas")) {
            int schoolLevel = rpgPlayer.caosarcana$getSchoolLevel(spell.school());
            if (schoolLevel < spell.minSchoolLevel()) {
                player.displayClientMessage(
                        Component.literal("§c[Grimório] Requisito não atingido: Requer " + spell.schoolDisplayName() + " nível " + spell.minSchoolLevel() + " (Atual: " + schoolLevel + ")!"),
                        false
                );
                player.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                return StudyResult.REQUIREMENT_NOT_MET;
            }
        }

        // 6. Validação se já é conhecida (Idempotência sem duplicação)
        if (rpgPlayer.caosarcana$knowsSpell(spell.id())) {
            player.sendSystemMessage(Component.literal("§e[Grimório] Você revisou as escrituras arcanas, mas a magia §6" + spell.name() + "§e já está gravada em sua memória arcana."));
            player.playNotifySound(SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);
            return StudyResult.ALREADY_KNOWN;
        }

        // 7. Gravação permanente do conhecimento
        boolean learned = rpgPlayer.caosarcana$learnSpell(spell.id());
        if (learned) {
            ModMessages.syncPlayerData(player);

            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.2f);
            player.playNotifySound(SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0f, 1.0f);

            player.sendSystemMessage(Component.literal("§6[Caos Arcana] §aVocê decifrou as runas arcanas e aprendeu permanentemente: §e" + spell.name() + "§a!"));
            player.displayClientMessage(Component.literal("§aMagia Aprendida: §e" + spell.name() + " §7(Pressione [R] para selecionar e [V] para conjurar)"), true);

            return StudyResult.SUCCESS;
        }

        return StudyResult.UNKNOWN_SPELL;
    }

    private static boolean isHoldingGrimoireFor(ServerPlayer player, String spellId) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();

        return checkStack(main, spellId) || checkStack(off, spellId);
    }

    private static boolean checkStack(ItemStack stack, String spellId) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getItem() instanceof SpellGrimoireItem grimoire) {
            return grimoire.getSpellId().equalsIgnoreCase(spellId);
        }
        if (SpellCastHandler.CENTELHA_IGNEA_ID.equalsIgnoreCase(spellId) && stack.is(ModItems.CENTELHA_IGNEA)) {
            return true;
        }
        return false;
    }
}
