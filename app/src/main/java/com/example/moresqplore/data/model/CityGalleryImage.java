package com.example.moresqplore.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class CityGalleryImage implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("city_id")
    private String cityId;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("caption")
    private String caption;

    public CityGalleryImage() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCityId() { return cityId; }
    public void setCityId(String cityId) { this.cityId = cityId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
