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


---

## 7. Decisão Oficial: Aprovação Exclusiva do Novo Elenco de 10 Raças (28/10/2026)

Em 28/10/2026, o usuário formalizou a **aprovação exclusiva do novo elenco de 10 raças jogáveis** para o Caos Arcana:

1. **Humano** (anteriormente Imperial)
2. **Elfo da Floresta** (Bosmer)
3. **Alto Elfo** (Altmer)
4. **Orc** (Orsimer)
5. **Felino** (Khajiit)
6. **Nórdico** (Nord)
7. **Sangue-Daedra** (Dremora — substitui o Bretão)
8. **Redguard** (anteriormente Guarda Vermelho)
9. **Argoniano**
10. **Voidmer** (Elfo do Vazio — substitui o Elfo Negro / Dunmer)

### 7.1 Delimitação Estrita do Escopo Aprovado
Esta aprovação restringe-se **estritamente à composição e nomenclatura das 10 raças**. Ficam explicitamente **fora desta aprovação e pendentes de decisões futuras independentes**:
* Novos atributos e modificadores numéricos de atributos;
* Conjunto de 3 habilidades ativas por raça;
* Exceção de bypass do Humano (*Versatilidade* para Tiers 1–3);
* Escala física de entidades, dimensões de hitbox e altura de câmera;
* Neutralidade pacífica com mobs planares (Nether / The End);
* Vulnerabilidades elementais ou ambientais (água, calor, frio);
* Estrutura de 4 Tiers de equipamentos e bônus de conjunto;
* Sistema de vinculação Soulbound.

### 7.2 Planejamento de Compatibilidade e Migração de Saves
Nenhuma alteração de código ou modificação de arquivos de save foi executada nesta etapa. O planejamento arquitetural estabelece:

1. **Renomeações Transparentes (`imperial` → `humano` e `guarda_vermelho` → `redguard`):**
   * Os métodos de desserialização de NBT (`PlayerRPGMixin.readCustomDataFromTag`) e os comandos administrativos aceitarão os IDs antigos como sinônimos permanentes.
   * O personagem mantém todos os seus dados sem qualquer interrupção, migrando silenciosamente para a nova chave.
2. **Substituições Temáticas (`bretao` e `elfo_negro`):**
   * **Proibição de Conversão Compulsória:** Um jogador previamente registrado como Bretão não será forçado a se tornar Sangue-Daedra; um jogador registrado como Dunmer não será forçado a se tornar Voidmer.
   * **Mecanismo de Re-escolha Livre:** No primeiro carregamento pós-migração, o jogador com `bretao` ou `elfo_negro` terá o status de raça marcada como pendente de confirmação (`caosarcana$hasChosenRace = false`), abrindo automaticamente a interface oficial de seleção (`OPEN_RACE_SELECT_ID`) para que escolha livremente qualquer uma das 10 raças do novo elenco.
3. **Preservação de Dados do Personagem:**
   * **Conhecimento de Magias:** Todas as magias universais aprendidas através de Grimórios ou Provações Arcanas permanecem intocadas em `caosarcana$knownSpells`.
   * **Níveis e Proficiências:** O Nível Geral e as proficiências nas Escolas de Magia (escala oficial de 1 a 100) permanecem 100% inalterados.
   * **Inventário e Recargas:** O conteúdo dos inventários e os tempos de recarga ativos de feitiços são preservados.
4. **Tratamento de Poderes Raciais Antigos e Bloqueio Preventivo:**
   * **Bloqueio Imediato de Poderes:** Personagens aguardando nova seleção racial (`hasChosenRace == false` / `Race.NONE`) ficam **terminantemente impedidos de ativar poderes da raça antiga** enquanto a nova escolha não for formalizada. O vínculo ao feitiço racial anterior é neutralizado imediatamente no carregamento da migração, sem conceder nenhum poder provisório ou indevido.
   * **Preservação Integral de Dados:** O sistema preserva todos os dados necessários para concluir a migração sem qualquer perda (Nível Geral, proficiências em todas as Escolas de Magia, magias universais em `caosarcana$knownSpells`, inventários, tempos de recarga e itens na fila de custódia segura).
   * **Atribuição da Nova Linhagem:** No momento em que o jogador confirma sua nova raça na tela de seleção, o feitiço racial correspondente à nova linhagem é então atribuído de forma definitiva.
5. **Tratamento de Equipamentos com Requisitos Violados:**
   * Caso a mudança de raça faça com que peças de armadura atualmente equipadas violem os requisitos raciais de uso, o sistema utilizará a infraestrutura existente de **Custódia Segura** (`EquipmentValidationHandler.validateAndEnforce` / `caosarcana$addPendingDelivery`).
   * A peça é desequipada de forma segura para o inventário; se o inventário estiver lotado, é enviada para a lista de custódia protegida do jogador. Nenhum item é dropado no chão, deletado ou transferido para terceiros.

---

## 8. Planejamento Arquitetural de Soulbound (Decisão Desacoplada e Identificação de Termos)

O conceito de Soulbound permanece em fase de planejamento conceitual e **não está aprovado nem implementado**. A pedido da governança do projeto, seus quatro eixos ortogonais e termos técnicos são explicitamente diferenciados entre **propostas conceituais** e **elementos comprovados no código atual**:

1. **Vínculo ao Jogador (*Proposta Conceitual: `PlayerBinding`*):**
   * *Status:* **Proposta de design** (não existe classe ou interface `PlayerBinding` no código Java atual).
   * *Conceito:* Registro do UUID do proprietário legítimo na tag NBT do item (`SoulboundOwnerUUID`). Determina a titularidade pessoal do item (ex: forjado pelo jogador ou recebido como recompensa mística).
2. **Restrição Racial (`RaceRequirement`):**
   * *Status:* Mecânica já existente e ativa no `EquipmentRequirementRegistry` (suporte a requisitos de raça via `Set<Race> allowedRaces`).
   * *Conceito:* Requisito genérico de linhagem biológica ou cultural gerenciado pelo `EquipmentRequirementRegistry` (ex: espada forjada para a fisiologia de um Orc). Independe da titularidade pessoal.
3. **Possibilidade de Comércio e Transferência (*Proposta: `Tradeability`* / *Comprovado: `RequirementType.TRADE`*):**
   * *Status:* O termo conceitual `Tradeability` é uma **proposta de design**. No entanto, a constante **`RequirementType.TRADE` está comprovada no código atual**, declarada formalmente no enum [`br.com.caosarcana.common.equipment.RequirementType`](file:///c:/Users/Matt/Desktop/modpacknovo/caos-arcana-mod/src/main/java/br/com/caosarcana/common/equipment/RequirementType.java#L6).
   * *Conceito:* Avaliado via `RequirementType.TRADE`. Um item com restrição de comércio não pode ser vendido em mercados de vilas, transferido para baús públicos de guildas ou negociado em janelas de troca direta entre jogadores.
4. **Comportamento na Morte (*Proposta Conceitual: `DeathBehavior`*):**
   * *Status:* **Proposta de design** (não existe enum ou classe `DeathBehavior` no código Java atual).
   * *Conceito:* Define o destino do item quando o proprietário morre. O comportamento é dissociado do inventário comum (que dropa no chão):
     * *Retenção no Inventário:* O item Soulbound permanece nos slots do jogador ao ressuscitar; ou
     * *Custódia Segura:* O item é recolhido para a fila de entregas pendentes (`PendingDeliveries`) do jogador até que haja espaço para reavê-lo.

---

## 9. Decisão Oficial: Versatilidade do Humano e Restrições de Equipamento (28/10/2026)

Em 28/10/2026, o usuário formalizou a **decisão de design para a Versatilidade do Humano**:

1. **Escopo Estrito do Bypass Racial (T1–T3):**
   * O Humano pode ignorar **exclusivamente** o requisito racial de equipamentos formalmente classificados como Tier 1 (T1), Tier 2 (T2) ou Tier 3 (T3).
2. **Inviolabilidade de Outros Requisitos:**
   * A Versatilidade **não ignora** requisito de Nível Geral, requisito de Proficiência por Escola de Magia, vínculo ao proprietário (Soulbound/Owner UUID), nem quaisquer outros requisitos (ex: tipo de armadura, proficiência em armas ou futuras restrições de classe).
3. **Não Concessão de Poderes ou Conjuntos:**
   * O Humano que equipar uma peça pertencente a outra raça (T1–T3) **não recebe** os poderes raciais intrínsecos nem bônus de conjunto exclusivos daquela raça. A peça fornece unicamente seus atributos materiais básicos e propriedades de proteção/dano genéricas.
4. **Vedação Absoluta ao Tier 4 (T4):**
   * O Humano **não pode usar equipamentos Tier 4 (T4) exclusivos de outras raças** sob nenhuma circunstância. O Tier 4 representa a expressão máxima cultural/sagrada da raça de origem e é estritamente reservado aos membros legítimos daquela linhagem.
5. **Itens Sem Tier Explícito:**
   * Equipamentos que não possuam um Tier explicitamente cadastrado no registro de requisitos **não recebem bypass automático**. Para tais itens, a restrição racial permanece 100% mandatória e inegociável.

### 9.1 Delimitação Estrita do Escopo Aprovado
Esta decisão aprova unicamente a regra de design de *Versatilidade do Humano*. Permanecem **explicitamente não aprovados**:
* Os números, valores de proteção, dano e atributos dos equipamentos;
* O catálogo completo de tiers de equipamentos e listas de peças;
* O sistema de vinculação Soulbound e suas regras definitivas de ciclo de vida.

---

## 10. Planejamento Futuro: Sistema de Dificuldade de Mobs e Interface de Identificação (29/10/2026)

Este tópico registra as diretrizes estratégicas e pontos de decisão futura para a evolução do combate no Caos Arcana. **Nenhuma funcionalidade ou mod desta seção está implementado ou instalado no lote atual**.

### 10.1 Escopo das Novas Mecânicas
1. **Níveis e Variação de Dificuldade:**
   - Aplicável exclusivamente a **mobs hostis**.
   - Fica **terminantemente vedada a aplicação automática em animais, aldeões e criaturas pacíficas**.
2. **Interface de Identificação Visual:**
   - Exibição de HUD/overlay contendo nome, nível e barra/quantidade de vida do mob.
   - Preferencialmente renderizada de forma contextual ao mirar no alvo (crosshair raycast).

### 10.2 Direção Proposta (Sujeita a Balanceamento Futuro)
- **Zoneamento por Bioma/Estrutura:**
  - Superfície inicial delimitada com faixa baixa e teto rígido de dificuldade.
  - Regiões perigosas, dimensões e dungeons com faixas próprias de nível, comunicadas claramente ao jogador ao adentrar a área.
- **Isolamento de Veteranos vs. Iniciantes:**
  - Influência limitada da progressão/nível do jogador na escalada de inimigos da superfície, impedindo que a presença de um veterano torne os arredores do spawn inviáveis para novatos.
- **Tratamento Especial:**
  - Chefes (bosses) e criaturas especiais com tratamento e balanceamento individualizados.
- **Qualidade da Dificuldade:**
  - O aumento de desafio **não dependerá exclusivamente de inflar vida/esponja de dano**; incluirá variações táticas (velocidade, dano, armadura, resistências elementais).
- **Escalonamento Econômico:**
  - Recompensas de loot (moedas, materiais) e ganho de XP devem acompanhar o risco real, contendo farm repetitivo em criaturas triviais.

### 10.3 Decisões Futuras Pendentes de Definição
- **Métrica de Força do Jogador:** Definição de como calcular a força (apenas nível geral, soma de proficiências, média de equipamentos equipados ou combinação ponderada).
- **Grupos Múltiplos:** Regras de escalonamento para grupos cooperativos compostos por jogadores com disparidade de níveis.
- **Atributos Afetados e Limites:** Matriz formal de atributos modificáveis (HP, ataque, armadura, knockback resistance) e seus tetos absolutos.
- **Compatibilidade e Preservação:** Tratamento de entidades pré-existentes no mundo e preservação integral de mecânicas de mobs de outros mods.
- **Integração Sistêmica:** Fórmulas de integração entre nível do mob, rendimento de XP de escolas arcanas, pools de moedas e bosses de mods externos.

### 10.4 Diretriz Metodológica Obrigatória para a Frente
Quando o desenvolvimento deste sistema for formalmente autorizado:
- **Prioridade a Mods Existentes:** Conduzir estudo preliminar de mods consolidados e compatíveis com Minecraft 1.20.1 e Fabric (tanto para escalonamento de dificuldade quanto para HUD/identificação de alvos).
- **Não Reinvenção:** Evitar sistemas caseiros duplicados quando houver solução estável, performática e configurável, garantindo interoperabilidade com o ecossistema Fabric e preservando a IA nativa dos mobs de outros mods.
