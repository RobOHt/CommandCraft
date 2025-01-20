package net.robin.commandcraft.LLM;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LLM {
    private static final String SERVER_URL = "http://localhost:5000/process";

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
}