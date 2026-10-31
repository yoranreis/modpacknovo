package br.com.caosarcana.mixin;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.EquipmentValidationHandler;
import br.com.caosarcana.common.equipment.RequirementResult;
import br.com.caosarcana.common.race.Race;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
    private final Map<String, Integer> caosarcana$schoolsXp = new HashMap<>();
    @Unique
    private final Set<String> caosarcana$claimedMilestones = new HashSet<>();
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
    public int caosarcana$getSchoolXp(String school) {
        if (school == null) return 0;
        return caosarcana$schoolsXp.getOrDefault(school.toLowerCase(), 0);
    }

    @Override
    public void caosarcana$setSchoolXp(String school, int xp) {
        if (school == null) return;
        caosarcana$schoolsXp.put(school.toLowerCase(), Math.max(0, xp));
    }

    @Override
    public Set<String> caosarcana$getClaimedMilestones() {
        return Collections.unmodifiableSet(caosarcana$claimedMilestones);
    }

    @Override
    public boolean caosarcana$hasClaimedMilestone(String milestoneId) {
        if (milestoneId == null) return false;
        return caosarcana$claimedMilestones.contains(milestoneId.toLowerCase());
    }

    @Override
    public void caosarcana$claimMilestone(String milestoneId) {
        if (milestoneId == null) return;
        caosarcana$claimedMilestones.add(milestoneId.toLowerCase());
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
    public boolean caosarcana$unlearnSpell(String spellId) {
        if (spellId == null || spellId.trim().isEmpty()) return false;
        String normalized = spellId.trim().toLowerCase();
        boolean removed = caosarcana$knownSpells.remove(normalized);
        if (removed && normalized.equalsIgnoreCase(caosarcana$selectedSpell)) {
            caosarcana$selectedSpell = "";
        }
        return removed;
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

    @Unique
    private static boolean caosarcana$isNetherNativeEntity(Entity entity) {
        if (entity == null) return false;
        return entity instanceof Blaze ||
               entity instanceof Ghast ||
               entity instanceof MagmaCube ||
               entity instanceof WitherSkeleton ||
               entity instanceof AbstractPiglin ||
               entity instanceof ZombifiedPiglin ||
               entity instanceof Hoglin;
    }

    /**
     * Imunidade Ambiental Permanente e Neutralidade Absoluta com Mobs do Nether
     * Exclusiva da raça Sangue-Daedra:
     * - Imunidade total permanente a fogo, lava, chamas e magma (DamageTypeTags.IS_FIRE).
     * - Imunidade total a qualquer ataque ou dano de contato físico originado por mobs nativos do Nether.
     * - Danos físicos de outras fontes (esqueletos do overworld, zumbis, aranhas, creepers, PvP e quedas) continuam normais.
     * - Não concedida ao Voidmer ou outras raças.
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void caosarcana$onPlayerHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (caosarcana$hasChosenRace && caosarcana$race == Race.SANGUE_DAEDRA) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                cir.setReturnValue(false);
                return;
            }
            Entity attacker = source.getEntity();
            Entity direct = source.getDirectEntity();
            if (caosarcana$isNetherNativeEntity(attacker) || caosarcana$isNetherNativeEntity(direct)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void caosarcana$isInvulnerableTo(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (caosarcana$hasChosenRace && caosarcana$race == Race.SANGUE_DAEDRA) {
            if (source.is(DamageTypeTags.IS_FIRE)) {
                cir.setReturnValue(true);
            }
        }
    }

    @ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), argsOnly = true)
    private float caosarcana$reduceFireAndLavaForPlayer(float amount, DamageSource source) {
        if (caosarcana$hasChosenRace && source.is(DamageTypeTags.IS_FIRE)) {
            if (caosarcana$race == Race.SANGUE_DAEDRA) {
                return 0.0f; // Imunidade total permanente
            }
        }
        return amount;
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

        CompoundTag schoolsXpTag = new CompoundTag();
        for (Map.Entry<String, Integer> entry : caosarcana$schoolsXp.entrySet()) {
            schoolsXpTag.putInt(entry.getKey(), entry.getValue());
        }
        rpgTag.put("SchoolsXp", schoolsXpTag);

        ListTag milestonesList = new ListTag();
        for (String m : caosarcana$claimedMilestones) {
            milestonesList.add(StringTag.valueOf(m));
        }
        rpgTag.put("ClaimedMilestones", milestonesList);

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
            String rawRace = rpgTag.getString("Race");

            if (rawRace.equalsIgnoreCase("bretao") || rawRace.equalsIgnoreCase("elfo_negro")) {
                // Legados substituídos: oferecer nova seleção racial sem conversão compulsória
                this.caosarcana$race = Race.NONE;
                this.caosarcana$hasChosenRace = false;
            } else if (rawRace.equalsIgnoreCase("imperial")) {
                this.caosarcana$race = Race.HUMANO;
                this.caosarcana$hasChosenRace = rpgTag.getBoolean("RaceChosen");
            } else if (rawRace.equalsIgnoreCase("guarda_vermelho")) {
                this.caosarcana$race = Race.REDGUARD;
                this.caosarcana$hasChosenRace = rpgTag.getBoolean("RaceChosen");
            } else {
                this.caosarcana$race = Race.fromId(rawRace);
                this.caosarcana$hasChosenRace = rpgTag.getBoolean("RaceChosen");
            }

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

            caosarcana$schoolsXp.clear();
            if (rpgTag.contains("SchoolsXp", 10)) {
                CompoundTag schoolsXpTag = rpgTag.getCompound("SchoolsXp");
                for (String key : schoolsXpTag.getAllKeys()) {
                    caosarcana$schoolsXp.put(key, schoolsXpTag.getInt(key));
                }
            }

            caosarcana$claimedMilestones.clear();
            if (rpgTag.contains("ClaimedMilestones", 9)) {
                ListTag milestonesList = rpgTag.getList("ClaimedMilestones", 8);
                for (int i = 0; i < milestonesList.size(); i++) {
                    caosarcana$claimedMilestones.add(milestonesList.getString(i));
                }
            }

            caosarcana$knownSpells.clear();
            if (rpgTag.contains("KnownSpells", 9)) {
                ListTag spellsList = rpgTag.getList("KnownSpells", 8);
                for (int i = 0; i < spellsList.size(); i++) {
                    caosarcana$knownSpells.add(spellsList.getString(i));
                }
            }

            if (!this.caosarcana$hasChosenRace) {
                // Bloqueio de poderes antigos durante seleção pendente:
                // Remove magias raciais antigas da lista de conhecidas
                this.caosarcana$knownSpells.remove("caosarcana:ira_ancestral");
                this.caosarcana$knownSpells.remove("caosarcana:pele_de_dragao");
                this.caosarcana$knownSpells.remove("caosarcana:voz_do_imperador");
                this.caosarcana$knownSpells.remove("caosarcana:impeto_de_adrenalina");
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

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void caosarcana$onAttack(Entity target, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        ItemStack attackingWeapon = EquipmentValidationHandler.resolveAttackingWeapon(player);
        if (attackingWeapon != null && !attackingWeapon.isEmpty()) {
            RequirementResult result = EquipmentRequirementRegistry.canEquip(player, attackingWeapon);
            if (!result.isAllowed()) {
                ci.cancel();
                EquipmentValidationHandler.notifyAttackDenied(player, result);
            }
        }
    }
}
