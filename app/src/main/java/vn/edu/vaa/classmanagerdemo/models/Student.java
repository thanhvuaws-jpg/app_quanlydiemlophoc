package vn.edu.vaa.classmanagerdemo.models;

public class Student {
    private int id;
    private String name;
    private String className;
    private String email;
    private String phone;
    private String studentCode;
    private int classId;

    public Student() { }

    public Student(int id, String name, String className, String email, String phone) {
        this.id = id;
        this.name = name;
        this.className = className;
        this.email = email;
        this.phone = phone;
    }

    public Student(String name, String className, String email, String phone) {
        this.name = name;
        this.className = className;
        this.email = email;
        this.phone = phone;
    }

    public Student(String studentCode, String name, int classId, String email, String phone, String className) {
        this.studentCode = studentCode;
        this.name = name;
        this.classId = classId;
        this.email = email;
        this.phone = phone;
        this.className = className;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getStudentCode() { return studentCode != null ? studentCode : ""; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    @Override
    public String toString() {
        return id + " - " + name + " - " + className;
    }
}
