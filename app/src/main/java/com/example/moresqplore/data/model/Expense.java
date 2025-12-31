package com.example.moresqplore.data.model;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

/**
 * Model representing a single expense
 */
public class Expense implements Serializable {

    @DocumentId
    private String id;

    private String userId;
    private String tripId; // Link to itinerary

    private double amount;
    private String currency; // MAD, USD, EUR, GBP
    private double amountInMAD; // Converted amount

    private String category; // ExpenseCategory enum name
    private String description;

    private String date; // ISO format
    private String time; // HH:mm

    private String paymentMethod; // Cash, Card
    private String location; // Optional

    @ServerTimestamp
    private Date createdAt;

    // Empty constructor for Firebase
    public Expense() {
        this.currency = "MAD";
        this.paymentMethod = "Cash";
    }

    public Expense(double amount, String category, String description) {
        this();
        this.amount = amount;
        this.amountInMAD = amount;
        this.category = category;
        this.description = description;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getAmountInMAD() {
        return amountInMAD;
    }

    public void setAmountInMAD(double amountInMAD) {
        this.amountInMAD = amountInMAD;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get formatted amount with currency
     */
    public String getFormattedAmount() {
        return String.format("%.2f %s", amount, currency);
    }

    /**
     * Get category enum
     */
    public ExpenseCategory getCategoryEnum() {
        try {
            return ExpenseCategory.valueOf(category);
        } catch (Exception e) {
            return ExpenseCategory.OTHER;
        }
    }
}
