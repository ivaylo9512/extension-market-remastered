package com.tick42.quicksilver.models;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import javax.persistence.*;

@Entity
@Table(name = "files")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = File.class)
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @PrimaryKeyJoinColumn
    private Extension extension;

    @Column(name = "download_count")
    private int downloadCount;

    private String name;
    private String type;
    private double size;

    public File(){

    }

    public File(String name, double size, String type){
        this.name = name;
        this.size = size;
        this.type = type;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof File)) return false;

        File extension = (File) obj;
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

    public void setSize(double size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }
}
