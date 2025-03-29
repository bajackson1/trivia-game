package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;

import model.PlayerAnswer;
import model.Question;
import model.QuestionBank;
import model.TCPMessage;
import model.UDPMessage;

import java.util.Timer;
import java.util.TimerTask;

// Modified by Brooks - Client GUI for multiplayer trivia game
// Handles all user interaction and network communication
public class ClientWindow implements ActionListener {

    // UI Components
    private JButton poll;
    private JButton submit;
    private JRadioButton[] options;
    private ButtonGroup optionGroup;
    private JLabel question;
    private JLabel timer;
    private JLabel score;
    private TimerTask clock;
    private JFrame window;
    
    // Game State
    private Question currentQuestion;
    private int playerScore = 0;
    
    // Network Configuration
    private String serverIP;
    private int TCPserverPort;
    private int UDPserverPort;
    
    // Added by Brooks - TCP network connections
    private ObjectInputStream tcpIn;
    private ObjectOutputStream tcpOut;

    // Added by Eric - Client window constructor
    public ClientWindow() {
        // Initialize UI first
        initializeUI();
        
        // Then configure network
        readConfig();
        connectToServer();
        
        // Show the window immediately
        window.setVisible(true);
        
        // Start TCP listener
        new Thread(this::listenForTcpMessages).start();
    }

    // Added by Eric - Reads server configuration
    private void readConfig() {
        String configFile = "config/config.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            serverIP = reader.readLine().trim();
            TCPserverPort = Integer.parseInt(reader.readLine().trim());
            UDPserverPort = Integer.parseInt(reader.readLine().trim());
            System.out.println("Configured server: " + serverIP + ":" + TCPserverPort);
        } catch (IOException e) {
            throw new RuntimeException("Error reading config: " + e.getMessage());
        }
    }

    // Added by Brooks - Initializes all UI components
    private void initializeUI() {
        
        window = new JFrame("Trivia Client");
        window.setSize(400, 400);
        window.setLayout(null);
        
        // Question label with word wrapping
        question = new JLabel("<html>Waiting for first question...</html>");
        question.setBounds(10, 5, 380, 100);
        window.add(question);
        
        // Answer options
        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for(int i = 0; i < options.length; i++) {
            options[i] = new JRadioButton("Option " + (i+1));
            options[i].addActionListener(this);
            options[i].setBounds(10, 110 + (i * 30), 350, 25);
            options[i].setEnabled(false);
            window.add(options[i]);
            optionGroup.add(options[i]);
        }
        
        // Timer display
        timer = new JLabel("15", SwingConstants.CENTER);
        timer.setBounds(250, 250, 50, 20);
        timer.setForeground(Color.BLUE);
        window.add(timer);
        
        // Score display
        score = new JLabel("SCORE: 0");
        score.setBounds(50, 250, 100, 20);
        window.add(score);
        
        // Poll button
        poll = new JButton("Poll");
        poll.setBounds(50, 300, 100, 30);
        poll.addActionListener(this);
        window.add(poll);
        
        // Submit button
        submit = new JButton("Submit");
        submit.setBounds(250, 300, 100, 30);
        submit.addActionListener(this);
        submit.setEnabled(false);
        window.add(submit);
        
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    // Added by Brooks - Establishes server connection with proper resource management
    private void connectToServer() {
        Socket tcpSocket = null;
        try {
            tcpSocket = new Socket(serverIP, TCPserverPort);
            tcpOut = new ObjectOutputStream(tcpSocket.getOutputStream());
            tcpIn = new ObjectInputStream(tcpSocket.getInputStream());
            System.out.println("Connected to server");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Connection failed: " + e.getMessage());
            try {
                if (tcpSocket != null) {
                    tcpSocket.close();
                }
            } catch (IOException ex) {
                System.err.println("Error closing socket: " + ex.getMessage());
            }
            System.exit(1);
        }
    }

    // Added by Brooks - Listens for incoming TCP messages
    private void listenForTcpMessages() {
        try {
            while (true) {
                Object message = tcpIn.readObject();
                System.out.print("received");
                if (message instanceof TCPMessage) {
                    System.out.print("tcp");
                    processTcpMessage((TCPMessage) message);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); // Log the specific exception
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(window, "Connection error: " + e.getMessage()));
            System.exit(1);
        }
    }

    // Added by Brooks - Handles different message types
    private void processTcpMessage(TCPMessage message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case QUESTION:
                    System.out.print("switch case");
                    currentQuestion = (Question) message.getPayload();
                    loadQuestion(currentQuestion);
                    break;
                    
                case ACK:
                    handleAck();
                    break;
                    
                case NACK:
                    handleNack();
                    break;
                    
                case CORRECT:
                    playerScore += 10;
                    updateScore(playerScore);
                    showFeedback(true);
                    break;
                    
                case WRONG:
                    playerScore -= 10;
                    updateScore(playerScore);
                    showFeedback(false);
                    break;

                case TIMEOUT:
                    playerScore -= 20;
                    updateScore(playerScore);
                    showFeedback(false);
                    break;
                    
                case GAME_OVER:
                    endGame();
                    break;
                    
                case SCORE_UPDATE:
                    updateScore((Integer) message.getPayload());
                    break;
                
                case KILL_CLIENT:
                    killClient();
                    break;
            }
        });
    }

    private void killClient(){
        JOptionPane.showMessageDialog(window, "You have been terminated by the server.");
        try {
            Thread.sleep(3000);  // Delay for 3 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    // Added by Brooks - Loads and displays new question
    private void loadQuestion(Question question) {
        this.currentQuestion = question;
        this.question.setText("<html>Q" + question.getQuestionNumber() + 
                           ". " + question.getQuestionText() + "</html>");
        
        String[] currentOptions = question.getOptions();
        for (int i = 0; i < options.length; i++) {
            options[i].setText(currentOptions[i]);
            options[i].setSelected(false);
        }
        
        System.out.print("load");
        resetForNewQuestion();
    }

    // Added by Brooks - Resets UI for new question
    private void resetForNewQuestion() {
        if (clock != null) clock.cancel();
        
        optionGroup.clearSelection();
        for (JRadioButton option : options) {
            option.setSelected(false);
            option.setEnabled(false);
        }
        
        clock = new TimerCode(15, false);
        new Timer().schedule(clock, 0, 1000);
        poll.setEnabled(true);
        submit.setEnabled(false);

        System.out.print("reset");
    }

    // Added by Brooks - Handles poll button click
    //Modified by Pierce - Poll should be able to be spammed until timer reaches 0(client recieves message from serv).
    private void handlePoll() {
        sendBuzzMessage();
    }

    // Added by Eric - send the buzz to the server using UDP when polling
    private void sendBuzzMessage() {
        try (DatagramSocket socket = new DatagramSocket()) {
            UDPMessage message = new UDPMessage(
                System.currentTimeMillis(), 
                InetAddress.getLocalHost().getHostAddress()
            );
            
            byte[] data = message.encode();
            DatagramPacket packet = new DatagramPacket(
                data, data.length, 
                InetAddress.getByName(serverIP), 
                UDPserverPort
            );
            
            socket.send(packet);
            System.out.println("Buzz message sent");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Error sending buzz: " + e.getMessage());
        }
    }

    // Added by Brooks - Handles ACK from server
    // Modified by Eric - Polling disabled
    private void handleAck() {
        System.out.println("Received ACK from server");
        poll.setEnabled(false);
        submit.setEnabled(true);
        for (JRadioButton option : options) {
            option.setEnabled(true);
        }
        
        if (clock != null) {
            clock.cancel();
        }
        clock = new TimerCode(10, true);
        new Timer().schedule(clock, 0, 1000);
    }

    // Added by Brooks - Handles NACK from server
    // Modified by Eric - disabled polling, cancels the buzzing timer and replaces with too slow text
    private void handleNack() {
        System.out.println("Received NACK from server");
        poll.setEnabled(false);
        if (clock != null) {
            clock.cancel();
        }
        timer.setText("Too slow! Please wait.");
    }

    // Added by Brooks - Updates score display
    private void updateScore(int newScore) {
        playerScore = newScore;
        score.setText("SCORE: " + playerScore);
    }

    // Added by Brooks - Shows feedback popup
    private void showFeedback(boolean isCorrect) {
        JOptionPane.showMessageDialog(window, 
            isCorrect ? "Correct! +10 points" : "Wrong! -10 points");
        
        //loadNextQuestion();
    }

    // Added by Brooks - Loads next question from bank
    private void loadNextQuestion() {
        if (currentQuestion == null) {
            endGame();
            return;
        }
        loadQuestion(currentQuestion);
    }

    // Added by Brooks - Ends game session
    private void endGame() {
        JOptionPane.showMessageDialog(window, 
            "Game over! Your final score: " + playerScore);
        window.dispose();
        System.exit(0);
    }

    // Added by Eric - Handles all button clicks
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        
        if ("Poll".equals(command)) {
            handlePoll();
        } 
        else if ("Submit".equals(command)) {
            handleSubmit();
        }
    }

    // Added by Brooks - Handles answer submission
    private void handleSubmit() {
        char selectedAnswer = ' ';
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                selectedAnswer = (char) ('A' + i);
                break;
            }
        }
        
        if (selectedAnswer != ' ') {
            try {
                PlayerAnswer answer = new PlayerAnswer(
                    currentQuestion.getQuestionNumber(),
                    selectedAnswer
                );



                System.out.print(currentQuestion.getQuestionNumber());



                tcpOut.writeObject(answer);
                tcpOut.flush();
                submit.setEnabled(false);
                if (clock != null) clock.cancel();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(window, "Error submitting answer");
            }
        }
    }

    // Modified by Brooks - Enhanced timer class
    // Modified by Eric - removes local penalty application and waits for TIMEOUT of QUESTION
    private class TimerCode extends TimerTask {
        private int duration;
        private boolean isAnswerPeriod;
        
        public TimerCode(int duration, boolean isAnswerPeriod) {
            this.duration = duration;
            this.isAnswerPeriod = isAnswerPeriod;
        }
        
        @Override
        public void run() {
            SwingUtilities.invokeLater(() -> {
                if (duration < 0) {
                    timer.setText("Time expired");
                    this.cancel();
                } else {
                    timer.setForeground(duration < 6 ? Color.RED : Color.BLUE);
                    timer.setText(duration + "");
                    duration--;
                }
                window.repaint();
            });
        }
    }

    public static void main(String[] args) {
        new ClientWindow();
    }
}