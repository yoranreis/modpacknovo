package br.com.caosarcana.common.talent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class TalentConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("caosarcana-talents");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Limites de validação dos talentos
    public static final float MIN_MANA_COST = 1.0f;
    public static final float MAX_MANA_COST = 20.0f; // Não pode custar mais que o feitiço base (20)

    public static final int MIN_FIRE_SECONDS = 5; // Não pode ser menor que a base vanilla do feitiço (5)
    public static final int MAX_FIRE_SECONDS = 60;

    public static final double MIN_KNOCKBACK = 0.1;
    public static final double MAX_KNOCKBACK = 5.0;

    // Limites de validação da progressão de maestria e marcos
    public static final int MIN_MILESTONE_LEVEL = 2;
    public static final int MAX_MILESTONE_LEVEL = 100;

    public static final int MIN_XP_PER_HIT = 1;
    public static final int MAX_XP_PER_HIT = 1000;

    public static final int MIN_HITS_PER_TARGET = 1;
    public static final int MAX_HITS_PER_TARGET = 100;

    public static final int MIN_BASE_XP = 10;
    public static final int MAX_BASE_XP = 10000;

    public static final int MIN_XP_PER_LEVEL = 0;
    public static final int MAX_XP_PER_LEVEL = 10000;

    // Valores padrão dos modificadores
    private static float rootManaCost = 15.0f;
    private static int combustionFireSeconds = 10;
    private static double kineticKnockbackStrength = 1.5;

    // Valores padrão da progressão e marcos
    private static int milestone1Level = 10;
    private static int milestone1Points = 1;
    private static int milestone2Level = 25;
    private static int milestone2Points = 1;

    private static int pyromancyXpPerHit = 10;
    private static int maxHitsPerTarget = 5;
    private static int pyromancyBaseXp = 50;
    private static int pyromancyXpPerLevel = 25;

    public static float getRootManaCost() {
        return rootManaCost;
    }

    public static int getCombustionFireSeconds() {
        return combustionFireSeconds;
    }

    public static double getKineticKnockbackStrength() {
        return kineticKnockbackStrength;
    }

    public static int getMilestone1Level() {
        return milestone1Level;
    }

    public static int getMilestone1Points() {
        return milestone1Points;
    }

    public static int getMilestone2Level() {
        return milestone2Level;
    }

    public static int getMilestone2Points() {
        return milestone2Points;
    }

    public static int getPyromancyXpPerHit() {
        return pyromancyXpPerHit;
    }

    public static int getMaxHitsPerTarget() {
        return maxHitsPerTarget;
    }

    public static int getPyromancyBaseXp() {
        return pyromancyBaseXp;
    }

    public static int getPyromancyXpPerLevel() {
        return pyromancyXpPerLevel;
    }

    public static synchronized void load() {
        try {
            Path configDir = FabricLoader.getInstance().getConfigDir();
            File configFile = configDir.resolve("caosarcana_talents.json").toFile();

            if (!configFile.exists()) {
                saveDefaults(configFile);
                LOGGER.info("[Caos Arcana] Arquivo de configuração de talentos criado em: {}", configFile.getAbsolutePath());
            } else {
                readConfig(configFile);
            }

            syncDefinitions(configDir);
        } catch (Throwable t) {
            LOGGER.error("[Caos Arcana] Erro ao carregar configuração de talentos, mantendo padrões de segurança.", t);
            resetToDefaults();
        }
    }

    private static void saveDefaults(File file) {
        JsonObject json = new JsonObject();
        json.addProperty("_comentario", "Configuração de talentos e progressão de Piromancia do Caos Arcana");
        json.addProperty("rootManaCost", rootManaCost);
        json.addProperty("combustionFireSeconds", combustionFireSeconds);
        json.addProperty("kineticKnockbackStrength", kineticKnockbackStrength);

        json.addProperty("milestone1Level", milestone1Level);
        json.addProperty("milestone1Points", milestone1Points);
        json.addProperty("milestone2Level", milestone2Level);
        json.addProperty("milestone2Points", milestone2Points);

        json.addProperty("pyromancyXpPerHit", pyromancyXpPerHit);
        json.addProperty("maxHitsPerTarget", maxHitsPerTarget);
        json.addProperty("pyromancyBaseXp", pyromancyBaseXp);
        json.addProperty("pyromancyXpPerLevel", pyromancyXpPerLevel);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (Exception e) {
            LOGGER.error("[Caos Arcana] Falha ao salvar configuração padrão de talentos.", e);
        }
    }

    private static void readConfig(File file) {
        try (FileReader reader = new FileReader(file)) {
            resetToDefaults();
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (json.has("rootManaCost")) {
                float rawMana = json.get("rootManaCost").getAsFloat();
                if (!Float.isFinite(rawMana)) throw new IllegalArgumentException("rootManaCost deve ser finito");
                rootManaCost = Math.max(MIN_MANA_COST, Math.min(MAX_MANA_COST, rawMana));
            }

            if (json.has("combustionFireSeconds")) {
                int rawFire = json.get("combustionFireSeconds").getAsInt();
                combustionFireSeconds = Math.max(MIN_FIRE_SECONDS, Math.min(MAX_FIRE_SECONDS, rawFire));
            }

            if (json.has("kineticKnockbackStrength")) {
                double rawKb = json.get("kineticKnockbackStrength").getAsDouble();
                if (!Double.isFinite(rawKb)) throw new IllegalArgumentException("kineticKnockbackStrength deve ser finito");
                kineticKnockbackStrength = Math.max(MIN_KNOCKBACK, Math.min(MAX_KNOCKBACK, rawKb));
            }

            if (json.has("milestone1Level")) {
                int rawM1 = json.get("milestone1Level").getAsInt();
                milestone1Level = Math.max(MIN_MILESTONE_LEVEL, Math.min(MAX_MILESTONE_LEVEL - 1, rawM1));
            }
            if (json.has("milestone1Points")) {
                milestone1Points = Math.max(1, Math.min(10, json.get("milestone1Points").getAsInt()));
            }

            if (json.has("milestone2Level")) {
                int rawM2 = json.get("milestone2Level").getAsInt();
                milestone2Level = Math.max(milestone1Level + 1, Math.min(MAX_MILESTONE_LEVEL, rawM2));
            }
            if (json.has("milestone2Points")) {
                milestone2Points = Math.max(1, Math.min(10, json.get("milestone2Points").getAsInt()));
            }

            if (json.has("pyromancyXpPerHit")) {
                pyromancyXpPerHit = Math.max(MIN_XP_PER_HIT, Math.min(MAX_XP_PER_HIT, json.get("pyromancyXpPerHit").getAsInt()));
            }

            if (json.has("maxHitsPerTarget")) {
                maxHitsPerTarget = Math.max(MIN_HITS_PER_TARGET, Math.min(MAX_HITS_PER_TARGET, json.get("maxHitsPerTarget").getAsInt()));
            }

            if (json.has("pyromancyBaseXp")) {
                pyromancyBaseXp = Math.max(MIN_BASE_XP, Math.min(MAX_BASE_XP, json.get("pyromancyBaseXp").getAsInt()));
            }

            if (json.has("pyromancyXpPerLevel")) {
                pyromancyXpPerLevel = Math.max(MIN_XP_PER_LEVEL, Math.min(MAX_XP_PER_LEVEL, json.get("pyromancyXpPerLevel").getAsInt()));
            }

            milestone2Level = Math.max(milestone1Level + 1, milestone2Level);

            LOGGER.info("[Caos Arcana] Configuração carregada: Mana={}, Fogo={}s, Recuo={}, Marcos=[Lvl {}: +{}pts, Lvl {}: +{}pts], XP=[Hit={}, MaxHits={}, Base={}, PerLvl={}]",
                    rootManaCost, combustionFireSeconds, kineticKnockbackStrength,
                    milestone1Level, milestone1Points, milestone2Level, milestone2Points,
                    pyromancyXpPerHit, maxHitsPerTarget, pyromancyBaseXp, pyromancyXpPerLevel);
        } catch (Exception e) {
            LOGGER.error("[Caos Arcana] Falha ao ler arquivo de configuração de talentos. Restaurando padrões.", e);
            resetToDefaults();
        }
    }

    public static void syncDefinitions(Path configDir) {
        try {
            Path skillsPiroDir = configDir.resolve("puffish_skills/categories/piromancia");
            if (!Files.exists(skillsPiroDir)) {
                return;
            }

            File defsFile = skillsPiroDir.resolve("definitions.json").toFile();
            JsonObject definitionsJson = buildDynamicDefinitions();

            try (FileWriter writer = new FileWriter(defsFile)) {
                GSON.toJson(definitionsJson, writer);
            }
            LOGGER.info("[Caos Arcana] definitions.json sincronizado com valores configurados em: {}", defsFile.getAbsolutePath());
        } catch (Throwable t) {
            LOGGER.warn("[Caos Arcana] Não foi possível sincronizar definitions.json com a pasta de config: {}", t.getMessage());
        }
    }

    public static JsonObject buildDynamicDefinitions() {
        JsonObject root = new JsonObject();

        // Raiz: Afinidade com as Chamas
        JsonObject raiz = new JsonObject();
        JsonObject raizTitle = new JsonObject();
        raizTitle.addProperty("text", "Afinidade com as Chamas");
        raizTitle.addProperty("color", "gold");
        raiz.add("title", raizTitle);

        JsonObject raizDesc = new JsonObject();
        raizDesc.addProperty("text", "Estabilização elemental da Centelha Ígnea, reduzindo seu custo de conjuração. O custo efetivo aparece na tela de seleção de magias.");
        raiz.add("description", raizDesc);

        JsonObject raizExtra = new JsonObject();
        raizExtra.addProperty("text", "§6[Requisito] §fAprender a Centelha Ígnea (Grimório)\n§b[Custo] §f1 Ponto de Especialização");
        raiz.add("extra_description", raizExtra);

        JsonObject raizIcon = new JsonObject();
        raizIcon.addProperty("type", "item");
        JsonObject raizIconData = new JsonObject();
        raizIconData.addProperty("item", "minecraft:blaze_powder");
        raizIcon.add("data", raizIconData);
        raiz.add("icon", raizIcon);

        raiz.addProperty("frame", "goal");
        raiz.addProperty("cost", 1);
        root.add("caosarcana:piro_raiz_afinidade", raiz);

        // Alt A: Foco em Combustão
        JsonObject combustao = new JsonObject();
        JsonObject combustaoTitle = new JsonObject();
        combustaoTitle.addProperty("text", "Foco em Combustão");
        combustaoTitle.addProperty("color", "red");
        combustao.add("title", combustaoTitle);

        JsonObject combustaoDesc = new JsonObject();
        combustaoDesc.addProperty("text", String.format(Locale.ROOT, "Especialização em queima prolongada: a Centelha Ígnea inflige %ds de fogo contínuo (mantém 5 de dano de impacto).", combustionFireSeconds));
        combustao.add("description", combustaoDesc);

        JsonObject combustaoExtra = new JsonObject();
        combustaoExtra.addProperty("text", "§6[Requisito] §fAfinidade com as Chamas\n§c[Exclusivo] §7Incompatível com Foco Cinético\n§b[Custo] §f1 Ponto de Especialização");
        combustao.add("extra_description", combustaoExtra);

        JsonObject combustaoIcon = new JsonObject();
        combustaoIcon.addProperty("type", "item");
        JsonObject combustaoIconData = new JsonObject();
        combustaoIconData.addProperty("item", "minecraft:fire_charge");
        combustaoIcon.add("data", combustaoIconData);
        combustao.add("icon", combustaoIcon);

        combustao.addProperty("frame", "challenge");
        combustao.addProperty("cost", 1);
        root.add("caosarcana:piro_foco_combustao", combustao);

        // Alt B: Foco Cinético
        JsonObject cinetico = new JsonObject();
        JsonObject cineticoTitle = new JsonObject();
        cineticoTitle.addProperty("text", "Foco Cinético");
        cineticoTitle.addProperty("color", "yellow");
        cinetico.add("title", cineticoTitle);

        JsonObject cineticoDesc = new JsonObject();
        cineticoDesc.addProperty("text", String.format(Locale.ROOT, "Especialização em ruptura tática: substitui as chamas (0s de fogo) por impacto físico com repulsão direcional (recuo %.1f).", kineticKnockbackStrength));
        cinetico.add("description", cineticoDesc);

        JsonObject cineticoExtra = new JsonObject();
        cineticoExtra.addProperty("text", "§6[Requisito] §fAfinidade com as Chamas\n§c[Exclusivo] §7Incompatível com Foco em Combustão\n§b[Custo] §f1 Ponto de Especialização");
        cinetico.add("extra_description", cineticoExtra);

        JsonObject cineticoIcon = new JsonObject();
        cineticoIcon.addProperty("type", "item");
        JsonObject cineticoIconData = new JsonObject();
        cineticoIconData.addProperty("item", "minecraft:piston");
        cineticoIcon.add("data", cineticoIconData);
        cinetico.add("icon", cineticoIcon);

        cinetico.addProperty("frame", "challenge");
        cinetico.addProperty("cost", 1);
        root.add("caosarcana:piro_foco_cinetico", cinetico);

        return root;
    }

    public static void setValuesForTest(float mana, int fire, double kb) {
        rootManaCost = mana;
        combustionFireSeconds = fire;
        kineticKnockbackStrength = kb;
    }

    public static void resetToDefaults() {
        rootManaCost = 15.0f;
        combustionFireSeconds = 10;
        kineticKnockbackStrength = 1.5;
        milestone1Level = 10;
        milestone1Points = 1;
        milestone2Level = 25;
        milestone2Points = 1;
        pyromancyXpPerHit = 10;
        maxHitsPerTarget = 5;
        pyromancyBaseXp = 50;
        pyromancyXpPerLevel = 25;
    }
}
