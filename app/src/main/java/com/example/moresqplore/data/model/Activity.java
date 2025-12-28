package com.example.moresqplore.data.model;

import java.io.Serializable;

/**
 * Represents a single activity in a day plan
 * Firebase-compatible POJO
 */
public class Activity implements Serializable {

    // Activity types
    public static final String TYPE_VISIT = "visit";
    public static final String TYPE_MEAL = "meal";
    public static final String TYPE_TRANSPORT = "transport";
    public static final String TYPE_ACCOMMODATION = "accommodation";
    public static final String TYPE_EXPERIENCE = "experience";

    private String activityType;
    private String placeId; // Reference to Place object
    private String placeName;
    private String description;

    // Timing
    private String startTime; // "09:00"
    private String endTime; // "11:30"
    private int durationMinutes;

    // Location
    private double latitude;
    private double longitude;
    private String city;

    // Cost
    private double estimatedCost;
    private String currency; // "MAD"

    // Additional info
    private String notes;
    private boolean requiresBooking;
    private int travelTimeFromPrevious; // minutes

    // Constructors
    public Activity() {
        // Required for Firebase
        this.currency = "MAD";
    }

    public Activity(String activityType, String placeName, String startTime,
            int durationMinutes, double estimatedCost) {
        this();
        this.activityType = activityType;
        this.placeName = placeName;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.estimatedCost = estimatedCost;
    }

    // Helper methods
    public String getTimeRange() {
        return startTime + " - " + endTime;
    }

    public String getFormattedCost() {
        return String.format("%.2f %s", estimatedCost, currency);
    }

    public boolean isVisitActivity() {
        return TYPE_VISIT.equals(activityType);
    }

    // Getters and Setters
    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isRequiresBooking() {
        return requiresBooking;
    }

    public void setRequiresBooking(boolean requiresBooking) {
        this.requiresBooking = requiresBooking;
    }

    public int getTravelTimeFromPrevious() {
        return travelTimeFromPrevious;
    }

    public void setTravelTimeFromPrevious(int travelTimeFromPrevious) {
        this.travelTimeFromPrevious = travelTimeFromPrevious;
    }
}
