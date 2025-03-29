package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionBank {
    private List<Question> questions;
    private int currentQuestionIndex;
    
    public QuestionBank() {
        questions = new ArrayList<>();
        currentQuestionIndex = 0;
        loadQuestionsFromFile();
    }
    
    // Load questions from config file
    private void loadQuestionsFromFile() {
        try (BufferedReader reader = new BufferedReader(
            new FileReader(Paths.get("config", "questions.txt").toFile()))) {
            
            String line;
            int number = 1; // Start from 1
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    questions.add(new Question(
                        parts[0], 
                        Arrays.copyOfRange(parts, 1, 5),
                        parts[5].charAt(0),
                        number // Assigns the correct question number
                    ));
                }
                number++;
            }
        } catch (IOException e) {
            System.err.println("Error loading questions: " + e.getMessage());
            loadDefaultQuestions();
        }
    }
    
    // Fallback if file loading fails
    private void loadDefaultQuestions() {
        questions.add(new Question(
            "Who holds the single-game points record?",
            new String[]{"Michael Jordan", "Kobe Bryant", "Wilt Chamberlain", "LeBron James"},
            'C',
            1
        ));
    }
    
    public Question getNextQuestion() {
        return hasMoreQuestions() ? questions.get(currentQuestionIndex++) : null;
    }
    
    public boolean hasMoreQuestions() {
        return currentQuestionIndex < questions.size();
    }
    
    public int getCurrentQuestionNumber() {
        return currentQuestionIndex + 1;
    }

    public Question getQuestion(int questionId) {
        if (questionId >= 1 && questionId <= questions.size()) {
            return questions.get(questionId - 1);
        }
        return null;
    }
}
