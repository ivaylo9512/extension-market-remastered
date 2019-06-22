package com.tick42.quicksilver.models.DTO;

import com.tick42.quicksilver.models.GitHub;
import java.time.LocalDateTime;

public class GitHubDTO {

    private int id;
    private int pullRequests;
    private int openIssues;

    private String user;
    private String repo;
    private String link;
    private String failMessage;

    private String lastCommit;
    private String lastSuccess;
    private String lastFail;

    public GitHubDTO() {

    }

    public GitHubDTO(GitHub gitHub) {
        this.id = gitHub.getId();
        this.user = gitHub.getUser();
        this.pullRequests = gitHub.getPullRequests();
        this.openIssues = gitHub.getOpenIssues();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public int getPullRequests() {
        return pullRequests;
    }

    public void setPullRequests(int pullRequests) {
        this.pullRequests = pullRequests;
    }

    public int getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(int openIssues) {
        this.openIssues = openIssues;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(LocalDateTime lastCommit) {
        this.lastCommit = lastCommit.toString();
    }

    public String getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(LocalDateTime lastSuccess) {
        this.lastSuccess = lastSuccess.toString();
    }

    public String getLastFail() {
        return lastFail;
    }

    public void setLastFail(LocalDateTime lastFail) {
        this.lastFail = lastFail.toString();
    }
}
