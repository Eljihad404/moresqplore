package com.example.moresqplore.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents a price comparison for hotels, flights, or activities
 */
public class PriceComparison implements Serializable {

    // Item types
    public static final String TYPE_HOTEL = "HOTEL";
    public static final String TYPE_FLIGHT = "FLIGHT";
    public static final String TYPE_ACTIVITY = "ACTIVITY";

    @DocumentId
    private String id;

    private String itemType; // HOTEL, FLIGHT, ACTIVITY
    private String itemName;
    private String location; // City or place name
    private String checkInDate; // ISO format for hotels/activities
    private String checkOutDate; // ISO format for hotels
    private int guests;

    private List<PriceOffer> offers;

    @ServerTimestamp
    private Date lastUpdated;

    private String currency;

    // Cached statistics
    private double minPrice;
    private double maxPrice;
    private double avgPrice;

    // Empty constructor for Firebase
    public PriceComparison() {
        this.offers = new ArrayList<>();
        this.currency = "MAD";
        this.guests = 1;
    }

    public PriceComparison(String itemType, String itemName, String location) {
        this.itemType = itemType;
        this.itemName = itemName;
        this.location = location;
        this.offers = new ArrayList<>();
        this.currency = "MAD";
        this.guests = 1;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getGuests() {
        return guests;
    }

    public void setGuests(int guests) {
        this.guests = guests;
    }

    public List<PriceOffer> getOffers() {
        return offers;
    }

    public void setOffers(List<PriceOffer> offers) {
        this.offers = offers;
        calculateStatistics();
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    /**
     * Add an offer and recalculate statistics
     */
    public void addOffer(PriceOffer offer) {
        if (offers == null) {
            offers = new ArrayList<>();
        }
        offers.add(offer);
        calculateStatistics();
    }

    /**
     * Calculate price statistics from offers
     */
    private void calculateStatistics() {
        if (offers == null || offers.isEmpty()) {
            minPrice = 0;
            maxPrice = 0;
            avgPrice = 0;
            return;
        }

        double sum = 0;
        minPrice = Double.MAX_VALUE;
        maxPrice = 0;

        for (PriceOffer offer : offers) {
            if (offer.isAvailable()) {
                double price = offer.getPrice();
                sum += price;
                if (price < minPrice)
                    minPrice = price;
                if (price > maxPrice)
                    maxPrice = price;
            }
        }

        avgPrice = sum / offers.size();

        // Mark best deal
        for (PriceOffer offer : offers) {
            offer.setBestDeal(offer.getPrice() == minPrice && offer.isAvailable());
        }
    }

    /**
     * Get number of available offers
     */
    public int getAvailableOffersCount() {
        if (offers == null)
            return 0;
        int count = 0;
        for (PriceOffer offer : offers) {
            if (offer.isAvailable())
                count++;
        }
        return count;
    }

    /**
     * Check if data is stale (older than 1 hour)
     */
    public boolean isStale() {
        if (lastUpdated == null)
            return true;
        long hourInMillis = 60 * 60 * 1000;
        return (System.currentTimeMillis() - lastUpdated.getTime()) > hourInMillis;
    }
}
