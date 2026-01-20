package org.example.service;

import org.example.persistence.FavoriteRepository;

import java.sql.SQLException;

// Business-Logik für Favoriten.
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    // Favorit setzen (doppelte verhindern).
    public void addFavorite(int userId, int mediaId) throws Exception {
        try {
            favoriteRepository.addFavorite(userId, mediaId);
        } catch (SQLException e) {
            throw new Exception("Favorit existiert bereits oder Fehler");
        }
    }

    // Favorit entfernen.
    public void removeFavorite(int userId, int mediaId) throws Exception {
        try {
            boolean removed = favoriteRepository.removeFavorite(userId, mediaId);
            if (!removed) {
                throw new Exception("Favorit nicht gefunden");
            }
        } catch (SQLException e) {
            throw new Exception("Fehler beim Entfernen");
        }
    }

    // Favoriten für einen User laden.
    public java.util.List<org.example.domain.MediaEntry> getFavoritesByUser(int userId) throws Exception {
        try {
            return favoriteRepository.findFavoritesByUser(userId);
        } catch (SQLException e) {
            throw new Exception("Fehler beim Laden der Favoriten");
        }
    }
}
