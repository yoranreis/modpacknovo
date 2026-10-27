# UV — Cão Fumegante v0.4

Atlas de 64 x 64 px, com fundo transparente, para cubos Java Fabric criados com CubeListBuilder.texOffs(u, v).addBox(...). A frente do modelo aponta para -Z.

| Parte | Caixa (largura x altura x profundidade) | UV inicial |
|---|---:|---:|
| Corpo | 8 x 6 x 11 | (0, 0) |
| Pescoço | 4 x 9 x 4 | (0, 34) |
| Cabeça | 6 x 5 x 5 | (40, 0) |
| Focinho superior | 4 x 2 x 3 | (40, 12) |
| Nariz largo | 3 x 2 x 2 | (46, 22) |
| Maxilar inferior | 4 x 1 x 3 | (40, 30) |
| Interior da boca | 3 x 1 x 1 | (40, 38) |
| Orelhas altas | 2 x 4 x 1 | (28, 22), (36, 22) |
| Pernas (quatro, atlas compartilhado) | 3 x 4 x 3 | (0, 22) |
| Cauda | 2 x 2 x 4 | (14, 22) |

O desdobramento de cada caixa segue o padrão do Minecraft: topo, base, faces laterais, frente (-Z) e traseira (+Z). A cabeça é filha do pescoço para acompanhar sua rotação sem se soltar do corpo. O nariz é filho do focinho e se sobrepõe à sua ponta para não ficar flutuando. O maxilar inferior é uma peça separada com uma linha escura de boca e interior avermelhado, pronta para receber animação de uivo rouco.

O arquivo cao_fumegante.png dentro de assets/caosarcana/textures/entity/terras_da_cinza é a textura do jogo. cao_fumegante_uv_preview.png é uma ampliação nearest-neighbor do atlas para inspeção.

## Entidade

Invocação de teste: /summon caosarcana:cao_fumegante.

O Cão Fumegante tem 10 pontos de vida (5 corações), 2 pontos de dano e velocidade 0.34. Ele persegue jogadores e animais passivos, mas ignora criaturas domesticáveis e inimigos hostis. Usa os sons do lobo com tom reduzido; ao atacar, rosna com um som grave. Cão e Rastejante recebem metade do dano de fontes marcadas como fogo através da classe compartilhada TerrasDaCinzaMob. Fumaça leve sai do corpo; ao correr, pode soltar cinzas, e um acerto emite uma baforada curta com brasas.

## Silhueta v0.2

O atlas permanece 64 x 64 px. A silhueta mede aproximadamente 24 px (1,5 bloco) do topo das orelhas às patas e perto de 24 px do focinho à ponta da cauda. O tronco é baixo e estreito; as pernas alongadas ficam abaixo do ventre, sem atravessá-lo. A caixa de colisão mede 0,95 x 1,5 bloco.
