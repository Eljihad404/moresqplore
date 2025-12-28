package com.example.moresqplore.data.model;

import java.io.Serializable;

/**
 * Represents a single price offer from a booking provider
 */
public class PriceOffer implements Serializable {

    private String providerId; // BookingProvider enum name
    private String providerName;
    private double price;
    private String currency;
    private String bookingUrl;
    private double rating;
    private int reviewCount;
    private boolean isAvailable;
    private String specialOffer; // e.g., "10% off", "Free cancellation"
    private boolean isBestDeal; // Lowest price flag

    // Empty constructor for Firebase
    public PriceOffer() {
        this.currency = "MAD";
        this.isAvailable = true;
    }

    public PriceOffer(String providerId, String providerName, double price) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.price = price;
        this.currency = "MAD";
        this.isAvailable = true;
    }

    // Getters and Setters
    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBookingUrl() {
        return bookingUrl;
    }

    public void setBookingUrl(String bookingUrl) {
        this.bookingUrl = bookingUrl;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getSpecialOffer() {
        return specialOffer;
    }

    public void setSpecialOffer(String specialOffer) {
        this.specialOffer = specialOffer;
    }

    public boolean isBestDeal() {
        return isBestDeal;
    }

    public void setBestDeal(boolean bestDeal) {
        isBestDeal = bestDeal;
    }

    /**
     * Get formatted price string
     */
    public String getFormattedPrice() {
        return String.format("%.0f %s", price, currency);
    }

    /**
     * Get savings compared to a reference price
     */
    public double getSavings(double referencePrice) {
        return referencePrice - price;
    }

    /**
     * Get savings percentage
     */
    public int getSavingsPercentage(double referencePrice) {
        if (referencePrice == 0)
            return 0;
        return (int) ((referencePrice - price) / referencePrice * 100);
    }
}
