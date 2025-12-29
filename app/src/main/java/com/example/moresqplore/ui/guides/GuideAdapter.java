package com.example.moresqplore.ui.guides;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Guide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying guide cards
 */
public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private List<Guide> guides = new ArrayList<>();
    private OnGuideClickListener listener;

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide);
    }

    public void setGuides(List<Guide> guides) {
        this.guides = guides;
        notifyDataSetChanged();
    }

    public void setOnGuideClickListener(OnGuideClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_guide_card, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        Guide guide = guides.get(position);
        holder.bind(guide, listener);
    }

    @Override
    public int getItemCount() {
        return guides.size();
    }

    static class GuideViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivGuidePhoto;
        private final TextView tvGuideName;
        private final Chip chipBadge;
        private final TextView tvCity;
        private final TextView tvRating;
        private final TextView tvReviews;
        private final TextView tvSpecializations;
        private final TextView tvLanguages;
        private final TextView tvPrice;
        private final MaterialButton btnViewProfile;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGuidePhoto = itemView.findViewById(R.id.ivGuidePhoto);
            tvGuideName = itemView.findViewById(R.id.tvGuideName);
            chipBadge = itemView.findViewById(R.id.chipBadge);
            tvCity = itemView.findViewById(R.id.tvCity);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviews = itemView.findViewById(R.id.tvReviews);
            tvSpecializations = itemView.findViewById(R.id.tvSpecializations);
            tvLanguages = itemView.findViewById(R.id.tvLanguages);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
        }

        public void bind(Guide guide, OnGuideClickListener listener) {
            // Name
            tvGuideName.setText(guide.getName());

            // Badge
            if (guide.getVerificationBadge() != null && !guide.getVerificationBadge().isEmpty()) {
                chipBadge.setVisibility(View.VISIBLE);
                chipBadge.setText(guide.getVerificationBadge());
            } else {
                chipBadge.setVisibility(View.GONE);
            }

            // City
            tvCity.setText("📍 " + guide.getCity());

            // Rating
            tvRating.setText(String.format(Locale.US, "⭐ %.1f", guide.getRating()));
            tvReviews.setText(String.format(Locale.US, "(%d reviews)", guide.getTotalReviews()));

            // Specializations
            tvSpecializations.setText(guide.getSpecializationsString().replace(", ", " • "));

            // Languages
            tvLanguages.setText("🗣️ " + guide.getLanguagesString());

            // Price
            if (guide.getDayRate() > 0 && guide.getHourlyRate() == 0) {
                tvPrice.setText(String.format(Locale.US, "%.0f MAD/day", guide.getDayRate()));
            } else {
                tvPrice.setText(String.format(Locale.US, "%.0f MAD/hour", guide.getHourlyRate()));
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideClick(guide);
                }
            });

            btnViewProfile.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGuideClick(guide);
                }
            });
        }
    }
}
