package com.tick42.quicksilver.models.specs;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UserSpec {
    private long id;

    @Size(min = 8, max = 18)
    private String username;
    @NotNull
    private String country;
    @NotNull
    private String info;

    public UserSpec() {
    }

    public UserSpec(long id, String username, String country, String info) {
        this.id = id;
        this.username = username;
        this.info = info;
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
