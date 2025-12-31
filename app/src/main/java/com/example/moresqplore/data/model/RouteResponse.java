package com.example.moresqplore.data.model;

import com.google.gson.annotations.SerializedName;
import org.maplibre.android.geometry.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Response model for OSRM routing API
 */
public class RouteResponse {
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("routes")
    private List<Route> routes;
    
    public String getCode() {
        return code;
    }
    
    public List<Route> getRoutes() {
        return routes;
    }
    
    public static class Route {
        @SerializedName("geometry")
        private String geometry; // Encoded polyline
        
        @SerializedName("distance")
        private double distance; // Distance in meters
        
        @SerializedName("duration")
        private double duration; // Duration in seconds
        
        public String getGeometry() {
            return geometry;
        }
        
        public double getDistance() {
            return distance;
        }
        
        public double getDuration() {
            return duration;
        }
        
        /**
         * Get distance in kilometers
         */
        public double getDistanceInKm() {
            return distance / 1000.0;
        }
        
        /**
         * Get duration in minutes
         */
        public double getDurationInMinutes() {
            return duration / 60.0;
        }
        
        /**
         * Decode polyline geometry to list of LatLng points
         * Uses Polyline Algorithm Format (precision 5)
         */
        public List<LatLng> decodePolyline() {
            List<LatLng> points = new ArrayList<>();
            int index = 0, len = geometry.length();
            int lat = 0, lng = 0;
            
            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = geometry.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;
                
                shift = 0;
                result = 0;
                do {
                    b = geometry.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;
                
                LatLng point = new LatLng(lat / 1E5, lng / 1E5);
                points.add(point);
            }
            
            return points;
        }
    }
}
