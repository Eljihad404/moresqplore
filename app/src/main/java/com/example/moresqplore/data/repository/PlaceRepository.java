package com.example.moresqplore.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.Place;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing Place data operations with Firebase Firestore.
 * Handles all CRUD operations and queries for tourist places in Morocco.
 *
 * This repository follows the MVVM architecture pattern and provides
 * a clean API for data access to the ViewModels.
 */
public class PlaceRepository {

    private static final String TAG = "PlaceRepository";
    private static final String PLACES_COLLECTION = "places";
    private static final String CATEGORIES_COLLECTION = "categories";

    // Singleton instance
    private static volatile PlaceRepository instance;
    private final FirebaseFirestore firestore;

    // Cache for places data
    private final MutableLiveData<List<Place>> cachedPlaces;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;

    private PlaceRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.cachedPlaces = new MutableLiveData<>(new ArrayList<>());
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
    }

    /**
     * Returns the singleton instance of PlaceRepository.
     * Thread-safe implementation with double-checked locking.
     *
     * @return PlaceRepository instance
     */
    public static PlaceRepository getInstance() {
        if (instance == null) {
            synchronized (PlaceRepository.class) {
                if (instance == null) {
                    instance = new PlaceRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance (useful for testing).
     */
    public static void resetInstance() {
        synchronized (PlaceRepository.class) {
            instance = null;
        }
    }

    /**
     * Gets the LiveData for observing places list.
     *
     * @return LiveData containing list of places
     */
    public LiveData<List<Place>> getPlaces() {
        return cachedPlaces;
    }

    /**
     * Gets the LiveData for observing loading state.
     *
     * @return LiveData containing loading state boolean
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Gets the LiveData for observing error messages.
     *
     * @return LiveData containing error message string
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Fetches all places from Firestore.
     * Updates the cached places list and loading state.
     */
    public void fetchAllPlaces() {
        setLoading(true);
        firestore.collection(PLACES_COLLECTION)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        setLoading(false);
                        if (task.isSuccessful()) {
                            List<Place> places = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Place place = document.toObject(Place.class);
                                place.setId(document.getId());
                                places.add(place);
                            }
                            updateCachedPlaces(places);
                            Log.d(TAG, "Successfully fetched " + places.size() + " places");
                        } else {
                            String error = "Failed to fetch places: " + task.getException().getMessage();
                            Log.e(TAG, error, task.getException());
                            setError(error);
                        }
                    }
                });
    }

    /**
     * Fetches a single place by its ID.
     *
     * @param placeId The ID of the place to fetch
     * @return LiveData containing the Place object
     */
    public LiveData<Place> fetchPlaceById(String placeId) {
        MutableLiveData<Place> placeLiveData = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .document(placeId)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        Place place = task.getResult().toObject(Place.class);
                        if (place != null) {
                            place.setId(placeId);
                            placeLiveData.setValue(place);
                        } else {
                            setError("Place not found");
                        }
                    } else {
                        String error = "Failed to fetch place: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return placeLiveData;
    }

    /**
     * Fetches places by category.
     *
     * @param category The category to filter by
     * @return LiveData containing list of places in the category
     */
    public LiveData<List<Place>> fetchPlacesByCategory(String category) {
        MutableLiveData<List<Place>> categoryPlaces = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .whereEqualTo("category", category)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        List<Place> places = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Place place = document.toObject(Place.class);
                            place.setId(document.getId());
                            places.add(place);
                        }
                        categoryPlaces.setValue(places);
                    } else {
                        String error = "Failed to fetch places by category: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return categoryPlaces;
    }

    /**
     * Fetches places within a specified radius from a location.
     * Uses geospatial queries for efficient nearby place search.
     *
     * @param latitude  Center latitude
     * @param longitude Center longitude
     * @param radiusKm  Search radius in kilometers
     * @return LiveData containing list of nearby places
     */
    public LiveData<List<Place>> fetchNearbyPlaces(double latitude, double longitude, double radiusKm) {
        MutableLiveData<List<Place>> nearbyPlaces = new MutableLiveData<>();
        setLoading(true);

        // Calculate bounding box for initial filtering
        double latDelta = radiusKm / 111.0; // Approximate km per degree latitude
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)));

        double minLat = latitude - latDelta;
        double maxLat = latitude + latDelta;
        double minLon = longitude - lonDelta;
        double maxLon = longitude + lonDelta;

        firestore.collection(PLACES_COLLECTION)
                .whereGreaterThanOrEqualTo("location.latitude", minLat)
                .whereLessThanOrEqualTo("location.latitude", maxLat)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        List<Place> places = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Place place = document.toObject(Place.class);
                            place.setId(document.getId());

                            // Calculate exact distance and filter
                            double distance = calculateDistance(
                                    latitude, longitude,
                                    place.getLocation().getLatitude(),
                                    place.getLocation().getLongitude()
                            );

                            if (distance <= radiusKm) {
                                place.setDistanceFromUser(distance);
                                places.add(place);
                            }
                        }

                        // Sort by distance
                        sortPlacesByDistance(places);
                        nearbyPlaces.setValue(places);
                    } else {
                        String error = "Failed to fetch nearby places: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return nearbyPlaces;
    }

    /**
     * Searches places by name or description.
     *
     * @param query Search query string
     * @return LiveData containing list of matching places
     */
    public LiveData<List<Place>> searchPlaces(String query) {
        MutableLiveData<List<Place>> searchResults = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .orderBy("name")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        List<Place> places = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Place place = document.toObject(Place.class);
                            place.setId(document.getId());
                            places.add(place);
                        }
                        searchResults.setValue(places);
                    } else {
                        String error = "Failed to search places: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return searchResults;
    }

    /**
     * Fetches top-rated places sorted by rating.
     *
     * @param limit Maximum number of places to return
     * @return LiveData containing list of top-rated places
     */
    public LiveData<List<Place>> fetchTopRatedPlaces(int limit) {
        MutableLiveData<List<Place>> topRatedPlaces = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .orderBy("rating", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        List<Place> places = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Place place = document.toObject(Place.class);
                            place.setId(document.getId());
                            places.add(place);
                        }
                        topRatedPlaces.setValue(places);
                    } else {
                        String error = "Failed to fetch top rated places: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return topRatedPlaces;
    }

    /**
     * Fetches places for a specific city or region.
     *
     * @param city The city name to filter by
     * @return LiveData containing list of places in the city
     */
    public LiveData<List<Place>> fetchPlacesByCity(String city) {
        MutableLiveData<List<Place>> cityPlaces = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .whereEqualTo("city", city)
                .get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        List<Place> places = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Place place = document.toObject(Place.class);
                            place.setId(document.getId());
                            places.add(place);
                        }
                        cityPlaces.setValue(places);
                    } else {
                        String error = "Failed to fetch places by city: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                    }
                });

        return cityPlaces;
    }

    /**
     * Adds a new place to Firestore.
     *
     * @param place The Place object to add
     * @return LiveData containing the ID of the newly created place
     */
    public LiveData<String> addPlace(Place place) {
        MutableLiveData<String> resultLiveData = new MutableLiveData<>();
        setLoading(true);

        Map<String, Object> placeData = createPlaceMap(place);

        firestore.collection(PLACES_COLLECTION)
                .add(placeData)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        String placeId = task.getResult().getId();
                        Log.d(TAG, "Place added successfully with ID: " + placeId);
                        resultLiveData.setValue(placeId);

                        // Update cache
                        List<Place> currentPlaces = cachedPlaces.getValue();
                        if (currentPlaces != null) {
                            place.setId(placeId);
                            currentPlaces.add(place);
                            cachedPlaces.postValue(currentPlaces);
                        }
                    } else {
                        String error = "Failed to add place: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                        resultLiveData.setValue(null);
                    }
                });

        return resultLiveData;
    }

    /**
     * Updates an existing place in Firestore.
     *
     * @param place The Place object with updated data
     * @return LiveData containing true if update was successful
     */
    public LiveData<Boolean> updatePlace(Place place) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        setLoading(true);

        Map<String, Object> placeData = createPlaceMap(place);

        firestore.collection(PLACES_COLLECTION)
                .document(place.getId())
                .set(placeData)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Place updated successfully: " + place.getId());
                        resultLiveData.setValue(true);

                        // Update cache
                        updatePlaceInCache(place);
                    } else {
                        String error = "Failed to update place: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                        resultLiveData.setValue(false);
                    }
                });

        return resultLiveData;
    }

    /**
     * Deletes a place from Firestore.
     *
     * @param placeId The ID of the place to delete
     * @return LiveData containing true if deletion was successful
     */
    public LiveData<Boolean> deletePlace(String placeId) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();
        setLoading(true);

        firestore.collection(PLACES_COLLECTION)
                .document(placeId)
                .delete()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Place deleted successfully: " + placeId);
                        resultLiveData.setValue(true);

                        // Update cache
                        removePlaceFromCache(placeId);
                    } else {
                        String error = "Failed to delete place: " +
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error");
                        Log.e(TAG, error);
                        setError(error);
                        resultLiveData.setValue(false);
                    }
                });

        return resultLiveData;
    }

    /**
     * Increments the view count for a place.
     *
     * @param placeId The ID of the place
     */
    public void incrementViewCount(String placeId) {
        firestore.collection(PLACES_COLLECTION)
                .document(placeId)
                .update("viewCount", com.google.firebase.firestore.FieldValue.increment(1));
    }

    /**
     * Adds a review to a place.
     *
     * @param placeId  The ID of the place
     * @param rating   The rating value (1-5)
     * @param comment  The review comment
     * @param userId   The ID of the user
     * @param userName The name of the user
     */
    public void addReview(String placeId, float rating, String comment, String userId, String userName) {
        Map<String, Object> review = new HashMap<>();
        review.put("userId", userId);
        review.put("userName", userName);
        review.put("rating", rating);
        review.put("comment", comment);
        review.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firestore.collection(PLACES_COLLECTION)
                .document(placeId)
                .collection("reviews")
                .add(review)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Review added successfully for place: " + placeId);
                        // Update place's average rating
                        updatePlaceRating(placeId);
                    } else {
                        Log.e(TAG, "Failed to add review", task.getException());
                    }
                });
    }

    /**
     * Updates the average rating for a place based on all reviews.
     *
     * @param placeId The ID of the place
     */
    private void updatePlaceRating(String placeId) {
        firestore.collection(PLACES_COLLECTION)
                .document(placeId)
                .collection("reviews")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double totalRating = 0;
                        int reviewCount = task.getResult().size();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            totalRating += document.getDouble("rating");
                        }

                        double averageRating = reviewCount > 0 ? totalRating / reviewCount : 0;

                        firestore.collection(PLACES_COLLECTION)
                                .document(placeId)
                                .update("rating", averageRating)
                                .addOnCompleteListener(updateTask -> {
                                    if (updateTask.isSuccessful()) {
                                        Log.d(TAG, "Place rating updated: " + averageRating);
                                    }
                                });
                    }
                });
    }

    /**
     * Creates a optimized trajectory for visiting multiple places.
     * Uses a simple nearest-neighbor algorithm for route optimization.
     *
     * @param places    List of places to visit
     * @param startLat  Starting latitude
     * @param startLon  Starting longitude
     * @return List of places in optimized order
     */
    public List<Place> optimizeTrajectory(List<Place> places, double startLat, double startLon) {
        if (places == null || places.isEmpty()) {
            return new ArrayList<>();
        }

        List<Place> optimizedRoute = new ArrayList<>();
        List<Place> remainingPlaces = new ArrayList<>(places);

        // Current position
        double currentLat = startLat;
        double currentLon = startLon;

        // Find nearest place repeatedly
        while (!remainingPlaces.isEmpty()) {
            Place nearestPlace = null;
            double nearestDistance = Double.MAX_VALUE;
            int nearestIndex = -1;

            for (int i = 0; i < remainingPlaces.size(); i++) {
                Place place = remainingPlaces.get(i);
                GeoPoint location = place.getLocation();
                if (location != null) {
                    double distance = calculateDistance(
                            currentLat, currentLon,
                            location.getLatitude(),
                            location.getLongitude()
                    );

                    if (distance < nearestDistance) {
                        nearestDistance = distance;
                        nearestPlace = place;
                        nearestIndex = i;
                    }
                }
            }

            if (nearestPlace != null) {
                optimizedRoute.add(nearestPlace);
                remainingPlaces.remove(nearestIndex);

                // Update current position
                GeoPoint location = nearestPlace.getLocation();
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                }
            }
        }

        return optimizedRoute;
    }

    /**
     * Calculates the distance between two points using the Haversine formula.
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Sorts a list of places by distance from user location.
     *
     * @param places List of places to sort
     */
    private void sortPlacesByDistance(List<Place> places) {
        places.sort((p1, p2) -> {
            Double distance1 = p1.getDistanceFromUser() != null ? p1.getDistanceFromUser() : Double.MAX_VALUE;
            Double distance2 = p2.getDistanceFromUser() != null ? p2.getDistanceFromUser() : Double.MAX_VALUE;
            return distance1.compareTo(distance2);
        });
    }

    /**
     * Creates a Map representation of a Place object for Firestore.
     *
     * @param place The Place object
     * @return Map containing place data
     */
    private Map<String, Object> createPlaceMap(Place place) {
        Map<String, Object> placeData = new HashMap<>();

        placeData.put("name", place.getName());
        placeData.put("description", place.getDescription());
        placeData.put("category", place.getCategory());
        placeData.put("city", place.getCity());
        placeData.put("address", place.getAddress());
        placeData.put("imageUrl", place.getImageUrl());
        placeData.put("thumbnailUrl", place.getThumbnailUrl());
        placeData.put("rating", place.getRating() != null ? place.getRating() : 0.0);
        placeData.put("reviewCount", place.getReviewCount() != null ? place.getReviewCount() : 0);
        placeData.put("openingHours", place.getOpeningHours());
        placeData.put("ticketPrice", place.getTicketPrice());
        placeData.put("isFreeEntry", place.isFreeEntry());
        placeData.put("website", place.getWebsite());
        placeData.put("phoneNumber", place.getPhoneNumber());
        placeData.put("viewCount", place.getViewCount() != null ? place.getViewCount() : 0);

        if (place.getLocation() != null) {
            Map<String, Double> locationMap = new HashMap<>();
            locationMap.put("latitude", place.getLocation().getLatitude());
            locationMap.put("longitude", place.getLocation().getLongitude());
            placeData.put("location", locationMap);
        }

        if (place.getTags() != null) {
            placeData.put("tags", place.getTags());
        }

        placeData.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        placeData.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        return placeData;
    }

    /**
     * Updates the cached places list.
     *
     * @param places New list of places
     */
    private void updateCachedPlaces(List<Place> places) {
        if (places != null) {
            cachedPlaces.postValue(new ArrayList<>(places));
        }
    }

    /**
     * Updates a single place in the cache.
     *
     * @param updatedPlace The updated Place object
     */
    private void updatePlaceInCache(Place updatedPlace) {
        List<Place> currentPlaces = cachedPlaces.getValue();
        if (currentPlaces != null) {
            for (int i = 0; i < currentPlaces.size(); i++) {
                if (currentPlaces.get(i).getId().equals(updatedPlace.getId())) {
                    currentPlaces.set(i, updatedPlace);
                    break;
                }
            }
            cachedPlaces.postValue(new ArrayList<>(currentPlaces));
        }
    }

    /**
     * Removes a place from the cache.
     *
     * @param placeId The ID of the place to remove
     */
    private void removePlaceFromCache(String placeId) {
        List<Place> currentPlaces = cachedPlaces.getValue();
        if (currentPlaces != null) {
            currentPlaces.removeIf(place -> place.getId().equals(placeId));
            cachedPlaces.postValue(new ArrayList<>(currentPlaces));
        }
    }

    /**
     * Sets the loading state.
     *
     * @param loading New loading state
     */
    private void setLoading(boolean loading) {
        isLoading.postValue(loading);
    }

    /**
     * Sets an error message.
     *
     * @param message Error message
     */
    private void setError(String message) {
        errorMessage.postValue(message);
    }

    /**
     * Clears the current error message.
     */
    public void clearError() {
        errorMessage.postValue(null);
    }
}