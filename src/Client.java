import java.io.*;
import java.net.*;

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

        try (Socket socket = new Socket(serverAddress, serverPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connected to the server. Starting quiz...");
            String question;

            while ((question = in.readLine()) != null) {
                if (question.startsWith("Quiz Over!")) {
                    System.out.println(question);
                    break;
                }
                System.out.println(question);
                String answer = userInput.readLine();
                out.println(answer);

                String feedback = in.readLine();
                System.out.println(feedback);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
