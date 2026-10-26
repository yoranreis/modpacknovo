package br.com.caosarcana.client;

import br.com.caosarcana.common.race.Race;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ClientRPGData {
    private static Race currentRace = Race.NONE;
    private static boolean raceChosen = false;
    private static float mana = 100.0f;
    private static float maxMana = 100.0f;
    private static int generalLevel = 1;

    public static void update(String raceId, boolean chosen, float currentMana, float maximumMana, int level) {
        currentRace = Race.fromId(raceId);
        raceChosen = chosen;
        mana = currentMana;
        maxMana = maximumMana;
        generalLevel = level;
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
}
