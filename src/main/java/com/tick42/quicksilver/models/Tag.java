package com.tick42.quicksilver.models;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    private String name;

    @ManyToMany(mappedBy = "tags")
    private List<Extension> extensions;

    public Tag() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public Tag(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Extension> getExtensions() {
        return extensions;
    }
}
