package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.GitHubModel;
import java.time.LocalDateTime;

public class GitHubDto {
    private long id;
    private int pullRequests;
    private int openIssues;

    private String user;
    private String repo;
    private String link;
    private String failMessage;

    private String lastCommit;
    private String lastSuccess;
    private String lastFail;

    public GitHubDto() {

    }

    public GitHubDto(GitHubModel gitHubModel) {
        this.id = gitHubModel.getId();
        this.user = gitHubModel.getUser();
        this.pullRequests = gitHubModel.getPullRequests();
        this.openIssues = gitHubModel.getOpenIssues();
        setLastCommit(gitHubModel.getLastCommit());
        setLastFail(gitHubModel.getLastFail());
        setLastSuccess(gitHubModel.getLastSuccess());
        setFailMessage(gitHubModel.getFailMessage());
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
        if(failMessage != null){
            this.failMessage = failMessage;
        }
    }

    public String getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(LocalDateTime lastCommit) {
        if(lastCommit != null){
            this.lastCommit = lastCommit.toString();
        }
    }

    public String getLastSuccess() {
        return lastSuccess;
    }

    public void setLastSuccess(LocalDateTime lastSuccess) {
        if(lastSuccess != null){
            this.lastSuccess = lastSuccess.toString();
        }
    }

    public String getLastFail() {
        return lastFail;
    }

    public void setLastFail(LocalDateTime lastFail) {
        if(lastFail != null){
            this.lastFail = lastFail.toString();
        }
    }
}
