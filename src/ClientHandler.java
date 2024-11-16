import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    private static final List<ClientHandler> clientsList = new ArrayList<>();
    private Socket clientSocket;
    private String clientName;
    private BufferedReader in;
    private PrintWriter out;

    // Constructor that accepts the client socket
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.clientName = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
        try {
            // Initialize I/O streams in the constructor
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error initializing I/O streams for " + clientName + ": " + e.getMessage());
        }
        synchronized (clientsList) {
            clientsList.add(this);
        }
    }

    // Main method executed in the thread
    @Override
    public void run() {
        try {
            System.out.println("Client " + clientName + " connected");

            // Read messages from the client and echo them back
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received from " + clientName + ": " + line);
                //out.println(line);
                sendMessage(line);
                System.out.println("Sent");
            }

            System.out.println("Client " + clientName + " disconnected");

        } catch (IOException e) {
            System.out.println("Error handling client " + clientName + ": " + e.getMessage());
        } finally {
            synchronized (clientsList) {
                clientsList.remove(this);
            }
            try {

                // Close streams and socket
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void sendMessage(String message) {
        synchronized (clientsList) {
            for (ClientHandler client : clientsList) {
                if (!client.clientName.equals(this.clientName)) {
                    client.out.println("From " + clientName + ": " + message);
                }
            }
        }
    }
}
