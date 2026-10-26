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
        "Nenhuma"),

    ORC("orc", "Orc", "Mestres da forja e combate pesado. Corpo robusto e tenacidade incomparável.",
        "Referência Explícita (Decidido)",
        "Dano físico +2, Vida Máxima +4 (2 corações), Armadura Natural +2",
        "Velocidade de movimento -10%",
        "Nenhuma",
        "Custo de mana +25%"),

    FELINO("felino", "Felino", "Povo ágil e silencioso. Perfeitos para exploração e combate acrobático.",
        "Referência Explícita (Decidido)",
        "Velocidade de movimento +15%",
        "Vida Máxima -2 (1 coração)",
        "Nenhuma",
        "Redução de 50% de dano de queda, Salto aprimorado, Menor resistência contra dano pesado"),

    ALTO_ELFO("alto_elfo", "Alto Elfo", "Linhagem mística ancestral com profundo domínio das artes arcanas.",
        "Proposta para Estudo (Pendente de Aprovação)",
        "Nenhuma",
        "Dano corpo a corpo -15%, Vida Máxima -2 (1 coração)",
        "Mana Máxima +50 (campo NBT ativo, sistema mágico pendente)",
        "Regeneração de mana +30%, Dano mágico +15%");

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

    Race(String id, String displayName, String description,
         String documentationStatus,
         String activeAdvantages, String activeDisadvantages,
         String partialTraits, String plannedTraits) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.documentationStatus = documentationStatus;
        this.activeAdvantages = activeAdvantages;
        this.activeDisadvantages = activeDisadvantages;
        this.partialTraits = partialTraits;
        this.plannedTraits = plannedTraits;
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

    public static Race fromId(String id) {
        if (id == null) return NONE;
        for (Race race : values()) {
            if (race.id.equalsIgnoreCase(id.trim())) {
                return race;
            }
        }
        return NONE;
    }

    public void applyModifiers(ServerPlayer player) {
        if (player == null) return;
        removeModifiers(player);

        if (this == ORC) {
            applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Orc Health", 4.0, AttributeModifier.Operation.ADDITION);
            applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Orc Attack", 2.0, AttributeModifier.Operation.ADDITION);
            applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Orc Slowness", -0.10, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, Attributes.ARMOR, ARMOR_MOD_ID, "Orc Natural Armor", 2.0, AttributeModifier.Operation.ADDITION);
        } else if (this == FELINO) {
            applyModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID, "Felino Agility", 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
            applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Felino Frailty", -2.0, AttributeModifier.Operation.ADDITION);
        } else if (this == ALTO_ELFO) {
            applyModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID, "Elfo Fragility", -2.0, AttributeModifier.Operation.ADDITION);
            applyModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID, "Elfo Weak Attack", -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
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
