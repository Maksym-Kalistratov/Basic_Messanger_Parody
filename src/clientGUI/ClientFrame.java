package clientGUI;
import client.Client;
import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ClientFrame extends JFrame implements KeyListener {
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

        addKeyListener(this);
        setLocationRelativeTo(null);
        setFocusable(true);
    }

    public Client getClient() {
        return client;
    }

    public ClientPanel getChatPanel() {
        return chatPanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setFocusable(false);
            System.exit(0);
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            setFocusable(false);
            System.exit(0);
        }
    }
    @Override
    public void keyReleased(KeyEvent e) {

    }

}

