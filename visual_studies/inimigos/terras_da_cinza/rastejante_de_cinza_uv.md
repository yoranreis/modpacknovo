# UV — Rastejante de Cinza v0.1

Textura 64 x 64 px, com fundo transparente. O atlas foi organizado para cubos de modelo Java Fabric usando CubeListBuilder.texOffs(u, v).addBox(x, y, z, width, height, depth). A frente do mob aponta para +Z.

| Parte | Caixa (largura x altura x profundidade) | UV inicial |
|---|---:|---:|
| Corpo/carapaça | 10 x 5 x 7 | (0, 0) |
| Cabeça | 5 x 4 x 4 | (36, 0) |
| Abdômen | 6 x 3 x 4 | (0, 20) |
| Pernas (seis, atlas compartilhado) | 2 x 3 x 2 | (22, 20) |
| Mandíbulas (par, atlas compartilhado) | 2 x 2 x 1 | (32, 20) |

Cada caixa usa o desdobramento padrão do Minecraft: topo, base e faixa das faces laterais, frontal e traseira. Pernas e mandíbulas reutilizam os pixels; podemos dar UVs próprios se quisermos assimetria.

As fissuras luminosas ficam na frente da carapaça. Os olhos amarelos estão na face frontal da cabeça. Topo e laterais usam tons variados de basalto; abdômen e juntas recebem reflexos menores de brasa.

## Uso futuro

Use atlas 64 x 64 e os offsets da tabela ao criar cada CubeListBuilder. As dimensões e UV formam uma proposta inicial de modelo, ainda não uma entidade implementada. Ao integrar, conferiremos a orientação das faces renderizadas e ajustaremos qualquer face invertida.

## Estado da integracao

A geometria inicial esta em src/client/java/br/com/caosarcana/client/model/RastejanteDeCinzaModel.java; a camada e registrada pelo inicializador do cliente. A textura foi copiada para src/main/resources/assets/caosarcana/textures/entity/terras_da_cinza/rastejante_de_cinza.png. A entidade, os atributos, a IA basica e o renderer ja estao conectados. Por enquanto, invoque com /summon caosarcana:rastejante_de_cinza; ainda nao ha spawn natural por bioma.

