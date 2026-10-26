package br.com.caosarcana.common.command;

import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.network.ModMessages;
import br.com.caosarcana.common.race.Race;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
                        ctx.getSource().sendSuccess(() -> Component.literal("§6--- [Caos Arcana: Status] ---"), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eRaça: §f" + race.getDisplayName()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§eNível Geral: §f" + rpgPlayer.caosarcana$getGeneralLevel()), false);
                        ctx.getSource().sendSuccess(() -> Component.literal("§bMana: §f" + String.format("%.0f/%.0f", rpgPlayer.caosarcana$getMana(), rpgPlayer.caosarcana$getMaxMana())), false);
                    }
                    return 1;
                })
            )
            .then(Commands.literal("resetrace")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    if (player instanceof IRPGPlayerData rpgPlayer) {
                        rpgPlayer.caosarcana$getRace().removeModifiers(player);
                        rpgPlayer.caosarcana$setRace(Race.NONE);
                        rpgPlayer.caosarcana$setChosenRace(false);

                        ModMessages.syncPlayerData(player);

                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, ModMessages.OPEN_RACE_SELECT_ID, buf);

                        ctx.getSource().sendSuccess(() -> Component.literal("§a[Caos Arcana] Raça resetada. Tela de seleção aberta."), false);
                    }
                    return 1;
                })
            )
        );
    }
}
