package net.robin.commandcraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.robin.commandcraft.commands.VillagerCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCraft implements ModInitializer {
	public static final String MOD_ID = "commandcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * Things to do as soon as minecraft loads.
	 */
	@Override
	public void onInitialize() {
		// Register custom commands
		registerVillagerCommands();
	}

	/**
	 * Registers a set of commands that forces villagers do certain things.
	 */
	public static void registerVillagerCommands() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			VillagerCommand.register(dispatcher);
		});
	}
}