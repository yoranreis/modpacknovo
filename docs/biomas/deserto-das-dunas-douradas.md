# Deserto das Dunas Douradas

## 1. Visão Geral da Região
* **Nome do Bioma:** Deserto das Dunas Douradas
* **Atmosfera:** Vasta extensão de areias escaldantes, ruínas antigas soterradas, monumentos de arenito com hieróglifos e variações térmicas drásticas entre o dia e a noite.
* **Objetivo de Gameplay:** Área de exploração de nível intermediário/alto focada em combate tático, resistência ambiental (calor/fome) e exploração de estruturas complexas (pirâmides e santuários).

---

## 2. Inimigos Normais (Mobs)

| Inimigo | Tipo / Papel | Mecânicas e Habilidades | Estado |
| --- | --- | --- | --- |
| **Víbora das Arenas** (`caosarcana:vibora_das_arenas`) | Hostil / Emboscada | **Emboscada Subterrânea:** Oculta sob blocos de areia até o jogador chegar a menos de 3 blocos.<br>**Mordida Paralisante:** Aplica *Lentidão IV* + *Veneno* por 4 segundos (80 ticks). | Implementado; aguardando teste em jogo |
| **Escaravelho Solar** (`caosarcana:escaravelho_solar`) | Hostil / Enxame | **Reflexão Térmica:** Reflete 15% de dano à distância/mágico sob luz solar direta.<br>**Investida em Bando:** Alerta e avança coletivamente com outros escaravelhos próximos com mordidas rápidas. | Implementado; aguardando teste em jogo |
| **Golem de Areia** (`caosarcana:golem_de_areia`) | Hostil / Tanque | **Desmoronamento Tático:** Desfaz-se temporariamente em poeira ao receber dano de alto impacto, esquivando-se e reposicionando-se.<br>**Soco Sísmico:** Ataque em área (AoE) no chão com repulsão severa. | Implementado; aguardando teste em jogo |
| **Múmia Esquelética** (`caosarcana:mumia_esqueletica`) | Hostil / Combatente Ágil | **Fúria do Deserto:** Aumenta velocidade de movimento e ataque ao avistar o jogador diretamente.<br>**Maldição da Desidratação:** Acelera o consumo da barra de fome do jogador durante o combate. | Implementado; aguardando teste em jogo |

---

## 3. Chefão da Região: Faraó do Sol Eterno (`caosarcana:farao_do_sol_eterno`)
* **Aparência:** Entidade real colossal com bandagens douradas, touca azul/dourada, olhos incandescentes e cajado sagrado solar.
* **Fases e Habilidades:**
  * **Raio Solar Incandescente:** Canaliza o cajado para disparar um feixe de luz guiado que persegue o alvo.
  * **Redemoinho de Areia:** Tempestade de vento localizada que puxa o jogador para o centro da arena.
  * **Invocação de Guardiões (Fase 2):** Convoca ondas de Múmias Esqueléticas para regenerar o chefe e desviar o foco.

---

## Testes atuais

```mcfunction
/summon caosarcana:escaravelho_solar ~ ~ ~
/summon caosarcana:vibora_das_arenas ~ ~ ~
/summon caosarcana:golem_de_areia ~ ~ ~
/summon caosarcana:mumia_esqueletica ~ ~ ~
/summon caosarcana:farao_do_sol_eterno ~ ~ ~
```
