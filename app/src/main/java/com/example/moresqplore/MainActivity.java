package com.example.moresqplore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import com.example.moresqplore.data.model.Place;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private List<Place> mPlaces = new ArrayList<>();

    private CardView searchCard;
    private EditText searchEditText;
    private ImageView filterIcon;
    private FloatingActionButton fabMyLocation, fabListView, fabTripPlanner;
    private HorizontalScrollView categoryScrollView;
    private CardView bottomSheetCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        initializeViews();
        setupMap();
        setupClickListeners();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        createSamplePlaces();
    }
    private void initializeViews() {
        searchCard = findViewById(R.id.searchCard);
        searchEditText = findViewById(R.id.searchEditText);
        filterIcon = findViewById(R.id.filterIcon);
        fabMyLocation = findViewById(R.id.fabMyLocation);
        fabListView = findViewById(R.id.fabListView);
        fabTripPlanner = findViewById(R.id.fabTripPlanner);
        categoryScrollView = findViewById(R.id.categoryScrollView);
        bottomSheetCard = findViewById(R.id.bottomSheetCard);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        // Filter icon click
        filterIcon.setOnClickListener(v -> toggleCategoryFilters());

        // FAB clicks
        fabMyLocation.setOnClickListener(v -> centerMapOnUserLocation());
        fabListView.setOnClickListener(v -> openPlacesListActivity());
        fabTripPlanner.setOnClickListener(v -> openTripPlannerActivity());
    }

    private void toggleCategoryFilters() {
        if (categoryScrollView.getVisibility() == View.VISIBLE) {
            categoryScrollView.setVisibility(View.GONE);
        } else {
            categoryScrollView.setVisibility(View.VISIBLE);
        }
    }

    private void centerMapOnUserLocation() {
        enableMyLocation();
    }

    private void openPlacesListActivity() {
        // TODO: We'll create this activity next
        Toast.makeText(this, "Places List - Coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void openTripPlannerActivity() {
        // TODO: We'll create this activity next
        Toast.makeText(this, "Trip Planner - Coming soon!", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();
        addPlaceMarkers();
    }

    private void createSamplePlaces() {
        // Casablanca
        Place hassanMosque = new Place();
        hassanMosque.setId("1");
        hassanMosque.setName("Hassan II Mosque");
        hassanMosque.setDescription("One of the largest mosques in the world, with stunning oceanfront views and intricate Moroccan architecture.");
        hassanMosque.setCity("Casablanca");
        hassanMosque.setCategory("Historical/Religious");
        hassanMosque.setLatitude(33.6080);
        hassanMosque.setLongitude(-7.6328);
        hassanMosque.setOpeningHours("9:00 AM - 5:00 PM");
        hassanMosque.setFreeEntry(true);
        hassanMosque.setTicketPrice(130.0);
        hassanMosque.setRating(4.8);
        hassanMosque.setReviewCount(12500);

        mPlaces.add(hassanMosque);
        
        // Add just a few for sample to avoid huge file
        Place jardinMajorelle = new Place();
        jardinMajorelle.setId("2");
        jardinMajorelle.setName("Jardin Majorelle");
        jardinMajorelle.setDescription("Beautiful botanical garden.");
        jardinMajorelle.setCity("Marrakech");
        jardinMajorelle.setCategory("Garden/Cultural");
        jardinMajorelle.setLatitude(31.6414);
        jardinMajorelle.setLongitude(-8.0037);
        mPlaces.add(jardinMajorelle);
    }

    private void addPlaceMarkers() {
        if (mMap != null) {
            for (Place place : mPlaces) {
                mMap.addMarker(new MarkerOptions().position(new LatLng(place.getLatitude(), place.getLongitude())).title(place.getName()));
            }
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }
}
