package com.tick42.quicksilver.models.Dtos;

import java.util.List;

public class HomePageDto {
    private List<ExtensionDto> mostRecent;
    private List<ExtensionDto> featured;
    private List<ExtensionDto> mostDownloaded;

    public HomePageDto() {
    }

    public HomePageDto(List<ExtensionDto> mostRecent, List<ExtensionDto> featured, List<ExtensionDto> mostDownloaded) {
        this.mostRecent = mostRecent;
        this.featured = featured;
        this.mostDownloaded = mostDownloaded;
    }

    public List<ExtensionDto> getMostRecent() {
        return mostRecent;
    }

    public void setMostRecent(List<ExtensionDto> mostRecent) {
        this.mostRecent = mostRecent;
    }

    public List<ExtensionDto> getFeatured() {
        return featured;
    }

    public void setFeatured(List<ExtensionDto> featured) {
        this.featured = featured;
    }

    public List<ExtensionDto> getMostDownloaded() {
        return mostDownloaded;
    }

    public void setMostDownloaded(List<ExtensionDto> mostDownloaded) {
        this.mostDownloaded = mostDownloaded;
    }
}
