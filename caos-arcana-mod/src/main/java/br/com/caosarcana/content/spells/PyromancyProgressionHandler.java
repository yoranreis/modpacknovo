package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.talent.CaosArcanaTalentBridge;
import br.com.caosarcana.common.talent.TalentConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PyromancyProgressionHandler {
    public static final String SCHOOL_PYROMANCY = "piromancia";
    private static final Pattern HIT_TAG_PATTERN = Pattern.compile("^caosarcana:piro_hits_(\\d+)$");

    public static int getXpNeededForLevel(int level) {
        if (level < 1) level = 1;
        return TalentConfig.getPyromancyBaseXp() + (level - 1) * TalentConfig.getPyromancyXpPerLevel();
    }

    public static boolean isValidHostileTarget(Player player, Entity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player) {
            return false;
        }
        if (player != null && target.isAlliedTo(player)) {
            return false;
        }
        if (target instanceof TamableAnimal tamed && tamed.isTame()) {
            return false;
        }
        for (String tag : target.getTags()) {
            if (tag.equals("caosarcana:spectral_familiar") || tag.startsWith("caosarcana:summon_")) {
                return false;
            }
        }
        // Deve ser criatura hostil (Enemy ou mob hostil com alvo)
        if (target instanceof Enemy) {
            return true;
        }
        if (target instanceof Mob mob && mob.getTarget() != null) {
            return true;
        }
        return false;
    }

    public static int parseHitsFromTags(Iterable<String> tags) {
        if (tags == null) return 0;
        for (String tag : tags) {
            Matcher m = HIT_TAG_PATTERN.matcher(tag);
            if (m.matches()) {
                try {
                    return Integer.parseInt(m.group(1));
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    public static int getTargetHits(Entity target) {
        if (target == null) return 0;
        return parseHitsFromTags(target.getTags());
    }

    public static void setTargetHits(Entity target, int hits) {
        if (target == null) return;
        String toRemove = null;
        for (String tag : target.getTags()) {
            if (HIT_TAG_PATTERN.matcher(tag).matches()) {
                toRemove = tag;
                break;
            }
        }
        if (toRemove != null) {
            target.removeTag(toRemove);
        }
        target.addTag("caosarcana:piro_hits_" + hits);
    }

    /** Valida o alvo vivo antes do dano; recompensa somente impactos aceitos, inclusive letais. */
    public static boolean applyPyromancyDamage(ServerPlayer player, Entity target,
            net.minecraft.world.damagesource.DamageSource source, float damage) {
        boolean eligible = isValidHostileTarget(player, target);
        boolean hurt = target.hurt(source, damage);
        if (hurt && eligible && player instanceof IRPGPlayerData rpgPlayer
                && Float.isFinite(damage) && damage > 0) {
            awardHit(rpgPlayer, target);
        }
        return hurt;
    }

    private static void awardHit(IRPGPlayerData rpgPlayer, Entity target) {
        int maxHits = TalentConfig.getMaxHitsPerTarget();
        int currentHits = getTargetHits(target);
        if (currentHits >= maxHits) {
            return;
        }

        setTargetHits(target, currentHits + 1);

        int xpGained = TalentConfig.getPyromancyXpPerHit();
        addPyromancyXp(rpgPlayer, xpGained);
    }

    public static void addPyromancyXp(IRPGPlayerData rpgPlayer, int xpGained) {
        if (rpgPlayer == null) {
            return;
        }
        int currentLevel = rpgPlayer.caosarcana$getSchoolLevel(SCHOOL_PYROMANCY);
        if (currentLevel >= 100) {
            return;
        }

        int currentXp = rpgPlayer.caosarcana$getSchoolXp(SCHOOL_PYROMANCY);
        currentXp += xpGained;

        boolean leveledUp = false;
        while (currentLevel < 100) {
            int needed = getXpNeededForLevel(currentLevel);
            if (currentXp >= needed) {
                currentXp -= needed;
                currentLevel++;
                leveledUp = true;
            } else {
                break;
            }
        }

        rpgPlayer.caosarcana$setSchoolLevel(SCHOOL_PYROMANCY, currentLevel);
        rpgPlayer.caosarcana$setSchoolXp(SCHOOL_PYROMANCY, currentXp);

        if (rpgPlayer instanceof ServerPlayer serverPlayer) {
            if (leveledUp) {
                final int finalLevel = currentLevel;
                serverPlayer.displayClientMessage(
                        Component.literal("§c[Caos Arcana] Seu domínio sobre a Piromancia aumentou para o Nível " + finalLevel + "!"),
                        false
                );
                checkAndGrantMilestones(serverPlayer);
            }
            ModMessages.syncPlayerData(serverPlayer);
        }
    }

    public static void checkAndGrantMilestones(ServerPlayer player) {
        if (!(player instanceof IRPGPlayerData rpgPlayer)) {
            return;
        }
        checkAndGrantMilestones(player, rpgPlayer);
    }

    public static void checkAndGrantMilestones(ServerPlayer player, IRPGPlayerData rpgPlayer) {
        int currentLevel = rpgPlayer.caosarcana$getSchoolLevel(SCHOOL_PYROMANCY);

        int m1Level = TalentConfig.getMilestone1Level();
        int m1Points = TalentConfig.getMilestone1Points();
        checkSingleMilestone(player, rpgPlayer, "piromancia_" + m1Level, currentLevel, m1Level, m1Points);

        int m2Level = TalentConfig.getMilestone2Level();
        int m2Points = TalentConfig.getMilestone2Points();
        checkSingleMilestone(player, rpgPlayer, "piromancia_" + m2Level, currentLevel, m2Level, m2Points);
    }

    public static boolean checkSingleMilestone(ServerPlayer player, IRPGPlayerData rpgPlayer, String milestoneKey, int currentLevel, int reqLevel, int points) {
        if (currentLevel >= reqLevel && !rpgPlayer.caosarcana$hasClaimedMilestone(milestoneKey)) {
            boolean awarded = false;
            if (player != null) {
                awarded = CaosArcanaTalentBridge.addPoints(player, points);
            }
            if (awarded) {
                rpgPlayer.caosarcana$claimMilestone(milestoneKey);
                player.displayClientMessage(
                        Component.literal("§6[Caos Arcana] Você alcançou o marco de Piromancia (Nível " + reqLevel + ")! +" + points + " ponto(s) de talento concedido(s)."),
                        false
                );
                return true;
            }
        }
        return false;
    }
}
