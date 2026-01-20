package org.example.service;

import org.example.domain.MediaEntry;
import org.example.persistence.FavoriteRepository;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Unit-Tests für FavoriteService (ohne echte Datenbank).
public class FavoriteServiceTest {

    @Test
    void addFavoriteSuccess() throws Exception {
        // Test: Favorit kann hinzugefügt werden.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        FavoriteService service = new FavoriteService(repo);

        service.addFavorite(1, 2);
        assertEquals(1, repo.favorites.size());
    }

    @Test
    void addFavoriteFailsWhenDuplicate() {
        // Test: Doppelter Favorit führt zu Fehler.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        repo.throwOnAdd = true;
        FavoriteService service = new FavoriteService(repo);

        Exception ex = assertThrows(Exception.class, () -> service.addFavorite(1, 2));
        assertEquals("Favorit existiert bereits oder Fehler", ex.getMessage());
    }

    @Test
    void removeFavoriteSuccess() throws Exception {
        // Test: Favorit kann entfernt werden.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        repo.favorites.add("1:2");
        FavoriteService service = new FavoriteService(repo);

        service.removeFavorite(1, 2);
        assertEquals(0, repo.favorites.size());
    }

    @Test
    void removeFavoriteFailsWhenMissing() {
        // Test: Entfernen eines nicht vorhandenen Favoriten.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        FavoriteService service = new FavoriteService(repo);

        Exception ex = assertThrows(Exception.class, () -> service.removeFavorite(1, 2));
        assertEquals("Favorit nicht gefunden", ex.getMessage());
    }

    @Test
    void favoritesListEmpty() throws Exception {
        // Test: Favoritenliste ist leer.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        FavoriteService service = new FavoriteService(repo);

        List<MediaEntry> list = service.getFavoritesByUser(1);
        assertTrue(list.isEmpty());
    }

    @Test
    void favoritesListWithEntries() throws Exception {
        // Test: Favoritenliste enthält Einträge.
        FakeFavoriteRepository repo = new FakeFavoriteRepository();
        repo.favoriteMedia.add(new MediaEntry());
        FavoriteService service = new FavoriteService(repo);

        List<MediaEntry> list = service.getFavoritesByUser(1);
        assertEquals(1, list.size());
    }

    // Fake-Repository für Favoriten (In-Memory).
    static class FakeFavoriteRepository extends FavoriteRepository {
        boolean throwOnAdd = false;
        List<String> favorites = new ArrayList<>();
        List<MediaEntry> favoriteMedia = new ArrayList<>();

        FakeFavoriteRepository() {
            super((Connection) null);
        }

        @Override
        public void addFavorite(int userId, int mediaId) throws SQLException {
            if (throwOnAdd) {
                throw new SQLException("duplicate");
            }
            favorites.add(userId + ":" + mediaId);
        }

        @Override
        public boolean removeFavorite(int userId, int mediaId) {
            return favorites.remove(userId + ":" + mediaId);
        }

        @Override
        public List<MediaEntry> findFavoritesByUser(int userId) {
            return favoriteMedia;
        }
    }
}
