package com.tick42.quicksilver.models.Dtos;

import com.tick42.quicksilver.models.Extension;
import com.tick42.quicksilver.models.File;

public class FileDto {
    private long id;
    private String resourceType;
    private long ownerId;
    private String extensionType;
    private long extensionId;
    private String type;
    private double size;

    public FileDto(){}

    public FileDto(File file) {
        this.id = file.getId();
        this.resourceType = file.getResourceType();
        this.ownerId = file.getOwner().getId();
        this.extensionType = file.getExtensionType();
        this.type = file.getType();
        this.size = file.getSize();
        setExtensionId(file.getExtension());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public long getExtensionId() {
        return extensionId;
    }

    public void setExtensionId(Extension extension) {
        if(extension != null){
            this.extensionId = extension.getId();
        }
    }

    public String getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(String extensionType) {
        this.extensionType = extensionType;
    }
}
