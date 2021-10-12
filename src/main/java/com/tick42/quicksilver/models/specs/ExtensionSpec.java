package com.tick42.quicksilver.models.specs;

import org.springframework.web.multipart.MultipartFile;

public class ExtensionSpec {
    private MultipartFile image;
    private MultipartFile file;
    private MultipartFile cover;

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public MultipartFile getCover() {
        return cover;
    }

    public void setCover(MultipartFile cover) {
        this.cover = cover;
    }
}
