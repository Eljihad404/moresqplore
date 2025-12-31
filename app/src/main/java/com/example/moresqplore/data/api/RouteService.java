package com.example.moresqplore.data.api;

import com.example.moresqplore.data.model.RouteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit service interface for OSRM (Open Source Routing Machine) API
 * Used to calculate routes between locations
 */
public interface RouteService {
    
    /**
     * Get route between coordinates
     * @param profile Route profile: "driving", "walking", or "cycling"
     * @param coordinates Semicolon-separated coordinates in format "lng,lat;lng,lat"
     * @param overview Route geometry overview: "full", "simplified", or "false"
     * @param steps Whether to include turn-by-turn instructions
     * @return RouteResponse containing route geometry, distance, and duration
     */
    @GET("route/v1/{profile}/{coordinates}")
    Call<RouteResponse> getRoute(
        @Path("profile") String profile,
        @Path("coordinates") String coordinates,
        @Query("overview") String overview,
        @Query("steps") boolean steps
    );
}
