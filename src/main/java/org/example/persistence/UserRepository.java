package org.example.persistence;

import org.example.domain.User;

import java.sql.*;

public class UserRepository {

    // Die Verbindung zur Datenbank
    private final Connection connection;

    // Constructor: Bekommt die Datenbankverbindung
    public UserRepository(Connection connection) {
        this.connection = connection;
    }


    // SAVE - Neuen User speichern (für Registration)

    public User save(User user) {
        // SQL Befehl: Füge neuen User ein
        // RETURNING id = gib die generierte ID zurück
        String sql = "INSERT INTO users (username, password) VALUES (?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // ? durch echte Werte ersetzen
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());

            // Ausführen und Ergebnis holen
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Die generierte ID setzen
                user.setId(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
            return null;
        }

        return user;
    }


    // FIND BY USERNAME - User anhand Username finden (für Login)

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            // Wenn User gefunden wurde
            if (rs.next()) {
                return User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .token(rs.getString("token"))
                        .email(rs.getString("email"))
                        .favoriteGenre(rs.getString("favoritegenre"))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Suchen: " + e.getMessage());
        }

        // Kein User gefunden
        return null;
    }


    // UPDATE TOKEN - Token speichern nach Login

    public void updateToken(User user) {
        String sql = "UPDATE users SET token = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getToken());
            stmt.setInt(2, user.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Token-Update: " + e.getMessage());
        }
    }


    // FIND BY TOKEN - User anhand Token finden (für Authentifizierung)

    public User findByToken(String token) {
        String sql = "SELECT * FROM users WHERE token = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .token(rs.getString("token"))
                        .email(rs.getString("email"))
                        .favoriteGenre(rs.getString("favoritegenre"))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Token-Suchen: " + e.getMessage());
        }

        return null;
    }

    // User anhand ID finden (Profil).
    public User findById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return User.builder()
                        .id(rs.getInt("id"))
                        .username(rs.getString("username"))
                        .password(rs.getString("password"))
                        .token(rs.getString("token"))
                        .email(rs.getString("email"))
                        .favoriteGenre(rs.getString("favoritegenre"))
                        .build();
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Suchen: " + e.getMessage());
        }

        return null;
    }

    // Profilfelder aktualisieren.
    public void updateProfile(int userId, String email, String favoriteGenre) {
        String sql = "UPDATE users SET email = ?, favoritegenre = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, favoriteGenre);
            stmt.setInt(3, userId);

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Profil-Update: " + e.getMessage());
        }
    }
}
