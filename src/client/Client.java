package client;
import clientGUI.ClientFrame;
import java.net.*;
import java.io.*;

public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader stdIn;
    private String hostname;
    private int port;

    // Constructor to initialize the client
    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    // Method to connect to the server and initialize I/O streams
    public void connect() {
        try {
            System.out.println("Trying to connect to " + hostname + " on port " + port);
            socket = new Socket(hostname, port);
            System.out.println("Connected to the server");

            // Initialize I/O streams
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            stdIn = new BufferedReader(new InputStreamReader(System.in));
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + hostname);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error while connecting to " + hostname);
            System.exit(1);
        }
    }

    // Method to start a listener thread for receiving messages from the server
    public void startListener() {
        Thread listenerThread = Thread.ofVirtual().start(() -> {
            try {
                String serverMessage;
                while ((serverMessage = in.readLine()) != null) {
                    System.out.println("\n[Server]: " + serverMessage);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        });
    }
    public String getMessage() {
        try {
            return in.readLine(); // Returns a message or null if the connection is closed
        } catch (IOException e) {
            System.out.println("Error reading from server: " + e.getMessage());
            return null;
        }
    }

    // Method to send messages to the server
    public void sendMessages(String message) {
        if (out != null) {
            out.println(message);
        }
    }
    public void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (stdIn != null) stdIn.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String hostname = "localhost";
        int port = 9999;

        // If hostname and port are provided as arguments, use them

        Client client = new Client(hostname, port);
        new ClientFrame(client);
        client.connect();
        client.startListener();

        // end the job
        client.closeResources();
    }
}