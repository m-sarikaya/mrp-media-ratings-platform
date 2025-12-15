package org.example.domain;

public class MediaEntry {

    private int id;
    private String title;
    private String description;
    private String mediaType;      // MOVIE, SERIES, GAME
    private int releaseYear;
    private String genre;
    private int ageRestriction;
    private int creatorId;         // Welcher User hat es erstellt

    // Leerer Constructor (braucht Jackson)
    public MediaEntry() {
    }

    // ===== GETTER =====

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMediaType() {
        return mediaType;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getGenre() {
        return genre;
    }

    public int getAgeRestriction() {
        return ageRestriction;
    }

    public int getCreatorId() {
        return creatorId;
    }

    // ===== SETTER =====

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setAgeRestriction(int ageRestriction) {
        this.ageRestriction = ageRestriction;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }
}
