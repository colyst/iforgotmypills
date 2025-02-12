package com.example;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DimensionEvents {

    public static void onPlayerEnterNether(ServerPlayerEntity player) {
        String message = player.getName().getString() + " has entered the nether";
        player.getServerWorld().getServer().getPlayerManager().broadcast(Text.literal(message), false);
    }

    public static void onPlayerLeaveNether(ServerPlayerEntity player) {
        String message = player.getName().getString() + " has left the nether";
        player.getServerWorld().getServer().getPlayerManager().broadcast(Text.literal(message), false);
    }

    public static void onPlayerEnterEnd(ServerPlayerEntity player) {
        String message = player.getName().getString() + " has entered the end";
        player.getServerWorld().getServer().getPlayerManager().broadcast(Text.literal(message), false);
    }

    public static void onPlayerLeaveEnd(ServerPlayerEntity player) {
        String message = player.getName().getString() + " has left the end";
        player.getServerWorld().getServer().getPlayerManager().broadcast(Text.literal(message), false);
    }
}
