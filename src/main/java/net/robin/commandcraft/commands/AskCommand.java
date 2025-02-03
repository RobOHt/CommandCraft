package net.robin.commandcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.robin.commandcraft.CommandCraftClient;
import net.robin.commandcraft.LLM.LLM;


public class AskCommand {
    /**
     * Register the /ask command.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ask")
                .then(CommandManager.argument("prompt", StringArgumentType.greedyString())
                        .executes(context -> {
                            // Get the player who issued the command
                            ServerPlayerEntity player = context.getSource().getPlayer();

                            // Get the prompt from the command arguments
                            String prompt = StringArgumentType.getString(context, "prompt");

                            // Stream responses
                            MinecraftServer server = context.getSource().getServer();
                            LLM llm = CommandCraftClient.getInstance().getLLM();
                            llm.askAtActionBar(prompt, player, server);
                            player.sendMessage(Text.literal("Thinking..."), true);
                            return 1;
                        })
                )
        );
    }
}