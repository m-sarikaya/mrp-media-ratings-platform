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
            stmt.setString(5, null);
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setInt(7, media.getCreatorId());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                media.setId(rs.getInt("id"));
            }

            saveGenres(media);

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
                MediaEntry media = mapResultSetToMedia(rs);
                media.setGenres(findGenresByMediaId(media.getId()));
                mediaList.add(media);
            }

        } catch (SQLException e) {
            System.out.println("Fehler beim Laden: " + e.getMessage());
        }

        return mediaList;
    }

    // ========================================
    // SEARCH - Medien mit Filtern und Sortierung
    // ========================================
    public List<MediaEntry> search(String title, String genre, String mediaType, Integer releaseYear,
                                   Integer ageRestriction, Double rating, String sortBy) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.* FROM mediaentries m ");

        // Join fuer Genre-Filter
        if (genre != null && !genre.isBlank()) {
            sql.append("JOIN media_genres mg ON mg.mediaid = m.id ");
        }

        // Durchschnittliche Ratings als Subquery (Filter + Sort)
        sql.append("LEFT JOIN (SELECT mediaid, AVG(stars) AS avgscore FROM ratings GROUP BY mediaid) r ");
        sql.append("ON r.mediaid = m.id ");

        sql.append("WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (title != null && !title.isBlank()) {
            sql.append("AND LOWER(m.title) LIKE ? ");
            params.add("%" + title.toLowerCase() + "%");
        }
        if (genre != null && !genre.isBlank()) {
            sql.append("AND LOWER(mg.genre) = ? ");
            params.add(genre.toLowerCase());
        }
        if (mediaType != null && !mediaType.isBlank()) {
            sql.append("AND LOWER(m.mediatype) = ? ");
            params.add(mediaType.toLowerCase());
        }
        if (releaseYear != null) {
            sql.append("AND m.releaseyear = ? ");
            params.add(releaseYear);
        }
        if (ageRestriction != null) {
            sql.append("AND m.agerestriction = ? ");
            params.add(ageRestriction);
        }
        if (rating != null) {
            sql.append("AND r.avgscore >= ? ");
            params.add(rating);
        }

        // Sortierung
        if (sortBy != null) {
            if (sortBy.equalsIgnoreCase("title")) {
                sql.append("ORDER BY m.title ASC ");
            } else if (sortBy.equalsIgnoreCase("year")) {
                sql.append("ORDER BY m.releaseyear ASC ");
            } else if (sortBy.equalsIgnoreCase("score")) {
                sql.append("ORDER BY r.avgscore DESC NULLS LAST ");
            }
        }

        List<MediaEntry> list = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry media = mapResultSetToMedia(rs);
                media.setGenres(findGenresByMediaId(media.getId()));
                list.add(media);
            }
        } catch (SQLException e) {
            System.out.println("Fehler bei der Suche: " + e.getMessage());
        }

        return list;
    }

    // ========================================
    // RECOMMENDATIONS - Genre-basiert
    // ========================================
    public List<MediaEntry> recommendByGenre(int userId) throws SQLException {
        String sql = "SELECT DISTINCT m.* " +
                     "FROM mediaentries m " +
                     "JOIN media_genres mg ON mg.mediaid = m.id " +
                     "WHERE mg.genre IN ( " +
                     "  SELECT mg2.genre " +
                     "  FROM ratings r " +
                     "  JOIN media_genres mg2 ON mg2.mediaid = r.mediaid " +
                     "  WHERE r.userid = ? AND r.stars >= 4 " +
                     ") " +
                     "AND m.id NOT IN (SELECT mediaid FROM ratings WHERE userid = ?)";
        List<MediaEntry> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry media = mapResultSetToMedia(rs);
                media.setGenres(findGenresByMediaId(media.getId()));
                list.add(media);
            }
        }
        return list;
    }

    // ========================================
    // RECOMMENDATIONS - Content-basiert
    // ========================================
    public List<MediaEntry> recommendByContent(int userId) throws SQLException {
        String sql = "SELECT DISTINCT m.* " +
                     "FROM mediaentries m " +
                     "JOIN media_genres mg ON mg.mediaid = m.id " +
                     "WHERE (m.mediatype, m.agerestriction) IN ( " +
                     "  SELECT m2.mediatype, m2.agerestriction " +
                     "  FROM ratings r2 " +
                     "  JOIN mediaentries m2 ON m2.id = r2.mediaid " +
                     "  WHERE r2.userid = ? AND r2.stars >= 4 " +
                     ") " +
                     "AND mg.genre IN ( " +
                     "  SELECT mg3.genre " +
                     "  FROM ratings r3 " +
                     "  JOIN media_genres mg3 ON mg3.mediaid = r3.mediaid " +
                     "  WHERE r3.userid = ? AND r3.stars >= 4 " +
                     ") " +
                     "AND m.id NOT IN (SELECT mediaid FROM ratings WHERE userid = ?)";
        List<MediaEntry> list = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MediaEntry media = mapResultSetToMedia(rs);
                media.setGenres(findGenresByMediaId(media.getId()));
                list.add(media);
            }
        }
        return list;
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
                MediaEntry media = mapResultSetToMedia(rs);
                media.setGenres(findGenresByMediaId(media.getId()));
                return media;
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
            stmt.setString(5, null);
            stmt.setInt(6, media.getAgeRestriction());
            stmt.setInt(7, media.getId());

            stmt.executeUpdate();

            deleteGenresByMediaId(media.getId());
            saveGenres(media);

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
        media.setAgeRestriction(rs.getInt("agerestriction"));
        media.setCreatorId(rs.getInt("creatorid"));
        return media;
    }

    // Genres fuer ein Medium speichern.
    private void saveGenres(MediaEntry media) throws SQLException {
        if (media.getGenres() == null) {
            return;
        }

        String sql = "INSERT INTO media_genres (mediaid, genre) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String genre : media.getGenres()) {
                if (genre == null || genre.isBlank()) {
                    continue;
                }
                stmt.setInt(1, media.getId());
                stmt.setString(2, genre);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    // Genres fuer ein Medium loeschen (z. B. vor Update).
    private void deleteGenresByMediaId(int mediaId) throws SQLException {
        String sql = "DELETE FROM media_genres WHERE mediaid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            stmt.executeUpdate();
        }
    }

    // Genres fuer ein Medium laden.
    private List<String> findGenresByMediaId(int mediaId) throws SQLException {
        String sql = "SELECT genre FROM media_genres WHERE mediaid = ?";
        List<String> genres = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mediaId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                genres.add(rs.getString("genre"));
            }
        }
        return genres;
    }
}
