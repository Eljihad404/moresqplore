package com.example.moresqplore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
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

        // Initialize OSMDroid configuration with user agent
        Configuration.getInstance().load(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        setContentView(R.layout.activity_roadmap);

        initializeViews();
        setupMap();
        createSamplePlaces();
        checkLocationPermission();
        setupClickListeners();
    }

    private FloatingActionButton fabMapStyle;
    private int currentMapStyle = 0;

    private void initializeViews() {
        mapView = findViewById(R.id.mapView);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabListView = findViewById(R.id.fabListView);
        fabMapStyle = findViewById(R.id.fabMapStyle);
    }

    private void setupMap() {
        // OPTION 1: OpenTopoMap - Shows terrain, relief, labels (RECOMMENDED)
        mapView.setTileSource(new XYTileSource(
                "OpenTopoMap",
                0, 18, 256, ".png",
                new String[]{
                        "https://a.tile.opentopomap.org/",
                        "https://b.tile.opentopomap.org/",
                        "https://c.tile.opentopomap.org/"
                },
                "© OpenTopoMap"
        ));

    /* OPTION 2: Standard OSM with labels (similar to Google Maps)
    mapView.setTileSource(TileSourceFactory.MAPNIK);
    */

    /* OPTION 3: Humanitarian style - Clear labels, roads
    mapView.setTileSource(new XYTileSource(
        "HOT",
        0, 18, 256, ".png",
        new String[]{
            "https://a.tile.openstreetmap.fr/hot/",
            "https://b.tile.openstreetmap.fr/hot/"
        },
        "© OpenStreetMap contributors, Tiles style by Humanitarian OpenStreetMap Team"
    ));
    */

    /* OPTION 4: Satellite-like imagery
    mapView.setTileSource(new XYTileSource(
        "USGS_SAT",
        0, 18, 256, ".jpg",
        new String[]{
            "https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/"
        },
        "© USGS"
    ));
    */

        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(12.0);
        GeoPoint startPoint = new GeoPoint(33.5731, -7.5898);
        mapView.getController().setCenter(startPoint);
    }
    private void switchMapStyle() {
        currentMapStyle = (currentMapStyle + 1) % 3;

        switch (currentMapStyle) {
            case 0: // Terrain with labels
                mapView.setTileSource(new XYTileSource(
                        "OpenTopoMap", 0, 18, 256, ".png",
                        new String[]{"https://a.tile.opentopomap.org/"},
                        "© OpenTopoMap"
                ));
                Toast.makeText(this, "Terrain Map", Toast.LENGTH_SHORT).show();
                break;

            case 1: // Standard
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                Toast.makeText(this, "Standard Map", Toast.LENGTH_SHORT).show();
                break;

            case 2: // Humanitarian (clear labels)
                mapView.setTileSource(new XYTileSource(
                        "HOT", 0, 18, 256, ".png",
                        new String[]{"https://a.tile.openstreetmap.fr/hot/"},
                        "© OSM HOT"
                ));
                Toast.makeText(this, "Detailed Map", Toast.LENGTH_SHORT).show();
                break;
        }

        mapView.invalidate();
    }
    private void createSamplePlaces() {
        // Casablanca
        Place hassanMosque = new Place(1, "Hassan II Mosque",
                "One of the largest mosques in the world with stunning oceanfront views",
                "Casablanca", "Historical/Religious",
                33.6080, -7.6328, 90,
                "9:00 AM - 5:00 PM", true, 130);
        hassanMosque.setAverageRating(4.8f);
        hassanMosque.setTotalReviews(12500);

        // Marrakech
        Place jardinMajorelle = new Place(2, "Jardin Majorelle",
                "Beautiful botanical garden created by Jacques Majorelle",
                "Marrakech", "Garden/Cultural",
                31.6414, -8.0037, 60,
                "8:00 AM - 6:30 PM", true, 70);
        jardinMajorelle.setAverageRating(4.6f);
        jardinMajorelle.setTotalReviews(8900);

        Place djemaaElFna = new Place(3, "Jemaa el-Fnaa",
                "Famous main square with storytellers, musicians, and food stalls",
                "Marrakech", "Cultural/Market",
                31.6259, -7.9893, 120,
                "Open 24/7", false, 0);
        djemaaElFna.setAverageRating(4.5f);
        djemaaElFna.setTotalReviews(15600);

        Place bahiaPalace = new Place(4, "Bahia Palace",
                "19th-century palace with beautiful gardens and Moroccan architecture",
                "Marrakech", "Historical/Palace",
                31.6215, -7.9815, 75,
                "9:00 AM - 5:00 PM", true, 70);
        bahiaPalace.setAverageRating(4.4f);
        bahiaPalace.setTotalReviews(6700);

        // Meknes & Volubilis
        Place volubilis = new Place(5, "Volubilis",
                "Ancient Roman ruins with well-preserved mosaics, UNESCO World Heritage",
                "Meknes", "Historical/Archaeological",
                34.0739, -5.5534, 120,
                "8:30 AM - 6:30 PM", true, 70);
        volubilis.setAverageRating(4.7f);
        volubilis.setTotalReviews(4200);

        // Fes
        Place fesMedina = new Place(6, "Fes el-Bali",
                "World's largest car-free urban area, medieval walled city",
                "Fes", "Historical/Cultural",
                34.0631, -4.9767, 180,
                "Open 24/7", false, 0);
        fesMedina.setAverageRating(4.6f);
        fesMedina.setTotalReviews(9800);

        // Chefchaouen
        Place chefchaouen = new Place(7, "Chefchaouen",
                "Picturesque blue city in the Rif Mountains",
                "Chefchaouen", "Cultural/Scenic",
                35.1689, -5.2636, 240,
                "Open 24/7", false, 0);
        chefchaouen.setAverageRating(4.9f);
        chefchaouen.setTotalReviews(11200);

        // Essaouira
        Place essaouira = new Place(8, "Essaouira Medina",
                "Charming coastal town with 18th-century fortified medina",
                "Essaouira", "Coastal/Historical",
                31.5125, -9.7700, 180,
                "Open 24/7", false, 0);
        essaouira.setAverageRating(4.7f);
        essaouira.setTotalReviews(7800);

        mPlaces.add(hassanMosque);
        mPlaces.add(jardinMajorelle);
        mPlaces.add(djemaaElFna);
        mPlaces.add(bahiaPalace);
        mPlaces.add(volubilis);
        mPlaces.add(fesMedina);
        mPlaces.add(chefchaouen);
        mPlaces.add(essaouira);

        addPlaceMarkers();
    }

    private void addPlaceMarkers() {
        for (Place place : mPlaces) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            marker.setTitle(place.getName());

            // Create detailed snippet with rating
            String snippet = place.getCity() + "\n" +
                    "★ " + place.getAverageRating() + " (" + place.getTotalReviews() + " reviews)\n" +
                    place.getDescription();
            marker.setSnippet(snippet);

            // Set anchor point
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

            // Custom marker icon based on category
            Drawable icon = getMarkerIconForCategory(place.getCategory());
            if (icon != null) {
                marker.setIcon(icon);
            }

            // Set text style
            marker.setTextLabelFontSize(16);
            marker.setTextLabelBackgroundColor(Color.WHITE);
            marker.setTextLabelForegroundColor(Color.BLACK);

            // Click listener
            marker.setOnMarkerClickListener((m, map) -> {
                m.showInfoWindow();
                mapView.getController().animateTo(m.getPosition());
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private Drawable getMarkerIconForCategory(String category) {
        // You can create custom marker icons for different categories
        // For now, using default with different colors
        Drawable drawable = ContextCompat.getDrawable(this,
                org.osmdroid.library.R.drawable.marker_default);
        return drawable;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
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

        // Customize location marker
        myLocationOverlay.setDrawAccuracyEnabled(true);

        mapView.getOverlays().add(myLocationOverlay);

        // Zoom to user location when ready
        myLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(14.0);
            }
        }));
    }

    private void setupClickListeners() {
        fabMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(15.0);
                Toast.makeText(this, "Centered on your location",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Getting your location...",
                        Toast.LENGTH_SHORT).show();
                enableMyLocation();
            }
        });

        fabListView.setOnClickListener(v ->
                Toast.makeText(this, "Places List - Coming soon!",
                        Toast.LENGTH_SHORT).show());
        fabMapStyle.setOnClickListener(v -> switchMapStyle());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission required for full features",
                        Toast.LENGTH_LONG).show();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}