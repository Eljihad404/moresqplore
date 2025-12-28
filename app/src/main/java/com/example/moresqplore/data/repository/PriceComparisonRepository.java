package com.example.moresqplore.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.PriceComparison;
import com.example.moresqplore.data.service.PriceAggregationService;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing price comparison data with Firebase caching
 */
public class PriceComparisonRepository {

    private static volatile PriceComparisonRepository instance;

    private final FirebaseFirestore firestore;
    private final CollectionReference priceComparisonsRef;
    private final PriceAggregationService aggregationService;

    private final MutableLiveData<List<PriceComparison>> cachedComparisons = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private PriceComparisonRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.priceComparisonsRef = firestore.collection("price_comparisons");
        this.aggregationService = new PriceAggregationService();
    }

    public static PriceComparisonRepository getInstance() {
        if (instance == null) {
            synchronized (PriceComparisonRepository.class) {
                if (instance == null) {
                    instance = new PriceComparisonRepository();
                }
            }
        }
        return instance;
    }

    // LiveData getters
    public LiveData<List<PriceComparison>> getCachedComparisons() {
        return cachedComparisons;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Fetch hotel prices (generates mock data, optionally caches to Firebase)
     */
    public void fetchHotelPrices(String hotelName, String city, String checkIn,
            String checkOut, int guests, OnPricesLoadedListener listener) {
        isLoading.postValue(true);

        // Generate mock prices
        PriceComparison comparison = aggregationService.generateHotelPrices(
                hotelName, city, checkIn, checkOut, guests);

        // Sort by price by default
        aggregationService.sortByPrice(comparison);

        isLoading.postValue(false);

        if (listener != null) {
            listener.onSuccess(comparison);
        }

        // Optionally cache to Firebase (for future reference)
        cacheToFirebase(comparison);
    }

    /**
     * Fetch flight prices (generates mock data)
     */
    public void fetchFlightPrices(String route, String date, int passengers,
            OnPricesLoadedListener listener) {
        isLoading.postValue(true);

        PriceComparison comparison = aggregationService.generateFlightPrices(
                route, date, passengers);

        aggregationService.sortByPrice(comparison);

        isLoading.postValue(false);

        if (listener != null) {
            listener.onSuccess(comparison);
        }

        cacheToFirebase(comparison);
    }

    /**
     * Fetch activity prices (generates mock data)
     */
    public void fetchActivityPrices(String activityName, String city, String date,
            int participants, OnPricesLoadedListener listener) {
        isLoading.postValue(true);

        PriceComparison comparison = aggregationService.generateActivityPrices(
                activityName, city, date, participants);

        aggregationService.sortByPrice(comparison);

        isLoading.postValue(false);

        if (listener != null) {
            listener.onSuccess(comparison);
        }

        cacheToFirebase(comparison);
    }

    /**
     * Cache price comparison to Firebase
     */
    private void cacheToFirebase(PriceComparison comparison) {
        priceComparisonsRef.add(comparison)
                .addOnSuccessListener(documentReference -> {
                    comparison.setId(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    // Silent fail for caching
                });
    }

    /**
     * Load user's saved price comparisons
     */
    public void loadUserComparisons(String userId) {
        isLoading.postValue(true);

        priceComparisonsRef
                .whereEqualTo("userId", userId)
                .orderBy("lastUpdated", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PriceComparison> comparisons = queryDocumentSnapshots.toObjects(PriceComparison.class);
                    cachedComparisons.postValue(comparisons);
                    isLoading.postValue(false);
                })
                .addOnFailureListener(e -> {
                    errorMessage.postValue("Failed to load comparisons: " + e.getMessage());
                    isLoading.postValue(false);
                });
    }

    /**
     * Sort comparison by different criteria
     */
    public void sortComparison(PriceComparison comparison, SortType sortType) {
        switch (sortType) {
            case PRICE:
                aggregationService.sortByPrice(comparison);
                break;
            case RATING:
                aggregationService.sortByRating(comparison);
                break;
            case VALUE:
                aggregationService.sortByValue(comparison);
                break;
        }
    }

    public enum SortType {
        PRICE, RATING, VALUE
    }

    /**
     * Callback for price loading
     */
    public interface OnPricesLoadedListener {
        void onSuccess(PriceComparison comparison);

        void onFailure(Exception e);
    }
}
