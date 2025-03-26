package src;

import java.net.Socket;


// Added by Eric - THIS CLASS HANDLES THE COMMUNICATION BETWEEN THE SERVER AND THE ONE CLIENT ON THAT THREAD
// TCP
public class ClientThread implements Runnable {

    private int id;
    private Socket socket;

    // Added by Eric - Contructor for the client thread
    public ClientThread(Socket clientSocket, int id) {
        this.id = id;
        this.socket = clientSocket;
    }

    // Added by Eric - Method to listen for the incoming TCP packets from the client
    @Override
    public void run() {
        
    }

    // Added by Eric - Getter for the client IP, client socket, and the client ID for this client thread.
    public String getClientIP() {
        return socket.getInetAddress().getHostAddress();
    }

    public Socket getClientSocket() {
        return socket;
    }

    public int getID() {
        return id;
    }
}
