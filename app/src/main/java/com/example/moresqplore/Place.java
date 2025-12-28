package com.example.moresqplore;

import java.io.Serializable;

public class Place implements Serializable {
    private int id;
    private String name;
    private String description;
    private String city;
    private String category; // Historical, Beach, Mountain, Cultural, etc.
    private double latitude;
    private double longitude;
    private float averageRating;
    private int totalReviews;
    private String imageUrl;
    private int estimatedVisitDuration; // in minutes
    private String openingHours;
    private boolean requiresTicket;
    private double ticketPrice;
    private double distanceFromUser; // in kilometers

    // Basic constructor (your current one)
    public Place(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.averageRating = 0.0f;
        this.totalReviews = 0;
    }

    // Full constructor
    public Place(int id, String name, String description, String city, String category,
                 double latitude, double longitude, int estimatedVisitDuration,
                 String openingHours, boolean requiresTicket, double ticketPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.city = city;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
        this.estimatedVisitDuration = estimatedVisitDuration;
        this.openingHours = openingHours;
        this.requiresTicket = requiresTicket;
        this.ticketPrice = ticketPrice;
        this.averageRating = 0.0f;
        this.totalReviews = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public float getAverageRating() { return averageRating; }
    public void setAverageRating(float averageRating) { this.averageRating = averageRating; }

    public int getTotalReviews() { return totalReviews; }
    public void setTotalReviews(int totalReviews) { this.totalReviews = totalReviews; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getEstimatedVisitDuration() { return estimatedVisitDuration; }
    public void setEstimatedVisitDuration(int duration) { this.estimatedVisitDuration = duration; }

    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String hours) { this.openingHours = hours; }

    public boolean isRequiresTicket() { return requiresTicket; }
    public void setRequiresTicket(boolean requiresTicket) { this.requiresTicket = requiresTicket; }

    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double price) { this.ticketPrice = price; }

    public double getDistanceFromUser() { return distanceFromUser; }
    public void setDistanceFromUser(double distance) { this.distanceFromUser = distance; }
}