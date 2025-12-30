package com.example.moresqplore;

import android.graphics.Color;
import android.os.Bundle;
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
    }

    @Override
    public void onMapReady(@NonNull MapLibreMap map) {
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + API_KEY;
        String cityName = getIntent().getStringExtra("CITY_NAME");

        map.setStyle(styleUrl, style -> {
            if (cityName != null) {
                loadCityRoute(map, cityName);
            } else {
                // Default fallback
                LatLng morocco = new LatLng(31.7917, -7.0926);
                map.setCameraPosition(new CameraPosition.Builder().target(morocco).zoom(5).build());
            }
        });
    }

    private void loadCityRoute(MapLibreMap map, String cityName) {
        com.example.moresqplore.data.repository.PlaceRepository.getInstance()
                .fetchPlacesByCity(cityName)
                .observe(this, places -> {
                    if (places != null && !places.isEmpty()) {
                        List<LatLng> points = new ArrayList<>();
                        for (com.example.moresqplore.data.model.Place place : places) {
                            if (place.getLocation() != null) {
                                LatLng latLng = new LatLng(place.getLocation().getLatitude(), place.getLocation().getLongitude());
                                points.add(latLng);
                                map.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                            }
                        }

                        if (!points.isEmpty()) {
                            map.addPolyline(new PolylineOptions()
                                    .addAll(points)
                                    .color(Color.parseColor("#C0392B"))
                                    .width(5f));

                            // Move Camera to first point
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(points.get(0))
                                    .zoom(12)
                                    .tilt(20)
                                    .build();
                            map.setCameraPosition(position);
                        }
                    }
                });
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