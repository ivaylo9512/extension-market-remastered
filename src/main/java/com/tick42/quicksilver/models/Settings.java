package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import javax.persistence.*;

@Entity
public class Settings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "git_token")
    private String token;

    @Column(name = "git_username")
    private String username;

    @OneToOne
    @JoinColumn(name = "user")
    private UserModel user;

    private int rate;
    private int wait;

    public Settings() {
    }

    public Settings(GitHubSettingSpec settingsSpec, UserModel user, long id) {
        this.id = id;
        this.rate = settingsSpec.getRate();
        this.wait = settingsSpec.getWait();
        this.token = settingsSpec.getToken();
        this.username = settingsSpec.getUsername();
        this.user = user;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
