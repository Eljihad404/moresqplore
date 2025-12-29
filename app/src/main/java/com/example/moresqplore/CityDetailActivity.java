package com.example.moresqplore;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.card.MaterialCardView;

public class CityDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_details);

        // 1. Get Data from Intent (passed from previous screen)
        String cityName = getIntent().getStringExtra("CITY_NAME");

        // 2. Initialize Views
        TextView tvCityName = findViewById(R.id.tvCityName);
        TextView tvCityDescription = findViewById(R.id.tvCityDescription);
        ImageView imgCityHero = findViewById(R.id.imgCityHero);
        LinearLayout timelineContainer = findViewById(R.id.timelineContainer);
        MaterialCardView cardRoadmap = findViewById(R.id.cardRoadmap);
        MaterialCardView cardAI = findViewById(R.id.cardAI);

        // Set Basic Info
        tvCityName.setText(cityName != null ? cityName : "Morocco");
        tvCityDescription.setText(getMockDescription(cityName));

        // 3. Dynamic Timeline Population
        // In a real app, you would fetch this list from Firebase
        String[][] historyEvents = {
                {"7th Century", "Area settled by Berbers"},
                {"15th Century", "Destroyed by Portuguese"},
                {"1912", "French Protectorate established"},
                {"1993", "Hassan II Mosque completed"}
        };

        for (String[] event : historyEvents) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_timeline, timelineContainer, false);
            TextView tvYear = itemView.findViewById(R.id.tvYear);
            TextView tvDesc = itemView.findViewById(R.id.tvEventDesc);

            tvYear.setText(event[0]);
            tvDesc.setText(event[1]);
            timelineContainer.addView(itemView);
        }

        // 4. Smooth Animations
        // Use the same animation you already have!
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slow_fade_up);
        findViewById(R.id.scrollContent).startAnimation(slideUp);

        // Stagger the buttons so they pop up slightly later
        Animation buttonAnim = AnimationUtils.loadAnimation(this, R.anim.slow_fade_up);
        buttonAnim.setStartOffset(200);
        cardRoadmap.startAnimation(buttonAnim);
        cardAI.startAnimation(buttonAnim);

        // 5. Click Listeners
        cardRoadmap.setOnClickListener(v -> {
            // Logic to open Map/Roadmap Activity
            Intent intent = new Intent(CityDetailActivity.this, RoadmapActivity.class);
            intent.putExtra("CITY_NAME", cityName);
            startActivity(intent);
        });

        cardAI.setOnClickListener(v -> {
            // Open AI Chat Assistant
            Intent intent = new Intent(CityDetailActivity.this, 
                    com.example.moresqplore.ui.chat.ChatActivity.class);
            // Optionally pass city context to the chat
            if (cityName != null) {
                intent.putExtra("CITY_CONTEXT", cityName);
            }
            startActivity(intent);
        });
    }

    private String getMockDescription(String city) {
        if ("Casablanca".equals(city)) return "Casablanca is the economic lung of the Kingdom...";
        return "Discover the beauty of Morocco.";
    }
}
