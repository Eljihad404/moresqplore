package com.example.moresqplore.ui.guides;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Guide;
import com.example.moresqplore.data.repository.GuideRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

/**
 * Activity for browsing local guides marketplace
 */
public class GuideMarketplaceActivity extends AppCompatActivity {

    private TextInputEditText etSearch;
    private ChipGroup chipGroupCities;
    private TextView tvResultsCount;
    private RecyclerView recyclerViewGuides;
    private ProgressBar progressBar;

    private GuideAdapter adapter;
    private GuideRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_marketplace);

        initializeViews();
        setupRecyclerView();
        setupListeners();

        repository = GuideRepository.getInstance();
        loadGuides(null);
    }

    private void initializeViews() {
        etSearch = findViewById(R.id.etSearch);
        chipGroupCities = findViewById(R.id.chipGroupCities);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        recyclerViewGuides = findViewById(R.id.recyclerViewGuides);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new GuideAdapter();
        recyclerViewGuides.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerViewGuides.setAdapter(adapter);

        adapter.setOnGuideClickListener(guide -> {
            // TODO: Open guide profile activity
            Toast.makeText(this, "Guide profile: " + guide.getName(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupListeners() {
        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    searchGuides(s.toString());
                } else {
                    loadGuides(getSelectedCity());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // City filter
        chipGroupCities.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                String city = getSelectedCity();
                loadGuides(city);
            }
        });
    }

    private String getSelectedCity() {
        int checkedId = chipGroupCities.getCheckedChipId();
        if (checkedId == R.id.chipAll || checkedId == View.NO_ID) {
            return null;
        } else if (checkedId == R.id.chipMarrakech) {
            return "Marrakech";
        } else if (checkedId == R.id.chipFes) {
            return "Fes";
        } else if (checkedId == R.id.chipAgadir) {
            return "Agadir";
        } else if (checkedId == R.id.chipMerzouga) {
            return "Merzouga";
        }
        return null;
    }

    private void loadGuides(String city) {
        progressBar.setVisibility(View.VISIBLE);

        repository.fetchGuides(city, new GuideRepository.OnGuidesLoadedListener() {
            @Override
            public void onSuccess(List<Guide> guides) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.setGuides(guides);
                    tvResultsCount.setText(String.format(Locale.US, "%d guides available", guides.size()));
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(GuideMarketplaceActivity.this,
                            "Error loading guides", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void searchGuides(String query) {
        progressBar.setVisibility(View.VISIBLE);

        repository.searchGuides(query, new GuideRepository.OnGuidesLoadedListener() {
            @Override
            public void onSuccess(List<Guide> guides) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.setGuides(guides);
                    tvResultsCount.setText(String.format(Locale.US, "%d guides found", guides.size()));
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                });
            }
        });
    }
}
