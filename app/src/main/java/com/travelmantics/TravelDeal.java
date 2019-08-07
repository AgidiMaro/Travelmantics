package com.travelmantics;

import java.io.Serializable;
import java.lang.annotation.Target;

public class TravelDeal implements Serializable {
    private String id;
    private String title;
    private String price;
    private String imageURL;
    private String description;
    private String imageName;

    public TravelDeal(){}

    public TravelDeal(String title, String price, String description, String imageURL, String imageName) {
        this.setTitle(title);
        this.setPrice(price);
        this.setImageURL(imageURL);
        this.setDescription(description);
        this.setImageName(imageName);
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }
}
