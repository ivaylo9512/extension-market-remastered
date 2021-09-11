package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.models.specs.UserSpec;

public class UserDto {
    private long id;
    private String username;
    private String email;
    private boolean isActive;
    private double rating;
    private int extensionsRated;
    private String profileImage;
    private String country;
    private String info;
    private String role;

    public UserDto() {

    }

    public UserDto(UserModel userModel) {
        setProfileImage(userModel.getProfileImage());
        this.id = userModel.getId();
        this.username = userModel.getUsername();
        this.email = userModel.getEmail();
        this.isActive = userModel.isActive();
        this.extensionsRated = userModel.getExtensionsRated();
        this.rating = userModel.getRating();
        this.country = userModel.getCountry();
        this.info = userModel.getInfo();
        this.role = userModel.getRole();
    }

    public UserDto(UserSpec user, String role){
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.country = user.getCountry();
        this.info = user.getInfo();
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean active) {
        isActive = active;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getExtensionsRated() {
        return extensionsRated;
    }

    public void setExtensionsRated(int extensionsRated) {
        this.extensionsRated = extensionsRated;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(File profileImage) {
        if(profileImage != null){
            this.profileImage = "logo" + id + "." + profileImage.getExtensionType();
        }
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
