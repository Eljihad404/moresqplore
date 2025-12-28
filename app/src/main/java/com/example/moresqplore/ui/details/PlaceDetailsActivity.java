package com.example.moresqplore.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.moresqplore.Place;
import com.example.moresqplore.R;
import com.example.moresqplore.data.network.WikiService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PlaceDetailsActivity extends AppCompatActivity {

    private Place place;
    private TextView tvDescription, tvWikiContent;
    private MaterialButton btnReadMore;
    private View loadingWiki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        // Get Place Data
        place = (Place) getIntent().getSerializableExtra("PLACE_DATA");
        if (place == null) {
            finish();
            return;
        }

        initializeViews();
        displayLocalData();
        fetchWikiData();
    }

    private void initializeViews() {
        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Standard Views
        TextView tvTitle = findViewById(R.id.tvPlaceTitle);
        TextView tvCity = findViewById(R.id.tvCityName);
        TextView tvRating = findViewById(R.id.tvRating);
        tvDescription = findViewById(R.id.tvDescription);
        tvWikiContent = findViewById(R.id.tvWikiContent);
        Chip chipCategory = findViewById(R.id.chipCategory);
        btnReadMore = findViewById(R.id.btnReadMore);
        loadingWiki = findViewById(R.id.loadingWiki);

        tvTitle.setText(place.getName());
        tvCity.setText(place.getCity());
        tvRating.setText(String.format("★ %.1f (%d reviews)", place.getAverageRating(), place.getTotalReviews()));
        chipCategory.setText(place.getCategory());
    }

    private void displayLocalData() {
        tvDescription.setText(place.getDescription());

        // Placeholder image logic (same as adapter) - In real app, use
        // place.getImageUrl()
        ImageView imgHeader = findViewById(R.id.imgPlaceHeader);
        int placeholderRes = getPlaceholderImage(place.getCity());
        imgHeader.setImageResource(placeholderRes);
    }

    private int getPlaceholderImage(String city) {
        // Simplified Logic mapping city to drawable
        // Note: Ensure these drawables exist, otherwise fallback
        if ("Casablanca".equals(city))
            return R.drawable.casablanca;
        if ("Marrakech".equals(city))
            return R.drawable.marrakech;
        if ("Chefchaouen".equals(city))
            return R.drawable.chefchaouen;
        if ("Fez".equals(city))
            return R.drawable.fez;
        if ("Rabat".equals(city))
            return R.drawable.rabat;
        if ("Essaouira".equals(city))
            return R.drawable.essaouira;
        if ("Agadir".equals(city))
            return R.drawable.agadir;
        if ("Tanger".equals(city))
            return R.drawable.tanger;
        return R.drawable.ic_launcher_background; // Fallback
    }

    private void fetchWikiData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://en.wikipedia.org/api/rest_v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WikiService service = retrofit.create(WikiService.class);

        // Use place name for query
        Call<WikiService.WikiSummary> call = service.getSummary(place.getName().replace(" ", "_"));

        call.enqueue(new Callback<WikiService.WikiSummary>() {
            @Override
            public void onResponse(Call<WikiService.WikiSummary> call, Response<WikiService.WikiSummary> response) {
                loadingWiki.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    WikiService.WikiSummary summary = response.body();
                    tvWikiContent.setText(summary.extract);
                    tvWikiContent.setVisibility(View.VISIBLE);

                    if (summary.contentUrls != null && summary.contentUrls.mobile != null) {
                        btnReadMore.setVisibility(View.VISIBLE);
                        btnReadMore.setOnClickListener(v -> {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(summary.contentUrls.mobile.page));
                            startActivity(browserIntent);
                        });
                    }
                } else {
                    tvWikiContent.setText("Wikipedia summary not available for this location.");
                    tvWikiContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<WikiService.WikiSummary> call, Throwable t) {
                loadingWiki.setVisibility(View.GONE);
                tvWikiContent.setText("Could not connect to Wikipedia.");
                tvWikiContent.setVisibility(View.VISIBLE);
            }
        });
    }
}
