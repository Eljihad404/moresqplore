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

    private ImageView imgCityHero;

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
        imgCityHero = findViewById(R.id.imgCityHero);
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
            final String selectedCity = tvCityName.getText().toString();
            intent.putExtra("CITY_NAME", selectedCity);
            
            // Try to get coordinates from city data, or use fallback
            Double cityLat = null;
            Double cityLng = null;
            
            if (currentCity != null && currentCity.getLatitude() != null && currentCity.getLongitude() != null) {
                cityLat = currentCity.getLatitude();
                cityLng = currentCity.getLongitude();
            } else {
                // Fallback coordinates for major cities
                switch (selectedCity) {
                    case "Casablanca":
                        cityLat = 33.5731;
                        cityLng = -7.5898;
                        break;
                    case "Marrakech":
                        cityLat = 31.6295;
                        cityLng = -7.9811;
                        break;
                    case "Rabat":
                        cityLat = 34.0209;
                        cityLng = -6.8416;
                        break;
                    case "Fez":
                        cityLat = 34.0181;
                        cityLng = -5.0078;
                        break;
                    case "Tanger":
                        cityLat = 35.7595;
                        cityLng = -5.8340;
                        break;
                    case "Agadir":
                        cityLat = 30.4278;
                        cityLng = -9.5981;
                        break;
                    case "Chefchaouen":
                        cityLat = 35.1689;
                        cityLng = -5.2636;
                        break;
                    case "Essaouira":
                        cityLat = 31.5125;
                        cityLng = -9.7700;
                        break;
                }
            }
            
            if (cityLat != null && cityLng != null) {
                intent.putExtra("CITY_LAT", cityLat);
                intent.putExtra("CITY_LNG", cityLng);
            }
            
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

    private City currentCity; // Store loaded city

    private void loadCityData(String cityName) {
        tvCityDescription.setText("Loading city details...");
        
        cityRepository.getCityData(cityName, new CityRepository.CityCallback() {
            @Override
            public void onSuccess(City city) {
                currentCity = city; // Store for usage in click listeners
                // Ensure UI updates happen on Main Thread
                mainHandler.post(() -> {
                    tvCityDescription.setText(city.getDescription());
                    
                    // Load Hero Image
                    // Load Hero Image (Local Drawables)
                    switch (city.getName()) {
                        case "Casablanca":
                            imgCityHero.setImageResource(R.drawable.casablanca);
                            break;
                        case "Marrakech":
                            imgCityHero.setImageResource(R.drawable.marrakech);
                            break;
                        case "Tanger":
                            imgCityHero.setImageResource(R.drawable.tanger);
                            break;
                        case "Rabat":
                            imgCityHero.setImageResource(R.drawable.rabat);
                            break;
                        case "Agadir":
                            imgCityHero.setImageResource(R.drawable.agadir);
                            break;
                        case "Fez":
                            imgCityHero.setImageResource(R.drawable.fez);
                            break;
                        case "Chefchaouen":
                            imgCityHero.setImageResource(R.drawable.chefchaouen);
                            break;
                         case "Essaouira":
                            imgCityHero.setImageResource(R.drawable.essaouira);
                            break;
                        default:
                             // Try to load from URL if not in local list, or fallback
                             if (city.getCoverImageUrl() != null && !city.getCoverImageUrl().isEmpty()) {
                                // Create a GlideUrl with headers to avoid 429 errors
                                com.bumptech.glide.load.model.GlideUrl glideUrl = new com.bumptech.glide.load.model.GlideUrl(
                                    city.getCoverImageUrl(), 
                                    new com.bumptech.glide.load.model.LazyHeaders.Builder()
                                        .addHeader("User-Agent", "MoresqploreApp/1.0 (Android; +https://github.com/Eljihad404/moresqplore)")
                                        .build()
                                );

                                com.bumptech.glide.Glide.with(CityDetailActivity.this)
                                    .load(glideUrl)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .error(android.R.drawable.ic_menu_report_image)
                                    .centerCrop()
                                    .into(imgCityHero);
                             } else {
                                imgCityHero.setImageResource(android.R.drawable.ic_menu_gallery);
                             }
                    }
                    
                    populateTimeline(city.getHistoryEvents());
                });
            }

            @Override
            public void onError(Exception e) {
                mainHandler.post(() -> {
                    tvCityDescription.setText("Failed to load details. Please check your connection.");
                    Toast.makeText(CityDetailActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    // Fallback to mock data if needed
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
                // Navigate to map with place coordinates
                Intent intent = new Intent(CityDetailActivity.this, RoadmapActivity.class);
                intent.putExtra("CITY_NAME", place.getCity());
                
                // Pass place coordinates if available
                if (place.getLatitude() != null && place.getLongitude() != null) {
                    intent.putExtra("PLACE_LAT", place.getLatitude());
                    intent.putExtra("PLACE_LNG", place.getLongitude());
                    intent.putExtra("PLACE_NAME", place.getName());
                }
                
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
