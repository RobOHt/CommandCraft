package net.robin.commandcraft.LLM;

import com.google.gson.JsonObject;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;

public class LLM {
    private static final String SERVER_URL = "http://localhost:5000/chat";

    private final LLMServer llmServer;

    /**
     * The LLM object is a large language model that you can communicate with.
     * @param llmServer The server object hosting a local LLM API.
     */
    public LLM(LLMServer llmServer) {
        this.llmServer = llmServer;
    }

    /**
     * Sends an input to the LLM server and returns the response.
     * @param prompt Your LLM prompt.
     */
    public String ask(String prompt) {
        if (!llmServer.isRunning()) {
            throw new IllegalStateException("LLM server is not running.");
        }

        try {
            // Create the HTTP connection
            URL url = new URL(SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send the input text as JSON
            String jsonInput = "{\"input\": \"" + prompt + "\"}";
            try (OutputStream os = conn.getOutputStream()) {
                byte[] inputBytes = jsonInput.getBytes(StandardCharsets.UTF_8);
                os.write(inputBytes, 0, inputBytes.length);
            }

            // Read the response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process prompt: " + e.getMessage(), e);
        }
    }

    /**
     * Sends a prompt to the LLM server and streams the response.
     * @param prompt Your LLM prompt.
     * @param callback A function to process each received chunk of data. E.g. <code>System.out::println()</code>
     */
    public void ask(String prompt, String playerID, String villagerID, Consumer<String> callback) {
        if (!llmServer.isRunning()) {
            throw new IllegalStateException("LLM server is not running.");
        }
        try {
            // Create the HTTP connection
            URL url = new URL(SERVER_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Send the input text as JSON
            JsonObject json = new JsonObject();
            json.addProperty("input", prompt);
            json.addProperty("player", playerID);
            json.addProperty("villager", villagerID);
            String jsonInput = json.toString();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInput.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            // Read and stream response character-by-character
            try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)) {
                char[] buffer = new char[16]; // Buffer size for reading chunks
                int charsRead;
                while ((charsRead = isr.read(buffer)) != -1) {
                    String chunk = new String(buffer, 0, charsRead); // Convert buffer to string
                    callback.accept(chunk); // Process each chunk in real-time
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to process prompt: " + e.getMessage(), e);
        }
    }

    /**
     * Asks the LLM a question and stream results on Minecraft's action bar.
     * @param prompt a message to the LLM
     * @param player The player entity to whom the response will be sent.
     */
    public void askAtActionBar(String prompt, PlayerEntity player, VillagerEntity villager, MinecraftServer server) {
        // Run the entire ask process asynchronously
        server.execute(() -> {
            StringBuilder buffer = new StringBuilder();
            int maxLength = 70;  // Max amount of char to display on the action bar. Excess characters are truncated FIFO.

            // Start the whole LLM request thing in a background thread
            new Thread(() -> {
                // Play a villager trade sound so mimic talking
                villager.playSound(SoundEvents.ENTITY_VILLAGER_TRADE, 2.0F, villager.getSoundPitch());

                // Send an LLM request and pass in a function chunk -> {} which gets called for each chunk streamed
                this.ask(prompt, player.getUuidAsString(), villager.getUuidAsString(), chunk -> {
                    // Process chunks in the background
                    buffer.append(chunk);
                    if (buffer.length() > maxLength) {
                        buffer.delete(0, buffer.length() - maxLength);
                    }
                    // Send buffered LLM response to Minecraft's action bar (on the main thread)
                    player.sendMessage(Text.literal(buffer.toString()).styled(style -> style.withItalic(false)), true);
                });

            }).start();
        });
    }
}