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

    @Column(name = "pull_requests")
    private int pullRequests;

    @Column(name = "open_issues")
    private int openIssues;

    @Column(name = "last_commit", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastCommit;

    @Column(name = "last_success")
    private LocalDateTime lastSuccess;

    @Column(name = "last_fail")
    private LocalDateTime lastFail;

    @Column(name = "fail_message")
    private String failMessage;

    private String user;
    private String repo;

    public GitHubModel() {

    }

    public GitHubModel(String user, String repo) {
        this.user = user;
        this.repo = repo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
