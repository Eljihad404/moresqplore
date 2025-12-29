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
        // 3. Load a nice style (MapTiler offers many: STREETS, SATELLITE, VOYAGER)
        // Note: Replace {YOUR_API_KEY} in the URL
        String styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=" + API_KEY;

        map.setStyle(styleUrl, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                // Define Points
                LatLng mosque = new LatLng(33.6085, -7.6327);
                LatLng medina = new LatLng(33.5958, -7.6176);
                LatLng mall   = new LatLng(33.5760, -7.7069);

                // Add Markers
                map.addMarker(new MarkerOptions().position(mosque).title("Hassan II Mosque"));
                map.addMarker(new MarkerOptions().position(medina).title("Old Medina"));
                map.addMarker(new MarkerOptions().position(mall).title("Morocco Mall"));

                // Draw Polyline
                List<LatLng> points = new ArrayList<>();
                points.add(mosque);
                points.add(medina);
                points.add(mall);

                map.addPolyline(new PolylineOptions()
                        .addAll(points)
                        .color(Color.parseColor("#C0392B"))
                        .width(5f)); // Width is in pixels

                // Move Camera
                CameraPosition position = new CameraPosition.Builder()
                        .target(medina)
                        .zoom(12)
                        .tilt(20) // Gives a nice 3D effect
                        .build();
                map.setCameraPosition(position);
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