package vn.edu.vaa.classmanagerdemo.models;

public class Student {
    private int id;
    private int classId;
    private String studentCode;
    private String fullName;
    private String note = "";

    public Student() {}

    public Student(int classId, String studentCode, String fullName) {
        this.classId = classId;
        this.studentCode = studentCode;
        this.fullName = fullName;
    }

    public Student(int id, int classId, String studentCode, String fullName) {
        this.id = id;
        this.classId = classId;
        this.studentCode = studentCode;
        this.fullName = fullName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getInitials() {
        if (fullName == null || fullName.isEmpty()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1].substring(0, 1).toUpperCase();
    }

    public String getNote() { return note != null ? note : ""; }
    public void setNote(String note) { this.note = note; }
}
