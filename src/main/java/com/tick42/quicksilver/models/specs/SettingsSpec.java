package com.tick42.quicksilver.models.specs;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SettingsSpec {
    @NotBlank(message = "Provide a GitHubModel username.")
    @Size(min = 1, message = "Username should be filled in.")
    private String username;

    @NotBlank(message = "Provide a token, associated with the username")
    @Size(min = 1, message = "Token should be filled in.")
    private String token;

    @NotNull(message = "Enter github data refresh rate in milliseconds.")
    @Min(value = 500000, message = "The rate should be 500000 millisecond or more.")
    private int rate;

    @NotNull(message = "Enter waiting period in milliseconds before initial data fetch.")
    @Min(value = 1000, message = "Initial wait should be 1000 millisecond or more.")
    private int wait;

    public SettingsSpec() {
    }

    public SettingsSpec(String token, int rate, int wait, String username) {
        this.token = token;
        this.rate = rate;
        this.wait = wait;
        this.username = username;
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
