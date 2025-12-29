package com.example.moresqplore;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final List<String> cityList;
    private final Context context;

    public CityAdapter(Context context, List<String> cityList) {
        this.context = context;
        this.cityList = cityList;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_card, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        String cityName = cityList.get(position);

        // 1. Set the Name
        holder.tvCityName.setText(cityName);

        // 2. Set the Image (Kept your logic, just ensure these files exist in res/drawable)
        switch (cityName) {
            case "Casablanca":
                holder.imgCity.setImageResource(R.drawable.casablanca);
                break;
            case "Marrakech":
                holder.imgCity.setImageResource(R.drawable.marrakech);
                break;
            case "Tanger":
                holder.imgCity.setImageResource(R.drawable.tanger);
                break;
            case "Rabat":
                holder.imgCity.setImageResource(R.drawable.rabat);
                break;
            case "Agadir":
                holder.imgCity.setImageResource(R.drawable.agadir);
                break;
            case "Fez":
                holder.imgCity.setImageResource(R.drawable.fez);
                break;
            case "Chefchaouen":
                holder.imgCity.setImageResource(R.drawable.chefchaouen);
                break;
            case "Essaouira":
                holder.imgCity.setImageResource(R.drawable.essaouira);
                break;
            default:
                // Fallback image if city not found
                holder.imgCity.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 3. HANDLE CLICK - UPDATED DESTINATION
        holder.itemView.setOnClickListener(v -> {
            // CHANGE 1: Point to the new Detail Activity
            Intent intent = new Intent(context, CityDetailActivity.class);

            // CHANGE 2: Use the key "CITY_NAME" (must match the key in CityDetailActivity)
            intent.putExtra("CITY_NAME", cityName);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        ImageView imgCity;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            // Ensure your item_city_card.xml actually has an ImageView with id "imgCity"
            imgCity = itemView.findViewById(R.id.imgCity);
        }
    }
}