package com.tick42.quicksilver.models.DTOs;

import com.tick42.quicksilver.models.File;
import com.tick42.quicksilver.models.UserModel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private long id;
    private String username;
    private int totalExtensions;
    private List<ExtensionDTO> extensions = new ArrayList<>();
    private boolean isActive;
    private double rating;
    private int extensionsRated;
    private String profileImage;
    private String country;
    private String info;

    public UserDTO() {

    }

    public UserDTO(UserModel userModel) {
        setProfileImage(userModel.getProfileImage());
        this.id = userModel.getId();
        this.username = userModel.getUsername();
        this.extensions = userModel.getExtensions()
                .stream()
                .map(ExtensionDTO::new)
                .collect(Collectors.toList());
        this.totalExtensions = this.extensions.size();
        this.isActive = userModel.getIsActive();
        this.extensionsRated = userModel.getExtensionsRated();
        this.rating = userModel.getRating();
        this.country = userModel.getCountry();
        this.info = userModel.getInfo();
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

    public List<ExtensionDTO> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionDTO> extensions) {
        this.extensions = extensions;
    }

    public int getTotalExtensions() {
        return totalExtensions;
    }

    public void setTotalExtensions(int totalExtensions) {
        this.totalExtensions = totalExtensions;
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
            this.profileImage = profileImage.getName();
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
}
