package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Added by Brooks - Manages loading and serving questions
public class QuestionBank {
    private List<Question> questions;
    private int currentQuestionIndex;
    
    // Added by Brooks
    public QuestionBank() {
        questions = new ArrayList<>();
        currentQuestionIndex = 0;
        loadQuestionsFromFile();
    }
    
    // Added by Brooks - Load questions from config file
    private void loadQuestionsFromFile() {
        try (BufferedReader reader = new BufferedReader(
            new FileReader(Paths.get("config", "questions.txt").toFile()))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    questions.add(new Question(
                        parts[0], 
                        Arrays.copyOfRange(parts, 1, 5),
                        parts[5].charAt(0)
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            loadDefaultQuestions();
        }
    }
    
    // Added by Brooks - Fallback if file loading fails
    private void loadDefaultQuestions() {
        questions.add(new Question(
            "Who holds the single-game points record?",
            new String[]{"Michael Jordan", "Kobe Bryant", "Wilt Chamberlain", "LeBron James"},
            'C'
        ));
    }
    
    // Added by Brooks - Get next question in sequence
    public Question getNextQuestion() {
        return hasMoreQuestions() ? questions.get(currentQuestionIndex++) : null;
    }
    
    // Added by Brooks - Check if more questions available
    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size();
    }
    
    // Added by Brooks - Get current question number
    public int getCurrentQuestionNumber() {
        return currentQuestionIndex;
    }
}