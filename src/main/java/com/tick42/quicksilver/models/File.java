package com.tick42.quicksilver.models;

import javax.persistence.*;

@Entity
@Table(name = "files")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "extension")
    private Extension extension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner")
    private UserModel owner;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "profileImage")
    private UserModel profileOwner;

    @Column(name = "download_count")
    private int downloadCount;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "extension_type")
    private String extensionType;

    private String type;
    private double size;

    @PreRemove
    private void preRemove() {
        if(owner.getProfileImage().equals(this)){
            profileOwner.setProfileImage(null);
        }
    }

    public File(){

    }

    public File(String resourceType, double size, String type, String extensionType){
        this.resourceType = resourceType;
        this.size = size;
        this.type = type;
        this.extensionType = extensionType;
    }

    public File(String resourceType, double size, String type, String extensionType, UserModel owner){
        this.resourceType = resourceType;
        this.size = size;
        this.type = type;
        this.extensionType = extensionType;
        this.owner = owner;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof File extension)) return false;

        return extension.getId() == getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Extension getExtension() {
        return extension;
    }

    public void setExtension(Extension extension) {
        this.extension = extension;
    }

    public double getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(String extensionType) {
        this.extensionType = extensionType;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public UserModel getOwner() {
        return owner;
    }

    public void setOwner(UserModel owner) {
        this.owner = owner;
    }
}
