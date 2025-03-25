package src;

import java.net.Socket;


// Added by Eric - THIS CLASS HANDLES THE COMMUNICATION BETWEEN THE SERVER AND THE ONE CLIENT ON THAT THREAD
// TCP
public class ClientThread implements Runnable {

    private int id;
    private Socket socket;

    public ClientThread(Socket clientSocket, int id) {
        this.id = id;
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        
    }
}
