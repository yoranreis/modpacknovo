# Proposta de Protótipo Futuro: Árvore de Especialização Arcana

> **Status atual (31/10/2026):** O primeiro protótipo da árvore de Piromancia foi implementado. Parte da jornada foi verificada no Lote B; a medição numérica dos focos continua pendente.  
> **Data:** 28/10/2026  
> **Escopo histórico:** Este documento registra a proposta arquitetural antes da implementação. A integração atual usa *Pufferfish’s Skills* como dependência opcional e isolada; consulte `docs/collaboration/TASKS.md` para o estado e as verificações atuais. As observações de licença e API abaixo descrevem a investigação da versão 0.19.2.

---

### 1. Comprovação da Versão, Código-Fonte e Licença

#### 1.1 Metadados da Versão Candidata
* **Mod:** Pufferfish's Skills (`puffish_skills`)
* **Versão Avaliada:** `0.19.2` para Minecraft 1.20.1 Fabric
* **Arquivo JAR Oficial:** `puffish_skills-0.19.2-1.20-fabric.jar` (Tamanho: 636.935 bytes)
* **Hash SHA-512 Oficial:**  
  `5e78f6ec5b084833248dea113f8081e8cc4700e0fd97a4a00820f23ea0e5db6c1705f0fbdb904658279f716b34d8eede3ae7e6ad6c008b4ff277a65fa3e423d0`
* **Links Oficiais:**
  - [Modrinth - Projeto](https://modrinth.com/mod/skills)
  - [Modrinth - Versão 0.19.2](https://modrinth.com/mod/skills/version/0.19.2)
  - [Repositório GitHub (pufmat/skillsmod)](https://github.com/pufmat/skillsmod)

#### 1.2 Vínculo com o Código-Fonte e Limitação de Tags
* **Ramo de Desenvolvimento:** `1.20`
* **Commit Correspondente:** `6ce660ad050a343d83ec40505db1fbb31668ae59` (08/10/2024 - *"Bump version"*).  
  *Evidência de correspondência 1:1:* Este commit consolida as alterações de `22068bda` (*Prefix injected methods*), `d8dd9199` (*Use mixin accessors*) e `b9d144c5` (*Avoid possible concurrent modification errors*), que formam o changelog exato publicado na versão `0.19.2` do Modrinth.
* **Limitação Registrada:** O autor do mod **não utiliza Git Tags** em seu repositório GitHub (a consulta ao endpoint de tags retorna vazia). O vínculo foi estabelecido via inspeção do histórico de commits do ramo `1.20` e cruzamento direto de data, arquivos alterados e changelog oficial.

#### 1.3 Licenciamento e Restrições de Redistribuição
* O código em `LICENSE.txt` é **All Rights Reserved** (direitos autorais reservados a Pufferfish).
* Os recursos visuais em `LICENSE-RESOURCES.txt` são licenciados sob **MIT**.
* **Cláusula Restritiva de Distribuição (Modrinth):**
  > *"## Modpacks*  
  > *You can use this mod in your modpack as long as you do not redistribute this mod."*
* **Fundamento Legal/Técnico:** A distribuição padrão através de modpacks em plataformas oficiais (CurseForge/Modrinth) é permitida, pois o inicializador oficial faz o download do binário diretamente dos servidores de borda (CDN) do Modrinth. É vedada a redistribuição direta do arquivo JAR binário em launchers proprietários ou servidores espelho (*mirrors*) sem anuência formal do autor.

---

### 2. Mapeamento de APIs e Dependência de Implementação (Mixin)

#### 2.1 Métodos e Eventos Auditados no Commit `6ce660a`
* `net.puffish.skillsmod.api.SkillsAPI`:
  - `registerSkillUnlockEvent(Events.SkillUnlock event)`: `void onSkillUnlock(ServerPlayerEntity, Identifier, String)` (`SkillsAPI.java:23`). **Evento de pós-desbloqueio (notificação)**.
  - `registerSkillLockEvent(Events.SkillLock event)`: `void onSkillLock(ServerPlayerEntity, Identifier, String)` (`SkillsAPI.java:27`).
  - `registerReward(Identifier key, RewardFactory factory)`: (`SkillsAPI.java:35`).
  - `getCategory(Identifier categoryId)`: retorna `Optional<Category>` (`SkillsAPI.java:82`).
* `net.puffish.skillsmod.api.Category`:
  - `streamUnlockedSkills(ServerPlayerEntity player)`: retorna `Stream<Skill>` (`Category.java:24`).
  - `resetSkills(ServerPlayerEntity player)`: tranca nós e estorna pontos (`Category.java:28`).
  - `addPoints(ServerPlayerEntity, Identifier, int)` / `setPoints(...)`: manipulação de saldo (`Category.java:40`).
* `net.puffish.skillsmod.api.Skill`:
  - `getState(ServerPlayerEntity player)`: retorna `Skill.State` (`LOCKED`, `AVAILABLE`, `AFFORDABLE`, `UNLOCKED`, `EXCLUDED`) (`Skill.java:12`).

#### 2.2 Proposta de Interceptação Prévia via Mixin (Pendente de Teste Real)
* **Limitação da API Nativa:** O evento `Events.SkillUnlock` é despachado **depois** que o Pufferfish desconta os pontos e aplica as recompensas em `SkillsMod.java:388-394`. Utilizá-lo para impedir desbloqueios exigiria rollback reativo (desfazer compra), violando a premissa de segurança do projeto.
* **Proposta Arquitetural (NÃO implementada nesta etapa):**  
  Planeja-se para um protótipo futuro injetar um Mixin no início do processamento de compra do mod externo:
  ```java
  // PROPOSTA FUTURA - NÃO APLICADA NO CÓDIGO ATUAL
  @Inject(method = "tryUnlockSkill", at = @At("HEAD"), cancellable = true)
  private void caosarcana$interceptPreUnlock(ServerPlayerEntity player, Identifier categoryId, String skillId, boolean force, CallbackInfo ci) {
      if (!CaosArcanaTalentBridge.canUnlock(player, categoryId, skillId)) {
          ci.cancel(); // Bloqueia antes de gastar pontos, sem aplicar recompensas
      }
  }
  ```
* **Ressalva:** Esta injeção é uma dependência de implementação interna atrelada à estrutura privada da versão `0.19.2`. Permanece estritamente como especificação técnica teórica, **ainda pendente de prototipagem e teste real de compatibilidade**.

---

### 3. Preservação da Arquitetura do RPG

#### 3.1 Fonte Única da Verdade e Separação de Dados
Para eliminar redundâncias e impedir dessincronização:
* **Caos Arcana (`rpg_player_data` no NBT do jogador):**
  - **Nível Geral:** Escala oficial de **1 a 100** (intocada).
  - **Proficiência das Escolas de Magia:** Escala oficial de **1 a 100** (a menção anterior a 1..10 foi puramente didática; a escala master de 1 a 100 permanece soberana).
  - **Conhecimento Permanente de Magias:** Lista `caosarcana$knownSpells` aprendida via Grimórios e Provações Arcanas. Grimórios continuam sendo o único meio de aprender magias básicas.
  - **Execução e Dano:** Lógica de conjuração, custo de mana e projéteis.
* **Pufferfish's Skills (`puffish_skills.dat` via `PersistentState` do mundo):**
  - Responsável unicamente pela **camada de apresentação visual (GUI)**, coordenadas na tela e armazenamento dos nós ativados (`UNLOCKED` / `EXCLUDED`).
  - As engenharias nativas de experiência e troca (`experience` e `exchange`) do mod externo serão desativadas na configuração da categoria.

#### 3.2 Concessão Idempotente de Pontos de Especialização
* Para impedir que mortes, reconexões ou reinicializações do servidor gerem pontos duplicados ou perdas de saldo:
  - O Caos Arcana manterá em `rpg_player_data` um registro de controle com os marcos de progressão já resgatados pelo personagem (ex: `ClaimedTalentMilestones: ["general_lvl_5", "piro_lvl_20"]`).
  - O servidor somente injetará pontos na categoria do Pufferfish via `category.addPoints(...)` se o marco correspondente ainda não tiver sido concedido.
  - O processo é estritamente idempotente: a checagem é executada no carregamento do jogador e cada patamar de nível geral ou proficiência recompensa exatamente uma única vez.

---

### 4. Proposta de Protótipo Futuro Enxuto (Especialização da Centelha)

Nenhum número de balanceamento (percentuais de dano, custos exatos de mana ou tempos em segundos) está aprovado nesta etapa. O primeiro protótipo técnico é reduzido a **uma raiz** e **duas alternativas mutuamente exclusivas**, modificando exclusivamente o comportamento da magia já aprendida (`Centelha Ígnea`):

```
                   [caosarcana:piro_raiz_afinidade]
                     (Nó Raiz: Afinidade Básica)
                                  |
              +-------------------+-------------------+
              | (Conexão Mutuamente Exclusiva - OR)   |
              v                                       v
[caosarcana:piro_foco_combustao]         [caosarcana:piro_foco_cinetico]
    (Alternativa A: Queimadura)              (Alternativa B: Impacto)
```

1. **Nó Raiz: `caosarcana:piro_raiz_afinidade`**
   - *Pré-requisito:* Ter aprendido a magia `Centelha Ígnea` através do Grimório de Piromancia.
   - *Efeito pretendido:* Estabilização de conjuração (redução de custo de mana da Centelha).
2. **Alternativa A: `caosarcana:piro_foco_combustao`**
   - *Pré-requisito:* Conexão com o Nó Raiz.
   - *Exclusividade:* Mutuamente exclusivo com a Alternativa B.
   - *Efeito pretendido:* Foco em dano prolongado. A Centelha aumenta a duração das chamas infligidas ao alvo, sem alterar o impacto inicial.
3. **Alternativa B: `caosarcana:piro_foco_cinetico`**
   - *Pré-requisito:* Conexão com o Nó Raiz.
   - *Exclusividade:* Mutuamente exclusivo com a Alternativa A.
   - *Efeito pretendido:* Foco em ruptura tática. A Centelha suprime o fogo residual contínuo em troca de maior impacto imediato e recuo (*knockback*) direcional no alvo.
* **Exclusões Proposais:** Estão formalmente descartadas do protótipo inicial explosões em área, disparo duplo de projéteis, perfuração de paredes/escudos ou concessão de novos feitiços ativos.

---

### 5. Cálculo Dinâmico e Preservação de Grimórios

* **Imutabilidade do Registro Mestre:** A definição base `SpellDefinition` da Centelha Ígnea no `SpellRegistry` permanece um `record` imutável e intocado na memória.
* **Cálculo por Jogador:** As propriedades do feitiço são computadas em tempo de execução no instante do disparo, lendo os nós ativos daquele `ServerPlayer` específico.
* **Redistribuição (*Respec*):**
  - A execução de `/puffish_skills skills reset <player> <category>` bloqueia os nós e devolve os pontos alocados.
  - Os modificadores de combustão ou impacto deixam de ser aplicados no disparo.
  - O feitiço `caosarcana:centelha_ignea` **permanece na lista de magias conhecidas** do jogador, garantindo que o aprendizado do grimório nunca seja perdido.

---

### 6. Comportamento Comprovado vs. Política Própria

| Aspecto | Comportamento Comprovado no Código 0.19.2 | Política que Teremos de Implementar |
| :--- | :--- | :--- |
| **Persistência em Respawn** | Comprovado: `eraseOnDeath` é `false` por padrão em `GeneralConfig`, preservando dados após a morte. | Garantir a idempotência na concessão de pontos via `rpg_player_data`. |
| **Comando de Reset** | Comprovado: `/puffish_skills skills reset <player> <category>` estorna pontos e tranca os nós. | Implementar comando integrado ou diálogo de NPC seguro para acionar o reset no servidor. |
| **Remoção de Nós em Atualizações** | Parcialmente comprovado: Nós removidos do JSON desaparecem da interface, mas chaves antigas podem restar no NBT sem estorno automático. | **Obrigatório implementar rotina de migração:** Se nós forem alterados ou removidos em patches futuros, o Caos Arcana deve executar um reset e reconciliação dos pontos. |

---

### 7. Conclusão da Proposta

1. **Viabilidade:** A integração do Pufferfish’s Skills como camada de visualização é viável e recomendada para poupar meses de desenvolvimento de GUI.
2. **Limitações Críticas:** Licença fechada (*All Rights Reserved* para binários), ausência de pré-cancelamento nativo na API oficial (exige Mixin em `tryUnlockSkill`) e ausência de migração automática nativa de pontos para nós removidos.
3. **Menor Experimento Necessário:** Uma prova de conceito futura isolada no ambiente de testes com um Data Pack contendo a categoria `piromancia` e os 3 nós descritos, validando se o Mixin impede a compra não autorizada sem descontar pontos.

---
*Fim da proposta original de 28/10/2026. Atualização de 31/10/2026: o protótipo inicial foi implementado; conclusões desta seção sobre uma prova de conceito futura são históricas. A homologação numérica dos efeitos permanece pendente.*
