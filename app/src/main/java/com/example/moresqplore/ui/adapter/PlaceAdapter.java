package com.example.moresqplore.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Place;
import java.util.ArrayList;
import java.util.List;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> places = new ArrayList<>();
    private final OnPlaceClickListener listener;

    public interface OnPlaceClickListener {
        void onPlaceClick(Place place);
    }

    public PlaceAdapter(OnPlaceClickListener listener) {
        this.listener = listener;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
        Place place = places.get(position);
        holder.bind(place);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgThumbnail;
        private final TextView tvName;
        private final TextView tvCategory;
        private final TextView tvRating;
        private final TextView tvReviewCount;
        private final TextView tvPrice;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgPlaceThumbnail);
            tvName = itemView.findViewById(R.id.tvPlaceName);
            tvCategory = itemView.findViewById(R.id.tvPlaceCategory);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPlaceClick(places.get(position));
                }
            });
        }

        public void bind(Place place) {
            tvName.setText(place.getName());
            tvCategory.setText(place.getCategory());
            
            // Handle optional rating
            double rating = place.getRating() != null ? place.getRating() : 0.0;
            tvRating.setText(String.format("%.1f", rating));
            
            // Handle optional review count
            int count = place.getReviewCount() != null ? place.getReviewCount() : 0;
            tvReviewCount.setText("(" + count + ")");

            // Handle Price
            if (place.isFreeEntry()) {
                tvPrice.setText("Free");
                tvPrice.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
            } else if (place.getTicketPrice() != null) {
                tvPrice.setText(String.format("MAD %.2f", place.getTicketPrice()));
            } else {
                tvPrice.setText("");
            }

            // Load Image
            if (place.getThumbnailUrl() != null && !place.getThumbnailUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(place.getThumbnailUrl())
                        .placeholder(R.drawable.bg_chat_bubble_assistant) // Use a valid drawable
                        .centerCrop()
                        .into(imgThumbnail);
            } else {
                 imgThumbnail.setImageResource(R.drawable.bg_chat_bubble_assistant);
            }
        }
    }
}
