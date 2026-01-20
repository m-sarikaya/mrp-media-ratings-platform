package org.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private int id;
    private String username;
    private String email;
    private String favoriteGenre;

    //kann empfangen, aber nie gesendet werden (Sicherheit)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    //wird ignoriert in JSON
    @JsonIgnore
    private String token;

    public User() {
    }

    // ===== GETTER =====

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

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    // ===== SETTER =====

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

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // ===== toString =====

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }

    // ===== BUILDER =====

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private final User user = new User();

        public UserBuilder id(int id) {
            user.id = id;
            return this;
        }

        public UserBuilder username(String username) {
            user.username = username;
            return this;
        }

        public UserBuilder password(String password) {
            user.password = password;
            return this;
        }

        public UserBuilder token(String token) {
            user.token = token;
            return this;
        }

        public UserBuilder email(String email) {
            user.email = email;
            return this;
        }

        public UserBuilder favoriteGenre(String favoriteGenre) {
            user.favoriteGenre = favoriteGenre;
            return this;
        }

        public User build() {
            return user;
        }
    }
}
