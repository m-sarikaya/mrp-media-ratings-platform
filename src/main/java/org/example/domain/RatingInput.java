package org.example.domain;

// Eingabe-DTO für Rating-Requests (stars + comment).
public class RatingInput {
    private int stars;
    private String comment;

    public RatingInput() {
    }

    public int getStars() {
        return stars;
    }

    public String getComment() {
        return comment;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
