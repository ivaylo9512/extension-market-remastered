package com.tick42.quicksilver.models;

import com.tick42.quicksilver.models.specs.SettingsSpec;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Table(name = "settings")
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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserModel user;

    private int rate;
    private int wait;

    public Settings() {
    }

    public Settings(SettingsSpec settingsSpec, UserModel user, long id) {
        this(id, settingsSpec.getRate(), settingsSpec.getWait(), settingsSpec.getToken(),
                settingsSpec.getUsername());
        this.user = user;
    }

    public Settings(String username, String token){
        this.username = username;
        this.token = token;
    }

    public Settings(long id, int rate, int wait, String token, String username) {
        this.id = id;
        this.rate = rate;
        this.wait = wait;
        this.token = token;
        this.username = username;
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

    public int getWait() {
        return wait;
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

    public UserModel getUser() {
        return user;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }
}
