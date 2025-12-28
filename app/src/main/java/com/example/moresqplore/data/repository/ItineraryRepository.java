package com.example.moresqplore.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.Itinerary;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for managing Itinerary data
 * Supports both Firebase Firestore (future) and local storage
 */
public class ItineraryRepository {

    private static final String TAG = "ItineraryRepository";
    private static final String COLLECTION_ITINERARIES = "itineraries";

    private static ItineraryRepository instance;
    private final FirebaseFirestore firestore;
    private final CollectionReference itinerariesRef;

    // LiveData for reactive updates
    private final MutableLiveData<List<Itinerary>> userItineraries = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private ItineraryRepository() {
        firestore = FirebaseFirestore.getInstance();
        itinerariesRef = firestore.collection(COLLECTION_ITINERARIES);
    }

    public static synchronized ItineraryRepository getInstance() {
        if (instance == null) {
            instance = new ItineraryRepository();
        }
        return instance;
    }

    /**
     * Save itinerary to Firebase
     */
    public void saveItinerary(@NonNull Itinerary itinerary, OnSaveListener listener) {
        isLoading.setValue(true);

        itinerary.setSaved(true);

        if (itinerary.getId() == null) {
            // Create new document
            itinerariesRef.add(itinerary)
                    .addOnSuccessListener(documentReference -> {
                        itinerary.setId(documentReference.getId());
                        isLoading.setValue(false);
                        if (listener != null)
                            listener.onSuccess(itinerary.getId());
                        Log.d(TAG, "Itinerary saved: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        error.setValue("Failed to save itinerary: " + e.getMessage());
                        if (listener != null)
                            listener.onFailure(e);
                        Log.e(TAG, "Error saving itinerary", e);
                    });
        } else {
            // Update existing document
            itinerariesRef.document(itinerary.getId())
                    .set(itinerary)
                    .addOnSuccessListener(aVoid -> {
                        isLoading.setValue(false);
                        if (listener != null)
                            listener.onSuccess(itinerary.getId());
                    })
                    .addOnFailureListener(e -> {
                        isLoading.setValue(false);
                        error.setValue("Failed to update itinerary: " + e.getMessage());
                        if (listener != null)
                            listener.onFailure(e);
                    });
        }
    }

    /**
     * Load user's itineraries from Firebase
     */
    public void loadUserItineraries(@NonNull String userId) {
        isLoading.setValue(true);

        itinerariesRef
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    isLoading.setValue(false);

                    if (error != null) {
                        this.error.setValue("Error loading itineraries: " + error.getMessage());
                        Log.e(TAG, "Error loading itineraries", error);
                        return;
                    }

                    if (value != null) {
                        List<Itinerary> itineraries = new ArrayList<>();
                        itineraries.addAll(value.toObjects(Itinerary.class));
                        userItineraries.setValue(itineraries);
                        Log.d(TAG, "Loaded " + itineraries.size() + " itineraries");
                    }
                });
    }

    /**
     * Delete itinerary from Firebase
     */
    public void deleteItinerary(@NonNull String itineraryId, OnDeleteListener listener) {
        itinerariesRef.document(itineraryId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (listener != null)
                        listener.onSuccess();
                    Log.d(TAG, "Itinerary deleted: " + itineraryId);
                })
                .addOnFailureListener(e -> {
                    error.setValue("Failed to delete itinerary: " + e.getMessage());
                    if (listener != null)
                        listener.onFailure(e);
                    Log.e(TAG, "Error deleting itinerary", e);
                });
    }

    // LiveData getters
    public LiveData<List<Itinerary>> getUserItineraries() {
        return userItineraries;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Callback interfaces
    public interface OnSaveListener {
        void onSuccess(String itineraryId);

        void onFailure(Exception e);
    }

    public interface OnDeleteListener {
        void onSuccess();

        void onFailure(Exception e);
    }
}
