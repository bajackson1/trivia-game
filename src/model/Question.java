package model;

import java.io.Serializable;

// Added by Brooks - Class to represent a single trivia question
public class Question implements Serializable {
    // Add serialVersionUID for version control
    private static final long serialVersionUID = 1L;

    private String questionText;
    private String[] options;
    private char correctAnswer;

    // Added by Brooks
    public Question(String questionText, String[] options, char correctAnswer) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    // Added by Brooks - Getters for question data
    public String getQuestionText() { return questionText; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }
}