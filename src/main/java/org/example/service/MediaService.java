package org.example.service;

import org.example.domain.MediaEntry;
import org.example.persistence.MediaRepository;

import java.util.List;

public class MediaService {

    private final MediaRepository mediaRepository;

    public MediaService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    // ========================================
    // CREATE - Neues Medium erstellen
    // ========================================
    public MediaEntry create(MediaEntry media, int creatorId) throws Exception {

        // 1. Validierung
        if (media.getTitle() == null || media.getTitle().isEmpty()) {
            throw new Exception("Titel ist erforderlich");
        }

        if (media.getMediaType() == null || media.getMediaType().isEmpty()) {
            throw new Exception("MediaType ist erforderlich (MOVIE, SERIES, GAME)");
        }

        // 2. MediaType prüfen
        String type = media.getMediaType().toUpperCase();
        if (!type.equals("MOVIE") && !type.equals("SERIES") && !type.equals("GAME")) {
            throw new Exception("MediaType muss MOVIE, SERIES oder GAME sein");
        }
        media.setMediaType(type);

        // 3. Creator setzen
        media.setCreatorId(creatorId);

        // 4. Speichern
        MediaEntry saved = mediaRepository.save(media);

        if (saved == null) {
            throw new Exception("Fehler beim Speichern");
        }

        return saved;
    }

    // ========================================
    // GET ALL - Alle Medien holen
    // ========================================
    public List<MediaEntry> getAll() {
        return mediaRepository.findAll();
    }

    // ========================================
    // GET BY ID - Ein Medium holen
    // ========================================
    public MediaEntry getById(int id) throws Exception {
        MediaEntry media = mediaRepository.findById(id);

        if (media == null) {
            throw new Exception("Medium nicht gefunden");
        }

        return media;
    }

    // ========================================
    // UPDATE - Medium aktualisieren
    // ========================================
    public MediaEntry update(int id, MediaEntry updatedMedia, int userId) throws Exception {

        // 1. Medium finden
        MediaEntry existingMedia = mediaRepository.findById(id);

        if (existingMedia == null) {
            throw new Exception("Medium nicht gefunden");
        }

        // 2. Prüfen ob der User der Creator ist
        if (existingMedia.getCreatorId() != userId) {
            throw new Exception("Nur der Ersteller kann das Medium bearbeiten");
        }

        // 3. Felder aktualisieren (nur wenn neue Werte vorhanden)
        if (updatedMedia.getTitle() != null) {
            existingMedia.setTitle(updatedMedia.getTitle());
        }
        if (updatedMedia.getDescription() != null) {
            existingMedia.setDescription(updatedMedia.getDescription());
        }
        if (updatedMedia.getMediaType() != null) {
            existingMedia.setMediaType(updatedMedia.getMediaType().toUpperCase());
        }
        if (updatedMedia.getReleaseYear() != 0) {
            existingMedia.setReleaseYear(updatedMedia.getReleaseYear());
        }
        if (updatedMedia.getGenre() != null) {
            existingMedia.setGenre(updatedMedia.getGenre());
        }
        if (updatedMedia.getAgeRestriction() != 0) {
            existingMedia.setAgeRestriction(updatedMedia.getAgeRestriction());
        }

        // 4. Speichern
        return mediaRepository.update(existingMedia);
    }

    // ========================================
    // DELETE - Medium löschen
    // ========================================
    public void delete(int id, int userId) throws Exception {

        // 1. Medium finden
        MediaEntry media = mediaRepository.findById(id);

        if (media == null) {
            throw new Exception("Medium nicht gefunden");
        }

        // 2. Prüfen ob der User der Creator ist
        if (media.getCreatorId() != userId) {
            throw new Exception("Nur der Ersteller kann das Medium löschen");
        }

        // 3. Löschen
        boolean deleted = mediaRepository.delete(id);

        if (!deleted) {
            throw new Exception("Fehler beim Löschen");
        }
    }
}
