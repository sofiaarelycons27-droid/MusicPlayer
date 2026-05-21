// models/User.java
package com.musicplayer.models;

public class User {
    private int id;
    private String email;
    private String username;
    private String password;
    private String joinDate;

    public User(int id, String email, String username, String password, String joinDate) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.joinDate = joinDate;
    }

    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }
}