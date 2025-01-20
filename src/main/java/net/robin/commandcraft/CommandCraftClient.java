package net.robin.commandcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.robin.commandcraft.LLM.LLM;
import net.robin.commandcraft.LLM.LLMServer;

public class CommandCraftClient implements ClientModInitializer {
    private static CommandCraftClient INSTANCE; // Singleton instance

    private LLMServer llmServer;
    private LLM llm;

    @Override
    public void onInitializeClient() {
        INSTANCE = this; // Set the singleton instance

        // Initialize the Flask server when the client starts
        llmServer = new LLMServer();
        llmServer.start();
        llm = new LLM(llmServer);

        // Register a shutdown hook to stop the Flask server when the client exits
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (llmServer != null) {
                llmServer.stop();
            }
        });
    }

    /**
     * Stops the LLM server if it is running. Hopefully makes sure LLMServer is terminated when minecraft is forcibly
     * killed.
     */
    private void stopLLMServer() {
        if (llmServer != null) {
            llmServer.stop();
            llmServer = null; // Prevent duplicate stops
        }
    }

    /**
     * Returns the singleton instance of CommandCraftClient.
     */
    public static CommandCraftClient getInstance() {
        return INSTANCE;
    }

    /**
     * The mod runs an LLM server on client side. This method returns the LLM server object.
     * @return an LLM server object.
     */
    public LLMServer getLLMServer() {
        return llmServer;
    }

    /**
     * The mod runs an LLM object that interacts with the client-side LLM server. This method returns it.
     * @return an LLM object.
     */
    public LLM getLLM() {
        return llm;
    }
}