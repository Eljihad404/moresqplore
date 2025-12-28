package com.example.moresqplore.ui.itinerary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.Activity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying activities within a day plan
 */
public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private List<Activity> activities = new ArrayList<>();

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvActivityTime;
        private final TextView tvActivityName;
        private final TextView tvActivityDescription;
        private final Chip chipActivityType;
        private final TextView tvActivityDuration;
        private final TextView tvActivityCost;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvActivityTime = itemView.findViewById(R.id.tvActivityTime);
            tvActivityName = itemView.findViewById(R.id.tvActivityName);
            tvActivityDescription = itemView.findViewById(R.id.tvActivityDescription);
            chipActivityType = itemView.findViewById(R.id.chipActivityType);
            tvActivityDuration = itemView.findViewById(R.id.tvActivityDuration);
            tvActivityCost = itemView.findViewById(R.id.tvActivityCost);
        }

        public void bind(Activity activity) {
            tvActivityTime.setText(activity.getStartTime());
            tvActivityName.setText(activity.getPlaceName());
            tvActivityDescription.setText(activity.getDescription());

            // Set activity type chip
            String type = activity.getActivityType();
            chipActivityType.setText(capitalizeFirst(type));

            // Set duration
            int hours = activity.getDurationMinutes() / 60;
            int minutes = activity.getDurationMinutes() % 60;
            String durationText = hours > 0 ? String.format("⏱ %dh %dm", hours, minutes)
                    : String.format("⏱ %dm", minutes);
            tvActivityDuration.setText(durationText);

            // Set cost
            tvActivityCost.setText(String.format("%.0f MAD", activity.getEstimatedCost()));
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty())
                return text;
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }
    }
}
