package br.com.caosarcana.content;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.content.spells.FlameSparkItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class ModItems {
    public static final Item CENTELHA_IGNEA = new FlameSparkItem();

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("centelha_ignea"), CENTELHA_IGNEA);
        CaosArcana.LOGGER.info("[Caos Arcana] Itens arcanos e feitiços registrados (Centelha Ígnea).");
    }
}
