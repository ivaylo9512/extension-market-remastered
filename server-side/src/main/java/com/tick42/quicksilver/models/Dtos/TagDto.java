package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.Tag;
import java.util.ArrayList;
import java.util.List;

public class TagDto {
    private String tag;
    private int totalExtensions;
    private List<ExtensionDto> extensions = new ArrayList<>();

    public TagDto() {

    }

    public TagDto(Tag tag) {
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

    public List<ExtensionDto> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ExtensionDto> extensions) {
        this.extensions = extensions;
    }
}
