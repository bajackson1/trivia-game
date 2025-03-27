package src;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.*;

public class ClientWindow implements ActionListener
{
    private JButton poll;
    private JButton submit;
    private JRadioButton options[];
    private ButtonGroup optionGroup;
    private JLabel question;
    private JLabel timer;
    private JLabel score;
    private TimerTask clock;
    
    private JFrame window;
    
    @SuppressWarnings("unused")
    private static SecureRandom random = new SecureRandom();
    
    // Added by Brooks - New fields for question handling
    private QuestionBank questionBank;
    private Question currentQuestion;
    private int playerScore = 0;

    // Added by Eric - fields for server information (IP and Ports)
    private String serverIP;
    private int TCPserverPort;
    private int UDPserverPort;
    
    // write setters and getters as you need
    
    public ClientWindow()
    {
        // Added by Brooks - Initialize question bank
        questionBank = new QuestionBank();
        
        JOptionPane.showMessageDialog(window, "This is a trivia game");
        
        window = new JFrame("Trivia");
        // Modified by Brooks - Added HTML wrapping for question text
        question = new JLabel("<html>Q1. Loading question...</html>");
        question.setVerticalAlignment(SwingConstants.TOP);
        question.setBounds(10, 5, 380, 100); // Adjusted width
        window.add(question);
        
        options = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for(int index=0; index<options.length; index++)
        {
            options[index] = new JRadioButton("Option " + (index+1));  // represents an option
            // if a radio button is clicked, the event would be thrown to this class to handle
            options[index].addActionListener(this);
            options[index].setBounds(10, 110+(index*30), 350, 25); // Adjusted spacing
            window.add(options[index]);
            optionGroup.add(options[index]);
        }

        timer = new JLabel("15", SwingConstants.CENTER);  // Modified by Brooks - Centered timer
        timer.setBounds(250, 250, 50, 20);
        timer.setForeground(Color.BLUE);
        window.add(timer);
        
        score = new JLabel("SCORE: 0"); // Modified by Brooks - Added initial score
        score.setBounds(50, 250, 100, 20);
        window.add(score);

        poll = new JButton("Poll");  // button that use clicks/ like a buzzer
        poll.setBounds(50, 300, 100, 30); // Adjusted position
        poll.addActionListener(this);  // calls actionPerformed of this class
        window.add(poll);
        
        submit = new JButton("Submit");  // button to submit their answer
        submit.setBounds(250, 300, 100, 30); // Adjusted position
        submit.addActionListener(this);  // calls actionPerformed of this class
        submit.setEnabled(false);
        window.add(submit);
        
        window.setSize(400,400);
        window.setBounds(50, 50, 400, 400);
        window.setLayout(null);
        window.setVisible(true);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        
        // Added by Brooks - Load first question
        loadNextQuestion();
    }
    
    // Added by Brooks - Modified to ensure no options are selected when question appears
    private void loadNextQuestion() {
        currentQuestion = questionBank.getNextQuestion();
        if (currentQuestion == null) {
            question.setText("<html>Game Over! Final Score: " + playerScore + "</html>");
            poll.setEnabled(false);
            submit.setEnabled(false);
            return;
        }
        
        // Use HTML for automatic word wrapping
        question.setText("<html>Q" + questionBank.getCurrentQuestionNumber() + 
                       ". " + currentQuestion.getQuestionText() + "</html>");
        
        String[] currentOptions = currentQuestion.getOptions();
        for (int i = 0; i < options.length; i++) {
            options[i].setText(currentOptions[i]);
            options[i].setSelected(false); // Ensure no option is selected
        }
        optionGroup.clearSelection(); // Clear any radio button selection
        
        resetForNewQuestion();
    }
    
    // Added by Brooks - Enhanced to ensure clean state for new questions
    private void resetForNewQuestion() {
        if (clock != null) {
            clock.cancel();
        }
        
        // Double-clear selection to prevent any sticking
        optionGroup.clearSelection();
        for (JRadioButton option : options) {
            option.setSelected(false);
        }
        
        clock = new TimerCode(15, false); // 15s question period
        new Timer().schedule(clock, 0, 1000);
        poll.setEnabled(true);
        submit.setEnabled(false);
        for (JRadioButton option : options) {
            option.setEnabled(false);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        System.out.println("You clicked " + e.getActionCommand());
        
        // input refers to the radio button you selected or button you clicked
        String input = e.getActionCommand();  
        switch(input)
        {
            case "Option 1":	// Your code here
                                break;
            case "Option 2":	// Your code here
                                break;
            case "Option 3":	// Your code here
                                break;
            case "Option 4":	// Your code here
                                break;
            case "Poll":		// Added by Brooks - Handle poll button
                                handlePoll();
                                break;
            case "Submit":		// Added by Brooks - Handle submit button
                                handleSubmit();
                                break;
            default:
                                System.out.println("Incorrect Option");
        }
    }
    
    // Added by Brooks - Handles poll button click
    // Modified by Eric - send the buzz to the server using UDP send message method
    private void handlePoll() {
        poll.setEnabled(false);
        submit.setEnabled(true);
        for (JRadioButton option : options) {
            option.setEnabled(true);
        }

        // Send poll notification to the server via UDP
        this.sendBuzzMessage();
        
        // Cancel question timer and start answer timer
        clock.cancel();
        clock = new TimerCode(10, true); // 10s answer period
        new Timer().schedule(clock, 0, 1000);
    }
    
    // Added by Brooks - Handles submit button click
    private void handleSubmit() {
        clock.cancel(); // Stop the answer timer
        submit.setEnabled(false);
        
        char selectedAnswer = ' ';
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                selectedAnswer = (char) ('A' + i);
                break;
            }
        }
        
        if (selectedAnswer == currentQuestion.getCorrectAnswer()) {
            playerScore += 10;
            JOptionPane.showMessageDialog(window, "Correct! +10 points");
        } else {
            playerScore -= 10;
            JOptionPane.showMessageDialog(window, "Wrong! -10 points");
        }
        score.setText("SCORE: " + playerScore);
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                loadNextQuestion();
            }
        }, 1500);
    }
    
    // Modified by Brooks - Enhanced timer class
    private class TimerCode extends TimerTask
    {
        private int duration;
        private boolean isAnswerPeriod;
        
        public TimerCode(int duration, boolean isAnswerPeriod)
        {
            this.duration = duration;
            this.isAnswerPeriod = isAnswerPeriod;
        }
        
        @Override
        public void run()
        {
            if(duration < 0)
            {
                timer.setText("Time expired");
                if(isAnswerPeriod) {
                    playerScore -= 20; // Penalty for not answering
                    score.setText("SCORE: " + playerScore);
                    JOptionPane.showMessageDialog(window, "Time's up! -20 points");
                }
                window.repaint();
                this.cancel();
                
                if(isAnswerPeriod) {
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            loadNextQuestion();
                        }
                    }, 1500);
                }
                return;
            }
            
            if(duration < 6)
                timer.setForeground(Color.red);
            else
                timer.setForeground(Color.blue);
            
            timer.setText(duration+"");
            duration--;
            window.repaint();
        }
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
                TCPserverPort = Integer.parseInt(line.trim());
                System.out.println("TCP Port: " + TCPserverPort);
            } else {
                throw new IOException("Invalid TCP port in config.");
            }

            // Read UDP Port
            line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                UDPserverPort = Integer.parseInt(line.trim());
                System.out.println("UDP Port: " + UDPserverPort);
            } else {
                throw new IOException("Invalid UDP port in config.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading server config: " + e.getMessage());
        }
    }

    // Added by Eric - send the buzz to the server using UDP when polling
    private void sendBuzzMessage() {
        try (DatagramSocket socket = new DatagramSocket()) {
            // Create UDPMessage with current timestamp and local IP
            UDPMessage message = new UDPMessage(System.currentTimeMillis(), InetAddress.getLocalHost().getHostAddress());
            byte[] data = message.encode();

            // Send the packet
            InetAddress serverAddress = InetAddress.getByName(serverIP);
            DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, UDPserverPort);
            socket.send(packet);
            System.out.println("Message sent to " + serverIP);
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }
}