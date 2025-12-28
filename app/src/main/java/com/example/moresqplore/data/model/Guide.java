package com.example.moresqplore.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model representing a local guide in the marketplace
 */
public class Guide implements Serializable {

    @DocumentId
    private String id;

    private String userId; // Firebase Auth UID
    private String name;
    private String photoUrl;
    private String bio;
    private List<String> languages; // e.g., ["Arabic", "English", "French"]
    private List<String> specializations; // e.g., ["City Tours", "Food Tours"]
    private String city; // Primary operating city

    // Pricing
    private double hourlyRate; // MAD per hour
    private double dayRate; // MAD per day

    // Stats
    private double rating; // Average rating (0-5)
    private int totalReviews;
    private int totalBookings;

    // Verification
    private boolean isVerified;
    private String verificationBadge; // "Verified", "Top Rated", "New"

    // Availability
    private boolean isAvailable;
    private String availabilityNote; // e.g., "Available weekends only"

    @ServerTimestamp
    private Date joinedDate;

    @ServerTimestamp
    private Date lastActive;

    // Empty constructor for Firebase
    public Guide() {
        this.languages = new ArrayList<>();
        this.specializations = new ArrayList<>();
        this.isAvailable = true;
        this.rating = 0.0;
        this.totalReviews = 0;
        this.totalBookings = 0;
    }

    public Guide(String name, String city) {
        this();
        this.name = name;
        this.city = city;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public double getDayRate() {
        return dayRate;
    }

    public void setDayRate(double dayRate) {
        this.dayRate = dayRate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public int getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(int totalBookings) {
        this.totalBookings = totalBookings;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public String getVerificationBadge() {
        return verificationBadge;
    }

    public void setVerificationBadge(String verificationBadge) {
        this.verificationBadge = verificationBadge;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getAvailabilityNote() {
        return availabilityNote;
    }

    public void setAvailabilityNote(String availabilityNote) {
        this.availabilityNote = availabilityNote;
    }

    public Date getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(Date joinedDate) {
        this.joinedDate = joinedDate;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    /**
     * Get formatted languages string
     */
    public String getLanguagesString() {
        if (languages == null || languages.isEmpty())
            return "";
        return String.join(", ", languages);
    }

    /**
     * Get formatted specializations string
     */
    public String getSpecializationsString() {
        if (specializations == null || specializations.isEmpty())
            return "";
        return String.join(", ", specializations);
    }

    /**
     * Calculate badge based on stats
     */
    public void calculateBadge() {
        if (rating >= 4.8 && totalReviews >= 50) {
            verificationBadge = "Top Rated";
        } else if (isVerified) {
            verificationBadge = "Verified";
        } else if (totalBookings < 5) {
            verificationBadge = "New";
        } else {
            verificationBadge = null;
        }
    }
}
