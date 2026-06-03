package vn.edu.vaa.classmanagerdemo.models;

public class ClassRoom {
    private int id;
    private String name;
    private String schoolYear;
    private int studentCount;

    public ClassRoom() {}

    public ClassRoom(int id, String name, String schoolYear) {
        this.id = id;
        this.name = name;
        this.schoolYear = schoolYear;
    }

    public ClassRoom(String name, String schoolYear) {
        this.name = name;
        this.schoolYear = schoolYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    @Override
    public String toString() { return name; }
}
