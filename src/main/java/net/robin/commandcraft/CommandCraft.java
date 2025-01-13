package net.robin.commandcraft;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.passive.VillagerEntity;
import net.robin.commandcraft.commands.VillagerCommand;
import net.robin.commandcraft.mixin.ActivityAccessor;
import net.robin.commandcraft.villagertask.ConversationTask;
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

	/**
	 * Creates a set of tasks, aka an activity, for conversation.
	 * @return a set of tasks
	 */
	public static ImmutableList<Pair<Integer, ? extends Task<? super VillagerEntity>>> createConversationTasks() {
		return ImmutableList.of(
				Pair.of(1, new ConversationTask())
		);
	}

	/**
	 * Register activity CONVERSATION
	 */
	public static final Activity CONVERSATION = ActivityAccessor.invokeRegister("conversation");
}