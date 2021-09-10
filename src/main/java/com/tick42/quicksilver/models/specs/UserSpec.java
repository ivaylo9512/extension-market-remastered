package com.tick42.quicksilver.models.specs;

import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class UserSpec {
    @NotNull(message = "Id is required")
    private long id;

    @NotNull(message = "Username is required")
    @Length(min = 8, max = 18, message = ("Password must be between 8 and 18 characters"))
    private String username;

    @NotNull(message = "Email is required")
    @Email(message = "Must be a valid email")
    private String email;

    @NotNull(message = "Country is required")
    private String country;

    @NotNull(message = "Info is required")
    private String info;

    public UserSpec() {
    }

    public UserSpec(long id, String username, String email, String country, String info) {
        this.id = id;
        this.username = username;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
