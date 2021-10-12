package com.tick42.quicksilver.models;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@IdClass(RatingPK.class)
@Table(name = "ratings")
public class Rating implements Serializable {
    @Id
    private long extension;

    @Id
    private long user;

    private int rating;

    public Rating() {
    }

    public Rating(int rating, long extension, long user) {
        this.rating = rating;
        this.extension = extension;
        this.user = user;
    }

    public Rating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public long getExtension() {
        return extension;
    }

    public long getUser() {
        return user;
    }
}
