package src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// Added by Eric - THIS CLASS HANDLES THE COMMUNICATION BETWEEN THE SERVER AND ALL THE CLIENTS
// UDP 
public class UDPThread implements Runnable{

    private DatagramSocket socket;
    private ServerTrivia server;
    private Queue<Integer> buzzQueue;
    private long latestTimestamp;

    public UDPThread(DatagramSocket socket, ServerTrivia server) {
        this.socket = socket;
        this.server = server;
        this.buzzQueue = new ConcurrentLinkedQueue<>();
        this.latestTimestamp = Long.MIN_VALUE;
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

    public void clearBuzzQueue() {
        buzzQueue.clear();
        latestTimestamp = Long.MIN_VALUE;
    }

    public Integer getFirstBuzzedClient() {
        return buzzQueue.poll();
    }

    // Added by Eric - Method to process the buzz while maintaining timestamp order
    private void processBuzz(ClientThread clientThread, UDPMessage receivedMessage) {
        if (clientThread != null) {
            Integer clientID = clientThread.getClientID();
            long timestamp = receivedMessage.getTimestamp(); // Get timestamp from message
    
            // If the packet timestamp is newer than the latest, just add it
            if (timestamp > latestTimestamp) {
                latestTimestamp = timestamp;
                buzzQueue.add(clientID);
                System.out.println("Newer packet received. Client " + clientID + " added to queue.");
            } else {
                // If the packet timestamp is older, we need to handle out-of-order packets
                Queue<Integer> tempQueue = new ConcurrentLinkedQueue<>(buzzQueue);
    
                // Clear the main queue and add the new packet
                buzzQueue.clear();
                buzzQueue.add(clientID);
    
                // Reinsert all previous clients
                for (Integer id : tempQueue) {
                    buzzQueue.add(id);
                }
    
                System.out.println("Older packet received. Requeueing with Client " + clientID + " added at the front.");
            }
    
            System.out.println("Current Queue: " + buzzQueue);
        } else {
            System.out.println("Client not found for IP: " + receivedMessage.getClientIP());
        }
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
