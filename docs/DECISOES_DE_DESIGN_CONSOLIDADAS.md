# Caos Arcana — Resumo Executivo das Decisões de Design (27/10/2026)

Este documento consolida as diretrizes estratégicas e parâmetros fundamentais definidos e aprovados pelo usuário para orientar a arquitetura e desenvolvimento contínuo do modpack e do mod Caos Arcana.

---

## 1. Elenco Completo de Raças Jogáveis (Base Skyrim Adaptada)

O Caos Arcana implementará oficialmente as **10 raças jogáveis clássicas de Skyrim**, mantendo arquétipos balanceados, poderes únicos e desvantagens táticas claras no Minecraft Fabric:

| Raça | Linhagem / Arquétipo | Poder Ativo / Habilidade Racial | Vantagens Permanentes | Desvantagens Táticas |
| :--- | :--- | :--- | :--- | :--- |
| **Orc (Orsimer)** | Guerreiro de Impacto e Forja | *Ira Berserker:* Reduz dano recebido em 50% e aumenta dano físico por 10s. | +4 HP (Corações Dourados), +2 Dano físico, +2 Armadura. | -10% Velocidade, regeneração de mana mais lenta (1.5/s). |
| **Felino (Khajiit)** | Batedor e Assassino Ágil | *Olhos da Noite:* Visão noturna ativável sem poção. | +15% Velocidade, -50% Dano de queda, Ataque desarmado com garras. | -2 HP (Vida menor contra contusão pesada). |
| **Alto Elfo (Altmer)** | Conjurador Místico Nato | *Nobreza Arcana:* Regeneração de mana acelerada temporária. | +50 Mana Máxima, regeneração de mana rápida (3.0/s). | -2 HP, -15% Dano de ataque corpo a corpo. |
| **Nórdico (Nord)** | Guerreiro Resistente do Gelo | *Grito de Batalha:* Repele e desorienta inimigos próximos. | +50% Resistência a feitiços de gelo/água, bônus com armas pesadas. | Custo aumentado de magias de fogo. |
| **Elfo Negro (Dunmer)** | Mago de Guerra e Fogo | *Ira do Ancestral:* Envolve-se em fogo místico temporário. | +50% Resistência a fogo/queimaduras, bônus em Piromancia. | Vulnerabilidade aumentada contra dano de gelo. |
| **Elfo da Floresta (Bosmer)**| Arqueiro Furtivo e Rastreador | *Comunhão Selvagem:* Acalma feras e mobs selvagens. | +50% Resistência a veneno/doenças, velocidade de saque de arco +25%. | Menor resistência a magias de destruição. |
| **Bretão (Breton)** | Híbrido Arcano Defensivo | *Pele de Dragão:* Absorve parte da mana de feitiços inimigos recebidos. | +25% Resistência mágica permanente a todas as escolas. | Menor afinidade com combate pesado/armaduras maciças. |
| **Imperial** | Diplomata e Soldado Tático | *Voz do Imperador:* Acalma alvos hostis em raio curto. | Encontra mais moedas em baús de dungeons, bônus de bloqueio com escudo. | Sem bônus direto de dano bruto ou mana máxima. |
| **Guarda Vermelho (Redguard)**| Duelista Marcial e Estamina | *Ímpeto de Adrenalina:* Recuperação instantânea de estamina/fome. | +50% Resistência a veneno, velocidade de ataque corpo a corpo +10%. | Menor aptidão para escolas de Ilusão e Conjuração. |
| **Argoniano (Reptiliano)** | Anfíbio e Sobrevivência | *Pele de Hist:* Regeneração rápida de vida em momentos críticos. | Respiração aquática permanente, nado veloz, imunidade a afogamento. | Vulnerabilidade severa ao frio e feitiços de gelo. |

---

## 2. Catálogo Extensivo de Feitiços (Base Skyrim)

O catálogo de magias adotará a progressão consolidada das 5 escolas de Skyrim, adaptada à mana e ao HUD do Caos Arcana:

* **Destruição (Piromancia, Criomancia, Eletromancia):**
  * *Novato:* Centelha Ígnea (Fogo), Dardo de Gelo (Gelo), Faísca (Raio).
  * *Aprendiz:* Bola de Fogo (Projétil em área), Espiga de Gelo (Projétil penetrante), Relâmpago (Ataque direto rápido).
  * *Adepto:* Manto de Chamas (Aura ofensiva), Muralha de Chamas/Gelo (Negação de área), Corrente de Raios (Ricochete).
  * *Especialista:* Incinerar (Dano maciço), Nevasca (Vórtice contínuo), Tempestade de Raios (Feixe contínuo).
  * *Mestre:* Tempestade de Fogo, Nevasca Máxima, Tempestade Elétrica Global.
* **Restauração:**
  * Cura básica, Cura Rápida, Círculo de Proteção (repele mortos-vivos), Barreira Menor/Maior (bloqueio frontal de feitiços).
* **Alteração:**
  * Armadura de Carvalho/Pedra/Ferro/Ébano (adiciona armadura natural), Luz Arcana, Telecinese, Respiração Aquática, Paralisia de Toque.
* **Conjuração:**
  * Espada Vinculada, Machado Vinculado, Arco Vinculado, Invocar Familiar, Reanimar Morto-Vivo, Invocar Attronach de Chamas/Gelo.
* **Ilusão:**
  * Clarividência (guia de caminho), Silêncio (passos sem som), Fúria (provoca mobs), Calma, Invisibilidade.

---

## 3. Economia de Moedas Físicas Próprias

Para evitar que minérios abundantes do Minecraft (como ferro, cobre vanilla e ouro vanilla de mineração) destruam o valor da moeda:

* **Moedas Cunhadas Próprias (Itens Não Mineráveis):**
  * `caosarcana:moeda_cobre` (Moeda de Cobre Antiga / Bronze) — Unidade base (1x).
  * `caosarcana:moeda_prata` (Moeda de Prata Imperial) — Vale 100 Moedas de Cobre.
  * `caosarcana:moeda_ouro` (Moeda de Ouro Arcana / Septim) — Vale 100 Moedas de Prata (10.000 Cobres).
* **Origem da Moeda:**
  * Encontradas exclusivamente em baús de dungeons, recompensas de missões com NPCs, eventos mundiais disputados e derrotando chefes.
  * Impossível forjar moedas apenas fundindo minério de ferro/cobre comum, preservando a raridade econômica e estabilidade de mercado.

---

## 4. Mecânica de Morte: Queda de Itens com Recuperação

* **Regra de Morte:**
  * Conhecimentos de magias, escolas, nível geral e raça **são permanentes e nunca são perdidos**.
  * Ao morrer, os itens do inventário comum e equipamentos caem no local exato do óbito.
  * O jogador pode retornar ao local para recuperar seus pertences.
  * Em áreas de PvP ou dungeons competitivas, outros jogadores podem disputar ou resgatar o espólio.

---

## 5. Governança e Trabalho em Conjunto com o Desenvolvedor Auxiliar

* **Harmonização de Branches:**
  * O colaborador auxiliar está desenvolvendo com sucesso a esteira de mobs em `feature/rastejante-de-cinza` e `feat/floresta-negra-guardiao` (Terras da Cinza: *Rastejante*, *Cão Fumegante*, *Mineiro Oco*, *Vigia das Brasas*, *Carapaça Basáltica*, *Colosso Basáltico*; Floresta Negra: *Guardião de Madeira Negra*).
  * O código do auxiliar respeita o isolamento de pacotes (`common.entity.*`, `client.model.*`, `client.renderer.*`).
  * Não sobrescreveremos, descartaremos ou alteraremos as entidades e renderers criados por ele.
  * A integração na `main` será realizada de forma harmoniosa, mantendo ambos os núcleos (Sistemas de RPG/Mana/Magias + Bestiário/Inimigos) plenamente sincronizados.

---

## 6. Arquitetura em Duas Camadas de Magia: Poderes Raciais vs. Magias Universais

Implementado e validado em código e testes unitários:

1. **Poderes Raciais Específicos (1 por Raça):**
   * Desbloqueados automaticamente quando o jogador confirma sua raça ou entra no mundo.
   * Não podem ser aprendidos ou conjurados por outras raças (`SpellDefinition.isRacial() == true`).
   * Aplicam efeitos ativos de alta fidelidade:
     * **Orc:** *Ira Berserker* (`caosarcana:ira_berserker`) -> Resistência II + Força II por 15s.
     * **Khajiit (Felino):** *Olhos da Noite* (`caosarcana:olhos_da_noite`) -> Visão Noturna por 60s.
     * **Alto Elfo:** *Nobreza Arcana* (`caosarcana:nobreza_arcana`) -> +40 Mana instantâneo + regeneração.
     * **Nórdico:** *Grito de Batalha* (`caosarcana:grito_de_batalha`) -> Onda de choque sonora (repulsão em 8 blocos + fraqueza/lentidão).
     * **Elfo Negro:** *Ira Ancestral* (`caosarcana:ira_ancestral`) -> Tempestade de fogo em volta (incendeia e danifica inimigos em 5 blocos).
     * **Elfo da Floresta:** *Comunhão Selvagem* (`caosarcana:comunhao_selvagem`) -> Pacifica feras e mobs hostis em 12 blocos.
     * **Bretão:** *Pele de Dragão* (`caosarcana:pele_de_dragao`) -> Absorção de dano II e restauração de 25 mana.
     * **Imperial:** *Voz do Imperador* (`caosarcana:voz_do_imperador`) -> Pacifica alvos em 10 blocos e concede Herói da Vila II.
     * **Guarda Vermelho:** *Ímpeto de Adrenalina* (`caosarcana:impeto_de_adrenalina`) -> Restaura comida/estamina + Velocidade II e Pressa II.
     * **Argoniano:** *Pele de Hist* (`caosarcana:pele_de_hist`) -> Regeneração III por 10s + Absorção.

2. **Magias Universais e Formas de Obtenção:**
   * Acessíveis a qualquer raça mediante estudo, mérito ou exploração (`isRacial() == false`).
   * **Grimórios em Dungeons:** Gerados proceduralmente nos baús de masmorras vanilla (Simple Dungeon, Abandoned Mineshaft, Nether Bridge, Stronghold, Ancient City) via `DungeonLootHandler`.
   * **Provações Arcanas (Arcane Trials):** Desafios temáticos por escola através de `ArcaneTrialRegistry`:
     * *Provação das Chamas* -> `caosarcana:centelha_ignea` (Piromancia)
     * *Provação da Geada Eterna* -> `caosarcana:geada_congelante` (Criomancia)
     * *Provação dos Céus Eletrizados* -> `caosarcana:relampago_arcano` (Eletromancia)
     * *Provação do Toque Restaurador* -> `caosarcana:toque_de_cura` (Restauração)
     * *Provação do Círculo Sagrado* -> `caosarcana:circulo_sagrado` (Restauração)
     * *Provação da Casca de Carvalho* -> `caosarcana:corpo_de_carvalho` (Alteração)
     * *Provação da Luz Guia* -> `caosarcana:luz_de_vela` (Alteração)
     * *Provação da Lâmina Espectral* -> `caosarcana:espada_vinculada` (Conjuração)
     * *Provação do Espírito Familiar* -> `caosarcana:familiar_espectral` (Conjuração)
     * *Provação do Véu das Sombras* -> `caosarcana:manto_de_sombras` (Ilusão)
   * Comandos de auditoria e mestres: `/caos provacao listar`, `/caos provacao concluir <id>`, `/caos learn <spell_id>`, `/caos spells`.
