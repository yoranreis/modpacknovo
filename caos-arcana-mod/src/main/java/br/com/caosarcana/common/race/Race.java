package br.com.caosarcana.common.race;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public enum Race {
    NONE("none", "Sem Raça", "Nenhuma raça selecionada.",
        "Não Definido",
        "Nenhuma",
        "Nenhuma",
        "Nenhuma",
        "Nenhuma",
        0.0f,
        2.0f),

    ORC("orc", "Orc", "Mestres da forja e combate pesado. Corpo robusto e tenacidade incomparável.",
        "Decidido (Oficial Skyrim)",
        "Dano físico +2, Vida Máxima +4 (2 corações), Armadura Natural +2",
        "Velocidade de movimento -10%, regeneração de mana mais lenta",
        "Nenhuma",
        "Ira Berserker: redução de 50% de dano e bônus físico temporário",
        0.0f,
        1.5f),

    FELINO("felino", "Khajiit (Felino)", "Povo ágil e silencioso de Elsweyr. Perfeitos para exploração e emboscada.",
        "Decidido (Oficial Skyrim)",
        "Velocidade de movimento +15%, redução de dano de queda, garras afiadas",
        "Vida Máxima -2 (1 coração)",
        "Nenhuma",
        "Olhos da Noite: visão noturna ativável",
        0.0f,
        2.0f),

    ALTO_ELFO("alto_elfo", "Alto Elfo (Altmer)", "Linhagem nobre ancestral com a mais profunda reserva e afinidade arcana de Tamriel.",
        "Decidido (Oficial Skyrim)",
        "Mana Máxima +50, regeneração de mana acelerada (3.0/s)",
        "Dano corpo a corpo -15%, Vida Máxima -2 (1 coração)",
        "Mana Máxima +50 ativa no NBT",
        "Nobreza Arcana: surto de regeneração arcana",
        50.0f,
        3.0f),

    NORDICO("nordico", "Nórdico", "Filhos de Skyrim com incrível resistência ao frio cortante e maestria em armas de duas mãos.",
        "Decidido (Oficial)",
        "Vida Máxima +2 (1 coração), resistência severa a gelo/criomancia",
        "Afinidade reduzida com piromancia",
        "Nenhuma",
        "Grito de Batalha: dispersão e intimidação de adversários",
        0.0f,
        2.0f),

    ELFO_FLORESTA("elfo_floresta", "Elfo da Floresta (Bosmer)", "Mestres do tiro com arco e rastreamento em Valenwood. Ágeis e resistentes a toxinas.",
        "Decidido (Oficial)",
        "Velocidade de movimento +10%, resistência a veneno/fome +50%",
        "Vida Máxima -2 (1 coração), menor tenacidade corpo a corpo",
        "Nenhuma",
        "Comunhão Selvagem: acalma e comanda bestas naturais",
        10.0f,
        2.0f),

    HUMANO("humano", "Humano", "Versáteis e adaptáveis em qualquer ambiente e ofício, capazes de manejar equipamentos ancestrais.",
        "Decidido (Oficial)",
        "Armadura Natural +1, versatilidade com equipamentos T1-T3 de outras raças",
        "Sem especialização elemental extrema",
        "Nenhuma",
        "Voz do Imperador: pacifica multidões e desarma agressões",
        10.0f,
        2.0f),

    REDGUARD("redguard", "Redguard", "Os mais habilidosos guerreiros espadachins de Tamriel, dotados de estamina implacável.",
        "Decidido (Oficial)",
        "Velocidade de ataque físico +10%, resistência e mobilidade marcial",
        "Afinidade arcana inicial mais lenta (regeneração 1.8/s)",
        "Nenhuma",
        "Ímpeto de Adrenalina: restaura estamina e acelera velocidade de combate",
        0.0f,
        1.8f),

    ARGONIANO("argoniano", "Argoniano", "Povo réptil das profundezas de Black Marsh, mestres em emboscadas aquáticas e resistência.",
        "Decidido (Oficial)",
        "Respiração aquática permanente, velocidade de nado dobrada, imunidade a afogamento",
        "Vulnerabilidade aumentada a temperaturas geladas e feitiços de gelo",
        "Nenhuma",
        "Pele de Hist: regeneração emergencial acelerada de vida",
        0.0f,
        2.0f),

    SANGUE_DAEDRA("sangue_daedra", "Sangue-Daedra", "Linhagem tocada pelos planos de Oblivion. Imunes às chamas e reverenciados pelos habitantes do Nether.",
        "Decidido (Oficial)",
        "Imunidade permanente a dano de fogo, lava e magma; neutralidade absoluta de mobs nativos do Nether",
        "Pendente de definição formal de atributos e desvantagens",
        "Imunidade ambiental e neutralidade do Nether",
        "Pendente de definição formal de poder ativo",
        0.0f,
        2.0f),

    VOIDMER("voidmer", "Voidmer", "Povo misterioso sintonizado com o Vazio e distorções do éter.",
        "Decidido (Oficial)",
        "Pendente de definição formal de atributos e vantagens",
        "Pendente de definição formal de desvantagens",
        "Nenhuma",
        "Pendente de definição formal de poder ativo",
        0.0f,
        2.0f);

    private static final UUID HEALTH_MOD_ID = UUID.fromString("c00a0001-ca05-4000-8000-000000000001");
    private static final UUID DAMAGE_MOD_ID = UUID.fromString("c00a0002-ca05-4000-8000-000000000002");
    private static final UUID SPEED_MOD_ID = UUID.fromString("c00a0003-ca05-4000-8000-000000000003");
    private static final UUID ARMOR_MOD_ID = UUID.fromString("c00a0004-ca05-4000-8000-000000000004");

    private final String id;
    private final String displayName;
    private final String description;
    private final String documentationStatus;
    private final String activeAdvantages;
    private final String activeDisadvantages;
    private final String partialTraits;
    private final String plannedTraits;
    private final float manaBonus;
    private final float manaRegenPerSecond;

    Race(String id, String displayName, String description,
         String documentationStatus,
         String activeAdvantages, String activeDisadvantages,
         String partialTraits, String plannedTraits,
         float manaBonus,
         float manaRegenPerSecond) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.documentationStatus = documentationStatus;
        this.activeAdvantages = activeAdvantages;
        this.activeDisadvantages = activeDisadvantages;
        this.partialTraits = partialTraits;
        this.plannedTraits = plannedTraits;
        this.manaBonus = manaBonus;
        this.manaRegenPerSecond = manaRegenPerSecond;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getDocumentationStatus() {
        return documentationStatus;
    }

    public String getActiveAdvantages() {
        return activeAdvantages;
    }

    public String getActiveDisadvantages() {
        return activeDisadvantages;
    }

    public String getPartialTraits() {
        return partialTraits;
    }

    public String getPlannedTraits() {
        return plannedTraits;
    }

    public float getManaBonus() {
        return manaBonus;
    }

    public float getManaRegenPerSecond() {
        return manaRegenPerSecond;
    }

    public float getVisualScale() {
        return switch (this) {
            case ORC -> 1.055f;             // 1,90 m
            case ALTO_ELFO -> 1.100f;       // 1,98 m
            case NORDICO -> 1.044f;         // 1,88 m
            case SANGUE_DAEDRA -> 1.033f;   // 1,86 m
            case REDGUARD -> 1.011f;        // 1,82 m
            case VOIDMER -> 1.011f;         // 1,82 m
            case ARGONIANO -> 1.000f;       // 1,80 m
            case HUMANO -> 1.000f;          // 1,80 m
            case NONE -> 1.000f;            // 1,80 m
            case FELINO -> 0.988f;          // 1,78 m
            case ELFO_FLORESTA -> 0.944f;   // 1,70 m
        };
    }

    public static Race fromId(String id) {
        if (id == null) return NONE;
        String clean = id.trim().toLowerCase();
        // Compatibilidade com IDs antigos renomeados
        if (clean.equals("imperial")) return HUMANO;
        if (clean.equals("guarda_vermelho")) return REDGUARD;
        // IDs legados substituídos (oferecer nova seleção, sem conversão compulsória)
        if (clean.equals("bretao") || clean.equals("elfo_negro")) return NONE;

        for (Race race : values()) {
            if (race.id.equalsIgnoreCase(clean) || race.name().equalsIgnoreCase(clean)) {
                return race;
            }
        }
        return NONE;
    }

    public void applyModifiers(ServerPlayer player) {
        if (player == null) return;
        removeModifiers(player);

        switch (this) {
            case ORC -> {
                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Orc Health", 4.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Orc Attack", 2.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Orc Slowness", -0.10, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.ARMOR, ARMOR_MOD_ID, "Orc Natural Armor", 2.0, AttributeModifier.Operation.ADDITION);
            }
            case FELINO -> {
                applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Felino Agility", 0.15, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Felino Frailty", -2.0, AttributeModifier.Operation.ADDITION);
            }
            case ALTO_ELFO -> {
                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Elfo Fragility", -2.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Elfo Weak Attack", -0.15, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case NORDICO -> {
                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Nord Health", 2.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Nord Strength", 1.0, AttributeModifier.Operation.ADDITION);
            }
            case ELFO_FLORESTA -> {
                applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Bosmer Swiftness", 0.10, AttributeModifier.Operation.MULTIPLY_BASE);
                applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Bosmer Fragility", -2.0, AttributeModifier.Operation.ADDITION);
            }
            case HUMANO -> {
                applyModifier(player, Attributes.ARMOR, ARMOR_MOD_ID, "Human Tenacity", 1.0, AttributeModifier.Operation.ADDITION);
            }
            case REDGUARD -> {
                applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Redguard Finesse", 1.0, AttributeModifier.Operation.ADDITION);
                applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Redguard Footwork", 0.05, AttributeModifier.Operation.MULTIPLY_BASE);
            }
            case ARGONIANO -> {
                applyModifier(player, Attributes.ARMOR, ARMOR_MOD_ID, "Argonian Scales", 2.0, AttributeModifier.Operation.ADDITION);
            }
            case SANGUE_DAEDRA, VOIDMER, NONE -> {}
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public void removeModifiers(ServerPlayer player) {
        if (player == null) return;
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID);
        removeModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID);
        removeModifier(player, Attributes.ARMOR, ARMOR_MOD_ID);

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void applyModifier(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(id, name, amount, op));
        }
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID id) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}
