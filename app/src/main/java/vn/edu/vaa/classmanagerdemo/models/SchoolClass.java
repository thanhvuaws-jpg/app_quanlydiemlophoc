package vn.edu.vaa.classmanagerdemo.models;

public class SchoolClass {
    private int id;
    private int teacherId;
    private String className;
    private String subject;
    private String schoolYear;
    private int studentCount;

    public SchoolClass() {}

    public SchoolClass(int teacherId, String className, String subject, String schoolYear) {
        this.teacherId = teacherId;
        this.className = className;
        this.subject = subject;
        this.schoolYear = schoolYear;
    }

    public SchoolClass(int id, int teacherId, String className, String subject, String schoolYear) {
        this.id = id;
        this.teacherId = teacherId;
        this.className = className;
        this.subject = subject;
        this.schoolYear = schoolYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
