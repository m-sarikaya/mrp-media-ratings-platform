package org.example.persistence;

import org.example.domain.MediaEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaRepository {

    private final Connection connection;

    public MediaRepository(Connection connection) {
        this.connection = connection;
    }

    // ========================================
    // SAVE - Neues Media speichern
    // ========================================
    public MediaEntry save(MediaEntry media) {
        String sql = "INSERT INTO mediaentries (title, description, mediatype, releaseyear, genre, agerestriction, creatorid) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getReleaseYear());
            stmt.setString(5, media.getGenre());
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setInt(7, media.getCreatorId());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                media.setId(rs.getInt("id"));
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Speichern: " + e.getMessage());
            return null;
        }

        return media;
    }

    // ========================================
    // FIND ALL - Alle Medien holen
    // ========================================
    public List<MediaEntry> findAll() {
        String sql = "SELECT * FROM mediaentries";
        List<MediaEntry> mediaList = new ArrayList<>();

        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                mediaList.add(mapResultSetToMedia(rs));
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }

        return mediaList;
    }

    // ========================================
    // FIND BY ID - Ein Medium anhand ID finden
    // ========================================
    public MediaEntry findById(int id) {
        String sql = "SELECT * FROM mediaentries WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToMedia(rs);
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Suchen: " + e.getMessage());
        }

        return null;
    }

    // ========================================
    // UPDATE - Medium aktualisieren
    // ========================================
    public MediaEntry update(MediaEntry media) {
        String sql = "UPDATE mediaentries SET title=?, description=?, mediatype=?, releaseyear=?, genre=?, agerestriction=? WHERE id=?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, media.getTitle());
            stmt.setString(2, media.getDescription());
            stmt.setString(3, media.getMediaType());
            stmt.setInt(4, media.getReleaseYear());
            stmt.setString(5, media.getGenre());
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setInt(7, media.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Fehler beim Update: " + e.getMessage());
            return null;
        }

        return media;
    }

    // ========================================
    // DELETE - Medium löschen
    // ========================================
    public boolean delete(int id) {
        String sql = "DELETE FROM mediaentries WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Fehler beim Löschen: " + e.getMessage());
            return false;
        }
    }

    // ========================================
    // HILFSMETHODE - ResultSet zu MediaEntry umwandeln
    // ========================================
    private MediaEntry mapResultSetToMedia(ResultSet rs) throws SQLException {
        MediaEntry media = new MediaEntry();
        media.setId(rs.getInt("id"));
        media.setTitle(rs.getString("title"));
        media.setDescription(rs.getString("description"));
        media.setMediaType(rs.getString("mediatype"));
        media.setReleaseYear(rs.getInt("releaseyear"));
        media.setGenre(rs.getString("genre"));
        media.setAgeRestriction(rs.getInt("agerestriction"));
        media.setCreatorId(rs.getInt("creatorid"));
        return media;
    }
}
