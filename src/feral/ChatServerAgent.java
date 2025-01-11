package feral;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;
import java.util.List;

public class ChatServerAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("ChatServerAgent started.");
        
        // Add a behavior to receive messages
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Waiting for a message from a client
                MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
                ACLMessage msg = receive(template);
                
                if (msg != null) {
                    // Message received from a client
                    System.out.println("Received message: " + msg.getContent());
                    
                    // Add the message to the queue and broadcast it to other clients
                    broadcastMessage(msg);
                } else {
                    block();
                }
            }
        });
    }

    private void broadcastMessage(ACLMessage message) {
        // Broadcast the message to all connected clients (in this example, we are just printing it)
        System.out.println("Broadcasting message to all clients: " + message.getContent());
        // Add message forwarding logic here if needed to a list of clients
    }

    @Override
    protected void takeDown() {
        System.out.println("ChatServerAgent is terminating.");
    }
}
