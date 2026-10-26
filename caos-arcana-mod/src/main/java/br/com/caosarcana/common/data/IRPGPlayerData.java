package br.com.caosarcana.common.data;

import br.com.caosarcana.common.race.Race;

public interface IRPGPlayerData {
    Race caosarcana$getRace();
    void caosarcana$setRace(Race race);

    boolean caosarcana$hasChosenRace();
    void caosarcana$setChosenRace(boolean chosen);

    float caosarcana$getMana();
    void caosarcana$setMana(float mana);

    float caosarcana$getMaxMana();
    void caosarcana$setMaxMana(float maxMana);

    int caosarcana$getGeneralLevel();
    void caosarcana$setGeneralLevel(int level);

    int caosarcana$getSchoolLevel(String school);
    void caosarcana$setSchoolLevel(String school, int level);
}
