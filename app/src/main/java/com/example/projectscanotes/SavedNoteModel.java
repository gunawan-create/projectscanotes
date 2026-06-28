package com.example.projectscanotes;

public class SavedNoteModel {
    private int id;
    private String title;
    private String explanation;
    private String examples;
    private String summary;
    private String timestamp;

    public SavedNoteModel(int id, String title, String explanation, String examples, String summary, String timestamp) {
        this.id = id;
        this.title = title;
        this.explanation = explanation;
        this.examples = examples;
        this.summary = summary;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getExplanation() { return explanation; }
    public String getExamples() { return examples; }
    public String getSummary() { return summary; }
    public String getTimestamp() { return timestamp; }
}