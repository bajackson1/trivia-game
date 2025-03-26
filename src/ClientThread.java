package src;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


// Added by Eric - THIS CLASS HANDLES THE COMMUNICATION BETWEEN THE SERVER AND THE ONE CLIENT ON THAT THREAD
// TCP
public class ClientThread implements Runnable {

    private int id;
    private Socket socket;
    private ServerTrivia server; // Added by Brooks

    // Added by Eric - Contructor for the client thread
    // Modified by Brooks - Added server reference
    public ClientThread(Socket clientSocket, int id, ServerTrivia server) {
        this.id = id;
        this.socket = clientSocket;
        this.server = server;
    }

    // Added by Eric - Method to listen for the incoming TCP packets from the client
    // Modified by Brooks - Implemented run() with score tracking
    @Override
    public void run() {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
            
            // Send initial score
            sendScoreUpdate(out);
            
            while (true) {
                Object input = in.readObject();
                if (input instanceof PlayerAnswer) {
                    processAnswer((PlayerAnswer) input, out);
                }
                // Other message types can be added here
            }
        } catch (Exception e) {
            System.err.println("Client " + id + " disconnected: " + e.getMessage());
        } finally {
            server.removeClient(id);
        }
    }

    // Added by Brooks - Process answer and update score
    private void processAnswer(PlayerAnswer answer, ObjectOutputStream out) throws IOException {
        boolean isCorrect = validateAnswer(answer);
        int scoreDelta = isCorrect ? 10 : -10;
        server.updateClientScore(id, scoreDelta);
        sendScoreUpdate(out);
    }

    // Added by Brooks - Validate answer (placeholder implementation)
    private boolean validateAnswer(PlayerAnswer answer) {
        // TODO: Implement actual validation against correct answers
        return false;
    }

    // Added by Brooks - Send score update to client
    private void sendScoreUpdate(ObjectOutputStream out) throws IOException {
        out.writeObject(new ScoreUpdate(server.getClientScore(id)));
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
