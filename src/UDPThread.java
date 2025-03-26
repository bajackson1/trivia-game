package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

// Added by Eric - THIS CLASS HANDLES THE COMMUNICATION BETWEEN THE SERVER AND ALL THE CLIENTS
// UDP 
public class UDPThread implements Runnable{

    private DatagramSocket socket;
    private ServerTrivia server;

    public UDPThread(DatagramSocket socket, ServerTrivia server) {
        this.socket = socket;
        this.server = server;
    }

    // Added by Eric - Method to listen for incoming UDP packets from all clients
    @Override
    public void run() {
        try {
            byte[] incomingData = new byte[512];
            while (true) {
                DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
                socket.receive(incomingPacket);

                // Process the incoming packet
                String clientIP = incomingPacket.getAddress().getHostAddress();
                
                // Decode the message from the packet
                UDPMessage receivedMessage = UDPMessage.decode(
                    java.util.Arrays.copyOfRange(incomingPacket.getData(), 0, incomingPacket.getLength())
                );
                
                if (receivedMessage == null) {
                    System.err.println("Failed to decode message from IP: " + clientIP);
                    continue;
                }

                // Find the client thread based on IP
                ClientThread clientThread = getClientThreadByIP(clientIP);
                if (clientThread != null) {
                    // Process the message and handle buzz logic
                    processBuzz(clientThread, receivedMessage);
                } else {
                    System.err.println("Unknown client IP: " + clientIP);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in UDP listening thread: " + e.getMessage());
        }
    }

    // Added by Eric - Method will be used to process the buzz when receiving packet from clients
    // This method will find the client ID of the client that buzzed and then add them to the queue of buzzed clients
    private void processBuzz(ClientThread clientThread, UDPMessage receivedMessage) {
        System.out.println("Buzz received from client: " + receivedMessage.getClientIP() + " at " + receivedMessage.getTimestamp());
    }

    // Added by Eric - Method to get the client thread based on the UDP client sender IP
    private ClientThread getClientThreadByIP(String ip) {
        for (ClientThread client : server.getActiveClients().values()) {
            if (client.getClientIP().equals(ip)) {
                return client;
            }
        }
        return null; 
    }
}
