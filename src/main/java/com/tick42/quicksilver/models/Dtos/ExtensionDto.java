package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.GitHubModel;
import com.tick42.quicksilver.models.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExtensionDto {
    private long id;
    private String name;
    private String version;
    private String description;
    private int timesDownloaded;
    private boolean isPending;
    private boolean isFeatured;
    private String uploadDate;
    private String ownerName;
    private long ownerId;
    private String fileName;
    private String imageName;
    private String coverName;
    private List<String> tags = new ArrayList<>();
    private double rating;
    private int timesRated;
    private int currentUserRatingValue;
    private GitHubDto github;

    public ExtensionDto() {

    }

    public ExtensionDto(Extension extension) {
        this.id = extension.getId();
        this.name = extension.getName();
        this.description = extension.getDescription();
        this.isFeatured = extension.isFeatured();
        this.ownerId = extension.getOwner().getId();
        this.ownerName = extension.getOwner().getUsername();
        this.isPending = extension.isPending();
        this.version = extension.getVersion();
        this.rating = extension.getRating();
        this.timesRated = extension.getTimesRated();

        toGitHubDto(extension.getGithub());
        setTimesDownloaded(extension.getFile());
        toTagsDto(extension.getTags());
        setImageName(extension.getImage());
        setCoverName(extension.getCover());
        setFileName(extension.getFile());
        setUploadDate(extension.getUploadDate());
    }

    public ExtensionDto(long id){
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionDto that = (ExtensionDto) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
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

    public int getTimesDownloaded() {
        return timesDownloaded;
    }

    public void setTimesDownloaded(int timesDownloaded) {
        this.timesDownloaded = timesDownloaded;
    }

    public void setTimesDownloaded(File file) {
        if(file != null){
            this.timesDownloaded = file.getDownloadCount();
        }
    }

    public boolean isPending() {
        return isPending;
    }

    public void setPending(boolean pending) {
        isPending = pending;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        if(uploadDate != null){
            this.uploadDate = uploadDate.toString();
        }
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public int getCurrentUserRatingValue() {
        return currentUserRatingValue;
    }

    public void setCurrentUserRatingValue(int currentUserRatingValue) {
        this.currentUserRatingValue = currentUserRatingValue;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileName(File file) {
        if(file != null){
            this.fileName = file.getResourceType() + id + "." + file.getExtensionType();
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setImageName(File file) {
        if(file != null){
            this.imageName = file.getResourceType() + id + "." + file.getExtensionType();
        }
    }

    public String getCoverName() {
        return coverName;
    }

    public void setCoverName(String coverName) {
        this.coverName = coverName;
    }

    public void setCoverName(File file) {
        if(file != null){
            this.coverName = file.getResourceType() + id + "." + file.getExtensionType();
        }
    }

    public GitHubDto getGithub() {
        return github;
    }

    public void setGithub(GitHubDto github) {
        this.github = github;
    }

    public void toGitHubDto(GitHubModel github){
        if(github != null){
            this.github = new GitHubDto(github);
        }
    }

    private void toTagsDto(Set<Tag> tags) {
        if(tags != null){
            this.tags = tags.stream().map(Tag::getName).collect(Collectors.toList());
        }
    }
}
