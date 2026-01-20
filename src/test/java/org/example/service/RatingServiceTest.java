package org.example.service;

import org.example.domain.Rating;
import org.example.persistence.RatingRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Unit-Tests für RatingService (ohne echte Datenbank).
public class RatingServiceTest {

    @Test
    void createFailsWhenStarsTooLow() {
        // Test: Sterne unter 1 sind ungültig.
        RatingService service = new RatingService(new FakeRatingRepository());
        Exception ex = assertThrows(Exception.class, () -> service.createRating(1, 1, 0, "x"));
        assertEquals("Stars müssen zwischen 1 und 5 liegen", ex.getMessage());
    }

    @Test
    void createFailsWhenStarsTooHigh() {
        // Test: Sterne über 5 sind ungültig.
        RatingService service = new RatingService(new FakeRatingRepository());
        Exception ex = assertThrows(Exception.class, () -> service.createRating(1, 1, 6, "x"));
        assertEquals("Stars müssen zwischen 1 und 5 liegen", ex.getMessage());
    }

    @Test
    void createFailsWhenDuplicateRating() {
        // Test: Pro User/Medium nur ein Rating.
        FakeRatingRepository repo = new FakeRatingRepository();
        Rating existing = new Rating();
        existing.setId(1);
        existing.setMediaId(1);
        existing.setUserId(1);
        existing.setStars(5);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.createRating(1, 1, 4, "x"));
        assertEquals("Es existiert bereits ein Rating für dieses Medium", ex.getMessage());
    }

    @Test
    void updateFailsWhenRatingMissing() {
        // Test: Update ohne vorhandenes Rating.
        RatingService service = new RatingService(new FakeRatingRepository());
        Exception ex = assertThrows(Exception.class, () -> service.updateRating(99, 1, 4, "x"));
        assertEquals("Rating nicht gefunden", ex.getMessage());
    }

    @Test
    void updateFailsWhenNotOwner() {
        // Test: Nur Owner darf update.
        FakeRatingRepository repo = new FakeRatingRepository();
        Rating existing = new Rating();
        existing.setId(1);
        existing.setUserId(1);
        existing.setMediaId(1);
        existing.setStars(5);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.updateRating(1, 2, 4, "x"));
        assertEquals("Nur der Ersteller darf das Rating ändern", ex.getMessage());
    }

    @Test
    void deleteFailsWhenMissing() {
        // Test: Löschen ohne Rating.
        RatingService service = new RatingService(new FakeRatingRepository());
        Exception ex = assertThrows(Exception.class, () -> service.deleteRating(99, 1));
        assertEquals("Rating nicht gefunden", ex.getMessage());
    }

    @Test
    void deleteFailsWhenNotOwner() {
        // Test: Nur Owner darf löschen.
        FakeRatingRepository repo = new FakeRatingRepository();
        Rating existing = new Rating();
        existing.setId(1);
        existing.setUserId(1);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.deleteRating(1, 2));
        assertEquals("Nur der Ersteller darf das Rating löschen", ex.getMessage());
    }

    @Test
    void confirmFailsWhenNotOwner() {
        // Test: Nur Owner darf Kommentar bestätigen.
        FakeRatingRepository repo = new FakeRatingRepository();
        Rating existing = new Rating();
        existing.setId(1);
        existing.setUserId(1);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.confirmComment(1, 2));
        assertEquals("Nur der Ersteller darf den Kommentar bestätigen", ex.getMessage());
    }

    @Test
    void likeFailsWhenRatingMissing() {
        // Test: Like ohne Rating.
        RatingService service = new RatingService(new FakeRatingRepository());
        Exception ex = assertThrows(Exception.class, () -> service.likeRating(99, 1));
        assertEquals("Rating nicht gefunden", ex.getMessage());
    }

    @Test
    void likeFailsForOwnRating() {
        // Test: Eigenes Rating darf nicht geliked werden.
        FakeRatingRepository repo = new FakeRatingRepository();
        Rating existing = new Rating();
        existing.setId(1);
        existing.setUserId(1);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.likeRating(1, 1));
        assertEquals("Eigenes Rating kann nicht geliked werden", ex.getMessage());
    }

    @Test
    void likeFailsWhenDuplicate() {
        // Test: Like darf nicht doppelt sein.
        FakeRatingRepository repo = new FakeRatingRepository();
        repo.likeThrows = true;
        Rating existing = new Rating();
        existing.setId(1);
        existing.setUserId(2);
        repo.save(existing);

        RatingService service = new RatingService(repo);
        Exception ex = assertThrows(Exception.class, () -> service.likeRating(1, 1));
        assertEquals("Like bereits gesetzt oder Fehler", ex.getMessage());
    }

    @Test
    void createRatingSuccess() throws Exception {
        // Test: Rating kann erfolgreich erstellt werden.
        FakeRatingRepository repo = new FakeRatingRepository();
        RatingService service = new RatingService(repo);

        Rating rating = service.createRating(1, 1, 5, "Top");
        assertEquals(1, rating.getMediaId());
        assertEquals(1, rating.getUserId());
        assertEquals(5, rating.getStars());
        assertEquals("Top", rating.getComment());
    }

    // Fake-Repository für Ratings (In-Memory).
    static class FakeRatingRepository extends RatingRepository {
        private final Map<Integer, Rating> byId = new HashMap<>();
        boolean likeThrows = false;
        private int nextId = 1;

        FakeRatingRepository() {
            super((Connection) null);
        }

        @Override
        public Rating save(Rating rating) {
            if (rating.getId() == 0) {
                rating.setId(nextId++);
            }
            byId.put(rating.getId(), rating);
            return rating;
        }

        @Override
        public Rating findById(int ratingId) {
            return byId.get(ratingId);
        }

        @Override
        public Rating findByMediaAndUser(int mediaId, int userId) {
            return byId.values().stream()
                    .filter(r -> r.getMediaId() == mediaId && r.getUserId() == userId)
                    .findFirst()
                    .orElse(null);
        }

        @Override
        public Rating update(Rating rating) {
            byId.put(rating.getId(), rating);
            return rating;
        }

        @Override
        public boolean delete(int ratingId) {
            return byId.remove(ratingId) != null;
        }

        @Override
        public void confirmComment(int ratingId) {
            Rating rating = byId.get(ratingId);
            if (rating != null) {
                rating.setCommentConfirmed(true);
            }
        }

        @Override
        public void addLike(int ratingId, int userId) throws SQLException {
            if (likeThrows) {
                throw new SQLException("duplicate");
            }
        }
    }
}
