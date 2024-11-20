import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Server {
    private ServerSocket serverSocket;
    private String serverName;
    private int serverPort;
    private Set<String> bannedWords = new HashSet<>();
    private String filePath;
    BufferedReader reader;
    public Server(String name, int port) {
        this.serverPort = port;
        this.serverName = name;
    }
    public Server(String filePath){
        this.filePath = filePath;
        try {
            reader = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        readConfig();
    }
    public void start() {
        try {
            System.out.println("Attempting to create a server socket...");
            serverSocket = new ServerSocket(serverPort);
            System.out.println("Server is running on port " + serverPort);
        } catch (IOException e) {
            System.out.println("Failed to create server socket: " + e.getMessage());
            System.exit(1);
        }

        while (true) {
            try {
                System.out.println("Waiting for a client to connect...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("A client has connected from " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());

                ClientThread clientThread = new ClientThread(clientSocket, this);
                clientThread.start();

            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
                break;
            }
        }
        this.stop();
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server stopped.");
            }
        } catch (IOException e) {
            System.out.println("Error closing server socket: " + e.getMessage());
        }
    }

    private void readConfig() {
        try {
            if(serverName == null) serverName = reader.readLine();
            else reader.readLine();
            if(serverPort == 0) serverPort = Integer.parseInt(reader.readLine());
            else reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                for (String word : line.split(",")) {
                    bannedWords.add(word.trim().toLowerCase());
                }
            }
            System.out.println("Configuration loaded:");
            System.out.println("Server Name: " + serverName);
            System.out.println("Port: " + serverPort);
            System.out.println("Banned Words: " + bannedWords);

        } catch (IOException e) {
            System.out.println("Error loading configuration: " + e.getMessage());
            System.exit(1);
        }
    }
    public Set<String> getBannedWords(){
        return bannedWords;
    }
    public static void main(String[] args) {
        int port = 9999;
        Server server = new Server("src/Config.txt");
        server.start();

    }
}
