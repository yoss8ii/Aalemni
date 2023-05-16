package com.example.aalemni;

import java.util.Date;


//La classe qui contient les données de l'image
public class ImageHandler {

    private String imageUrl;
    private Date uploadTime;

    public ImageHandler(String imageUrl, Date uploadTime) {
        this.imageUrl = imageUrl;
        this.uploadTime = uploadTime;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

}
