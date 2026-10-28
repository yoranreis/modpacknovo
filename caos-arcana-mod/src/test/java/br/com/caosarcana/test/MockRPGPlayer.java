package br.com.caosarcana.test;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockRPGPlayer implements IRPGPlayerData {
    private Race race = Race.NONE;
    private boolean chosenRace = false;
    private float mana = 100.0f;
    private float baseMaxMana = 100.0f;
    private int generalLevel = 1;
    private final Map<String, Integer> schools = new HashMap<>();
    private final List<ItemStack> pendingDeliveries = new ArrayList<>();
    private final java.util.Set<String> knownSpells = new java.util.HashSet<>();
    private String selectedSpell = "";

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
        this.race = race != null ? race : Race.NONE;
        caosarcana$recalculateMaxMana();
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
        float max = caosarcana$getMaxMana();
        this.mana = Math.max(0.0f, Math.min(mana, max));
    }

    @Override
    public float caosarcana$getBaseMaxMana() {
        return baseMaxMana;
    }

    @Override
    public void caosarcana$setBaseMaxMana(float baseMax) {
        this.baseMaxMana = Math.max(1.0f, baseMax);
        caosarcana$recalculateMaxMana();
    }

    @Override
    public float caosarcana$getMaxMana() {
        return baseMaxMana + race.getManaBonus();
    }

    @Override
    public void caosarcana$recalculateMaxMana() {
        float max = caosarcana$getMaxMana();
        if (this.mana > max) {
            this.mana = max;
        }
    }

    @Override
    public boolean caosarcana$consumeMana(float amount) {
        if (amount <= 0.0f) return true;
        if (this.mana >= amount) {
            this.mana -= amount;
            return true;
        }
        return false;
    }

    @Override
    public void caosarcana$restoreMana(float amount) {
        if (amount <= 0.0f) return;
        float max = caosarcana$getMaxMana();
        this.mana = Math.min(max, this.mana + amount);
    }

    @Override
    public float caosarcana$getManaRegenPerSecond() {
        if (this.race == Race.ALTO_ELFO) return 3.0f;
        if (this.race == Race.ORC) return 1.5f;
        return 2.0f;
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

    @Override
    public boolean caosarcana$knowsSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        return knownSpells.contains(spellId.trim().toLowerCase());
    }

    @Override
    public boolean caosarcana$learnSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        String normalized = spellId.trim().toLowerCase();
        boolean added = knownSpells.add(normalized);
        if (added && (selectedSpell == null || selectedSpell.isEmpty())) {
            selectedSpell = normalized;
        }
        return added;
    }

    @Override
    public java.util.Set<String> caosarcana$getKnownSpells() {
        return java.util.Collections.unmodifiableSet(knownSpells);
    }

    @Override
    public String caosarcana$getSelectedSpell() {
        return selectedSpell != null ? selectedSpell : "";
    }

    @Override
    public void caosarcana$setSelectedSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) {
            this.selectedSpell = "";
            return;
        }
        String normalized = spellId.trim().toLowerCase();
        if (knownSpells.contains(normalized)) {
            this.selectedSpell = normalized;
        }
    }

    @Override
    public List<ItemStack> caosarcana$getPendingDeliveries() {
        return pendingDeliveries;
    }

    @Override
    public void caosarcana$addPendingDelivery(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            pendingDeliveries.add(stack.copy());
        }
    }

    @Override
    public void caosarcana$clearPendingDeliveries() {
        pendingDeliveries.clear();
    }
}
