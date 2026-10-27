# UV — Mineiro Oco v0.1

Atlas de 64 x 64 px para o modelo Java `MineiroOcoModel`. A frente do modelo aponta para -Z. As ilhas usam cubos padrão do `CubeListBuilder`.

| Parte | Caixa (largura x altura x profundidade) | UV inicial |
|---|---:|---:|
| Casaco/torso | 8 x 10 x 4 | (0, 0) |
| Cabeça | 6 x 7 x 6 | (32, 0) |
| Capacete e aba | 8 x 3 x 8 | (32, 20) |
| Braços (atlas compartilhado) | 3 x 10 x 3 | (0, 20), (14, 20) |
| Pernas (atlas compartilhado) | 3 x 7 x 3 | (0, 38), (14, 38) |
| Cabo da picareta | 1 x 10 x 1 | (28, 40) |
| Cabeça da picareta | 8 x 2 x 2 | (34, 40) |
| Ponta da picareta | 2 x 4 x 2 | (54, 40) |

Os olhos em brasa ficam na frente da cabeça, sob o capacete. A roupa usa couro queimado e carvão; a picareta tem cabo de madeira escura e cabeça de basalto rachado com veios de brasa.

## Comportamento

Invocação de teste: `/summon caosarcana:mineiro_oco ~ ~ ~`.

O Mineiro Oco tem 18 pontos de vida (9 corações), 3 pontos de dano, velocidade 0.23 e armadura 1. Persegue jogadores com passos lentos e mantém a picareta erguida durante a aproximação. Como criatura das Terras da Cinza, recebe metade do dano de fogo através de `TerrasDaCinzaMob`.
