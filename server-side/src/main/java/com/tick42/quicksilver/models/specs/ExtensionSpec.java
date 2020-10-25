package com.tick42.quicksilver.models.specs;

import com.tick42.quicksilver.models.File;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ExtensionSpec {
    @NotNull
    private long id;

    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Version is required")
    private String version;

    @NotNull(message = "Description is required")
    private String description;

    @NotNull(message = "Github is required")
    @Pattern(regexp = "^https://github.com/.+/.+$", message = "Link to github should match https://github.com/USER/REPOSITORY")
    private String github;

    @NotNull(message = "Github id is required")
    private int githubId;

    private String tags;
    private File file;
    private File image;
    private File cover;

    public ExtensionSpec() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getImage() {
        return image;
    }

    public void setImage(File image) {
        this.image = image;
    }

    public File getCover() {
        return cover;
    }

    public void setCover(File cover) {
        this.cover = cover;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getGithubId() {
        return githubId;
    }

    public void setGithubId(int githubId) {
        this.githubId = githubId;
    }
}
