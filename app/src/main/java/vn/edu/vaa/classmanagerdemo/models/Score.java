package vn.edu.vaa.classmanagerdemo.models;

public class Score {
    private int id;
    private int studentId;
    private String studentName;
    private String subject;
    private float score;
    private String semester;

    public Score() {}

    public Score(int id, int studentId, String subject, float score, String semester) {
        this.id = id;
        this.studentId = studentId;
        this.subject = subject;
        this.score = score;
        this.semester = semester;
    }

    public Score(int studentId, String subject, float score, String semester) {
        this.studentId = studentId;
        this.subject = subject;
        this.score = score;
        this.semester = semester;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public float getScore() { return score; }
    public void setScore(float score) { this.score = score; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public String getGradeLabel() {
        if (score >= 8.5f) return "Giỏi";
        if (score >= 7.0f) return "Khá";
        if (score >= 5.5f) return "Trung bình";
        if (score >= 4.0f) return "Yếu";
        return "Kém";
    }

    // Returns hex color string based on grade
    public String getGradeColor() {
        if (score >= 8.5f) return "#10B981";
        if (score >= 7.0f) return "#3B82F6";
        if (score >= 5.5f) return "#F59E0B";
        if (score >= 4.0f) return "#F97316";
        return "#EF4444";
    }
}
