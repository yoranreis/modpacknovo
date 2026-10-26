package br.com.caosarcana.test;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;

import java.util.HashMap;
import java.util.Map;

public class MockRPGPlayer implements IRPGPlayerData {
    private Race race = Race.NONE;
    private boolean chosenRace = false;
    private float mana = 100.0f;
    private float maxMana = 100.0f;
    private int generalLevel = 1;
    private final Map<String, Integer> schools = new HashMap<>();

    public MockRPGPlayer(Race race, int generalLevel) {
        this.race = race;
        this.chosenRace = (race != Race.NONE);
        this.generalLevel = generalLevel;
    }

    @Override
    public Race caosarcana$getRace() {
        return race;
    }

    @Override
    public void caosarcana$setRace(Race race) {
        this.race = race;
    }

    @Override
    public boolean caosarcana$hasChosenRace() {
        return chosenRace;
    }

    @Override
    public void caosarcana$setChosenRace(boolean chosen) {
        this.chosenRace = chosen;
    }

    @Override
    public float caosarcana$getMana() {
        return mana;
    }

    @Override
    public void caosarcana$setMana(float mana) {
        this.mana = mana;
    }

    @Override
    public float caosarcana$getMaxMana() {
        return maxMana;
    }

    @Override
    public void caosarcana$setMaxMana(float maxMana) {
        this.maxMana = maxMana;
    }

    @Override
    public int caosarcana$getGeneralLevel() {
        return generalLevel;
    }

    @Override
    public void caosarcana$setGeneralLevel(int level) {
        this.generalLevel = level;
    }

    @Override
    public int caosarcana$getSchoolLevel(String school) {
        return schools.getOrDefault(school.toLowerCase(), 1);
    }

    @Override
    public void caosarcana$setSchoolLevel(String school, int level) {
        schools.put(school.toLowerCase(), level);
    }
}
