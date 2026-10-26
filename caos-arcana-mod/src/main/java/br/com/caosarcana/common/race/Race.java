package br.com.caosarcana.common.race;

import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public enum Race {
    NONE("none", "Sem Raça", "Nenhuma raça selecionada."),
    ORC("orc", "Orc", "Mestres da forja e combate pesado. Corpo robusto e tenacidade incomparável.",
        "Dano físico +2, Vida Máxima +4 (2 corações), Armadura Natural +2",
        "Velocidade de movimento -10%, Custo de mana +25%"),
    FELINO("felino", "Felino", "Povo ágil e silencioso. Perfeitos para exploração e combate acrobático.",
        "Velocidade de movimento +15%, Redução de 50% de dano de queda, Salto aprimorado",
        "Vida Máxima -2 (1 coração), Menor resistência física contra dano pesado"),
    ALTO_ELFO("alto_elfo", "Alto Elfo", "Linhagem mística ancestral com profundo domínio das artes arcanas.",
        "Mana Máxima +50, Regeneração de mana +30%, Dano mágico +15%",
        "Dano corpo a corpo -15%, Vida Máxima -2 (1 coração)");

    private static final UUID HEALTH_MOD_ID = UUID.fromString("c00a0001-ca05-4000-8000-000000000001");
    private static final UUID DAMAGE_MOD_ID = UUID.fromString("c00a0002-ca05-4000-8000-000000000002");
    private static final UUID SPEED_MOD_ID = UUID.fromString("c00a0003-ca05-4000-8000-000000000003");
    private static final UUID ARMOR_MOD_ID = UUID.fromString("c00a0004-ca05-4000-8000-000000000004");

    private final String id;
    private final String displayName;
    private final String description;
    private final String advantages;
    private final String disadvantages;

    Race(String id, String displayName, String description, String advantages, String disadvantages) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.advantages = advantages;
        this.disadvantages = disadvantages;
    }

    Race(String id, String displayName, String description) {
        this(id, displayName, description, "", "");
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

    public String getAdvantages() {
        return advantages;
    }

    public String getDisadvantages() {
        return disadvantages;
    }

    public static Race fromId(String id) {
        for (Race race : values()) {
            if (race.id.equalsIgnoreCase(id)) {
                return race;
            }
        }
        return NONE;
    }

    public void applyModifiers(ServerPlayer player) {
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
        removeModifier(player, Attributes.MAX_HEALTH, HEALTH_MOD_ID);
        removeModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_MOD_ID);
        removeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_MOD_ID);
        removeModifier(player, Attributes.ARMOR, ARMOR_MOD_ID);
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
