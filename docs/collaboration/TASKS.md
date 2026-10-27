# Quadro de Tarefas Colaborativas (TASKS.md)

Este quadro rastreia as tarefas em andamento, disponíveis e concluídas no projeto.

---

## 1. Em Execução / Próximas

### [TASK-004] Revisão e Integração do Rastejante de Cinza (Auxiliar)
* **Status:** Aguardando auditoria e integração formal.
* **Origem:** Branch remota `origin/feature/rastejante-de-cinza` enviada pelo colaborador auxiliar.
* **Escopo:** Revisão de compatibilidade em `CaosArcana.java`, `CaosArcanaClient.java` e `lang/*.json` antes do merge na `main`.

---

## 2. Disponíveis para o Desenvolvedor Auxiliar

### [TASK-002] Assets e Texturas das Raças & Ícones
* **Responsável:** Desenvolvedor Auxiliar.
* **Branch Sugerida:** `feat/assets-racas-icones`
* **Escopo:**
  * Criação ou adaptação de ícones de itens e texturas de blocos a partir de `visual_studies/icones/`.
  * Conversão de estudos conceituais para arquivos `.png` 32x32 em `src/main/resources/assets/caosarcana/textures/item/`.
* **Arquivos Autorizados:** Apenas dentro de `src/main/resources/assets/caosarcana/` e `visual_studies/`.

### [TASK-003] Efeitos Sonoros e Identidade de Áudio dos Feitiços
* **Responsável:** Desenvolvedor Auxiliar.
* **Branch Sugerida:** `feat/sounds-spells`
* **Escopo:**
  * Mapeamento de sons para `sounds.json` (cast de fogo, cast de gelo, cura, falha de mana).
* **Arquivos Autorizados:** `src/main/resources/assets/caosarcana/sounds.json` e arquivos de áudio em `sounds/`.

---

## 3. Concluídas

### [CONCLUÍDA] TASK-001: Sistema de Mana, HUD Dinâmico, Centelha Ígnea e Corações Dourados
* **Responsável:** Desenvolvedor Principal.
* **Validação em Jogo:** Aprovada integralmente em 26/10/2026.
* **Entregas Realizadas:**
  * **Mana & Regeneração:** Débito de 20 de mana por conjuração; bloqueio sem saldo; regeneração de 1s na Server Thread (Alto Elfo 3.0/s, Felino 2.0/s, Orc 1.5/s).
  * **HUD de Mana (Opção D):** Posicionado acima da saúde/armadura; fade-out após 5 segundos em 100%; despertar imediato ao gastar.
  * **Corações Dourados Sobrepostos:** Layout travado em linha única de 10 corações; vida extra do Orc (+4 HP) sobrepõe em dourado sobre os corações vermelhos sem subir linha.
  * **Centelha Ígnea & Projétil:** Projétil próprio (`FlameSparkProjectile`) com encerramento explícito no primeiro dos limites: 60 ticks (3s) ou 40 blocos da origem, dissipando sem explosão ou dano residual.
* **Parâmetros Provisórios de Balanceamento:**
  * Custo da Centelha: 20 de mana.
  * Recarga do item: 15 ticks (0.75s).
  * Tempo de vida do projétil: 60 ticks.
  * Distância máxima do projétil: 40 blocos.
* **Pendências Futuras Mapeadas (Não iniciadas):**
  * Aprendizado permanente por estudo em grimórios.
  * Árvore de talentos e progressão de escolas de magia.
  * Conjuração por voz (Speech-to-Spell).

* `[CONCLUÍDA]` **TASK-000:** Sistema Central de Raças, Restrições de Equipamento e Custódia Segura com NBT.
  * Validação formal em jogo aprovada em 26/10/2026.
  * 9 testes automatizados cobrindo requisitos, atributos e devolução íntegra.
