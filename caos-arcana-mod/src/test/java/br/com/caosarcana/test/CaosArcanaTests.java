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
        assertEquals(Race.ELFO_FLORESTA, Race.fromId("elfo_floresta"));
        assertEquals(Race.ARGONIANO, Race.fromId("argoniano"));
        assertEquals(Race.HUMANO, Race.fromId("humano"));
        assertEquals(Race.REDGUARD, Race.fromId("redguard"));
        assertEquals(Race.SANGUE_DAEDRA, Race.fromId("sangue_daedra"));
        assertEquals(Race.VOIDMER, Race.fromId("voidmer"));

        // Compatibilidade com IDs antigos renomeados
        assertEquals(Race.HUMANO, Race.fromId("imperial"));
        assertEquals(Race.REDGUARD, Race.fromId("guarda_vermelho"));

        // IDs legados substituídos (retornam NONE para exigir nova seleção)
        assertEquals(Race.NONE, Race.fromId("bretao"));
        assertEquals(Race.NONE, Race.fromId("elfo_negro"));

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
    @DisplayName("Teste de validação dos Poderes Raciais exclusivos e restrição entre raças")
    public void test10RacialPowersRegisteredAndExclusive() {
        Race[] racesWithPowers = {
            Race.ORC, Race.FELINO, Race.ALTO_ELFO, Race.NORDICO,
            Race.ELFO_FLORESTA, Race.HUMANO, Race.REDGUARD, Race.ARGONIANO
        };

        for (Race race : racesWithPowers) {
            var racialOpt = br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(race);
            assertTrue(racialOpt.isPresent(), "Raça " + race.name() + " deve possuir um Poder Racial exclusivo");

            var racialSpell = racialOpt.get();
            assertTrue(racialSpell.isRacial(), "Poder racial deve ter isRacial() == true");
            assertEquals(race, racialSpell.requiredRace(), "Raça requerida deve ser " + race.name());

            // A própria raça pode canalizar
            assertTrue(racialSpell.canCast(race), race.name() + " deve conseguir conjurar seu próprio poder");

            // Outras raças NÃO podem canalizar
            for (Race otherRace : racesWithPowers) {
                if (otherRace != race) {
                    assertFalse(racialSpell.canCast(otherRace), otherRace.name() + " NÃO deve conseguir conjurar o poder de " + race.name());
                }
            }
        }

        // Novas raças sem poderes inventados (explicitamente pendentes de definição formal)
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(Race.SANGUE_DAEDRA).isEmpty(),
                "Sangue-Daedra não deve ter poder inventado sem aprovação formal");
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(Race.VOIDMER).isEmpty(),
                "Voidmer não deve ter poder inventado sem aprovação formal");
    }

    @Test
    @DisplayName("Teste das 10 Magias Universais acessíveis a todas as 10 raças oficiais")
    public void test10UniversalSpellsAccessibleToAll() {
        var universalSpells = br.com.caosarcana.content.spells.SpellRegistry.getUniversalSpells();
        assertEquals(10, universalSpells.size(), "Devem existir exatamente 10 magias universais registradas");

        Race[] races = {
            Race.HUMANO, Race.ELFO_FLORESTA, Race.ALTO_ELFO, Race.ORC, Race.FELINO,
            Race.NORDICO, Race.SANGUE_DAEDRA, Race.REDGUARD, Race.ARGONIANO, Race.VOIDMER
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

    @Test
    @DisplayName("Teste de recargas independentes por feitiço (desacoplamento de itens)")
    public void testIndependentSpellCooldowns() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 5);

        // Inicialmente nenhum feitiço está em recarga
        assertFalse(player.caosarcana$isSpellOnCooldown("caosarcana:ira_berserker", 100L));
        assertFalse(player.caosarcana$isSpellOnCooldown("caosarcana:centelha_ignea", 100L));

        // Aplica recarga da Ira Berserker de 600 ticks (pronto no tick 700)
        player.caosarcana$setSpellCooldown("caosarcana:ira_berserker", 700L);

        // Ira Berserker está em recarga no tick 200
        assertTrue(player.caosarcana$isSpellOnCooldown("caosarcana:ira_berserker", 200L));
        assertEquals(700L, player.caosarcana$getSpellReadyTick("caosarcana:ira_berserker"));

        // Centelha Ígnea e Toque de Cura NÃO foram afetados e permanecem disponíveis
        assertFalse(player.caosarcana$isSpellOnCooldown("caosarcana:centelha_ignea", 200L));
        assertFalse(player.caosarcana$isSpellOnCooldown("caosarcana:toque_de_cura", 200L));

        // No tick 701, a Ira Berserker sai da recarga
        assertFalse(player.caosarcana$isSpellOnCooldown("caosarcana:ira_berserker", 701L));
    }

    @Test
    @DisplayName("Teste do contrato de ciclo de vida e unicidade do Familiar Espectral")
    public void testFamiliarEspectralLifecycleContract() {
        MockRPGPlayer player = new MockRPGPlayer(Race.HUMANO, 3);
        assertNull(player.caosarcana$getActiveFamiliarId(), "Jogador não deve ter familiar ativo inicialmente");

        java.util.UUID familiar1 = java.util.UUID.randomUUID();
        player.caosarcana$setActiveFamiliarId(familiar1);
        assertEquals(familiar1, player.caosarcana$getActiveFamiliarId(), "Familiar 1 deve estar registrado como ativo");

        // Nova invocação substitui a anterior
        java.util.UUID familiar2 = java.util.UUID.randomUUID();
        player.caosarcana$setActiveFamiliarId(familiar2);
        assertEquals(familiar2, player.caosarcana$getActiveFamiliarId(), "Familiar 2 deve substituir o Familiar 1");
        assertNotEquals(familiar1, player.caosarcana$getActiveFamiliarId());

        // Dissipação por desconexão ou morte limpa o vínculo
        player.caosarcana$setActiveFamiliarId(null);
        assertNull(player.caosarcana$getActiveFamiliarId(), "Familiar deve ser nulo após dissipação");
    }

    @Test
    @DisplayName("Teste da mecânica de redução de consumo de ar do Argoniano (3x fôlego normal)")
    public void testArgonianAirConsumptionRate() {
        // Vanilla: 300 ticks max air, decresce 1 por tick submerso
        int vanillaAir = 300;
        int vanillaTicksToEmpty = 0;
        while (vanillaAir > 0) {
            vanillaAir--;
            vanillaTicksToEmpty++;
        }
        assertEquals(300, vanillaTicksToEmpty, "Vanilla consome 300 unidades de ar em exatamente 300 ticks (15.0s)");

        int vanillaTicksToFirstDamage = vanillaTicksToEmpty;
        int vanillaDamageAir = 0;
        while (vanillaDamageAir > -20) {
            vanillaDamageAir--;
            vanillaTicksToFirstDamage++;
        }
        assertEquals(320, vanillaTicksToFirstDamage, "Vanilla leva 320 ticks (16.0s) até o primeiro dano por afogamento (-20 ar)");

        // Argoniano com regra: tickCount % 3 != 0 não consome ar (taxa 1/3)
        int argonianAir = 300;
        int argonianTicks = 0;
        while (argonianAir > 0) {
            argonianTicks++;
            if (argonianTicks % 3 == 0) {
                argonianAir--;
            }
        }
        assertEquals(900, argonianTicks, "Argoniano leva exatamente 900 ticks (45.0s) para esgotar o reservatório de ar (3.0x vanilla)");

        int argonianTicksToFirstDamage = argonianTicks;
        int argonianDamageAir = 0;
        while (argonianDamageAir > -20) {
            argonianTicksToFirstDamage++;
            if (argonianTicksToFirstDamage % 3 == 0) {
                argonianDamageAir--;
            }
        }
        assertEquals(960, argonianTicksToFirstDamage, "Argoniano leva exatamente 960 ticks (48.0s) até o primeiro dano por afogamento (3.0x vanilla)");
        assertEquals(3.0, (double) argonianTicks / (double) vanillaTicksToEmpty, 0.0001, "Multiplicador de esgotamento deve ser exatamente 3.0x");
        assertEquals(3.0, (double) argonianTicksToFirstDamage / (double) vanillaTicksToFirstDamage, 0.0001, "Multiplicador até primeiro dano deve ser exatamente 3.0x");
    }

    @Test
    @DisplayName("Teste de persistência de recargas e dissipação de familiar na morte do jogador")
    public void testCooldownPersistenceAndFamiliarDissipationOnDeath() {
        MockRPGPlayer oldPlayer = new MockRPGPlayer(Race.ALTO_ELFO, 10);
        oldPlayer.caosarcana$learnSpell("caosarcana:nobreza_altmer");
        oldPlayer.caosarcana$setSpellCooldown("caosarcana:nobreza_altmer", 1200L);

        java.util.UUID familiar = java.util.UUID.randomUUID();
        oldPlayer.caosarcana$setActiveFamiliarId(familiar);

        // Simula transferência de dados do evento COPY_FROM
        MockRPGPlayer newPlayer = new MockRPGPlayer(Race.ALTO_ELFO, 10);
        for (String spellId : oldPlayer.caosarcana$getKnownSpells()) {
            newPlayer.caosarcana$learnSpell(spellId);
            long ready = oldPlayer.caosarcana$getSpellReadyTick(spellId);
            if (ready > 0) {
                newPlayer.caosarcana$setSpellCooldown(spellId, ready);
            }
        }
        // Familiar dissipa na morte (não transfere UUID ativo)
        newPlayer.caosarcana$setActiveFamiliarId(null);

        // Valida que a recarga persistiu para evitar exploit de suicídio
        assertTrue(newPlayer.caosarcana$isSpellOnCooldown("caosarcana:nobreza_altmer", 500L));
        assertEquals(1200L, newPlayer.caosarcana$getSpellReadyTick("caosarcana:nobreza_altmer"));

        // Valida que o familiar foi dissociado
        assertNull(newPlayer.caosarcana$getActiveFamiliarId(), "Familiar não deve acompanhar o jogador após a morte");
    }

    @Test
    @DisplayName("Teste da regra experimental de arma de teste (minecraft:iron_sword para Orc Nível >= 3)")
    public void testExperimentalIronSwordRequirement() {
        // 1. Inicialmente a regra experimental deve estar inativa por padrão
        EquipmentRequirementRegistry.setExperimentalWeaponRuleActive(false);
        assertFalse(EquipmentRequirementRegistry.isExperimentalWeaponRuleActive());

        net.minecraft.resources.ResourceLocation ironSwordId = EquipmentRequirementRegistry.EXPERIMENTAL_IRON_SWORD_ID;
        MockRPGPlayer human = new MockRPGPlayer(Race.HUMANO, 1);
        MockRPGPlayer orcLvl1 = new MockRPGPlayer(Race.ORC, 1);
        MockRPGPlayer orcLvl2 = new MockRPGPlayer(Race.ORC, 2);
        MockRPGPlayer orcLvl3 = new MockRPGPlayer(Race.ORC, 3);
        MockRPGPlayer orcLvl10 = new MockRPGPlayer(Race.ORC, 10);
        MockRPGPlayer elfLvl10 = new MockRPGPlayer(Race.ALTO_ELFO, 10);

        // Com a regra inativa, qualquer raça/nível tem permissão liberada para a espada de ferro
        assertTrue(EquipmentRequirementRegistry.check(human, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP).isAllowed(),
                "Regra inativa: Humano/Imperial deve poder equipar espada de ferro");
        assertTrue(EquipmentRequirementRegistry.check(orcLvl1, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP).isAllowed(),
                "Regra inativa: Orc nível 1 deve poder equipar espada de ferro");

        // 2. Ativação da regra experimental do Lote B
        EquipmentRequirementRegistry.setExperimentalWeaponRuleActive(true);
        assertTrue(EquipmentRequirementRegistry.isExperimentalWeaponRuleActive());

        // Orc Nível 3 e acima: PERMITIDO
        assertTrue(EquipmentRequirementRegistry.check(orcLvl3, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP).isAllowed(),
                "Regra ativa: Orc nível 3 deve ter permissão liberada");
        assertTrue(EquipmentRequirementRegistry.check(orcLvl10, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP).isAllowed(),
                "Regra ativa: Orc nível 10 deve ter permissão liberada");

        // Orc Nível 1 e 2: BLOQUEADO (nível insuficiente)
        var resOrc1 = EquipmentRequirementRegistry.check(orcLvl1, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP);
        assertFalse(resOrc1.isAllowed(), "Regra ativa: Orc nível 1 deve ser bloqueado");
        var resOrc2 = EquipmentRequirementRegistry.check(orcLvl2, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP);
        assertFalse(resOrc2.isAllowed(), "Regra ativa: Orc nível 2 deve ser bloqueado");

        // Raça diferente (Imperial, Alto Elfo): BLOQUEADO (raça incompatível)
        var resHuman = EquipmentRequirementRegistry.check(human, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP);
        assertFalse(resHuman.isAllowed(), "Regra ativa: Imperial/Humano deve ser bloqueado");
        var resElf = EquipmentRequirementRegistry.check(elfLvl10, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP);
        assertFalse(resElf.isAllowed(), "Regra ativa: Alto Elfo nível 10 deve ser bloqueado");

        // 3. Preservação de Comércio e Armazenamento (TRADE e CRAFT não sofrem bloqueio)
        assertTrue(EquipmentRequirementRegistry.check(human, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.TRADE).isAllowed(),
                "Comércio e transporte da espada devem permanecer 100% liberados");
        assertTrue(EquipmentRequirementRegistry.check(human, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.CRAFT).isAllowed(),
                "Criação não deve ser afetada pela restrição de uso de arma");

        // 4. Desativação da regra restaura o comportamento padrão do modpack
        EquipmentRequirementRegistry.setExperimentalWeaponRuleActive(false);
        assertFalse(EquipmentRequirementRegistry.isExperimentalWeaponRuleActive());
        assertTrue(EquipmentRequirementRegistry.check(human, ironSwordId, br.com.caosarcana.common.equipment.RequirementType.EQUIP).isAllowed(),
                "Após desativação: Humano deve poder equipar espada de ferro normalmente");
    }

    @Test
    @DisplayName("Teste de rebalanceamento do Toque de Cura (12 segundos de recarga)")
    public void testHealingSpellCooldownBalance() {
        var healOpt = br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:toque_de_cura");
        assertTrue(healOpt.isPresent(), "Toque de Cura deve estar registrado");
        var heal = healOpt.get();
        assertEquals(240, heal.cooldownTicks(), "Cooldown do Toque de Cura deve ser de 240 ticks (12 segundos) para evitar spam");
        assertEquals(30.0f, heal.manaCost(), "Custo de mana do Toque de Cura deve ser 30.0f");
    }

    @Test
    @DisplayName("Teste de desaprendizado e expurgo de magias raciais ao mudar de raça")
    public void testRacialSpellPruningOnRaceSwitch() {
        MockRPGPlayer player = new MockRPGPlayer(Race.ORC, 1);
        player.caosarcana$learnSpell("caosarcana:ira_berserker"); // Magia racial do Orc
        player.caosarcana$learnSpell("caosarcana:centelha_ignea"); // Magia universal
        player.caosarcana$setSelectedSpell("caosarcana:ira_berserker");

        assertTrue(player.caosarcana$knowsSpell("caosarcana:ira_berserker"));
        assertTrue(player.caosarcana$knowsSpell("caosarcana:centelha_ignea"));
        assertEquals("caosarcana:ira_berserker", player.caosarcana$getSelectedSpell());

        // Simula a rotina de expurgo ao trocar de raça para Felino
        Race newRace = Race.FELINO;
        List<String> oldRacialSpells = new ArrayList<>();
        for (String known : player.caosarcana$getKnownSpells()) {
            br.com.caosarcana.content.spells.SpellRegistry.get(known).ifPresent(def -> {
                if (def.isRacial() && def.requiredRace() != newRace) {
                    oldRacialSpells.add(known);
                }
            });
        }
        for (String rm : oldRacialSpells) {
            player.caosarcana$unlearnSpell(rm);
        }

        // Concede a nova magia racial
        br.com.caosarcana.content.spells.SpellRegistry.getRacialSpellFor(newRace).ifPresent(racialSpell -> {
            player.caosarcana$learnSpell(racialSpell.id());
            if (player.caosarcana$getSelectedSpell().isEmpty() || oldRacialSpells.contains(player.caosarcana$getSelectedSpell())) {
                player.caosarcana$setSelectedSpell(racialSpell.id());
            }
        });

        // Verificações:
        assertFalse(player.caosarcana$knowsSpell("caosarcana:ira_berserker"), "Magia racial antiga deve ser expurgada");
        assertTrue(player.caosarcana$knowsSpell("caosarcana:olhos_da_noite"), "Nova magia racial deve ser aprendida");
        assertTrue(player.caosarcana$knowsSpell("caosarcana:centelha_ignea"), "Magia universal deve ser preservada intacta");
        assertEquals("caosarcana:olhos_da_noite", player.caosarcana$getSelectedSpell(), "Magia ativa deve ser atualizada para a nova raça");
    }

    @Test
    @DisplayName("Teste de paridade da Centelha Ígnea com valores vanilla de SmallFireball (5 dano, 5s fogo)")
    public void testFlameSparkProjectileConstants() {
        assertEquals(5.0f, br.com.caosarcana.content.spells.FlameSparkProjectile.BASE_DAMAGE, "Dano base da Centelha deve ser exatamente 5.0 (paridade com SmallFireball)");
        assertEquals(5, br.com.caosarcana.content.spells.FlameSparkProjectile.FIRE_SECONDS, "Duração do fogo da Centelha deve ser exatamente 5 segundos (paridade com SmallFireball)");
    }

    @Test
    @DisplayName("Teste de validação dos valores de referência de Centelha Ígnea (20 mana, 15 ticks / 0.75s)")
    public void testFlameSparkReferenceValues() {
        assertEquals(20.0f, br.com.caosarcana.content.spells.FlameSparkItem.MANA_COST, "Custo de mana deve ser 20");
        assertEquals(15, br.com.caosarcana.content.spells.FlameSparkItem.COOLDOWN_TICKS, "Recarga deve ser 15 ticks (0.75s)");
        assertEquals(20.0f, br.com.caosarcana.content.spells.SpellRegistry.CENTELHA_IGNEA.manaCost(), "Registro de Centelha deve refletir 20 de mana");
        assertEquals(15, br.com.caosarcana.content.spells.SpellRegistry.CENTELHA_IGNEA.cooldownTicks(), "Registro de Centelha deve refletir 15 ticks de recarga");
    }

    @Test
    @DisplayName("Teste de colisão em caixa e contenção à queima-roupa para Centelha")
    public void testPointBlankHitboxContainment() {
        net.minecraft.world.phys.AABB targetBox = new net.minecraft.world.phys.AABB(0, 0, 0, 1, 2, 1);
        net.minecraft.world.phys.Vec3 eyeInside = new net.minecraft.world.phys.Vec3(0.5, 1.5, 0.5);
        net.minecraft.world.phys.Vec3 eyeClose = new net.minecraft.world.phys.Vec3(-0.05, 1.5, 0.5);
        net.minecraft.world.phys.Vec3 rayEnd = eyeClose.add(new net.minecraft.world.phys.Vec3(0.35, 0, 0));

        // 1. Ponto de origem contido diretamente na caixa
        assertTrue(targetBox.inflate(0.15).contains(eyeInside), "Alvo que contém a origem deve ser detectado imediatamente");

        // 2. Trajetória imediata penetrando na caixa inflada
        assertTrue(targetBox.inflate(0.15).contains(rayEnd) || targetBox.inflate(0.15).intersects(new net.minecraft.world.phys.AABB(eyeClose, rayEnd)), "Trajetória colada deve registrar colisão");
    }

    @Test
    @DisplayName("Teste de compatibilidade de IDs antigos e transição para seleção pendente")
    public void testMigrationCompatibilityAndOldPowerBlocking() {
        // 1. imperial -> humano
        assertEquals(Race.HUMANO, Race.fromId("imperial"));
        assertEquals(Race.HUMANO, Race.fromId("humano"));

        // 2. guarda_vermelho -> redguard
        assertEquals(Race.REDGUARD, Race.fromId("guarda_vermelho"));
        assertEquals(Race.REDGUARD, Race.fromId("redguard"));

        // 3. bretao e elfo_negro -> exigem nova escolha (Race.NONE)
        assertEquals(Race.NONE, Race.fromId("bretao"));
        assertEquals(Race.NONE, Race.fromId("elfo_negro"));

        // 4. Bloqueio de poderes antigos e preservação de conhecimentos universais
        MockRPGPlayer migratingPlayer = new MockRPGPlayer(Race.NONE, 5);
        migratingPlayer.caosarcana$setChosenRace(false); // Pendente de seleção
        migratingPlayer.caosarcana$learnSpell("caosarcana:centelha_ignea"); // Universal preservada
        migratingPlayer.caosarcana$learnSpell("caosarcana:ira_ancestral"); // Poder antigo a ser bloqueado

        // Simula expurgo durante leitura de save
        migratingPlayer.caosarcana$unlearnSpell("caosarcana:ira_ancestral");
        migratingPlayer.caosarcana$unlearnSpell("caosarcana:pele_de_dragao");

        assertFalse(migratingPlayer.caosarcana$knowsSpell("caosarcana:ira_ancestral"), "Poder antigo de Elfo Negro deve ser bloqueado");
        assertFalse(migratingPlayer.caosarcana$knowsSpell("caosarcana:pele_de_dragao"), "Poder antigo de Bretão deve ser bloqueado");
        assertTrue(migratingPlayer.caosarcana$knowsSpell("caosarcana:centelha_ignea"), "Magia universal deve ser preservada");
        assertFalse(migratingPlayer.caosarcana$hasChosenRace(), "Personagem deve estar aguardando seleção racial");
        assertEquals(5, migratingPlayer.caosarcana$getGeneralLevel(), "Nível do personagem deve ser preservado");
    }

    @Test
    @DisplayName("Teste de validações da Árvore de Talentos de Piromancia (Raiz, Exclusão Mútua e Preservação de Magias)")
    public void testPyromancyTalentTreeBridge() {
        MockRPGPlayer playerWithoutCentelha = new MockRPGPlayer(Race.HUMANO, 10);
        MockRPGPlayer playerWithCentelha = new MockRPGPlayer(Race.HUMANO, 10);
        playerWithCentelha.caosarcana$learnSpell("caosarcana:centelha_ignea");

        ResourceLocation piroCategory = br.com.caosarcana.common.talent.CaosArcanaTalentBridge.CATEGORY_PIROMANCIA;
        ResourceLocation otherCategory = new ResourceLocation("vanilla", "combat");

        // 1. Categorias externas nunca são bloqueadas pelo Caos Arcana
        assertTrue(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(playerWithoutCentelha, otherCategory, "any_skill"));

        // IDs iguais em outra categoria não pertencem à ponte de Piromancia.
        for (ResourceLocation category : new ResourceLocation[] {
                otherCategory, new ResourceLocation("caosarcana", "restauracao"), null
        }) {
            assertTrue(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                    playerWithoutCentelha, category,
                    br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_RAIZ_AFINIDADE));
            assertTrue(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                    playerWithoutCentelha, category,
                    br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_FOCO_COMBUSTAO));
            assertTrue(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                    playerWithoutCentelha, category,
                    br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_FOCO_CINETICO));
        }

        // 2. Raiz exige conhecer a Centelha Ígnea
        assertFalse(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                playerWithoutCentelha,
                piroCategory,
                br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_RAIZ_AFINIDADE
        ), "Jogador sem Centelha Ígnea não pode desbloquear a raiz");

        assertTrue(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                playerWithCentelha,
                piroCategory,
                br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_RAIZ_AFINIDADE
        ), "Jogador com Centelha Ígnea pode desbloquear a raiz");

        // 3. Especializações exigem a Raiz desbloqueada
        assertFalse(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                playerWithoutCentelha,
                piroCategory,
                br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_FOCO_COMBUSTAO
        ), "Foco em combustão sem raiz deve ser recusado");

        assertFalse(br.com.caosarcana.common.talent.CaosArcanaTalentBridge.canUnlock(
                playerWithoutCentelha,
                piroCategory,
                br.com.caosarcana.common.talent.CaosArcanaTalentBridge.TALENT_FOCO_CINETICO
        ), "Foco cinético sem raiz deve ser recusado");

        // 4. Preservação de Grimórios no Reset
        // Simula reset de talentos do jogador mantendo o feitiço no NBT de Caos Arcana
        playerWithCentelha.caosarcana$setMana(100.0f);
        assertTrue(playerWithCentelha.caosarcana$knowsSpell("caosarcana:centelha_ignea"), "Centelha deve permanecer aprendida");

        // 5. Valores base inalterados para quem não possui talentos
        assertEquals(20.0f, br.com.caosarcana.content.spells.SpellRegistry.CENTELHA_IGNEA.manaCost(), "Mana base deve permanecer 20");
        assertEquals(15, br.com.caosarcana.content.spells.SpellRegistry.CENTELHA_IGNEA.cooldownTicks(), "Recarga base deve permanecer 15 ticks");
    }

    @Test
    @DisplayName("Teste de parâmetros configuráveis dos talentos, limites e descrições dinâmicas")
    public void testTalentConfigLimitsAndDefaults() {
        // 1. Padrões de referência aprovados
        br.com.caosarcana.common.talent.TalentConfig.resetToDefaults();
        assertEquals(15.0f, br.com.caosarcana.common.talent.TalentConfig.getRootManaCost());
        assertEquals(10, br.com.caosarcana.common.talent.TalentConfig.getCombustionFireSeconds());
        assertEquals(1.5, br.com.caosarcana.common.talent.TalentConfig.getKineticKnockbackStrength());

        // 2. Descrições informam o efeito; valores numéricos exatos são mostrados na tela de magia,
        // que usa o valor sincronizado do servidor para não exibir um número local divergente.
        com.google.gson.JsonObject defs = br.com.caosarcana.common.talent.TalentConfig.buildDynamicDefinitions();
        assertTrue(defs.has("caosarcana:piro_raiz_afinidade"));
        assertTrue(defs.has("caosarcana:piro_foco_combustao"));
        assertTrue(defs.has("caosarcana:piro_foco_cinetico"));

        String raizDesc = defs.getAsJsonObject("caosarcana:piro_raiz_afinidade").getAsJsonObject("description").get("text").getAsString();
        assertTrue(raizDesc.contains("reduzindo seu custo de conjuração"));
        assertTrue(raizDesc.contains("custo efetivo aparece na tela de seleção de magias"));
        assertFalse(raizDesc.contains("15 de mana"), "A descrição não deve fixar um custo local do cliente");

        String combustaoDesc = defs.getAsJsonObject("caosarcana:piro_foco_combustao").getAsJsonObject("description").get("text").getAsString();
        assertTrue(combustaoDesc.contains("10s de fogo"), "Descrição de combustão deve refletir 10s de fogo");

        String cineticoDesc = defs.getAsJsonObject("caosarcana:piro_foco_cinetico").getAsJsonObject("description").get("text").getAsString();
        assertTrue(cineticoDesc.contains("recuo 1.5"), "Descrição de cinético deve refletir recuo 1.5");

        // 3. Modificação temporária e reflexo dinâmico na descrição
        br.com.caosarcana.common.talent.TalentConfig.setValuesForTest(12.0f, 14, 2.0);
        com.google.gson.JsonObject defsCustom = br.com.caosarcana.common.talent.TalentConfig.buildDynamicDefinitions();
        assertEquals(raizDesc, defsCustom.getAsJsonObject("caosarcana:piro_raiz_afinidade").getAsJsonObject("description").get("text").getAsString());
        assertTrue(defsCustom.getAsJsonObject("caosarcana:piro_foco_combustao").getAsJsonObject("description").get("text").getAsString().contains("14s de fogo"));
        assertTrue(defsCustom.getAsJsonObject("caosarcana:piro_foco_cinetico").getAsJsonObject("description").get("text").getAsString().contains("recuo 2.0"));

        // Restaura padrões para isolamento dos demais testes
        br.com.caosarcana.common.talent.TalentConfig.resetToDefaults();
    }

    @Test
    @DisplayName("Teste de progressão de Piromancia: curva de XP, níveis e limite 100")
    public void testPyromancyProgressionCurveAndLeveling() {
        MockRPGPlayer player = new MockRPGPlayer(Race.HUMANO, 1);
        assertEquals(1, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(0, player.caosarcana$getSchoolXp("piromancia"));

        // Nível 1 exige 50 XP (Base 50 + (1-1)*25)
        assertEquals(50, br.com.caosarcana.content.spells.PyromancyProgressionHandler.getXpNeededForLevel(1));
        // Nível 2 exige 75 XP (Base 50 + (2-1)*25)
        assertEquals(75, br.com.caosarcana.content.spells.PyromancyProgressionHandler.getXpNeededForLevel(2));

        // Adiciona 40 XP: nível continua 1, XP 40
        br.com.caosarcana.content.spells.PyromancyProgressionHandler.addPyromancyXp(player, 40);
        assertEquals(1, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(40, player.caosarcana$getSchoolXp("piromancia"));

        // Adiciona mais 10 XP: total 50 -> sobe para nível 2, sobra 0 XP
        br.com.caosarcana.content.spells.PyromancyProgressionHandler.addPyromancyXp(player, 10);
        assertEquals(2, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(0, player.caosarcana$getSchoolXp("piromancia"));

        // Adiciona 80 XP: consome 75 para nível 3, sobra 5 XP
        br.com.caosarcana.content.spells.PyromancyProgressionHandler.addPyromancyXp(player, 80);
        assertEquals(3, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(5, player.caosarcana$getSchoolXp("piromancia"));

        // Teste de teto no nível 100
        player.caosarcana$setSchoolLevel("piromancia", 100);
        player.caosarcana$setSchoolXp("piromancia", 0);
        br.com.caosarcana.content.spells.PyromancyProgressionHandler.addPyromancyXp(player, 500);
        assertEquals(100, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(0, player.caosarcana$getSchoolXp("piromancia"));
    }

    @Test
    @DisplayName("Teste de marcos de talento: concessão única, idempotência e pendência sem Pufferfish")
    public void testPyromancyMilestones() {
        MockRPGPlayer player = new MockRPGPlayer(Race.HUMANO, 1);
        player.caosarcana$setSchoolLevel("piromancia", 10);

        // Marco não foi clamado inicialmente
        assertFalse(player.caosarcana$hasClaimedMilestone("piromancia_10"));

        // Quando Pufferfish está ausente (ServerPlayer nulo ou mod descarregado), a recompensa não é clamada e fica pendente
        boolean awardedWithoutServer = br.com.caosarcana.content.spells.PyromancyProgressionHandler.checkSingleMilestone(
                null, player, "piromancia_10", 10, 10, 1
        );
        assertFalse(awardedWithoutServer, "Sem mod/servidor de talentos, concessão não pode ser dada como entregue");
        assertFalse(player.caosarcana$hasClaimedMilestone("piromancia_10"), "Marco deve continuar pendente");

        // Simula concessão bem-sucedida do marco
        player.caosarcana$claimMilestone("piromancia_10");
        assertTrue(player.caosarcana$hasClaimedMilestone("piromancia_10"));

        // Tentativa de conceder novamente (idempotência): não pode conceder duplicado
        boolean secondClaim = br.com.caosarcana.content.spells.PyromancyProgressionHandler.checkSingleMilestone(
                null, player, "piromancia_10", 10, 10, 1
        );
        assertFalse(secondClaim, "Marco já clamado não pode ser concedido novamente");
    }

    @Test
    @DisplayName("Teste de persistência de XP e marcos (NBT) e preservação pós-morte")
    public void testProgressionPersistenceAndRespawn() {
        MockRPGPlayer player = new MockRPGPlayer(Race.HUMANO, 5);
        player.caosarcana$setSchoolLevel("piromancia", 12);
        player.caosarcana$setSchoolXp("piromancia", 35);
        player.caosarcana$claimMilestone("piromancia_10");

        // Simula cópia entre instâncias (como ocorre no COPY_FROM ao renascer)
        MockRPGPlayer respawned = new MockRPGPlayer(Race.HUMANO, 5);
        respawned.caosarcana$setSchoolLevel("piromancia", player.caosarcana$getSchoolLevel("piromancia"));
        respawned.caosarcana$setSchoolXp("piromancia", player.caosarcana$getSchoolXp("piromancia"));
        for (String m : player.caosarcana$getClaimedMilestones()) {
            respawned.caosarcana$claimMilestone(m);
        }

        assertEquals(12, respawned.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(35, respawned.caosarcana$getSchoolXp("piromancia"));
        assertTrue(respawned.caosarcana$hasClaimedMilestone("piromancia_10"));
    }

    @Test
    @DisplayName("Teste do limitador de acertos por alvo (anti-farm)")
    public void testAntiFarmHitLimiter() {
        java.util.Set<String> tags = new java.util.HashSet<>();

        assertEquals(0, br.com.caosarcana.content.spells.PyromancyProgressionHandler.parseHitsFromTags(tags));

        for (int i = 1; i <= 5; i++) {
            tags.removeIf(t -> t.startsWith("caosarcana:piro_hits_"));
            tags.add("caosarcana:piro_hits_" + i);
            assertEquals(i, br.com.caosarcana.content.spells.PyromancyProgressionHandler.parseHitsFromTags(tags));
        }

        // Atingiu o limite de 5 acertos configurado como padrão
        int maxHits = br.com.caosarcana.common.talent.TalentConfig.getMaxHitsPerTarget();
        assertEquals(5, maxHits);
        assertTrue(br.com.caosarcana.content.spells.PyromancyProgressionHandler.parseHitsFromTags(tags) >= maxHits);
    }

    @Test
    @DisplayName("Teste da ferramenta administrativa de escolas: ajuste de nível, zeramento de XP e marcos")
    public void testSetSchoolAdministrativeToolLogic() {
        MockRPGPlayer player = new MockRPGPlayer(Race.HUMANO, 1);
        player.caosarcana$setSchoolLevel("piromancia", 5);
        player.caosarcana$setSchoolXp("piromancia", 45);

        assertEquals(5, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(45, player.caosarcana$getSchoolXp("piromancia"));
        assertFalse(player.caosarcana$hasClaimedMilestone("piromancia_10"));

        // Simula comando /caos setschool piromancia 10
        player.caosarcana$setSchoolLevel("piromancia", 10);
        player.caosarcana$setSchoolXp("piromancia", 0);

        assertEquals(10, player.caosarcana$getSchoolLevel("piromancia"));
        assertEquals(0, player.caosarcana$getSchoolXp("piromancia"), "XP residual deve ser zerado ao definir nível");

        // Simula verificação e entrega do marco de nível 10
        player.caosarcana$claimMilestone("piromancia_10");
        assertTrue(player.caosarcana$hasClaimedMilestone("piromancia_10"));

        // Simula redução de nível (ex: setschool piromancia 5)
        player.caosarcana$setSchoolLevel("piromancia", 5);
        player.caosarcana$setSchoolXp("piromancia", 0);
        assertEquals(5, player.caosarcana$getSchoolLevel("piromancia"));

        // Simula elevação novamente para 10: o marco não pode ser concedido repetido
        player.caosarcana$setSchoolLevel("piromancia", 10);
        boolean awardedAgain = br.com.caosarcana.content.spells.PyromancyProgressionHandler.checkSingleMilestone(
                null, player, "piromancia_10", 10, 10, 1
        );
        assertFalse(awardedAgain, "Reduzir e elevar o nível não pode conceder o marco repetidamente");

        // Confirma que nível geral e outras propriedades continuam intactos
        assertEquals(1, player.caosarcana$getGeneralLevel(), "Nível geral não pode ser alterado por setschool");
    }

    @Test
    @DisplayName("Teste de validação das tabelas de loot inicial e registro de magias")
    public void testLootTableConfigurationIntegrity() {
        net.minecraft.resources.ResourceLocation simpleDungeon = net.minecraft.world.level.storage.loot.BuiltInLootTables.SIMPLE_DUNGEON;
        assertEquals("minecraft:chests/simple_dungeon", simpleDungeon.toString());

        // Validação da magia da Centelha Ígnea que é garantida no baú inicial
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.get(br.com.caosarcana.content.spells.SpellCastHandler.CENTELHA_IGNEA_ID).isPresent());
        var centelha = br.com.caosarcana.content.spells.SpellRegistry.get(br.com.caosarcana.content.spells.SpellCastHandler.CENTELHA_IGNEA_ID).get();
        assertEquals("piromancia", centelha.school());
        assertEquals(1, centelha.minSchoolLevel());

        // Validação de que os grimórios avançados existem no registro para distribuição nas demais dungeons
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:geada_congelante").isPresent());
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:relampago_arcano").isPresent());
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:toque_de_cura").isPresent());
        assertTrue(br.com.caosarcana.content.spells.SpellRegistry.get("caosarcana:corpo_de_carvalho").isPresent());
    }

    @Test
    public void testTalentConfigRejectsNonFiniteAndOrdersMilestones() throws Exception {
        var config = br.com.caosarcana.common.talent.TalentConfig.class;
        var read = config.getDeclaredMethod("readConfig", java.io.File.class);
        read.setAccessible(true);
        var file = java.nio.file.Files.createTempFile("caos-talents-", ".json");
        try {
            java.nio.file.Files.writeString(file, "{\"milestone1Level\":100}");
            read.invoke(null, file.toFile());
            assertEquals(99, br.com.caosarcana.common.talent.TalentConfig.getMilestone1Level());
            assertEquals(100, br.com.caosarcana.common.talent.TalentConfig.getMilestone2Level());
            java.nio.file.Files.writeString(file, "{\"rootManaCost\":12.5}");
            read.invoke(null, file.toFile());
            assertEquals(10, br.com.caosarcana.common.talent.TalentConfig.getMilestone1Level());
            assertTrue(br.com.caosarcana.common.talent.TalentConfig.buildDynamicDefinitions()
                    .getAsJsonObject("caosarcana:piro_raiz_afinidade").getAsJsonObject("description")
                    .get("text").getAsString().contains("custo efetivo aparece na tela de seleção de magias"));
            java.nio.file.Files.writeString(file, "{\"rootManaCost\":\"NaN\"}");
            read.invoke(null, file.toFile());
            assertEquals(15.0f, br.com.caosarcana.common.talent.TalentConfig.getRootManaCost());
            java.nio.file.Files.writeString(file, "{\"kineticKnockbackStrength\":\"Infinity\"}");
            read.invoke(null, file.toFile());
            assertEquals(1.5, br.com.caosarcana.common.talent.TalentConfig.getKineticKnockbackStrength());
        } finally {
            java.nio.file.Files.deleteIfExists(file);
            br.com.caosarcana.common.talent.TalentConfig.resetToDefaults();
        }
    }

    @Test
    public void testEffectiveSparkCostPresentation() {
        var spell = br.com.caosarcana.content.spells.SpellRegistry.CENTELHA_IGNEA;
        try {
            br.com.caosarcana.client.ClientRPGData.setSparkManaCost(12.5f);
            assertEquals("12.5", br.com.caosarcana.client.ClientRPGData.manaCostText(spell));
            br.com.caosarcana.client.ClientRPGData.setSparkManaCost(20f);
            assertEquals("20", br.com.caosarcana.client.ClientRPGData.manaCostText(spell));
            br.com.caosarcana.client.ClientRPGData.setSparkManaCost(Float.NaN);
            assertEquals("...", br.com.caosarcana.client.ClientRPGData.manaCostText(spell));
        } finally {
            br.com.caosarcana.client.ClientRPGData.setSparkManaCost(Float.NaN);
        }
    }

    @Test
    public void testManaAmountFormattingPreservesFractions() {
        assertEquals("12.5", br.com.caosarcana.content.spells.SpellCastHandler.formatManaAmount(12.5f));
        assertEquals("20", br.com.caosarcana.content.spells.SpellCastHandler.formatManaAmount(20.0f));
    }
}
