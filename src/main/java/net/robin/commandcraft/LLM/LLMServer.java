package net.robin.commandcraft.LLM;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.ServerSocket;
import java.net.SocketException;

public class LLMServer {
    private static final String RESOURCE_FOLDER = "/assets/LLM-API/dist/"; // Folder in resources
    private static final String EXECUTABLE_NAME = "main";      // Base name of the executable
    private static final String EXECUTABLE_EXTENSION_WINDOWS = ".exe"; // Extension for Windows
    private static final String TEMP_DIR_PREFIX = "llm-api-";  // Prefix for temporary directory
    private static final int PORT = 5000;

    private Process process;
    private Path tempDir;
    private ExecutorService logExecutor;

    /**
     * Starts a Flask server.
     */
    public void start() {
        // Check if LLM server port is already in use
        if (isPortInUse(PORT)) {
            System.err.println("LLM server is already in use, likely cause by force quitting minecraft. Should work anyways though.");
            return; // Exit the method without starting the server
        }

        try {
            // Create a temporary directory to extract the executable
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            System.out.println("Temporary directory created: " + tempDir);

            // Determine the executable name based on the OS
            String executableName = getExecutableName();
            Path executablePath = tempDir.resolve(executableName);

            // Extract the executable from resources
            extractResource(RESOURCE_FOLDER + executableName, executablePath);

            // Make the executable file executable (Linux/Mac)
            if (!isWindows()) {
                executablePath.toFile().setExecutable(true);
            }

            // Start the Flask server
            ProcessBuilder pb = new ProcessBuilder(executablePath.toString());
            pb.directory(tempDir.toFile());
            pb.redirectErrorStream(true); // Redirect error stream to standard output
            process = pb.start();

            // Log server output asynchronously
            logExecutor = Executors.newSingleThreadExecutor();
            logExecutor.submit(this::logProcessOutput);

            System.out.println("Flask server started.");
        } catch (Exception e) {
            System.err.println("Failed to start Flask server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if a specific port is already in use.
     * @param port The port to check.
     * @return true if the port is in use, false otherwise.
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // If the port is available, close the socket and return false
            serverSocket.close();
            return false;
        } catch (SocketException e) {
            // Port is already in use
            return true;
        } catch (Exception e) {
            // Other exceptions (e.g., permission denied)
            System.err.println("Error checking port " + port + ": " + e.getMessage());
            return true; // Assume the port is in use to avoid conflicts
        }
    }

    /**
     * Stops a Flask server.
     */
    public void stop() {
        if (process != null) {
            process.destroy();
            System.out.println("Flask server stopped.");
        }

        // Clean up the temporary directory
        if (tempDir != null) {
            try {
                Files.walk(tempDir)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (Exception e) {
                                System.err.println("Failed to delete file: " + path);
                                e.printStackTrace();
                            }
                        });
                System.out.println("Temporary directory deleted: " + tempDir);
            } catch (Exception e) {
                System.err.println("Failed to clean up temporary directory: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Shutdown the log executor
        if (logExecutor != null) {
            logExecutor.shutdown();
        }
    }

    /**
     * Checks if the Flask server is running.
     */
    public boolean isRunning() {
        return (process != null && process.isAlive()) || // LLM server started normally and is active.
                isPortInUse(PORT); // LLM server didn't start normally but is active anyways.
    }

    /**
     * Extracts a resource file to the specified path.
     */
    private void extractResource(String resourcePath, Path targetPath) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Resource not found: " + resourcePath);
            }
            Files.createDirectories(targetPath.getParent());
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Extracted resource: " + resourcePath + " to " + targetPath);
        }
    }

    /**
     * Logs the output of the Flask server process.
     */
    private void logProcessOutput() {
        try (InputStream inputStream = process.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                System.out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            System.err.println("Failed to log process output: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the executable name based on the operating system.
     * <p>
     *     Returns EXECUTABLE_NAME for Linux/Mac, and EXECUTABLE_NAME.exe for Windows.
     * </p>
     */
    private String getExecutableName() {
        return isWindows() ? EXECUTABLE_NAME + EXECUTABLE_EXTENSION_WINDOWS : EXECUTABLE_NAME;
    }

    /**
     * Checks if the current OS is Windows.
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}