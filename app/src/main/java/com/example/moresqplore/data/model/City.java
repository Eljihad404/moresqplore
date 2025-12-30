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
    private String id; // Supabase UUID
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("cover_image_url")
    private String coverImageUrl;
    
    private List<HistoryEvent> historyEvents;
    
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
}
