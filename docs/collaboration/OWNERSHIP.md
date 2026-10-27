# Governança de Propriedade de Arquivos (OWNERSHIP.md)

Este documento define a propriedade temporária e as responsabilidades sobre os arquivos do projeto para evitar conflitos entre o Desenvolvedor Principal e o Desenvolvedor Auxiliar.

---

## 1. Classificação de Arquivos

### A. Zona Vermelha (Exclusiva do Desenvolvedor Principal)
Arquivos críticos de arquitetura, ciclo de vida e infraestrutura. **O Desenvolvedor Auxiliar não deve modificar estes arquivos sem autorização explícita prévia.**

* `caos-arcana-mod/build.gradle` e `gradle.properties`
* `caos-arcana-mod/src/main/resources/fabric.mod.json`
* `caos-arcana-mod/src/main/resources/caosarcana.mixins.json`
* `br.com.caosarcana.CaosArcana` (ModInitializer)
* `br.com.caosarcana.client.CaosArcanaClient` (ClientModInitializer)
* `br.com.caosarcana.mixin.PlayerRPGMixin`
* `br.com.caosarcana.common.data.IRPGPlayerData`
* `br.com.caosarcana.common.network.ModMessages`
* `br.com.caosarcana.common.command.CaosArcanaCommands`

### B. Zona Amarela (Compartilhada sob Coordenação)
Arquivos que aceitam extensões através de registros formais, mas cujas alterações devem ser notificadas.

* `br.com.caosarcana.common.equipment.EquipmentRequirementRegistry`
* `br.com.caosarcana.common.race.Race`
* `src/test/java/br/com/caosarcana/test/*`

### C. Zona Verde (Livre para o Desenvolvedor Auxiliar em Branches `feat/*`)
Módulos isolados e recursos onde o auxiliar pode criar e desenvolver novas funcionalidades sem risco de quebrar o núcleo do servidor.

* `br.com.caosarcana.content.spells.*` (implementações autônomas de feitiços que consom `IRPGPlayerData`)
* `br.com.caosarcana.content.items.*` (novos itens de RPG, grimórios, cajados, varinhas)
* `src/main/resources/assets/caosarcana/textures/*` (texturas, ícones de itens e blocos)
* `src/main/resources/assets/caosarcana/models/*` (modelos JSON/Blockbench)
* `src/main/resources/assets/caosarcana/sounds.json` e arquivos de áudio
* `visual_studies/*` e `docs/*`

---

## 2. Bloqueio Temporário Atual (Lock de Tarefas)

| Arquivo / Módulo | Responsável Atual | Tarefa Ativa | Estado |
| :--- | :--- | :--- | :--- |
| `PlayerRPGMixin.java` | Dev Principal | TASK-001: Mana & Regeneração | **BLOQUEADO** para o Auxiliar |
| `CaosArcanaClient.java` | Dev Principal | TASK-001: HUD Dinâmico de Mana | **BLOQUEADO** para o Auxiliar |
| `IRPGPlayerData.java` | Dev Principal | TASK-001: Contrato de Mana | **BLOQUEADO** para o Auxiliar |
| `ModMessages.java` | Dev Principal | TASK-001: Sincronização de Mana | **BLOQUEADO** para o Auxiliar |
| `content/spells/` | Aberto | TASK-002: Feitiços Adicionais | **LIVRE** para o Auxiliar |
