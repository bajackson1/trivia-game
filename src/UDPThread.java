package src;

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

    @Override
    public void run() {
        
    }
}
