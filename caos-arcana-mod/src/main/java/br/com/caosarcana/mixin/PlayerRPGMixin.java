package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(Player.class)
public class PlayerRPGMixin implements IRPGPlayerData {
    @Unique
    private Race caosarcana$race = Race.NONE;
    @Unique
    private boolean caosarcana$hasChosenRace = false;
    @Unique
    private float caosarcana$mana = 100.0f;
    @Unique
    private float caosarcana$baseMaxMana = 100.0f;
    @Unique
    private int caosarcana$generalLevel = 1;
    @Unique
    private final Map<String, Integer> caosarcana$schools = new HashMap<>();
    @Unique
    private final List<ItemStack> caosarcana$pendingDeliveries = new ArrayList<>();
    @Unique
    private final Set<String> caosarcana$knownSpells = new HashSet<>();
    @Unique
    private String caosarcana$selectedSpell = "";
    @Unique
    private final Map<String, Long> caosarcana$spellCooldowns = new HashMap<>();
    @Unique
    private UUID caosarcana$activeFamiliarId = null;

    @Override
    public Race caosarcana$getRace() {
        return caosarcana$race;
    }

    @Override
    public void caosarcana$setRace(Race race) {
        this.caosarcana$race = race != null ? race : Race.NONE;
        caosarcana$recalculateMaxMana();
    }

    @Override
    public boolean caosarcana$hasChosenRace() {
        return caosarcana$hasChosenRace;
    }

    @Override
    public void caosarcana$setChosenRace(boolean chosen) {
        this.caosarcana$hasChosenRace = chosen;
    }

    @Override
    public float caosarcana$getMana() {
        return caosarcana$mana;
    }

    @Override
    public void caosarcana$setMana(float mana) {
        float max = caosarcana$getMaxMana();
        this.caosarcana$mana = Math.max(0.0f, Math.min(mana, max));
    }

    @Override
    public float caosarcana$getBaseMaxMana() {
        return caosarcana$baseMaxMana;
    }

    @Override
    public void caosarcana$setBaseMaxMana(float baseMax) {
        this.caosarcana$baseMaxMana = Math.max(1.0f, baseMax);
        caosarcana$recalculateMaxMana();
    }

    @Override
    public float caosarcana$getMaxMana() {
        return this.caosarcana$baseMaxMana + this.caosarcana$race.getManaBonus();
    }

    @Override
    public void caosarcana$recalculateMaxMana() {
        float effectiveMax = caosarcana$getMaxMana();
        // Clamping anti-exploit: se o maximo diminuiu (ex: perdeu o bonus racial de elfo),
        // a mana atual e cortada para o novo teto sem recuperar vida ou mana gratis.
        if (this.caosarcana$mana > effectiveMax) {
            this.caosarcana$mana = effectiveMax;
        }
    }

    @Override
    public boolean caosarcana$consumeMana(float amount) {
        if (amount <= 0.0f) return true;
        if (this.caosarcana$mana >= amount) {
            this.caosarcana$mana -= amount;
            return true;
        }
        return false;
    }

    @Override
    public void caosarcana$restoreMana(float amount) {
        if (amount <= 0.0f) return;
        float max = caosarcana$getMaxMana();
        this.caosarcana$mana = Math.min(max, this.caosarcana$mana + amount);
    }

    @Override
    public float caosarcana$getManaRegenPerSecond() {
        return this.caosarcana$race != null ? this.caosarcana$race.getManaRegenPerSecond() : 2.0f;
    }

    @Override
    public int caosarcana$getGeneralLevel() {
        return caosarcana$generalLevel;
    }

    @Override
    public void caosarcana$setGeneralLevel(int level) {
        this.caosarcana$generalLevel = Math.max(1, level);
    }

    @Override
    public int caosarcana$getSchoolLevel(String school) {
        return caosarcana$schools.getOrDefault(school.toLowerCase(), 1);
    }

    @Override
    public void caosarcana$setSchoolLevel(String school, int level) {
        caosarcana$schools.put(school.toLowerCase(), Math.max(1, Math.min(100, level)));
    }

    @Override
    public boolean caosarcana$knowsSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        return caosarcana$knownSpells.contains(spellId.trim().toLowerCase());
    }

    @Override
    public boolean caosarcana$learnSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        String normalized = spellId.trim().toLowerCase();
        boolean added = caosarcana$knownSpells.add(normalized);
        if (added && (caosarcana$selectedSpell == null || caosarcana$selectedSpell.isEmpty())) {
            caosarcana$selectedSpell = normalized;
        }
        return added;
    }

    @Override
    public Set<String> caosarcana$getKnownSpells() {
        return Collections.unmodifiableSet(caosarcana$knownSpells);
    }

    @Override
    public String caosarcana$getSelectedSpell() {
        return caosarcana$selectedSpell != null ? caosarcana$selectedSpell : "";
    }

    @Override
    public void caosarcana$setSelectedSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) {
            this.caosarcana$selectedSpell = "";
            return;
        }
        String normalized = spellId.trim().toLowerCase();
        if (caosarcana$knownSpells.contains(normalized)) {
            this.caosarcana$selectedSpell = normalized;
        }
    }

    @Override
    public boolean caosarcana$isSpellOnCooldown(String spellId, long currentTick) {
        if (spellId == null || spellId.isEmpty()) return false;
        Long ready = caosarcana$spellCooldowns.get(spellId.toLowerCase());
        return ready != null && currentTick < ready;
    }

    @Override
    public void caosarcana$setSpellCooldown(String spellId, long readyTick) {
        if (spellId != null && !spellId.isEmpty()) {
            caosarcana$spellCooldowns.put(spellId.toLowerCase(), readyTick);
        }
    }

    @Override
    public long caosarcana$getSpellReadyTick(String spellId) {
        if (spellId == null) return 0L;
        return caosarcana$spellCooldowns.getOrDefault(spellId.toLowerCase(), 0L);
    }

    @Override
    public UUID caosarcana$getActiveFamiliarId() {
        return caosarcana$activeFamiliarId;
    }

    @Override
    public void caosarcana$setActiveFamiliarId(UUID id) {
        this.caosarcana$activeFamiliarId = id;
    }

    @Override
    public List<ItemStack> caosarcana$getPendingDeliveries() {
        return caosarcana$pendingDeliveries;
    }

    @Override
    public void caosarcana$addPendingDelivery(ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            caosarcana$pendingDeliveries.add(stack.copy());
        }
    }

    @Override
    public void caosarcana$clearPendingDeliveries() {
        caosarcana$pendingDeliveries.clear();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$saveNbt(CompoundTag compound, CallbackInfo ci) {
        CompoundTag rpgTag = new CompoundTag();
        rpgTag.putString("Race", caosarcana$race.getId());
        rpgTag.putBoolean("RaceChosen", caosarcana$hasChosenRace);
        rpgTag.putFloat("Mana", caosarcana$mana);
        rpgTag.putFloat("BaseMaxMana", caosarcana$baseMaxMana);
        rpgTag.putInt("GeneralLevel", caosarcana$generalLevel);

        CompoundTag schoolsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : caosarcana$schools.entrySet()) {
            schoolsTag.putInt(entry.getKey(), entry.getValue());
        }
        rpgTag.put("Schools", schoolsTag);

        // Persistência segura das magias aprendidas permanentemente
        ListTag spellsList = new ListTag();
        for (String spell : caosarcana$knownSpells) {
            spellsList.add(StringTag.valueOf(spell));
        }
        rpgTag.put("KnownSpells", spellsList);
        rpgTag.putString("SelectedSpell", caosarcana$selectedSpell != null ? caosarcana$selectedSpell : "");

        // Persistência de recargas de magias
        CompoundTag cooldownsTag = new CompoundTag();
        for (Map.Entry<String, Long> entry : caosarcana$spellCooldowns.entrySet()) {
            cooldownsTag.putLong(entry.getKey(), entry.getValue());
        }
        rpgTag.put("SpellCooldowns", cooldownsTag);

        // Identificação persistente do Familiar Espectral ativo
        if (caosarcana$activeFamiliarId != null) {
            rpgTag.putUUID("ActiveFamiliarUUID", caosarcana$activeFamiliarId);
        }

        // Persistência segura da fila de devoluções pendentes
        ListTag pendingList = new ListTag();
        for (ItemStack stack : caosarcana$pendingDeliveries) {
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(itemTag);
                pendingList.add(itemTag);
            }
        }
        rpgTag.put("PendingDeliveries", pendingList);

        compound.put("CaosArcanaData", rpgTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$readNbt(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("CaosArcanaData", 10)) {
            CompoundTag rpgTag = compound.getCompound("CaosArcanaData");
            this.caosarcana$race = Race.fromId(rpgTag.getString("Race"));
            this.caosarcana$hasChosenRace = rpgTag.getBoolean("RaceChosen");
            this.caosarcana$mana = rpgTag.getFloat("Mana");

            if (rpgTag.contains("BaseMaxMana")) {
                this.caosarcana$baseMaxMana = Math.max(1.0f, rpgTag.getFloat("BaseMaxMana"));
            } else if (rpgTag.contains("MaxMana")) {
                this.caosarcana$baseMaxMana = Math.max(1.0f, rpgTag.getFloat("MaxMana"));
            }
            caosarcana$recalculateMaxMana();

            this.caosarcana$generalLevel = Math.max(1, rpgTag.getInt("GeneralLevel"));

            caosarcana$schools.clear();
            if (rpgTag.contains("Schools", 10)) {
                CompoundTag schoolsTag = rpgTag.getCompound("Schools");
                for (String key : schoolsTag.getAllKeys()) {
                    caosarcana$schools.put(key, schoolsTag.getInt(key));
                }
            }

            caosarcana$knownSpells.clear();
            if (rpgTag.contains("KnownSpells", 9)) {
                ListTag spellsList = rpgTag.getList("KnownSpells", 8);
                for (int i = 0; i < spellsList.size(); i++) {
                    caosarcana$knownSpells.add(spellsList.getString(i));
                }
            }

            if (rpgTag.contains("SelectedSpell", 8)) {
                String selected = rpgTag.getString("SelectedSpell");
                if (caosarcana$knownSpells.contains(selected.trim().toLowerCase())) {
                    this.caosarcana$selectedSpell = selected.trim().toLowerCase();
                } else {
                    this.caosarcana$selectedSpell = "";
                }
            } else {
                this.caosarcana$selectedSpell = "";
            }

            caosarcana$spellCooldowns.clear();
            if (rpgTag.contains("SpellCooldowns", 10)) {
                CompoundTag cooldownsTag = rpgTag.getCompound("SpellCooldowns");
                for (String key : cooldownsTag.getAllKeys()) {
                    this.caosarcana$spellCooldowns.put(key, cooldownsTag.getLong(key));
                }
            }

            if (rpgTag.hasUUID("ActiveFamiliarUUID")) {
                this.caosarcana$activeFamiliarId = rpgTag.getUUID("ActiveFamiliarUUID");
            } else {
                this.caosarcana$activeFamiliarId = null;
            }

            caosarcana$pendingDeliveries.clear();
            if (rpgTag.contains("PendingDeliveries", 9)) {
                ListTag pendingList = rpgTag.getList("PendingDeliveries", 10);
                for (int i = 0; i < pendingList.size(); i++) {
                    CompoundTag itemTag = pendingList.getCompound(i);
                    ItemStack stack = ItemStack.of(itemTag);
                    if (!stack.isEmpty()) {
                        caosarcana$pendingDeliveries.add(stack);
                    }
                }
            }
        }
    }
}
