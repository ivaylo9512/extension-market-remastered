package com.tick42.quicksilver.models.DTOs;

import java.util.List;

public class HomePageDTO {
    private List<ExtensionDTO> mostRecent;
    private List<ExtensionDTO> featured;
    private List<ExtensionDTO> mostDownloaded;

    public HomePageDTO() {
    }

    public HomePageDTO(List<ExtensionDTO> mostRecent, List<ExtensionDTO> featured, List<ExtensionDTO> mostDownloaded) {
        this.mostRecent = mostRecent;
        this.featured = featured;
        this.mostDownloaded = mostDownloaded;
    }

    public List<ExtensionDTO> getMostRecent() {
        return mostRecent;
    }

    public void setMostRecent(List<ExtensionDTO> mostRecent) {
        this.mostRecent = mostRecent;
    }

    public List<ExtensionDTO> getFeatured() {
        return featured;
    }

    public void setFeatured(List<ExtensionDTO> featured) {
        this.featured = featured;
    }

    public List<ExtensionDTO> getMostDownloaded() {
        return mostDownloaded;
    }

    public void setMostDownloaded(List<ExtensionDTO> mostDownloaded) {
        this.mostDownloaded = mostDownloaded;
    }
}
