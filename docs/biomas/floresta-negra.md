# Floresta Negra — Vila Oculta

## Identidade do bioma

Uma mata fechada ao redor de uma vila antiga. Os encontros devem usar árvores, copas e baixa visibilidade para criar emboscadas, enquanto sinais visuais e sonoros dão ao jogador tempo para reagir.

## Inimigos

| Inimigo | Papel no combate | Mecânica central | Estado |
| --- | --- | --- | --- |
| Guardião de Madeira Negra | Emboscador de curta distância | Camufla-se parado perto de troncos e folhas; revela-se ao mover, receber dano ou quando o jogador se aproxima | Implementado; aguardando teste em jogo |
| Aranha de Casca Solta | Caçadora vertical | Prepara teias nas copas, aplica lentidão e salta sobre o alvo | Implementado; aguardando teste em jogo |
| Espírito da Névoa | Controle de área | Flutua a até 4 blocos, dispara orbe com bafo de dragão que causa lentidão e cegueira | Implementado; aguardando teste em jogo |
| Cultista Oculto | Conjurador e suporte | Dispara projéteis de maldição (fraqueza) com báculo arcano e invoca espíritos para distrair | Implementado; aguardando teste em jogo |

## Chefe: Ancião das Sombras

Árvore humanoide ancestral que controla uma arena cercada por raízes. A luta pode evoluir em três etapas: raízes limitam rotas de fuga; sombras duplicadas confundem o alvo; folhas venenosas fecham partes da arena. Cada etapa deve ter sinais claros e pausas para o jogador reagir.

## Testes atuais

Para invocar e testar os mobs no mundo:

```mcfunction
/summon caosarcana:guardiao_madeira_negra ~ ~ ~
/summon caosarcana:aranha_casca_solta ~ ~ ~
/summon caosarcana:espirito_nevoa ~ ~ ~
/summon caosarcana:cultista_oculto ~ ~ ~
```
