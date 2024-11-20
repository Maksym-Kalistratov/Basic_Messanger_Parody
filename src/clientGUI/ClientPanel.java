package clientGUI;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ClientPanel extends JPanel {

    private final JTextArea chat;
    private final JTextField inputField;
    private final JButton sendButton;
   private final ClientFrame base;

    public ClientPanel(ClientFrame base) {
        this.base = base;
        BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(box);
        setBackground(Color.WHITE);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        // Поле для вывода текста с прокруткой
        chat = new JTextArea();
        chat.setEditable(false);
        chat.setBackground(Color.LIGHT_GRAY);
        chat.setFont(new Font("Arial", Font.PLAIN, 14));
        chat.setAlignmentX(Component.CENTER_ALIGNMENT);
        JScrollPane scrollPane = new JScrollPane(chat);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.setAlignmentX(Component.CENTER_ALIGNMENT);

        sendButton = createButton("Send", e -> {});
        sendButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(scrollPane);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(inputField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(sendButton);

    }
    private JButton createButton(String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        button.setPreferredSize(new Dimension(100, 30));
        button.setMaximumSize(new Dimension(100, 30));
        button.setBackground(Color.LIGHT_GRAY);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        return button;
    }
    public void appendToChat(String text) {
        SwingUtilities.invokeLater(() -> chat.append(text + "\n"));
    }
}

