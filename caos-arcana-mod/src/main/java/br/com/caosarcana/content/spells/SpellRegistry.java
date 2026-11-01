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

    /** Valores provisórios do primeiro lote jogável de Piromancia. */
    public static final SpellDefinition BOLA_DE_FOGO = register(new SpellDefinition(
            "caosarcana:bola_de_fogo",
            "Bola de Fogo",
            "piromancia",
            "Piromancia (Destruição)",
            10,
            30.0f,
            80,
            0,
            24.0,
            "Projétil explosivo de fogo que atinge um alvo e incendeia criaturas próximas."
    ));

    public static final SpellDefinition MANTO_DE_CHAMAS = register(new SpellDefinition(
            "caosarcana:manto_de_chamas",
            "Manto de Chamas",
            "piromancia",
            "Piromancia (Destruição)",
            20,
            40.0f,
            300,
            200,
            4.0,
            "Envolve o conjurador em chamas, protegendo-o do fogo e queimando inimigos próximos."
    ));

    public static final SpellDefinition MURALHA_DE_CHAMAS = register(new SpellDefinition(
            "caosarcana:muralha_de_chamas",
            "Muralha de Chamas",
            "piromancia",
            "Piromancia (Destruição)",
            25,
            50.0f,
            400,
            100,
            12.0,
            "Projeta uma faixa de fogo à frente do conjurador, queimando inimigos que a atravessam."
    ));

    public static final SpellDefinition DARDO_DE_FOGO = register(new SpellDefinition(
            "caosarcana:dardo_de_fogo", "Dardo de Fogo", "piromancia", "Piromancia (Destruição)",
            1, 22.0f, 30, 0, 32.0,
            "Dispara um dardo ígneo veloz contra o primeiro alvo na linha de visão."
    ));

    public static final SpellDefinition INCINERAR = register(new SpellDefinition(
            "caosarcana:incinerar", "Incinerar", "piromancia", "Piromancia (Destruição)",
            35, 60.0f, 500, 0, 30.0,
            "Canaliza um feixe concentrado de fogo que incinera o alvo atingido."
    ));

    public static final SpellDefinition TEMPESTADE_DE_FOGO = register(new SpellDefinition(
            "caosarcana:tempestade_de_fogo", "Tempestade de Fogo", "piromancia", "Piromancia (Destruição)",
            50, 80.0f, 800, 0, 8.0,
            "Convoca uma explosão de chamas ao redor do conjurador, atingindo inimigos próximos."
    ));

    public static final SpellDefinition DARDO_DE_GELO = register(new SpellDefinition(
            "caosarcana:dardo_de_gelo", "Dardo de Gelo", "criomancia", "Criomancia (Destruição)",
            1, 20.0f, 30, 0, 28.0,
            "Dispara uma lâmina de gelo que causa dano e reduz a velocidade do alvo."
    ));

    public static final SpellDefinition ESPIGA_DE_GELO = register(new SpellDefinition(
            "caosarcana:espiga_de_gelo", "Espiga de Gelo", "criomancia", "Criomancia (Destruição)",
            15, 35.0f, 100, 0, 24.0,
            "Lança uma espiga congelante que atravessa a linha de visão e imobiliza brevemente o primeiro alvo."
    ));

    public static final SpellDefinition NEVASCA = register(new SpellDefinition(
            "caosarcana:nevasca", "Nevasca", "criomancia", "Criomancia (Destruição)",
            40, 65.0f, 600, 0, 8.0,
            "Invoca uma nevasca concentrada ao redor do conjurador, congelando inimigos próximos."
    ));

    public static final SpellDefinition FASCULA_ELETRICA = register(new SpellDefinition(
            "caosarcana:faisca_eletrica", "Faísca Elétrica", "eletromancia", "Eletromancia (Destruição)",
            1, 16.0f, 20, 0, 24.0,
            "Dispara uma faísca elétrica rápida contra o primeiro alvo na linha de visão."
    ));

    public static final SpellDefinition CORRENTE_DE_RELAMPAGO = register(new SpellDefinition(
            "caosarcana:corrente_de_relampago", "Corrente de Relâmpago", "eletromancia", "Eletromancia (Destruição)",
            20, 38.0f, 140, 0, 20.0,
            "Encadeia uma descarga elétrica do alvo principal para criaturas próximas."
    ));

    public static final SpellDefinition TEMPESTADE_DE_RAIOS = register(new SpellDefinition(
            "caosarcana:tempestade_de_raios", "Tempestade de Raios", "eletromancia", "Eletromancia (Destruição)",
            45, 70.0f, 700, 0, 10.0,
            "Convoca uma tempestade elétrica concentrada que atinge inimigos ao redor do conjurador."
    ));

    public static final SpellDefinition CURA_BASICA = register(new SpellDefinition(
            "caosarcana:cura_basica", "Cura Básica", "restauracao", "Restauração", 1, 18.0f, 100, 0, 0,
            "Restaura uma pequena quantidade de vida do conjurador."
    ));
    public static final SpellDefinition CURA_RAPIDA = register(new SpellDefinition(
            "caosarcana:cura_rapida", "Cura Rápida", "restauracao", "Restauração", 15, 35.0f, 240, 0, 0,
            "Aplica regeneração acelerada ao conjurador por alguns segundos."
    ));
    public static final SpellDefinition BARREIRA_MENOR = register(new SpellDefinition(
            "caosarcana:barreira_menor", "Barreira Menor", "restauracao", "Restauração", 30, 45.0f, 300, 0, 0,
            "Forma uma barreira arcana que reduz o dano recebido temporariamente."
    ));

    public static final SpellDefinition ARMADURA_DE_PEDRA = register(new SpellDefinition(
            "caosarcana:armadura_de_pedra", "Armadura de Pedra", "alteracao", "Alteração", 1, 26.0f, 180, 0, 0,
            "Endurece a pele do conjurador com a resistência mineral da pedra."
    ));
    public static final SpellDefinition LUZ_ARCANA = register(new SpellDefinition(
            "caosarcana:luz_arcana", "Luz Arcana", "alteracao", "Alteração", 15, 20.0f, 80, 0, 0,
            "Emite uma luz arcana intensa que revela o ambiente e criaturas próximas."
    ));
    public static final SpellDefinition RESPIRACAO_AQUATICA = register(new SpellDefinition(
            "caosarcana:respiracao_aquatica", "Respiração Aquática", "alteracao", "Alteração", 30, 32.0f, 300, 0, 0,
            "Permite respirar sob a água e enxergar com clareza por um período prolongado."
    ));

    public static final SpellDefinition MACHADO_VINCULADO = register(new SpellDefinition(
            "caosarcana:machado_vinculado", "Machado Vinculado", "conjuracao", "Conjuração", 1, 34.0f, 180, 0, 0,
            "Invoca o poder de uma arma vinculada, fortalecendo os golpes do conjurador."
    ));
    public static final SpellDefinition ARCO_VINCULADO = register(new SpellDefinition(
            "caosarcana:arco_vinculado", "Arco Vinculado", "conjuracao", "Conjuração", 15, 38.0f, 220, 0, 0,
            "Vincula um arco espectral ao conjurador, aprimorando seus ataques à distância."
    ));
    public static final SpellDefinition REANIMAR_MORTO_VIVO = register(new SpellDefinition(
            "caosarcana:reanimar_morto_vivo", "Reanimar Morto-Vivo", "conjuracao", "Conjuração", 35, 58.0f, 500, 1200, 0,
            "Levanta um servo morto-vivo temporário para lutar ao lado do conjurador."
    ));

    public static final SpellDefinition CLARIVIDENCIA = register(new SpellDefinition(
            "caosarcana:clarividencia", "Clarividência", "ilusao", "Ilusão", 1, 16.0f, 120, 0, 0,
            "Revela o caminho e destaca pontos de interesse próximos ao conjurador."
    ));
    public static final SpellDefinition SILENCIO = register(new SpellDefinition(
            "caosarcana:silencio", "Silêncio", "ilusao", "Ilusão", 15, 24.0f, 240, 0, 0,
            "Abafa a presença do conjurador, dificultando que criaturas o percebam."
    ));
    public static final SpellDefinition FURIA = register(new SpellDefinition(
            "caosarcana:furia", "Fúria", "ilusao", "Ilusão", 30, 35.0f, 300, 0, 10.0,
            "Instiga criaturas próximas a entrarem em combate contra o conjurador."
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
            30.0f,
            240, // 12 segundos de recarga (evita spam contínuo)
            0,
            0,
            "Converte energia arcana pura em vitalidade regenerativa, restaurando 6 pontos de vida (3 corações) do conjurador. Recarga: 12 segundos."
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
            "Comando diplomático e autoridade versátil dos Humanos que desarma agressões, acalma multidões e traz respeito.",
            Race.HUMANO
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
            "Explosão marcial dos Redguards que restaura o fôlego e acelera o ritmo de combate e movimentação.",
            Race.REDGUARD
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
