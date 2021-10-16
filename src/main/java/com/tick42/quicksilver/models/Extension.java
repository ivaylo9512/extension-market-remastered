package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.specs.ExtensionCreateSpec;
import com.tick42.quicksilver.models.specs.ExtensionUpdateSpec;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File file;

    @OneToOne(cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File image;

    @OneToOne(cascade = CascadeType.ALL)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private File cover;

    @OneToOne(cascade = CascadeType.ALL)
    private GitHubModel github;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserModel owner;

    @Column(name = "times_rated")
    private int timesRated;

    @CreationTimestamp
    @Column(name = "upload_date", columnDefinition = "DATETIME(6)")
    private LocalDateTime uploadDate;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "extension_tags",
            joinColumns = @JoinColumn(name = "extension"),
            inverseJoinColumns = @JoinColumn(name = "tag"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Tag> tags = new HashSet<>();

    @Column(columnDefinition = "text")
    private String description;

    @Column(unique = true)
    private String name;

    private String version;
    private boolean pending = true;
    private boolean featured;
    private double rating;

    @PreRemove
    private void preRemove() {
        file.setExtension(null);
        image.setExtension(null);
        cover.setExtension(null);
        tags.forEach(tag -> tag.getExtensions().remove(this));
    }

    public Extension() {

    }

    public Extension(long id, UserModel owner) {
        this.owner = owner;
        this.id = id;
    }

    public Extension(ExtensionCreateSpec extensionCreateSpec, UserModel owner, Set<Tag> tags) {
        this(extensionCreateSpec.getName(), extensionCreateSpec.getDescription(),
                extensionCreateSpec.getVersion(), owner);
        this.tags = tags;
    }

    public Extension(ExtensionUpdateSpec extensionUpdateSpec, Extension extension, Set<Tag> tags) {
        this(extensionUpdateSpec.getName(), extensionUpdateSpec.getDescription(),
                extensionUpdateSpec.getVersion(), extension.getOwner());
        this.id = extensionUpdateSpec.getId();
        this.tags = tags;
        this.file = extension.getFile();
        this.image = extension.getImage();
        this.cover = extension.getCover();
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
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
