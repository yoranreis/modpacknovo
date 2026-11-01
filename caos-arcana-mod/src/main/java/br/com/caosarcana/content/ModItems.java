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
    public static final Item GRIMORIO_BOLA_DE_FOGO = new SpellGrimoireItem("caosarcana:bola_de_fogo");
    public static final Item GRIMORIO_MANTO_DE_CHAMAS = new SpellGrimoireItem("caosarcana:manto_de_chamas");
    public static final Item GRIMORIO_MURALHA_DE_CHAMAS = new SpellGrimoireItem("caosarcana:muralha_de_chamas");
    public static final Item GRIMORIO_DARDO_DE_FOGO = new SpellGrimoireItem("caosarcana:dardo_de_fogo");
    public static final Item GRIMORIO_INCINERAR = new SpellGrimoireItem("caosarcana:incinerar");
    public static final Item GRIMORIO_TEMPESTADE_DE_FOGO = new SpellGrimoireItem("caosarcana:tempestade_de_fogo");
    public static final Item GRIMORIO_DARDO_DE_GELO = new SpellGrimoireItem("caosarcana:dardo_de_gelo");
    public static final Item GRIMORIO_ESPIGA_DE_GELO = new SpellGrimoireItem("caosarcana:espiga_de_gelo");
    public static final Item GRIMORIO_NEVASCA = new SpellGrimoireItem("caosarcana:nevasca");
    public static final Item GRIMORIO_FAISCA_ELETRICA = new SpellGrimoireItem("caosarcana:faisca_eletrica");
    public static final Item GRIMORIO_CORRENTE_DE_RELAMPAGO = new SpellGrimoireItem("caosarcana:corrente_de_relampago");
    public static final Item GRIMORIO_TEMPESTADE_DE_RAIOS = new SpellGrimoireItem("caosarcana:tempestade_de_raios");
    public static final Item GRIMORIO_CURA_BASICA = new SpellGrimoireItem("caosarcana:cura_basica");
    public static final Item GRIMORIO_CURA_RAPIDA = new SpellGrimoireItem("caosarcana:cura_rapida");
    public static final Item GRIMORIO_BARREIRA_MENOR = new SpellGrimoireItem("caosarcana:barreira_menor");
    public static final Item GRIMORIO_ARMADURA_DE_PEDRA = new SpellGrimoireItem("caosarcana:armadura_de_pedra");
    public static final Item GRIMORIO_LUZ_ARCANA = new SpellGrimoireItem("caosarcana:luz_arcana");
    public static final Item GRIMORIO_RESPIRACAO_AQUATICA = new SpellGrimoireItem("caosarcana:respiracao_aquatica");
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
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_bola_de_fogo"), GRIMORIO_BOLA_DE_FOGO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_manto_de_chamas"), GRIMORIO_MANTO_DE_CHAMAS);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_muralha_de_chamas"), GRIMORIO_MURALHA_DE_CHAMAS);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_dardo_de_fogo"), GRIMORIO_DARDO_DE_FOGO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_incinerar"), GRIMORIO_INCINERAR);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_tempestade_de_fogo"), GRIMORIO_TEMPESTADE_DE_FOGO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_dardo_de_gelo"), GRIMORIO_DARDO_DE_GELO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_espiga_de_gelo"), GRIMORIO_ESPIGA_DE_GELO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_nevasca"), GRIMORIO_NEVASCA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_faisca_eletrica"), GRIMORIO_FAISCA_ELETRICA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_corrente_de_relampago"), GRIMORIO_CORRENTE_DE_RELAMPAGO);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_tempestade_de_raios"), GRIMORIO_TEMPESTADE_DE_RAIOS);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_cura_basica"), GRIMORIO_CURA_BASICA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_cura_rapida"), GRIMORIO_CURA_RAPIDA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_barreira_menor"), GRIMORIO_BARREIRA_MENOR);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_armadura_de_pedra"), GRIMORIO_ARMADURA_DE_PEDRA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_luz_arcana"), GRIMORIO_LUZ_ARCANA);
        Registry.register(BuiltInRegistries.ITEM, CaosArcana.id("grimorio_respiracao_aquatica"), GRIMORIO_RESPIRACAO_AQUATICA);
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
