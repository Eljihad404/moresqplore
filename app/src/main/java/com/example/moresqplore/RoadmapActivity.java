package com.example.moresqplore;

import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// MapLibre Imports
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.OnMapReadyCallback;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class RoadmapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private static final String API_KEY = "gtDFpjWXQSNku7Z8CvqQ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Initialize MapLibre BEFORE setContentView
        MapLibre.getInstance(this);

        setContentView(R.layout.activity_roadmap);

        // 2. Setup MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        
        // 3. Setup Search
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

                        if (!points.isEmpty()) {
                            map.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .color(Color.parseColor("#C0392B"))
                                    .width(5f));

                            // Move Camera to first point logic handled in onMapReady mostly, but good fallback here
                        }
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
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        android.view.View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_place, null);
        bottomSheetDialog.setContentView(sheetView);

        android.widget.TextView tvName = sheetView.findViewById(R.id.bsPlaceName);
        android.widget.TextView tvRating = sheetView.findViewById(R.id.bsPlaceRating);
        android.widget.TextView tvCategory = sheetView.findViewById(R.id.bsPlaceCategory);
        android.widget.TextView tvDesc = sheetView.findViewById(R.id.bsPlaceDesc);
        android.widget.ImageView imgPlace = sheetView.findViewById(R.id.bsPlaceImage);
        android.widget.Button btnView = sheetView.findViewById(R.id.btnViewDetails);

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

        bottomSheetDialog.show();
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