package org.example.domain;

import java.time.LocalDateTime;

// Model fuer eine Bewertung eines Mediums.
public class Rating {
    private int id;
    private int mediaId;
    private int userId;
    private int stars;
    private String comment;
    private boolean commentConfirmed;
    private LocalDateTime createdAt;

    public Rating() {
    }

    // ===== GETTER =====

    public int getId() {
        return id;
    }

    public int getMediaId() {
        return mediaId;
    }

    public int getUserId() {
        return userId;
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public boolean isCommentConfirmed() {
        return commentConfirmed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== SETTER =====

    public void setId(int id) {
        this.id = id;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCommentConfirmed(boolean commentConfirmed) {
        this.commentConfirmed = commentConfirmed;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
