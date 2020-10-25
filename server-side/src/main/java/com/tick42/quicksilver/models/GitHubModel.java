package com.tick42.quicksilver.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "github")
public class GitHubModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    @PrimaryKeyJoinColumn
    private Extension extension;

    private String link;
    private String user;
    private String repo;
    private LocalDateTime lastCommit;
    private int pullRequests;
    private int openIssues;
    private LocalDateTime lastSuccess;
    private LocalDateTime lastFail;
    private String failMessage;

    public GitHubModel() {

    }

    public GitHubModel(String link, String user, String repo) {
        this.link = link;
        this.user = user;
        this.repo = repo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    public LocalDateTime getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(LocalDateTime lastCommit) {
        this.lastCommit = lastCommit;
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

    public LocalDateTime getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(LocalDateTime lastSuccess) {
        this.lastSuccess = lastSuccess;
    }

    public LocalDateTime getLastFail() {
        return lastFail;
    }

    public void setLastFail(LocalDateTime lastFail) {
        this.lastFail = lastFail;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }
}
