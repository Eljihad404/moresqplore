package com.example.moresqplore.ui.places;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.repository.PlaceRepository;

import java.util.List;

/**
 * ViewModel for managing Place details and related data.
 * Acts as a communication center between the UI and the data repository.
 * Follows the MVVM architecture pattern for clean separation of concerns.
 */
public class PlaceDetailsViewModel extends AndroidViewModel {

    private final PlaceRepository placeRepository;

    // Single place data
    private final MutableLiveData<Place> place = new MutableLiveData<>();
    private final MutableLiveData<String> placeId = new MutableLiveData<>();

    // Loading and error states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Places list data
    private final MutableLiveData<List<Place>> nearbyPlaces = new MutableLiveData<>();
    private final MutableLiveData<List<Place>> filteredPlaces = new MutableLiveData<>();
    private final LiveData<List<Place>> allPlaces;

    // Search functionality
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>();
    private final MutableLiveData<List<Place>> searchResults = new MutableLiveData<>();

    // Active filters
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<Double> searchRadius = new MutableLiveData<>(10.0);

    // Observer for repository loading state
    private final Observer<Boolean> loadingObserver = isLoadingRepo -> {
        if (!isLoadingRepo) {
            // Repository finished loading, check if we need to update our loading state
            if (Boolean.TRUE.equals(isLoading.getValue())) {
                isLoading.setValue(false);
            }
        }
    };

    // Observer for repository error messages
    private final Observer<String> errorObserver = error -> {
        if (error != null && !error.isEmpty()) {
            errorMessage.setValue(error);
        }
    };

    /**
     * Constructor initializes the ViewModel with Application context.
     * Sets up the PlaceRepository singleton and observes repository states.
     *
     * @param application Application context for AndroidViewModel
     */
    public PlaceDetailsViewModel(@NonNull Application application) {
        super(application);
        placeRepository = PlaceRepository.getInstance();

        // Get the all places LiveData from repository
        allPlaces = placeRepository.getPlaces();

        // Observe repository loading state
        placeRepository.getIsLoading().observeForever(loadingObserver);

        // Observe repository error messages
        placeRepository.getErrorMessage().observeForever(errorObserver);

        // Initial data fetch
        fetchAllPlaces();
    }

    // ==================== PLACE DATA OPERATIONS ====================

    /**
     * Loads a specific place by its ID.
     * Uses the repository's fetchPlaceById method and observes the result.
     *
     * @param placeId The unique identifier of the place to load
     */
    public void loadPlace(String placeId) {
        if (placeId == null || placeId.isEmpty()) {
            errorMessage.setValue("Invalid place ID");
            return;
        }

        this.placeId.setValue(placeId);
        isLoading.setValue(true);

        // Observe the LiveData from repository
        LiveData<Place> placeLiveData = placeRepository.fetchPlaceById(placeId);
        placeLiveData.observeForever(loadedPlace -> {
            place.setValue(loadedPlace);
            isLoading.setValue(false);

            // Increment view count when place is loaded
            if (loadedPlace != null) {
                placeRepository.incrementViewCount(placeId);
            }
        });
    }

    /**
     * Reloads the current place using the stored place ID.
     * Useful for refreshing data after an update.
     */
    public void reloadPlace() {
        String currentPlaceId = placeId.getValue();
        if (currentPlaceId != null && !currentPlaceId.isEmpty()) {
            loadPlace(currentPlaceId);
        }
    }

    /**
     * Fetches all places from the repository.
     * The result is available through getAllPlaces() LiveData.
     */
    public void fetchAllPlaces() {
        isLoading.setValue(true);
        placeRepository.fetchAllPlaces();
    }

    /**
     * Loads places within a specified radius from user's location.
     *
     * @param latitude  User's current latitude
     * @param longitude User's current longitude
     * @param radiusKm  Search radius in kilometers
     */
    public void loadNearbyPlaces(double latitude, double longitude, double radiusKm) {
        isLoading.setValue(true);
        searchRadius.setValue(radiusKm);

        LiveData<List<Place>> nearbyPlacesLiveData =
                placeRepository.fetchNearbyPlaces(latitude, longitude, radiusKm);

        nearbyPlacesLiveData.observeForever(places -> {
            nearbyPlaces.setValue(places);
            isLoading.setValue(false);
        });
    }

    /**
     * Loads places by category.
     *
     * @param category The category to filter by (e.g., "Historical", "Cultural")
     */
    public void loadPlacesByCategory(String category) {
        if (category == null || category.isEmpty()) {
            errorMessage.setValue("Invalid category");
            return;
        }

        selectedCategory.setValue(category);
        isLoading.setValue(true);

        LiveData<List<Place>> categoryPlacesLiveData =
                placeRepository.fetchPlacesByCategory(category);

        categoryPlacesLiveData.observeForever(places -> {
            filteredPlaces.setValue(places);
            isLoading.setValue(false);
        });
    }

    /**
     * Loads places by city.
     *
     * @param city The city name to filter by (e.g., "Marrakech", "Fes")
     */
    public void loadPlacesByCity(String city) {
        if (city == null || city.isEmpty()) {
            errorMessage.setValue("Invalid city name");
            return;
        }

        selectedCity.setValue(city);
        isLoading.setValue(true);

        LiveData<List<Place>> cityPlacesLiveData =
                placeRepository.fetchPlacesByCity(city);

        cityPlacesLiveData.observeForever(places -> {
            filteredPlaces.setValue(places);
            isLoading.setValue(false);
        });
    }

    /**
     * Loads top-rated places.
     *
     * @param limit Maximum number of places to return
     */
    public void loadTopRatedPlaces(int limit) {
        isLoading.setValue(true);

        LiveData<List<Place>> topRatedLiveData =
                placeRepository.fetchTopRatedPlaces(limit);

        topRatedLiveData.observeForever(places -> {
            filteredPlaces.setValue(places);
            isLoading.setValue(false);
        });
    }

    /**
     * Searches places by name or description.
     *
     * @param query Search query string
     */
    public void searchPlaces(String query) {
        if (query == null || query.trim().isEmpty()) {
            errorMessage.setValue("Search query cannot be empty");
            return;
        }

        searchQuery.setValue(query);
        isLoading.setValue(true);

        LiveData<List<Place>> searchResultsLiveData =
                placeRepository.searchPlaces(query.trim());

        searchResultsLiveData.observeForever(places -> {
            searchResults.setValue(places);
            isLoading.setValue(false);
        });
    }

    /**
     * Clears the current search results.
     */
    public void clearSearch() {
        searchQuery.setValue(null);
        searchResults.setValue(null);
    }

    /**
     * Clears all filters and shows all places.
     */
    public void clearFilters() {
        selectedCategory.setValue(null);
        selectedCity.setValue(null);
        filteredPlaces.setValue(null);
        fetchAllPlaces();
    }

    // ==================== TRAJECTORY OPTIMIZATION ====================

    /**
     * Optimizes a route for visiting multiple places.
     * Uses nearest-neighbor algorithm for route optimization.
     *
     * @param places    List of places to optimize
     * @param startLat  Starting latitude
     * @param startLon  Starting longitude
     * @return List of places in optimized order
     */
    public List<Place> optimizeRoute(List<Place> places, double startLat, double startLon) {
        return placeRepository.optimizeTrajectory(places, startLat, startLon);
    }

    /**
     * Creates an optimized circular trajectory for nearby places.
     * Useful for day-trip planning.
     *
     * @param userLat   User's current latitude
     * @param userLon   User's current longitude
     * @param maxPlaces Maximum number of places to include
     * @param radiusKm  Maximum radius in kilometers
     * @return LiveData containing optimized list of places
     */
    public LiveData<List<Place>> createCircularTrajectory(
            double userLat, double userLon, int maxPlaces, double radiusKm) {

        isLoading.setValue(true);
        MutableLiveData<List<Place>> trajectoryResult = new MutableLiveData<>();

        LiveData<List<Place>> nearbyLiveData =
                placeRepository.fetchNearbyPlaces(userLat, userLon, radiusKm);

        nearbyLiveData.observeForever(places -> {
            if (places != null && !places.isEmpty()) {
                // Limit to max places
                List<Place> limitedPlaces = places.size() > maxPlaces
                        ? places.subList(0, maxPlaces)
                        : places;

                // Optimize the route
                List<Place> optimizedRoute = optimizeRoute(
                        new java.util.ArrayList<>(limitedPlaces), userLat, userLon);

                trajectoryResult.setValue(optimizedRoute);
            } else {
                trajectoryResult.setValue(new java.util.ArrayList<>());
            }
            isLoading.setValue(false);
        });

        return trajectoryResult;
    }

    // ==================== DATA MODIFICATION ====================

    /**
     * Adds a new place to the database.
     *
     * @param place The Place object to add
     * @return LiveData containing the ID of the newly created place
     */
    public LiveData<String> addPlace(Place place) {
        isLoading.setValue(true);
        LiveData<String> result = placeRepository.addPlace(place);

        result.observeForever(placeId -> {
            if (placeId != null) {
                isLoading.setValue(false);
            }
        });

        return result;
    }

    /**
     * Updates an existing place.
     *
     * @param place The Place object with updated data
     * @return LiveData containing true if update was successful
     */
    public LiveData<Boolean> updatePlace(Place place) {
        if (place == null || place.getId() == null || place.getId().isEmpty()) {
            errorMessage.setValue("Invalid place data for update");
            return new MutableLiveData<>(false);
        }

        isLoading.setValue(true);
        LiveData<Boolean> result = placeRepository.updatePlace(place);

        result.observeForever(success -> {
            if (success != null && success) {
                isLoading.setValue(false);
                // Reload the place to get updated data
                reloadPlace();
            }
        });

        return result;
    }

    /**
     * Deletes a place from the database.
     *
     * @param placeId The ID of the place to delete
     * @return LiveData containing true if deletion was successful
     */
    public LiveData<Boolean> deletePlace(String placeId) {
        if (placeId == null || placeId.isEmpty()) {
            errorMessage.setValue("Invalid place ID for deletion");
            return new MutableLiveData<>(false);
        }

        isLoading.setValue(true);
        LiveData<Boolean> result = placeRepository.deletePlace(placeId);

        result.observeForever(success -> {
            isLoading.setValue(false);
            if (success != null && success) {
                // Clear the current place if it was deleted
                if (placeId.equals(this.placeId.getValue())) {
                    place.setValue(null);
                }
            }
        });

        return result;
    }

    /**
     * Adds a review to the current place.
     *
     * @param rating   Rating value (1-5)
     * @param comment  Review comment
     * @param userId   User's ID
     * @param userName User's display name
     */
    public void addReview(float rating, String comment, String userId, String userName) {
        String currentPlaceId = placeId.getValue();
        if (currentPlaceId == null) {
            errorMessage.setValue("No place loaded");
            return;
        }

        if (rating < 1 || rating > 5) {
            errorMessage.setValue("Rating must be between 1 and 5");
            return;
        }

        placeRepository.addReview(currentPlaceId, rating, comment, userId, userName);

        // Reload place to get updated rating
        reloadPlace();
    }

    // ==================== LIVE DATA GETTERS ====================

    /**
     * Returns LiveData for observing the current place.
     *
     * @return LiveData containing the Place object
     */
    public LiveData<Place> getPlace() {
        return place;
    }

    /**
     * Returns LiveData for observing all places.
     *
     * @return LiveData containing list of all places
     */
    public LiveData<List<Place>> getAllPlaces() {
        return allPlaces;
    }

    /**
     * Returns LiveData for observing nearby places.
     *
     * @return LiveData containing list of nearby places
     */
    public LiveData<List<Place>> getNearbyPlaces() {
        return nearbyPlaces;
    }

    /**
     * Returns LiveData for observing filtered places.
     *
     * @return LiveData containing list of filtered places
     */
    public LiveData<List<Place>> getFilteredPlaces() {
        return filteredPlaces;
    }

    /**
     * Returns LiveData for observing search results.
     *
     * @return LiveData containing list of search results
     */
    public LiveData<List<Place>> getSearchResults() {
        return searchResults;
    }

    /**
     * Returns LiveData for observing loading state.
     *
     * @return LiveData containing loading state boolean
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Returns LiveData for observing error messages.
     *
     * @return LiveData containing error message string
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns LiveData for the current search query.
     *
     * @return LiveData containing search query string
     */
    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    /**
     * Returns LiveData for the selected category filter.
     *
     * @return LiveData containing category string
     */
    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    /**
     * Returns LiveData for the selected city filter.
     *
     * @return LiveData containing city string
     */
    public LiveData<String> getSelectedCity() {
        return selectedCity;
    }

    /**
     * Returns the current place ID.
     *
     * @return String containing place ID or null
     */
    public String getCurrentPlaceId() {
        return placeId.getValue();
    }

    /**
     * Returns the current search radius.
     *
     * @return Double containing search radius in kilometers
     */
    public Double getSearchRadius() {
        return searchRadius.getValue();
    }

    // ==================== HELPER METHODS ====================

    /**
     * Checks if a place is currently loaded.
     *
     * @return true if a place is loaded, false otherwise
     */
    public boolean hasPlaceLoaded() {
        return place.getValue() != null;
    }

    /**
     * Gets the current place name for display purposes.
     *
     * @return String containing place name or empty string
     */
    public String getPlaceName() {
        Place currentPlace = place.getValue();
        return currentPlace != null ? currentPlace.getName() : "";
    }

    /**
     * Gets formatted distance to the place.
     *
     * @return Formatted distance string or empty string
     */
    public String getFormattedDistance() {
        Place currentPlace = place.getValue();
        if (currentPlace != null && currentPlace.getDistanceFromUser() != null) {
            double distance = currentPlace.getDistanceFromUser();
            if (distance < 1.0) {
                return String.format("%.0f m", distance * 1000);
            } else {
                return String.format("%.1f km", distance);
            }
        }
        return "";
    }

    /**
     * Clears the current error message.
     */
    public void clearError() {
        errorMessage.setValue(null);
        placeRepository.clearError();
    }

    /**
     * Refreshes all data by re-fetching from the repository.
     */
    public void refreshData() {
        clearError();
        fetchAllPlaces();

        // Also refresh current place if loaded
        if (hasPlaceLoaded()) {
            reloadPlace();
        }
    }

    /**
     * Clears all ViewModel data.
     * Useful when navigating away from the place details screen.
     */
    public void clearData() {
        place.setValue(null);
        placeId.setValue(null);
        errorMessage.setValue(null);
        nearbyPlaces.setValue(null);
        filteredPlaces.setValue(null);
        searchResults.setValue(null);
        searchQuery.setValue(null);
    }

    /**
     * Called when the ViewModel is being cleared.
     * Removes observers and clears resources.
     */
    @Override
    protected void onCleared() {
        super.onCleared();

        // Remove observers from repository LiveData
        placeRepository.getIsLoading().removeObserver(loadingObserver);
        placeRepository.getErrorMessage().removeObserver(errorObserver);
    }
}