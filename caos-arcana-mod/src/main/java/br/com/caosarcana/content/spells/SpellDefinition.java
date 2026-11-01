package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.race.Race;
import net.minecraft.network.chat.Component;

/**
 * Definição imutável de metadados de uma magia registrada no Caos Arcana.
 * Suporta magias universais e magias restritas por raça.
 */
public record SpellDefinition(
        String id,
        String name,
        String school,
        String schoolDisplayName,
        int minSchoolLevel,
        float manaCost,
        int cooldownTicks,
        int maxLifetimeTicks,
        double maxDistanceBlocks,
        String description,
        Race requiredRace // null se for Universal
) {
    public SpellDefinition(
            String id,
            String name,
            String school,
            String schoolDisplayName,
            int minSchoolLevel,
            float manaCost,
            int cooldownTicks,
            int maxLifetimeTicks,
            double maxDistanceBlocks,
            String description
    ) {
        this(id, name, school, schoolDisplayName, minSchoolLevel, manaCost, cooldownTicks, maxLifetimeTicks, maxDistanceBlocks, description, null);
    }

    public boolean isRacial() {
        return requiredRace != null && requiredRace != Race.NONE;
    }

    public boolean canCast(Race race) {
        if (!isRacial()) return true;
        return this.requiredRace == race;
    }

    public Component getDisplayName() {
        return Component.literal(name);
    }
}
