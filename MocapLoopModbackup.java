package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.util.StringIdentifiable;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.util.Uuids;
//
//import java.util.UUID;
//import com.mojang.authlib.GameProfile;

public class MocapLoopMod implements ModInitializer {
	private static final int RECORD_INTERVAL_TICKS = 6000; // 5 minutes
	private boolean isLoopActive = false;

	@Override
	public void onInitialize() {
		System.out.println("Mocap Loop Mod Initialized!");
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(server, handler.getPlayer()));
	}

	private void onServerTick(MinecraftServer server) {
		if (!isLoopActive) return;

		// increment the timer scoreboard
		executeCommand(server, "scoreboard players add #timer mocap_timer 1");

		// start recording at tick 1
		executeCommand(server, "execute if score #timer mocap_timer matches 1 run mocap recording start @a");

		// stop, save, and loop playback when the interval is reached
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap recording stop");
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap playing stopAll");
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap recording save loop" + getLoopCount(server));

		// read loopCount from scoreboard and increment it
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run scoreboard players add #loops mocap_loopcount 1");

		// replay all loops up to the stored loop count
		for (int i = 0; i <= getLoopCount(server); i++) {
			executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap playing start loop" + i);
		}

		// reset timer
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run scoreboard players set #timer mocap_timer 0");
	}

	private void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
		executeCommand(server, "say mocap loop started automatically!");
		// HERE IS THE MOCAP LOOP STARTED AUTOMATICALLY OUTPUT

		if (!isLoopActive) {
			startLoop(server);
		}
	}

	public void startLoop(MinecraftServer server) {
		isLoopActive = true;

		// ensure scoreboards exist
		executeCommand(server, "scoreboard objectives add mocap_timer dummy");
		executeCommand(server, "scoreboard objectives add mocap_loopcount dummy");
		// add code here if you want it to be executed upon joining the world
		executeCommand(server, "say mocap looping started!");
	}

	public void stopLoop(MinecraftServer server) {
		isLoopActive = false;
		executeCommand(server, "mocap playing stopAll");
		//executeCommand(server, "scoreboard players add #loops mocap_loopcount 1");
		//executeCommand(server, "mocap recording save loop" + getLoopCount(server));
		executeCommand(server, "say mocap looping stopped!");
	}

	public int getLoopCount(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		ScoreboardObjective objective = scoreboard.getNullableObjective("mocap_loopcount");

		if (objective != null) {
			ReadableScoreboardScore score = scoreboard.getScore(ScoreHolder.fromName("#loops"), objective);
			return (score != null) ? score.getScore() : 0;
		}

		return 0; // default to 0 if the scoreboard or score is missing
	}

	private void executeCommand(MinecraftServer server, String command) {
		server.getCommandManager().executeWithPrefix(
				server.getCommandSource().withLevel(4).withSilent(), command
		);
	}
}