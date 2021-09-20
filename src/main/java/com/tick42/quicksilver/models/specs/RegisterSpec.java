package com.tick42.quicksilver.models.specs;

import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class RegisterSpec {
    @NotNull(message = "You must provide username.")
    @Length(min = 8, max = 18, message = ("Username must be between 8 and 20 characters."))
    private String username;

    @NotNull(message = "You must provide email.")
    @Email(message = "Must be a valid email.")
    private String email;

    @NotNull(message = "You must provide password.")
    @Length(min = 10, max = 25, message = ("Password must be between 10 and 25 characters."))
    private String password;

    private MultipartFile profileImage;

    @NotNull(message = "You must provide country.")
    private String country;

    @NotNull(message = "You must provide info.")
    private String info;

    public RegisterSpec() {
    }

    public RegisterSpec(String username, String email, String password, MultipartFile profileImage, String country, String info) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
        this.country = country;
        this.info = info;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
