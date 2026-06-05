package vn.edu.vaa.classmanagerdemo.models;

public class Score {
    private int id;
    private int studentId;
    private int classId;
    private String studentName;
    private String studentCode;
    private String subject;
    private String semester;

    private float scoreQT = 0f;
    private int weightQT = 20;
    private float scoreGK = 0f;
    private int weightGK = 30;
    private float scoreCK = 0f;
    private int weightCK = 50;

    private float score;

    public Score() {}

    public Score(int studentId, int classId, float scoreQT, int weightQT,
                 float scoreGK, int weightGK, float scoreCK, int weightCK, String semester) {
        this.studentId = studentId;
        this.classId = classId;
        this.scoreQT = scoreQT;
        this.weightQT = weightQT;
        this.scoreGK = scoreGK;
        this.weightGK = weightGK;
        this.scoreCK = scoreCK;
        this.weightCK = weightCK;
        this.semester = semester;
        this.score = calculateScore();
    }

    private float calculateScore() {
        float raw = (scoreQT * weightQT + scoreGK * weightGK + scoreCK * weightCK) / 100f;
        return Math.round(raw * 10f) / 10f;
    }

    public float getScore() {
        if (score == 0f && (scoreQT != 0f || scoreGK != 0f || scoreCK != 0f)) {
            score = calculateScore();
        }
        return score;
    }

    public String getGradeLetter() {
        float s = getScore();
        if (s >= 8.5f) return "A";
        if (s >= 7.0f) return "B";
        if (s >= 5.5f) return "C";
        if (s >= 4.0f) return "D";
        return "F";
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
        if (s >= 8.5f) return "#10B981";
        if (s >= 7.0f) return "#3B82F6";
        if (s >= 5.5f) return "#F59E0B";
        if (s >= 4.0f) return "#F97316";
        return "#EF4444";
    }

    public String generateComment() {
        float s = getScore();
        if (s >= 9.0f) {
            return "Học sinh xuất sắc, tiếp thu bài cực nhanh, tích cực phát biểu xây dựng bài.";
        } else if (s >= 8.5f) {
            return "Học sinh giỏi, hoàn thành tốt các bài tập, có tinh thần tự giác cao.";
        } else if (s >= 8.0f) {
            return "Học tập tốt, nắm vững kiến thức căn bản và nâng cao, cần phát huy hơn nữa.";
        } else if (s >= 7.0f) {
            return "Khá, làm bài tập đầy đủ, đôi lúc còn thiếu tập trung nhưng nhìn chung học tốt.";
        } else if (s >= 6.0f) {
            return "Trung bình khá, có cố gắng trong học tập, cần luyện tập thêm các bài nâng cao.";
        } else if (s >= 5.0f) {
            return "Trung bình, nắm được kiến thức cơ bản, cần nỗ lực nhiều hơn ở bài thi cuối kỳ.";
        } else if (s >= 4.0f) {
            return "Yếu, còn hổng kiến thức, bài tập chưa hoàn thiện tốt, cần phụ đạo thêm.";
        } else {
            return "Kém, không tập trung trong lớp, kết quả thi chưa đạt yêu cầu, gia đình cần sát sao hơn.";
        }
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public float getScoreQT() { return scoreQT; }
    public void setScoreQT(float v) { this.scoreQT = v; this.score = calculateScore(); }
    public int getWeightQT() { return weightQT; }
    public void setWeightQT(int v) { this.weightQT = v; this.score = calculateScore(); }
    public float getScoreGK() { return scoreGK; }
    public void setScoreGK(float v) { this.scoreGK = v; this.score = calculateScore(); }
    public int getWeightGK() { return weightGK; }
    public void setWeightGK(int v) { this.weightGK = v; this.score = calculateScore(); }
    public float getScoreCK() { return scoreCK; }
    public void setScoreCK(float v) { this.scoreCK = v; this.score = calculateScore(); }
    public int getWeightCK() { return weightCK; }
    public void setWeightCK(int v) { this.weightCK = v; this.score = calculateScore(); }
}
