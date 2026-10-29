package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.race.Race;
import java.util.*;

/**
 * Registro central de metadados das magias do Caos Arcana.
 * Suporta o ecossistema completo de:
 * 1. Poderes Raciais Específicos (1 exclusivo por raça, desbloqueado por linhagem ancestral).
 * 2. Magias Universais (acessíveis a todas as raças através de grimórios, provações, missões e dungeons).
 */
public class SpellRegistry {
    private static final Map<String, SpellDefinition> REGISTRY = new LinkedHashMap<>();

    // ==========================================
    // 1. MAGIAS UNIVERSAIS (ESCOLAS DE SKYRIM)
    // Conquistáveis por qualquer raça via grimórios, provações e dungeons
    // ==========================================

    // --- DESTRUIÇÃO: PIROMANCIA ---
    public static final SpellDefinition CENTELHA_IGNEA = register(new SpellDefinition(
            SpellCastHandler.CENTELHA_IGNEA_ID,
            "Centelha Ígnea",
            "piromancia",
            "Piromancia (Destruição)",
            1,
            FlameSparkItem.MANA_COST,
            FlameSparkItem.COOLDOWN_TICKS,
            FlameSparkProjectile.MAX_LIFETIME_TICKS,
            FlameSparkProjectile.MAX_DISTANCE_BLOCKS,
            "Dispara uma centelha de chamas arcanas que incendeia o alvo atingido e causa dano de fogo contínuo."
    ));

    // --- DESTRUIÇÃO: CRIOMANCIA ---
    public static final SpellDefinition GEADA_CONGELANTE = register(new SpellDefinition(
            "caosarcana:geada_congelante",
            "Geada Congelante",
            "criomancia",
            "Criomancia (Destruição)",
            1,
            18.0f,
            30,
            0,
            16.0,
            "Lança uma rajada gélida cortante que penetra a carne do alvo, causando dano místico, congelamento e lentidão severa."
    ));

    // --- DESTRUIÇÃO: ELETROMANCIA ---
    public static final SpellDefinition RELAMPAGO_ARCANO = register(new SpellDefinition(
            "caosarcana:relampago_arcano",
            "Relâmpago Arcano",
            "eletromancia",
            "Eletromancia (Destruição)",
            2,
            28.0f,
            45,
            0,
            24.0,
            "Canaliza uma descarga de alta voltagem mística instantânea em linha reta, provocando choque estrondoso e desorientando a vítima."
    ));

    // --- RESTAURAÇÃO ---
    public static final SpellDefinition TOQUE_DE_CURA = register(new SpellDefinition(
            "caosarcana:toque_de_cura",
            "Toque de Cura",
            "restauracao",
            "Restauração",
            1,
            25.0f,
            40,
            0,
            0,
            "Converte energia arcana pura em vitalidade regenerativa, restaurando imediatamente 6 pontos de vida (3 corações) do conjurador."
    ));

    public static final SpellDefinition CIRCULO_SAGRADO = register(new SpellDefinition(
            "caosarcana:circulo_sagrado",
            "Círculo Sagrado",
            "restauracao",
            "Restauração",
            2,
            35.0f,
            100,
            0,
            6.0,
            "Manifesta uma barreira luminosa sagrada em torno do conjurador que repele mortos-vivos e reduz temporariamente o dano recebido."
    ));

    // --- ALTERAÇÃO ---
    public static final SpellDefinition CORPO_DE_CARVALHO = register(new SpellDefinition(
            "caosarcana:corpo_de_carvalho",
            "Corpo de Carvalho",
            "alteracao",
            "Alteração",
            1,
            30.0f,
            120,
            0,
            0,
            "Endurece magicamente a pele do conjurador na densidade do cerne de carvalho, concedendo armadura natural e resistência por 45 segundos."
    ));

    public static final SpellDefinition LUZ_DE_VELA = register(new SpellDefinition(
            "caosarcana:luz_de_vela",
            "Luz de Vela",
            "alteracao",
            "Alteração",
            1,
            12.0f,
            60,
            0,
            0,
            "Cria uma esfera etérea flutuante de luz dourada que acompanha o conjurador, iluminando caminhos escuros e cavernas profundas."
    ));

    // --- CONJURAÇÃO ---
    public static final SpellDefinition ESPADA_VINCULADA = register(new SpellDefinition(
            "caosarcana:espada_vinculada",
            "Espada Vinculada",
            "conjuracao",
            "Conjuração",
            1,
            32.0f,
            160,
            0,
            0,
            "Invoca temporariamente dos planos de Oblivion uma lâmina espectral arcana imbuída de poder místico, aumentando drasticamente o ataque físico."
    ));

    public static final SpellDefinition FAMILIAR_ESPECTRAL = register(new SpellDefinition(
            "caosarcana:familiar_espectral",
            "Familiar Espectral",
            "conjuracao",
            "Conjuração",
            2,
            45.0f,
            400,
            0,
            0,
            "Materializa um espírito lupino místico leal que luta fervorosamente ao lado do conjurador, perseguindo agressores por 60 segundos."
    ));

    // --- ILUSÃO ---
    public static final SpellDefinition MANTO_DE_SOMBRAS = register(new SpellDefinition(
            "caosarcana:manto_de_sombras",
            "Manto de Sombras",
            "ilusao",
            "Ilusão",
            1,
            28.0f,
            240,
            0,
            0,
            "Dobra as ondas de luz ao redor do corpo com véus de névoa escura, tornando o conjurador invisível a olhos nus e monstros por 20 segundos."
    ));

    // ==========================================
    // 2. PODERES MÁGICOS RACIAIS EXCLUSIVOS (1 POR RAÇA)
    // Desbloqueados pela linhagem ancestral de cada raça
    // ==========================================
    public static final SpellDefinition IRA_BERSERKER = register(new SpellDefinition(
            "caosarcana:ira_berserker",
            "Ira Berserker",
            "racas",
            "Poder Racial",
            1,
            35.0f,
            600, // 30s de recarga
            0,
            0,
            "Desperta o sangue ancestral do Orc, concedendo resistência extrema a dano e força física esmagadora temporariamente.",
            Race.ORC
    ));

    public static final SpellDefinition OLHOS_DA_NOITE = register(new SpellDefinition(
            "caosarcana:olhos_da_noite",
            "Olhos da Noite",
            "racas",
            "Poder Racial",
            1,
            15.0f,
            200, // 10s de recarga
            0,
            0,
            "Concede visão noturna cristalina e percepção aguçada sob a luz das duas luas de Elsweyr por 60 segundos.",
            Race.FELINO
    ));

    public static final SpellDefinition NOBREZA_ARCANA = register(new SpellDefinition(
            "caosarcana:nobreza_arcana",
            "Nobreza Arcana",
            "racas",
            "Poder Racial",
            1,
            10.0f,
            800, // 40s de recarga
            0,
            0,
            "Canaliza a essência arcana da linhagem Altmer, restaurando 40 de mana e acelerando a regeneração mágica.",
            Race.ALTO_ELFO
    ));

    public static final SpellDefinition GRITO_DE_BATALHA = register(new SpellDefinition(
            "caosarcana:grito_de_batalha",
            "Grito de Batalha",
            "racas",
            "Poder Racial",
            1,
            25.0f,
            500,
            0,
            8.0,
            "Grito intimidador dos filhos de Skyrim que repele e desorienta adversários em um raio de 8 blocos com onda de choque sonora.",
            Race.NORDICO
    ));

    public static final SpellDefinition IRA_ANCESTRAL = register(new SpellDefinition(
            "caosarcana:ira_ancestral",
            "Ira Ancestral",
            "racas",
            "Poder Racial",
            1,
            30.0f,
            600,
            0,
            5.0,
            "Envolve o Elfo Negro em uma tempestade protetora de cinzas e chamas que queima e incendeia inimigos próximos.",
            Race.ELFO_NEGRO
    ));

    public static final SpellDefinition COMUNHAO_SELVAGEM = register(new SpellDefinition(
            "caosarcana:comunhao_selvagem",
            "Comunhão Selvagem",
            "racas",
            "Poder Racial",
            1,
            20.0f,
            400,
            0,
            12.0,
            "Sintonia dos Bosmer com o coração verde da floresta, pacificando feras hostis instantaneamente e fortalecendo aliados naturais.",
            Race.ELFO_FLORESTA
    ));

    public static final SpellDefinition PELE_DE_DRAGAO = register(new SpellDefinition(
            "caosarcana:pele_de_dragao",
            "Pele de Dragão",
            "racas",
            "Poder Racial",
            1,
            40.0f,
            700,
            0,
            0,
            "Barreira arcana dos Bretões inspirada na linhagem dos dragões, absorvendo impactos e convertendo energia mágica recebida.",
            Race.BRETAO
    ));

    public static final SpellDefinition VOZ_DO_IMPERADOR = register(new SpellDefinition(
            "caosarcana:voz_do_imperador",
            "Voz do Imperador",
            "racas",
            "Poder Racial",
            1,
            25.0f,
            500,
            0,
            10.0,
            "Comando diplomático inquestionável dos Imperiais que desarma agressões, acalma multidões e traz respeito comercial.",
            Race.IMPERIAL
    ));

    public static final SpellDefinition IMPETO_DE_ADRENALINA = register(new SpellDefinition(
            "caosarcana:impeto_de_adrenalina",
            "Ímpeto de Adrenalina",
            "racas",
            "Poder Racial",
            1,
            20.0f,
            500,
            0,
            0,
            "Explosão marcial dos Guardas Vermelhos que restaura o fôlego e acelera o ritmo de combate e movimentação.",
            Race.GUARDA_VERMELHO
    ));

    public static final SpellDefinition PELE_DE_HIST = register(new SpellDefinition(
            "caosarcana:pele_de_hist",
            "Pele de Hist",
            "racas",
            "Poder Racial",
            1,
            30.0f,
            600,
            0,
            0,
            "Bênção das árvores sagradas de Hist que regenera a vitalidade do Argoniano em ritmo ultra acelerado por 10 segundos.",
            Race.ARGONIANO
    ));

    public static SpellDefinition register(SpellDefinition spell) {
        REGISTRY.put(spell.id().toLowerCase(), spell);
        return spell;
    }

    public static Optional<SpellDefinition> get(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(REGISTRY.get(id.trim().toLowerCase()));
    }

    public static boolean contains(String id) {
        if (id == null) return false;
        return REGISTRY.containsKey(id.trim().toLowerCase());
    }

    public static Collection<SpellDefinition> getAll() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static Optional<SpellDefinition> getRacialSpellFor(Race race) {
        if (race == null || race == Race.NONE) return Optional.empty();
        for (SpellDefinition spell : REGISTRY.values()) {
            if (spell.isRacial() && spell.requiredRace() == race) {
                return Optional.of(spell);
            }
        }
        return Optional.empty();
    }

    public static List<SpellDefinition> getUniversalSpells() {
        List<SpellDefinition> list = new ArrayList<>();
        for (SpellDefinition spell : REGISTRY.values()) {
            if (!spell.isRacial()) {
                list.add(spell);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public static List<SpellDefinition> getSpellsForSchool(String school) {
        if (school == null) return Collections.emptyList();
        List<SpellDefinition> list = new ArrayList<>();
        for (SpellDefinition spell : REGISTRY.values()) {
            if (school.equalsIgnoreCase(spell.school())) {
                list.add(spell);
            }
        }
        return Collections.unmodifiableList(list);
    }
}
