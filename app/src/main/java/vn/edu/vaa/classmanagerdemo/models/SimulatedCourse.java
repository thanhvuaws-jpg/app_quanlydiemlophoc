package vn.edu.vaa.classmanagerdemo.models;

public class SimulatedCourse {
    private String subjectName;
    private int credits;
    private float score10;

    public SimulatedCourse(String subjectName, int credits, float score10) {
        this.subjectName = subjectName;
        this.credits = credits;
        this.score10 = score10;
    }

    public String getSubjectName() { return subjectName; }
    public int getCredits() { return credits; }
    public float getScore10() { return score10; }

    public float getGrade4() {
        if (score10 >= 8.5f) return 4.0f;
        if (score10 >= 8.0f) return 3.5f;
        if (score10 >= 7.0f) return 3.0f;
        if (score10 >= 6.5f) return 2.5f;
        if (score10 >= 5.5f) return 2.0f;
        if (score10 >= 5.0f) return 1.5f;
        if (score10 >= 4.0f) return 1.0f;
        return 0.0f;
    }
}
