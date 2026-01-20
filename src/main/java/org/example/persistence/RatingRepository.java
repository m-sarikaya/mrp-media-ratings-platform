package org.example.persistence;

import org.example.domain.Rating;
import org.example.domain.LeaderboardEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Repository für Ratings und Rating-Likes.
public class RatingRepository {
    private final Connection connection;

    public RatingRepository(Connection connection) {
        this.connection = connection;
    }

    // Neues Rating speichern.
    public Rating save(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (mediaid, userid, stars, comment) VALUES (?, ?, ?, ?) RETURNING id, createdat, commentconfirmed";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getMediaId());
            stmt.setInt(2, rating.getUserId());
            stmt.setInt(3, rating.getStars());
            stmt.setString(4, rating.getComment());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                rating.setId(rs.getInt("id"));
                rating.setCreatedAt(rs.getTimestamp("createdat").toLocalDateTime());
                rating.setCommentConfirmed(rs.getBoolean("commentconfirmed"));
            }
            return rating;
        }
    }

    // Rating nach ID holen.
    public Rating findById(int ratingId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRating(rs);
            }
            return null;
        }
    }

    // Rating nach Media und User holen (1 Rating pro User/Medium).
    public Rating findByMediaAndUser(int mediaId, int userId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE mediaid = ? AND userid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRating(rs);
            }
            return null;
        }
    }

    // Rating aktualisieren (Sterne + Kommentar).
    public Rating update(Rating rating) throws SQLException {
        String sql = "UPDATE ratings SET stars = ?, comment = ?, commentconfirmed = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, rating.getStars());
            stmt.setString(2, rating.getComment());
            stmt.setBoolean(3, rating.isCommentConfirmed());
            stmt.setInt(4, rating.getId());
            stmt.executeUpdate();
            return rating;
        }
    }

    // Kommentar bestätigen.
    public void confirmComment(int ratingId) throws SQLException {
        String sql = "UPDATE ratings SET commentconfirmed = TRUE WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            stmt.executeUpdate();
        }
    }

    // Rating löschen.
    public boolean delete(int ratingId) throws SQLException {
        String sql = "DELETE FROM ratings WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Rating-Like speichern.
    public void addLike(int ratingId, int userId) throws SQLException {
        String sql = "INSERT INTO rating_likes (ratingid, userid) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    // Rating-Likes zählen.
    public int countLikes(int ratingId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM rating_likes WHERE ratingid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, ratingId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            return 0;
        }
    }

    // Ratings fuer einen User (History).
    public List<Rating> findByUser(int userId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE userid = ? ORDER BY createdat DESC";
        List<Rating> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapRating(rs));
            }
        }
        return list;
    }

    // Anzahl Ratings eines Users.
    public int countByUser(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM ratings WHERE userid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            return 0;
        }
    }

    // Durchschnittliche Sterne eines Users.
    public double averageByUser(int userId) throws SQLException {
        String sql = "SELECT AVG(stars) AS avgscore FROM ratings WHERE userid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avgscore");
            }
            return 0.0;
        }
    }

    // Leaderboard: User nach Anzahl Ratings.
    public java.util.List<LeaderboardEntry> getLeaderboard() throws SQLException {
        String sql = "SELECT u.id, u.username, COUNT(r.id) AS ratingcount " +
                     "FROM users u " +
                     "LEFT JOIN ratings r ON r.userid = u.id " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY ratingcount DESC";
        java.util.List<LeaderboardEntry> list = new java.util.ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new LeaderboardEntry(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getInt("ratingcount")
                ));
            }
        }
        return list;
    }

    private Rating mapRating(ResultSet rs) throws SQLException {
        Rating rating = new Rating();
        rating.setId(rs.getInt("id"));
        rating.setMediaId(rs.getInt("mediaid"));
        rating.setUserId(rs.getInt("userid"));
        rating.setStars(rs.getInt("stars"));
        rating.setComment(rs.getString("comment"));
        rating.setCommentConfirmed(rs.getBoolean("commentconfirmed"));
        Timestamp ts = rs.getTimestamp("createdat");
        if (ts != null) {
            rating.setCreatedAt(ts.toLocalDateTime());
        }
        return rating;
    }
}
