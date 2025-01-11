package feral;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import jade.core.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class ChatClientAgent extends Agent {

    private String username;
    private JFrame frame;

    private JTextPane statusPane;
    private JButton refreshButton;

    @Override
    protected void setup() {
        // Set up Swing GUI to get username
        setupGUI();

        // Add behavior to listen for incoming messages
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String sender = msg.getSender().getLocalName();
                    String content = msg.getContent();

                    // Ensure Swing updates are done on the EDT
                    SwingUtilities.invokeLater(() -> {
                        // Append the colored message to the statusPane
                        appendColoredText(statusPane, "Message from " + sender + ": ", Color.BLUE, true);
                        appendColoredText(statusPane, content + "\n", Color.BLACK, false);
                    });
                } else {
                    block(); // Block the agent to wait for the next message
                }
            }
        });
    }

    private void setupGUI() {
        frame = new JFrame("Chat Client");
        frame.setSize(1600, 900);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        frame.setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 1));
        JLabel label = new JLabel("Enter Username:");
        JTextField usernameField = new JTextField();
        JButton registerButton = new JButton("Login");
        inputPanel.add(label);
        inputPanel.add(usernameField);

        // Add components to frame
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(registerButton, BorderLayout.CENTER);
        frame.add(new JScrollPane(statusPane), BorderLayout.SOUTH);
        
        // Status Pane with styled document
        statusPane = new JTextPane();
        statusPane.setEditable(false);

        registerButton.addActionListener(e -> {
            username = usernameField.getText();
            if (username != null && !username.isEmpty()) {
                registerToDF();
                showChatInterface();
            } else {
                appendColoredText(statusPane, "Please enter a username.\n", Color.RED, false);
            }
        });

        frame.setVisible(true);
    }

    private void registerToDF() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());

            ServiceDescription sd = new ServiceDescription();
            sd.setType("ChatClient");
            sd.setName(username);
            dfd.addServices(sd);

            DFService.register(this, dfd);

            appendColoredText(statusPane, "Client " + username + " registered to DF.\n", Color.GREEN, false);

        } catch (FIPAException e) {
            e.printStackTrace();
            appendColoredText(statusPane, "Error registering client to DF.\n", Color.RED, false);
        }
    }

    private void showChatInterface() {
        // Clear the frame for the new interface
        frame.getContentPane().removeAll();
        frame.repaint();
        frame.setTitle(username);
        // Create the chat components
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        JList<String> clientsList = new JList<>();
        refreshButton = new JButton("<html><b><font color='green'>Refresh</font></b></html>"); // Refresh button

        // Set component sizes
        statusPane.setPreferredSize(new Dimension(1200, 200)); // Status pane size
        messageField.setPreferredSize(new Dimension(1150, 60)); // Message input field size
        sendButton.setPreferredSize(new Dimension(100, 30)); // Send button size
        refreshButton.setPreferredSize(new Dimension(100, 60)); // Refresh button size

        // Set up the left panel with the client list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER); // Add list to the left side
        leftPanel.add(refreshButton, BorderLayout.SOUTH);
        
        leftPanel.setPreferredSize(new Dimension(300, 800)); // Client list size
        
        // Set up the message panel with message field and send button
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.WEST);
        messagePanel.add(sendButton, BorderLayout.EAST); // Send button on the right side

        // Set up the right panel with status pane and message panel
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(1300, 900)); // Set size of the right panel

        // Set up status pane and message panel inside right panel
        statusPane.setEditable(false);

        rightPanel.add(statusPane, BorderLayout.CENTER); // Add status pane at the top
        rightPanel.add(messagePanel, BorderLayout.SOUTH); // Add message panel at the bottom

        // Add components to chat panel
        frame.setLayout(new BorderLayout());
        frame.add(leftPanel, BorderLayout.WEST); // Add client list on the left
        frame.add(rightPanel, BorderLayout.CENTER); // Add right panel in the center

        
        // Refresh button action listener
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshClientList(clientsList);
            }
        });

        // Send button action listener
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = messageField.getText();
                String recipient = clientsList.getSelectedValue();
                if (message != null && !message.isEmpty() && recipient != null) {
                    sendMessageToClient(recipient, message);
                    messageField.setText(""); // Clear the text field
                } else {
                	appendColoredText(statusPane, "Cannot send an empty message or no recipient selected.\n", Color.BLUE, false);
                }
            }
        });

        // Refresh the frame
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
        
        // Initial refresh to populate the client list
        // refreshClientList(clientsList);
    }


    private void refreshClientList(JList<String> clientsList) {
        refreshButton.setText("<html><b><font color='red'>Refresh</font></b></html>");
        refreshButton.setEnabled(false); // Disable the button during the operation

        new Thread(() -> {
            try {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("ChatClient");
                template.addServices(sd);

                DFAgentDescription[] results = DFService.search(this, template);

                SwingUtilities.invokeLater(() -> {
                    Vector<String> clientNames = new Vector<>();
                    for (DFAgentDescription result : results) {
                        if (!result.getName().equals(getAID())) {
                            clientNames.add(result.getName().getLocalName());
                        }
                    }
                    clientsList.setListData(clientNames); // Update the list with the new clients
                    appendColoredText(statusPane, "Client list refreshed. Found " + clientNames.size() + " clients.\n", Color.BLUE, true);
                });
            } catch (FIPAException e) {
                SwingUtilities.invokeLater(() -> 
                appendColoredText(statusPane, "Error: Unable to refresh client list.\n", Color.RED, true)
                );
            } finally {
            	refreshButton.setEnabled(true); // Re-enable the button after the operation
                refreshButton.setText("<html><b><font color='green'>Refresh</font></b></html>");
            }
        }).start();
    }

    private void sendMessageToClient(String recipientName, String message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(recipientName, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);
        appendColoredText(statusPane, "Message sent to " + recipientName + ": ", Color.CYAN, true);
        appendColoredText(statusPane, message + "\n", Color.BLACK, false);
    }

    private void appendColoredText(JTextPane textPane, String text, Color color, boolean bold) {
    // This method adds colored, bolded text to the JTextPane
    StyledDocument doc = textPane.getStyledDocument();
    Style style = textPane.addStyle("ColoredText", null);
    StyleConstants.setForeground(style, color);
    StyleConstants.setBold(style, bold);

    try {
        // Insert the text into the document
        doc.insertString(doc.getLength(), text, style);
    } catch (BadLocationException e) {
        e.printStackTrace();
    }
}
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("Client agent " + getAID().getName() + " deregistered from DF");
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }
}
