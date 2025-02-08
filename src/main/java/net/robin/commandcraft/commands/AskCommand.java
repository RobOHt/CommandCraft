package net.robin.commandcraft.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.server.MinecraftServer;
import net.robin.commandcraft.CommandCraftClient;
import net.robin.commandcraft.LLM.LLM;
import net.robin.commandcraft.utils.VillagerUtils;

import java.util.Optional;


public class AskCommand {
    /**
     * Register the /ask command.
     */
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ask")
                .then(CommandManager.argument("prompt", StringArgumentType.greedyString())
                        .executes(context -> {
                            // Determine the two parties involved in this conversation
                            ServerPlayerEntity player = context.getSource().getPlayer();  // Get the player who issued the command
                            Optional<VillagerEntity> villager = VillagerUtils.getNearestVillager(30);

                            // Begin conversation if a villager is nearby
                            if (villager.isPresent()) {
                                // Get the prompt from the command arguments
                                String prompt = StringArgumentType.getString(context, "prompt");

                                // Start ConversationTask for villager
                                boolean villagerAcceptsConversation = VillagerUtils.startConversation(villager.get(), player);

                                // Stream responses
                                if (villagerAcceptsConversation) {
                                    MinecraftServer server = context.getSource().getServer();
                                    LLM llm = CommandCraftClient.getInstance().getLLM();
                                    llm.askAtActionBar(prompt, player, villager.get(), server);
                                    player.sendMessage(Text.literal("Thinking..."), true);
                                }
                            }
                            return 1;
                        })
                )
        );
    }
}