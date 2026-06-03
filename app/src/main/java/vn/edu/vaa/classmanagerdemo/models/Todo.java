package vn.edu.vaa.classmanagerdemo.models;

public class Todo {
    private int id;
    private String title;
    private String deadline;
    private boolean completed;

    public Todo() { }

    public Todo(int id, String title, String deadline, boolean completed) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.completed = completed;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
