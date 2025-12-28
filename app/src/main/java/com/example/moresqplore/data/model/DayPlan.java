package com.example.moresqplore.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single day in an itinerary
 * Firebase-compatible POJO
 */
public class DayPlan implements Serializable {

    private int dayNumber;
    private String date; // ISO format: "2024-01-15"
    private String city;
    private List<Activity> activities;
    private double dailyBudget;
    private double estimatedCost;
    private int totalTravelTimeMinutes;
    private String summary; // AI-generated day summary

    // Constructors
    public DayPlan() {
        // Required for Firebase
        this.activities = new ArrayList<>();
    }

    public DayPlan(int dayNumber, String date, String city) {
        this();
        this.dayNumber = dayNumber;
        this.date = date;
        this.city = city;
    }

    // Helper methods
    public void addActivity(Activity activity) {
        activities.add(activity);
        recalculateCost();
    }

    public void recalculateCost() {
        estimatedCost = 0;
        totalTravelTimeMinutes = 0;
        for (Activity activity : activities) {
            estimatedCost += activity.getEstimatedCost();
            totalTravelTimeMinutes += activity.getDurationMinutes();
        }
    }

    public boolean isWithinBudget() {
        return estimatedCost <= dailyBudget;
    }

    public int getActivityCount() {
        return activities.size();
    }

    // Getters and Setters
    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        recalculateCost();
    }

    public double getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(double dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public double getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(double estimatedCost) {
        this.estimatedCost = estimatedCost;
    }

    public int getTotalTravelTimeMinutes() {
        return totalTravelTimeMinutes;
    }

    public void setTotalTravelTimeMinutes(int totalTravelTimeMinutes) {
        this.totalTravelTimeMinutes = totalTravelTimeMinutes;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
