package com.example.moresqplore;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.repository.PlaceRepository;

public class PlaceDetailActivity extends AppCompatActivity {

    private String placeId;
    private PlaceRepository placeRepository;

    private ImageView imgPlaceHero;
    private TextView tvPlaceName;
    private TextView tvCategory;
    private TextView tvRating;
    private TextView tvReviewCount;
    private TextView tvOpeningHours;
    private TextView tvPlaceDescription;
    private TextView tvAddress;
    private TextView tvTicketPrice;
    private Button btnAddToItinerary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        // Init Views
        imgPlaceHero = findViewById(R.id.imgPlaceHero);
        tvPlaceName = findViewById(R.id.tvPlaceName);
        tvCategory = findViewById(R.id.tvCategory);
        tvRating = findViewById(R.id.tvRating);
        tvReviewCount = findViewById(R.id.tvReviewCount);
        tvOpeningHours = findViewById(R.id.tvOpeningHours);
        tvPlaceDescription = findViewById(R.id.tvPlaceDescription);
        tvAddress = findViewById(R.id.tvAddress);
        tvTicketPrice = findViewById(R.id.tvTicketPrice);
        btnAddToItinerary = findViewById(R.id.btnAddToItinerary);

        placeRepository = PlaceRepository.getInstance();

        // Get Place ID
        placeId = getIntent().getStringExtra("PLACE_ID");
        if (placeId == null) {
            Toast.makeText(this, "Error: No place ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPlaceDetails(placeId);
    }

    private void loadPlaceDetails(String id) {
        placeRepository.fetchPlaceById(id).observe(this, place -> {
            if (place != null) {
                populateUI(place);
            } else {
                Toast.makeText(this, "Failed to load place details", Toast.LENGTH_SHORT).show();
            }
        });
        
        // Also observe errors
        placeRepository.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                placeRepository.clearError();
            }
        });
    }

    private void populateUI(Place place) {
        tvPlaceName.setText(place.getName());
        tvCategory.setText(place.getCategory());
        
        double rating = place.getRating() != null ? place.getRating() : 0.0;
        tvRating.setText(String.format("%.1f", rating));
        
        int reviews = place.getReviewCount() != null ? place.getReviewCount() : 0;
        tvReviewCount.setText("(" + reviews + " reviews)");
        
        tvOpeningHours.setText("Open: " + (place.getOpeningHours() != null ? place.getOpeningHours() : "N/A"));
        tvPlaceDescription.setText(place.getDescription());
        tvAddress.setText(place.getAddress());
        
        if (place.isFreeEntry()) {
            tvTicketPrice.setText("Free Entry");
        } else {
             tvTicketPrice.setText("Entrance Fee: MAD " + (place.getTicketPrice() != null ? place.getTicketPrice() : "0.00"));
        }

        if (place.getImageUrl() != null && !place.getImageUrl().isEmpty()) {
            Glide.with(this)
                .load(place.getImageUrl())
                .placeholder(R.drawable.bg_chat_bubble_assistant)
                .centerCrop()
                .into(imgPlaceHero);
        }

        btnAddToItinerary.setOnClickListener(v -> {
            Toast.makeText(this, "Added to Itinerary (Coming Soon)", Toast.LENGTH_SHORT).show();
            // TODO: Implement Logic
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
