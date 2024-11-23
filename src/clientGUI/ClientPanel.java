package clientGUI;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ClientPanel extends JPanel {

    private final JTextPane chat; // Replaced JTextArea with JTextPane for styling
    private final JTextField inputField;
    private final JButton sendButton;
    private final ClientFrame base;

    public ClientPanel(ClientFrame base) {
        this.base = base;
        BoxLayout box = new BoxLayout(this, BoxLayout.Y_AXIS);
        setLayout(box);
        setBackground(Color.WHITE);
        setAlignmentX(Component.CENTER_ALIGNMENT);

        chat = new JTextPane();
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
        inputField.addActionListener(e -> sendMessage());

        sendButton = createButton("Send", e -> {
            sendMessage();
        });
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

    public void sendMessage(){
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            base.getClient().sendMessage(message);
            inputField.setText("");
            appendToChat("[You]: " + message);
        }
    }

    // Appends text to the chat pane with coloring based on ending
    public void appendToChat(String text) {
        StyledDocument doc = chat.getStyledDocument();
        Style style;
        if (text.startsWith("[You]:")) {
            style = chat.addStyle("You", null);
            StyleConstants.setForeground(style, Color.BLUE);
        } else if (text.startsWith("[Server]: From")) {
            style = chat.addStyle("ServerFrom", null);
            StyleConstants.setForeground(style, Color.GREEN);
        } else if (text.startsWith("[Server]:")) {
            style = chat.addStyle("Server", null);
            StyleConstants.setForeground(style, Color.RED);
        } else {
            style = chat.getStyle("default");
        }

        try {
            doc.insertString(doc.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

}
