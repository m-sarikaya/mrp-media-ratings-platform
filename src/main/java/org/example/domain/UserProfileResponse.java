package org.example.domain;

// Ausgabe-DTO für Profil + Statistiken.
public class UserProfileResponse {
    private int id;
    private String username;
    private String email;
    private String favoriteGenre;
    private int totalRatings;
    private double averageScore;

    public UserProfileResponse() {
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }
}
