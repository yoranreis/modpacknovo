package br.com.caosarcana.common.command;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.equipment.EquipmentValidationHandler;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class CaosArcanaCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CaosArcanaCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        dispatcher.register(Commands.literal("caos")
            .then(Commands.literal("status")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        Race race = rpgPlayer.caosarcana$getRace();
                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Caos Arcana: Status do Personagem] ---"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eRaça Atual: §f" + race.getDisplayName() + " §7(" + race.getDocumentationStatus() + ")"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eNível Geral: §f" + rpgPlayer.caosarcana$getGeneralLevel()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§bMana: §f" + String.format("%.0f/%.0f", rpgPlayer.caosarcana$getMana(), rpgPlayer.caosarcana$getMaxMana())), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§aVantagens Ativas: §7" + race.getActiveAdvantages()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§cPenalidades Ativas: §7" + race.getActiveDisadvantages()), false);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("resetrace")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        // 1. Remove modificadores antigos da raça
                        rpgPlayer.caosarcana$getRace().removeModifiers(player);

                        // 2. Limpa SOMENTE os dados de raça, preservando nível geral, mana e escolas
                        rpgPlayer.caosarcana$setRace(Race.NONE);
                        rpgPlayer.caosarcana$setChosenRace(false);

                        // 3. Sincroniza dados com o cliente
                        ModMessages.syncPlayerData(player);

                        // 4. Valida e desequipa imediatamente itens que dependiam da raça antiga
                        EquipmentValidationHandler.validateAndEnforce(player);

                        // 5. Reabre a tela de escolha obrigatória
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, ModMessages.OPEN_RACE_SELECT_ID, buf);

                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Raça resetada com sucesso. Modificadores antigos removidos, equipamentos incompatíveis desequipados e tela reaberta."), true);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("setlevel")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("level", IntegerArgumentType.integer(1, 100))
                    .executes(ctx -> {
                        int newLevel = IntegerArgumentType.getInteger(ctx, "level");
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        if (player instanceof IRPGPlayerData rpgPlayer) {
                            rpgPlayer.caosarcana$setGeneralLevel(newLevel);
                            ModMessages.syncPlayerData(player);
                            EquipmentValidationHandler.validateAndEnforce(player);

                            ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Nível geral alterado para §e" + newLevel + "§a. Requisitos revalidados."), true);
                        }
                        return 1;
                    })
                )
            )
            .then(Commands.literal("checkequipment")
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    EquipmentValidationHandler.validateAndEnforce(player);
                    ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Verificação de equipamentos concluída no servidor."), false);
                    return 1;
                })
            )
        );
    }
}
