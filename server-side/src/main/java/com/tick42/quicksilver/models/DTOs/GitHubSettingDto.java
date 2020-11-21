package com.tick42.quicksilver.models.DTOs;

import com.tick42.quicksilver.models.Settings;

public class GitHubSettingDTO {

    private String username;

    private String token;

    private int rate;

    private int wait;

    public GitHubSettingDTO() {

    }

    public GitHubSettingDTO(Settings settings) {
        this.username = settings.getUsername();
        this.token = settings.getToken();
        this.rate = settings.getRate();
        this.wait = settings.getWait();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getWait() {
        return wait;
    }

    public void setWait(Integer wait) {
        this.wait = wait;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
