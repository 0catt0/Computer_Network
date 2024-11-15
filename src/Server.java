// Server.java
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 1234;
    private static final int MAX_THREADS = 10;
    private static final List<String[]> QUESTIONS = Arrays.asList(
            new String[]{"What is the capital of France?", "Paris"},
            new String[]{"What is 5 + 7?", "12"},
            new String[]{"Solve for x: 3x + 12 = 24", "4"},
            new String[]{"Who wrote 'Hamlet'?", "Shakespeare"}
    );

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    private static final ConcurrentHashMap<Socket, Integer> clientScores = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            System.out.println("Maximum concurrent clients: " + MAX_THREADS);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    clientScores.put(clientSocket, 0);
                    threadPool.execute(new ClientHandler(clientSocket));
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + PORT);
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;
        private final String clientId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                System.out.println("Starting quiz for client: " + clientId);

                // Send welcome message
                out.println("Welcome to the Quiz Game! You are player: " + clientId);
                out.println("There are " + QUESTIONS.size() + " questions in total.");

                int questionNumber = 1;
                for (String[] qa : QUESTIONS) {
                    out.println("Question " + questionNumber + "/" + QUESTIONS.size() + ": " + qa[0]);

                    String response = in.readLine();
                    if (response == null) {
                        break; // Client disconnected
                    }

                    if (response.equalsIgnoreCase(qa[1])) {
                        out.println("Correct!");
                        clientScores.computeIfPresent(socket, (k, v) -> v + 1);
                    } else {
                        out.println("Incorrect! The correct answer is: " + qa[1]);
                    }

                    questionNumber++;
                }

                // Send final score
                Integer finalScore = clientScores.get(socket);
                out.println("Quiz Over! Your final score is: " + finalScore + "/" + QUESTIONS.size());

                // Show current leaderboard
                out.println("\nCurrent Active Players Scores:");
                clientScores.forEach((s, score) -> {
                    String playerId = s.getInetAddress().getHostAddress() + ":" + s.getPort();
                    out.println(playerId + ": " + score + "/" + QUESTIONS.size());
                });

            } catch (IOException e) {
                System.err.println("Error handling client " + clientId + ": " + e.getMessage());
            } finally {
                try {
                    clientScores.remove(socket);
                    socket.close();
                    System.out.println("Client disconnected: " + clientId);
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
}