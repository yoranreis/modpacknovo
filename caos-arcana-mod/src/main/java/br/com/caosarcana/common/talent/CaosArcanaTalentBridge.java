package br.com.caosarcana.common.talent;

import br.com.caosarcana.common.data.IRPGPlayerData;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.Optional;

public class CaosArcanaTalentBridge {
    public static final String MOD_ID_SKILLS = "puffish_skills";
    public static final ResourceLocation CATEGORY_PIROMANCIA = new ResourceLocation("caosarcana", "piromancia");
    public static final String TALENT_RAIZ_AFINIDADE = "caosarcana:piro_raiz_afinidade";
    public static final String TALENT_FOCO_COMBUSTAO = "caosarcana:piro_foco_combustao";
    public static final String TALENT_FOCO_CINETICO = "caosarcana:piro_foco_cinetico";

    public static boolean isSkillsModLoaded() {
        return FabricLoader.getInstance().isModLoaded(MOD_ID_SKILLS);
    }

    public static boolean canUnlock(Object rawPlayer, ResourceLocation categoryId, String skillId) {
        if (!(rawPlayer instanceof IRPGPlayerData rpgPlayer)) {
            return true;
        }

        // A ponte de Piromancia não interfere em nenhuma outra categoria.
        if (!CATEGORY_PIROMANCIA.equals(categoryId)) {
            return true;
        }

        if (TALENT_RAIZ_AFINIDADE.equals(skillId)) {
            if (!rpgPlayer.caosarcana$getKnownSpells().contains("caosarcana:centelha_ignea")) {
                if (rawPlayer instanceof Player player) {
                    player.displayClientMessage(
                            Component.literal("§c[Caos Arcana] Você precisa aprender a Centelha Ígnea em um Grimório antes de despertar este talento!"),
                            true
                    );
                }
                return false;
            }
        } else if (TALENT_FOCO_COMBUSTAO.equals(skillId)) {
            if (rawPlayer instanceof Player player) {
                if (!hasRootUnlocked(player)) {
                    player.displayClientMessage(
                            Component.literal("§c[Caos Arcana] Requer o talento Afinidade com as Chamas."),
                            true
                    );
                    return false;
                }
                if (hasKineticFocus(player)) {
                    player.displayClientMessage(
                            Component.literal("§c[Caos Arcana] Foco Cinético já escolhido. As especializações são mutuamente exclusivas!"),
                            true
                    );
                    return false;
                }
            } else {
                return false;
            }
        } else if (TALENT_FOCO_CINETICO.equals(skillId)) {
            if (rawPlayer instanceof Player player) {
                if (!hasRootUnlocked(player)) {
                    player.displayClientMessage(
                            Component.literal("§c[Caos Arcana] Requer o talento Afinidade com as Chamas."),
                            true
                    );
                    return false;
                }
                if (hasCombustionFocus(player)) {
                    player.displayClientMessage(
                            Component.literal("§c[Caos Arcana] Foco em Combustão já escolhido. As especializações são mutuamente exclusivas!"),
                            true
                    );
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    public static boolean hasTalent(Player player, String skillId) {
        if (!isSkillsModLoaded() || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        return PufferfishSkillsAccess.hasUnlockedSkill(serverPlayer, skillId);
    }

    public static boolean hasRootUnlocked(Player player) {
        return hasTalent(player, TALENT_RAIZ_AFINIDADE);
    }

    public static boolean hasCombustionFocus(Player player) {
        return hasTalent(player, TALENT_FOCO_COMBUSTAO);
    }

    public static boolean hasKineticFocus(Player player) {
        return hasTalent(player, TALENT_FOCO_CINETICO);
    }

    public static boolean addPoints(ServerPlayer player, int points) {
        if (!isSkillsModLoaded()) {
            return false;
        }
        return PufferfishSkillsAccess.addPoints(player, points);
    }

    public static boolean resetTalents(ServerPlayer player) {
        if (!isSkillsModLoaded()) {
            return false;
        }
        return PufferfishSkillsAccess.resetSkills(player);
    }

    public static boolean openScreen(ServerPlayer player) {
        if (!isSkillsModLoaded()) {
            return false;
        }
        return PufferfishSkillsAccess.openScreen(player);
    }

    /**
     * Classe interna isolada para evitar NoClassDefFoundError caso o mod externo não esteja instalado.
     */
    private static class PufferfishSkillsAccess {
        static boolean hasUnlockedSkill(ServerPlayer player, String skillId) {
            try {
                var skillsMod = net.puffish.skillsmod.SkillsMod.getInstance();
                if (skillsMod == null) return false;
                // Identifier no Yarn/Mojmap
                net.minecraft.resources.ResourceLocation catId = CATEGORY_PIROMANCIA;
                Optional<Collection<String>> skillsOpt = skillsMod.getUnlockedSkills(
                        (net.minecraft.server.level.ServerPlayer) player,
                        (net.minecraft.resources.ResourceLocation) catId
                );
                return skillsOpt.map(c -> c.contains(skillId)).orElse(false);
            } catch (Throwable t) {
                return false;
            }
        }

        static boolean addPoints(ServerPlayer player, int points) {
            try {
                var skillsMod = net.puffish.skillsmod.SkillsMod.getInstance();
                if (skillsMod == null) return false;
                skillsMod.addPoints(player, CATEGORY_PIROMANCIA, net.puffish.skillsmod.util.PointSources.COMMANDS, points, true);
                return true;
            } catch (Throwable t) {
                return false;
            }
        }

        static boolean resetSkills(ServerPlayer player) {
            try {
                var skillsMod = net.puffish.skillsmod.SkillsMod.getInstance();
                if (skillsMod == null) return false;
                skillsMod.resetSkills(player, CATEGORY_PIROMANCIA);
                return true;
            } catch (Throwable t) {
                return false;
            }
        }

        static boolean openScreen(ServerPlayer player) {
            try {
                var skillsMod = net.puffish.skillsmod.SkillsMod.getInstance();
                if (skillsMod == null) return false;
                skillsMod.openScreen(player, Optional.of(CATEGORY_PIROMANCIA));
                return true;
            } catch (Throwable t) {
                return false;
            }
        }
    }
}
