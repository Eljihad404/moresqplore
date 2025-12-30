package com.example.moresqplore.ui.itinerary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.model.Itinerary;
import com.example.moresqplore.data.repository.PlaceRepository;
import com.example.moresqplore.data.service.ItineraryService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for collecting user preferences for itinerary generation
 */
public class ItineraryInputActivity extends AppCompatActivity {

    private Slider sliderBudget, sliderDuration;
    private TextView tvBudgetValue, tvDurationValue;
    private Spinner spinnerCity;
    private ChipGroup chipGroupInterests;
    private RadioButton radioBudget, radioComfort, radioLuxury;
    private MaterialButton btnGenerate;

    private ItineraryService itineraryService;
    private PlaceRepository placeRepository;

    private static final String[] CITIES = {
            "Marrakech", "Casablanca", "Fes", "Rabat",
            "Chefchaouen", "Essaouira", "Agadir", "Tanger"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_input);

        initializeViews();
        setupListeners();

        itineraryService = new ItineraryService();
        placeRepository = PlaceRepository.getInstance();

        // Load places data
        placeRepository.fetchAllPlaces();
    }

    private void initializeViews() {
        sliderBudget = findViewById(R.id.sliderBudget);
        sliderDuration = findViewById(R.id.sliderDuration);
        tvBudgetValue = findViewById(R.id.tvBudgetValue);
        tvDurationValue = findViewById(R.id.tvDurationValue);
        spinnerCity = findViewById(R.id.spinnerCity);
        chipGroupInterests = findViewById(R.id.chipGroupInterests);
        radioBudget = findViewById(R.id.radioBudget);
        radioComfort = findViewById(R.id.radioComfort);
        radioLuxury = findViewById(R.id.radioLuxury);
        btnGenerate = findViewById(R.id.btnGenerate);

        // Setup city spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CITIES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);
    }

    private void setupListeners() {
        // Budget slider
        sliderBudget.addOnChangeListener((slider, value, fromUser) -> {
            tvBudgetValue.setText(String.format("%.0f MAD", value));
        });

        // Duration slider
        sliderDuration.addOnChangeListener((slider, value, fromUser) -> {
            int days = (int) value;
            tvDurationValue.setText(days + (days == 1 ? " Day" : " Days"));
        });

        // Generate button
        btnGenerate.setOnClickListener(v -> generateItinerary());
    }

    private void generateItinerary() {
        // Collect user inputs
        double budget = sliderBudget.getValue();
        int duration = (int) sliderDuration.getValue();
        String startingCity = spinnerCity.getSelectedItem().toString();
        List<String> interests = getSelectedInterests();
        String travelStyle = getSelectedTravelStyle();

        // Validate
        if (interests.isEmpty()) {
            Toast.makeText(this, "Please select at least one interest",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        btnGenerate.setEnabled(false);
        btnGenerate.setText("Generating...");

        // Create itinerary request
        String userId = "user_" + System.currentTimeMillis(); // TODO: Get from auth
        Itinerary request = new Itinerary(userId, duration, budget,
                startingCity, interests, travelStyle);

        // Get available places
        placeRepository.getPlaces().observe(this, places -> {
            if (places != null && !places.isEmpty()) {
                // Generate itinerary
                itineraryService.generateItinerary(request, places,
                        new ItineraryService.OnItineraryGeneratedListener() {
                            @Override
                            public void onSuccess(Itinerary itinerary) {
                                runOnUiThread(() -> {
                                    btnGenerate.setEnabled(true);
                                    btnGenerate.setText("Generate My Itinerary");

                                    // Navigate to result activity
                                    Intent intent = new Intent(ItineraryInputActivity.this,
                                            ItineraryResultActivity.class);
                                    intent.putExtra("ITINERARY", itinerary);
                                    startActivity(intent);
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                runOnUiThread(() -> {
                                    btnGenerate.setEnabled(true);
                                    btnGenerate.setText("Generate My Itinerary");
                                    Toast.makeText(ItineraryInputActivity.this,
                                            "Failed to generate itinerary: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        });
            } else {
                btnGenerate.setEnabled(true);
                btnGenerate.setText("Generate My Itinerary");
                Toast.makeText(this, "No places available. Please try again.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> getSelectedInterests() {
        List<String> interests = new ArrayList<>();
        for (int i = 0; i < chipGroupInterests.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupInterests.getChildAt(i);
            if (chip.isChecked()) {
                interests.add(chip.getText().toString());
            }
        }
        return interests;
    }

    private String getSelectedTravelStyle() {
        if (radioBudget.isChecked())
            return "budget";
        if (radioLuxury.isChecked())
            return "luxury";
        return "comfort";
    }
}
