package com.example.moresqplore.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.moresqplore.data.local.AppDatabase;
import com.example.moresqplore.data.local.CityDao;
import com.example.moresqplore.data.model.City;
import com.example.moresqplore.data.model.HistoryEvent;
import com.example.moresqplore.data.remote.SupabaseApi;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CityRepository {
    private final CityDao cityDao;
    private final SupabaseApi supabaseApi;
    private final ExecutorService executorService;
    
    // Config (Ideally move to BuildConfig)
    private static final String SUPABASE_URL = "https://nnipiussbpgiugauhfgg.supabase.co/rest/v1/";
    private static final String SUPABASE_KEY = "sb_publishable_4osNsT6_rVjY_V-C6WBtEA_b4QiBkOw";

    public interface CityCallback {
        void onSuccess(City city);
        void onError(Exception e);
    }

    public CityRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.cityDao = db.cityDao();
        this.executorService = Executors.newSingleThreadExecutor();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.supabaseApi = retrofit.create(SupabaseApi.class);
    }

    public void getCityData(String cityName, CityCallback callback) {
        // 1. Check Local DB asynchronously
        executorService.execute(() -> {
            City localCity = cityDao.getCity(cityName);
            if (localCity != null) {
                Log.d("CityRepository", "Found city in local DB: " + cityName);
                callback.onSuccess(localCity);
                // Optionally refresh in background
                fetchFromSupabase(cityName, null); 
            } else {
                Log.d("CityRepository", "City not found locally, fetching from Supabase: " + cityName);
                fetchFromSupabase(cityName, callback);
            }
        });
    }

    private void fetchFromSupabase(String cityName, CityCallback callback) {
        // 2. Fetch City Details
        String authHeader = "Bearer " + SUPABASE_KEY;
        supabaseApi.getCityByName(SUPABASE_KEY, authHeader, "eq." + cityName, "*").enqueue(new Callback<List<City>>() {
            @Override
            public void onResponse(Call<List<City>> call, Response<List<City>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    City city = response.body().get(0);
                    
                    // 3. Fetch History
                    fetchHistoryForCity(city, callback);
                } else {
                    if (callback != null) {
                        callback.onError(new Exception("City not found in Supabase: " + response.message()));
                    }
                }
            }

            @Override
            public void onFailure(Call<List<City>> call, Throwable t) {
                if (callback != null) {
                    callback.onError(new Exception("Network error: " + t.getMessage()));
                }
            }
        });
    }
    
    private void fetchHistoryForCity(City city, CityCallback callback) {
        String authHeader = "Bearer " + SUPABASE_KEY;
        supabaseApi.getHistoryByCityId(SUPABASE_KEY, authHeader, "eq." + city.getId(), "order_index.asc").enqueue(new Callback<List<HistoryEvent>>() {
            @Override
            public void onResponse(Call<List<HistoryEvent>> call, Response<List<HistoryEvent>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    city.setHistoryEvents(response.body());
                } else {
                    city.setHistoryEvents(Collections.emptyList());
                }
                
                // 4. Save to Local DB (after fetching gallery)
                fetchGalleryForCity(city, callback);
            }

            @Override
            public void onFailure(Call<List<HistoryEvent>> call, Throwable t) {
                Log.e("CityRepository", "Failed to fetch history for city: " + city.getId(), t);
                // If history fails, try gallery anyway
                city.setHistoryEvents(Collections.emptyList());
                fetchGalleryForCity(city, callback);
            }
        });
    }
    
    private void fetchGalleryForCity(City city, CityCallback callback) {
        String authHeader = "Bearer " + SUPABASE_KEY;
        supabaseApi.getCityGallery(SUPABASE_KEY, authHeader, "eq." + city.getId()).enqueue(new Callback<List<com.example.moresqplore.data.model.CityGalleryImage>>() {
            @Override
            public void onResponse(Call<List<com.example.moresqplore.data.model.CityGalleryImage>> call, Response<List<com.example.moresqplore.data.model.CityGalleryImage>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("CityRepository", "Fetched " + response.body().size() + " gallery images for " + city.getName());
                    city.setGallery(response.body());
                } else {
                    Log.e("CityRepository", "Failed to fetch gallery: " + response.code() + " " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("CityRepository", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    city.setGallery(Collections.emptyList());
                }
                saveAndCallback(city, callback);
            }

            @Override
            public void onFailure(Call<List<com.example.moresqplore.data.model.CityGalleryImage>> call, Throwable t) {
                Log.e("CityRepository", "Network error fetching gallery: " + t.getMessage(), t);
                city.setGallery(Collections.emptyList());
                saveAndCallback(city, callback);
            }
        });
    }
    
    private void saveAndCallback(City city, CityCallback callback) {
        executorService.execute(() -> {
            cityDao.insertCity(city);
            Log.d("CityRepository", "Saved city to local DB: " + city.getName());
        });
        
        if (callback != null) {
            callback.onSuccess(city);
        }
    }
}
