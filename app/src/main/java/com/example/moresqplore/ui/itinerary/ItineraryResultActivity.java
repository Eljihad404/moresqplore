package com.example.moresqplore.ui.itinerary;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.ChatActivity;
import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Itinerary;
import com.example.moresqplore.data.repository.ItineraryRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Activity for displaying generated itinerary results
 */
public class ItineraryResultActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvItineraryTitle;
    private TextView tvBudget;
    private TextView tvEstimatedCost;
    private TextView tvOptimizationScore;
    private RecyclerView recyclerViewDays;
    private MaterialButton btnRefine;
    private MaterialButton btnSave;
    private View loadingOverlay;

    private DayPlanAdapter dayPlanAdapter;
    private Itinerary itinerary;
    private ItineraryRepository itineraryRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_result);

        initializeViews();
        setupRecyclerView();

        itineraryRepository = ItineraryRepository.getInstance();

        // Get itinerary from intent
        itinerary = (Itinerary) getIntent().getSerializableExtra("ITINERARY");

        if (itinerary != null) {
            displayItinerary();
        } else {
            Toast.makeText(this, "Error loading itinerary", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvItineraryTitle = findViewById(R.id.tvItineraryTitle);
        tvBudget = findViewById(R.id.tvBudget);
        tvEstimatedCost = findViewById(R.id.tvEstimatedCost);
        tvOptimizationScore = findViewById(R.id.tvOptimizationScore);
        recyclerViewDays = findViewById(R.id.recyclerViewDays);
        btnRefine = findViewById(R.id.btnRefine);
        btnSave = findViewById(R.id.btnSave);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        dayPlanAdapter = new DayPlanAdapter();
        recyclerViewDays.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDays.setAdapter(dayPlanAdapter);
    }

    private void displayItinerary() {
        tvItineraryTitle.setText(itinerary.getTitle());
        tvBudget.setText(String.format("%.0f MAD", itinerary.getTotalBudget()));
        tvEstimatedCost.setText(String.format("%.0f MAD", itinerary.getEstimatedCost()));
        tvOptimizationScore.setText(String.format("%.0f/100", itinerary.getOptimizationScore()));

        dayPlanAdapter.setDayPlans(itinerary.getDayPlans());
    }

    private void setupListeners() {
        // Refine with AI button
        btnRefine.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CONTEXT_TYPE", "itinerary_refinement");
            intent.putExtra("ITINERARY_ID", itinerary.getId());
            startActivity(intent);
        });

        // Save button
        btnSave.setOnClickListener(v -> saveItinerary());
    }

    private void saveItinerary() {
        loadingOverlay.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        itineraryRepository.saveItinerary(itinerary,
                new ItineraryRepository.OnSaveListener() {
                    @Override
                    public void onSuccess(String itineraryId) {
                        runOnUiThread(() -> {
                            loadingOverlay.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            btnSave.setText("Saved ✓");
                            btnSave.setIconResource(android.R.drawable.checkbox_on_background);
                            Toast.makeText(ItineraryResultActivity.this,
                                    "Itinerary saved successfully!", Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            loadingOverlay.setVisibility(View.GONE);
                            btnSave.setEnabled(true);
                            Toast.makeText(ItineraryResultActivity.this,
                                    "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
}
