# Caos Arcana — guia de passagem para outra IA

Atualizado em 27/10/2026. Este documento resume o projeto e o estado de trabalho para continuidade por outro agente.

## Objetivo do projeto

Mod Fabric de fantasia/RPG para Minecraft, com raças, mana, feitiços, itens e inimigos próprios. O repositório original é [yoranreis/modpacknovo](https://github.com/yoranreis/modpacknovo); o módulo do mod fica em `caos-arcana-mod/`.

## Ambiente e caminhos locais

- Minecraft **1.20.1**, Fabric Loader **0.20.1**, Fabric API **0.92.12+1.20.1**.
- O projeto compila para bytecode Java 17; o JDK local usado nas compilações foi o JDK 25.
- Repositório: `C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo`
- Módulo: `...\modpacknovo\caos-arcana-mod`
- JAR: `build\libs\caosarcana-1.0.0-alpha.jar`
- Pasta de mods do perfil Minecraft: `C:\Users\Matheus\AppData\Roaming\.minecraft\versions\projetinho\mods`
- Comando de compilação no PowerShell:

  ```powershell
  $env:JAVA_HOME = "C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\work\jdk25\jdk-25.0.5.0.2+1"
  Set-Location "C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo\caos-arcana-mod"
  .\gradlew.bat assemble --no-daemon
  ```

- Após compilação bem-sucedida, instalar o JAR:

  ```powershell
  Copy-Item ".\build\libs\caosarcana-1.0.0-alpha.jar" `
    "C:\Users\Matheus\AppData\Roaming\.minecraft\versions\projetinho\mods\caosarcana-1.0.0-alpha.jar" -Force
  ```

  Fechar e reabrir o Minecraft para carregar o JAR atualizado.

## Estado atual do Git

- Branch atual: `feat/floresta-negra-guardiao`.
- Commit atual: `3ab92cd Add Darkwood Guardian for Black Forest`.
- A branch remota existe em `origin/feat/floresta-negra-guardiao`; o trabalho ainda precisa de revisão/PR para integração.
- Essa branch parte de `feature/rastejante-de-cinza`, que contém o trabalho anterior até `4899b0c` (`origin/feature/rastejante-de-cinza`).
- `main` não contém toda a sequência recente de mobs; não fazer checkout/merge para `main` sem buscar e revisar a história.
- Git no Windows pode reclamar de “dubious ownership”. Git incluído no runtime do Codex e exceção por comando:

  ```powershell
  $git = "$env:USERPROFILE\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\git\cmd\git.exe"
  $repo = "C:\Users\Matheus\Documents\Codex\2026-10-26\https-chatgpt-com-codex-desktop-auth\modpacknovo"
  & $git -c "safe.directory=$repo" -C $repo status --short --branch
  ```

- Push autenticado funcionou recentemente com a conta já configurada no ambiente. Não pedir senha/token. Seguir `docs/collaboration/INTEGRATION.md`: trabalhar em `feat/*`, não enviar direto para `main`, não usar `reset --hard`, `push --force` ou `git clean`.
- Existe uma pasta local não rastreada `work/`; não adicionar, apagar ou publicar sem verificar o conteúdo e autorização específica.

## Preferências de colaboração

- Conversar em português brasileiro, de forma direta.
- O usuário quer um commit para cada atualização concluída, compilação do JAR e cópia para a pasta de mods indicada.
- Ele testa no jogo e manda capturas para corrigir aparência, colisão, dano e cadência. Iterar em mudanças pequenas e dizer o que foi alterado e compilado.
- Não afirmar que um mob surge naturalmente em um bioma enquanto geração e regras de spawn não existirem.

## Sistemas e inimigos já implementados

### Base do mod

- Sistema de raças, dados RPG, mana/HUD, validações de equipamento e feitiço Centelha Ígnea já existem.
- `CaosArcanaEntities.java` registra tipos e atributos; `CaosArcanaClient.java` registra camadas e renderers. `CaosArcanaClient.java` é zona vermelha em `docs/collaboration/OWNERSHIP.md`; adicionar mobs exige o registro de renderização e está coberto pelas solicitações de criação de inimigos do usuário.
- `TerrasDaCinzaMob` reduz dano de fogo recebido em 50%. Só mobs que herdam dessa classe recebem essa resistência; não estendê-la a mobs da Floresta Negra sem decisão de gameplay.

### Terras da Cinza — implementado

- **Rastejante de Cinza** (`/summon caosarcana:rastejante_de_cinza`): inimigo básico; 4 HP (2 corações), dano baixo como silverfish. Orientação e UV corrigidas após teste no jogo.
- **Cão Fumegante** (`/summon caosarcana:cao_fumegante`): 10 HP (5 corações), velocidade 0,34, dano 2, fumaça e brasas; resistente ao fogo; hostil ao jogador e caça animais não domesticados/não hostis. Modelo esguio e baixo, com focinho e boca ajustados.
- **Mineiro Oco** (`/summon caosarcana:mineiro_oco`): 18 HP; segura uma picareta de ferro real, tem capacete com lanterna e camada de item na mão.
- **Vigia das Brasas** (`/summon caosarcana:vigia_das_brasas`): 24 HP; hostil, atira pequenas bolas de fogo e solta fumaça/chamas; resistente ao fogo.
- **Carapaça Basáltica** (`/summon caosarcana:carapaca_basaltica`): 30 HP, armadura 5; ataque próximo com empurrão e partículas de basalto.
- **Colosso Basáltico** (`/summon caosarcana:colosso_basaltico`): boss com barra vermelha, 180 HP, armadura 8, resistência a empurrão e fogo. Ataques: corpo a corpo, investida e impacto em área. Depois do teste, hitbox reduzida para 2,8 × 3,2 blocos; golpe comum tem intervalo 48 ticks; investida curta ocorre entre 7–12 blocos com recarga 130 ticks; impacto prepara por 18 ticks e recarrega em 150 ticks.

### Floresta Negra (Vila Oculta) — em andamento

- **Guardião de Madeira Negra** (`/summon caosarcana:guardiao_madeira_negra`): primeiro mob implementado. 26 HP, dano 5, armadura 2. Construto humanoide de troncos, galhos, garras e musgo. Fica invisível após 40 ticks parado perto de ao menos 3 blocos de tronco e 8 de folhas; revela-se ao mover, sofrer dano ou quando o jogador chega a menos de 8 blocos. Não herda redução de fogo das Terras da Cinza.
- Próximos mobs ainda planejados:
  - **Aranha de Casca Solta:** prepara teias nas copas, aplica lentidão e salta sobre o jogador.
  - **Espírito da Névoa:** efeitos breves de cegueira/desorientação, com sinais prévios.
  - **Cultista Oculto:** maldições e invocação de espíritos menores.
  - **Ancião das Sombras (boss):** arena com raízes, clones sombrios e tempestade de folhas venenosas; ataques com avisos e pausas para reação.
- Roster registrado em `docs/biomas/floresta-negra.md`.
- Bioma customizado, geração da Vila Oculta/masmorra e regras de spawn natural ainda não foram feitos. Por ora, mobs registrados podem ser testados por `/summon`.

## Principais caminhos

- Entidades e atributos: `caos-arcana-mod/src/main/java/br/com/caosarcana/common/entity/`
- Modelos e renderers: `caos-arcana-mod/src/client/java/br/com/caosarcana/client/`
- Texturas: `caos-arcana-mod/src/main/resources/assets/caosarcana/textures/entity/`
- Nomes: `caos-arcana-mod/src/main/resources/assets/caosarcana/lang/pt_br.json` e `en_us.json`
- Registros: `CaosArcanaEntities.java` e `CaosArcanaClient.java`
- Regras locais: `docs/collaboration/INTEGRATION.md`, `OWNERSHIP.md`, `CONTRACTS.md`, `TASKS.md`.

## Continuidade recomendada

1. Pedir o resultado do teste visual do Guardião e ajustar modelo, textura ou camuflagem conforme a captura.
2. Criar a Aranha de Casca Solta com salto da copa e teia com sinalização legível.
3. Implementar Espírito da Névoa e Cultista Oculto; depois detalhar o Ancião e sua arena.
4. Planejar geração do bioma e spawn natural numa etapa própria, depois de definir vila/estrutura.
