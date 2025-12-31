package com.example.moresqplore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

// MapLibre Imports
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Polyline;
import org.maplibre.android.annotations.PolylineOptions;

// Location Services
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

// Routing API
import com.example.moresqplore.data.api.RouteService;
import com.example.moresqplore.data.model.RouteResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoadmapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "RoadmapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String API_KEY = "gtDFpjWXQSNku7Z8CvqQ";
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/";
    
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RouteService routeService;
    
    // Current location and routing
    private LatLng currentLocation;
    private Polyline currentRoutePolyline;
    private com.example.moresqplore.data.model.Place selectedPlace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize MapLibre BEFORE setContentView
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_roadmap);

        // 2. Initialize services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupRouteService();
        
        // 3. Setup MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        // 4. Setup My Location FAB
        setupMyLocationButton();
        
        // 4.5 Setup Plan Trip Button
        setupPlanTripButton();

        // 4.6 Setup Budget Tracker Button
        setupBudgetTrackerButton();
        
        // 5. Setup Search
        android.widget.EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    performSearch(query);
                }
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }
    
    private void setupRouteService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OSRM_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        routeService = retrofit.create(RouteService.class);
    }
    
    private void setupMyLocationButton() {
        com.google.android.material.floatingactionbutton.FloatingActionButton fabMyLocation = 
                findViewById(R.id.fabMyLocation);
        fabMyLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void setupPlanTripButton() {
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabPlanTrip =
                findViewById(R.id.fabItineraryPlanner);
        fabPlanTrip.setOnClickListener(v -> planEfficientTrip());
    }

    private void setupBudgetTrackerButton() {
        com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabBudget =
                findViewById(R.id.fabBudgetTracker);
        fabBudget.setOnClickListener(v -> {
            Intent intent = new Intent(RoadmapActivity.this, com.example.moresqplore.ui.budget.BudgetTrackerActivity.class);
            startActivity(intent);
        });
    }

    private void planEfficientTrip() {
        if (currentLocation == null) {
            Toast.makeText(this, "Getting your location first...", Toast.LENGTH_SHORT).show();
            getCurrentLocation();
            return;
        }

        if (markerPlaceMap.isEmpty()) {
            Toast.makeText(this, "No places found on map to visit.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Calculating most efficient route...", Toast.LENGTH_SHORT).show();

        // Get all places from the map
        List<com.example.moresqplore.data.model.Place> placesToVisit = new ArrayList<>(markerPlaceMap.values());
        
        // Run optimization (Nearest Neighbor)
        List<LatLng> orderedPoints = optimizeRoute(currentLocation, placesToVisit);
        
        // Fetch and display route
        getRouteForPoints(orderedPoints);
    }

    /**
     * Optimizes the route using Nearest Neighbor algorithm.
     * Starts at user location, finds nearest place, moves there, repeats.
     */
    private List<LatLng> optimizeRoute(LatLng start, List<com.example.moresqplore.data.model.Place> places) {
        List<LatLng> path = new ArrayList<>();
        path.add(start);

        List<com.example.moresqplore.data.model.Place> unvisited = new ArrayList<>(places);
        LatLng currentPos = start;

        while (!unvisited.isEmpty()) {
            com.example.moresqplore.data.model.Place nearestPlace = null;
            double minDistance = Double.MAX_VALUE;

            for (com.example.moresqplore.data.model.Place place : unvisited) {
                if (place.getLocation() == null) continue;
                
                double dist = calculateDistance(
                        currentPos.getLatitude(), currentPos.getLongitude(),
                        place.getLocation().getLatitude(), place.getLocation().getLongitude());
                
                if (dist < minDistance) {
                    minDistance = dist;
                    nearestPlace = place;
                }
            }

            if (nearestPlace != null) {
                unvisited.remove(nearestPlace);
                LatLng placeLoc = new LatLng(nearestPlace.getLocation().getLatitude(), nearestPlace.getLocation().getLongitude());
                path.add(placeLoc);
                currentPos = placeLoc;
            } else {
                // Should not happen unless places have null location
                break;
            }
        }
        
        return path;
    }

    /**
     * Calculates distance between two points in kilometers.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private void getRouteForPoints(List<LatLng> points) {
        if (points.size() < 2) return;

        // Build coordinate string for OSRM: lon,lat;lon,lat;...
        StringBuilder coords = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            LatLng p = points.get(i);
            coords.append(String.format(Locale.US, "%.6f,%.6f", p.getLongitude(), p.getLatitude()));
            if (i < points.size() - 1) {
                coords.append(";");
            }
        }

        routeService.getRoute("driving", coords.toString(), "full", false)
                .enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RouteResponse routeResponse = response.body();
                            if ("Ok".equals(routeResponse.getCode()) && 
                                    routeResponse.getRoutes() != null && 
                                    !routeResponse.getRoutes().isEmpty()) {
                                
                                RouteResponse.Route route = routeResponse.getRoutes().get(0);
                                displayRoute(route);
                                
                                Toast.makeText(RoadmapActivity.this, 
                                        String.format(Locale.US, "Trip Plan: %.1f km, %.0f mins", 
                                                route.getDistanceInKm(), route.getDurationInMinutes()), 
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(RoadmapActivity.this, "Could not find a route connecting all places.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RoadmapActivity.this, "Route service error.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {
                        Log.e(TAG, "Optimization route failed", t);
                        Toast.makeText(RoadmapActivity.this, "Failed to connect to route service.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation called");
        
        // Check if location permissions are granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED && 
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Location permission not granted, requesting...");
            ActivityCompat.requestPermissions(this, 
                    new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        
        Log.d(TAG, "Location permission granted, getting location...");
        Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        Log.d(TAG, "Location found: " + location.getLatitude() + ", " + location.getLongitude());
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mapLibreMap != null) {
                            mapLibreMap.animateCamera(
                                    org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                            currentLocation, 15));
                        }
                        Toast.makeText(this, "Location found!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "Location is null, trying to request fresh location");
                        Toast.makeText(this, "Unable to get current location. Please ensure location services are enabled.", Toast.LENGTH_LONG).show();
                        // Try to get fresh location
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    Toast.makeText(this, "Error getting location: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                });
    }
    
    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        // Request a fresh location update
        com.google.android.gms.location.LocationRequest locationRequest = 
                com.google.android.gms.location.LocationRequest.create();
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        
        fusedLocationClient.requestLocationUpdates(locationRequest, 
                new com.google.android.gms.location.LocationCallback() {
                    @Override
                    public void onLocationResult(com.google.android.gms.location.LocationResult locationResult) {
                        if (locationResult != null) {
                            Location location = locationResult.getLastLocation();
                            if (location != null) {
                                Log.d(TAG, "Fresh location obtained: " + location.getLatitude() + ", " + location.getLongitude());
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                if (mapLibreMap != null) {
                                    mapLibreMap.animateCamera(
                                            org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(
                                                    currentLocation, 15));
                                }
                                Toast.makeText(RoadmapActivity.this, "Location found!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                }, null);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Location permission granted by user");
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                // Permission granted, get location
                getCurrentLocation();
            } else {
                Log.w(TAG, "Location permission denied by user");
                Toast.makeText(this, "Location permission is required for navigation features", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void performSearch(String query) {
        mapView.getMapAsync(map -> {
            com.example.moresqplore.data.repository.PlaceRepository.getInstance()
                .searchPlaces(query)
                .observe(this, places -> {
                    if (places != null && !places.isEmpty()) {
                        markerPlaceMap.clear();
                        map.clear(); // Clear existing markers/routes
                        
                        List<LatLng> points = new ArrayList<>();
                        org.maplibre.android.geometry.LatLngBounds.Builder boundsBuilder = new org.maplibre.android.geometry.LatLngBounds.Builder();

                        for (com.example.moresqplore.data.model.Place place : places) {
                            if (place.getLocation() != null) {
                                LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
                                points.add(latLng);
                                boundsBuilder.include(latLng);
                                
                                org.maplibre.android.annotations.Marker marker = map.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(place.getName())
                                    .snippet(place.getCategory())); // Show category in snippet
                                markerPlaceMap.put(marker.getId(), place);
                            }
                        }
                        
                        if (!points.isEmpty()) {
                            // Zoom to results
                            if (points.size() > 1) {
                                map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                            } else {
                                map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(points.get(0), 14));
                            }
                        } else {
                            android.widget.Toast.makeText(this, "No valid locations found for '" + query + "'", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        android.widget.Toast.makeText(this, "No places found matching '" + query + "'", android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
        });
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        this.mapLibreMap = map;
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + API_KEY;
        String cityName = getIntent().getStringExtra("CITY_NAME");
        
        // Check for specific place coordinates first
        double placeLat = getIntent().getDoubleExtra("PLACE_LAT", 0.0);
        double placeLng = getIntent().getDoubleExtra("PLACE_LNG", 0.0);
        String placeName = getIntent().getStringExtra("PLACE_NAME");
        
        // Fallback to city coordinates
        double cityLat = getIntent().getDoubleExtra("CITY_LAT", 0.0);
        double cityLng = getIntent().getDoubleExtra("CITY_LNG", 0.0);

        map.setStyle(styleUrl, style -> {
            if (placeLat != 0.0 && placeLng != 0.0) {
                // Focus on specific place
                LatLng placeLocation = new LatLng(placeLat, placeLng);
                map.setCameraPosition(new CameraPosition.Builder()
                    .target(placeLocation)
                    .zoom(15)
                    .build());
                
                // Add marker for the place
                map.addMarker(new MarkerOptions()
                    .position(placeLocation)
                    .title(placeName != null ? placeName : "Selected Place"));
                    
                // Still load city route if city name is available
                if (cityName != null) {
                    loadCityRoute(map, cityName);
                }
            } else if (cityLat != 0.0 && cityLng != 0.0) {
                // Focus on specific city
                LatLng cityLocation = new LatLng(cityLat, cityLng);
                map.setCameraPosition(new CameraPosition.Builder().target(cityLocation).zoom(13).build());
                if (cityName != null) {
                    loadCityRoute(map, cityName);
                }
            } else if (cityName != null) {
                 // Fallback if no coords but city name exists (fetch manually or just load route)
                 loadCityRoute(map, cityName);
            } else {
                // Default fallback
                LatLng morocco = new LatLng(31.7917, -7.0926);
                map.setCameraPosition(new CameraPosition.Builder().target(morocco).zoom(5).build());
            }
        });
    }

    // Map to store Place objects for markers
    private java.util.Map<Long, com.example.moresqplore.data.model.Place> markerPlaceMap = new java.util.HashMap<>();

    private void loadCityRoute(MapLibreMap map, String cityName) {
        com.example.moresqplore.data.repository.PlaceRepository.getInstance()
                .fetchPlacesByCity(cityName)
                .observe(this, places -> {
                    if (places != null && !places.isEmpty()) {
                        List<LatLng> points = new ArrayList<>();
                        markerPlaceMap.clear(); // Clear old markers
                        
                        for (com.example.moresqplore.data.model.Place place : places) {
                            if (place.getLocation() != null) {
                                LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
                                points.add(latLng);
                                org.maplibre.android.annotations.Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                                markerPlaceMap.put(marker.getId(), place);
                            }
                        }


                        // Markers added, polyline removed to show only pin icons
                        // Camera positioning handled in onMapReady
                    }
                });
                
        map.setOnMarkerClickListener(marker -> {
            com.example.moresqplore.data.model.Place place = markerPlaceMap.get(marker.getId());
            if (place != null) {
                showPlaceBottomSheet(place);
                return true;
            }
            return false;
        });
    }
    
    private void showPlaceBottomSheet(com.example.moresqplore.data.model.Place place) {
        this.selectedPlace = place;
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_place, null);
        bottomSheetDialog.setContentView(sheetView);

        android.widget.TextView tvName = sheetView.findViewById(R.id.bsPlaceName);
        android.widget.TextView tvRating = sheetView.findViewById(R.id.bsPlaceRating);
        android.widget.TextView tvCategory = sheetView.findViewById(R.id.bsPlaceCategory);
        android.widget.TextView tvDesc = sheetView.findViewById(R.id.bsPlaceDesc);
        android.widget.ImageView imgPlace = sheetView.findViewById(R.id.bsPlaceImage);
        android.widget.Button btnView = sheetView.findViewById(R.id.btnViewDetails);
        android.widget.Button btnGetDirections = sheetView.findViewById(R.id.btnGetDirections);
        android.widget.Button btnClearRoute = sheetView.findViewById(R.id.btnClearRoute);
        android.view.View routeInfoSection = sheetView.findViewById(R.id.routeInfoSection);
        android.widget.TextView tvRouteDistance = sheetView.findViewById(R.id.tvRouteDistance);
        android.widget.TextView tvRouteDuration = sheetView.findViewById(R.id.tvRouteDuration);

        tvName.setText(place.getName());
        double rating = place.getRating() != null ? place.getRating() : 0.0;
        tvRating.setText("★ " + rating);
        tvCategory.setText(place.getCategory());
        tvDesc.setText(place.getDescription());

        // Load image with Glide
        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(place.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imgPlace);
        } else {
            imgPlace.setImageResource(R.drawable.casablanca);
        }

        btnView.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(RoadmapActivity.this,
                    com.example.moresqplore.ui.details.PlaceDetailsActivity.class);
            intent.putExtra("PLACE_DATA", place);
            startActivity(intent);
        });
        
        // Get Directions button
        btnGetDirections.setOnClickListener(v -> {
            if (currentLocation == null) {
                Toast.makeText(this, "Getting your location...", Toast.LENGTH_SHORT).show();
                getCurrentLocation();
                // Try again after a delay
                btnGetDirections.postDelayed(() -> {
                    if (currentLocation != null) {
                        getRouteToPlace(place, routeInfoSection, tvRouteDistance, tvRouteDuration);
                    } else {
                        Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show();
                    }
                }, 2000);
            } else {
                getRouteToPlace(place, routeInfoSection, tvRouteDistance, tvRouteDuration);
            }
        });
        
        // Clear Route button
        btnClearRoute.setOnClickListener(v -> {
            clearRoute();
            routeInfoSection.setVisibility(android.view.View.GONE);
        });

        bottomSheetDialog.show();
    }
    
    private void getRouteToPlace(com.example.moresqplore.data.model.Place place,
                                  android.view.View routeInfoSection,
                                  android.widget.TextView tvDistance,
                                  android.widget.TextView tvDuration) {
        if (place.getLocation() == null) {
            Toast.makeText(this, "Place location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        LatLng destination = new LatLng(
                place.getLocation().getLatitude(),
                place.getLocation().getLongitude());
        
        // Format coordinates: lon,lat;lon,lat
        String coordinates = String.format(Locale.US, "%.6f,%.6f;%.6f,%.6f",
                currentLocation.getLongitude(), currentLocation.getLatitude(),
                destination.getLongitude(), destination.getLatitude());
        
        Toast.makeText(this, "Calculating route...", Toast.LENGTH_SHORT).show();
        
        routeService.getRoute("driving", coordinates, "full", false)
                .enqueue(new Callback<RouteResponse>() {
                    @Override
                    public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            RouteResponse routeResponse = response.body();
                            if ("Ok".equals(routeResponse.getCode()) && 
                                    routeResponse.getRoutes() != null && 
                                    !routeResponse.getRoutes().isEmpty()) {
                                
                                RouteResponse.Route route = routeResponse.getRoutes().get(0);
                                displayRoute(route);
                                
                                // Update UI with route info
                                tvDistance.setText(String.format(Locale.US, "%.1f km", 
                                        route.getDistanceInKm()));
                                tvDuration.setText(String.format(Locale.US, "%.0f mins", 
                                        route.getDurationInMinutes()));
                                routeInfoSection.setVisibility(android.view.View.VISIBLE);
                                
                                Toast.makeText(RoadmapActivity.this, 
                                        "Route calculated!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RoadmapActivity.this, 
                                        "No route found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(RoadmapActivity.this, 
                                    "Failed to get route", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RouteResponse> call, Throwable t) {
                        Log.e(TAG, "Route request failed", t);
                        Toast.makeText(RoadmapActivity.this, 
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void displayRoute(RouteResponse.Route route) {
        if (mapLibreMap == null) return;
        
        // Clear previous route
        clearRoute();
        
        // Decode polyline and add to map
        List<LatLng> routePoints = route.decodePolyline();
        if (routePoints.isEmpty()) return;
        
        currentRoutePolyline = mapLibreMap.addPolyline(new PolylineOptions()
                .addAll(routePoints)
                .color(Color.parseColor("#E2725B")) // Morocco terracotta
                .width(6f));
        
        // Zoom to show entire route
        org.maplibre.android.geometry.LatLngBounds.Builder boundsBuilder = 
                new org.maplibre.android.geometry.LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            boundsBuilder.include(point);
        }
        mapLibreMap.animateCamera(
                org.maplibre.android.camera.CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), 100));
    }
    
    private void clearRoute() {
        if (currentRoutePolyline != null && mapLibreMap != null) {
            mapLibreMap.removePolyline(currentRoutePolyline);
            currentRoutePolyline = null;
        }
    }

    // MapLibre requires lifecycle handling
    @Override
    protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    protected void onStop() { super.onStop(); mapView.onStop(); }
    @Override
    protected void onSaveInstanceState(Bundle outState) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState); }
    @Override
    public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override
    protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}