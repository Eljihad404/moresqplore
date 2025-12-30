package com.example.moresqplore.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Data model representing a tourist place in Morocco.
 */
public class Place implements Serializable {
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("address")
    private String address;
    
    @SerializedName("image_url")
    private String imageUrl;
    
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    
    @SerializedName("rating")
    private Double rating;
    
    @SerializedName("review_count")
    private Integer reviewCount;
    
    @SerializedName("opening_hours")
    private String openingHours;
    
    @SerializedName("ticket_price")
    private Double ticketPrice;
    
    @SerializedName("is_free_entry")
    private Boolean isFreeEntry;
    
    @SerializedName("website")
    private String website;
    
    @SerializedName("phone_number")
    private String phoneNumber;
    
    @SerializedName("latitude")
    private Double latitude;
    
    @SerializedName("longitude")
    private Double longitude;
    
    @SerializedName("tags")
    private List<String> tags;
    
    @SerializedName("view_count")
    private Integer viewCount;
    
    private Double distanceFromUser;

    public Place() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }

    public Double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(Double ticketPrice) { this.ticketPrice = ticketPrice; }

    public boolean isFreeEntry() { return isFreeEntry != null && isFreeEntry; }
    public void setFreeEntry(Boolean freeEntry) { isFreeEntry = freeEntry; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    // Helper to maintain compatibility
    public GeoPoint getLocation() {
        if (latitude != null && longitude != null) {
            return new GeoPoint(latitude, longitude);
        }
        return null;
    }

    public void setLocation(GeoPoint location) {
        if (location != null) {
            this.latitude = location.getLatitude();
            this.longitude = location.getLongitude();
        }
    }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Double getDistanceFromUser() { return distanceFromUser; }
    public void setDistanceFromUser(Double distanceFromUser) { this.distanceFromUser = distanceFromUser; }

    @SerializedName("estimated_duration")
    private Integer estimatedVisitDuration;

    public Integer getEstimatedVisitDuration() { return estimatedVisitDuration != null ? estimatedVisitDuration : 60; } // Default 60 mins
    public void setEstimatedVisitDuration(Integer estimatedVisitDuration) { this.estimatedVisitDuration = estimatedVisitDuration; }
}