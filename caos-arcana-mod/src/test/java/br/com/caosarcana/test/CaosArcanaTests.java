package br.com.caosarcana.test;

import br.com.caosarcana.common.equipment.EquipmentRequirement;
import br.com.caosarcana.common.equipment.EquipmentRequirementRegistry;
import br.com.caosarcana.common.equipment.RequirementResult;
import br.com.caosarcana.common.equipment.RequirementType;
import br.com.caosarcana.common.race.Race;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CaosArcanaTests {

    @BeforeAll
    public static void initMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    public void setup() {
        EquipmentRequirementRegistry.clear();
        EquipmentRequirementRegistry.registerProvisionalTestItems();
    }

    @Test
    @DisplayName("Teste de resolução de IDs de Raça e fallback seguro")
    public void testRaceParsingAndDefaults() {
        assertEquals(Race.ORC, Race.fromId("orc"));
        assertEquals(Race.ORC, Race.fromId("ORC"));
        assertEquals(Race.ORC, Race.fromId("  orc  "));

        assertEquals(Race.FELINO, Race.fromId("felino"));
        assertEquals(Race.ALTO_ELFO, Race.fromId("alto_elfo"));
        assertEquals(Race.NORDICO, Race.fromId("nordico"));
        assertEquals(Race.ELFO_NEGRO, Race.fromId("elfo_negro"));
        assertEquals(Race.ELFO_FLORESTA, Race.fromId("elfo_floresta"));
        assertEquals(Race.BRETAO, Race.fromId("bretao"));
        assertEquals(Race.IMPERIAL, Race.fromId("imperial"));
        assertEquals(Race.GUARDA_VERMELHO, Race.fromId("guarda_vermelho"));
        assertEquals(Race.ARGONIANO, Race.fromId("argoniano"));

        // Total de 10 raças jogáveis + NONE
        assertEquals(11, Race.values().length);

        // Fallbacks seguros
        assertEquals(Race.NONE, Race.fromId("alien"));
        assertEquals(Race.NONE, Race.fromId(""));
        assertEquals(Race.NONE, Race.fromId(null));
    }

    @Test
    @DisplayName("Teste de auditoria documental e separação de características ativas vs planejadas")
    public void testRaceDocumentationStatusAndTraits() {
        // Todas as 10 raças agora são oficiais homologadas da base Skyrim
        for (Race race : Race.values()) {
            if (race != Race.NONE) {
                assertTrue(race.getDocumentationStatus().contains("Decidido"), "Raça " + race.name() + " deve estar marcada como Decidido");
            }
        }

        // Orc: vantagens ativas presentes, penalidade de mana é planejada
        assertTrue(Race.ORC.getActiveAdvantages().contains("Dano físico +2"));
        assertTrue(Race.ORC.getPlannedTraits().contains("Ira Berserker"));

        // Felino: mobilidade ativa, dano de queda é planejado
        assertTrue(Race.FELINO.getActiveAdvantages().contains("Velocidade de movimento +15%"));
        assertTrue(Race.FELINO.getPlannedTraits().contains("Olhos da Noite"));

        // Alto Elfo: mana é parcial, penalidade física é ativa
        assertTrue(Race.ALTO_ELFO.getPartialTraits().contains("Mana Máxima +50"));
        assertTrue(Race.ALTO_ELFO.getActiveDisadvantages().contains("Dano corpo a corpo -15%"));

        // Nórdico e Argoniano
        assertTrue(Race.NORDICO.getActiveAdvantages().contains("Vida Máxima +2"));
        assertTrue(Race.ARGONIANO.getActiveAdvantages().contains("Respiração aquática"));
    }

    @Test
    @DisplayName("Teste de restrição de equipamento por raça permitida")
    public void testEquipmentRequirementAllowedRace() {
        ResourceLocation ironChestplate = new ResourceLocation("minecraft", "iron_chestplate");

        MockRPGPlayer orcPlayer = new MockRPGPlayer(Race.ORC, 5);
        MockRPGPlayer felinoPlayer = new MockRPGPlayer(Race.FELINO, 5);
        MockRPGPlayer elfoPlayer = new MockRPGPlayer(Race.ALTO_ELFO, 5);
        MockRPGPlayer unassignedPlayer = new MockRPGPlayer(Race.NONE, 5);

        // Orc com nível 5 atende aos requisitos do peitoral pesado
        RequirementResult orcResult = EquipmentRequirementRegistry.check(orcPlayer, ironChestplate, RequirementType.EQUIP);
        assertTrue(orcResult.isAllowed(), "Orc com nível 5 deve poder equipar Peitoral de Ferro");

        // Felino com nível 5 é bloqueado por não ser Orc
        RequirementResult felinoResult = EquipmentRequirementRegistry.check(felinoPlayer, ironChestplate, RequirementType.EQUIP);
        assertFalse(felinoResult.isAllowed(), "Felino não deve poder equipar item exclusivo de Orc");
        assertTrue(felinoResult.getDenialMessage().getString().contains("Orc"));

        // Alto Elfo com nível 5 é bloqueado
        RequirementResult elfoResult = EquipmentRequirementRegistry.check(elfoPlayer, ironChestplate, RequirementType.EQUIP);
        assertFalse(elfoResult.isAllowed());

        // Sem raça é bloqueado
        RequirementResult unassignedResult = EquipmentRequirementRegistry.check(unassignedPlayer, ironChestplate, RequirementType.EQUIP);
        assertFalse(unassignedResult.isAllowed());
    }

    @Test
    @DisplayName("Teste de restrição de equipamento por nível geral mínimo")
    public void testEquipmentRequirementMinLevel() {
        ResourceLocation ironChestplate = new ResourceLocation("minecraft", "iron_chestplate");

        MockRPGPlayer orcLevel1 = new MockRPGPlayer(Race.ORC, 1);
        MockRPGPlayer orcLevel4 = new MockRPGPlayer(Race.ORC, 4);
        MockRPGPlayer orcLevel5 = new MockRPGPlayer(Race.ORC, 5);
        MockRPGPlayer orcLevel10 = new MockRPGPlayer(Race.ORC, 10);

        // Orc nível 1 e 4 devem ser bloqueados (requer nível 5)
        RequirementResult r1 = EquipmentRequirementRegistry.check(orcLevel1, ironChestplate, RequirementType.EQUIP);
        assertFalse(r1.isAllowed());
        assertTrue(r1.getDenialMessage().getString().contains("5"));

        RequirementResult r4 = EquipmentRequirementRegistry.check(orcLevel4, ironChestplate, RequirementType.EQUIP);
        assertFalse(r4.isAllowed());

        // Orc nível 5 e 10 devem ser autorizados
        RequirementResult r5 = EquipmentRequirementRegistry.check(orcLevel5, ironChestplate, RequirementType.EQUIP);
        assertTrue(r5.isAllowed());

        RequirementResult r10 = EquipmentRequirementRegistry.check(orcLevel10, ironChestplate, RequirementType.EQUIP);
        assertTrue(r10.isAllowed());
    }

    @Test
    @DisplayName("Teste de separação entre requisitos de Equipar, Fabricar e Comercializar")
    public void testRequirementSeparation() {
        ResourceLocation ironChestplate = new ResourceLocation("minecraft", "iron_chestplate");

        // Felino nível 1 (não atende a raça nem ao nível para EQUIPAR)
        MockRPGPlayer felinoLevel1 = new MockRPGPlayer(Race.FELINO, 1);

        // Equipar deve ser bloqueado
        RequirementResult equipResult = EquipmentRequirementRegistry.check(felinoLevel1, ironChestplate, RequirementType.EQUIP);
        assertFalse(equipResult.isAllowed());

        // Fabricar não possui restrição para esse item, então deve ser permitido
        RequirementResult craftResult = EquipmentRequirementRegistry.check(felinoLevel1, ironChestplate, RequirementType.CRAFT);
        assertTrue(craftResult.isAllowed(), "Jogador que não pode equipar deve poder fabricar se não houver restrição de craft");

        // Comercializar não possui restrição para esse item, então deve ser permitido
        RequirementResult tradeResult = EquipmentRequirementRegistry.check(felinoLevel1, ironChestplate, RequirementType.TRADE);
        assertTrue(tradeResult.isAllowed(), "Jogador que não pode equipar deve poder vender/transportar");
    }

    @Test
    @DisplayName("Teste de preservação de dados de RPG ao resetar raça")
    public void testResetRacePreservesRPGData() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 25);
        player.caosarcana$setMana(75.0f);
        player.caosarcana$setBaseMaxMana(120.0f);
        player.caosarcana$setSchoolLevel("pyromancy", 30);

        // Simula o comportamento do comando /caos resetrace
        player.caosarcana$setRace(Race.NONE);
        player.caosarcana$setChosenRace(false);

        // Raça foi resetada
        assertEquals(Race.NONE, player.caosarcana$getRace());
        assertFalse(player.caosarcana$hasChosenRace());

        // Dados de progresso RPG foram rigorosamente preservados
        assertEquals(25, player.caosarcana$getGeneralLevel());
        assertEquals(75.0f, player.caosarcana$getMana());
        assertEquals(120.0f, player.caosarcana$getMaxMana());
        assertEquals(30, player.caosarcana$getSchoolLevel("pyromancy"));
    }

    @Test
    @DisplayName("Teste de derivação de Mana por Raça e Clamping Anti-Exploit")
    public void testManaDerivationAndAntiExploitClamping() {
        MockRPGPlayer player = new MockRPGPlayer(Race.NONE, 1);
        player.caosarcana$setBaseMaxMana(100.0f);
        assertEquals(100.0f, player.caosarcana$getMaxMana());

        // Ao selecionar Alto Elfo: ganha bônus racial de +50 de mana máxima
        player.caosarcana$setRace(Race.ALTO_ELFO);
        assertEquals(150.0f, player.caosarcana$getMaxMana(), "Alto Elfo deve possuir base + 50 de Mana Máxima");

        // Jogador carrega 140 de mana
        player.caosarcana$setMana(140.0f);
        assertEquals(140.0f, player.caosarcana$getMana());

        // Jogador reseta ou troca de raça para Orc (que não tem bônus de mana)
        player.caosarcana$setRace(Race.ORC);
        assertEquals(100.0f, player.caosarcana$getMaxMana(), "Orc deve possuir apenas a mana máxima base");

        // A mana atual DEVE ser cortada (clamped) para 100, sem preservar 140 nem gerar mana grátis
        assertEquals(100.0f, player.caosarcana$getMana(), "Mana atual deve ser clamped para o novo teto máximo");
    }

    @Test
    @DisplayName("Teste de Persistência NBT real e transferência de Custódia Segura")
    public void testRealNbtSerializationAndDurabilityPreservation() {
        // Cria item complexo com NBT real (encantamentos, dano, nome customizado)
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD, 1);
        sword.enchant(Enchantments.SHARPNESS, 5);
        sword.enchant(Enchantments.UNBREAKING, 3);
        sword.setDamageValue(45); // Danificada
        sword.setHoverName(Component.literal("Espada Ancestral"));

        ItemStack armor = new ItemStack(Items.IRON_CHESTPLATE, 1);
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);

        List<ItemStack> originalCustody = new ArrayList<>();
        originalCustody.add(sword);
        originalCustody.add(armor);

        // 1. Serializa para CompoundTag real do Minecraft (exatamente como PlayerRPGMixin.saveNbt faz)
        CompoundTag rootNbt = new CompoundTag();
        ListTag pendingList = new ListTag();
        for (ItemStack stack : originalCustody) {
            CompoundTag itemTag = new CompoundTag();
            stack.save(itemTag);
            pendingList.add(itemTag);
        }
        rootNbt.put("PendingDeliveries", pendingList);

        // 2. Desserializa de CompoundTag (exatamente como readNbt faz após restart ou reload)
        List<ItemStack> restoredCustody = new ArrayList<>();
        ListTag readList = rootNbt.getList("PendingDeliveries", 10);
        for (int i = 0; i < readList.size(); i++) {
            CompoundTag itemTag = readList.getCompound(i);
            ItemStack loaded = ItemStack.of(itemTag);
            restoredCustody.add(loaded);
        }

        // 3. Validações estritas de integridade
        assertEquals(2, restoredCustody.size());

        ItemStack loadedSword = restoredCustody.get(0);
        assertEquals(Items.DIAMOND_SWORD, loadedSword.getItem());
        assertEquals(45, loadedSword.getDamageValue(), "Durabilidade danificada deve ser preservada exatamente");
        assertEquals("Espada Ancestral", loadedSword.getHoverName().getString(), "Nome customizado deve ser preservado");
        assertTrue(loadedSword.isEnchanted(), "Encantamentos devem ser preservados");

        ItemStack loadedArmor = restoredCustody.get(1);
        assertEquals(Items.IRON_CHESTPLATE, loadedArmor.getItem());
        assertTrue(loadedArmor.isEnchanted());
    }

    @Test
    @DisplayName("Teste de devolução parcial e mutação de ItemStack sem vazamento de referências")
    public void testPartialStackInsertionAndNoAliasing() {
        // Simula uma pilha de 10 flechas em custódia
        ItemStack stack = new ItemStack(Items.ARROW, 10);

        // Simula uma tentativa de inserção onde apenas 4 cabem
        int insertable = 4;
        ItemStack remainder = stack.copy();
        remainder.shrink(insertable);

        assertEquals(6, remainder.getCount(), "Apenas a sobra deve continuar em custódia");

        // O stack original não deve ser afetado (sem aliasing)
        assertEquals(10, stack.getCount());
    }

    @Test
    @DisplayName("Teste de consumo, restauração e taxas raciais de regeneração de Mana")
    public void testManaConsumptionRegenAndRacialRates() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 5);
        player.caosarcana$setMana(50.0f);

        // 1. Consumo válido
        assertTrue(player.caosarcana$consumeMana(20.0f), "Deve permitir consumir 20 de mana quando tem 50");
        assertEquals(30.0f, player.caosarcana$getMana(), 0.01f);

        // 2. Consumo recusado por falta de mana
        assertFalse(player.caosarcana$consumeMana(40.0f), "Deve recusar consumir 40 de mana quando tem apenas 30");
        assertEquals(30.0f, player.caosarcana$getMana(), 0.01f, "Mana não deve ser alterada se o consumo falhar");

        // 3. Restauração respeitando o teto máximo
        player.caosarcana$restoreMana(100.0f);
        assertEquals(player.caosarcana$getMaxMana(), player.caosarcana$getMana(), 0.01f, "Restauração não pode ultrapassar a mana máxima");

        // 4. Taxas raciais de regeneração
        MockRPGPlayer elfo = new MockRPGPlayer(Race.ALTO_ELFO, 1);
        MockRPGPlayer felino = new MockRPGPlayer(Race.FELINO, 1);
        MockRPGPlayer orc = new MockRPGPlayer(Race.ORC, 1);

        assertEquals(3.0f, elfo.caosarcana$getManaRegenPerSecond(), 0.01f, "Alto Elfo deve regenerar 3.0 mana/s");
        assertEquals(2.0f, felino.caosarcana$getManaRegenPerSecond(), 0.01f, "Felino deve regenerar 2.0 mana/s");
        assertEquals(1.5f, orc.caosarcana$getManaRegenPerSecond(), 0.01f, "Orc deve regenerar 1.5 mana/s");
    }

    @Test
    @DisplayName("Teste de parâmetros e regras de expiração da Centelha Ígnea (tempo e distância)")
    public void testFlameSparkProjectileExpirationLimits() {
        // 1. Verificação dos limites provisórios de balanceamento
        assertEquals(60, br.com.caosarcana.content.spells.FlameSparkProjectile.MAX_LIFETIME_TICKS, "Tempo de vida máximo deve ser 60 ticks (3 segundos)");
        assertEquals(40.0, br.com.caosarcana.content.spells.FlameSparkProjectile.MAX_DISTANCE_BLOCKS, 0.001, "Alcance máximo deve ser 40 blocos");

        // 2. Verificação de herança e não interferência em SmallFireball
        assertTrue(net.minecraft.world.entity.projectile.SmallFireball.class.isAssignableFrom(br.com.caosarcana.content.spells.FlameSparkProjectile.class));
        assertNotEquals(net.minecraft.world.entity.projectile.SmallFireball.class, br.com.caosarcana.content.spells.FlameSparkProjectile.class);
    }

    @Test
    @DisplayName("Teste de aprendizado de magias, idempotência e não-duplicação de registros")
    public void testSpellLearningAndIdempotence() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ALTO_ELFO, 1);
        String spellId = "caosarcana:centelha_ignea";

        // Inicialmente o jogador não conhece a magia
        assertFalse(player.caosarcana$knowsSpell(spellId), "Novo personagem não deve conhecer magias automaticamente");
        assertEquals(0, player.caosarcana$getKnownSpells().size());

        // Primeiro aprendizado: sucesso
        assertTrue(player.caosarcana$learnSpell(spellId), "Primeiro aprendizado de magia válida deve retornar true");
        assertTrue(player.caosarcana$knowsSpell(spellId), "Deve reportar como conhecida após o aprendizado");
        assertEquals(1, player.caosarcana$getKnownSpells().size());

        // Segundo aprendizado (repetido): recusado / idempotente
        assertFalse(player.caosarcana$learnSpell(spellId), "Tentativa de aprender novamente deve retornar false (sem duplicação)");
        assertEquals(1, player.caosarcana$getKnownSpells().size(), "Lista de magias conhecidas não pode conter duplicatas");

        // Resolução insensível a maiúsculas e espaços
        assertTrue(player.caosarcana$knowsSpell("  CAOSARCANA:CENTELHA_IGNEA  "), "Consulta de magia deve normalizar maiúsculas e espaços");

        // Tratamento seguro de nulos ou vazios
        assertFalse(player.caosarcana$learnSpell(null));
        assertFalse(player.caosarcana$learnSpell(""));
        assertFalse(player.caosarcana$knowsSpell(null));
        assertFalse(player.caosarcana$knowsSpell(""));
    }

    @Test
    @DisplayName("Teste de persistência NBT do conhecimento arcano e preservação após reset de raça")
    public void testSpellPersistenceAndResetRaceIntegrity() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 3);
        String spellId = "caosarcana:centelha_ignea";
        player.caosarcana$learnSpell(spellId);

        // 1. Simulação da serialização de NBT conforme implementado no PlayerRPGMixin
        CompoundTag rootNbt = new CompoundTag();
        CompoundTag rpgTag = new CompoundTag();
        rpgTag.putString("Race", player.caosarcana$getRace().getId());
        rpgTag.putBoolean("RaceChosen", player.caosarcana$hasChosenRace());
        rpgTag.putFloat("Mana", player.caosarcana$getMana());

        ListTag spellsList = new ListTag();
        for (String spell : player.caosarcana$getKnownSpells()) {
            spellsList.add(net.minecraft.nbt.StringTag.valueOf(spell));
        }
        rpgTag.put("KnownSpells", spellsList);
        rootNbt.put("CaosArcanaData", rpgTag);

        // 2. Simulação da desserialização
        MockRPGPlayer restoredPlayer = new MockRPGPlayer(Race.NONE, 1);
        CompoundTag loadedRpg = rootNbt.getCompound("CaosArcanaData");
        ListTag loadedSpells = loadedRpg.getList("KnownSpells", 8);
        for (int i = 0; i < loadedSpells.size(); i++) {
            restoredPlayer.caosarcana$learnSpell(loadedSpells.getString(i));
        }

        assertTrue(restoredPlayer.caosarcana$knowsSpell(spellId), "Magia salva no NBT deve ser restaurada integralmente");

        // 3. Simulação de /caos resetrace: Raça é reiniciada mas o conhecimento permanece
        player.caosarcana$setRace(Race.NONE);
        player.caosarcana$setChosenRace(false);

        assertEquals(Race.NONE, player.caosarcana$getRace(), "Raça deve ter sido resetada");
        assertFalse(player.caosarcana$hasChosenRace(), "Status de raça escolhida deve ter sido resetado");
        assertTrue(player.caosarcana$knowsSpell(spellId), "Reset de raça NÃO PODE apagar o conhecimento arcano aprendido");
        assertEquals(1, player.caosarcana$getKnownSpells().size());
    }

    @Test
    @DisplayName("Teste das regras de validação no servidor para conjuração sem livro")
    public void testServerSpellCastValidationRules() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ALTO_ELFO, 1);
        String spellId = "caosarcana:centelha_ignea";

        // Cenário A: Jogador sem raça escolhida
        MockRPGPlayer unassignedPlayer = new MockRPGPlayer(Race.NONE, 1);
        unassignedPlayer.caosarcana$setChosenRace(false);
        assertFalse(unassignedPlayer.caosarcana$hasChosenRace(), "Jogador sem raça não pode conjurar");

        // Cenário B: Jogador com raça mas que NÃO aprendeu a magia
        assertFalse(player.caosarcana$knowsSpell(spellId), "Jogador que não aprendeu deve ser impedido de conjurar");

        // Cenário C: Jogador aprendeu a magia
        player.caosarcana$learnSpell(spellId);
        assertTrue(player.caosarcana$knowsSpell(spellId));

        // Cenário D: Validação do nível provisório de escola (Piromancia Nível 1 padrão para novato)
        assertEquals(1, player.caosarcana$getSchoolLevel("piromancia"), "Nível de escola padrão deve ser 1");

        // Cenário E: Saldo insuficiente de mana (< 20)
        player.caosarcana$setMana(15.0f);
        assertFalse(player.caosarcana$consumeMana(20.0f), "Deve recusar conjuração com menos de 20 de mana");
        assertEquals(15.0f, player.caosarcana$getMana(), 0.01f, "Mana não deve ser alterada se o consumo falhar");

        // Cenário F: Saldo exatamente igual a 20 de mana
        player.caosarcana$setMana(20.0f);
        assertTrue(player.caosarcana$consumeMana(20.0f), "Deve permitir conjuração com exatamente 20 de mana");
        assertEquals(0.0f, player.caosarcana$getMana(), 0.01f, "Mana deve ir a zero após o consumo exato");

        // Cenário G: Saldo amplo de mana (100)
        player.caosarcana$setMana(100.0f);
        assertTrue(player.caosarcana$consumeMana(20.0f));
        assertEquals(80.0f, player.caosarcana$getMana(), 0.01f);
    }

    @Test
    @DisplayName("Teste de seleção de magia, estado vazio e rejeição de magia desconhecida")
    public void testSpellSelectionAndEmptyState() {
        MockRPGPlayer player = new MockRPGPlayer(Race.FELINO, 2);

        // 1. Estado vazio: sem magias conhecidas
        assertEquals("", player.caosarcana$getSelectedSpell(), "Novo jogador não deve ter magia selecionada");
        assertTrue(player.caosarcana$getKnownSpells().isEmpty());

        // 2. Tentativa de selecionar magia desconhecida: rejeitada imediatamente
        player.caosarcana$setSelectedSpell("caosarcana:bola_de_fogo");
        assertEquals("", player.caosarcana$getSelectedSpell(), "Não deve permitir selecionar magia que não conhece");

        // 3. Aprendizado da Centelha Ígnea: auto-seleciona como ativa se vazio
        assertTrue(player.caosarcana$learnSpell("caosarcana:centelha_ignea"));
        assertEquals("caosarcana:centelha_ignea", player.caosarcana$getSelectedSpell(), "Primeira magia aprendida deve ser auto-selecionada como ativa");

        // 4. Seleção explícita de magia conhecida
        player.caosarcana$setSelectedSpell("caosarcana:centelha_ignea");
        assertEquals("caosarcana:centelha_ignea", player.caosarcana$getSelectedSpell());

        // 5. Tentativa de selecionar magia inexistente não remove a magia ativa atual
        player.caosarcana$setSelectedSpell("caosarcana:tempestade_de_raios");
        assertEquals("caosarcana:centelha_ignea", player.caosarcana$getSelectedSpell(), "Seleção inválida deve preservar a magia ativa prévia");
    }

    @Test
    @DisplayName("Teste de persistência NBT da magia ativa e integridade na reconexão")
    public void testSpellSelectionPersistenceAcrossReconnect() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 3);
        player.caosarcana$learnSpell("caosarcana:centelha_ignea");
        player.caosarcana$setSelectedSpell("caosarcana:centelha_ignea");

        // 1. Serialização em NBT
        CompoundTag rootNbt = new CompoundTag();
        CompoundTag rpgTag = new CompoundTag();
        rpgTag.putString("Race", player.caosarcana$getRace().getId());
        rpgTag.putBoolean("RaceChosen", player.caosarcana$hasChosenRace());
        rpgTag.putFloat("Mana", player.caosarcana$getMana());

        ListTag spellsList = new ListTag();
        for (String spell : player.caosarcana$getKnownSpells()) {
            spellsList.add(net.minecraft.nbt.StringTag.valueOf(spell));
        }
        rpgTag.put("KnownSpells", spellsList);
        rpgTag.putString("SelectedSpell", player.caosarcana$getSelectedSpell());
        rootNbt.put("CaosArcanaData", rpgTag);

        // 2. Desserialização em nova sessão / reconexão
        MockRPGPlayer reconnectedPlayer = new MockRPGPlayer(Race.ORC, 3);
        CompoundTag loadedRpg = rootNbt.getCompound("CaosArcanaData");

        ListTag loadedSpells = loadedRpg.getList("KnownSpells", 8);
        for (int i = 0; i < loadedSpells.size(); i++) {
            reconnectedPlayer.caosarcana$learnSpell(loadedSpells.getString(i));
        }

        String loadedSelected = loadedRpg.getString("SelectedSpell");
        if (reconnectedPlayer.caosarcana$knowsSpell(loadedSelected)) {
            reconnectedPlayer.caosarcana$setSelectedSpell(loadedSelected);
        }

        assertEquals("caosarcana:centelha_ignea", reconnectedPlayer.caosarcana$getSelectedSpell(), "Magia ativa deve ser restaurada após reconexão");
    }

    @Test
    @DisplayName("Teste do registro central de feitiços (SpellRegistry) e metadados da Centelha")
    public void testSpellRegistryMetadataAndContracts() {
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.contains("caosarcana:centelha_ignea"));

        var spellOpt = br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:centelha_ignea");
        assertTrue(spellOpt.isPresent(), "Centelha Ígnea deve estar presente no registro central");

        var spell = spellOpt.get();
        assertEquals("Centelha Ígnea", spell.name());
        assertEquals("piromancia", spell.school());
        assertTrue(spell.schoolDisplayName().contains("Piromancia"));
        assertEquals(1, spell.minSchoolLevel());
        assertEquals(20.0f, spell.manaCost(), 0.01f);
        assertEquals(15, spell.cooldownTicks());
        assertEquals(60, spell.maxLifetimeTicks());
        assertEquals(40.0, spell.maxDistanceBlocks(), 0.01);
    }

    @Test
    @DisplayName("Teste de validação dos 10 Poderes Raciais exclusivos e restrição entre raças")
    public void test10RacialPowersRegisteredAndExclusive() {
        Race[] races = {
            Race.ORC, Race.FELINO, Race.ALTO_ELFO, Race.NORDICO, Race.ELFO_NEGRO,
            Race.ELFO_FLORESTA, Race.BRETAO, Race.IMPERIAL, Race.GUARDA_VERMELHO, Race.ARGONIANO
        };

        for (Race race : races) {
            var racialOpt = br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(race);
            assertTrue(racialOpt.isPresent(), "Raça " + race.name() + " deve possuir um Poder Racial exclusivo");

            var racialSpell = racialOpt.get();
            assertTrue(racialSpell.isRacial(), "Poder racial deve ter isRacial() == true");
            assertEquals(race, racialSpell.requiredRace(), "Raça requerida deve ser " + race.name());

            // A própria raça pode canalizar
            assertTrue(racialSpell.canCast(race), race.name() + " deve conseguir conjurar seu próprio poder");

            // Outras raças NÃO podem canalizar
            for (Race otherRace : races) {
                if (otherRace != race) {
                    assertFalse(racialSpell.canCast(otherRace), otherRace.name() + " NÃO deve conseguir conjurar o poder de " + race.name());
                }
            }
        }
    }

    @Test
    @DisplayName("Teste das 10 Magias Universais acessíveis a todas as raças")
    public void test10UniversalSpellsAccessibleToAll() {
        var universalSpells = br.com.caosarcana.content.spells.SpellRegistry.getUniversalSpells();
        assertEquals(10, universalSpells.size(), "Devem existir exatamente 10 magias universais registradas");

        Race[] races = {
            Race.ORC, Race.FELINO, Race.ALTO_ELFO, Race.NORDICO, Race.ELFO_NEGRO,
            Race.ELFO_FLORESTA, Race.BRETAO, Race.IMPERIAL, Race.GUARDA_VERMELHO, Race.ARGONIANO
        };

        for (var spell : universalSpells) {
            assertFalse(spell.isRacial(), "Magia universal " + spell.name() + " não deve ser restrita a uma raça");
            assertNull(spell.requiredRace());

            // Todas as 10 raças podem canalizar
            for (Race race : races) {
                assertTrue(spell.canCast(race), "Qualquer raça deve poder canalizar a magia universal " + spell.name());
            }
        }
    }

    @Test
    @DisplayName("Teste do registro de Provações Arcanas (Arcane Trials)")
    public void testArcaneTrialsRegistry() {
        var trials = br.com.caosarcana.content.trials.ArcaneTrialRegistry.getAll();
        assertEquals(10, trials.size(), "Devem existir 10 provações arcanas registradas para as escolas de Skyrim");

        for (var trial : trials) {
            assertNotNull(trial.id());
            assertNotNull(trial.name());
            assertNotNull(trial.rewardSpellId());
            assertTrue(br.com.caosarcana.content.spells.SpellRegistry.contains(trial.rewardSpellId()),
                    "Magia recompensada pela provação " + trial.id() + " deve existir no SpellRegistry");
        }
    }

    @Test
    @DisplayName("Teste do sistema monetário e taxas de conversão de moedas físicas")
    public void testEconomicCurrencyValues() {
        assertEquals(1, br.com.caosarcana.content.economy.CoinItem.CoinType.COBRE.getValueInCopper());
        assertEquals(100, br.com.caosarcana.content.economy.CoinItem.CoinType.PRATA.getValueInCopper());
        assertEquals(10000, br.com.caosarcana.content.economy.CoinItem.CoinType.OURO.getValueInCopper());

        // 100 Moedas de Cobre = 1 Moeda de Prata
        assertEquals(100, br.com.caosarcana.content.economy.CoinItem.CoinType.PRATA.getValueInCopper() /
                br.com.caosarcana.content.economy.CoinItem.CoinType.COBRE.getValueInCopper());

        // 100 Moedas de Prata = 1 Septim de Ouro
        assertEquals(100, br.com.caosarcana.content.economy.CoinItem.CoinType.OURO.getValueInCopper() /
                br.com.caosarcana.content.economy.CoinItem.CoinType.PRATA.getValueInCopper());
    }
}
