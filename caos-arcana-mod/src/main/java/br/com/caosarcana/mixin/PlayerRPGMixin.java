package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        if (this.caosarcana$race == Race.ALTO_ELFO) {
            return 3.0f; // Bônus arcano de regeneração
        } else if (this.caosarcana$race == Race.ORC) {
            return 1.5f; // Foco físico, regeneração mais lenta
        }
        return 2.0f; // Taxa padrão (Felino e outros)
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
