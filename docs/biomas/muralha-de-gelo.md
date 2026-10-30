# Bioma 5 — Muralha de Gelo

## Visão Geral da Região
Uma cadeia montanhosa colossal e perpétua de gelo e neve, marcada por ventos uivantes, nevascas constantes que reduzem drasticamente a visibilidade e precipícios mortais. Trata-se de uma região de desafio avançado onde o ambiente e as criaturas impõem efeitos severos de Congelamento e controle de grupo.

---

## Inimigos Comuns

### 1. Caçador de Gelo (`caosarcana:cacador_de_gelo`)
- **Aparência:** Humanóide ágil vestindo capuz espesso de pele branca camuflada na neve com estalactites de gelo, olhos cianos brilhantes sob a sombra do capuz, núcleo gélido no peitoral, aljava nas costas e uma Lança de Gelo Cristalino Afiado na mão direita.
- **Atributos Base:** `34.0 HP` | `6.5 Dano` | `4.0 Armadura`.
- **Habilidades:**
  - **Camuflagem Alpina:** Torna-se semitransparente e silencioso quando próximo a blocos de neve/gelo ou aguardando emboscada, revelando-se no momento do bote.
  - **Investida Gélida:** Avança rapidamente contra o alvo com sua lança de gelo, causando dano e aplicando *Lentidão III* por 3 segundos (`60 ticks`) além de acúmulo de congelamento.

### 2. Golem de Gelo (`caosarcana:golem_de_gelo`)
- **Aparência:** Colosso maciço construído por blocos irregulares de gelo compacto e neve endurecida, com núcleo de cristal ciano radiante no peito e cristais pontiagudos de gelo azul brotando dos ombros e antebraços.
- **Atributos Base:** `56.0 HP` | `8.5 Dano` | `10.0 Armadura` | `0.90 Resistência a Repulsão`.
- **Habilidades:**
  - **Aura de Arrepiar:** Causa dano de congelamento passivo e contínuo a inimigos que permanecerem a menos de 3 blocos de distância.
  - **Esmagamento Sísmico:** Golpeia o chão estilhaçando gelo, lançando detritos gélidos e causando dano em área com forte repulsão (*knockback*).

### 3. Aranha do Gelo (`caosarcana:aranha_do_gelo`)
- **Aparência:** Aracnídeo encouraçado em gelo cristalino e rocha congelada, com múltiplos olhos em azul neon, quelíceras de cristal de gelo, aglomerado de cristais de gelo no dorso e 8 patas com pontas de gelo translúcido.
- **Atributos Base:** `28.0 HP` | `5.0 Dano` | `3.0 Armadura`.
- **Habilidades:**
  - **Teia Congelante:** Dispara uma rajada/projétil de teia glacial que prende o jogador no lugar e aplica *Congelamento* instantâneo.
  - **Escalada Glacial:** Escala paredes de gelo e blocos verticalmente com rapidez para flanquear o alvo.

### 4. Fantasma do Gelo (`caosarcana:fantasma_do_gelo`)
- **Aparência:** Espectro etéreo e translúcido formado por blocos de energia gélida condensada, capuz de gelo com olhos cianos, ombreiras congeladas, núcleo luminoso no tórax e cauda fantasmagórica esvoaçante.
- **Atributos Base:** `30.0 HP` | `6.0 Dano` | Flutuante (`FlyingMoveControl`).
- **Habilidades:**
  - **Intangibilidade Espectral:** Entra em estado etéreo durante o deslocamento, ficando imune a dano físico comum enquanto flutua em direção ao alvo.
  - **Toque do Congelamento:** Ataque corpo a corpo que ignora parcialmente a armadura e aplica *Enfraquecimento II* (`WEAKNESS`) e *Congelamento Severo*.

---

## Chefe Regional

### O Rei do Gelo (`caosarcana:rei_do_gelo`)
- **Aparência:** Gigante colossal de gelo e neve com mandíbula monstruosa repleta de presas, coroa de gelo puro cravejada de cristais cianos e magenta, espinhos de gelo nos ombros e braço esquerdo, núcleo cristalino no peito e um imenso Cetro de Obsidiana e Gelo na mão direita.
- **Atributos Base:** `380.0 HP` | `11.5 Dano` | `11.0 Armadura` | `0.92 Resistência a Repulsão` | Barra de Chefe em 3 Fases (`ServerBossEvent`).
- **Fases e Habilidades:**
  - **Fase 1 (Domínio do Solo):**
    - *Erupções de Gelo:* Faz estacas afiadas de gelo surgirem do chão em onda de choque linear.
    - *Pisão Colossal:* Ataque corpo a corpo sísmico em área que estilhaça gelo e arremessa jogadores próximos.
  - **Fase 2 (Artilharia Pesada e Clima — `<= 65% HP`):**
    - *Bombardeio de Neve:* Lança blocos/orbes de neve e gelo que explodem gerando áreas gélidas.
    - *Nevasca Violenta:* Tempestade de neve localizada que reduz visibilidade e velocidade dos jogadores enquanto regenera o Rei lentamente.
  - **Fase 3 (Fúria Final — `<= 30% HP`):**
    - *Estátua de Gelo:* Prende o jogador em um aprisionamento de gelo maciço que deve ser rompido antes do golpe final do Rei.
