package com.example.moresqplore;

import android.location.Location;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocationUtils {

    // Calculate distance between two coordinates using Haversine formula
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // Radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    // Get places sorted by distance from user location
    public static List<Place> getPlacesSortedByDistance(List<Place> places, Location userLocation) {
        if (userLocation == null) return places;

        List<Place> sortedPlaces = new ArrayList<>(places);

        // Calculate distance for each place
        for (Place place : sortedPlaces) {
            double distance = calculateDistance(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    place.getLatitude(),
                    place.getLongitude()
            );
            place.setDistanceFromUser(distance);
        }

        // Sort by distance
        Collections.sort(sortedPlaces, new Comparator<Place>() {
            @Override
            public int compare(Place p1, Place p2) {
                return Double.compare(p1.getDistanceFromUser(), p2.getDistanceFromUser());
            }
        });

        return sortedPlaces;
    }

    // Get nearby places within a radius
    public static List<Place> getNearbyPlaces(List<Place> places, Location userLocation, double radiusKm) {
        List<Place> nearbyPlaces = new ArrayList<>();

        if (userLocation == null) return nearbyPlaces;

        for (Place place : places) {
            double distance = calculateDistance(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    place.getLatitude(),
                    place.getLongitude()
            );

            if (distance <= radiusKm) {
                place.setDistanceFromUser(distance);
                nearbyPlaces.add(place);
            }
        }

        return nearbyPlaces;
    }
}