package com.tick42.quicksilver.models.specs;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

public class ExtensionSpec {
    private long id;

    @NotBlank(message = "Name is required")
    @Length(min = 7, max = 30, message = ("Name must be between 7 and 30 characters."))
    private String name;

    @NotBlank(message = "Version is required")
    private String version;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Github is required")
    @Pattern(regexp = "^https://github.com/.+/.+$", message = "Link to github should match https://github.com/USER/REPOSITORY")
    private String github;

    private int githubId;

    private String tags;

    private MultipartFile image;
    private MultipartFile file;
    private MultipartFile cover;

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

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public MultipartFile getCover() {
        return cover;
    }

    public void setCover(MultipartFile cover) {
        this.cover = cover;
    }
}
