package com.example.moresqplore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MainActivityOSM extends AppCompatActivity {

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<Place> mPlaces = new ArrayList<>();
    private FloatingActionButton fabMyLocation, fabListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_main_osm);

        initializeViews();
        setupMap();
        createSamplePlaces();
        checkLocationPermission();
        setupClickListeners();
    }

    private void initializeViews() {
        mapView = findViewById(R.id.mapView);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabListView = findViewById(R.id.fabListView);
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Free OpenStreetMap tiles
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(12.0);

        // Set default location to Morocco (Casablanca)
        GeoPoint startPoint = new GeoPoint(33.5731, -7.5898);
        mapView.getController().setCenter(startPoint);
    }

    private void createSamplePlaces() {
        // Casablanca
        Place hassanMosque = new Place(1, "Hassan II Mosque",
                "One of the largest mosques in the world",
                "Casablanca", "Historical/Religious",
                33.6080, -7.6328, 90,
                "9:00 AM - 5:00 PM", true, 130);
        hassanMosque.setAverageRating(4.8f);
        hassanMosque.setTotalReviews(12500);

        // Marrakech
        Place jardinMajorelle = new Place(2, "Jardin Majorelle",
                "Beautiful botanical garden with vibrant blue buildings",
                "Marrakech", "Garden/Cultural",
                31.6414, -8.0037, 60,
                "8:00 AM - 6:30 PM", true, 70);
        jardinMajorelle.setAverageRating(4.6f);
        jardinMajorelle.setTotalReviews(8900);

        Place djemaaElFna = new Place(3, "Jemaa el-Fnaa",
                "Famous main square with storytellers and food stalls",
                "Marrakech", "Cultural/Market",
                31.6259, -7.9893, 120,
                "Open 24/7", false, 0);
        djemaaElFna.setAverageRating(4.5f);
        djemaaElFna.setTotalReviews(15600);

        mPlaces.add(hassanMosque);
        mPlaces.add(jardinMajorelle);
        mPlaces.add(djemaaElFna);

        addPlaceMarkers();
    }

    private void addPlaceMarkers() {
        for (Place place : mPlaces) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            marker.setTitle(place.getName());
            marker.setSnippet(place.getCity() + " • ★" + place.getAverageRating());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            marker.setOnMarkerClickListener((m, map) -> {
                Toast.makeText(this, place.getName() + "\n" +
                        place.getDescription(), Toast.LENGTH_LONG).show();
                return true;
            });

            mapView.getOverlays().add(marker);
        }
        mapView.invalidate();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        myLocationOverlay = new MyLocationNewOverlay(
                new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        myLocationOverlay.enableFollowLocation();
        mapView.getOverlays().add(myLocationOverlay);
    }

    private void setupClickListeners() {
        fabMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(15.0);
            } else {
                Toast.makeText(this, "Getting your location...",
                        Toast.LENGTH_SHORT).show();
            }
        });

        fabListView.setOnClickListener(v ->
                Toast.makeText(this, "Places List - Coming soon!",
                        Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
}