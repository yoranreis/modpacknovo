package br.com.caosarcana.common.equipment;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class EquipmentRequirement {
    private final ResourceLocation itemId;
    private final RequirementType type;
    private final Set<Race> allowedRaces;
    private final int minGeneralLevel;
    private final String schoolName;
    private final int minSchoolLevel;
    private final String customDenialReason;

    public EquipmentRequirement(ResourceLocation itemId, RequirementType type, Set<Race> allowedRaces,
                                int minGeneralLevel, String schoolName, int minSchoolLevel,
                                String customDenialReason) {
        this.itemId = itemId;
        this.type = type;
        this.allowedRaces = allowedRaces != null ? Collections.unmodifiableSet(allowedRaces) : Collections.emptySet();
        this.minGeneralLevel = Math.max(1, minGeneralLevel);
        this.schoolName = (schoolName != null && !schoolName.trim().isEmpty()) ? schoolName.trim().toLowerCase() : null;
        this.minSchoolLevel = Math.max(0, minSchoolLevel);
        this.customDenialReason = customDenialReason;
    }

    public ResourceLocation getItemId() {
        return itemId;
    }

    public RequirementType getType() {
        return type;
    }

    public Set<Race> getAllowedRaces() {
        return allowedRaces;
    }

    public int getMinGeneralLevel() {
        return minGeneralLevel;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public int getMinSchoolLevel() {
        return minSchoolLevel;
    }

    public String getCustomDenialReason() {
        return customDenialReason;
    }

    public RequirementResult check(Player player, ItemStack stack) {
        if (player instanceof IRPGPlayerData rpgPlayer) {
            return check(rpgPlayer, stack);
        }
        return RequirementResult.allow();
    }

    public RequirementResult check(IRPGPlayerData rpgPlayer, ItemStack stack) {
        if (rpgPlayer == null) {
            return RequirementResult.allow();
        }

        Race playerRace = rpgPlayer.caosarcana$getRace();
        int playerLevel = rpgPlayer.caosarcana$getGeneralLevel();

        // 1. Verificacao de Raca
        if (!allowedRaces.isEmpty() && !allowedRaces.contains(playerRace)) {
            String allowedNames = allowedRaces.stream().map(Race::getDisplayName).collect(Collectors.joining(" ou "));
            String msg = (customDenialReason != null && !customDenialReason.isEmpty())
                    ? customDenialReason
                    : "§c[Restrição de " + type.getDisplayName() + "] Item exclusivo para a raça: §e" + allowedNames + "§c.";
            return RequirementResult.deny(Component.literal(msg));
        }

        // 2. Verificacao de Nivel Geral
        if (playerLevel < minGeneralLevel) {
            String msg = (customDenialReason != null && !customDenialReason.isEmpty())
                    ? customDenialReason
                    : "§c[Restrição de " + type.getDisplayName() + "] Requer Nível Geral §e" + minGeneralLevel + "§c (atual: " + playerLevel + ").";
            return RequirementResult.deny(Component.literal(msg));
        }

        // 3. Verificacao de Proficiencia / Escola de Magia (quando aplicavel)
        if (schoolName != null && minSchoolLevel > 0) {
            int playerSchoolLevel = rpgPlayer.caosarcana$getSchoolLevel(schoolName);
            if (playerSchoolLevel < minSchoolLevel) {
                String msg = (customDenialReason != null && !customDenialReason.isEmpty())
                        ? customDenialReason
                        : "§c[Restrição de " + type.getDisplayName() + "] Requer proficiência em §e" + schoolName + " nível " + minSchoolLevel + "§c (atual: " + playerSchoolLevel + ").";
                return RequirementResult.deny(Component.literal(msg));
            }
        }

        return RequirementResult.allow();
    }
}
