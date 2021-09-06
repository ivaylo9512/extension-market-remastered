package com.tick42.quicksilver.models.specs;

import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.Size;

public class RegisterSpec {
    @Size(min = 8, max = 18)
    private String username;

    @Size(min = 10, max = 25)
    private String password;

    private MultipartFile profileImage;

    public RegisterSpec() {
    }

    public RegisterSpec(String username, String password) {
        this.username = username;
        this.password = password;
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
}
