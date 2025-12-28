package com.example.moresqplore.ui.itinerary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moresqplore.R;
import com.example.moresqplore.data.model.DayPlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying day plans in itinerary
 */
public class DayPlanAdapter extends RecyclerView.Adapter<DayPlanAdapter.DayViewHolder> {

    private List<DayPlan> dayPlans = new ArrayList<>();

    public void setDayPlans(List<DayPlan> dayPlans) {
        this.dayPlans = dayPlans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_day_plan, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayPlan dayPlan = dayPlans.get(position);
        holder.bind(dayPlan);
    }

    @Override
    public int getItemCount() {
        return dayPlans.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDayNumber;
        private final TextView tvDayCity;
        private final TextView tvDaySummary;
        private final TextView tvDayBudget;
        private final RecyclerView recyclerViewActivities;
        private final ActivityAdapter activityAdapter;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayCity = itemView.findViewById(R.id.tvDayCity);
            tvDaySummary = itemView.findViewById(R.id.tvDaySummary);
            tvDayBudget = itemView.findViewById(R.id.tvDayBudget);
            recyclerViewActivities = itemView.findViewById(R.id.recyclerViewActivities);

            // Setup activities RecyclerView
            activityAdapter = new ActivityAdapter();
            recyclerViewActivities.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            recyclerViewActivities.setAdapter(activityAdapter);
        }

        public void bind(DayPlan dayPlan) {
            tvDayNumber.setText(String.valueOf(dayPlan.getDayNumber()));
            tvDayCity.setText(dayPlan.getCity());
            tvDaySummary.setText(dayPlan.getSummary());
            tvDayBudget.setText(String.format("%.0f MAD", dayPlan.getEstimatedCost()));

            activityAdapter.setActivities(dayPlan.getActivities());
        }
    }
}
