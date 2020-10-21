package com.tick42.quicksilver.models.DTOs;

import com.tick42.quicksilver.models.Tag;
import java.util.ArrayList;
import java.util.List;

public class TagDTO {
    private String tag;
    private int totalExtensions;
    private List<ExtensionDTO> extensions = new ArrayList<>();

    public TagDTO() {

    }

    public TagDTO(Tag tag) {
        this.tag = tag.getName();
        this.totalExtensions = tag.getExtensions().size();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getTotalExtensions() {
        return totalExtensions;
    }

    public void setTotalExtensions(int totalExtensions) {
        this.totalExtensions = totalExtensions;
    }

    public List<ExtensionDTO> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionDTO> extensions) {
        this.extensions = extensions;
    }
}
