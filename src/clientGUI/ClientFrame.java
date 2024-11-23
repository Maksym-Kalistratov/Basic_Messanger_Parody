package clientGUI;
import client.Client;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ClientFrame extends JFrame{
    private final Client client;
    private final ClientPanel chatPanel;
    public ClientFrame(Client client) {
        this.client = client;

        setTitle("Client Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        chatPanel = new ClientPanel(this);
        add(chatPanel);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Client getClient() {
        return client;
    }

    public ClientPanel getChatPanel() {
        return chatPanel;
    }

}

