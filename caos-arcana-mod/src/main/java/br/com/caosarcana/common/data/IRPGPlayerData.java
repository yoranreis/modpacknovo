package br.com.caosarcana.common.data;

import br.com.caosarcana.common.race.Race;
import net.minecraft.world.item.ItemStack;
import java.util.List;

public interface IRPGPlayerData {
    Race caosarcana$getRace();
    void caosarcana$setRace(Race race);

    boolean caosarcana$hasChosenRace();
    void caosarcana$setChosenRace(boolean chosen);

    float caosarcana$getMana();
    void caosarcana$setMana(float mana);

    float caosarcana$getBaseMaxMana();
    void caosarcana$setBaseMaxMana(float baseMax);

    float caosarcana$getMaxMana();
    void caosarcana$recalculateMaxMana();

    boolean caosarcana$consumeMana(float amount);
    void caosarcana$restoreMana(float amount);
    float caosarcana$getManaRegenPerSecond();

    int caosarcana$getGeneralLevel();
    void caosarcana$setGeneralLevel(int level);

    int caosarcana$getSchoolLevel(String school);
    void caosarcana$setSchoolLevel(String school, int level);

    List<ItemStack> caosarcana$getPendingDeliveries();
    void caosarcana$addPendingDelivery(ItemStack stack);
    void caosarcana$clearPendingDeliveries();
}
