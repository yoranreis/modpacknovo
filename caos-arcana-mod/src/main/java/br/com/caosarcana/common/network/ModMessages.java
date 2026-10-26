package br.com.caosarcana.common.network;

import br.com.caosarcana.CaosArcana;
import br.com.caosarcana.common.data.IRPGPlayerData;
import br.com.caosarcana.common.race.Race;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ModMessages {
    public static final ResourceLocation OPEN_RACE_SELECT_ID = CaosArcana.id("open_race_select");
    public static final ResourceLocation SELECT_RACE_C2S_ID = CaosArcana.id("select_race");
    public static final ResourceLocation SYNC_RPG_DATA_S2C_ID = CaosArcana.id("sync_rpg_data");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(SELECT_RACE_C2S_ID, (server, player, handler, buf, responseSender) -> {
            String raceId = buf.readUtf();
            server.execute(() -> {
                if (player instanceof IRPGPlayerData rpgPlayer) {
                    if (!rpgPlayer.caosarcana$hasChosenRace()) {
                        Race race = Race.fromId(raceId);
                        if (race != Race.NONE) {
                            rpgPlayer.caosarcana$setRace(race);
                            rpgPlayer.caosarcana$setChosenRace(true);
                            race.applyModifiers(player);

                            syncPlayerData(player);

                            player.sendSystemMessage(Component.literal("§6[Caos Arcana] §aVocê escolheu a raça: §e" + race.getDisplayName() + "§a!"));
                            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);

                            CaosArcana.LOGGER.info("Jogador {} escolheu a raca: {}", player.getName().getString(), race.getDisplayName());
                        }
                    }
                }
            });
        });
    }

    public static void registerServerEvents() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player instanceof IRPGPlayerData rpgPlayer) {
                // Reaplica modificadores raciais ao conectar
                if (rpgPlayer.caosarcana$hasChosenRace()) {
                    rpgPlayer.caosarcana$getRace().applyModifiers(player);
                }

                syncPlayerData(player);

                // Se ainda nao escolheu a raca, manda abrir a tela
                if (!rpgPlayer.caosarcana$hasChosenRace()) {
                    server.execute(() -> {
                        FriendlyByteBuf buf = PacketByteBufs.create();
                        ServerPlayNetworking.send(player, OPEN_RACE_SELECT_ID, buf);
                    });
                }
            }
        });
    }

    public static void syncPlayerData(ServerPlayer player) {
        if (player instanceof IRPGPlayerData rpgPlayer) {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeUtf(rpgPlayer.caosarcana$getRace().getId());
            buf.writeBoolean(rpgPlayer.caosarcana$hasChosenRace());
            buf.writeFloat(rpgPlayer.caosarcana$getMana());
            buf.writeFloat(rpgPlayer.caosarcana$getMaxMana());
            buf.writeInt(rpgPlayer.caosarcana$getGeneralLevel());

            ServerPlayNetworking.send(player, SYNC_RPG_DATA_S2C_ID, buf);
        }
    }
}
