package com.example.moresqplore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.moresqplore.ui.chat.ChatActivity;
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

import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.repository.PlaceRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.Button;
import android.widget.TextView;
// ... other imports

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
        checkLocationPermission();
        setupClickListeners();
        
        // Fetch Real Data
        fetchPlaces();
    }

    private void fetchPlaces() {
        PlaceRepository.getInstance().fetchPlacesByCity("Morocco").observe(this, places -> {
             if (places != null) {
                 mPlaces.clear();
                 mPlaces.addAll(places);
                 addPlaceMarkers();
             }
        });
        // Also try fetching all top rated to populate map initially if city specific fails or for broad view
        PlaceRepository.getInstance().fetchTopRatedPlaces(50).observe(this, places -> {
             if (places != null && mPlaces.isEmpty()) {
                 mPlaces.addAll(places);
                 addPlaceMarkers();
             }
        });
    }


    // ... member variables ...

    // ... (keep logic same until instantiate sample places if any, but createSamplePlaces was removed/not used here anymore as we fetch real data)
    // Wait, createSamplePlaces WAS called in onCreate but I removed it in Step 238?
    // Let's check the current view of MainActivityOSM.
    
    // In step 238 I removed createSamplePlaces() call from onCreate. 
    // But the COMPILER error says: MainActivityOSM.java:247: error: constructor Place...
    // This means createSamplePlaces IS still in the file!
    // Ah, I might have failed to remove the METHOD DEFINITION even if I removed the call?
    // Or I removed the call but the method definition remains and uses the old constructor.
    // I should remove the `createSamplePlaces` method entirely if it's not used, OR update it.
    // Since we are fetching real data, I should just remove `createSamplePlaces` method entirely to avoid these errors.
    
    // Also need to fix getAverageRating usage in addPlaceMarkers/showPlaceBottomSheet
    
    private void addPlaceMarkers() {
        for (Place place : mPlaces) {
            Marker marker = new Marker(mapView);
            // safe check for location
            if (place.getLatitude() != null && place.getLongitude() != null) {
                marker.setPosition(new GeoPoint(place.getLatitude(), place.getLongitude()));
            } else if (place.getLocation() != null) {
                marker.setPosition(new GeoPoint(place.getLocation().getLatitude(), place.getLocation().getLongitude()));
            } else {
                continue; // Skip if no location
            }
            
            marker.setTitle(place.getName());

            double rating = place.getRating() != null ? place.getRating() : 0.0;
            int reviews = place.getReviewCount() != null ? place.getReviewCount() : 0;

            // Create detailed snippet with rating
            String snippet = place.getCity() + "\n" +
                    "★ " + rating + " (" + reviews + " reviews)\n" +
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
                showPlaceBottomSheet(place);
                return true;
            });

            mapView.getOverlays().add(marker);
        }

        mapView.invalidate();
    }

    private void showPlaceBottomSheet(Place place) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_place, null);
        bottomSheetDialog.setContentView(sheetView);

        TextView tvName = sheetView.findViewById(R.id.bsPlaceName);
        TextView tvRating = sheetView.findViewById(R.id.bsPlaceRating);
        TextView tvCategory = sheetView.findViewById(R.id.bsPlaceCategory);
        TextView tvDesc = sheetView.findViewById(R.id.bsPlaceDesc);
        ImageView imgPlace = sheetView.findViewById(R.id.bsPlaceImage);
        Button btnView = sheetView.findViewById(R.id.btnViewDetails);

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
            // Placeholder based on category or city
            imgPlace.setImageResource(R.drawable.casablanca); // Default fallback
        }

        btnView.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Intent intent = new Intent(MainActivityOSM.this,
                    com.example.moresqplore.ui.details.PlaceDetailsActivity.class);
            intent.putExtra("PLACE_DATA", place);
            startActivity(intent);
        });

        bottomSheetDialog.show();
    }

    private FloatingActionButton fabMapStyle;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabChatAssistant;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabItineraryPlanner;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabPriceComparison; // Kept as variable name or change to fabBudget? Better change.
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabBudgetTracker;
    private com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton fabGuideMarketplace;
    private HorizontalScrollView categoryScrollView;
    private ImageView filterIcon;
    private int currentMapStyle = 0;

    private void initializeViews() {
        mapView = findViewById(R.id.mapView);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabListView = findViewById(R.id.fabListView);
        fabMapStyle = findViewById(R.id.fabMapStyle);
        fabChatAssistant = findViewById(R.id.fabChatAssistant);
        fabItineraryPlanner = findViewById(R.id.fabItineraryPlanner);
        fabBudgetTracker = findViewById(R.id.fabBudgetTracker);
        fabGuideMarketplace = findViewById(R.id.fabGuideMarketplace);

        // Initialize search and filter views
        View searchCard = findViewById(R.id.searchCard);
        filterIcon = findViewById(R.id.filterIcon);
        categoryScrollView = findViewById(R.id.categoryScrollView);
    }

    private void setupClickListeners() {
        // My Location button
        fabMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                mapView.getController().animateTo(myLocationOverlay.getMyLocation());
                mapView.getController().setZoom(15.0);
            } else {
                enableMyLocation();
            }
        });

        fabListView
                .setOnClickListener(v -> Toast.makeText(this, "Places List - Coming soon!", Toast.LENGTH_SHORT).show());

        fabMapStyle.setOnClickListener(v -> switchMapStyle());

        // AI Assistant chat
        fabChatAssistant.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityOSM.this, ChatActivity.class);
            startActivity(intent);
        });

        // Itinerary planner
        fabItineraryPlanner.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityOSM.this,
                    com.example.moresqplore.ui.itinerary.ItineraryInputActivity.class);
            startActivity(intent);
        });

        // Budget Tracker
        fabBudgetTracker.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityOSM.this, com.example.moresqplore.ui.budget.BudgetTrackerActivity.class);
            startActivity(intent);
        });

        // Guide marketplace
        fabGuideMarketplace.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivityOSM.this,
                    com.example.moresqplore.ui.guides.GuideMarketplaceActivity.class);
            startActivity(intent);
        });

        // Filter toggle
        if (filterIcon != null) {
            filterIcon.setOnClickListener(v -> {
                if (categoryScrollView.getVisibility() == View.VISIBLE) {
                    categoryScrollView.setVisibility(View.GONE);
                } else {
                    categoryScrollView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void setupMap() {
        // OPTION 1: OpenTopoMap - Shows terrain, relief, labels (RECOMMENDED)
        mapView.setTileSource(new XYTileSource(
                "OpenTopoMap",
                0, 18, 256, ".png",
                new String[] {
                        "https://a.tile.opentopomap.org/",
                        "https://b.tile.opentopomap.org/",
                        "https://c.tile.opentopomap.org/"
                },
                "© OpenTopoMap"));

        /*
         * OPTION 2: Standard OSM with labels (similar to Google Maps)
         * mapView.setTileSource(TileSourceFactory.MAPNIK);
         */

        /*
         * OPTION 3: Humanitarian style - Clear labels, roads
         * mapView.setTileSource(new XYTileSource(
         * "HOT",
         * 0, 18, 256, ".png",
         * new String[]{
         * "https://a.tile.openstreetmap.fr/hot/",
         * "https://b.tile.openstreetmap.fr/hot/"
         * },
         * "© OpenStreetMap contributors, Tiles style by Humanitarian OpenStreetMap Team"
         * ));
         */

        /*
         * OPTION 4: Satellite-like imagery
         * mapView.setTileSource(new XYTileSource(
         * "USGS_SAT",
         * 0, 18, 256, ".jpg",
         * new String[]{
         * "https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryOnly/MapServer/tile/"
         * },
         * "© USGS"
         * ));
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
                        new String[] { "https://a.tile.opentopomap.org/" },
                        "© OpenTopoMap"));
                Toast.makeText(this, "Terrain Map", Toast.LENGTH_SHORT).show();
                break;

            case 1: // Standard
                mapView.setTileSource(TileSourceFactory.MAPNIK);
                Toast.makeText(this, "Standard Map", Toast.LENGTH_SHORT).show();
                break;

            case 2: // Humanitarian (clear labels)
                mapView.setTileSource(new XYTileSource(
                        "HOT", 0, 18, 256, ".png",
                        new String[] { "https://a.tile.openstreetmap.fr/hot/" },
                        "© OSM HOT"));
                Toast.makeText(this, "Detailed Map", Toast.LENGTH_SHORT).show();
                break;
        }

        mapView.invalidate();
    }





    private Drawable getMarkerIconForCategory(String category) {
        // Create custom marker icons with Moroccan colors based on category
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_marker_morocco);

        if (drawable != null) {
            drawable = drawable.mutate(); // Make it mutable to change color

            // Color code by category
            if (category != null) {
                if (category.contains("Historical") || category.contains("Archaeological")) {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_terracotta));
                } else if (category.contains("Cultural") || category.contains("Market")) {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_blue));
                } else if (category.contains("Garden") || category.contains("Scenic")) {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_green));
                } else if (category.contains("Coastal") || category.contains("Beach")) {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_sand));
                } else if (category.contains("Palace") || category.contains("Religious")) {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_sand));
                } else {
                    drawable.setTint(ContextCompat.getColor(this, R.color.morocco_blue));
                }
            }
        }

        return drawable;
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {
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