import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class Client {
    private static final String CONFIG_FILE = "server_info.dat";

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int serverPort = 1234;

        // Load server configuration from file
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
                serverAddress = br.readLine();
                serverPort = Integer.parseInt(br.readLine());
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error reading config file. Using default values.");
            }
        }

        try (
                Socket socket = new Socket(serverAddress, serverPort);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))
        ) {
            // Create separate thread for receiving server messages
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<?> receiverFuture = executor.submit(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        if (serverMessage.startsWith("Question")) {
                            System.out.println("\n" + serverMessage);
                            System.out.print("Your answer: ");
                        } else {
                            System.out.println(serverMessage);
                        }
                    }
                } catch (IOException e) {
                    if (!socket.isClosed()) {
                        System.err.println("Error receiving message from server: " + e.getMessage());
                    }
                }
            });

            // Main thread handles user input
            try {
                String userAnswer;
                while ((userAnswer = userInput.readLine()) != null) {
                    out.println(userAnswer);
                }
            } catch (IOException e) {
                System.err.println("Error sending message to server: " + e.getMessage());
            }

            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}