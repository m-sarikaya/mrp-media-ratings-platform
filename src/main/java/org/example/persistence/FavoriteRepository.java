package org.example.persistence;

import org.example.domain.MediaEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Repository für Favoriten (Favorites-Tabelle).
public class FavoriteRepository {
    private final Connection connection;

    public FavoriteRepository(Connection connection) {
        this.connection = connection;
    }

    // Favorit speichern.
    public void addFavorite(int userId, int mediaId) throws SQLException {
        String sql = "INSERT INTO favorites (userid, mediaid) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            stmt.executeUpdate();
        }
    }

    // Favorit entfernen.
    public boolean removeFavorite(int userId, int mediaId) throws SQLException {
        String sql = "DELETE FROM favorites WHERE userid = ? AND mediaid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, mediaId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Favoriten eines Users laden.
    public List<MediaEntry> findFavoritesByUser(int userId) throws SQLException {
        String sql = "SELECT m.* FROM mediaentries m " +
                     "JOIN favorites f ON f.mediaid = m.id " +
                     "WHERE f.userid = ?";
        List<MediaEntry> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry media = new MediaEntry();
                media.setId(rs.getInt("id"));
                media.setTitle(rs.getString("title"));
                media.setDescription(rs.getString("description"));
                media.setMediaType(rs.getString("mediatype"));
                media.setReleaseYear(rs.getInt("releaseyear"));
                media.setAgeRestriction(rs.getInt("agerestriction"));
                media.setCreatorId(rs.getInt("creatorid"));
                list.add(media);
            }
        }
        return list;
    }
}
