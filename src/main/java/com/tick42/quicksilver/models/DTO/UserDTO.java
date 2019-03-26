package com.tick42.quicksilver.models.DTO;

import com.tick42.quicksilver.models.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDTO {
    private int id;
    private String username;
    private int totalExtensions;
    private List<ExtensionDTO> extensions = new ArrayList<>();
    private boolean isActive;
    private double rating;
    private int extensions_rated;

    public UserDTO() {

    }

    public UserDTO(UserModel userModel) {
        this.setId(userModel.getId());
        this.setUsername(userModel.getUsername());
        this.setExtensions(
                userModel.getExtensions()
                        .stream()
                        .map(ExtensionDTO::new)
                        .collect(Collectors.toList()));
        this.setTotalExtensions(this.extensions.size());
        this.setIsActive(userModel.getIsActive());
        this.setExtensions_rated(userModel.getExtensionsRated());
        this.setRating(userModel.getRating());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public int getExtensions_rated() {
        return extensions_rated;
    }

    public void setExtensions_rated(int extensions_rated) {
        this.extensions_rated = extensions_rated;
    }
}
