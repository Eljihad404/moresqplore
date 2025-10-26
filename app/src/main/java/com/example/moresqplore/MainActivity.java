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
        Place hassanMosque = new Place(1, "Hassan II Mosque",
                "One of the largest mosques in the world, with stunning oceanfront views and intricate Moroccan architecture.",
                "Casablanca", "Historical/Religious",
                33.6080, -7.6328, 90,
                "9:00 AM - 5:00 PM", true, 130);
        hassanMosque.setAverageRating(4.8f);
        hassanMosque.setTotalReviews(12500);

        // Marrakech
        Place jardinMajorelle = new Place(2, "Jardin Majorelle",
                "A beautiful botanical garden created by French painter Jacques Majorelle, featuring exotic plants and vibrant blue buildings.",
                "Marrakech", "Garden/Cultural",
                31.6414, -8.0037, 60,
                "8:00 AM - 6:30 PM", true, 70);
        jardinMajorelle.setAverageRating(4.6f);
        jardinMajorelle.setTotalReviews(8900);

        Place djemaaElFna = new Place(3, "Jemaa el-Fnaa",
                "The main square in Marrakech's medina, famous for its vibrant atmosphere with storytellers, musicians, and food stalls.",
                "Marrakech", "Cultural/Market",
                31.6259, -7.9893, 120,
                "Open 24/7", false, 0);
        djemaaElFna.setAverageRating(4.5f);
        djemaaElFna.setTotalReviews(15600);

        Place bahiaPalace = new Place(4, "Bahia Palace",
                "A 19th-century palace with beautiful gardens and ornate Moroccan architecture showcasing Islamic and Moroccan styles.",
                "Marrakech", "Historical/Palace",
                31.6215, -7.9815, 75,
                "9:00 AM - 5:00 PM", true, 70);
        bahiaPalace.setAverageRating(4.4f);
        bahiaPalace.setTotalReviews(6700);

        // Meknes & Volubilis
        Place volubilis = new Place(5, "Volubilis",
                "Ancient Roman ruins featuring well-preserved mosaics and columns, a UNESCO World Heritage site.",
                "Meknes", "Historical/Archaeological",
                34.0739, -5.5534, 120,
                "8:30 AM - 6:30 PM", true, 70);
        volubilis.setAverageRating(4.7f);
        volubilis.setTotalReviews(4200);

        // Fes
        Place fesMedina = new Place(6, "Fes el-Bali (Old Medina)",
                "The world's largest car-free urban area and one of the oldest medieval cities, famous for its traditional crafts and narrow alleys.",
                "Fes", "Historical/Cultural",
                34.0631, -4.9767, 180,
                "Open 24/7", false, 0);
        fesMedina.setAverageRating(4.6f);
        fesMedina.setTotalReviews(9800);

        // Chefchaouen
        Place chefchaouen = new Place(7, "Chefchaouen Blue City",
                "A picturesque mountain town known for its striking blue-painted buildings and relaxed atmosphere.",
                "Chefchaouen", "Cultural/Scenic",
                35.1689, -5.2636, 240,
                "Open 24/7", false, 0);
        chefchaouen.setAverageRating(4.9f);
        chefchaouen.setTotalReviews(11200);

        // Essaouira
        Place essaouira = new Place(8, "Essaouira Medina",
                "A charming coastal town with a well-preserved 18th-century fortified medina and beautiful beaches.",
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
