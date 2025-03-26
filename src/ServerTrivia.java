package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerTrivia {

    private ExecutorService executorService;
    private int nextClientID = 1;
    private String serverIP;
    private int serverPort1; // TCP Port
    private int serverPort2; // UDP Port
    private Map<Integer, ClientThread> activeClients;
    
    // Added by Eric - Server Trivia Constructor
    public ServerTrivia() {
        executorService = Executors.newCachedThreadPool();
        activeClients = new HashMap<>();
    }

    // Added by Eric - Read Server Config for IP and Port
    public void readConfig() {
        String configFile = "config/config.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            // Read server IP
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                serverIP = line.trim();
                System.out.println("Server IP: " + serverIP);
            } else {
                throw new IOException("Config file is empty or invalid.");
            }

            // Read TCP Port
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                serverPort1 = Integer.parseInt(line.trim());
                System.out.println("TCP Port: " + serverPort1);
            } else {
                throw new IOException("Invalid TCP port in config.");
            }

            // Read UDP Port
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                serverPort2 = Integer.parseInt(line.trim());
                System.out.println("UDP Port: " + serverPort2);
            } else {
                throw new IOException("Invalid UDP port in config.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading server config: " + e.getMessage());
        }
    }

    // Added by Eric - Start Trivia Server 
    public void startServer() {
        try {

            // Start TCP server socket
            ServerSocket serverSocket = new ServerSocket(serverPort1, 50, InetAddress.getByName(serverIP));
            System.out.println("Server started on IP: " + serverIP + " Port: " + serverPort1);

            // Start UDP server socket
            DatagramSocket udpSocket = new DatagramSocket(serverPort2, InetAddress.getByName(serverIP));
            System.out.println("UDP server started on Port: " + serverPort2);

            // Start UDP thread to handle all incoming UDP packets
            UDPThread udpThread = new UDPThread(udpSocket, this);
            executorService.submit(udpThread);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                // Assign a unique clientID and create a new ClientThread for the client
                // Client thread handles the TCP communication with the client
                int clientID = nextClientID;
                ClientThread clientThread = new ClientThread(clientSocket, clientID);
                this.nextClientID++;

                // Add the client to the activeClients map
                activeClients.put(clientID, clientThread);

                // Start the client thread
                executorService.submit(clientThread);
            }

        } catch (Exception e) {
            System.err.println("Error in server startup: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    // Added by Eric - Shutdown server thread pool
    private void shutdown() {
        executorService.shutdown();
        System.out.println("Server shutting down...");
    }

    // Added by Eric - Method to get all connected clients
    public Map<Integer, ClientThread> getActiveClients() {
        return activeClients;
    }

    // Added by Eric - Method to add a new client to the map
    public void addClient(int clientID, ClientThread clientThread) {
        activeClients.put(clientID, clientThread);
    }

    // Added by Eric - Method to remove a client from the map
    public void removeClient(int clientID) {
        activeClients.remove(clientID);
    }

    // Added by Eric - main method to start trivia server
    public static void main(String[] args) {
        ServerTrivia server = new ServerTrivia();
        server.readConfig();
        server.startServer();
    }
}