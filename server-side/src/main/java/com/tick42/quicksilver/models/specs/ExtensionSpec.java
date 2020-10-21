package com.tick42.quicksilver.models.specs;

import com.tick42.quicksilver.models.File;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ExtensionSpec {
    @NotNull
    private int id;

    @NotNull
    @Size(min=1, message="Name should be at least 1 character.")
    private String name;


    @NotNull
    @Size(min=1, message="Version should be at least 1 character.")
    private String version;

    @NotNull
    @Size(min=1, message="Description should be at least 1 character.")
    private String description;

    @NotNull
    @Pattern(regexp = "^https://github.com/.+/.+$", message = "Link to github should match https://github.com/USER/REPOSITORY")
    private String github;

    @NotNull
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGithubId() {
        return githubId;
    }

    public void setGithubId(int githubId) {
        this.githubId = githubId;
    }
}
