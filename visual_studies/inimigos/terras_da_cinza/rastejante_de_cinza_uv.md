# UV — Rastejante de Cinza v0.2

Textura 64 x 64 px, com fundo transparente. O atlas foi organizado para cubos de modelo Java Fabric usando CubeListBuilder.texOffs(u, v).addBox(x, y, z, width, height, depth). A frente do mob aponta para -Z, o sentido frontal padrão dos modelos de entidades do Minecraft.

| Parte | Caixa (largura x altura x profundidade) | UV inicial |
|---|---:|---:|
| Corpo/carapaça | 10 x 5 x 7 | (0, 0) |
| Cabeça | 5 x 4 x 4 | (36, 0) |
| Abdômen | 6 x 3 x 4 | (0, 20) |
| Pernas (seis, atlas compartilhado) | 2 x 3 x 2 | (22, 20) |
| Mandíbulas (par, atlas compartilhado) | 2 x 2 x 1 | (32, 20) |

Cada caixa usa o desdobramento padrão do Minecraft: topo, base e faixa das faces laterais, frontal e traseira. Pernas e mandíbulas reutilizam os pixels; podemos dar UVs próprios se quisermos assimetria. Nesta versão, todas as faces das caixas receberam pixels opacos para evitar áreas vazias no modelo. Os olhos foram colocados na face frontal (-Z) da cabeça; as fissuras de brasa aparecem no topo e na frente da carapaça.

As fissuras luminosas ficam na frente da carapaça. Os olhos amarelos estão na face frontal da cabeça. Topo e laterais usam tons variados de basalto; abdômen e juntas recebem reflexos menores de brasa.

## Uso futuro

Use atlas 64 x 64 e os offsets da tabela ao criar cada CubeListBuilder. O atlas do jogo e a prévia ampliada foram regenerados com esse mapeamento. A orientação das faces ainda precisa da confirmação final dentro do Minecraft.

## Estado da integracao

A geometria inicial esta em src/client/java/br/com/caosarcana/client/model/RastejanteDeCinzaModel.java; a camada e registrada pelo inicializador do cliente. A textura foi copiada para src/main/resources/assets/caosarcana/textures/entity/terras_da_cinza/rastejante_de_cinza.png. A entidade, os atributos, a IA basica e o renderer ja estao conectados. Por enquanto, invoque com /summon caosarcana:rastejante_de_cinza; ainda nao ha spawn natural por bioma.

