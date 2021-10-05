package com.tick42.quicksilver.models;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag {
    @Id
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<Extension> extensions = new HashSet<>();

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

    public Set<Extension> getExtensions() {
        return extensions;
    }

    public void setExtensions(Set<Extension> extensions) {
        this.extensions = extensions;
    }

    @Override
    public String toString() {
        return name;
    }
}
