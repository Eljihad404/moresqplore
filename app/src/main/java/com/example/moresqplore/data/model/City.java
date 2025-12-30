package com.example.moresqplore.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

@Entity(tableName = "cities")
public class City implements Serializable {
    
    @PrimaryKey
    @NonNull
    @SerializedName("name")
    private String name;

    @SerializedName("id")
    private String id;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("cover_image_url")
    private String coverImageUrl;
    
    @androidx.room.Ignore
    private List<HistoryEvent> historyEvents;
    
    @androidx.room.Ignore
    private List<CityGalleryImage> gallery;

    public City() {
    }

    @androidx.room.Ignore
    public City(@NonNull String name, String description, List<HistoryEvent> historyEvents) {
        this.name = name;
        this.description = description;
        this.historyEvents = historyEvents;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public List<HistoryEvent> getHistoryEvents() {
        return historyEvents;
    }

    public void setHistoryEvents(List<HistoryEvent> historyEvents) {
        this.historyEvents = historyEvents;
    }
    
    public List<CityGalleryImage> getGallery() {
        return gallery;
    }

    public void setGallery(List<CityGalleryImage> gallery) {
        this.gallery = gallery;
    }
    
    // Parse the PostGIS 'location' field (GeoJSON format or String)
    @androidx.room.Ignore
    @SerializedName("location")
    private com.google.gson.JsonElement locationObj;

    public com.google.gson.JsonElement getLocationObj() { return locationObj; }
    public void setLocationObj(com.google.gson.JsonElement locationObj) { this.locationObj = locationObj; }

    // Coordinates (persisted in Room)
    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    public Double getLatitude() {
        if (latitude != null) return latitude;
        if (locationObj != null && locationObj.isJsonObject()) {
             // GeoJSON: { "type": "Point", "coordinates": [lon, lat] }
             com.google.gson.JsonObject obj = locationObj.getAsJsonObject();
             if (obj.has("coordinates")) {
                 com.google.gson.JsonArray coords = obj.getAsJsonArray("coordinates");
                 if (coords.size() >= 2) return coords.get(1).getAsDouble();
             }
        }
        return null; 
    }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() {
        if (longitude != null) return longitude;
        if (locationObj != null && locationObj.isJsonObject()) {
             com.google.gson.JsonObject obj = locationObj.getAsJsonObject();
             if (obj.has("coordinates")) {
                 com.google.gson.JsonArray coords = obj.getAsJsonArray("coordinates");
                 if (coords.size() >= 2) return coords.get(0).getAsDouble();
             }
        }
        return null;
    }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
