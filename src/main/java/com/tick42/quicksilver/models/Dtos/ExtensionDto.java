package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    private String gitHubLink;
    private String lastCommit;
    private int openIssues;
    private int pullRequests;
    private String lastSuccessfulPullOfData;
    private String lastFailedAttemptToCollectData;
    private String lastErrorMessage;
    private String fileName;
    private String imageName;
    private String coverName;
    private List<String> tags = new ArrayList<>();
    private double rating;
    private int timesRated;
    private int currentUserRatingValue;
    private long githubId;

    public ExtensionDto() {

    }

    public ExtensionDto(Extension extension) {
        this.id = extension.getId();
        this.name = extension.getName();
        this.description = extension.getDescription();
        this.isFeatured = extension.isFeatured();
        this.ownerId = extension.getOwner().getId();
        this.ownerName = extension.getOwner().getUsername();
        this.isPending = extension.getIsPending();
        this.tags = extension.getTags().stream().map(Tag::getName).collect(Collectors.toList());
        this.timesDownloaded = extension.getFile().getDownloadCount();
        this.version = extension.getVersion();
        this.rating = extension.getRating();
        this.timesRated = extension.getTimesRated();

        setImageName(extension.getImage());
        setCoverName(extension.getCover());
        setFileName(extension.getFile());
        setUploadDate(extension.getUploadDate());
    }
    @Override
    public boolean equals(Object o){
        if(o instanceof ExtensionDto){
            ExtensionDto compare = (ExtensionDto) o;
            return this.id == compare.getId();
        }
        return false;
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

    public boolean getIsPending() {
        return isPending;
    }

    public void setIsPending(boolean pending) {
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
        this.uploadDate = uploadDate.toString();
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

    public String getGitHubLink() {
        return gitHubLink;
    }

    public void setGitHubLink(String gitHubLink) {
        this.gitHubLink = gitHubLink;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(LocalDateTime lastCommit) {
        this.lastCommit = lastCommit.toString();
    }

    public int getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(int openIssues) {
        this.openIssues = openIssues;
    }

    public int getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(int pullRequests) {
        this.pullRequests = pullRequests;
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

    public String getLastSuccessfulPullOfData() {
        return lastSuccessfulPullOfData;
    }

    public void setLastSuccessfulPullOfData(LocalDateTime lastSuccessfulPullOfData) {
        this.lastSuccessfulPullOfData = lastSuccessfulPullOfData.toString();
    }

    public String getLastFailedAttemptToCollectData() {
        return lastFailedAttemptToCollectData;
    }

    public void setLastFailedAttemptToCollectData(LocalDateTime lastFailedAttemptToCollectData) {
        this.lastFailedAttemptToCollectData = lastFailedAttemptToCollectData.toString();
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
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

    public void setFileName(File file) {
        if(file != null){
            this.fileName = file.getExtensionType() + file.getExtension().getId() + file.getExtensionType();
        }
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(File file) {
        if(file != null){
            this.imageName = file.getExtensionType() + file.getExtension().getId() + file.getExtensionType();
        }
    }

    public String getCoverName() {
        return coverName;
    }

    public void setCoverName(File file) {
        if(file != null){
            this.coverName = file.getExtensionType() + file.getExtension().getId() + file.getExtensionType();
        }
    }

    public long getGithubId() {
        return githubId;
    }

    public void setGithubId(long githubId) {
        this.githubId = githubId;
    }
}
