
package feral;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIAgent extends Agent {

    @Override
    protected void setup() {
        // Add a one-shot behaviour to launch the GUI
        addBehaviour(new OneShotBehaviour() {
            @Override
            public void action() {
                launchGUI();
            }
        });
    }

    private void launchGUI() {
        // Create a frame
        JFrame frame = new JFrame("Agent GUI");
        frame.setSize(1600, 900);  // Set window size
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);  // Center the window
        
        // Clear the frame for the new interface
        frame.getContentPane().removeAll();
        frame.repaint();

        // Create the chat components
        JTextPane statusPane = new JTextPane();
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("Send");
        JList<String> clientsList = new JList<>();
        JButton refreshButton = new JButton("<html><b><font color='green'>Refresh</font></b></html>"); // Refresh button

        // Set component sizes
        statusPane.setPreferredSize(new Dimension(1200, 200)); // Status pane size
        messageField.setPreferredSize(new Dimension(1200, 60)); // Message input field size
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

        frame.setVisible(true);
        
        // Initial refresh to populate the client list
    }


}
