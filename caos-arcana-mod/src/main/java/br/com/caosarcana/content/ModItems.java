package br.com.caosarcana.content;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.content.economy.CoinItem;
import br.com.caosarcana.content.spells.FlameSparkItem;
import br.com.caosarcana.content.spells.SpellGrimoireItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class ModItems {
    // Moedas Físicas da Economia do Caos Arcana
    public static final Item MOEDA_COBRE = new CoinItem(CoinItem.CoinType.COBRE);
    public static final Item MOEDA_PRATA = new CoinItem(CoinItem.CoinType.PRATA);
    public static final Item MOEDA_OURO = new CoinItem(CoinItem.CoinType.OURO);

    // Grimórios Arcanos de Magias Universais (Escolas de Skyrim)
    public static final Item CENTELHA_IGNEA = new FlameSparkItem();
    public static final Item GRIMORIO_GEADA = new SpellGrimoireItem("caosarcana:geada_congelante");
    public static final Item GRIMORIO_RELAMPAGO = new SpellGrimoireItem("caosarcana:relampago_arcano");
    public static final Item GRIMORIO_CURA = new SpellGrimoireItem("caosarcana:toque_de_cura");
    public static final Item GRIMORIO_CIRCULO_SAGRADO = new SpellGrimoireItem("caosarcana:circulo_sagrado");
    public static final Item GRIMORIO_CARVALHO = new SpellGrimoireItem("caosarcana:corpo_de_carvalho");
    public static final Item GRIMORIO_LUZ = new SpellGrimoireItem("caosarcana:luz_de_vela");
    public static final Item GRIMORIO_ESPADA = new SpellGrimoireItem("caosarcana:espada_vinculada");
    public static final Item GRIMORIO_FAMILIAR = new SpellGrimoireItem("caosarcana:familiar_espectral");
    public static final Item GRIMORIO_SOMBRAS = new SpellGrimoireItem("caosarcana:manto_de_sombras");

    public static void register() {
        // Moedas
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("moeda_cobre"), MOEDA_COBRE);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("moeda_prata"), MOEDA_PRATA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("moeda_ouro"), MOEDA_OURO);

        // Grimórios
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("centelha_ignea"), CENTELHA_IGNEA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_geada"), GRIMORIO_GEADA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_relampago"), GRIMORIO_RELAMPAGO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_cura"), GRIMORIO_CURA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_circulo_sagrado"), GRIMORIO_CIRCULO_SAGRADO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_carvalho"), GRIMORIO_CARVALHO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_luz"), GRIMORIO_LUZ);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_espada"), GRIMORIO_ESPADA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_familiar"), GRIMORIO_FAMILIAR);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_sombras"), GRIMORIO_SOMBRAS);

        CaosArcana.LOGGER.info("[Caos Arcana] Moedas econômicas e grimórios arcanos registrados com sucesso.");
    }
}
