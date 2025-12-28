package com.example.moresqplore.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a complete travel itinerary for Morocco
 * Firebase-ready with Firestore annotations
 */
public class Itinerary implements Serializable {

    @DocumentId
    private String id;

    private String userId;
    private String title;
    private int durationDays;
    private double totalBudget;
    private double estimatedCost;
    private String startingCity;

    // User preferences
    private List<String> interests;
    private String travelStyle; // "budget", "comfort", "luxury"

    // Generated itinerary
    private List<DayPlan> dayPlans;

    // Metadata
    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    private boolean isSaved;
    private double optimizationScore; // 0-100, quality metric

    // Constructors
    public Itinerary() {
        // Required empty constructor for Firebase
        this.dayPlans = new ArrayList<>();
        this.interests = new ArrayList<>();
        this.isSaved = false;
    }

    public Itinerary(String userId, int durationDays, double totalBudget,
            String startingCity, List<String> interests, String travelStyle) {
        this();
        this.userId = userId;
        this.durationDays = durationDays;
        this.totalBudget = totalBudget;
        this.startingCity = startingCity;
        this.interests = interests;
        this.travelStyle = travelStyle;
        this.title = generateTitle();
    }

    // Helper methods
    @Exclude
    private String generateTitle() {
        return durationDays + "-Day Morocco Adventure from " + startingCity;
    }

    @Exclude
    public int getTotalActivities() {
        int count = 0;
        for (DayPlan day : dayPlans) {
            count += day.getActivities().size();
        }
        return count;
    }

    @Exclude
    public boolean isWithinBudget() {
        return estimatedCost <= totalBudget;
    }

    @Exclude
    public double getBudgetUtilization() {
        return (estimatedCost / totalBudget) * 100;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public String getStartingCity() {
        return startingCity;
    }

    public void setStartingCity(String startingCity) {
        this.startingCity = startingCity;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public String getTravelStyle() {
        return travelStyle;
    }

    public void setTravelStyle(String travelStyle) {
        this.travelStyle = travelStyle;
    }

    public List<DayPlan> getDayPlans() {
        return dayPlans;
    }

    public void setDayPlans(List<DayPlan> dayPlans) {
        this.dayPlans = dayPlans;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public double getOptimizationScore() {
        return optimizationScore;
    }

    public void setOptimizationScore(double optimizationScore) {
        this.optimizationScore = optimizationScore;
    }
}
