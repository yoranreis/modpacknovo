# Bioma 4 — Recife de Coral Abissal & Arquipélago

## Visão Geral da Região
Uma região dividida entre pequenas ilhas tropicais na superfície e um vasto sistema de trincheiras oceânicas profundas iluminadas por corais bioluminescentes, fendas hidrotermais de obsidiana e ruínas submersas. A exploração exige preparo para combate subaquático, gerenciamento de oxigênio e resistência a efeitos de desorientação e correntes marítimas.

---

## Inimigos Comuns

### 1. Tragador do Abismo (`caosarcana:tragador_do_abismo`)
- **Aparência:** Predador abissal azul-petróleo escuro sem olhos, com uma enorme bocarra circular de lampreia repleta de dentes brancos afiados e fundo ciano bioluminescente, costelas abertas revelando um núcleo luminoso no tórax, cracas e corais nos ombros, garras longas e tentáculos inferiores no lugar das pernas.
- **Atributos Base:** `34.0 HP` | `6.5 Dano` | `4.0 Armadura` | `MobType.WATER` (respiração aquática ilimitada).
- **Habilidades:**
  - **Sucção Abissal:** Canaliza um vórtice de água diante da bocarra circular que puxa o jogador em sua direção para um golpe triturador corpo a corpo.
  - **Investida Oculta:** Desloca-se com extrema rapidez em áreas de baixa luminosidade (`luz <= 7`) ou dentro d'água, desferindo um bote cujo dano perfurante/abissal atravessa parte da armadura do alvo.

### 2. Guardião de Obsidiana (`caosarcana:guardiao_de_obsidiana`)
- **Aparência:** Construto blindado de rocha vulcânica/obsidiana negra-arroxeada com fendas de magma incandescente, visor em formato de `T` brilhando em lava laranja, cristais de obsidiana nos ombros e costas, núcleo de magma exposto no peito e algas marinhas escuras presas à carapaça.
- **Atributos Base:** `46.0 HP` | `7.5 Dano` | `10.0 Armadura` | `0.85 Resistência a Repulsão`.
- **Habilidades:**
  - **Cortina de Vapor:** Ao ser atingido, libera uma nuvem densa de vapor escaldante ao redor que aplica Cegueira curta (`BLINDNESS`) e queimadura leve a atacantes próximos.
  - **Resistência Vulcânica:** Imune a dano de fogo/lava e reduz em `40%` o dano físico recebido pela frente.

### 3. Medusa Bioluminescente (`caosarcana:medusa_bioluminescente`)
- **Aparência:** Água-viva flutuante translúcida em tons de ciano e magenta/lilás neon, com campânula gelatinosa abobadada, núcleo esférico pulsante no interior e longos tentáculos bioluminescentes articulados.
- **Atributos Base:** `24.0 HP` | `4.0 Dano` | Flutuante (`FlyingMoveControl` com teto de altura controlado).
- **Habilidades:**
  - **Pulso Eletromagnético:** Descarrega um choque bioelétrico em área que desacelera jogadores próximos (`MOVEMENT_SLOWDOWN`) e drena rapidamente seu oxigênio (`AirSupply`).
  - **Fio Paralisante:** Lança um pulso/filamento urticante à distância que paralisa o alvo no lugar por breves instantes (`MOVEMENT_SLOWDOWN` intenso).

### 4. Sereia das Profundezas (`caosarcana:sereia_das_profundezas`)
- **Aparência:** Criatura mística abissal com pele azul-profundo e escamas ciano-violeta, coroa de pedra abissal com detalhes dourados e algas, pérola luminosa no peito, nadadeiras dorsais translúcidas e longa cauda serpentina terminando em nadadeira caudal bifurcada.
- **Atributos Base:** `32.0 HP` | `5.5 Dano` | `3.0 Armadura` | `MobType.WATER`.
- **Habilidades:**
  - **Canto Desorientador:** Emite ondas sonoras hipnóticas que aplicam Náusea (`CONFUSION`), Fraqueza e um impulso reverso/lateral que desvia a movimentação do jogador.
  - **Projéteis Sônicos:** Dispara rajadas concentradas de energia sônica abissal à distância contra o alvo.

---

## Chefe Regional

### Lorde das Marés Abissais (`caosarcana:lorde_das_mares_abissais`)
- **Aparência:** Soberano colossal das fossas oceânicas usando coroa dourada cravejada de gemas cianas e corais, armadura pesada de ouro abissal e coral com cristal losangular ciano-magenta no peito, manto de tentáculos bioluminescentes nas costas, cauda serpentina encouraçada e um imenso Tridente Dourado Abissal com orbe de energia entre as pontas.
- **Atributos Base:** `360.0 HP` | `10.5 Dano` | `10.0 Armadura` | `0.90 Resistência a Repulsão` | Barra de Chefe Azul/Roxa (`ServerBossEvent`).
- **Habilidades:**
  - **Domínio das Correntes:** Cria redemoinhos e correntes marítimas na arena que puxam os jogadores para perto ou os arremessam violentamente para longe.
  - **Tridente Abissal:** Canaliza o tridente real para disparar feixes de energia abissal pressurizada e ondas de choque marinho em área.
  - **Invocações das Marés:** Convoca `Tragadores do Abismo` para defendê-lo e pressionar o jogador enquanto canaliza energia abissal.
