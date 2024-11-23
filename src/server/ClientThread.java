package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientThread extends Thread {
    private static final List<ClientThread> clientsList = new ArrayList<>();
    private Socket clientSocket;
    private String clientAddres;
    private String clientName;
    private BufferedReader in;
    private PrintWriter out;
    private String[] addressees = {"+"};
    private final Server baza;
    private Pattern wordsChecker;
    public ClientThread(Socket socket, Server baza) {
        this.clientSocket = socket;
        this.clientAddres = clientSocket.getInetAddress().toString() + ":" + clientSocket.getPort();
        try {
            // Initialize I/O streams
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error initializing I/O streams for " + clientAddres + ": " + e.getMessage());
        }
        synchronized (clientsList) {
            clientsList.add(this);
        }
        this.baza = baza;
        synchronized (baza) {
            this.wordsChecker = Pattern.compile(
                    "\\b(" + String.join("|", baza.getBannedWords()) + ")\\b",
                    Pattern.CASE_INSENSITIVE
            );
        }
    }

    @Override
    public void run() {
        try {
            synchronized (baza) {
                out.println("Banned words: " + baza.getBannedWords());
            }
            setClientName();
            out.println("Instructions:\n" +
                    "Write '->' And the list of names to send message only to certain users\n" +
                    "Example: ->Ben,John,Michael So Ben,John and Michael will receive the message\n" +
                    "Write '!->' And the list of names to exclude users that don't need to see your messages\n" +
                    "Example: !->John,Michael So John and Michael won't receive the message\n" +
                    "Write '->all' to reset and send messages to all users.");
            sendUsersList();
            System.out.println("Client " + clientName + " connected from addres " + clientAddres);
            // Read messages from the client and send them back
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received from " + clientName + ": " + line);
                if (!checkBannedWords(line)) {
                    if (line.trim().equalsIgnoreCase("->all")) {
                        addressees = new String[]{"+"};
                        out.println("Message will now be sent to all users.");
                        continue;
                    } else if (line.startsWith("->")) {
                        updateAddressees("+", line.substring(2).trim());
                        continue;
                    } else if (line.startsWith("!->")) {
                        updateAddressees("-", line.substring(3).trim());
                        continue;
                    }
                    sendMessage(line);
                    System.out.println("Sent");
                } else{
                    out.println("Your message contains banned words");
                }
            }

        } catch (IOException e) {
            System.out.println("Error handling client " + clientName + ": " + e.getMessage());
        } finally { //closing the connection
            synchronized (clientsList) {
                clientsList.remove(this);
            }
            try {
                // Close streams and socket
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null) clientSocket.close();
                System.out.println("Client " + clientName + " disconnected");
            } catch (IOException e) {
                System.out.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    // Sends a message to the recipients chosen by the client
    private void sendMessage(String message) {
        synchronized (clientsList) {
            for (ClientThread client : clientsList) {
                if (!client.clientName.equals(this.clientName) && shouldSend(client)) {
                    client.out.println("From " + clientName + ": " + message);
                }
            }
        }
    }
    // Checks if the message should be sent to a specific client
    private boolean shouldSend(ClientThread client){
        if(addressees.length == 1) return true;
        else{
            if (addressees[0].equals("+")) {
                for (int i = 1; i < addressees.length; i++) {
                    if (client.clientName.equals(addressees[i])) {
                        return true;
                    }
                }
                return false;
            } else if (addressees[0].equals("-")) {
                for (int i = 1; i < addressees.length; i++) {
                    if (client.clientName.equals(addressees[i])) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
    // Asks the client to set a unique name
    private void setClientName() throws IOException {
        while (true) {
            out.println("Enter your unique nickname:");
            String name = in.readLine();
            if (!checkBannedWords(name)) {
                if (name == null || name.equals("")) {
                    out.println("Nickname cannot be empty. Try again.");
                    continue;
                }

                synchronized (clientsList) {
                    if (isNameTaken(name)) {
                        out.println("Nickname '" + name + "' is already taken. Please choose another one.");
                    } else {
                        this.clientName = name;
                        out.println("Welcome, " + clientName + "!");
                        break;
                    }
                }
            } else{
                out.println("Your message contains banned words\n");
            }
        }
    }
    // Checks if the chosen name is already in use
    private boolean isNameTaken(String name) {
        for (ClientThread client : clientsList) {
            if (client.clientName != null && client.clientName.equals(name)) {
                return true;
            }
        }
        return false;
    }
    // Updates the list of recipients based on the client's input
    private void updateAddressees(String mode, String names) {
        String[] nameList = names.split(",");
        List<String> existingNames = new ArrayList<>();

        synchronized (clientsList) {
            for (String name : nameList) {
                if (isNameTaken(name.trim())) {
                    existingNames.add(name.trim());
                } else {
                    out.println("User '" + name.trim() + "' not found. Skipping.");
                }
            }
        }

        if (existingNames.isEmpty()) {
            out.println("No existing names found.");
        } else {
            addressees = new String[existingNames.size() + 1];
            addressees[0] = mode;
            for (int i = 0; i < existingNames.size(); i++) {
                addressees[i + 1] = existingNames.get(i);
            }
            if (mode.equals("+")) {
                out.println("Recipients set to: " + String.join(", ", existingNames));
            } else if (mode.equals("-")) {
                out.println("Recipients set to all excluding: " + String.join(", ", existingNames));
            }
        }
    }
    // Checks if the given message contains banned words
    private boolean checkBannedWords(String message){
        Matcher matcher = wordsChecker.matcher(message);
        return matcher.find();
    }
    // Sends the list of currently connected users to the client
    private void sendUsersList(){
        String answer = "Clients online: ";
        synchronized (clientsList) {
            for (int i = 0; i < clientsList.size(); i++) {
                answer += clientsList.get(i).clientName + "; ";
            }
        }
        out.println(answer);
    }
}
