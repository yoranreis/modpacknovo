package br.com.caosarcana.content.spells;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FlameSparkItem extends Item {
    public static final float MANA_COST = 20.0f;
    public static final int COOLDOWN_TICKS = 15; // 0.75 segundos

    public FlameSparkItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            if (player instanceof IRPGPlayerData rpgPlayer && rpgPlayer.caosarcana$getMana() < MANA_COST) {
                return InteractionResultHolder.fail(stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        // Lógica de Servidor
        if (player instanceof ServerPlayer serverPlayer && serverPlayer instanceof IRPGPlayerData rpgPlayer) {
            // 1. Exige ter escolhido uma raça
            if (!rpgPlayer.caosarcana$hasChosenRace()) {
                serverPlayer.displayClientMessage(Component.literal("§c[Caos Arcana] Você precisa escolher sua raça antes de canalizar magia!"), true);
                serverPlayer.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.8f, 1.2f);
                return InteractionResultHolder.fail(stack);
            }

            // 2. Validação e Consumo de Mana Seguro
            if (!rpgPlayer.caosarcana$consumeMana(MANA_COST)) {
                serverPlayer.displayClientMessage(
                        Component.literal("§cMana insuficiente! §7(Requer: §b" + (int) MANA_COST + "§7 | Atual: §b" + (int) rpgPlayer.caosarcana$getMana() + "§7)"),
                        true
                );
                serverPlayer.playNotifySound(SoundEvents.DISPENSER_FAIL, SoundSource.PLAYERS, 0.9f, 1.5f);
                ModMessages.syncPlayerData(serverPlayer); // Garante sincronização da mana com o cliente
                return InteractionResultHolder.fail(stack);
            }

            // Sincroniza imediatamente a nova mana para acionar o HUD e animação de gasto
            ModMessages.syncPlayerData(serverPlayer);

            // Cooldown de conjuração
            serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

            // Sons de conjuração arcana ígnea
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 1.3f);
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.6f, 1.6f);

            // Criação e disparo do projétil místico
            Vec3 look = serverPlayer.getLookAngle();
            double spawnX = serverPlayer.getX() + look.x * 0.8;
            double spawnY = serverPlayer.getEyeY() - 0.1 + look.y * 0.8;
            double spawnZ = serverPlayer.getZ() + look.z * 0.8;

            FlameSparkProjectile projectile = new FlameSparkProjectile(serverPlayer.level(), serverPlayer, look.x, look.y, look.z);
            projectile.setPos(spawnX, spawnY, spawnZ);
            serverPlayer.level().addFreshEntity(projectile);

            // Efeito visual adicional de partículas de chama arcana
            if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, spawnX, spawnY, spawnZ, 12, 0.15, 0.15, 0.15, 0.05);
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spawnX, spawnY, spawnZ, 4, 0.1, 0.1, 0.1, 0.02);
            }

            serverPlayer.displayClientMessage(
                    Component.literal("§6[Piromancia] §cCentelha Ígnea conjurada! §b(-" + (int) MANA_COST + " Mana)"),
                    true
            );

            return InteractionResultHolder.sidedSuccess(stack, false);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        tooltip.add(Component.literal("§6Escola: §cPiromancia §7(Nível 1)"));
        tooltip.add(Component.literal("§bCusto: §f20 Mana"));
        tooltip.add(Component.literal("§7Dispara um projétil de fogo místico que incendeia o alvo atingido.").withStyle(ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, isAdvanced);
    }
}
