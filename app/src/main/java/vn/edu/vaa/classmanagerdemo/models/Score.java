package vn.edu.vaa.classmanagerdemo.models;

public class Score {
    private int id;
    private int studentId;
    private String studentName;
    private String subject;
    private float score;
    private String semester;

    // VAA GPA breakdown fields
    private int credits = 3;
    private float scoreQT = 0f;
    private int weightQT = 50;
    private float scoreCK = 0f;
    private int weightCK = 50;

    public Score() {}

    public Score(int id, int studentId, String subject, float score, String semester) {
        this.id = id;
        this.studentId = studentId;
        this.subject = subject;
        this.score = score;
        this.semester = semester;
        this.credits = 3;
        this.scoreQT = score;
        this.weightQT = 0;
        this.scoreCK = score;
        this.weightCK = 100;
    }

    public Score(int studentId, String subject, float score, String semester) {
        this.studentId = studentId;
        this.subject = subject;
        this.score = score;
        this.semester = semester;
        this.credits = 3;
        this.scoreQT = score;
        this.weightQT = 0;
        this.scoreCK = score;
        this.weightCK = 100;
    }

    // New constructors for full grade details
    public Score(int id, int studentId, String subject, int credits, float scoreQT, int weightQT, float scoreCK, int weightCK, String semester) {
        this.id = id;
        this.studentId = studentId;
        this.subject = subject;
        this.credits = credits;
        this.scoreQT = scoreQT;
        this.weightQT = weightQT;
        this.scoreCK = scoreCK;
        this.weightCK = weightCK;
        this.semester = semester;
        this.score = calculateScoreTotal();
    }

    public Score(int studentId, String subject, int credits, float scoreQT, int weightQT, float scoreCK, int weightCK, String semester) {
        this.studentId = studentId;
        this.subject = subject;
        this.credits = credits;
        this.scoreQT = scoreQT;
        this.weightQT = weightQT;
        this.scoreCK = scoreCK;
        this.weightCK = weightCK;
        this.semester = semester;
        this.score = calculateScoreTotal();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public float getScore() { 
        return calculateScoreTotal(); 
    }
    public void setScore(float score) { this.score = score; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public float getScoreQT() { return scoreQT; }
    public void setScoreQT(float scoreQT) { this.scoreQT = scoreQT; }
    public int getWeightQT() { return weightQT; }
    public void setWeightQT(int weightQT) { this.weightQT = weightQT; }
    public float getScoreCK() { return scoreCK; }
    public void setScoreCK(float scoreCK) { this.scoreCK = scoreCK; }
    public int getWeightCK() { return weightCK; }
    public void setWeightCK(int weightCK) { this.weightCK = weightCK; }

    private float calculateScoreTotal() {
        float raw = (scoreQT * weightQT + scoreCK * weightCK) / 100f;
        return Math.round(raw * 10f) / 10f; // Round to 1 decimal place
    }

    public String getGradeLetter() {
        float s = getScore();
        if (s >= 8.5f) return "A";
        if (s >= 8.0f) return "B+";
        if (s >= 7.0f) return "B";
        if (s >= 6.5f) return "C+";
        if (s >= 5.5f) return "C";
        if (s >= 5.0f) return "D+";
        if (s >= 4.0f) return "D";
        return "F";
    }

    public float getGrade4() {
        float s = getScore();
        if (s >= 8.5f) return 4.0f;
        if (s >= 8.0f) return 3.5f;
        if (s >= 7.0f) return 3.0f;
        if (s >= 6.5f) return 2.5f;
        if (s >= 5.5f) return 2.0f;
        if (s >= 5.0f) return 1.5f;
        if (s >= 4.0f) return 1.0f;
        return 0.0f;
    }

    public String getGradeLabel() {
        float s = getScore();
        if (s >= 8.5f) return "Giỏi";
        if (s >= 7.0f) return "Khá";
        if (s >= 5.5f) return "Trung bình";
        if (s >= 4.0f) return "Yếu";
        return "Kém";
    }

    public String getGradeColor() {
        float s = getScore();
        if (s >= 8.5f) return "#10B981"; // Emerald
        if (s >= 7.0f) return "#3B82F6"; // Blue
        if (s >= 5.5f) return "#F59E0B"; // Amber
        if (s >= 4.0f) return "#F97316"; // Orange
        return "#EF4444"; // Red
    }
}
