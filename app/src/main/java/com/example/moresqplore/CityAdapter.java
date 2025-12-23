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
        // Inflate the card layout we designed earlier
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city_card, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        String cityName = cityList.get(position);

        // 1. Set the Name
        holder.tvCityName.setText(cityName);

        // 2. Set a specific image for each city (Optional Logic)
        // You should add images named img_casablanca, img_rabat to your drawable folder
        switch (cityName) {
            case "Casablanca":
                holder.imgCity.setImageResource(R.drawable.casablanca); // Replace with R.drawable.img_casablanca
                break;
            case "Marrakech":
                holder.imgCity.setImageResource(R.drawable.marrakech); // Replace with R.drawable.img_marrakech
                break;
            case "Tanger":
                holder.imgCity.setImageResource(R.drawable.tanger); // Replace with R.drawable.img_marrakech
                break;
            case "Rabat":
                holder.imgCity.setImageResource(R.drawable.rabat); // Replace with R.drawable.img_marrakech
                break;
            case "Agadir":
                holder.imgCity.setImageResource(R.drawable.agadir); // Replace with R.drawable.img_marrakech
                break;
            case "Fez":
                holder.imgCity.setImageResource(R.drawable.fez); // Replace with R.drawable.img_marrakech
                break;
            case "Chefchaouen":
                holder.imgCity.setImageResource(R.drawable.chefchaouen); // Replace with R.drawable.img_marrakech
                break;
            case "Essaouira":
                holder.imgCity.setImageResource(R.drawable.essaouira); // Replace with R.drawable.img_marrakech
                break;

            // Add other cases...
            default:
                holder.imgCity.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // 3. Handle Click - Navigate to Main Activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivityOSM.class);
            intent.putExtra("SELECTED_CITY", cityName);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    // ViewHolder Class
    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName;
        ImageView imgCity;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tvCityName);
            imgCity = itemView.findViewById(R.id.imgCity);
        }
    }
}