package br.com.caosarcana.client;

import br.com.caosarcana.common.race.Race;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ClientRPGData {
    private static Race currentRace = Race.NONE;
    private static boolean raceChosen = false;
    private static float mana = 100.0f;
    private static float maxMana = 100.0f;
    private static int generalLevel = 1;
    private static final Set<String> knownSpells = new HashSet<>();
    private static String selectedSpell = "";

    public static void update(String raceId, boolean chosen, float currentMana, float maximumMana, int level, Set<String> spells, String activeSpell) {
        currentRace = Race.fromId(raceId);
        raceChosen = chosen;
        mana = currentMana;
        maxMana = maximumMana;
        generalLevel = level;
        knownSpells.clear();
        if (spells != null) {
            for (String s : spells) {
                knownSpells.add(s.trim().toLowerCase());
            }
        }
        selectedSpell = (activeSpell != null) ? activeSpell.trim().toLowerCase() : "";
    }

    public static Race getCurrentRace() {
        return currentRace;
    }

    public static boolean hasChosenRace() {
        return raceChosen;
    }

    public static float getMana() {
        return mana;
    }

    public static float getMaxMana() {
        return maxMana;
    }

    public static int getGeneralLevel() {
        return generalLevel;
    }

    public static boolean knowsSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        return knownSpells.contains(spellId.trim().toLowerCase());
    }

    public static Set<String> getKnownSpells() {
        return Collections.unmodifiableSet(knownSpells);
    }

    public static String getSelectedSpell() {
        return selectedSpell != null ? selectedSpell : "";
    }

    public static void setSelectedSpell(String spellId) {
        selectedSpell = (spellId != null) ? spellId.trim().toLowerCase() : "";
    }
}
