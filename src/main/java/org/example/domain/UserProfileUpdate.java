package org.example.domain;

// Eingabe-DTO für Profil-Updates (nur erlaubte Felder).
public class UserProfileUpdate {
    private String email;
    private String favoriteGenre;

    public UserProfileUpdate() {
    }

    public String getEmail() {
        return email;
    }

    public String getFavoriteGenre() {
        return favoriteGenre;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFavoriteGenre(String favoriteGenre) {
        this.favoriteGenre = favoriteGenre;
    }
}
