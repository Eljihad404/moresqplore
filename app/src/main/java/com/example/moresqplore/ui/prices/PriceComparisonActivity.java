package com.example.moresqplore.ui.prices;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.PriceComparison;
import com.example.moresqplore.data.repository.PriceComparisonRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

/**
 * Activity for comparing prices from multiple booking providers
 */
public class PriceComparisonActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TextInputEditText etCity, etCheckIn, etCheckOut, etGuests;
    private MaterialButton btnSearchHotels;
    private RecyclerView recyclerViewOffers;
    private ProgressBar progressBar;
    private View emptyState;
    private MaterialCardView statsCard;
    private TextView tvResultsCount, tvMinPrice, tvAvgPrice, tvMaxPrice;
    private View resultsHeader;

    private PriceOfferAdapter adapter;
    private PriceComparisonRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_comparison);

        initializeViews();
        setupRecyclerView();
        setupListeners();

        repository = PriceComparisonRepository.getInstance();
    }

    private void initializeViews() {
        tabLayout = findViewById(R.id.tabLayout);
        etCity = findViewById(R.id.etCity);
        etCheckIn = findViewById(R.id.etCheckIn);
        etCheckOut = findViewById(R.id.etCheckOut);
        etGuests = findViewById(R.id.etGuests);
        btnSearchHotels = findViewById(R.id.btnSearchHotels);
        recyclerViewOffers = findViewById(R.id.recyclerViewOffers);
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        statsCard = findViewById(R.id.statsCard);
        tvResultsCount = findViewById(R.id.tvResultsCount);
        tvMinPrice = findViewById(R.id.tvMinPrice);
        tvAvgPrice = findViewById(R.id.tvAvgPrice);
        tvMaxPrice = findViewById(R.id.tvMaxPrice);
        resultsHeader = findViewById(R.id.resultsHeader);
    }

    private void setupRecyclerView() {
        adapter = new PriceOfferAdapter();
        recyclerViewOffers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewOffers.setAdapter(adapter);
    }

    private void setupListeners() {
        btnSearchHotels.setOnClickListener(v -> searchHotelPrices());

        // Tab selection (simplified - only hotels for MVP)
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Future: Switch between hotels, flights, activities
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void searchHotelPrices() {
        String city = etCity.getText().toString();
        String checkIn = "2024-06-01"; // Simplified for MVP
        String checkOut = "2024-06-03";
        int guests = Integer.parseInt(etGuests.getText().toString());

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recyclerViewOffers.setVisibility(View.GONE);

        repository.fetchHotelPrices("Hotels in " + city, city, checkIn, checkOut, guests,
                new PriceComparisonRepository.OnPricesLoadedListener() {
                    @Override
                    public void onSuccess(PriceComparison comparison) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            displayResults(comparison);
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            emptyState.setVisibility(View.VISIBLE);
                        });
                    }
                });
    }

    private void displayResults(PriceComparison comparison) {
        // Show results
        recyclerViewOffers.setVisibility(View.VISIBLE);
        statsCard.setVisibility(View.VISIBLE);
        resultsHeader.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);

        // Update adapter
        adapter.setOffers(comparison.getOffers());

        // Update statistics
        tvResultsCount.setText(String.format(Locale.US, "%d offers found",
                comparison.getAvailableOffersCount()));
        tvMinPrice.setText(String.format(Locale.US, "%.0f MAD", comparison.getMinPrice()));
        tvAvgPrice.setText(String.format(Locale.US, "%.0f MAD", comparison.getAvgPrice()));
        tvMaxPrice.setText(String.format(Locale.US, "%.0f MAD", comparison.getMaxPrice()));
    }
}
