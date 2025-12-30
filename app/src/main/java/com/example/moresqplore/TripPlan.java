package com.example.moresqplore;

import java.util.ArrayList;
import java.util.List;
import com.example.moresqplore.data.model.Place;

public class TripPlan {
    private String planName;
    private int durationMinutes; // 240 for half-day, 480 for full-day
    private List<Place> places;
    private double totalDistance;
    private int totalTravelTime;

    public TripPlan(String planName, int durationMinutes) {
        this.planName = planName;
        this.durationMinutes = durationMinutes;
        this.places = new ArrayList<>();
    }

    public void addPlace(Place place) {
        places.add(place);
    }

    public String getPlanName() { return planName; }
    public void setPlanName(String name) { this.planName = name; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int duration) { this.durationMinutes = duration; }

    public List<Place> getPlaces() { return places; }
    public void setPlaces(List<Place> places) { this.places = places; }

    public double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(double distance) { this.totalDistance = distance; }

    public int getTotalTravelTime() { return totalTravelTime; }
    public void setTotalTravelTime(int time) { this.totalTravelTime = time; }
}