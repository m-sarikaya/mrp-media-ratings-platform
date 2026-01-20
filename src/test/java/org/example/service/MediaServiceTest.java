package org.example.service;

import org.example.domain.MediaEntry;
import org.example.persistence.MediaRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// Unit-Tests für MediaService (ohne echte Datenbank).
public class MediaServiceTest {

    @Test
    void createFailsWhenTitleMissing() {
        // Test: Ohne Titel darf kein Medium erstellt werden.
        MediaService service = new MediaService(new FakeMediaRepository());
        MediaEntry media = baseMedia();
        media.setTitle(null);

        Exception ex = assertThrows(Exception.class, () -> service.create(media, 1));
        assertEquals("Titel ist erforderlich", ex.getMessage());
    }

    @Test
    void createFailsWhenMediaTypeMissing() {
        // Test: Ohne MediaType darf kein Medium erstellt werden.
        MediaService service = new MediaService(new FakeMediaRepository());
        MediaEntry media = baseMedia();
        media.setMediaType(null);

        Exception ex = assertThrows(Exception.class, () -> service.create(media, 1));
        assertEquals("MediaType ist erforderlich (MOVIE, SERIES, GAME)", ex.getMessage());
    }

    @Test
    void createFailsWhenMediaTypeInvalid() {
        // Test: Ungültiger MediaType wird abgelehnt.
        MediaService service = new MediaService(new FakeMediaRepository());
        MediaEntry media = baseMedia();
        media.setMediaType("BOOK");

        Exception ex = assertThrows(Exception.class, () -> service.create(media, 1));
        assertEquals("MediaType muss MOVIE, SERIES oder GAME sein", ex.getMessage());
    }

    @Test
    void createFailsWhenGenresMissing() {
        // Test: Mindestens ein Genre ist erforderlich.
        MediaService service = new MediaService(new FakeMediaRepository());
        MediaEntry media = baseMedia();
        media.setGenres(new ArrayList<>());

        Exception ex = assertThrows(Exception.class, () -> service.create(media, 1));
        assertEquals("Mindestens ein Genre ist erforderlich", ex.getMessage());
    }

    @Test
    void createFailsWhenGenresBlank() {
        // Test: Leere Genre-Einträge gelten als fehlend.
        MediaService service = new MediaService(new FakeMediaRepository());
        MediaEntry media = baseMedia();
        media.setGenres(List.of(" "));

        Exception ex = assertThrows(Exception.class, () -> service.create(media, 1));
        assertEquals("Mindestens ein Genre ist erforderlich", ex.getMessage());
    }

    @Test
    void updateFailsWhenUserNotOwner() {
        // Test: Nur der Ersteller darf das Medium updaten.
        FakeMediaRepository repo = new FakeMediaRepository();
        MediaService service = new MediaService(repo);

        MediaEntry existing = baseMedia();
        existing.setCreatorId(1);
        repo.save(existing);

        MediaEntry update = baseMedia();
        Exception ex = assertThrows(Exception.class, () -> service.update(existing.getId(), update, 2));
        assertEquals("Nur der Ersteller kann das Medium bearbeiten", ex.getMessage());
    }

    @Test
    void deleteFailsWhenUserNotOwner() {
        // Test: Nur der Ersteller darf das Medium löschen.
        FakeMediaRepository repo = new FakeMediaRepository();
        MediaService service = new MediaService(repo);

        MediaEntry existing = baseMedia();
        existing.setCreatorId(1);
        repo.save(existing);

        Exception ex = assertThrows(Exception.class, () -> service.delete(existing.getId(), 2));
        assertEquals("Nur der Ersteller kann das Medium löschen", ex.getMessage());
    }

    @Test
    void updateAppliesFields() throws Exception {
        // Test: Update übernimmt neue Felder.
        FakeMediaRepository repo = new FakeMediaRepository();
        MediaService service = new MediaService(repo);

        MediaEntry existing = baseMedia();
        existing.setCreatorId(1);
        repo.save(existing);

        MediaEntry update = new MediaEntry();
        update.setTitle("Neu");
        update.setDescription("NeuDesc");
        update.setMediaType("MOVIE");
        update.setReleaseYear(2020);
        update.setGenres(List.of("SCI-FI"));
        update.setAgeRestriction(16);

        MediaEntry result = service.update(existing.getId(), update, 1);
        assertEquals("Neu", result.getTitle());
        assertEquals("NeuDesc", result.getDescription());
        assertEquals(2020, result.getReleaseYear());
        assertEquals(16, result.getAgeRestriction());
        assertEquals(List.of("SCI-FI"), result.getGenres());
    }

    private MediaEntry baseMedia() {
        MediaEntry media = new MediaEntry();
        media.setTitle("Test");
        media.setDescription("Desc");
        media.setMediaType("MOVIE");
        media.setReleaseYear(2010);
        media.setGenres(List.of("SCI-FI"));
        media.setAgeRestriction(12);
        return media;
    }

    // Fake-Repository für Media (In-Memory).
    static class FakeMediaRepository extends MediaRepository {
        private final Map<Integer, MediaEntry> byId = new HashMap<>();
        private int nextId = 1;

        FakeMediaRepository() {
            super((Connection) null);
        }

        @Override
        public MediaEntry save(MediaEntry media) {
            if (media.getId() == 0) {
                media.setId(nextId++);
            }
            byId.put(media.getId(), media);
            return media;
        }

        @Override
        public MediaEntry findById(int id) {
            return byId.get(id);
        }

        @Override
        public MediaEntry update(MediaEntry media) {
            byId.put(media.getId(), media);
            return media;
        }

        @Override
        public boolean delete(int id) {
            return byId.remove(id) != null;
        }
    }
}
