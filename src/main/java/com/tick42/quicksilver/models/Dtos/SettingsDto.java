package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.Settings;

public class SettingsDto {
    private long id;
    private String username;
    private String token;
    private int rate;
    private int wait;

    public SettingsDto() {

    }

    public SettingsDto(Settings settings) {
        this.id = settings.getId();
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
