package com.example.moresqplore;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.moresqplore.data.model.City;
import com.example.moresqplore.data.model.HistoryEvent;
import com.example.moresqplore.data.repository.CityRepository;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class CityDetailActivity extends AppCompatActivity {

    private CityRepository cityRepository;
    private TextView tvCityName;
    private TextView tvCityDescription;
    private LinearLayout timelineContainer;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        cityRepository = new CityRepository(this);

        // 1. Get Data from Intent
        String cityName = getIntent().getStringExtra("CITY_NAME");
        if (cityName == null) cityName = "Morocco";

        // 2. Initialize Views
        tvCityName = findViewById(R.id.tvCityName);
        tvCityDescription = findViewById(R.id.tvCityDescription);
        ImageView imgCityHero = findViewById(R.id.imgCityHero);
        timelineContainer = findViewById(R.id.timelineContainer);
        MaterialCardView cardRoadmap = findViewById(R.id.cardRoadmap);
        MaterialCardView cardAI = findViewById(R.id.cardAI);

        // Set Basic Info
        tvCityName.setText(cityName);
        
        // Load Data
        loadCityData(cityName);
        
        // Load Places
        setupPlacesList(cityName);

        // 4. Smooth Animations
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slow_fade_up);
        findViewById(R.id.scrollContent).startAnimation(slideUp);

        // Stagger the buttons
        Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.slow_fade_up);
        buttonAnim.setStartOffset(200);
        cardRoadmap.startAnimation(buttonAnim);
        cardAI.startAnimation(buttonAnim);

        // 5. Click Listeners
        cardRoadmap.setOnClickListener(v -> {
            Intent intent = new Intent(CityDetailActivity.this, RoadmapActivity.class);
            intent.putExtra("CITY_NAME", tvCityName.getText().toString());
            startActivity(intent);
        });

        cardAI.setOnClickListener(v -> {
            Intent intent = new Intent(CityDetailActivity.this, 
                    com.example.moresqplore.ui.chat.ChatActivity.class);
            String currentCity = tvCityName.getText().toString();
            intent.putExtra("CITY_CONTEXT", currentCity);
            startActivity(intent);
        });
    }

    private void loadCityData(String cityName) {
        tvCityDescription.setText("Loading city details...");
        
        cityRepository.getCityData(cityName, new CityRepository.CityCallback() {
            @Override
            public void onSuccess(City city) {
                // Ensure UI updates happen on Main Thread
                mainHandler.post(() -> {
                    tvCityDescription.setText(city.getDescription());
                    populateTimeline(city.getHistoryEvents());
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    tvCityDescription.setText("Failed to load details. Please check your connection.");
                    Toast.makeText(CityDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    // Fallback to mock data if needed, or just leave as is
                    if ("Casablanca".equals(cityName)) {
                        tvCityDescription.setText("Casablanca is the economic lung of the Kingdom... (Offline Fallback)");
                    }
                });
            }
        });
    }

    private void populateTimeline(List<HistoryEvent> events) {
        timelineContainer.removeAllViews();
        if (events == null || events.isEmpty()) return;

        for (HistoryEvent event : events) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_timeline, timelineContainer, false);
            TextView tvYear = itemView.findViewById(R.id.tvYear);
            TextView tvDesc = itemView.findViewById(R.id.tvEventDesc);

            tvYear.setText(event.getYear());
            tvDesc.setText(event.getEvent());
            timelineContainer.addView(itemView);
        }
    }

    private void setupPlacesList(String cityName) {
        androidx.recyclerview.widget.RecyclerView recyclerPlaces = findViewById(R.id.recyclerPlaces);
        if (recyclerPlaces != null) {
            // Setup Adapter
            com.example.moresqplore.ui.adapter.PlaceAdapter adapter = new com.example.moresqplore.ui.adapter.PlaceAdapter(place -> {
                // Handle Click
                Intent intent = new Intent(CityDetailActivity.this, PlaceDetailActivity.class);
                intent.putExtra("PLACE_ID", place.getId());
                startActivity(intent);
            });
            recyclerPlaces.setAdapter(adapter);

            // Fetch Data
            com.example.moresqplore.data.repository.PlaceRepository.getInstance()
                .fetchPlacesByCity(cityName)
                .observe(this, places -> {
                    if (places != null && !places.isEmpty()) {
                        adapter.setPlaces(places);
                    } else {
                        // Maybe hide the list or show empty state?
                        // For now, let's just log or ignore
                    }
                });
        }
    }
}
