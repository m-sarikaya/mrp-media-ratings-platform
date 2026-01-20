package org.example.service;

import org.example.domain.MediaEntry;
import org.example.persistence.MediaRepository;
import org.example.persistence.RatingRepository;

import java.util.List;

// Business-Logik für Empfehlungen.
public class RecommendationService {
    private final MediaRepository mediaRepository;
    private final RatingRepository ratingRepository;

    public RecommendationService(MediaRepository mediaRepository, RatingRepository ratingRepository) {
        this.mediaRepository = mediaRepository;
        this.ratingRepository = ratingRepository;
    }

    // Empfehlungen basierend auf Genres (hoch bewertete Medien).
    public List<MediaEntry> recommendByGenre(int userId) throws Exception {
        try {
            return mediaRepository.recommendByGenre(userId);
        } catch (Exception e) {
            throw new Exception("Fehler bei Genre-Empfehlungen");
        }
    }

    // Empfehlungen basierend auf Content-Ähnlichkeit.
    public List<MediaEntry> recommendByContent(int userId) throws Exception {
        try {
            return mediaRepository.recommendByContent(userId);
        } catch (Exception e) {
            throw new Exception("Fehler bei Content-Empfehlungen");
        }
    }
}
