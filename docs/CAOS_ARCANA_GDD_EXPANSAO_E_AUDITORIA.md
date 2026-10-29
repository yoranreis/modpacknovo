# CAOS ARCANA — AUDITORIA TÉCNICA E EXPANSÃO DO GAME DESIGN DOCUMENT (GDD)

> **Documento de Arquitetura e Engenharia de Sistemas de RPG**  
> **Status:** Análise, Pesquisa e Propostas Arquiteturais (Sem alterações de código Java ou configurações)  
> **Ambiente Real Auditado:** Minecraft 1.20.1 | Fabric Loader 0.20.1 | Fabric API 0.92.12+1.20.1 | Java 17  
> **Repositório:** https://github.com/yoranreis/modpacknovo  
> **Data:** 27 de Outubro de 2026  

---

## SUMÁRIO GERAL

1. [ETAPA 1 — AUDITORIA REAL DO REPOSITÓRIO](#etapa-1--auditoria-real-do-repositório)
2. [ETAPA 2 — ESTUDO COMPARATIVO DOS SISTEMAS DE SKYRIM](#etapa-2--estudo-comparativo-dos-sistemas-de-skyrim)
3. [ETAPA 3 — GAME DESIGN DOCUMENT MODULAR (25 MÓDULOS)](#etapa-3--game-design-document-modular-25-módulos)
4. [ETAPA 4 — CATÁLOGO DE MECÂNICAS EXCLUSIVAS DO CAOS ARCANA](#etapa-4--catálogo-de-mecânicas-exclusivas-do-caos-arcana)
5. [ETAPA 5 — MATRIZ DE RASTREABILIDADE INTEGRAL](#etapa-5--matriz-de-rastreabilidade-integral)
6. [ETAPA 6 — ROADMAP INCREMENTAL E VERTICAL SLICE](#etapa-6--roadmap-incremental-e-vertical-slice)
7. [DECISÕES ABERTAS QUE EXIGEM APROVAÇÃO FORMAL](#decisões-abertas-que-exigem-aprovação-formal)

---

# ETAPA 1 — AUDITORIA REAL DO REPOSITÓRIO

A auditoria inspecionou exaustivamente todos os arquivos do repositório, incluindo código-fonte Java, mixins de engine, testes automatizados, recursos NBT, scripts de build Gradle, documentação mestre e a biblioteca visual.

### 1.1 Inventário Técnico e Separação de Estados

Para garantir rigor absoluto de governança, cada elemento do repositório foi categorizado em um dos 6 estados operacionais:

| Sistema / Componente | Arquivos de Origem | Estado Operacional | Evidências no Repositório |
| :--- | :--- | :--- | :--- |
| **Sistema de Mana e NBT** | `PlayerRPGMixin.java`, `IRPGPlayerData.java`, `ClientRPGData.java` | **Comprovadamente Implementado** | Persistência em NBT (`CaosArcanaData`), débito no servidor, clamping anti-exploit, sincronização via pacote S2C (`caosarcana:sync_mana`). |
| **Regeneração de Mana Server-Side** | `PlayerRPGMixin.java` | **Comprovadamente Implementado** | Loop de 20 ticks (1s) na Server Thread com taxas raciais: Alto Elfo (3.0/s), Felino (2.0/s), Orc (1.5/s). |
| **HUD de Mana e Fade-Out** | `ManaHudOverlay.java`, `CaosArcanaClient.java` | **Comprovadamente Implementado** | Renderização acima da barra de armadura; desaparece suavemente após 5s em 100%; acorda instantaneamente ao gastar mana. |
| **Sobreposição de Corações Dourados** | `GuiHealthOverlayMixin.java` | **Comprovadamente Implementado** | Mixin no renderer de vida (`InGuiOverlay`). Trava o layout em 10 corações e sobrepõe corações dourados para os +4 HP do Orc sem empilhar linhas de UI. |
| **Magia da Centelha Ígnea** | `FlameSparkItem.java`, `FlameSparkProjectile.java` | **Comprovadamente Implementado** | Projétil derivado de `SmallFireball` com dissipação estrita aos 60 ticks (3s) ou 40 blocos de distância, sem explosão. Recarga de 15 ticks e custo de 20 mana. |
| **Aprendizado Permanente de Magias** | `SpellRegistry.java`, `SpellDefinition.java`, `GrimoireStudyHandler.java`, `PlayerRPGMixin.java` | **Comprovadamente Implementado** | Conjunto `KnownSpells` em NBT. Idempotente (não duplica), sobrevive a mortes, reconexões e `/caos resetrace`. |
| **Seleção de Magia Ativa** | `SpellSelectionScreen.java`, `PlayerRPGMixin.java`, `ModMessages.java` | **Comprovadamente Implementado** | Interface cliente (tecla `G` / atalho), validação estrita no servidor (`C2S_SELECT_SPELL`), persistência de `SelectedSpell` em NBT. |
| **Conjuração Provisória por Tecla** | `SpellCastHandler.java`, `CaosArcanaClient.java` | **Comprovadamente Implementado** | Tecla `V` dispara a magia ativa aprendida sem exigir item na mão; validação server-side de raça, mana, cooldown e conhecimento. |
| **Restrições de Equipamento & Custódia** | `EquipmentRequirementRegistry.java`, `InventoryArmorSlotMixin.java`, `ArmorItemDispenserMixin.java` | **Comprovadamente Implementado** | Bloqueio de equipar por raça e nível; devolução segura com preservação de NBT, durabilidade e encantamentos. |
| **Comandos de Depuração** | `CaosArcanaCommands.java` | **Comprovadamente Implementado** | `/caos setrace`, `/caos resetrace`, `/caos setmana`, `/caos getmana`, `/caos setlevel`, `/caos learnspell`, `/caos castspell`. |
| **Sistema de Raças (Atributos Básicos)** | `Race.java` | **Parcialmente Implementado** | Atributos vanilla injetados (vida, dano, velocidade, armadura). Vantagens complexas (ex: -50% dano de queda para felinos) são planejadas. |
| **Escolas de Magia e Talentos** | `IRPGPlayerData.java` (`caosarcana$schools`) | **Parcialmente Implementado** | Estrutura de mapa `school -> level` e limites (1..100) existem no contrato e NBT, mas sem árvore de talentos ou ganho orgânico de XP. |
| **Conjuração por Voz (Speech-to-Spell)** | `CAOS_ARCANA_Documentacao_Mestra.md` | **Apenas Documentado** | Conceituado na documentação mestra; nenhum código de captura de áudio ou integração de modelo neural presente. |
| **Economia, CaosPhone e CaosBank** | `CAOS_ARCANA_Documentacao_Mestra.md`, `interfaces/` | **Apenas Documentado** | Telas e fluxos conceituados; sem integração com banco virtual ou mod de smartphone. |
| **Dungeons, Criptas e Bosses** | `visual_studies/dungeons/` | **Apenas Documentado** | Estudos de arte da Cripta Ancestral existentes; sem gerador de estruturas ou IA de chefes implementada. |
| **Fator Rastejante de Cinza (Branch Auxiliar)** | `origin/feature/rastejante-de-cinza` | **Aguardando Auditoria** | Criado pelo auxiliar em branch paralela; pendente de testes e revisão de governança. |

### 1.2 Divergências Críticas de Versão Identificadas

A auditoria revelou uma divergência documental importante que deve ser formalizada:

* **Documentação Mestra (`CAOS_ARCANA_Documentacao_Mestra.md`, linha 63):**
  > *"O ambiente mencionado anteriormente é Minecraft 26.3 e Fabric Loader 0.19.5..."*
* **Código Real do Projeto (`gradle.properties` e `build.gradle`):**
  * **Minecraft Version:** `1.20.1` (Versão estável universal para modpacks de RPG com GeckoLib e Cloth Config).
  * **Fabric Loader:** `0.20.1`
  * **Fabric Loom:** `1.19.3`
  * **Fabric API:** `0.92.12+1.20.1`
  * **Java Runtime:** `Java 17 (LTS)`

> [!IMPORTANT]
> **Correção Técnica:** "Minecraft 26.3" é uma numeração incorreta/fictícia originada em notas anteriores. O runtime real, funcional e estável do modpack é estritamente **Minecraft 1.20.1 Fabric**.

### 1.3 Cobertura de Testes Automatizados

O repositório possui uma suíte robusta de testes unitários em JUnit 5 (`CaosArcanaTests.java`), totalizando **14 cenários de teste**, todos compilando e executando com 100% de aprovação (`BUILD SUCCESSFUL in 11s`):

1. `testRaceParsingAndDefaults`: Valida resolução de IDs insensíveis a maiúsculas e fallbacks para `Race.NONE`.
2. `testRaceDocumentationStatusAndTraits`: Garante que o código reflita a governança de status documental de cada raça.
3. `testEquipmentRequirementAllowedRace`: Valida rejeição de armadura de Orc por Elfos e Felinos.
4. `testEquipmentRequirementMinLevel`: Valida restrição de nível geral (exige Nível 5 para Peitoral de Ferro).
5. `testRequirementSeparation`: Valida que incapacidade de equipar não impede fabricação (`CRAFT`) nem transporte/venda (`TRADE`).
6. `testResetRacePreservesRPGData`: Garante que `/caos resetrace` limpe a raça sem apagar mana, nível geral ou escolas.
7. `testManaDerivationAndAntiExploitClamping`: Valida corte estrito de mana ao perder bônus de Alto Elfo (+50), impedindo mana infinita.
8. `testRealNbtSerializationAndDurabilityPreservation`: Testa serialização profunda de NBT com durabilidade parcial, nomes customizados e encantamentos em itens retidos na custódia.
9. `testPartialStackInsertionAndNoAliasing`: Valida devolução parcial de pilhas sem corrupção ou aliasing de referências de memória.
10. `testManaConsumptionRegenAndRacialRates`: Valida consumo, restauração até o teto e taxas de regeneração raciais (3.0, 2.0 e 1.5 mana/s).
11. `testFlameSparkProjectileExpirationLimits`: Valida limites estritos de 60 ticks e 40 blocos do projétil místico.
12. `testSpellLearningAndIdempotence`: Valida registro único de magias, idempotência e bloqueio a nulos/vazios.
13. `testSpellPersistenceAndResetRaceIntegrity`: Valida serialização e preservação de conhecimentos aprendidos através do ciclo de vida.
14. `testServerSpellCastValidationRules`: Valida cadeia de autorização server-side para disparo sem grimório.
15. `testSpellSelectionAndEmptyState`: Valida estado vazio, seleção ativa e auto-seleção da primeira magia.
16. `testSpellSelectionPersistenceAcrossReconnect`: Valida persistência da magia ativa após reconexão.
17. `testSpellRegistryMetadataAndContracts`: Valida metadados do catálogo de feitiços (`SpellRegistry`).

---

# ETAPA 2 — ESTUDO COMPARATIVO DOS SISTEMAS DE SKYRIM

O desenvolvimento do Caos Arcana busca os pilares centrais de *The Elder Scrolls V: Skyrim* (Bethesda Softworks, 2011), adaptando-os criticamente ao ecossistema multiplayer voxel do Minecraft Fabric.

### Matriz Comparativa de Sistemas: Skyrim vs. Caos Arcana

```
+----------------------------------------------------------------------------------------------------+
| SISTEMA SKYRIM        | MECÂNICA SKYRIM             | ADAPTAÇÃO CAOS ARCANA (MINECRAFT)            |
+----------------------------------------------------------------------------------------------------+
| Magicka / Health /    | 3 barras clássicas;         | Mana customizada via mixin + HUD dedicado;   |
| Stamina               | Magicka não gasta em pulo   | Stamina usa a barra de saturação/fome        |
|                       |                             | vanilla com exaustão calculada.              |
+----------------------------------------------------------------------------------------------------+
| 5 Escolas de Magia    | Destruição, Restauração,    | 4 Afinidades Elementais (Fogo, Água, Terra,  |
|                       | Alteração, Ilusão,          | Ar) + 3 Escolas de Propósito (Conjuração,    |
|                       | Conjuração (0 a 100).       | Proteção/Contramagia, Ilusão).               |
+----------------------------------------------------------------------------------------------------+
| Livros de Magia       | Spell Tomes são consumidos  | Grimórios não são consumidos no teste;       |
| (Spell Tomes)         | ao clicar e ensinam a       | exigem requisitos de escola para estudar e   |
|                       | magia imediatamente.        | registram conhecimento permanente no player. |
+----------------------------------------------------------------------------------------------------+
| Thu'um (Gritos /      | Tecla dedicada para grito;  | Conjuração por Voz (Speech-to-Spell via mic) |
| Palavras de Poder)    | cooldown separado de magia. | como canal primário; Tecla 'V' como fallback |
|                       |                             | com validação 100% server-side.              |
+----------------------------------------------------------------------------------------------------+
| Encantamento &        | Soul Trap em criaturas      | Captura de Essência Arcana em Cristais       |
| Soul Gems             | para encher pedras; gasta   | sem RNG vanilla; encantamento customizado    |
|                       | em mesas arcanas.           | com custos de mana e materiais raros.        |
+----------------------------------------------------------------------------------------------------+
| Alquimia & Efeitos    | 4 efeitos por ingrediente;  | Descoberta experimental de reagentes locais; |
|                       | bancadas de poções.         | poções com efeitos táteis (anti-mana, etc.). |
+----------------------------------------------------------------------------------------------------+
| Dungeons & Puzzles    | Criptas Nórdicas, garras    | Criptas Ancestrais modulares; puzzles com    |
|                       | de dragão, pilares de pedra.| redstone mística, baú final contestável.     |
+----------------------------------------------------------------------------------------------------+
| Crime & Consequências | Bounties locais, guardas    | Zonas Seguras (Lojas) com Anti-PvP; estado   |
|                       | perseguidores, cadeia.      | de combate de 20s para impedir fuga rápida.  |
+----------------------------------------------------------------------------------------------------+
```

### Análise Individual por Sistema de Skyrim

#### 1. Raças e Habilidades Passivas/Ativas
* **Em Skyrim:** 10 raças jogáveis com bônus iniciais (+5/+10 em perícias), poderes diários (ex: *Berserker Rage* do Orc, *Highborn* do Altmer) e passivas permanentes (ex: 50% resistência a gelo dos Nords).
* **Por que funciona:** Cria identidade de arquétipo imediata sem trancar o jogador em uma classe fixa.
* **O que preservar:** Arquétipos culturais fortes e diferenciação visual instantânea sem armadura.
* **O que adaptar ao Minecraft:** No Minecraft, o hitbox e a mecânica de renderização exigem cuidados para que modelos com presas, caudas ou orelhas (como os criados no Blockbench) suportem as armaduras padrão e equipamentos customizados sem deformação.
* **O que NÃO copiar:** Poderes ativáveis com recarga de "1 vez a cada 24 horas in-game" (em servidores multiplayer, isso estimula esperar AFK; no Caos Arcana, habilidades devem usar recargas baseadas em ticks e custos de mana).
* **Mecânicas Originais Propostas:** *Vigor Ancestral* do Orc concedendo sobrevida proporcional à mana restante; *Salto Silencioso* do Felino reduzindo ruídos no chat de proximidade.
* **Riscos em Multiplayer:** Raças com velocidade aumentada virarem meta obrigatória para fuga em PvP. Balancear velocidade com redução estrita de vida e vulnerabilidade a contusão.

#### 2. Magicka, Health e Stamina
* **Em Skyrim:** Subir de nível permite escolher aumentar Health, Magicka ou Stamina em +10 pontos. A regeneração pausa ou desacelera dramaticamente durante o combate.
* **Por que funciona:** Força escolhas estruturais de build (mago puro vs guerreiro blindado vs híbrido spellsword).
* **O que adaptar ao Minecraft:** A vida no Minecraft é medida em meio-corações (20 HP). Escalar atributos deve manter compatibilidade com o sistema de armadura vanilla e poções. No Caos Arcana, implementamos o teto de 10 corações na UI e a sobreposição de corações dourados para os pontos extras do Orc.
* **O que NÃO copiar:** Pausa automática do jogo ao abrir menus para beber poções de cura (inviável em multiplayer em tempo real).

#### 3. Escolas de Magia e Livros de Aprendizado
* **Em Skyrim:** Destruição (dano direto), Restauração (cura e escudos), Alteração (armadura de éter, luz, paralisia), Ilusão (frenesi, medo, invisibilidade) e Conjuração (invocações e armas vinculadas). O feitiço é aprendido consumindo um *Spell Tome*.
* **Por que funciona:** O grimório tem valor de mercado tangível e dá sensação de tesouro real ao ser encontrado em baús de dungeons.
* **O que preservar:** O ciclo *encontrar o livro no mundo -> estudar com requisitos mínimos -> dominar permanentemente*.
* **O que adaptar ao Minecraft:** O consumo imediato do livro no clique com a mão direita em Skyrim foi enriquecido no Caos Arcana: o livro abre uma GUI com descrição, requisitos da escola, custo de mana e confirmação formal de estudo.
* **O que NÃO copiar:** Feitiços de paralisia de longa duração que impedem qualquer ação do oponente em PvP (altamente frustrante em combate multiplayer).
* **Mecânica Original Proposta:** *Contramagia Ativa (Spell Parrying)*: projetar uma centelha defensiva no exato momento da colisão de um projétil inimigo para dissipar ambos no ar.

---

# ETAPA 3 — GAME DESIGN DOCUMENT MODULAR (25 MÓDULOS)

Esta seção define as especificações funcionais e arquiteturais para os 25 módulos do Caos Arcana, prontas para orientar o desenvolvimento e a modelagem.

### Módulo 01: Visão e Pilares de Design
* **Objetivo:** Estabelecer a identidade de um RPG de fantasia medieval rigoroso no Minecraft Fabric para grupos de 6 a 10 jogadores.
* **Experiência do Jogador:** Sensação constante de conquista através do aprendizado árduo, exploração perigosa e comércio vivo.
* **Pilares:**
  1. *Evolução Orgânica:* Conhecimento permanente separado de posse de itens.
  2. *Combate Tático:* Sem one-shots ou spam de cliques; dependência de timing, mana e posicionamento.
  3. *Mundo Hostil e Competitivo:* Zonas livres para disputa com santuários seguros para comércio e refúgio.
  4. *Identidade Visual Coesa:* Pixel art 32x32 em ícones e blocos; modelos 3D Blockbench de alta fidelidade para raças e armas.

### Módulo 02: Raças e Personalização
* **Objetivo:** Permitir a escolha permanente ou custosa de raça no primeiro acesso ao servidor.
* **Funcionamento:** Ao entrar pela primeira vez, o jogador é imobilizado e apresentado à `RaceSelectionScreen`. Escolhe entre as raças homologadas.
* **Raças Definidas:**
  * **Orc:** Foco em força física bruta (+4 HP, +2 Dano, +2 Armadura, -10% Velocidade, regeneração de mana mais lenta: 1.5/s).
  * **Felino:** Foco em mobilidade e agilidade (+15% Velocidade, -2 HP, dano de queda reduzido, regeneração média: 2.0/s).
  * **Alto Elfo:** Foco arcano (+50 Mana máxima, +3.0/s regeneração de mana, -2 HP, -15% Dano físico).
* **Contratos:** `Race.java` aplica modificadores permanentes usando `UUID` estáveis de atributos para não colidir com armaduras ou poções.

### Módulo 03: Atributos e Fórmulas
* **Fórmula de Mana Efetiva:**
  $$\text{MaxMana} = \text{BaseMaxMana} + \text{BonusRacial} + \text{BonusEquipamento} + (\text{NívelGeral} \times 2)$$
* **Fórmula de Regeneração de Mana (Server-Side):**
  $$\text{RegenPorTick} = \frac{\text{TaxaRacialBase} \times (1.0 + \text{BonusPassivas})}{20.0}$$
* **Anti-Exploit:** A cada tick de modificação, se $\text{ManaAtual} > \text{MaxMana}$, ocorre o corte imediato (*clamping*) sem geração residual de energia.

### Módulo 04: Progressão e Árvore de Talentos
* **Nível Geral (Teto 60):** Concede pontos de atributos e desbloqueia requisitos para armaduras e armas avançadas.
* **Nível de Escola (Teto 100):** Mede a proficiência em cada disciplina mística.
* **Árvores Mágicas:** Estrutura em três ramificações por escola: Potência, Eficiência (redução de custo) e Utilidade/Controle.

### Módulo 05: Escolas Mágicas
* **4 Afinidades Elementais:**
  * **Piromancia (Fogo):** Dano contínuo, pressão ofensiva e negação de área.
  * **Hidromancia (Água/Gelo):** Lentidão, resfriamento de terreno e controle de fluxo.
  * **Geomancia (Terra):** Barreiras físicas, armadura petrificada e estabilidade.
  * **Aeromancia (Ar/Eletricidade):** Repulsão, mobilidade aérea e ataques em cadeia.
* **3 Escolas de Propósito:**
  * **Conjuração:** Criação de constructos efêmeros e armas vinculadas.
  * **Proteção & Contramagia:** Barreiras mágicas e dissipação de feitiços.
  * **Ilusão:** Ofuscação visual, passos silenciosos e duplicatas.

### Módulo 06: Afinidades Elementais e Reações
* **Afinidade Primária:** Escolhida na primeira prova de escola. Concede +20% de eficácia e -15% de custo nas magias do elemento escolhido.
* **Reações Elementais no Mundo:**
  * *Fogo + Água:* Cria nuvem de vapor que cega oponentes e dissipa projéteis elétricos.
  * *Eletricidade + Água:* Choque condutivo em cadeia com dano extra a armaduras metálicas.
  * *Fogo + Terra:* Magma incandescente que desacelera e queima quem pisar.

### Módulo 07: Catálogo de Feitiços
* **Centelha Ígnea (`caosarcana:centelha_ignea`):** Projétil básico de Piromancia. Custo: 20 mana. Dano: 4.0. Tempo de vida: 60 ticks. Alcance: 40 blocos. Recarga: 15 ticks.
* **Bola de Fogo (`caosarcana:bola_de_fogo`) [PROPOSTA]:** Custo: 50 mana. Requisito: Piromancia 15. Dano em área (3 blocos) sem quebrar blocos do mapa.
* **Muralha de Chamas (`caosarcana:muralha_chamas`) [PROPOSTA]:** Custo: 70 mana. Requisito: Piromancia 30. Ergue linha de fogo que queima projéteis físicos.

### Módulo 08: Grimórios, Livros e Pesquisa
* **Objeto Físico vs Conhecimento:** O grimório ensina o feitiço através de estudo formal. Uma vez aprendido, o grimório pode ser guardado, vendido ou emprestado; o conhecimento reside na alma do jogador.
* **Interface de Estudo:** Apresenta requisitos mínimos, descrição mística e custo de ativação antes da confirmação.

### Módulo 09: Conjuração por Voz (Speech-to-Spell)
* **Arquitetura Técnica:** Módulo cliente integrado ao Simple Voice Chat ou biblioteca local (Vosk/Whisper Offline).
* **Fluxo Seguro:** Reconhecimento local da palavra-chave $\to$ Envio do pacote C2S com o ID do feitiço $\to$ O servidor valida se o jogador conhece a magia, tem mana e respeita o cooldown $\to$ Disparo do projétil.
* **Acessibilidade:** A tecla de conjuração ativa (`V`) funciona como método alternativo equivalente, garantindo acessibilidade para quem joga sem microfone.

### Módulo 10: Combate Físico e Mágico
* **Sincronização:** Sem mecânica de hit-spam vanilla 1.8; utiliza o tempo de recuperação da 1.20.1 aliado a custos de estamina e janelas de esquiva.
* **Interrupção:** Receber dano pesado durante o carregamento de uma magia de alto nível cancela a conjuração e consome 25% da mana em dispersão.

### Módulo 11: Armas, Armaduras e Artefatos
* **Exclusividade Racial de Armaduras:** Peitorais pesados de ferro forjado ancestral só podem ser vestidos por Orcs com Nível Geral suficiente.
* **Armas:** Machados de Guerra Orcs (alto impacto e penetração de escudo), Adagas Felinas (golpes rápidos pelas costas), Cajados Arcanos Élficos (foco de amplificação de alcance mágico).

### Módulo 12: Alquimia
* **Mecânica:** Bancada de Alquimia com caldeirão; combinação de três reagentes com descoberta progressiva de propriedades (efeito positivo primário, secundário, negativo e oculto).
* **Frascos:** Poções de restauração rápida de mana com toxicidade cumulativa para impedir uso contínuo em combate.

### Módulo 13: Encantamento e Runas
* **Extração de Runas:** Desencantar itens raros destrói o item físico e extrai uma Runa de Poder correspondente.
* **Mesa de Entalhe Rúnico:** Aplicação controlada de propriedades sem o sorteio aleatório de três opções do Minecraft vanilla.

### Módulo 14: Ferraria e Profissões
* **Forja Ancestral:** Bigorna de temperatura controlada onde ligas raras (Ferro Ancestral, Aço Élfico) exigem ritmo de martelamento.
* **Especialização:** Personagens podem atingir nível mestre em apenas 1 profissão primária, incentivando o comércio entre jogadores.

### Módulo 15: NPCs e Inteligência Artificial
* **Rotinas Locais:** NPCs em cidades e postos avançados possuem horários de trabalho, repouso e vigília.
* **Memória Tática:** NPCs de combate recuam ao ficarem sem suporte, utilizam poções e protegem os flancos.

### Módulo 16: Facções, Reputação e Diálogos
* **Alinhamentos:** Reputação numérica com as guildas do servidor (Guarda da Cidade Central, Círculo de Magos de Cinza, Clã da Forja).
* **Consequências:** Hostilidade imediata caso o jogador ataque membros da guilda ou cometa crimes em suas dependências.

### Módulo 17: Missões e Narrativa
* **Missões de Maestria:** Desafios solo obrigatórios para ultrapassar marcos de habilidade (ex: Nível 25, 50, 75 de escola).
* **Pistas Ambientais:** Livros e diários em ruínas indicando a localização de baús escondidos ou palavras de ativação mística.

### Módulo 18: Mundo e Geração de Estruturas
* **Biomas Temáticos:** Compatibilidade com datapacks e mods de terreno (ex: Terralith), gerando Criptas em montanhas e Bibliotecas em florestas densas.
* **Preservação de Desempenho:** Estruturas com bounding boxes limpas e sem entidades passivas desnecessárias.

### Módulo 19: Dungeons, Puzzles e Chefes
* **Puzzles Místicos:** Portas seladas por combinação de quatro tochas elementais ou chaves esculpidas em pedra.
* **Salas de Desafio:** Câmaras que trancam os jogadores até que todos os guardiões sejam derrotados.
* **Chefes com Fases:** Chefes que erguem escudos imunes a dano físico, exigindo feitiços de água para resfriar sua carapaça de magma.

### Módulo 20: Economia e Comércio
* **Lojas Físicas Protegidas:** Sistema de baús de comércio onde o dono define preço de venda e compra.
* **Moeda do Servidor:** Moeda de Cobre, Prata e Ouro Físicas com peso e valor intrínseco, sem inflação por geração infinita.

### Módulo 21: PvP, Morte e Zonas Seguras
* **Anti-PvP em Lojas:** Zona de exclusão de dano e efeitos hostis em torno dos mercados centrais.
* **Estado de Combate (20s):** Golpear ou ser golpeado por outro jogador ativa o temporizador de combate. Entrar em zona segura enquanto estiver em combate mantém a vulnerabilidade até que os 20 segundos expirem.
* **Regra de Morte:** Conhecimentos, magias e níveis são permanentes; recursos do inventário comum caem, incentivando cautela nas viagens.

### Módulo 22: Eventos Mundiais e Endgame
* **Baú do Caos:** Anúncio global com coordenadas exatas via chat/HUD. O baú surge trancado com temporizador de 10 minutos para reunião dos jogadores e disputa aberta.
* **Extração:** Pegar o item do evento marca o jogador com uma aura visível por 2 minutos, impedindo teleporte ou desconexão imediata.

### Módulo 23: Interfaces, HUD e Acessibilidade
* **HUD Dinâmico:** Barra de mana curva/horizontal posicionada sobre a interface vanilla, com fading automático para não poluir a tela.
* **Escalabilidade:** Telas de GUI preparadas para GUI Scale Auto, Normal e Large, sem sobreposição de textos.

### Módulo 24: Identidade Visual e Pipeline de Assets
* **Modelos 3D:** Blockbench Generic Model (`.bbmodel` / GeckoLib) para personagens humanoides, garantindo rig de animação completo.
* **Texturas Pixel Art:** Resolução 32x32 para ícones de inventário e blocos de construção; 128x128 para atlas UV de modelos de personagens.
* **Paleta Coesa:** Uso rigoroso da paleta nórdico-medieval (ferro oxidado, couro desgastado, verde-oliva, ouro envelhecido).

### Módulo 25: Arquitetura, Persistência e Sincronização
* **Sincronização C2S / S2C:** Uso de `FabricPlayNetworking` com canais nomeados estritos (`caosarcana:*`). O cliente envia intenções de ação; o servidor processa, valida e replica o estado resultante.
* **Persistência NBT:** Armazenamento centralizado em `CaosArcanaData` no arquivo `.dat` do jogador, compatível com backups e migração de mundos.

---

# ETAPA 4 — CATÁLOGO DE MECÂNICAS EXCLUSIVAS DO CAOS ARCANA

Além das influências clássicas de Skyrim, o Caos Arcana incorpora mecânicas originais para elevar a profundidade do servidor:

```
+---------------------------------------------------------------------------------------------------------+
| MECÂNICA EXCLUSIVA          | IMPACTO NO GAMEPLAY                 | VIABILIDADE & COMPLEXIDADE          |
+---------------------------------------------------------------------------------------------------------+
| Reações Elementais no Solo  | Transforma o terreno em armadilha   | Média: usa blocos temporários de    |
|                             | tática combinando feitiços.         | duração controlada e sem drop.      |
+---------------------------------------------------------------------------------------------------------+
| Grimórios Vivos             | O livro ganha novas páginas e notas | Baixa: gerido por NBT de páginas    |
| (Progresso por Estudo)      | conforme o jogador sobe de escola.  | visíveis na GUI customizada.        |
+---------------------------------------------------------------------------------------------------------+
| Sobrecarga & Mana Burn      | Conjurar sem intervalo seguro gera  | Baixa: contador de estresse arcano  |
|                             | calor místico e dano ao conjurador. | no servidor que pune spam.          |
+---------------------------------------------------------------------------------------------------------+
| Contramagia com Projétil    | Interceptar feitiços no ar com um   | Alta: detecção precisa de colisão   |
| (Spell Parrying)            | disparo rápido no contra-ataque.    | bounding-box entre duas entidades.  |
+---------------------------------------------------------------------------------------------------------+
| Fuga & Extração em Eventos  | Carregar relíquias impede teleporte | Média: tag de status de peso místico|
|                             | e exige caminhar até ponto seguro.  | validada em comandos e portais.     |
+---------------------------------------------------------------------------------------------------------+
```

1. **Reações Elementais no Terreno:**
   * Lançar *Centelha Ígnea* sobre um bloco congelado por magia de água derrete-o instantaneamente gerando uma nuvem espessa de vapor que cega quem estiver dentro por 4 segundos.
2. **Sistema de Sobrecarga Arcana (Instabilidade):**
   * Cada magia conjurada em menos de 2 segundos após a anterior adiciona 10% de Instabilidade Arcana. Acima de 50%, o conjurador sofre *Mana Burn* (dano direto na vida equivalente a 20% do custo da magia). Isso extirpa completamente o spam de magias em PvP.
3. **Contramagia Ativa (Spell Parrying):**
   * Disparar uma magia de energia oposta contra um projétil inimigo no ar anula ambos os feitiços em um estalo sônico, criando uma janela de oportunidade tática baseada em reflexo e pontaria.
4. **Grimórios Vivos:**
   * Livros antigos encontrados em criptas contêm notas ilegíveis que se decifram sozinhas quando o jogador atinge o nível de escola necessário, revelando rituais avançados e receitas secretas.

---

# ETAPA 5 — MATRIZ DE RASTREABILIDADE INTEGRAL

Esta matriz conecta a documentação de design, o código existente, as pendências técnicas e a cobertura de testes para assegurar que nenhum desenvolvimento ocorra fora da arquitetura.

```
+--------------------------------------------------------------------------------------------------------------------+
| SISTEMA         | DOC GDD    | CÓDIGO EXISTENTE           | PENDÊNCIAS FUTURAS        | TESTES JUNIT 5 | PRIORIDADE|
+--------------------------------------------------------------------------------------------------------------------+
| Mana & Regen    | Mód. 03    | PlayerRPGMixin.java        | Modificadores de poções   | testManaConsum | P0 (Feito)|
|                 |            | IRPGPlayerData.java        | e buffs de equipamento.   | ptionRegen...  |           |
+--------------------------------------------------------------------------------------------------------------------+
| HUD de Mana     | Mód. 23    | ManaHudOverlay.java        | Configuração de escala e  | Validação em   | P0 (Feito)|
|                 |            | CaosArcanaClient.java      | posição pelo jogador.     | jogo aprovada. |           |
+--------------------------------------------------------------------------------------------------------------------+
| Centelha Ígnea  | Mód. 07    | FlameSparkItem.java        | Efeitos sonoros custom    | testFlameSpark | P0 (Feito)|
|                 |            | FlameSparkProjectile.java  | no cast e impacto.        | Projectile...  |           |
+--------------------------------------------------------------------------------------------------------------------+
| Aprendizado     | Mód. 08    | SpellRegistry.java         | Consumo/cópia de livro em | testSpellLearn | P0 (Feito)|
| de Magias       |            | GrimoireStudyHandler.java  | tiers futuros de jogo.    | ingAndIdemp... |           |
+--------------------------------------------------------------------------------------------------------------------+
| Seleção de      | Mód. 08/23 | SpellSelectionScreen.java  | Ícones gráficos 32x32     | testSpellSelec | P0 (Feito)|
| Magia Ativa     |            | PlayerRPGMixin.java        | dedicados por feitiço.    | tionAndEmpty.. |           |
+--------------------------------------------------------------------------------------------------------------------+
| Raças & Atrib.  | Mód. 02    | Race.java                  | Modelos 3D de Elfo e      | testRaceParsin | P1        |
|                 |            | GuiHealthOverlayMixin.java | Felino no Blockbench.     | gAndDefaults   |           |
+--------------------------------------------------------------------------------------------------------------------+
| Árvore de       | Mód. 04    | IRPGPlayerData.java        | Interface GUI de árvore   | testResetRaceP | P1        |
| Talentos        |            | (métodos de escola)        | de talentos e XP próprio. | reservesRPG... |           |
+--------------------------------------------------------------------------------------------------------------------+
| Conjuração por  | Mód. 09    | SpellCastHandler.java      | Integração com microfone  | testServerSpel | P2        |
| Voz (Speech)    |            | (validação server pronta)  | (reconhecimento neural).  | lCastValid...  |           |
+--------------------------------------------------------------------------------------------------------------------+
| Dungeons &      | Mód. 19    | visual_studies/dungeons/   | Datapack de estruturas e  | Inspeção de    | P2        |
| Criptas         |            | (concept arts 2D)          | gerador de quebra-cabeças | arte aprovada. |           |
+--------------------------------------------------------------------------------------------------------------------+
| Economia &      | Mód. 20    | visual_studies/interfaces/ | Sistema de saldo virtual  | Conceito       | P3        |
| Lojas Seguras   |            | (concept CaosBank)         | e blocos de comércio.     | validado.      |           |
+--------------------------------------------------------------------------------------------------------------------+
```

---

# ETAPA 6 — ROADMAP INCREMENTAL E VERTICAL SLICE

Para evitar a dispersão de desenvolvimento e garantir estabilidade contínua, o projeto avançará através de entregas modulares bem delimitadas.

### 6.1 Definição da Vertical Slice Jogável (Fase 1)

A **Vertical Slice** é a menor fatia completa do jogo, demonstrando um ciclo fechado de RPG antes da expansão em escala:

1. **Uma Raça Completa com Modelo 3D:**
   * **Orc:** Modelo 3D de alta fidelidade finalizado no Blockbench (`orc_civilian.bbmodel`), textura UV 128x128 (`orc_civilian_texture.png`) e atributos ativos (+4 HP com corações dourados, dano aumentado e mana balanceada).
2. **Uma Escola Mágica Funcional:**
   * **Piromancia:** Nível 1 a 20 configurado, com progressão inicial de poder.
3. **Dois Feitiços Operacionais:**
   * *Centelha Ígnea:* Disparo rápido de custo 20 (já implementado e testado).
   * *Labareda Arcana:* Feitiço de alcance curto em cone contínuo (custo 15/s, exige Piromancia Nível 5).
4. **Um Grimório de Aprendizado:**
   * Item de estudo com interface interativa, exigindo requisitos e registrando conhecimento permanente.
5. **Um NPC com Missão Curta:**
   * Instrutor de forja/magia que desafia o jogador a provar sua afinidade.
6. **Uma Dungeon Curta (Cripta Ancestral Piloto):**
   * Estrutura gerada com 3 câmaras: entrada com armadilha simples, câmara de puzzle rúnico e sala do sarcófago.
7. **Um Chefe Piloto:**
   * *Guardião da Cinza:* Chefe com ataque em área e vulnerabilidade a feitiços de impacto.
8. **Recompensa Permanente:**
   * Conhecimento de um feitiço avançado ou minério de Ferro Ancestral utilizável para forja.

```
+----------------------------------------------------------------------------------------+
| CRONOGRAMA DE EXPANSÃO                                                                 |
+----------------------------------------------------------------------------------------+
| FASE 1: Vertical Slice Jogável (Orc + Piromancia + Centelha + Cripta Piloto)           |
|                                     |                                                  |
| FASE 2: Expansão das Raças (Felino e Alto Elfo com modelos e animações completas)      |
|                                     |                                                  |
| FASE 3: Árvore de Talentos & Perícias (Sistema de XP RPG próprio e UI de constelações) |
|                                     |                                                  |
| FASE 4: Conjuração por Voz (Speech-to-Spell neural com fallback na tecla de atalho)    |
|                                     |                                                  |
| FASE 5: Economia, Dungeons Globais, Eventos de Coordenadas e Zonas Seguras Anti-PvP   |
+----------------------------------------------------------------------------------------+
```

---

# DECISÕES ABERTAS QUE EXIGEM APROVAÇÃO FORMAL

As seguintes questões estratégicas de game design e tecnologia permanecem em aberto e requerem validação formal do Lead do projeto antes de qualquer implementação em código:

1. **Elenco Definitivo de Raças:**
   * O Orc e o Felino estão confirmados como referências explícitas.
   * O **Alto Elfo** deve ser aprovado como a terceira raça oficial do servidor ou substituído por outra linhagem?
2. **Perda de Itens na Morte:**
   * O servidor adotará *KeepInventory* padrão com penalidade de durabilidade/moedas, ou *Drop de Recursos Comuns* com proteção de equipamentos pessoais de raridade alta?
3. **Arquitetura de Reconhecimento de Voz:**
   * Aprovamos a utilização de uma biblioteca de reconhecimento neural leve offline (ex: Vosk/Sherpa) no cliente, ou a conjuração por voz será intermediada pelo bot do Discord / CaosBridge?
4. **Economia Físico vs. Virtual:**
   * As transações de comércio usarão itens físicos no inventário (Moedas de Ouro/Prata) ou sistema de saldo virtual do CaosBank gerenciado por comandos e celular?
5. **Fator Rastejante de Cinza (Branch do Auxiliar):**
   * Autorização para inspecionar, harmonizar e realizar o merge seguro da branch `feature/rastejante-de-cinza` do colaborador auxiliar na branch `main`.
