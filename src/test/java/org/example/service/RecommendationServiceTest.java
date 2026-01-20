package org.example.service;

import org.example.domain.MediaEntry;
import org.example.persistence.MediaRepository;
import org.example.persistence.RatingRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit-Tests für RecommendationService (ohne echte Datenbank).
public class RecommendationServiceTest {

    @Test
    void recommendByGenreReturnsList() throws Exception {
        // Test: Genre-Empfehlungen liefern Ergebnisse.
        FakeMediaRepository mediaRepo = new FakeMediaRepository();
        mediaRepo.genreList.add(new MediaEntry());
        RecommendationService service = new RecommendationService(mediaRepo, new FakeRatingRepository());

        List<MediaEntry> list = service.recommendByGenre(1);
        assertEquals(1, list.size());
    }

    @Test
    void recommendByContentReturnsList() throws Exception {
        // Test: Content-Empfehlungen liefern Ergebnisse.
        FakeMediaRepository mediaRepo = new FakeMediaRepository();
        mediaRepo.contentList.add(new MediaEntry());
        RecommendationService service = new RecommendationService(mediaRepo, new FakeRatingRepository());

        List<MediaEntry> list = service.recommendByContent(1);
        assertEquals(1, list.size());
    }

    @Test
    void recommendByGenreEmpty() throws Exception {
        // Test: Genre-Empfehlungen können leer sein.
        RecommendationService service = new RecommendationService(new FakeMediaRepository(), new FakeRatingRepository());
        List<MediaEntry> list = service.recommendByGenre(1);
        assertTrue(list.isEmpty());
    }

    @Test
    void recommendByContentEmpty() throws Exception {
        // Test: Content-Empfehlungen können leer sein.
        RecommendationService service = new RecommendationService(new FakeMediaRepository(), new FakeRatingRepository());
        List<MediaEntry> list = service.recommendByContent(1);
        assertTrue(list.isEmpty());
    }

    @Test
    void recommendByGenreHandlesError() {
        // Test: Fehler im Repository wird als Exception geworfen.
        FakeMediaRepository mediaRepo = new FakeMediaRepository();
        mediaRepo.throwOnGenre = true;
        RecommendationService service = new RecommendationService(mediaRepo, new FakeRatingRepository());

        Exception ex = assertThrows(Exception.class, () -> service.recommendByGenre(1));
        assertEquals("Fehler bei Genre-Empfehlungen", ex.getMessage());
    }

    @Test
    void recommendByContentHandlesError() {
        // Test: Fehler im Repository wird als Exception geworfen.
        FakeMediaRepository mediaRepo = new FakeMediaRepository();
        mediaRepo.throwOnContent = true;
        RecommendationService service = new RecommendationService(mediaRepo, new FakeRatingRepository());

        Exception ex = assertThrows(Exception.class, () -> service.recommendByContent(1));
        assertEquals("Fehler bei Content-Empfehlungen", ex.getMessage());
    }

    // Fake-Repository für Media (In-Memory).
    static class FakeMediaRepository extends MediaRepository {
        List<MediaEntry> genreList = new ArrayList<>();
        List<MediaEntry> contentList = new ArrayList<>();
        boolean throwOnGenre = false;
        boolean throwOnContent = false;

        FakeMediaRepository() {
            super((Connection) null);
        }

        @Override
        public List<MediaEntry> recommendByGenre(int userId) throws SQLException {
            if (throwOnGenre) {
                throw new SQLException("fail");
            }
            return genreList;
        }

        @Override
        public List<MediaEntry> recommendByContent(int userId) throws SQLException {
            if (throwOnContent) {
                throw new SQLException("fail");
            }
            return contentList;
        }
    }

    // Fake-Repository für Ratings (nicht benötigt, aber als Stub vorhanden).
    static class FakeRatingRepository extends RatingRepository {
        FakeRatingRepository() {
            super((Connection) null);
        }
    }
}
