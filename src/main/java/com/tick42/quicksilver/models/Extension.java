package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.specs.ExtensionSpec;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "extensions")
public class Extension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(cascade = CascadeType.ALL)
    private File file;

    @OneToOne(cascade = CascadeType.ALL)
    private File image;

    @OneToOne(cascade = CascadeType.ALL)
    private File cover;

    @OneToOne(cascade = CascadeType.ALL)
    private GitHubModel github;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner")
    private UserModel owner;

    @Column(name = "times_rated")
    private int timesRated;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(
            name = "extension_tags",
            joinColumns = @JoinColumn(name = "extension_id"),
            inverseJoinColumns = @JoinColumn(name = "tag"))
    private Set<Tag> tags = new HashSet<>();

    private String name;
    private String description;
    private String version;
    private boolean pending = true;
    private boolean featured;
    private double rating;

    public Extension() {

    }

    public Extension(ExtensionSpec extensionSpec, UserModel owner, Set<Tag> tags) {
        this(extensionSpec.getName(), extensionSpec.getDescription(),
                extensionSpec.getVersion(), owner);
        this.tags = tags;
        this.github = getGithub();
    }

    public Extension(String name, String description, String version, UserModel owner){
        this.name = name;
        this.version = version;
        this.description = description;
        this.owner = owner;
    }

    public Extension(long id, String name, String description, String version, Set<Tag> tags, GitHubModel github, UserModel owner) {
        this(name, description, version, owner);
        this.id = id;
        this.tags = tags;
        this.github = github;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof Extension extension)) return false;

        return extension.getId() == getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }

    public UserModel getOwner() {
        return owner;
    }

    public void setOwner(UserModel owner) {
        this.owner = owner;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public GitHubModel getGithub() {
        return github;
    }

    public void setGithub(GitHubModel github) {
        this.github = github;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTimesRated() {
        return timesRated;
    }

    public void setTimesRated(int timesRated) {
        this.timesRated = timesRated;
    }

    public File getCover() {
        return cover;
    }

    public void setCover(File cover) {
        this.cover = cover;
    }
}
