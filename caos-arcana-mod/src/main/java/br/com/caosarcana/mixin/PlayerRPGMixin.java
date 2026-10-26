package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
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
    private float caosarcana$maxMana = 100.0f;
    @Unique
    private int caosarcana$generalLevel = 1;
    @Unique
    private final Map<String, Integer> caosarcana$schools = new HashMap<>();

    @Override
    public Race caosarcana$getRace() {
        return caosarcana$race;
    }

    @Override
    public void caosarcana$setRace(Race race) {
        this.caosarcana$race = race != null ? race : Race.NONE;
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
        this.caosarcana$mana = Math.max(0.0f, Math.min(mana, this.caosarcana$maxMana));
    }

    @Override
    public float caosarcana$getMaxMana() {
        return caosarcana$maxMana;
    }

    @Override
    public void caosarcana$setMaxMana(float maxMana) {
        this.caosarcana$maxMana = Math.max(1.0f, maxMana);
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

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$saveNbt(CompoundTag compound, CallbackInfo ci) {
        CompoundTag rpgTag = new CompoundTag();
        rpgTag.putString("Race", caosarcana$race.getId());
        rpgTag.putBoolean("RaceChosen", caosarcana$hasChosenRace);
        rpgTag.putFloat("Mana", caosarcana$mana);
        rpgTag.putFloat("MaxMana", caosarcana$maxMana);
        rpgTag.putInt("GeneralLevel", caosarcana$generalLevel);

        CompoundTag schoolsTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : caosarcana$schools.entrySet()) {
            schoolsTag.putInt(entry.getKey(), entry.getValue());
        }
        rpgTag.put("Schools", schoolsTag);

        compound.put("CaosArcanaData", rpgTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void caosarcana$readNbt(CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("CaosArcanaData", 10)) {
            CompoundTag rpgTag = compound.getCompound("CaosArcanaData");
            this.caosarcana$race = Race.fromId(rpgTag.getString("Race"));
            this.caosarcana$hasChosenRace = rpgTag.getBoolean("RaceChosen");
            this.caosarcana$mana = rpgTag.getFloat("Mana");
            this.caosarcana$maxMana = rpgTag.getFloat("MaxMana");
            if (this.caosarcana$maxMana <= 0) this.caosarcana$maxMana = 100.0f;
            this.caosarcana$generalLevel = Math.max(1, rpgTag.getInt("GeneralLevel"));

            caosarcana$schools.clear();
            if (rpgTag.contains("Schools", 10)) {
                CompoundTag schoolsTag = rpgTag.getCompound("Schools");
                for (String key : schoolsTag.getAllKeys()) {
                    caosarcana$schools.put(key, schoolsTag.getInt(key));
                }
            }
        }
    }
}
