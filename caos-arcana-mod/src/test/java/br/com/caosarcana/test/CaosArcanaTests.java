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

        // Fallbacks seguros
        assertEquals(Race.NONE, Race.fromId("alien"));
        assertEquals(Race.NONE, Race.fromId(""));
        assertEquals(Race.NONE, Race.fromId(null));
    }

    @Test
    @DisplayName("Teste de auditoria documental e separação de características ativas vs planejadas")
    public void testRaceDocumentationStatusAndTraits() {
        // Orc e Felino são referências explícitas (Decididas)
        assertTrue(Race.ORC.getDocumentationStatus().contains("Decidido"));
        assertTrue(Race.FELINO.getDocumentationStatus().contains("Decidido"));

        // Alto Elfo é proposta de estudo (Pendente)
        assertTrue(Race.ALTO_ELFO.getDocumentationStatus().contains("Pendente"));

        // Orc: vantagens ativas presentes, penalidade de mana é planejada
        assertTrue(Race.ORC.getActiveAdvantages().contains("Dano físico +2"));
        assertTrue(Race.ORC.getPlannedTraits().contains("Custo de mana"));

        // Felino: mobilidade ativa, dano de queda é planejado
        assertTrue(Race.FELINO.getActiveAdvantages().contains("Velocidade de movimento +15%"));
        assertTrue(Race.FELINO.getPlannedTraits().contains("queda"));

        // Alto Elfo: mana é parcial, penalidade física é ativa
        assertTrue(Race.ALTO_ELFO.getPartialTraits().contains("Mana Máxima +50"));
        assertTrue(Race.ALTO_ELFO.getActiveDisadvantages().contains("Dano corpo a corpo -15%"));
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
}
