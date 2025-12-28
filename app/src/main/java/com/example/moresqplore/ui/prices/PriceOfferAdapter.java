package com.example.moresqplore.ui.prices;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.PriceOffer;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying price offers from different providers
 */
public class PriceOfferAdapter extends RecyclerView.Adapter<PriceOfferAdapter.OfferViewHolder> {

    private List<PriceOffer> offers = new ArrayList<>();
    private double referencePrice = 0; // For calculating savings

    public void setOffers(List<PriceOffer> offers) {
        this.offers = offers;
        // Calculate reference price (average)
        if (!offers.isEmpty()) {
            double sum = 0;
            for (PriceOffer offer : offers) {
                sum += offer.getPrice();
            }
            referencePrice = sum / offers.size();
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_price_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        PriceOffer offer = offers.get(position);
        holder.bind(offer, referencePrice);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    static class OfferViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipBestDeal;
        private final ImageView ivProviderLogo;
        private final TextView tvProviderName;
        private final TextView tvRating;
        private final TextView tvReviewCount;
        private final TextView tvSpecialOffer;
        private final TextView tvPrice;
        private final TextView tvSavings;
        private final MaterialButton btnBook;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            chipBestDeal = itemView.findViewById(R.id.chipBestDeal);
            ivProviderLogo = itemView.findViewById(R.id.ivProviderLogo);
            tvProviderName = itemView.findViewById(R.id.tvProviderName);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            tvSpecialOffer = itemView.findViewById(R.id.tvSpecialOffer);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSavings = itemView.findViewById(R.id.tvSavings);
            btnBook = itemView.findViewById(R.id.btnBook);
        }

        public void bind(PriceOffer offer, double referencePrice) {
            // Provider name
            tvProviderName.setText(offer.getProviderName());

            // Best deal badge
            chipBestDeal.setVisibility(offer.isBestDeal() ? View.VISIBLE : View.GONE);

            // Rating
            tvRating.setText(String.format(Locale.US, "⭐ %.1f", offer.getRating()));
            tvReviewCount.setText(String.format(Locale.US, "(%d reviews)", offer.getReviewCount()));

            // Special offer
            if (offer.getSpecialOffer() != null && !offer.getSpecialOffer().isEmpty()) {
                tvSpecialOffer.setVisibility(View.VISIBLE);
                tvSpecialOffer.setText("✓ " + offer.getSpecialOffer());
            } else {
                tvSpecialOffer.setVisibility(View.GONE);
            }

            // Price
            tvPrice.setText(offer.getFormattedPrice());

            // Savings
            if (offer.getPrice() < referencePrice) {
                tvSavings.setVisibility(View.VISIBLE);
                double savings = referencePrice - offer.getPrice();
                tvSavings.setText(String.format(Locale.US, "Save %.0f MAD", savings));
            } else {
                tvSavings.setVisibility(View.GONE);
            }

            // Book button
            btnBook.setOnClickListener(v -> {
                if (offer.getBookingUrl() != null && !offer.getBookingUrl().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(offer.getBookingUrl()));
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
