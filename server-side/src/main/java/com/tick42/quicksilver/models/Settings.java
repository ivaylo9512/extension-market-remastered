package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.specs.GitHubSettingSpec;
import javax.persistence.*;

@Entity
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "rate")
    private int rate;

    @Column(name = "wait")
    private int wait;

    @Column(name = "git_token")
    private String token;

    @Column(name = "git_username")
    private String username;

    @OneToOne
    @JoinColumn(name = "user")
    private UserModel user;

    public Settings() {

    }
    public Settings(GitHubSettingSpec settingsSpec) {
        this.rate = settingsSpec.getRate();
        this.wait = settingsSpec.getWait();
        this.token = settingsSpec.getToken();
        this.username = settingsSpec.getUsername();
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public void setUser(UserModel user) {
        this.user = user;
    }
}
