# Caos Arcana Documentação Mestra

Server do Caos — visão do RPG, sistemas e referência para criação de assets

Versão 1.0 • 26 de outubro de 2026 • Responsável pelo projeto: Matheus

## 1 Finalidade e autoridade deste documento

O objetivo é criar um RPG de sobrevivência e fantasia medieval dentro do Minecraft, inspirado em Skyrim, com identidade própria no Server do Caos. O servidor deve oferecer meses de exploração, aprendizado, comércio e competição individual. Poderes, raças, equipamentos, conhecimentos e eventos devem funcionar como partes do mesmo jogo equilibrado.

Este documento consolida o que foi discutido e fornece uma referência reutilizável para IAs de desenvolvimento, modelagem e texturização. Não comprova que os sistemas estejam implementados nem substitui uma auditoria do servidor. Nenhum mod, configuração ou mundo foi alterado na elaboração desta documentação.

**Como interpretar os estados usados aqui:**

- **Decidido:** intenção ou requisito expresso pelo usuário.
- **Base proposta:** solução apresentada na conversa e preservada para desenvolvimento; números e regras detalhadas continuam sujeitos a validação.
- **Referência visual proposta:** orientação nova ou detalhamento editorial para ajudar a futura produção de assets. Não representa uma aparência já aprovada.
- **Pendente:** escolha que o histórico disponível não permite fechar com segurança.

Quando houver conflito, as decisões do usuário prevalecem. Uma IA não deve transformar automaticamente exemplos deste documento em conteúdo definitivo. A lista integral de raças, armas e magias ainda não foi fechada. Os conceitos visuais anteriores não estão anexados a este arquivo e não foram inspecionados nesta consolidação.

## 2 Visão e experiência desejada

### 2.1 Pilares decididos

1. RPG mágico inspirado em Skyrim, adaptado à linguagem visual e ao mundo de Minecraft.
2. Personagens com raça escolhida e personalização no primeiro acesso. Cada raça deve ter vantagens e desvantagens.
3. Jogadores distintos por afinidade, aprendizado, equipamentos e escolhas de progressão.
4. Magias predefinidas pronunciadas pelo microfone, descobertas em grimórios, missões, exploração e dungeons.
5. Uma afinidade principal mais forte; outras afinidades também utilizáveis, com desempenho inferior ao da especialidade principal.
6. Conhecimentos aprendidos permanentemente. Aprender uma magia é diferente de carregar seu livro.
7. Evolução difícil e duradoura, com requisitos de habilidade para magias e requisitos de raça e nível para determinados equipamentos.
8. Todos contra todos, sem times permanentes. Comércio e convivência continuam possíveis.
9. Zonas anti-PvP, especialmente lojas, e regiões de risco para exploração e competição.
10. Eventos públicos com coordenadas que criem disputa por baús, recursos e recompensas, com uso planejado do Xaero’s Minimap.
11. Revisão do modpack para retirar facilidades excessivas e evitar poder desproporcional já no primeiro dia.
12. Produção de imagens separadas para estudar raças, armaduras, armas, grimórios, conjurações, elementos e dungeons.

### 2.2 Ciclo de jogo

O jogador escolhe uma raça, constrói sua identidade, aprende técnicas iniciais e explora o mundo. Descobre conhecimentos e materiais, enfrenta desafios, evolui sua escola e sua profissão, fabrica ou compra equipamento e se prepara para disputas. Eventos oferecem recompensas contestáveis; cidades e lojas oferecem comércio e recuperação. A progressão avançada abre técnicas, construções e desafios, mantendo decisões relevantes mesmo depois de meses.

O público previsto é um grupo pequeno, em torno de 6 a 10 participantes. O equilíbrio não pode depender de dezenas de pessoas online ou da presença obrigatória de uma raça específica para toda atividade.

### 2.3 Dificuldade desejada

O usuário quer progressão realmente difícil. A base proposta é combinar conhecimento raro, desafios, domínio das mecânicas e requisitos de habilidade. A duração não deve depender apenas de lançar a mesma magia milhares de vezes ou multiplicar custos de receitas. O jogador precisa ter ferramentas úteis desde o início, sem acesso imediato ao poder máximo.

## 3 Escopo dos módulos e integrações

| Componente | Papel no projeto | Estado |
| --- | --- | --- |
| Server do Caos | Mundo, comunidade, sobrevivência e regras gerais | Contexto do projeto |
| Caos Arcana | Raças, magia, equipamentos, progressão e conteúdo RPG | Nome provisório e escopo planejado |
| Caos Balance | Política central de equilíbrio, combate, loot, eventos e integrações com mods | Módulo proposto |
| CaosPhone | Smartphone com CaosOS, banco/Pix virtual, câmera, galeria, mensagens, GPS e Jarvis por voz | Projeto associado planejado |
| CaosBank | Saldo e pagamentos virtuais, associados ao telefone e ao comércio | Conceito de integração |
| CaosBridge | Integração do servidor com o Discord | Componente citado no histórico; revisar versão instalada |
| MineRadio | Experiência musical própria | Preservação proposta; revisar estado instalado |
| Xaero’s Minimap | Navegação, coordenadas e apoio aos eventos | Integração automática pendente de validação |
| AutoModpack | Distribuição centralizada do pacote e configurações | Citado no histórico; verificar instalação atual |

O ambiente mencionado anteriormente é Minecraft 26.3 e Fabric Loader 0.19.5. Esses dados são contexto informado, não compatibilidade certificada nesta documentação. O desenvolvimento deverá verificar o runtime e as APIs reais antes de escolher formato de modelos, hooks ou bibliotecas.

Caos Balance é uma proposta de responsabilidade, não a promessa de controlar qualquer mod automaticamente. Alguns ajustes dependerão de configurações, receitas, loot tables, permissões ou integrações específicas.

## 4 Personagens e raças

### 4.1 Criação no primeiro acesso

**Decidido:** escolher raça e personalizar o personagem. Deve ser possível identificar visualmente a raça mesmo sem armadura. Vantagens e desvantagens raciais são obrigatórias; nenhuma raça deve ser superior em todas as situações.

**Pendente:** número final de raças, opções de corpo e rosto, limites de personalização, efeitos exatos, possibilidade e custo de mudança de raça e tratamento de equipamentos após uma mudança.

### 4.2 Raças citadas

| Raça ou referência | O que o histórico sustenta | O que falta definir |
| --- | --- | --- |
| Orc | Exemplo explícito de raça com armaduras próprias e identidade de ferraria | Atributos, penalidades, anatomia, habilidades e catálogo |
| Khajiit ou raça felina | Exemplo explícito de raça com armaduras exclusivas | Nome final, variações felinas, anatomia, habilidades e catálogo |
| Alto Elfo | Referência em avaliação visual anterior do assistente | Inclusão final e desenho aprovado |
| Outras raças | Não há lista completa consolidada no contexto disponível | Seleção e aprovação pelo usuário |

Não interpretar esta tabela como autorização para incluir todas as raças de Skyrim. A inspiração temática não define automaticamente o elenco, o lore ou o catálogo do projeto.

### 4.3 Direção visual proposta para raças

As seguintes descrições são pontos de partida para a IA explorar; não são modelos já aprovados.

**Orc:** corpo robusto em proporções compatíveis com o modelo escolhido para Minecraft, feições fortes e presas integradas à geometria quando tecnicamente viável. Referência de vestuário cotidiano: tecidos grossos, couro e trabalho artesanal. Pele, presas, rosto e roupas devem ser arquivos ou camadas identificáveis conforme o pipeline.

**Khajiit ou felino:** rosto felino legível, orelhas e cauda tratadas como partes geométricas quando necessárias. Padrões de pelagem e cores devem manter leitura em pequena escala. Vestuário cotidiano permite reconhecer a anatomia sem funcionar como armadura. A armadura posterior precisa respeitar cabeça, orelhas e cauda.

**Alto Elfo, se aprovado:** silhueta mais esguia, orelhas alongadas e linguagem arcana refinada. Tecidos, metais e ornamentos devem diferenciar a cultura sem depender de brilho exagerado. A proporção final precisa caber no mesmo sistema de animação e combate.

**Representação sem armadura:** significa personagem com vestimenta cotidiana simples, sem equipamento de proteção. Não significa nudez. Para avaliação de textura, usar pose neutra, iluminação legível e vistas coerentes.

## 5 Afinidades classes e escolas mágicas

### 5.1 Estrutura decidida

O personagem terá uma afinidade principal mais forte e poderá aprender outras com desempenho inferior. Classes e especializações devem criar identidade, preservando liberdade para aprender. Não foi fechado se classe será uma escolha rígida, uma árvore de talentos ou uma descrição da build desenvolvida.

**Pendente:** fórmula da vantagem de afinidade, quantidade de especializações simultâneas, troca de especialização, restrições por raça e eventual diferença entre elemento e escola.

### 5.2 Vocabulário discutido

| Elemento ou disciplina | Situação no planejamento | Identidade funcional |
| --- | --- | --- |
| Fogo e Piromancia | Elemento desejado e escola usada nos exemplos | Magias de fogo; requisitos próprios |
| Água | Elemento desejado | Catálogo e efeitos pendentes |
| Ar | Elemento desejado | Catálogo e efeitos pendentes |
| Terra | Elemento desejado | Catálogo e efeitos pendentes |
| Conjuração | Disciplina citada em requisitos e ideias | Invocações e técnicas a definir |
| Ilusão | Técnicas de invisibilidade e engano discutidas | Organização em escola ainda proposta |
| Proteção e contramagia | Barreiras, bloqueios e respostas discutidos | Nome da escola e catálogo pendentes |

Gelo, cura, relâmpagos e outros temas não devem ser acrescentados como escolas confirmadas por associação com Skyrim. Só entram como propostas identificadas, se solicitadas posteriormente.

### 5.3 Conjuração por voz

**Decidido:** falar feitiços pelo microfone. As magias serão predefinidas; a fala não cria qualquer efeito arbitrário. O jogador precisa ter aprendido o feitiço e cumprir seus requisitos.

**Fluxo técnico proposto:** o cliente reconhece a expressão, relaciona-a a um identificador conhecido e solicita a ação. O servidor valida conhecimento, nível, recursos, recarga, estado de combate, alvo e zona antes de executar. Reconhecimento de voz sozinho não concede poder.

**Pendente:** idioma, vocabulário falado, tolerância a pronúncia, ativação por tecla, seleção de magia, interação com chat de voz, acessibilidade e solução de reconhecimento. Não foi escolhida uma biblioteca nem comprovada integração com o voice chat.

### 5.4 Técnicas discutidas

Ataques como Fireball, barreiras, invisibilidade, detecção, interrupções, dissipação, teleporte limitado, troca de lugar e fuga estratégica fazem parte do espaço de design. Cada técnica forte precisa de uma resposta possível e de limites de alcance, duração, custo ou preparação.

A troca de lugar é uma ideia desejada, mas seus alvos válidos e resistências ainda precisam ser definidos. Teleporte e invisibilidade não podem anular permanentemente o risco de transportar loot ou disputar eventos.

## 6 Progressão níveis e aprendizagem

### 6.1 Quatro camadas propostas

| Camada | Função | Referência inicial |
| --- | --- | --- |
| Nível geral | Talentos e acesso a equipamentos ou desafios | Máximo 60 proposto |
| Nível de escola | Proficiência necessária para magias | Máximo 100 proposto |
| Maestria | Técnicas avançadas e especialização | Provas e desbloqueios propostos |
| Conhecimento | Registro permanente das descobertas | Grimórios, missões e exploração |

Os tetos 60 e 100 são a base numérica proposta na conversa, não valores calibrados. O usuário pediu dificuldade elevada; a curva final deverá refletir tempo real de jogo e oportunidades de evolução.

### 6.2 Requisitos cumulativos

**Para aprender:** obter o conhecimento e cumprir os requisitos de aprendizagem. **Para conjurar:** já conhecer, ter proficiência suficiente, recursos e condições válidas. **Para equipar:** atender raça, nível e demais exigências do item. **Para fabricar:** atender profissão, receita, materiais e infraestrutura.

Um personagem pode possuir, transportar e comercializar uma peça sem poder equipá-la. Não exigir profissão de fabricação automaticamente de quem só quer usar o equipamento.

| Exemplo de magia | Requisito de escola proposto | Estado |
| --- | --- | --- |
| Flame Spark | Piromancia 1 | Exemplo de design |
| Fireball | Piromancia 10 e grimório específico | Exemplo de design |
| Flame Wall | Piromancia 25 | Exemplo de design |
| Inferno | Piromancia 50 | Exemplo de design |
| Meteor Storm | Piromancia 80 e prova de maestria | Exemplo de design |

Os exemplos do usuário de 5 níveis de Piromancia, 10 de Conjuração e nível 25 para uma armadura indicam a mecânica desejada. Não fecham, isoladamente, o requisito de todos os itens.

### 6.3 Ganho de experiência e leitura

Base proposta: missões, descobertas inéditas, aprendizado de novas técnicas, combate válido, chefes e provas concedem experiência conforme contribuição. Grimórios e livros são parte importante da evolução e da descoberta. Reler indefinidamente o mesmo texto ou lançar feitiços sem desafio não deve gerar progressão ilimitada.

O sistema RPG deve ter experiência própria, sem depender diretamente do XP vanilla. Defesas contra farm precisam verificar repetição, contexto e contribuição real. A regra exata para mobs presos, alvos combinados, duelos e criaturas invocadas continua pendente.

### 6.4 Curva numérica ilustrativa preservada

| Nível da escola | XP acumulado ilustrativo |
| --- | --- |
| 1 | 0 |
| 10 | 2.000 |
| 20 | 7.500 |
| 30 | 18.000 |
| 40 | 35.000 |
| 50 | 60.000 |
| 60 | 100.000 |
| 70 | 160.000 |
| 80 | 250.000 |
| 90 | 380.000 |
| 100 | 550.000 |

Esta tabela é um rascunho anterior, sem taxas de XP por atividade. Não permite prometer uma duração. A previsão discutida foi: sobrevivência inicial nos primeiros dias; aprendiz nas semanas 1 a 2; especialista nas semanas 3 a 5; mestre nas semanas 6 a 10; desafios recorrentes depois disso. São metas de ritmo, sem bloqueio obrigatório por calendário.

## 7 Armaduras equipamentos e raridade

### 7.1 Regras decididas e propostas

**Decidido:** determinados equipamentos serão raciais e exclusivos para uso por sua raça. Raça e nível podem ser requisitos para armaduras avançadas. Deve existir comércio entre jogadores.

**Base proposta:** armaduras pesadas favorecem proteção com custo de mobilidade ou conjuração; leves favorecem deslocamento com menor resistência; vestes favorecem eficiência arcana com proteção física inferior. Nenhuma categoria garante vitória.

Fabricar, usar e vender são permissões diferentes. Um Orc poderia fabricar uma peça felina caso tivesse receita, profissão e materiais, sem poder equipá-la. Essa abertura da produção foi proposta para evitar mercados isolados em um servidor pequeno.

### 7.2 Exemplo racial preservado

**Guardião Ancestral Orc** é um nome de exemplo, não peça final aprovada. Requisito de uso proposto: Orc, nível geral 25 e Prova Ancestral. Requisito de fabricação proposto: Ferraria 30, receita e materiais. Não exigir Ferraria 30 automaticamente do comprador.

### 7.3 Evolução e raridade

Foram discutidas aparências básica, intermediária, avançada e lendária, além de um estudo visual anterior com quatro níveis de equipamento. A escala final de tiers visuais permanece pendente. Separadamente, foram propostas cinco raridades: comum, incomum, rara, épica e lendária. Tier visual, raridade e nível de uso não precisam ser o mesmo campo.

Uma proposta é evoluir visualmente uma armadura racial: o mesmo conjunto ganha reforços, acabamento e detalhes com a progressão. Isso preserva a identidade cultural. A decisão sobre melhorar uma peça ou substituí-la ainda está aberta.

Itens lendários devem oferecer opções táticas e custos, sem imunidade absoluta. Foi proposta manutenção de equipamentos muito raros por reparo, evitando destruição permanente por desgaste comum. As regras finais de quebra, reparo e morte ainda não foram aprovadas.

### 7.4 Direção visual proposta para conjuntos

| Família | Silhueta e materiais propostos | Cuidados de produção |
| --- | --- | --- |
| Orc pesada | Placas fortes, couro, rebites e metal trabalhado; ornamentação crescente | Não inflar a silhueta a ponto de esconder leitura do personagem |
| Felina leve | Couro, tecidos e proteção segmentada; linhas ágeis | Respeitar cauda, orelhas, pescoço e articulações |
| Alto Elfo arcana, se aprovado | Vestes em camadas e metal fino com inscrições discretas | Preservar leitura em pixel art e animação |
| Vestes de escola | Identidade da disciplina por material, símbolo e cor | A escola não deve apagar a raça nem depender só da cor |

As cores e materiais desta direção visual ainda precisam de aprovação. Não gerar logotipos, símbolos ou lore como se já fossem cânone.

## 8 Armas e artefatos

O usuário pediu armas por raça ou classe. Não há catálogo fechado de tipos, nomes, danos ou restrições. Exclusividade racial das armaduras não implica automaticamente a mesma exclusividade de todas as armas.

**Referências visuais propostas para exploração:** armas robustas forjadas para a cultura Orc; lâminas leves ou arcos para uma cultura felina; cajados ou focos para um arquétipo arcano élfico, se incluído. Estas associações são estudos possíveis, não limitação confirmada das builds.

Uma ficha de arma deve separar alcance, velocidade, dano, custo de recurso, habilidade especial, raça permitida e proficiência. A aparência precisa comunicar peso e função. Uma arma pesada não deve parecer leve apenas por possuir uma textura rara.

Artefatos amaldiçoados foram propostos como itens que oferecem vantagem em troca de penalidade relevante. Joias mágicas, relíquias, componentes e itens de profissão precisam ter papel econômico ou tático definido antes da produção em massa.

## 9 Grimórios livros e conhecimento

### 9.1 Papel no jogo

**Decidido:** leitura, grimórios, livros e descoberta de magias participam da progressão. Conhecimentos aprendidos são permanentes. As magias podem ser descobertas em missões e dungeons.

**Base proposta:** um grimório ensina uma técnica ou contribui para aprendê-la apenas quando os requisitos são atendidos. Livros podem trazer pesquisa, lore e pistas. Fragmentos podem compor uma descoberta. A quantidade de leitura, consumo do livro, possibilidade de cópia e regras de comércio ainda estão abertas.

Não confundir o objeto comercializável com o conhecimento registrado no personagem. Perder um livro não deve apagar automaticamente uma magia aprendida.

### 9.2 Identidade visual proposta

| Tema | Materiais e cores iniciais para estudo | Elemento de reconhecimento |
| --- | --- | --- |
| Fogo | Couro escuro, cobre, vermelho queimado e âmbar | Marcas de calor e símbolo de chama |
| Água | Couro azul profundo, metal claro e verde azulado | Linhas fluidas e símbolo de onda |
| Ar | Tecido claro, cinza e detalhes leves | Espirais ou sinais de fluxo |
| Terra | Couro terroso, pedra, bronze e verde mineral | Inscrições angulares ou estratos |
| Conjuração | Capa antiga e metal escuro; cor ainda a aprovar | Selo ou círculo de invocação |

Todos os símbolos são propostas. Não usar apenas cor para diferenciar escolas. O livro deve ser reconhecível fechado, aberto, no inventário e em mãos, quando essas representações existirem.

### 9.3 Conteúdo da ficha de grimório

Registrar identificador, nome, escola, magia ensinada, requisitos, origem, raridade, regras de uso e comércio, capa, lombada, contracapa, páginas, ícone e estado aberto. O texto narrativo e os símbolos legíveis devem ser aplicados de modo controlado; não confiar em letras aleatórias geradas por imagem.

## 10 Dungeons exploração e chefes

**Decidido:** exploração e dungeons devem fornecer conteúdo, aprendizado e recompensas durante meses. Devem integrar a progressão mágica, não servir apenas como corredores de baús.

**Base proposta:** ruínas, criptas, bibliotecas ocultas e fendas temporárias podem combinar puzzles, armadilhas, inimigos, lore, grimórios e materiais. Chefes devem exigir leitura de padrões, respostas e domínio de técnicas, além de causar dano.

### 10.1 Famílias visuais propostas

| Família para estudo | Atmosfera e materiais | Conteúdo compatível com a proposta |
| --- | --- | --- |
| Cripta ancestral | Pedra envelhecida, inscrições, sarcófagos e luz contida | Armadilhas, enigmas, técnicas antigas |
| Biblioteca oculta | Estantes, madeira antiga, páginas, selos e poeira | Pesquisa, grimórios e pistas |
| Santuário elemental | Arquitetura e fenômenos do elemento escolhido | Prova de escola e materiais temáticos |
| Fenda dimensional | Ruptura temporária em ruínas ou terreno | Disputa, instabilidade e recompensa contestável |

Essas famílias são referências de produção; nomes, biomas, bosses e estruturas finais não foram aprovados. Nenhum mob específico foi confirmado neste catálogo.

### 10.2 Ficha obrigatória de dungeon

Registrar função, tema, acesso, dificuldade, requisitos, regras de PvP, perigos, puzzles, inimigos, chefe, loot, repetibilidade, reposição e saída. Para arte: paleta, kit de blocos, portas, pilares, pisos, baús, altares, iluminação, props e marcadores de perigo. A solução do puzzle não deve depender de uma diferença de cor quase imperceptível.

A disponibilidade de conhecimento importante precisa considerar novos jogadores e reposição. Exclusividade cosmética pode existir; monopolizar para sempre uma resposta essencial de combate foi uma ideia rejeitada na proposta anterior.

## 11 PvP zonas seguras e consequências

### 11.1 Formato decidido

Todos contra todos, sem times permanentes. Jogadores podem se enfrentar, disputar loot e fugir. Lojas devem ter anti-PvP. O equilíbrio precisa permitir contrajogo por barreiras, invisibilidade, detecção, reposicionamento e outros recursos.

Não foi aprovada uma política automática de punição por alianças informais. A ausência de times no sistema não impede acordos entre pessoas; regras e eventual moderação precisam ser definidas separadamente.

### 11.2 Matriz de zonas proposta

| Local | PvP | Roubo e loot | Estado |
| --- | --- | --- | --- |
| Lojas | Desativado | Estoques e comércio protegidos | Anti-PvP decidido; detalhes propostos |
| Cidade central | Desativado | Proteção ampla | Proposta |
| Base privada | A definir | Proteção de construção a definir | Pendente |
| Mundo selvagem | Ativo | Perda conforme regra futura | Direção decidida; detalhes pendentes |
| Dungeon competitiva | Ativo | Recompensas disputáveis | Proposta |
| Evento mundial | Ativo | Loot disputável e fuga | Intenção decidida; extração proposta |

Foi proposto estado de combate de cerca de 20 segundos, renovado por hostilidade, para evitar invulnerabilidade imediata ao entrar em loja ou usar teleporte. Duração e implementação são pendentes. Projéteis, área de efeito, invocações e bordas de zona precisam obedecer à mesma política.

### 11.3 Morte e risco

Aprendizado permanente não deve ser roubado. A proposta separa recursos comuns, loot de evento e progressão permanente. Perda de equipamentos pessoais, saque do inventário, reparos e recuperação ainda exigem decisão. Não afirmar que full loot ou keepInventory foi escolhido.

A intenção é permitir consequências reais para a derrota sem apagar semanas de conhecimento adquirido. Proteção de iniciantes e limites de perseguição foram sugeridos; regras exatas continuam abertas.

## 12 Eventos mundiais e Xaero

**Decidido:** eventos anunciam coordenadas e atraem jogadores para uma recompensa disputada. É válido interceptar, roubar o loot disponível e fugir conforme as regras. Abrir o baú não precisa encerrar a disputa.

| Evento proposto | Recompensa e dinâmica |
| --- | --- |
| Baú do Caos | Baú público com recursos e utilidades de recompensa moderada |
| Queda Arcana | Meteorito ou ocorrência com cristais e materiais disputados |
| Fenda Dimensional | Dungeon temporária com risco de PvE e PvP |
| Artefato Perdido | Receita, técnica ou componente raro, sem poder absoluto |
| Caçada Mundial | Chefe anunciado com recompensa e disputa entre participantes |

**Fluxo proposto:** anúncio, deslocamento, tempo de preparação, liberação da recompensa, saque, perseguição e conclusão ou extração. Um aviso pode informar X, Y, Z, dimensão e tempo restante. Os 10 minutos de desbloqueio citados antes são um exemplo, não parâmetro fechado.

A integração automática com Xaero’s Minimap depende da versão e das interfaces disponíveis. Alternativa proposta: coordenadas no chat/HUD e marcação manual. Não prometer waypoint automático nem radar especial antes de validar tecnicamente.

Regras propostas: não surgir dentro de base privada; impedir proteção particular sobre o objetivo; controlar teleporte, voo e armazenamento durante a disputa; validar estoque e saque no servidor. Frequência, mínimo de participantes, janela de participação, extração e prevenção de acampamento continuam pendentes.

## 13 Profissões comércio e economia

**Decidido:** jogadores devem poder comercializar, ter lojas e negociar equipamentos. A economia deve coexistir com a competição individual.

**Profissões propostas:** ferraria, encantamento, alquimia, alfaiataria e joalheria mágica. Conhecimentos básicos podem ser amplos, enquanto produção avançada depende de especialização, receita e infraestrutura. O limite de especializações não foi fechado.

**Modalidades propostas:** loja física, troca direta, encomenda com materiais e pagamento, e marketplace futuro no CaosPhone. CaosBank representaria pagamentos em moeda virtual do jogo; não se trata de integração bancária real.

Uso, fabricação e venda devem continuar separados. Receitas negociáveis, reparos, melhorias, reagentes e serviços podem sustentar demanda recorrente. Ainda faltam fontes de moeda, custos, limites e política de preços. O exemplo de armadura por 2.500 moedas foi ilustrativo.

O servidor deve validar saldo, posse, estoque e entrega, com transações consistentes e proteção contra duplicação. O acesso remoto do CaosPhone a estoque e entrega não pode eliminar o risco de transportar loot em PvP.

## 14 Auditoria do modpack e controle de poder

### 14.1 Direção atual do usuário

Retirar mods que facilitam demais e reorganizar o servidor ao redor do RPG. Uma mineradora que obtenha indiscriminadamente todos os minérios e elimine a necessidade de exploração é incompatível com a intenção. A orientação mais recente é mais restritiva do que a proposta anterior de manter tecnologia com manutenção.

Isso não autoriza remoção cega de arquivos. O inventário real, dependências, configurações e efeitos no mundo ainda precisam ser lidos. Não há nesta documentação uma lista comprovada de mods a excluir.

### 14.2 Política de avaliação preservada

| Recurso | Direção para a auditoria |
| --- | --- |
| Mineradoras e geradores ilimitados de minérios | Candidatos prioritários a remoção |
| Duplicação ou transmutação livre de recursos | Remover ou bloquear a mecânica |
| Farms extremas de XP | Impedir atalho para a progressão RPG |
| Armaduras invulneráveis e armas desproporcionais | Ajustar ou remover |
| Voo e teleporte irrestritos | Remover ou limitar conforme risco e progressão |
| Mochilas e armazenamento remoto excessivos | Avaliar capacidade, acesso e interação com PvP |
| Radar de jogadores, entidades e cavernas | Revisar; detecção não deve anular exploração e magia |
| Waypoints e navegação | Preservação proposta para exploração e eventos |
| Construção e decoração | Preservar se não criam atalhos de poder |
| Biomas, estruturas e dimensões | Avaliar loot, acesso, dificuldade e desempenho |
| Otimização | Preservar quando compatível e sem efeitos de gameplay indevidos |

Cada mod deverá receber nome, ID, versão, dependências, função, impacto em combate/economia/mobilidade/progressão, decisões e testes. Backups, migração e homologação precedem aplicação no servidor principal.

O balanceamento deve verificar combinações, não só itens isolados: reduções acumuladas de custo, resistências, invocações, encantamentos, dano de mods externos, recursos renováveis e formas de extrair loot com segurança absoluta.

## 15 As vinte ideias discutidas

As ideias abaixo permanecem no planejamento como propostas a desenvolver. Não representam vinte sistemas já implementados ou especificados.

1. Grimórios com requisitos de proficiência e descoberta real.
2. Academias de magia com instrutores, bibliotecas, missões e exames.
3. Árvores de talentos com escolhas que diferenciam builds.
4. Provas de maestria para técnicas e patamares avançados.
5. Progressão racial com técnicas e identidade próprias.
6. Armaduras raciais condicionadas a raça, nível e conquista.
7. Profissões e fabricação com especializações.
8. Economia de jogadores com lojas, serviços e encomendas.
9. Dungeons com progressão, puzzles e armadilhas.
10. Chefes com barreiras, contramagias e padrões de combate.
11. Eventos mundiais de disputa com coordenadas públicas.
12. Arena de duelos, histórico, ranking e temporadas.
13. Contramagias e respostas táticas a estratégias fortes.
14. Artefatos amaldiçoados com vantagem e contrapartida.
15. Pedras de destino ou altares com uma bênção ativa por vez.
16. Pesquisa arcana com fragmentos, materiais e resultados predefinidos.
17. Contratos individuais de caça com regras contra abuso.
18. Sobrevivência ambiental por frio, calor, doenças e clima.
19. Conhecimentos secretos encontrados por enigmas, NPCs e ruínas.
20. Crônicas pessoais de descobertas, magias, chefes, duelos e conquistas.

## 16 Direção de arte e produção de texturas

### 16.1 O que foi pedido

O usuário quer visualizar como o conteúdo existiria no Minecraft, com fantasia medieval inspirada em Skyrim. Pediu raças sem armadura, armaduras por raça, armas por raça ou classe, grimórios, livros, conjurações, elementos e dungeons. Pediu explicitamente imagens unitárias separadas e sem repetição, não dez imagens comprimidas em uma montagem.

O conteúdo solicitado nesta etapa é documentação para uso posterior. Não foram produzidas novas imagens nem texturas instaláveis junto deste documento.

### 16.2 Linguagem visual proposta

Usar geometria compatível com Minecraft, texturas em pixel art e materiais de fantasia com leitura clara. A inspiração de Skyrim deve orientar atmosfera, culturas, ruínas e artesanato. O resultado precisa funcionar em escala de jogo, inventário e combate.

Pelo menos três sinais devem diferenciar famílias de assets: silhueta, material e motivo gráfico. Cor é um apoio. Detalhes minúsculos e brilho constante tendem a prejudicar leitura; reservar os efeitos mais intensos para ações importantes é uma orientação visual proposta.

Paleta, resolução e grau de detalhe ainda não foram aprovados. Uma IA deve manter coerência entre versões e registrar qualquer proposta de mudança antes de tratá-la como padrão.

### 16.3 Conceito imagem textura e modelo são entregas diferentes

| Entrega | O que contém | O que não comprova |
| --- | --- | --- |
| Concept art | Aparência, materiais, atmosfera e intenção | Compatibilidade ou textura instalável |
| Vistas de referência | Frente, costas e lados coerentes | UV pronta ou animação funcional |
| Textura | Pixels destinados a uma superfície ou UV definida | Modelo, lógica ou registro do item |
| Modelo | Geometria e mapeamento necessários ao asset | Combate, física ou equilíbrio |
| Ícone | Representação legível no inventário | Aparência completa em terceira pessoa |
| Efeito visual | Partícula, símbolo ou animação de magia | Aplicação real de dano ou validação |

Para orelhas, caudas, presas volumétricas, capacetes e silhuetas especiais, uma imagem de textura pode ser insuficiente. Modelos e animações personalizados podem ser necessários. Não escolher um formato de runtime antes de validar o pipeline do projeto.

### 16.4 Parâmetros de exportação a decidir

Confirmar antes de gerar texturas finais: escala de pixels por categoria, tamanho do atlas, UV, proporções de personagem, suporte a transparência, camadas emissivas, animação e formato de modelo. PNG e nomes técnicos em minúsculas são uma base de organização proposta; a configuração final depende do runtime.

Uma skin convencional pode usar um layout próprio do Minecraft, mas uma raça com geometria especial precisa do UV do seu modelo real. Não presumir que todas as raças possam ser entregues em uma única skin padrão.

Para orientação inicial, podem ser testados ícones de 16 ou 32 pixels e blocos de 16 ou 32 pixels; isso é hipótese de estudo, não padrão aprovado. A resolução das imagens conceituais pode ser maior e não deve ser confundida com a resolução da textura.

### 16.5 Organização de arquivos proposta

Usar identificadores estáveis, sem acentos ou espaços, com prefixo do projeto quando o pipeline exigir. Exemplos de convenção: `orc_guardiao_ancestral`, `grimorio_piromancia`, `cripta_ancestral_piso`. Esses identificadores são sugestões e não registros existentes.

Separar pastas de conceitos, referências, texturas, modelos, ícones, efeitos e fichas. Manter versões e registrar relações entre raça, tier, escola e item. Um exemplo de caminho lógico futuro é `textures/item/grimorio_piromancia.png`; ele não indica que o arquivo ou namespace já existam.

### 16.6 Critérios de avaliação visual

- A raça é reconhecível sem armadura e com equipamento.
- O item comunica sua função e não contradiz peso ou material.
- Vistas diferentes representam o mesmo objeto.
- Orelhas, caudas, presas e articulações não atravessam equipamento de forma indevida.
- Texturas respeitam UV e não exibem iluminação pintada incompatível entre faces.
- Ícones mantêm leitura em tamanho real de inventário.
- Efeitos permitem perceber início, trajetória, impacto e encerramento de uma ação.
- Raridade e evolução são legíveis sem depender só de brilho ou cor.
- Arquivos usam a escala e o formato aprovados para sua categoria.
- Uma imagem ilustrativa não é apresentada como prova de funcionamento no jogo.

## 17 Fichas para orientar uma IA de arte

### 17.1 Ficha universal de asset

Preencher antes da geração: identificador; nome; estado de aprovação; categoria; raça; classe ou escola; função; raridade; tier visual; requisitos de uso; requisitos de fabricação; origem no mundo; descrição da silhueta; materiais; paleta; símbolos; desgaste; partes móveis; efeitos; vistas necessárias; modelo/UV de destino; resolução; transparência; variantes; referências aprovadas; restrições; arquivos esperados e critérios de aceite.

Campos desconhecidos devem permanecer como **PENDENTE**, sem preenchimento inventado. A ficha mecânica informa o visual, mas a IA de imagem não decide atributos de jogo.

### 17.2 Complementos por categoria

**Raça:** proporções, rosto, pele ou pelagem, orelhas, cauda, presas, personalização, roupa cotidiana e compatibilidade com equipamento.

**Armadura:** peças independentes, encaixe no corpo, juntas, cobertura, interior visível, evolução entre tiers, identificação racial e interação com animações.

**Arma:** empunhadura, lâmina ou foco, peso aparente, alcance visual, posição em mãos, ícone e eventuais estados de uso.

**Grimório:** capa, lombada, contracapa, fechos, páginas, símbolo da escola, estado aberto/fechado, legibilidade em mãos e no inventário.

**Magia:** preparação, emissão, trajetória, área, impacto, duração, dissipação e sinais de contrajogo. Área desenhada e área efetiva devem coincidir na implementação.

**Dungeon:** kit modular, dimensões, transições de blocos, props, iluminação, pistas, portas, altares, baús, perigos e versões intactas/danificadas quando necessárias.

### 17.3 Instrução reutilizável para outra IA

> Use esta documentação como referência de continuidade do Caos Arcana. Preserve as decisões marcadas como Decidido. Trate Base proposta e Referência visual proposta como hipóteses de trabalho, sem convertê-las em cânone. Gere somente o asset solicitado e mantenha os campos pendentes explícitos. Para imagem conceitual, apresente uma peça ou cena por arquivo, sem montar vários pedidos em uma única prancha. Para textura final, exija o modelo ou UV de destino, a resolução e o formato antes de declarar o arquivo instalável. Não acrescente raças, escolas, bônus, nomes ou símbolos definitivos sem orientação. Mantenha a identidade do mesmo asset entre vistas e variantes e informe se a entrega é conceito, textura, modelo ou efeito.

## 18 Plano de dez imagens unitárias

A solicitação de dez imagens separadas permanece registrada. A seleção abaixo é uma organização proposta para cobrir o conteúdo sem repetir imagem. Não substitui a aprovação do elenco final e não significa que esses arquivos já foram gerados.

| Ordem | Imagem individual proposta | Objetivo |
| --- | --- | --- |
| 1 | Orc com vestimenta cotidiana e sem armadura | Anatomia e identidade racial |
| 2 | Khajiit ou felino com vestimenta cotidiana | Pelagem, cabeça, orelhas e cauda |
| 3 | Alto Elfo sem armadura, se aprovado | Identidade de uma terceira raça |
| 4 | Armadura Orc avançada isolada | Material, proteção e encaixe racial |
| 5 | Armadura felina isolada | Mobilidade e anatomia |
| 6 | Vestes arcanas da terceira raça aprovada | Silhueta de conjurador |
| 7 | Uma arma racial ou de classe escolhida | Forma, material e uso |
| 8 | Um grimório de Piromancia | Capa e identidade da escola |
| 9 | Uma conjuração elemental em ação | Preparação, efeito e leitura de combate |
| 10 | Uma dungeon e seu ponto focal | Atmosfera, arquitetura e recompensa |

Cada pedido corresponde a um arquivo, não a colagem. Uma prancha técnica com vistas só deve ser produzida quando explicitamente solicitada para aquele asset. As demais armas, elementos, grimórios e armaduras entrarão em lotes seguintes, sem fingir que dez arquivos cobrem todo o catálogo.

## 19 Desenvolvimento validação e pendências

### 19.1 Ordem de trabalho proposta

1. Recolher inventário real de mods, configurações, datapacks e versões.
2. Auditar dependências, atalhos de progressão e combinações de poder.
3. Aprovar regras gerais de progressão, PvP, zonas, morte e economia.
4. Fechar elenco inicial de raças, escolas e catálogo mínimo.
5. Aprovar direção visual por conceitos individuais.
6. Definir modelo, UV e resolução antes de texturas finais.
7. Produzir um conjunto piloto coerente de raça, armadura, arma e grimório.
8. Implementar e testar um ciclo completo: aprender, lutar, saquear, fugir e negociar.
9. Testar com jogadores, ajustar ritmo e ampliar conteúdo.
10. Aplicar mudanças no servidor principal apenas após homologação.

### 19.2 Testes de gameplay prioritários

Verificar aquisição indevida de XP por repetição, requisitos de aprendizado e equipamento, acúmulo de resistências, dano externo, duplicação, invasão de zonas seguras, fuga por teleporte, armazenamento remoto de loot, desconexão em combate e reposição de conhecimentos raros. Medir tempo real até patamares úteis, diferença entre iniciante e veterano e participação efetiva nos eventos.

Ainda não há metas numéricas de dano, tempo para derrotar um jogador, taxa de loot ou horas por nível. Esses parâmetros precisam ser estabelecidos e testados; não devem ser inventados pelo gerador de texturas.

### 19.3 Decisões abertas essenciais

- Nome definitivo do mod e identidade gráfica.
- Lista final de raças e aparência aprovada de cada uma.
- Modelo de classes e relação entre afinidade, elemento e escola.
- Nomes, pronúncias, requisitos, custos e contrajogo das magias.
- Atributos e penalidades raciais e limites globais de poder.
- Curva de XP, tetos e provas de maestria.
- Catálogo de armas e regras de exclusividade de uso.
- Tiers de armadura e diferença entre tier, nível e raridade.
- Regras de morte, loot, reparo e preservação do equipamento.
- Limites das zonas seguras e tratamento de combate nas bordas.
- Eventos, frequência, extração e integração real com Xaero.
- Especializações profissionais, receitas, fontes e consumo de moeda.
- Mods efetivamente removidos, mantidos ou configurados.
- Pipeline de modelos, UV, resolução, animação e efeitos.
- Referências visuais aprovadas que deverão acompanhar futuras gerações.

## 20 Registro de continuidade

Esta versão consolida a conversa fornecida e as decisões anteriores recuperadas sobre criação de personagem, afinidades, voz, aprendizado permanente e equipamentos raciais. O histórico é a origem dos exemplos numéricos e das vinte ideias. Os detalhamentos visuais e as fichas foram organizados para uso futuro e estão identificados como propostas quando não existia definição.

Não foram auditados arquivos do servidor, consultadas APIs de mods nem inspecionadas as imagens conceituais anteriores. Portanto, o documento registra intenção e especificação de trabalho, sem certificar implementação, compatibilidade ou aparência final.

Nas próximas versões, registrar data, item alterado, decisão do usuário e assets afetados. Novas escolhas devem atualizar a ficha correspondente, preservando as decisões anteriores que não tenham sido explicitamente substituídas.
