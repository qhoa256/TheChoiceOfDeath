package common;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private int score;
    private String status;

    public User(int id, String username, int points, String status) {
        this.id = id;
        this.username = username;
        this.score = points;
        this.status = status;
    }

    // Getters và Setters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }

    public void setScore(int points) {
        this.score = points;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
