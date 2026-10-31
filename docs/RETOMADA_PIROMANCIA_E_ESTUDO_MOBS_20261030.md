# Retomada de Piromancia e estudo preliminar de mobs — 30/10/2026

## Piromancia: revisão do protótipo existente

Escopo preservado: uma raiz e dois focos mutuamente exclusivos. Sem novos nós,
magias ou números de balanceamento. A proposta de 28/10 registra o estágio
histórico anterior; o estado operacional informado pelo usuário é o protótipo
funcional no Lote B.

Correção nesta retomada: `CaosArcanaTalentBridge.canUnlock` agora limita suas
restrições à categoria exata `caosarcana:piromancia`. Anteriormente, uma categoria
diferente no namespace `caosarcana` poderia ser bloqueada se reutilizasse um dos
IDs conhecidos. O teste existente da ponte foi ampliado para cobrir categorias
externas, outra categoria própria e categoria nula com os três IDs de talentos.

Inspeção estática confirmou conexões de raiz e exclusão nos JSONs, aplicação
opcional do Mixin, acesso ao mod externo em classe interna e conferência de
marcos pendentes no login. Isso não substitui teste de carregamento sem o mod,
nem homologação de compras e persistência em servidor real.

Validação automatizada desta retomada: `gradlew.bat test --offline --no-daemon
--console=plain` concluído com `BUILD SUCCESSFUL`. Cache de execução isolado em
`caos-arcana-mod/.gradle/isolated-home`, sem mudança nos parâmetros do projeto.
O launcher utilizado foi Java 25; compilação continua com `release = 17`.
Execução real do mod sob Java 17 permanece parte da homologação no Lote B.

### Roteiro de homologação no Lote B

1. Com personagem de teste sem Centelha, conceder um ponto com
   `/caos talent points <jogador> 1`. Abrir `/caos talent open` e tentar comprar
   a raiz: deve bloquear sem consumir o ponto.
2. Estudar o grimório; comprar a raiz e verificar custo de 15 de mana,
   mantendo custo base de 20 para personagem sem talento.
3. Usar `/caos setschool piromancia 10` e depois nível 25; conferir a concessão
   de cada marco uma única vez. Considerar o ponto administrativo do passo 1
   separadamente ao conferir o saldo.
4. Comprar Combustão: 5 de dano inicial e 10 segundos de fogo conforme padrão
   atual. Tentar comprar Cinético: bloqueado sem débito.
5. Executar `/caos talent reset <jogador>`: conferir estorno e Centelha ainda
   conhecida. Comprar raiz e Cinético: conferir recuo e ausência de novo fogo.
   O protótipo atual mantém 5 de dano inicial; aumento numérico fica pendente.
6. Morrer, reconectar e reiniciar o servidor: conferir conhecimento, XP, nós,
   saldo e ausência de duplicação dos marcos.
7. Em cópia isolada do ambiente, testar inicialização sem Pufferfish's Skills:
   Centelha base funciona e marcos ainda não entregues permanecem pendentes.

Nenhum JAR foi copiado por esta retomada. O hash fornecido pelo usuário continua
sendo referência do artefato anterior; não identifica o código corrigido aqui.

## Estudo preliminar: dificuldade e HUD

Somente levantamento documental. Nenhum mod instalado e nenhuma configuração
de dificuldade aplicada. Consultadas páginas oficiais em 30/10/2026.

| Candidato | Evidência publicada | Avaliação para Caos Arcana |
| --- | --- | --- |
| RpgDifficulty | Projeto Fabric com 1.20–1.20.1; escala vida/dano por distância e tempo. | Candidato à auditoria de configuração. Distância não comprova zoneamento por bioma/estrutura ou proteção do spawn. |
| Improved Mobs | Página inclui Fabric e 1.20.1; atributos e mudanças de comportamento configuráveis. | Maior superfície de alteração da IA; não habilitar antes de auditar exclusões e desligar comportamentos incompatíveis. |
| Jade | HUD contextual com API; algumas informações exigem instalação no servidor. | Candidato à interface; exibição de nível próprio depende de integração ainda não comprovada. |
| WTHIT | HUD contextual e API; dados mais avançados podem exigir servidor. | Alternativa ao Jade; mesmo requisito de comprovar integração do nível. |

A wiki do RpgDifficulty recomenda desligar progressão por tempo em servidores.
Essa orientação é coerente com a proteção de iniciantes, mas não comprova todos
os requisitos do projeto. Improved Mobs publica recursos que alteram a IA;
configuração seletiva e ensaio de compatibilidade são indispensáveis.

### Próxima etapa técnica do estudo

- Fixar versão e arquivo exatos para **Fabric 1.20.1**, dependências e hashes.
  A lista geral de plataformas/versões de um projeto não comprova que toda
  combinação é atendida; as listagens dinâmicas consultadas não expuseram os
  arquivos, portanto essa verificação permanece pendente.
- Auditar código/configuração dessa versão: apenas hostis, exclusão de animais,
  aldeões, invocações e chefes, tetos absolutos e preservação de entidades antigas.
- Comprovar faixas por bioma, estrutura e dimensão; proteção do spawn e grupos
  com níveis diferentes. Não presumir que escala por distância resolve isso.
- Escolher um HUD e verificar extensão para nível, nome e vida ao mirar;
  não criar dois overlays concorrentes.
- Ensaiar primeiro em cópia isolada, com mobs vanilla e do bestiário auxiliar.
  Comparar atributos, IA, persistência e custo de servidor antes/depois.
- Manter fórmulas de nível, limites de atributos, loot e XP explicitamente
  pendentes de design. Não derivar valores novos da configuração padrão de mods.

Recomendação preliminar: auditar primeiro RpgDifficulty e Jade; manter Improved
Mobs e WTHIT como alternativas. Não há seleção definitiva ou homologação.

### Fontes oficiais

- [RpgDifficulty](https://modrinth.com/mod/rpgdifficulty)
- [Wiki do RpgDifficulty](https://globox1997.github.io/wiki/mods/RpgDifficulty/)
- [Código do RpgDifficulty](https://github.com/Globox1997/RpgDifficulty)
- [Improved Mobs](https://modrinth.com/mod/improved-mobs)
- [Código do Improved Mobs](https://github.com/Flemmli97/ImprovedMobs)
- [Jade](https://modrinth.com/mod/jade)
- [WTHIT](https://modrinth.com/mod/wthit)
