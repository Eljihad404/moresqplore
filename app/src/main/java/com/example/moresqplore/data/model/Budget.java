package com.example.moresqplore.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a trip budget
 */
public class Budget implements Serializable {

    @DocumentId
    private String id;

    private String userId;
    private String tripId;
    private String tripName;

    private double totalBudget; // In MAD
    private double dailyBudget; // In MAD

    // Category budgets (optional)
    private Map<String, Double> categoryBudgets;

    private String startDate; // ISO format
    private String endDate;
    private int durationDays;

    // Calculated fields
    private double totalSpent; // Sum of all expenses
    private double remaining; // totalBudget - totalSpent

    // Alert thresholds
    private double alertThreshold80; // 80% of budget
    private double alertThreshold100; // 100% of budget
    private boolean alert80Triggered;
    private boolean alert100Triggered;

    @ServerTimestamp
    private Date createdAt;

    @ServerTimestamp
    private Date updatedAt;

    // Empty constructor for Firebase
    public Budget() {
        this.categoryBudgets = new HashMap<>();
        this.totalSpent = 0;
        this.alert80Triggered = false;
        this.alert100Triggered = false;
    }

    public Budget(double totalBudget, int durationDays) {
        this();
        this.totalBudget = totalBudget;
        this.durationDays = durationDays;
        this.dailyBudget = totalBudget / durationDays;
        this.remaining = totalBudget;
        calculateAlertThresholds();
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

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public double getTotalBudget() {
        return totalBudget;
    }

    public void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
        this.dailyBudget = totalBudget / Math.max(1, durationDays);
        calculateAlertThresholds();
    }

    public double getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(double dailyBudget) {
        this.dailyBudget = dailyBudget;
    }

    public Map<String, Double> getCategoryBudgets() {
        return categoryBudgets;
    }

    public void setCategoryBudgets(Map<String, Double> categoryBudgets) {
        this.categoryBudgets = categoryBudgets;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
        this.dailyBudget = totalBudget / Math.max(1, durationDays);
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
        this.remaining = totalBudget - totalSpent;
        checkAlerts();
    }

    public double getRemaining() {
        return remaining;
    }

    public void setRemaining(double remaining) {
        this.remaining = remaining;
    }

    public double getAlertThreshold80() {
        return alertThreshold80;
    }

    public double getAlertThreshold100() {
        return alertThreshold100;
    }

    public boolean isAlert80Triggered() {
        return alert80Triggered;
    }

    public void setAlert80Triggered(boolean alert80Triggered) {
        this.alert80Triggered = alert80Triggered;
    }

    public boolean isAlert100Triggered() {
        return alert100Triggered;
    }

    public void setAlert100Triggered(boolean alert100Triggered) {
        this.alert100Triggered = alert100Triggered;
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

    /**
     * Calculate alert thresholds
     */
    private void calculateAlertThresholds() {
        this.alertThreshold80 = totalBudget * 0.8;
        this.alertThreshold100 = totalBudget;
    }

    /**
     * Check if alerts should be triggered
     */
    private void checkAlerts() {
        if (totalSpent >= alertThreshold80 && !alert80Triggered) {
            alert80Triggered = true;
        }
        if (totalSpent >= alertThreshold100 && !alert100Triggered) {
            alert100Triggered = true;
        }
    }

    /**
     * Get budget progress percentage
     */
    @com.google.firebase.firestore.Exclude
    public int getProgressPercentage() {
        if (totalBudget == 0)
            return 0;
        return (int) ((totalSpent / totalBudget) * 100);
    }

    /**
     * Get status color based on spending
     */
    @com.google.firebase.firestore.Exclude
    public String getStatusColor() {
        int progress = getProgressPercentage();
        if (progress < 70)
            return "#27AE60"; // Green
        if (progress < 90)
            return "#F39C12"; // Orange
        return "#E74C3C"; // Red
    }
}
