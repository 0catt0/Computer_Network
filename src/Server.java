import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    private static final List<String[]> QUESTIONS = Arrays.asList(
            new String[]{"What is the capital of France?", "Paris"},
            new String[]{"What is 5 + 7?", "12"},
            new String[]{"Solve for x: 3x + 12 = 24", "4"},
            new String[]{"Who wrote 'Hamlet'?", "Shakespeare"}
    );

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                int score = 0;

                for (String[] qa : QUESTIONS) {
                    out.println(qa[0]); // Send question
                    String response = in.readLine();

                    if (response != null && response.equalsIgnoreCase(qa[1])) {
                        out.println("Correct!");
                        score++;
                    } else {
                        out.println("Incorrect! The correct answer is " + qa[1] + "");
                    }
                }
                out.println("Quiz Over! Your final score is: " + score);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
