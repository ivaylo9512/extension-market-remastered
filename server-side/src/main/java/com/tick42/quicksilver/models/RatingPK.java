package com.tick42.quicksilver.models;

import java.io.Serializable;
import java.util.Objects;

public class RatingPK implements Serializable {
    protected long extension;
    protected long user;

    public RatingPK() {
    }

    public RatingPK(long extension, long user) {
        this.extension = extension;
        this.user = user;
    }

    public long getExtension() {
        return extension;
    }

    public void setExtension(long extension) {
        this.extension = extension;
    }

    public long getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RatingPK)) return false;
        RatingPK that = (RatingPK) o;
        return Objects.equals(getExtension(), that.getExtension()) &&
                Objects.equals(getUser(), that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getExtension());
    }
}
