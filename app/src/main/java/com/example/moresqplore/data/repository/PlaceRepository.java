package com.example.moresqplore.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.model.GeoPoint;
import com.example.moresqplore.data.remote.SupabaseApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Repository for managing Place data operations with Supabase.
 */
public class PlaceRepository {

    private static final String TAG = "PlaceRepository";
    
    // Config (Ideally move to BuildConfig)
    private static final String SUPABASE_URL = "https://nnipiussbpgiugauhfgg.supabase.co/rest/v1/";
    private static final String SUPABASE_KEY = "sb_publishable_4osNsT6_rVjY_V-C6WBtEA_b4QiBkOw";

    // Singleton instance
    private static volatile PlaceRepository instance;
    private final SupabaseApi supabaseApi;

    // Cache for places data
    private final MutableLiveData<List<Place>> cachedPlaces;
    private final MutableLiveData<Boolean> isLoading;
    private final MutableLiveData<String> errorMessage;

    private PlaceRepository() {
        this.cachedPlaces = new MutableLiveData<>(new ArrayList<>());
        this.isLoading = new MutableLiveData<>(false);
        this.errorMessage = new MutableLiveData<>();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.supabaseApi = retrofit.create(SupabaseApi.class);
    }

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
    
    // For legacy/testing support
    public static void resetInstance() {
        synchronized (PlaceRepository.class) {
            instance = null;
        }
    }

    public LiveData<List<Place>> getPlaces() { return cachedPlaces; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    // --- READ Operations ---

    public void fetchAllPlaces() {
        // Not strictly needed for user flow, can implement using getTopRated with high limit
        fetchTopRatedPlaces(50); 
    }

    public LiveData<Place> fetchPlaceById(String placeId) {
        MutableLiveData<Place> placeLiveData = new MutableLiveData<>();
        setLoading(true);
        String authHeader = "Bearer " + SUPABASE_KEY;

        supabaseApi.getPlaceById(SUPABASE_KEY, authHeader, "eq." + placeId).enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    placeLiveData.setValue(response.body().get(0));
                } else {
                    setError("Place not found: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                setLoading(false);
                setError("Network error: " + t.getMessage());
            }
        });
        return placeLiveData;
    }
    
    // For simpler testing
    public LiveData<List<Place>> fetchPlacesByCategory(String category) {
        // Not implemented in API yet, falling back to all + filter or ignoring category for now if not critical
        // Adding TODO
        MutableLiveData<List<Place>> res = new MutableLiveData<>();
        setLoading(true);
        // Temporary: fetch top rated and filter manually? 
        // For now, return empty to avoid crash
        setLoading(false);
        return res;
    }

    public LiveData<List<Place>> fetchPlacesByCity(String city) {
        MutableLiveData<List<Place>> cityPlaces = new MutableLiveData<>();
        setLoading(true);
        String authHeader = "Bearer " + SUPABASE_KEY;

        supabaseApi.getPlacesByCity(SUPABASE_KEY, authHeader, "eq." + city).enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Fetched " + response.body().size() + " places for city: " + city);
                    List<Place> places = response.body();
                    cityPlaces.setValue(places);
                    updateCachedPlaces(places);
                } else {
                    String msg = "Failed to fetch places: " + response.code() + " " + response.message();
                    Log.e(TAG, msg);
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    setError(msg);
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                setLoading(false);
                Log.e(TAG, "Network error fetching places: " + t.getMessage(), t);
                setError("Network error: " + t.getMessage());
            }
        });
        return cityPlaces;
    }

    public LiveData<List<Place>> fetchNearbyPlaces(double latitude, double longitude, double radiusKm) {
        MutableLiveData<List<Place>> nearbyPlaces = new MutableLiveData<>();
        setLoading(true);
        String authHeader = "Bearer " + SUPABASE_KEY;

        // Fetch top rated and filter in memory
        supabaseApi.getTopRatedPlaces(SUPABASE_KEY, authHeader, "rating.desc", 100).enqueue(new Callback<List<Place>>() {
             @Override
             public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                 setLoading(false);
                 if (response.isSuccessful() && response.body() != null) {
                     List<Place> allPlaces = response.body();
                     List<Place> filtered = new ArrayList<>();
                     for (Place p : allPlaces) {
                         if (p.getLocation() != null) {
                             double dist = calculateDistance(latitude, longitude, p.getLocation().getLatitude(), p.getLocation().getLongitude());
                             if (dist <= radiusKm) {
                                 p.setDistanceFromUser(dist);
                                 filtered.add(p);
                             }
                         }
                     }
                     sortPlacesByDistance(filtered);
                     nearbyPlaces.setValue(filtered);
                 } else {
                     setError("Failed to fetch nearby places");
                 }
             }
             
             @Override
             public void onFailure(Call<List<Place>> call, Throwable t) {
                 setLoading(false);
                 setError(t.getMessage());
             }
        });
        
        return nearbyPlaces;
    }

    public LiveData<List<Place>> searchPlaces(String query) {
        MutableLiveData<List<Place>> searchResults = new MutableLiveData<>();
        setLoading(true);
        String authHeader = "Bearer " + SUPABASE_KEY;
        String filter = "ilike.*" + query + "*"; // Supabase ILIKE wildcard syntax: *value* not %value%? No, it's URL encoded % ? 
        // PostgREST: ilike=*%2Aquery%2A is *query*. Let's try simple ilike.
        
        supabaseApi.searchPlaces(SUPABASE_KEY, authHeader, query).enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.setValue(response.body());
                } else {
                    setError("Search failed");
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                setLoading(false);
                setError(t.getMessage());
            }
        });
        return searchResults;
    }
    
    public LiveData<List<Place>> fetchTopRatedPlaces(int limit) {
        MutableLiveData<List<Place>> topRated = new MutableLiveData<>();
        setLoading(true);
        String authHeader = "Bearer " + SUPABASE_KEY;
        
        supabaseApi.getTopRatedPlaces(SUPABASE_KEY, authHeader, "rating.desc", limit).enqueue(new Callback<List<Place>>() {
            @Override
            public void onResponse(Call<List<Place>> call, Response<List<Place>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    topRated.setValue(response.body());
                    updateCachedPlaces(response.body());
                } else {
                    setError("Failed to fetch top rated");
                }
            }

            @Override
            public void onFailure(Call<List<Place>> call, Throwable t) {
                setLoading(false);
                setError(t.getMessage());
            }
        });
        
        return topRated;
    }
    
    // --- Utils ---

    private void setLoading(boolean loading) {
        isLoading.postValue(loading);
    }

    private void setError(String message) {
        errorMessage.postValue(message);
        Log.e(TAG, "Error: " + message);
    }
    
    public void clearError() {
        errorMessage.postValue(null);
    }
    
    private void updateCachedPlaces(List<Place> places) {
        if (places != null) {
            cachedPlaces.postValue(new ArrayList<>(places));
        }
    }
    
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
    
    private void sortPlacesByDistance(List<Place> places) {
        places.sort((p1, p2) -> {
            Double distance1 = p1.getDistanceFromUser() != null ? p1.getDistanceFromUser() : Double.MAX_VALUE;
            Double distance2 = p2.getDistanceFromUser() != null ? p2.getDistanceFromUser() : Double.MAX_VALUE;
            return distance1.compareTo(distance2);
        });
    }
    
    // Stub for Optimize Trajectory (Keep logic if useful, but it relies on fetched places)
    public List<Place> optimizeTrajectory(List<Place> places, double startLat, double startLon) {
        if (places == null || places.isEmpty()) return new ArrayList<>();
        List<Place> optimizedRoute = new ArrayList<>();
        List<Place> remainingPlaces = new ArrayList<>(places);
        double currentLat = startLat;
        double currentLon = startLon;

        while (!remainingPlaces.isEmpty()) {
            Place nearestPlace = null;
            double nearestDistance = Double.MAX_VALUE;
            int nearestIndex = -1;

            for (int i = 0; i < remainingPlaces.size(); i++) {
                Place place = remainingPlaces.get(i);
                GeoPoint location = place.getLocation();
                if (location != null) {
                    double distance = calculateDistance(currentLat, currentLon, location.getLatitude(), location.getLongitude());
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
                GeoPoint location = nearestPlace.getLocation();
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLon = location.getLongitude();
                }
            }
        }
        return optimizedRoute;
    }

    // --- Write Operations (Stubbed / Disabled for now) ---
    public LiveData<String> addPlace(Place place) { return new MutableLiveData<>(null); }
    public LiveData<Boolean> updatePlace(Place place) { return new MutableLiveData<>(false); }
    public LiveData<Boolean> deletePlace(String placeId) { return new MutableLiveData<>(false); }
    public void incrementViewCount(String placeId) {}
    public void addReview(String placeId, float rating, String comment, String userId, String userName) {}
}