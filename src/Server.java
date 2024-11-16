import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        // Server port (can be changed if needed)
        final int serverPort = 9999;

        // Create the server socket
        try {
            System.out.println("Attempting to create a server socket...");
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is running on port " + serverPort);
        } catch (IOException e) {
            System.out.println("Failed to create server socket: " + e.getMessage());
            System.exit(1);
        }

        // Waiting for clients to connect
        while (true) {
            try {
                System.out.println("Waiting for a client to connect...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("A client has connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                // Create a new thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clientHandler.start(); // Start the thread

            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
        }
    }
}
