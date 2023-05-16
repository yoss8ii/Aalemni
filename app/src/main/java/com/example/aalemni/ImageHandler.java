package com.example.aalemni;

import java.util.Date;


//La classe qui contient les données de l'image
public class ImageHandler {

    private String imageUrl;
    private Date uploadTime;
    private String imageID;

    public ImageHandler(String imageUrl, Date uploadTime, String imageID) {
        this.imageUrl = imageUrl;
        this.uploadTime = uploadTime;
        this.imageID = imageID;
    }
    public String getImageUrl() {
        return imageUrl;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public String getImageID() {
        return imageID;
    }
}
