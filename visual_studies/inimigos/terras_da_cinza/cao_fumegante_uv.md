# UV — Cão Fumegante v0.1

Atlas de 64 x 64 px, com fundo transparente, para cubos Java Fabric criados com CubeListBuilder.texOffs(u, v).addBox(...). A frente do modelo aponta para -Z.

| Parte | Caixa (largura x altura x profundidade) | UV inicial |
|---|---:|---:|
| Corpo | 8 x 6 x 11 | (0, 0) |
| Cabeça | 6 x 5 x 5 | (40, 0) |
| Focinho | 4 x 3 x 3 | (40, 12) |
| Nariz | 2 x 2 x 1 | (46, 22) |
| Orelhas (atlas compartilhado) | 2 x 2 x 1 | (28, 22), (36, 22) |
| Pernas (quatro, atlas compartilhado) | 3 x 4 x 3 | (0, 22) |
| Cauda | 2 x 2 x 4 | (14, 22) |

O desdobramento de cada caixa segue o padrão do Minecraft: topo, base, faces laterais, frente (-Z) e traseira (+Z). Todas as faces do modelo têm pixels opacos. Os olhos ficam na face frontal da cabeça; o focinho e o nariz são mais escuros. Fissuras de brasa atravessam o dorso e aparecem nas laterais.

O arquivo cao_fumegante.png dentro de assets/caosarcana/textures/entity/terras_da_cinza é a textura do jogo. cao_fumegante_uv_preview.png é uma ampliação nearest-neighbor do atlas para inspeção.

## Entidade

Invocação de teste: /summon caosarcana:cao_fumegante.

O Cão Fumegante tem 10 pontos de vida (5 corações), 2 pontos de dano e velocidade 0.34. Fumaça leve sai do corpo; ao correr, pode soltar cinzas, e um acerto emite uma baforada curta com brasas.
