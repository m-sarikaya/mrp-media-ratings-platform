package org.example.service;

import org.example.domain.Rating;
import org.example.persistence.RatingRepository;

import java.sql.SQLException;

// Business-Logik für Ratings.
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    // Rating erstellen (ein Rating pro User/Medium).
    public Rating createRating(int mediaId, int userId, int stars, String comment) throws Exception {
        validateStars(stars);

        Rating existing = ratingRepository.findByMediaAndUser(mediaId, userId);
        if (existing != null) {
            throw new Exception("Es existiert bereits ein Rating für dieses Medium");
        }

        Rating rating = new Rating();
        rating.setMediaId(mediaId);
        rating.setUserId(userId);
        rating.setStars(stars);
        rating.setComment(comment);
        rating.setCommentConfirmed(false);

        try {
            return ratingRepository.save(rating);
        } catch (SQLException e) {
            throw new Exception("Fehler beim Speichern des Ratings");
        }
    }

    // Rating aktualisieren (nur Owner).
    public Rating updateRating(int ratingId, int userId, int stars, String comment) throws Exception {
        validateStars(stars);

        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null) {
            throw new Exception("Rating nicht gefunden");
        }
        if (existing.getUserId() != userId) {
            throw new Exception("Nur der Ersteller darf das Rating ändern");
        }

        existing.setStars(stars);
        existing.setComment(comment);
        existing.setCommentConfirmed(false);

        try {
            return ratingRepository.update(existing);
        } catch (SQLException e) {
            throw new Exception("Fehler beim Aktualisieren des Ratings");
        }
    }

    // Rating löschen (nur Owner).
    public void deleteRating(int ratingId, int userId) throws Exception {
        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null) {
            throw new Exception("Rating nicht gefunden");
        }
        if (existing.getUserId() != userId) {
            throw new Exception("Nur der Ersteller darf das Rating löschen");
        }

        try {
            boolean deleted = ratingRepository.delete(ratingId);
            if (!deleted) {
                throw new Exception("Fehler beim Löschen");
            }
        } catch (SQLException e) {
            throw new Exception("Fehler beim Löschen");
        }
    }

    // Kommentar bestätigen (nur Owner).
    public void confirmComment(int ratingId, int userId) throws Exception {
        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null) {
            throw new Exception("Rating nicht gefunden");
        }
        if (existing.getUserId() != userId) {
            throw new Exception("Nur der Ersteller darf den Kommentar bestätigen");
        }

        try {
            ratingRepository.confirmComment(ratingId);
        } catch (SQLException e) {
            throw new Exception("Fehler beim Bestätigen");
        }
    }

    // Like setzen (nicht doppelt, nicht eigenes Rating).
    public void likeRating(int ratingId, int userId) throws Exception {
        Rating existing = ratingRepository.findById(ratingId);
        if (existing == null) {
            throw new Exception("Rating nicht gefunden");
        }
        try {
            ratingRepository.addLike(ratingId, userId);
        } catch (SQLException e) {
            throw new Exception("Like bereits gesetzt oder Fehler");
        }
    }

    // Ratings eines Users laden (History).
    public java.util.List<Rating> getRatingsByUser(int userId) throws Exception {
        try {
            return ratingRepository.findByUser(userId);
        } catch (SQLException e) {
            throw new Exception("Fehler beim Laden der Ratings");
        }
    }

    // Leaderboard laden.
    public java.util.List<org.example.domain.LeaderboardEntry> getLeaderboard() throws Exception {
        try {
            return ratingRepository.getLeaderboard();
        } catch (SQLException e) {
            throw new Exception("Fehler beim Laden des Leaderboards");
        }
    }

    // Hilfsregel: Sterne nur 1-5.
    private void validateStars(int stars) throws Exception {
        if (stars < 1 || stars > 5) {
            throw new Exception("Stars müssen zwischen 1 und 5 liegen");
        }
    }
}
