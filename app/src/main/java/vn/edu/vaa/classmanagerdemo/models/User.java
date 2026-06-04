package vn.edu.vaa.classmanagerdemo.models;

public class User {
    private int id;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private int trainingPoints;

    public User() { }

    public User(int id, String fullName, String username, String password, String email, String phone, int trainingPoints) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.trainingPoints = trainingPoints;
    }

    public User(String fullName, String username, String password, String email, String phone) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.trainingPoints = 80; // default value
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getTrainingPoints() { return trainingPoints; }
    public void setTrainingPoints(int trainingPoints) { this.trainingPoints = trainingPoints; }
}
