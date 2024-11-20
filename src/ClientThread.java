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
                    "Example: ->Ben,John,Michael; So Ben,John and Michael will receive the message\n" +
                    "Write '!->' And the list of names to exclude users that don't need to see your messages\n" +
                    "Example: !->John,Michael; So John and Michael won't receive the message\n");
            sendUsersList();
            System.out.println("Client " + clientName + " connected from addres " + clientAddres);
            System.out.println(checkBannedWords("My bomb is ready"));
            // Read messages from the client and send them back
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("Received from " + clientName + ": " + line);
                if (!checkBannedWords(line)) {
                    if (line.startsWith("->")) {
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
            for (ClientThread client : clientsList) {
                if (!client.clientName.equals(this.clientName) && shouldSend(client)) {
                    client.out.println("From " + clientName + ": " + message);
                }
            }
        }
    }
    private boolean shouldSend(ClientThread client){
        if(addressees.length == 1) return true;
        else{
            if (addressees[0].equals("+")) {
                for (int i = 1; i < addressees.length; i++) {
                    if (client.clientName.equals(addressees[i])) {
                        return true;
                    }
                }
            } else if (addressees[0].equals("-")) {
                for (int i = 1; i < addressees.length; i++) {
                    if (client.clientName.equals(addressees[i])) {
                        return false;
                    }else return true;
                }
            }
        }
        return false;
    }
    // Method to set uniqe nickname for client
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
                out.println("Your message contains banned words");
            }
        }
    }
    // Method that checks if name received from user is unique
    private boolean isNameTaken(String name) {
        for (ClientThread client : clientsList) {
            if (client.clientName != null && client.clientName.equals(name)) {
                return true;
            }
        }
        return false;
    }
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
            out.println("Recipients set to: " + String.join(", ", existingNames));
        }
    }
    private boolean checkBannedWords(String message){
        Matcher matcher = wordsChecker.matcher(message);
        return matcher.find();
    }
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
