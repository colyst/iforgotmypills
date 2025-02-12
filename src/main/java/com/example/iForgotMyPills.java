package com.example;

// by memo and colyst
// discord: .memo_
// discord: colyst
// mail: colyst9@gmail.com

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class iForgotMyPills implements ModInitializer {
	private static final int RECORD_INTERVAL_TICKS = 12000; // 10 minutes
	private boolean isLoopActive = false;

	@Override
	public void onInitialize() {
		System.out.println("Mocap Loop Mod Initialized!");

		// Register lifecycle events to reset state on every new server instance.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			// Reset your state here
			isLoopActive = false;  // or whatever is appropriate
			// You might also want to call startLoop here, if that fits your design
			System.out.println("New server started: resetting loop state.");
		});

		// Register your tick event
		ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);

		// Register join/disconnect events (these may work in some contexts)
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			System.out.println("DEBUG: ServerPlayConnectionEvents.JOIN fired for " +
					handler.getPlayer().getName().getString());
			onPlayerJoin(server, handler.getPlayer());
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			onPlayerDisconnect(server, handler.getPlayer());
		});
	}

	private void onServerTick(MinecraftServer server) {
		//if (!isLoopActive) return;

		executeCommand(server, "execute if score #timer mocap_timer = #calc calc run mocap recording start @a");
		executeCommand(server, "execute if score #timer mocap_timer = #calc calc run mocap recording start @a");
		executeCommand(server, "execute if score #timer mocap_timer = #calc calc run mocap recording start @a");
		executeCommand(server, "execute if score #timer mocap_timer = #calc calc run scoreboard players set #calc calc -1");

		// Increment the timer scoreboard
		executeCommand(server, "scoreboard players add #timer mocap_timer 1");

		// Start recording at tick 1
		executeCommand(server, "execute if score #timer mocap_timer matches 1 run mocap recording start @a");
		executeCommand(server, "execute if score #timer mocap_timer matches 2 run mocap recording start @a");

		// When the interval is reached, stop recording, save, and trigger playback for each loop
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run scoreboard players add #loops mocap_loopcount 1");
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap recording stop");
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap playing stopAll");
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run mocap recording save loop" + getLoopCount(server));

		// Increment the loop count


		// Replay all loops up to the stored loop count
		for (int i = 0; i <= getLoopCount(server); i++) {
			executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run execute as @a[limit=1] at @s run mocap playing start loop" + i);
		}

		// Reset timer
		executeCommand(server, "execute if score #timer mocap_timer matches " + RECORD_INTERVAL_TICKS + ".. run scoreboard players set #timer mocap_timer 0");
	}

	/**
	 * Called every time a player joins the world.
	 */
	private void onPlayerJoin(MinecraftServer server, ServerPlayerEntity player) {
		executeCommand(server, "say " + player.getName().getString() + " joined the world.");
		executeCommand(server, "say EXECUTING COMMANDS ON SERVER AFTER THIS LINE.");

		// Restart the loop
		//if (isLoopActive) {
		stopLoop(server);
		//}
		startLoop(server, player);
	}

	/**
	 * Called every time a player leaves the world.
	 */
	private void onPlayerDisconnect(MinecraftServer server, ServerPlayerEntity player) {

		executeCommand(server, "scoreboard players operation " + player.getName().getString() + " last_tick = #timer mocap_timer");

		// Execute commands right before the player leaves.
		executeCommand(server, "scoreboard players add #loops mocap_loopcount 1");
		executeCommand(server, "mocap recording stop");
		executeCommand(server, "mocap recording save loop" + getLoopCount(server));
		executeCommand(server, "mocap playing stopAll");

		executeCommand(server, "say " + player.getName().getString() + " is leaving the world.");

		// If you want to stop the loop when a player leaves, call stopLoop here.
		// (Be careful in a multiplayer setting—you might not want to stop the loop
		// if other players are still online.)
		//if (isLoopActive) {
		stopLoop(server);
		//}
	}

	/**
	 * Starts the loop and initializes the scoreboards.
	 */
	public void startLoop(MinecraftServer server, ServerPlayerEntity player) {
		isLoopActive = true;

		// Create scoreboards if they don't already exist.
		executeCommand(server, "scoreboard objectives add mocap_timer dummy");
		executeCommand(server, "scoreboard objectives add mocap_loopcount dummy");
		executeCommand(server, "scoreboard objectives add last_tick dummy");
		executeCommand(server, "scoreboard objectives add calc dummy");
		String playerName = player.getName().getString();
		executeCommand(server, "scoreboard players operation #calc calc = " + player.getName().getString() + " last_tick");
		executeCommand(server, "scoreboard players add #calc calc 10");
		executeCommand(server, "scoreboard players add #calc calc 10");
		// Additional code to run upon joining the world.
		executeCommand(server, "say mocap looping started!");
	}

	/**
	 * Stops the loop and performs any necessary cleanup.
	 */
	public void stopLoop(MinecraftServer server) {
		isLoopActive = false;

		// Stop all playback
		executeCommand(server, "mocap playing stopAll");

		// Additional code to run upon leaving the world.
		executeCommand(server, "say mocap looping stopped!");
	}

	/**
	 * Returns the current loop count from the scoreboard.
	 */
	public int getLoopCount(MinecraftServer server) {
		Scoreboard scoreboard = server.getScoreboard();
		ScoreboardObjective objective = scoreboard.getNullableObjective("mocap_loopcount");

		if (objective != null) {
			ReadableScoreboardScore score = scoreboard.getScore(ScoreHolder.fromName("#loops"), objective);
			return (score != null) ? score.getScore() : 0;
		}
		return 0;
	}

	/**
	 * A helper method to execute commands on the server.
	 */
	private void executeCommand(MinecraftServer server, String command) {
		server.getCommandManager().executeWithPrefix(
				server.getCommandSource().withLevel(4).withSilent(), command
		);
	}
}