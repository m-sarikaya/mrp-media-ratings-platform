package org.example.domain;

// Ausgabe-DTO für Leaderboard-Einträge.
public class LeaderboardEntry {
    private int userId;
    private String username;
    private int ratingCount;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(int userId, String username, int ratingCount) {
        this.userId = userId;
        this.username = username;
        this.ratingCount = ratingCount;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }
}
