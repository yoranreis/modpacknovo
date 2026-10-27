# Contratos de Integração e Interfaces (CONTRACTS.md)

Este documento define as interfaces e contratos de código que devem ser respeitados ao criar novas funcionalidades.

---

## 1. Contrato de Dados do Jogador (`IRPGPlayerData`)

Todo jogador (`Player`) no Fabric é mixado com a interface `IRPGPlayerData`. Desenvolvedores auxiliares devem consumir estes métodos para manipular status de RPG sem mexer nos Mixins centrais:

```java
package br.com.caosarcana.common.data;

public interface IRPGPlayerData {
    // Raça
    Race caosarcana$getRace();
    void caosarcana$setRace(Race race);
    boolean caosarcana$hasChosenRace();

    // Mana
    float caosarcana$getMana();
    void caosarcana$setMana(float mana);
    float caosarcana$getMaxMana();
    float caosarcana$getBaseMaxMana();
    void caosarcana$setBaseMaxMana(float baseMax);
    boolean caosarcana$consumeMana(float amount); // Retorna true se consumiu, false se insuficiente
    void caosarcana$restoreMana(float amount);    // Restaura mana até o limite máximo

    // Níveis e Escolas
    int caosarcana$getGeneralLevel();
    void caosarcana$setGeneralLevel(int level);
    int caosarcana$getSchoolLevel(String school);
    void caosarcana$setSchoolLevel(String school, int level);
}
```

---

## 2. Contrato de Feitiços (`ISpell`)

Para criar um novo feitiço no pacote `br.com.caosarcana.content.spells.*`:

1. Implementar a interface ou estender a classe base de feitiços.
2. Definir:
   * **ID único** (ResourceLocation: `caosarcana:<id>`).
   * **Escola de Magia** (Ex: "piromancia", "restauracao", "alteracao", "ilusao", "conjuracao").
   * **Custo de Mana** (float).
   * **Nível Mínimo da Escola** (int).
   * **Ação de Conjuração** (`execute(ServerPlayer caster, ItemStack castItem)`).
3. Nunca acessar variáveis privadas do `PlayerRPGMixin`. Sempre usar `((IRPGPlayerData) player).caosarcana$consumeMana(...)`.
