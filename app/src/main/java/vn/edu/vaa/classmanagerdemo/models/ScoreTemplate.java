package vn.edu.vaa.classmanagerdemo.models;

public class ScoreTemplate {
    private int id;
    private int teacherId;
    private String templateName;
    private int weightQt;
    private int weightGk;
    private int weightCk;

    public ScoreTemplate() {}

    public ScoreTemplate(int id, int teacherId, String templateName, int weightQt, int weightGk, int weightCk) {
        this.id = id;
        this.teacherId = teacherId;
        this.templateName = templateName;
        this.weightQt = weightQt;
        this.weightGk = weightGk;
        this.weightCk = weightCk;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }

    public int getWeightQt() { return weightQt; }
    public void setWeightQt(int weightQt) { this.weightQt = weightQt; }

    public int getWeightGk() { return weightGk; }
    public void setWeightGk(int weightGk) { this.weightGk = weightGk; }

    public int getWeightCk() { return weightCk; }
    public void setWeightCk(int weightCk) { this.weightCk = weightCk; }
}
