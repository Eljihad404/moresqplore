package com.example.moresqplore.data.remote;

import com.example.moresqplore.data.model.City;
import com.example.moresqplore.data.model.HistoryEvent;
import com.example.moresqplore.data.model.Place;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface SupabaseApi {
    
    // Fetch city by name
    @GET("cities")
    Call<List<City>> getCityByName(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("name") String nameQuery, // e.g., "eq.Casablanca"
        @Query("select") String select // e.g., "*"
    );

    // Fetch history for a city
    @GET("history_timeline")
    Call<List<HistoryEvent>> getHistoryByCityId(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("city_id") String cityIdQuery, // e.g., "eq.<UUID>"
        @Query("order") String order // e.g., "order_index.asc"
    );
    
    // Fetch city gallery
    @GET("city_gallery")
    Call<List<com.example.moresqplore.data.model.CityGalleryImage>> getCityGallery(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("city_id") String cityIdQuery // e.g., "eq.<UUID>"
    );

    // --- Places Endpoints ---
    
    // Get places by city
    @GET("places")
    Call<List<Place>> getPlacesByCity(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("city") String cityQuery // e.g., "eq.Casablanca"
    );

    // Get place by ID
    @GET("places")
    Call<List<Place>> getPlaceById(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("id") String idQuery // e.g., "eq.<UUID>"
    );
    
    // Get top rated places
    @GET("places")
    Call<List<Place>> getTopRatedPlaces(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("order") String order, // e.g., "rating.desc"
        @Query("limit") int limit
    );
    
    // Search places (simple text search on name)
    @GET("places")
    Call<List<Place>> searchPlaces(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("name") String nameFilter // e.g., "ilike.%query%"
    );

    // Nearby places (Bounding Box)
    @GET("places")
    Call<List<Place>> getNearbyPlaces(
        @Header("apikey") String apiKey,
        @Header("Authorization") String authorization,
        @Query("latitude") String latFilter, // e.g., "gte.minLat&latitude=lte.maxLat" - Wait, Retrofit handles multiple queries with same key? 
        // Retrofit QueryMap is better for multiple filters on same field if needed, or simply pass "gte.min"
        // Actually for bounding box we need: latitude=gte.MIN & latitude=lte.MAX.
        // Retrofit doesn't support multiple @Query with same name easily unless List.
        // OR we can use @QueryMap.
        // Let's use @QueryMap for flexible filtering.
        @retrofit2.http.QueryMap java.util.Map<String, String> filters
    );
}

