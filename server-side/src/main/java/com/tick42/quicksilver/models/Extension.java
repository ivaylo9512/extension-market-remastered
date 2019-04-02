package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.Spec.ExtensionSpec;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "extensions")
public class Extension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private File file;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private File image;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "github_id")
    private GitHubModel github;

    @Column(name = "times_downloaded")
    private int timesDownloaded = 0;

    @Column(name = "version")
    private String version;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinTable(
            name = "extension_tags",
            joinColumns = @JoinColumn(name = "extension_id"),
            inverseJoinColumns = @JoinColumn(name = "tag"))
    private Set<Tag> tags = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner")
    private UserModel owner;

    @Column(name = "pending")
    private boolean pending = true;

    @Column(name = "upload_date")
    private Date uploadDate = new Date();

    @Column(name = "featured")
    private boolean featured;

    @Column(name = "rating")
    private double rating;

    @Column(name = "times_rated")
    private int timesRated;

    public Extension() {

    }

    public Extension(ExtensionSpec extensionSpec) {
        this.name = extensionSpec.getName();
        this.version = extensionSpec.getVersion();
        this.description = extensionSpec.getDescription();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getTimesDownloaded() {
        return timesDownloaded;
    }

    public void setTimesDownloaded(int timesDownloaded) {
        this.timesDownloaded = timesDownloaded;
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

    public void isPending(boolean pending) {
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

    public void isFeatured(boolean featured) {
        this.featured = featured;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
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
}
