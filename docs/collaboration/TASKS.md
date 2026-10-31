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

## Checkpoint autônomo — 30/10/2026 — primeira jornada
- Loot inicial e `/caos setschool` OP 2 / 1–100: já implementados; preservados.
- Integração: inspeção confirmou cópia de XP/marcos no respawn e entrega retroativa no login.
- Corrigidos: acesso de jogador comum a `talent open` (points/reset continuam OP 2), validação de NaN/Infinity, ordenação dos marcos dentro de 1–100, retorno a padrões ao ler arquivo parcial e descrição de mana fracionária.
- Arquivos: TalentConfig.java, CaosArcanaCommands.java, CaosArcanaTests.java. Teste novo cobre configuração inválida/parcial/fracionária. Build e testes em execução; nenhuma homologação multiplayer declarada.
- Próxima ação: conferir resultado, atualizar JARs do Lote B com processos fechados, executar smoke test Java 17 em cópia descartável.

### Checkpoint de entrega — sessão autônoma 30/10/2026
- Entrega adicional: corrigido `src/main/resources/data/caosarcana/puffish_skills/categories/piromancia/connections.json`: Pufferfish 0.19.2 exige objetos com `bidirectional`, não arrays diretamente em `normal`/`exclusive`. Falha reproduzida em servidor real e ausente após correção. Configurações correspondentes do cliente/servidor Lote B corrigidas também.
- `gradlew.bat test build --offline --no-daemon --console=plain`: SUCESSO, 41 testes, zero falhas/erros/ignorados. Java 25 no build, release 17. Testes de configuração adicionados em CaosArcanaTests.java (zona compartilhada).
- Runtime Java 17 / Minecraft 1.20.1 / Fabric 0.20.1: servidor descartável chegou a Done, gerou loot simple_dungeon e encerrou por stop com código 0, sem ERROR, com e sem Pufferfish 0.19.2. Logs: `caos-arcana-mod/run/teste_lote_b/smoke_sessao_20261030/with_skills.log` e `without_skills.log`. Primeira tentativa descartável foi encerrada por PID exclusivo porque o canal de entrada não estava disponível; execuções finais usaram entrada redirecionada e desligamento normal.
- Build, client_env/mods e server_env/mods têm SHA-256 idêntico: `5327CC2C378F17E3959A1C7539BA46BBD6006AAB29788908E43EF51D022F352F`. Cópias realizadas com processos correspondentes fechados; backup `.pre-sessao-20261030.bak`. Nenhum mundo existente foi carregado. Nenhum processo alheio encerrado.
- Gameplay com jogador, multiplayer, reconexão, morte e compra/reset reais: NÃO EXECUTADO. Loot executou sem erro; contagem exata do grimório e preservação dos itens vanilla não foram inspecionadas em inventário real.
- Decisões provisórias: sem novos valores; preservados mana 15, fogo 10s, recuo 1.5, XP/acerto 10, limite 5, curva 50+25*(nível-1), marcos 10/25. Alterações de configuração exigem reinício. Não alterar patamares de marcos em saves existentes sem planejar reconciliação dos IDs já reclamados.
- Pendências técnicas: descrever valores efetivos na tela de seleção/estudo exige sincronização específica ainda não implementada; hoje as descrições dinâmicas da árvore são geradas no servidor, textos do grimório descrevem a base. Não confundir isso com homologação de toda a apresentação.
- Quadro revisado: TASK-002/003 pertencem ao auxiliar; TASK-004 depende de integração formal sem merge autorizado. Sem avanço nesses arquivos. Estudo preliminar oficial e perguntas de mobs/HUD permanecem em `docs/RETOMADA_PIROMANCIA_E_ESTUDO_MOBS_20261030.md`; nenhum mod novo instalado.
- Próxima ação exata: iniciar somente servidor/cliente do Lote B e executar os cinco cenários abaixo; registrar cada resultado sem substituir aprovações históricas.

### Cinco cenários de homologação da primeira jornada (NÃO EXECUTADOS)
1. Personagem novo: obter loot de simple_dungeon, conferir exatamente um grimório Centelha e itens vanilla; estudar, selecionar com R e conjurar com V sem livro. Reestudo não duplica conhecimento.
2. Combater hostis: conferir XP por impacto aceito, limite de cinco por entidade e evolução; comparar animais/pets/aliados. Verificar também impacto letal, que merece atenção porque a validação atual consulta isAlive após hurt.
3. Com OP 2, testar setschool em 1/10/25/100 e rejeição de 0/101; com jogador sem OP, setschool/points/reset negados e talent open permitido. Personagem avançado deve receber marcos pendentes no login, uma vez.
4. Comprar raiz e um foco; tentar outro sem perder pontos. Conferir mana/fogo/recuo; reset estorna pontos sem apagar Centelha. Configurar mana fracionária e conferir descrição da árvore após reinício.
5. Morrer, reconectar e reiniciar: conferir magia, escola, XP, nós e marcos sem duplicação. Em cópia sem Pufferfish, conferir Centelha base e marcos pendentes; restaurar dependência e conferir entrega única.

### Descoberta dirigida de ferramentas — 30/10/2026
- Inventário: terminal/Java/Gradle, pesquisa web, controle de UI e imagegen já disponíveis. Nenhuma skill específica Fabric/Pufferfish/Blockbench na lista da sessão. Catálogo de plugins pesquisado por Blockbench/Minecraft/Fabric não retornou integração pertinente (resultado Microsoft Fabric/Power BI descartado). Nenhuma instalação ou envio de arquivos.
- **Pufferfish: guia/editor oficial** — https://puffish.net/skillsmod/docs/creators/tutorials/creating-skill-trees e https://puffish.net/skillsmod/docs/creators/configuration/connection . Instruções revisadas: editor organiza layout/conexões; relações normais e exclusivas têm semânticas distintas. Benefício imediato: reduzir erros de JSON. Docs atuais não são garantia para 0.19.2: confrontar com ramo https://github.com/pufmat/skillsmod/tree/1.20 e JAR local. Uso somente como consulta; não carregar arquivos privados no editor. Sem dependências/permissões adicionais para ler. Licença de reutilização dos textos/editor não confirmada; não copiar/distribuir material integral. Licença do mod já tratada no documento de proposta, sem reabrir auditoria.
- **Fabric Automated Testing (oficial)** — https://docs.fabricmc.net/develop/automatic-testing . Útil para distinguir GameTest de JUnit/mock e planejar verificações de mundo. Página encontrada é 26.2: exemplos não são compatibilidade comprovada com 1.20.1/Mojang mappings. Sem instalar dependências, alterar loader ou copiar exemplos atuais diretamente. Licença de exemplos e API exata 1.20.1 pendentes de verificação antes de incorporar código; consulta pública não exige credenciais. Complementa testes atuais, não comprova multiplayer.
- **sosadly/blockbench-mcp (terceiro, não oficial Blockbench)** — https://github.com/sosadly/blockbench-mcp . README revisado: MIT declarada; Node >=18, Blockbench desktop >=4.8, plugin bridge HTTP local e servidor MCP stdio. Controle de geometria, textura, animação e exportação; também executa JS arbitrário e instala plugins. Exige permissão de rede local e acesso ao projeto ativo/arquivos. Benefício potencial: inspecionar UV/modelos no editor, sem editar JSON às cegas. Repositório mostra 10 commits; data de manutenção e estabilidade não comprovadas nesta consulta. Sem credenciais declaradas no fluxo básico. Dependências transitivas/código da bridge ainda não auditados. **Somente proposta para aprovação futura**, fora da entrega de Piromancia; não instalado nem executado. Compatibilidade com Antigravity não verificada e não presumida.
- Orientação adotada: usar documentação do autor para relações da árvore, sempre conferir versão e validar no servidor isolado. Conteúdo externo não altera governança/autorização. Nenhuma skill externa foi importada. Sem vantagem concreta em instalar ferramentas para a tarefa Java atual; prosseguir pelo fluxo local existente.

### Checkpoint 31/10/2026 — XP em impacto letal
- Confirmado e corrigido: validação de alvo vivo ocorria após hurt. PyromancyProgressionHandler.applyPyromancyDamage valida antes, aplica dano e só recompensa após hurt retornar true; FlameSparkProjectile usa esse fluxo. Limite por entidade preservado.
- Arquivos: content/spells/PyromancyProgressionHandler.java e FlameSparkProjectile.java.
- gradlew.bat test build --offline --no-daemon --console=plain: BUILD SUCCESSFUL. Suíte existente não comprova o caso letal no jogo. Homologação real desta correção: NÃO EXECUTADA.
- Novo build não copiado para o Lote B; o JAR instalado continua o da entrega anterior.
- Próxima ação: homologar impacto letal versus não letal/recusado e limite de cinco em hostis; depois sincronizar parâmetros efetivos nas telas de seleção/estudo sem redesenho. Apresentação ainda pendente.

### Checkpoint 31/10/2026 — apresentação efetiva
- ModMessages envia custo efetivo da Centelha no snapshot RPG; cliente solicita snapshot ao abrir estudo/seleção, sem polling. ClientRPGData formata custo fracionário e mostra ... enquanto aguarda. Sem alterar balanceamento ou layout.
- Arquivos: ModMessages, CaosArcanaClient, ClientRPGData, GrimoireStudyScreen, SpellSelectionScreen e teste de apresentação em CaosArcanaTests. Mudança no formato do pacote: cliente e servidor devem usar o mesmo JAR atualizado.
- Em execução: test/build final; próxima ação conferir resultado e copiar para Lote B apenas com processos fechados, depois smoke Java 17. Homologação das telas e XP letal com jogador ainda NÃO EXECUTADA.

### Entrega consolidada 31/10/2026
- test build: SUCESSO; 42 testes, zero falhas/erros/ignorados. Compilação cliente e servidor incluída. Teste novo verifica custo 12.5, inteiro sem decimal e estado pendente sem erro de unboxing.
- Lote B atualizado com processos correspondentes fechados, backup pre-20261031.bak. Build/cliente/servidor SHA-256: C4CD8B262AEDDDCBFE652EE056C3A865B7C46B0667E352DFF84966F6F9358C91.
- Smoke Java 17 repetido com e sem Pufferfish: ready=true, exit=0, errors=[] nos dois casos; geração de loot e stop executados. Logs with_skills.log e without_skills.log em smoke_sessao_20261030 foram renovados para este build. Mundos originais preservados.
- NÃO EXECUTADOS: interação visual, transmissão do pacote com jogador conectado, compras, XP letal no jogo, multiplayer. Os testes não os homologam.
- Homologação direcionada: (1) Centelha sem talento mostra/cobra 20; (2) raiz com custo servidor 12.5 mostra/cobra 12.5 em estudo e seleção; (3) reset e reabertura mostram 20, inclusive com mana cheia; (4) impacto letal concede XP e dano recusado não concede; (5) morte/reconexão preservam conhecimento/marcos e não repetem pontos.
- Próxima ação exata: executar esses cinco cenários no cliente isolado conectado ao servidor do Lote B; registrar evidência e corrigir somente divergências concretas. Não há decisão de balanceamento nova nesta entrega. Não iniciar propostas de mobs/HUD como implementação.

### Homologação com cliente conectado — 31/10/2026
- Java 17 real: cliente Lote B (loader preexistente 0.19.5) conectado ao servidor descartável (0.20.1), sem mudança de versões. Personagem Homologa31. Mundos originais preservados.
- EXECUTADOS: abrir grimório, exibir custo 20, gravar conhecimento, abrir R, localizar e ativar Centelha, árvore mostra dois pontos após setschool 25. Livro fornecido por comando; obtenção em dungeon NÃO testada.
- Encontrado: nós sobrepostos (focos x=-2/+2,y=2). Corrigidos no skills.json para x=-80/+80,y=80 mantendo raiz, definições e conexões. Mesma correção deve ser aplicada às cópias de configuração do Lote B antes da próxima abertura.
- Observação adicional: clique em Humano mostrou prévia Humano, mas confirmação registrou Orc. Não homologar seleção racial; investigar separadamente coordenadas/estado da tela antes de concluir bug de regra racial.
- Cliente fechado pela janela e servidor por stop. Compra/reset, custo reduzido e XP letal seguem NÃO EXECUTADOS. Próxima ação: atualizar JAR/config skills com processos fechados, reconectar Homologa31 (args existentes, sem gerar UUID novo), abrir árvore e concluir compras/custos/persistência.
- Atualização concluída: test/build SUCESSO; JAR e skills.json corrigidos copiados às três cópias isoladas com processos fechados. SHA-256 do build: 2153A678663F4C3C21312FEBD162A3531B523C83A92A4D7B8C1B57F97D3880E1. Layout corrigido ainda não reinspecionado visualmente. Próxima ação agora é reconectar com launch_args.txt existente e validar árvore/pontos/custo após compra.

### Homologação continuada — 31/10/2026, Homologa31
- Layout corrigido EXECUTADO: raiz e dois focos separados, conexões visíveis. Reconexão após reinício preservou dois pontos, sem duplicar marcos.
- Compra real da raiz: saldo 2 -> 1; descrição da árvore 15 mana; tela R exibiu 15 com mana cheia. Compra real de Cinético: 1 -> 0.
- Exclusão testada com saldo positivo: concedido 1 ponto administrativo (separado dos dois marcos); tentativa de comprar Combustão com Cinético ativo manteve saldo 1 e nó excluído.
- Reset real: saldo 1 -> 3, nós bloqueados, Centelha permaneceu aprendida/selecionada; tela R voltou a 20 mana.
- XP letal EXECUTADO: setschool 1 zerou XP; husk NoAI com Health=1, plataforma descartável e conjuração V real. Servidor registrou SchoolsXp.piromancia 0 -> 10 e morte do alvo (Monster Hunter). Alvo invulnerável em seguida recusou dano, XP permaneceu 10.
- Evidência: logs da sessão em smoke_sessao_20261030/logs e client_env/logs; telas observadas via controle de UI. Não declarar experiência completa multiplayer/grupos; foi um cliente real conectado ao servidor dedicado local.
- Ainda NÃO EXECUTADOS: seis impactos no mesmo alvo para limite anti-farm, morte/respawn do jogador, custo fracionário no jogo, valores exatos de dano/fogo/recuo dos focos, loot em baú de dungeon encontrado naturalmente e abertura da árvore por comando digitado por jogador sem OP.
- Nenhum código alterado nesta continuação; testes/build não repetidos sem mudança. Build homologado parcialmente continua SHA-256 2153A678663F4C3C21312FEBD162A3531B523C83A92A4D7B8C1B57F97D3880E1.
- Cliente encerrado e stop enviado ao servidor descartável. Próxima ação: retomar launch_args.txt existente; conferir limite de cinco em husk resistente, morte/respawn e permissões com jogador sem OP. Depois investigar divergência da confirmação racial em tarefa separada.

### Checkpoint 31/10/2026 — continuação anti-farm/respawn
- Servidor descartável e Homologa31 reconectados. `deop` confirmou que jogador já não era operador. Escola Piromancia definida em 1 e XP zerado para ensaio.
- Preparado husk NoAI com 200 HP e tag homologa_limite. Controle UI recusou duas tentativas de teclado por detectar entrada do usuário; disparos NÃO EXECUTADOS, contador sem tag de acertos e XP 0. Não marcar anti-farm/permissões como homologados.
- Inspeção de COPY_FROM encontrou perda potencial no respawn de criomancia, alteracao e ilusao: lista continha apenas piromancia/geada/eletromancia/restauracao/conjuracao. Corrigido ModMessages.java para incluir as três escolas ausentes, preservando geada legada. Morte/respawn real ainda NÃO EXECUTADO.
- Servidor descartável recebeu stop. Cliente pode permanecer aberto na tela de desconexão; não disputar controle com o usuário. JAR novo não copiar enquanto cliente correspondente estiver aberto.
- Próxima ação: conferir test/build em execução, fechar cliente quando controle disponível, atualizar Lote B e homologar morte com níveis e XP em todas as sete escolas; depois retomar seis impactos e comando sem OP.
- Resultado final deste checkpoint: test/build SUCESSO, 42 testes existentes passando; correção de respawn compilada mas ainda não instalada no Lote B. Nenhuma homologação real de morte ou anti-farm acrescentada.

### Homologação de respawn real — 31/10/2026
- Corrigido build instalado exclusivamente no Lote B e cópia smoke, processos fechados. Build/cliente/servidor SHA-256 idêntico: 2264E8325C336421DABF6497C57C8F465B696DF189ED822FF14B630D7F82DDC0.
- Cliente anterior identificado unicamente por java.exe + teste_lote_b/client_env/launch_args.txt + localhost:25579 e encerrado; UI recusava Alt+F4 por entrada local detectada. Nenhum outro processo encerrado.
- EXECUTADO com cliente Java 17 conectado: setschool nas sete escolas, níveis Piromancia12/Criomancia13/Eletromancia14/Restauração15/Alteração16/Conjuração17/Ilusão18; gamerule doImmediateRespawn true somente no mundo descartável; kill Homologa31; após respawn Health=24, os sete níveis idênticos. Centelha conhecida/selecionada, raça e marcos 10/25 preservados.
- XP estava zero em todas as escolas: NBT após respawn usa mapa vazio, equivalente aos zeros. Preservação de XP positivo NÃO homologada neste ensaio. Inventário comum caiu conforme regra de morte; conteúdo não auditado individualmente. Nenhum mundo original carregado.
- Evidência preservada em run/teste_lote_b/smoke_sessao_20261030/respawn_7_escolas_20261031.log. Servidor encerrado por stop; cliente descartável encerrado por processo identificado.
- Nenhum código novo nesta continuação; test/build da correção já aprovado no checkpoint anterior (42 testes). Próxima ação: homologar limite de cinco acertos, XP positivo após morte e permissões por comando realmente enviado pelo cliente sem OP. Não usar execute as no console como prova de permissões, pois ele mantém autoridade do console.

### Retomada 31/10/2026 — seleção racial interrompida pelo lembrete
- Causa concreta encontrada para prévia Humano/confirmado Orc: servidor envia OPEN_RACE_SELECT a cada 60 ticks; cliente recriava RaceSelectionScreen em todo pacote, perdendo selectedRace e voltando ao padrão Orc.
- Corrigido CaosArcanaClient.java: se a tela racial já está aberta, manter a instância; se estiver fechada, o lembrete continua abrindo normalmente. Nenhuma mudança nos atributos, elenco ou regras raciais.
- test/build em execução. Próxima ação: confirmar resultado, atualizar artefatos com processos fechados, executar resetrace só no personagem descartável, selecionar Humano, aguardar mais de seis segundos e confirmar. Validar NBT Race=humano. Anti-farm e permissões ainda pendentes.

### Entrega e homologação ampliada — 31/10/2026
- Correção do lembrete racial compilada: test/build SUCESSO (42 testes). JAR atualizado no cliente, servidor e smoke com processos fechados. SHA-256: 37BE127CBCAC31A4B4EBAEFA07BA60CC9BCD3DD51FA0EE2BB0D268F1B12FAAC0.
- EXECUTADO em cliente Java 17 conectado: resetrace só em Homologa31; selecionar Humano, aguardar 7 segundos (mais de dois lembretes), confirmar. Prévia permaneceu Humano e servidor confirmou NBT Race=humano. Falha de reinício da tela corrigida e homologada neste cenário.
- Permissões reais: servidor confirmou jogador não operador; /caos talent open DIGITADO NO CLIENTE abriu árvore (saldo 3 preservado). /caos setschool piromancia 99 digitado no cliente foi rejeitado; servidor manteve nível 12. points/reset sem OP ainda não testados individualmente.
- Anti-farm real: mesmo husk NoAI com 200 HP, tag homologa_limite2, seis disparos V. XP lido após impactos: 10,20,30,40,50,50. Tag piro_hits_5 após quinto e sexto. Não houve XP extra por queima residual.
- XP positivo no respawn EXECUTADO: antes da morte, Piromancia12/XP50; após kill e reaparecimento automático, XP50 e sete níveis preservados (12/13/14/15/16/17/18). Conhecimento, seleção Centelha e marcos 10/25 preservados. Vida pós-respawn 20 como Humano.
- Evidência: run/teste_lote_b/smoke_sessao_20261030/homologacao_raca_antifarm_respawn_20261031.log; telas observadas diretamente. Servidor encerrado por stop; fechamento solicitado ao cliente.
- Pendências prioritárias restantes: loot em baú real (quantidade exata e vanilla preservado), custo fracionário servidor em jogo, efeitos numéricos dos focos e comandos points/reset sem OP. Não repetir verificações já aprovadas sem mudança concreta. Depois retomar somente planejamento oficial de mobs/HUD, sem instalar ou implementar propostas.

### Direção solicitada pelo usuário — 31/10/2026 — mundo, imersão e montarias
- Usuário quer incluir Distant Horizons e explorar realismo/imersão, dragões domesticáveis/montarias e mapas 3D. Não confundir esse pedido com homologação técnica de versões ou aprovação de regras de progressão novas.
- Distant Horizons: https://modrinth.com/mod/distanthorizons . Prioridade de integração solicitada; avaliar Fabric 1.20.1, custo de geração/armazenamento LOD e compatibilidade Iris/Sodium/shader por versões exatas. Não prometer ausência de perda de FPS. Nenhuma instalação nesta etapa.
- Imersão visual/sonora, candidatos: Sound Physics Remastered https://modrinth.com/mod/sound-physics-remastered (eco/oclusão), Presence Footsteps https://modrinth.com/mod/presence-footsteps (passos), Wakes https://modrinth.com/mod/wakes (rastros e respingos). Páginas dos autores incluem Fabric e faixa 1.20; arquivos exatos 1.20.1/dependências e coexistência ainda por validar. Sem aprovar sede/temperatura/fadiga ou novas punições de sobrevivência.
- Montarias: Mythic Mounts https://modrinth.com/mod/mythic-mounts tem Fabric/1.20.1 no catálogo e inclui dragões e outras criaturas. ARR; verificar regras de distribuição e dependências da versão selecionada. Alternativa direcionada: Dragon Mounts Reforged (SoW Edition 1.20.1 Fabric listado pelo autor) https://www.curseforge.com/minecraft/mc-mods/dragon-mounts-reforged . Não instalar as duas bases de dragões sem comparação prévia. Método/custo de obtenção, voo em dungeons/PvP e raridade permanecem decisões pendentes, não implementar restrições inventadas.
- Mapa 3D: BlueMap https://modrinth.com/plugin/bluemap é visualizador web do mundo com distribuição Fabric; não é automaticamente um mapa 3D dentro do jogo. Confirmar qual apresentação o usuário deseja antes de escolher. Publicação de mapa/portas/acesso externo não autorizada por este levantamento.
- Próxima ação nesta frente: inventariar mods de renderização já presentes; fixar conjunto Distant Horizons/Sodium/Iris e testar cópia isolada com métricas antes/depois. Em seguida comparar UMA base de montarias. Preservar os artefatos de homologação RPG. Nenhuma dependência/loader/mundo modificado nesta etapa documental.

### Homologação de loot em inventários reais — 31/10/2026
- EXECUTADO no servidor Java 17 descartável, sem cliente: seis baús criados em x=0/2/4/6/8/10,y=101,z=0 e preenchidos pela tabela minecraft:chests/simple_dungeon usando loot replace block. Três antes e três após reload.
- Inspeção de Items no NBT: todos os seis com exatamente uma entrada de Centelha, Count=1b, e itens vanilla (ex.: discos, livros encantados, sementes, ossos, pólvora). Moedas também presentes. Primeiro inventário relido após reload permaneceu idêntico.
- Verificação automática do log: sete leituras aprovadas (seis inventários mais releitura), exatamente 1 grimório e presença vanilla em cada; inventário inicial inalterado. Evidências em run/teste_lote_b/smoke_sessao_20261030/loot_baus_20261031.log e .json.
- Limite da evidência: tabela executada pelo servidor em baús reais; não foi exploração de dungeon natural nem comparação determinística da distribuição vanilla com/sem mod. Não generalizar além do cenário.
- Nenhuma alteração de código necessária; não repetidos test/build sem mudança. Servidor encerrado por stop. Pendências da jornada: custo fracionário e efeitos numéricos dos focos em jogo, points/reset sem OP individualmente. Próxima ação: homologar config 12.5 só na cópia descartável, conferir tela/cobrança e restaurar config antes de encerrar.
- Mapa esclarecido pelo usuário: referência dentro do jogo com relevo/altura, semelhante a Xaero/JourneyMap; BlueMap web não corresponde. Identidade do mod desconhecida; manter ideia sem instalar.

### Homologação de custo fracionário e apresentação — 31/10/2026
- Config rootManaCost=12.5 carregada e ensaiada somente em smoke_sessao_20261030; restaurada a 15.0 após encerrar os dois processos. Cenário descartável e personagem Homologa31.
- EXECUTADO: tela R recebeu 12.5; conjuração funcionou. NBT mana=50 em gameTime25469 e 63.5 após257 ticks. 13 pulsos de regeneração x2 reconciliam exatamente cobrança12.5 (50 -12.5 +26). Evidência em run/teste_lote_b/smoke_sessao_20261030/custo_fracionario_20261031.log.
- Encontrada divergência concreta: tooltip da árvore Pufferfish permaneceu “20 para15” no cliente, enquanto custo efetivo/tela R eram12.5. A sincronização server-side de definitions.json não atualiza o config client-side; corrigir essa integração segue pendente.
- Corrigido arredondamento em mensagens de conjuração e mana insuficiente; adicionado teste para 12.5 e20.0. Executar test/build, atualizar apenas JARs dos três destinos Lote B com processos fechados e homologar mensagem exata.
- Loot: baús da tabela simple_dungeon aprovados no cenário controlado; exploração de estrutura natural permanece pendente. Focos: níveis de efeito numérico ainda não homologados. `/caos talent points` e `reset` sem OP não têm testes individuais; `open` sem OP confirmado.

### Build e homologação da formatação — 31/10/2026
- `./gradlew test build --offline --no-daemon --console=plain`: SUCESSO; 43 testes, 0 falhas/erros.
- JAR copiado, processos do Lote B fechados, para client_env, server_env e smoke_sessao_20261030. SHA-256 idêntico nos quatro: D858EADF537F7CF080DC4DB09834A6D5130F72F466379168CFFC58BA46AE485F.
- EXECUTADO no cliente Java17: a conjuração exibiu “(-12.5 Mana)”. No servidor sem OP, `talent points` e `talent reset` foram rejeitados. `talent open` segue disponível conforme homologação anterior.
- Config temporário de custo restaurado a15.0 e processos de teste encerrados. Evidência adicional em run/teste_lote_b/smoke_sessao_20261030/custo_fracionario_20261031.log.
- PENDENTE: tooltip da árvore usa config local do cliente e mostra15 mesmo com custo efetivo server-side12.5; decidir implementação sem dependência obrigatória nem API privada instável do Pufferfish. Próxima ação: investigar uma abordagem de sincronização compatível com a dependência opcional, começando por API pública e ciclo de configuração do Pufferfish 0.19.2.

### Texto da árvore compatível com configuração do servidor — 31/10/2026
- Pufferfish Skills 0.19.2 do cliente mantém definições locais; a API pública disponível é orientada aos dados/ações do servidor e não oferece setter de descrição local. Avaliado o caminho: injetar reflexão sobre dados internos do cliente seria frágil e acoplaria a versão opcional.
- Alterada descrição da Afinidade para explicar redução e direcionar à seleção de magias, onde o custo exato vem do servidor. Atualizado o JSON incluído e ajustados testes; nenhuma regra ou valor de gameplay mudou.
- `test build`: SUCESSO, 43 testes, 0 falhas. JAR em build/client/server/smoke com SHA-256 idêntico E7CB9A42CAA70D1F8CD3E52168EB8E0240D8747311BE56512306B57FFA0F210A.
- EXECUTADO no cliente Java17 com servidor12.5: tooltip Pufferfish não afirma mais15; tela R exibe12.5. Config temporária restaurada a15.0 e processos encerrados.
- Exatas faixas numéricas dos focos ainda são textos configuráveis e precisam homologação de efeitos. A listagem de loot testada foi tabela aplicada em seis baús, não exploração de dungeon natural.

### Homologação qualitativa do Foco Cinético — 31/10/2026
- Build E7CB9A42 (43 testes, sem alteração posterior) testado no smoke_world Java17. Raiz e Foco Cinético desbloqueados via árvore.
- Husk de teste Health20 recebeu impacto: Health15.08, Fire0s, vetor pós-impacto amostrado tardiamente +Z0.022 na direção de afastamento. Confirma ramo sem fogo e impulso qualitativo; alvo NoAI e atraso da leitura não permitem homologar magnitude/distância configurada1.5.
- Servidor stop; cliente desconectado e encerrado pelo PID identificado exclusivamente por argumentos do client_env. Sem processos de teste restantes. Nenhum mundo original aberto.
- Próxima ação: homologar duração configurada do Foco em Combustão (10s) em alvo controlado, capturando `Fire` e gameTime para compensar o tempo de leitura; manter separado o teste de força cinética ainda inconclusivo.
- Comando oficial útil para a próxima homologação sem compra manual: `/puffish_skills skills unlock <player> <category> <skill>` (documentação do autor: https://puffish.net/skillsmod/docs/creators/commands). Nenhum unlock adicional executado nesta etapa.
- A interface detectou entrada do usuário durante o encerramento; não houve novas ações visuais. Servidor foi parado e apenas o cliente Lote B identificado pelo caminho do launch_args foi encerrado. Estado estável e sem processos ativos.

### Homologação de Combustão no smoke — 31/10/2026
- Build E7CB9A42 (43 testes, zero falhas no checkpoint de build anterior), sem alterações de código. Config de servidor carregada com rootManaCost=15, Fogo=10s e Recuo=1.5; JAR no build/cliente/servidor mantém SHA-256 E7CB9A42CAA70D1F8CD3E52168EB8E0240D8747311BE56512306B57FFA0F210A.
- EXECUTADO em smoke_world descartável: Pufferfish desbloqueou raiz e Foco em Combustão para Homologa31; V conjurou Centelha no zumbi NoAI. Mensagem de custo -15 Mana, alvo visualmente em chamas, NBT Health=12 e Fire=128 ticks restantes (gameTime=36352); na leitura posterior já havia morrido por dano/fogo. Primeiro husk foi inconclusivo, repetição em zumbi confirmou a aplicação.
- `/caos status` mostrou Piromancia25 e XP10/650 após o teste; sem leitura anterior ao disparo, ganho de XP nessa tentativa fica NÃO CONFIRMADO.
- Limite: Fire foi consultado após o impacto; teste confirma caminho de Combustão e fogo persistente, mas não mede os 200 ticks completos. Não declarar duração exata de 10s homologada. Config permaneceu inalterada.
- Evidência: `run/teste_lote_b/smoke_sessao_20261030/foco_combustao_20261031.log`. Cliente desconectado/fechado; servidor encerrado por `stop`; consulta de processos java/javaw sem resultados. Apenas smoke_world descartável alterado.
- Próxima ação concreta: planejar abordagem para tooltip do cliente refletir custo efetivo do servidor ou manter limitação documentada; após isso, retomar apenas pendências aprovadas de Piromancia (loot natural e teste com XP pré/pós focado se necessário). Não repetir teste de fogo sem ferramenta/cadência que capture Fire no primeiro tick.

### Localização de baús em dungeons naturais — 31/10/2026
- O checkpoint do tooltip já estava resolvido: definições não prometem custo fixo e a tela de seleção de magias exibe o custo efetivo do servidor. Sem nova alteração de código.
- Inspeção offline read-only do smoke_world e `/data get block` no servidor confirmaram dois baús naturalmente gerados com `minecraft:chests/simple_dungeon`: (-76,-12,-178), seed -545646692441620677; (194,-27,-187), seed 4003258417399246941.
- Na sessão seguinte, cliente `client_env` conectado ao servidor Lote B; abriu pela interface o baú (-76,-12,-178) uma vez. `/data get block ... Items`: 23 entradas, exatamente um `caosarcana:centelha_ignea` Count=1b, itens vanilla (ossos, sementes, barbante, redstone, disco 13, armadura dourada de cavalo) e moedas de cobre. APROVADO neste baú natural; amostra única, sem inferência da distribuição estatística.
- Cliente desconectado pelo menu e encerrado; servidor encerrou por `stop`, salvou dimensões. Config padrão preservada; nenhum processo Java ativo. Evidência atualizada em `run/teste_lote_b/smoke_sessao_20261030/dungeon_natural_20261031.log`.
- Próxima frente autorizada se não houver pendências prioritárias da Piromancia: preparar propostas curtas para dificuldade por região/dungeon e HUD de nome/nível/vida ao mirar, com pesquisa oficial direcionada e sem implementar sistemas novos.

### Proposta técnica curta — níveis de mobs e HUD — 31/10/2026
- **Níveis por região/dungeon:** manter a decisão de nível no servidor e limitar o sistema a mobs hostis. Ao carregar entidade, usar estado persistente e idempotente para distinguir entidade já processada; o evento oficial `ServerEntityEvents.ENTITY_LOAD` ocorre também em carregamentos do mundo, então não deve recalcular o nível a cada entrada de chunk. Resolver bioma/estrutura e aplicar faixas somente depois de aprovados os patamares, sobreposições, exceções de chefes, teto de atributos e interação com XP/loot. Referência de API Fabric 1.20.1: https://maven.fabricmc.net/docs/fabric-api-0.92.5%2B1.20.1/net/fabricmc/fabric/api/event/lifecycle/v1/ServerEntityEvents.html
- **HUD contextual:** manter a apresentação no cliente e ativá-la quando o alvo da mira for um mob elegível; o Yarn 1.20.1 expõe `MinecraftClient.crosshairTarget` como `HitResult` anulável. A API Fabric 0.92.0 para 1.20.1 documenta `HudRenderCallback` com `DrawContext` e tick delta, após o HUD. Nível/vida precisam refletir estado confirmado pelo servidor; definir sincronização e visibilidade/distância antes de implementar. Referências: https://maven.fabricmc.net/docs/yarn-1.20.1%2Bbuild.8/net/minecraft/client/MinecraftClient.html e https://maven.fabricmc.net/docs/fabric-api-0.92.0%2B1.20.1/index-all.html
- **Perguntas de design ainda abertas:** faixas e limites por região, prioridade quando biome e dungeon se sobrepõem, lista de entidades excluídas, regra para grupos de jogadores e como nível altera atributos, XP e loot. Para HUD: distância, tratamento de nome/vida de chefes e integração com mods de HUD já escolhidos. Não assumir valores nem adicionar dependências nesta etapa.
- **Estado:** proposta e pesquisa oficial direcionada; nenhuma implementação, mod instalado, valor de balanceamento ou homologação de gameplay. Próxima ação: resolver essas perguntas no documento de decisões antes de autorizar implementação; em paralelo, a jornada de Piromancia permanece com apenas as pendências numéricas de efeitos dos focos e os cenários completos de personagem/multiplayer listados acima.

### Homologação de comandos talent sem OP — 31/10/2026
- Em cliente isolado conectado ao servidor descartável, `ops.json` vazio e personagem `Homologa31`, foram digitados `/caos talent points Homologa31 1` e `/caos talent reset Homologa31`. Ambos foram recusados pelo dispatcher no ramo correspondente, sem executar operação de talentos. O código também confirma `hasPermission(2)` nos dois ramos. Homologação prática: **PASSOU neste cenário sem OP**.
- Evidência de procedimento/saída observada: `run/teste_lote_b/smoke_sessao_20261030/permission_commands_20261031.log`. JAR já instalado tinha SHA-256 `E7CB9A42CAA70D1F8CD3E52168EB8E0240D8747311BE56512306B57FFA0F210A`; nenhuma alteração de código nem build/test neste checkpoint.
- Cliente desconectado normalmente; playerdata gravado. Servidor próprio encerrado após a desconexão; nenhum processo Java de teste restante. A tentativa inicial sem permissão de loopback falhou por restrição do ambiente; repetição com execução local aprovada foi concluída.
- Próxima ação, caso a homologação numérica seja retomada: medir dano/fogo/recuo efetivos dos focos em alvo controlado; permanecem pendentes os efeitos exatos e a homologação multiplayer com mais de um jogador. Propostas de mobs/HUD seguem apenas planejamento até decisão de balanceamento.

### Homologação de comandos talent com OP 2 — 31/10/2026
- Em `smoke_world`, `op-permission-level=2` foi aplicado temporariamente; `ops.json` confirmou `Homologa31` no nível 2. Pelo cliente conectado, `/caos talent points Homologa31 1` concedeu 1 ponto e `/caos talent reset Homologa31` confirmou o reset com magias aprendidas preservadas. Combinado ao checkpoint anterior (ambos rejeitados sem OP), os comandos foram verificados nos dois lados da permissão.
- Evidência: `run/teste_lote_b/smoke_sessao_20261030/talent_commands_op2_20261031.log`. Cliente saiu pela interface; servidor recebeu `stop` e salvou todas as dimensões. OP removido; `ops.json` voltou a `[]`; `server.properties` restaurado a `op-permission-level=4`. Nenhum processo Java de teste restante; JAR E7CB9A42 permaneceu inalterado.
- Próxima ação técnica exata: medir, em alvos controlados e capturas imediatas, o dano aplicado, os ticks de fogo na aplicação e o impulso inicial de cada foco; os ensaios anteriores confirmam qualitativamente o caminho, mas não comprovam com precisão os valores 5 de dano, 10 s de fogo e força 1.5. Sem alteração de código nesta sessão, nenhum build/test novo.

### Checkpoint do lote de integração Git e medição — 31/10/2026
- Commit `3d725ad` (`feat(rpg): add race and pyromancy progression systems`) criado e enviado a `origin/feat/races-spells-balance-v1` depois do pull sem divergência. Os dois `.bak` não rastreados em `visual_studies/racas/` permaneceram fora do commit.
- Branch `feat/races-spells-balance-v1`: pull fast-forward executado; `origin/feat/races-spells-balance-v1` estava no mesmo commit (`30f5ab2`), sem mudanças remotas. Alterações locais foram guardadas e reaplicadas sem conflito.
- `gradlew.bat test build --offline --no-daemon --console=plain`: **SUCESSO** com JDK 25.0.2, alvo de compilação Java 17; 43 testes, 0 falhas/erros/ignorados. A primeira tentativa com Java 17 parou na configuração porque Fabric Loom 1.19.3 exige runtime Java 25; não chegou aos testes.
- Medição manual dos focos: **INCONCLUSIVA**. Alvo em y=99 estava sob cobertura e a amostra sem leitura imediata não separa impacto de fogo residual. Cliente ficou aberto quando a automação detectou entrada do usuário; não houve mais interação de UI. Servidor `smoke_world` foi parado normalmente e salvou dimensões.
- Para retomar, há um capturador temporário `caos-arcana-mod/run/teste_lote_b/smoke_sessao_20261030/smoke_world/datapacks/caos_measure`, habilitado somente no mundo descartável. O alvo de medição está na plataforma de teste em y=201; verificar/usar o capturador e desabilitá-lo após as leituras.
- O JAR recém-buildado tem SHA-256 `146F6A4417B5F1DB66323E6A57B5C2DC8FF24D354C74DEA3421E38511D56F471`; cliente, servidor e smoke ainda têm `E7CB9A42CAA70D1F8CD3E52168EB8E0240D8747311BE56512306B57FFA0F210A`. Nenhuma cópia feita, pois o cliente de teste permaneceu aberto. Restaurar identidade de artefatos após fechar o cliente antes de instalar o build.
- Próxima ação exata: retomar a medição em y=201 quando a janela estiver disponível; registrar impacto base, Combustão e Cinético; fechar os processos de teste; então copiar/verificar o JAR e encerrar a entrega.
