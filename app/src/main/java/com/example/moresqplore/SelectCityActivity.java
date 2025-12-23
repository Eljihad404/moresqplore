package com.example.moresqplore;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SelectCityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_city);

        RecyclerView recyclerCities = findViewById(R.id.recyclerCities);

        // 1. Create the Data
        List<String> cities = new ArrayList<>();
        cities.add("Casablanca");
        cities.add("Marrakech");
        cities.add("Tanger");
        cities.add("Rabat");
        cities.add("Fez");
        cities.add("Agadir");
        cities.add("Chefchaouen");
        cities.add("Essaouira");

        // 2. Set Layout Manager (Grid with 2 columns)
        recyclerCities.setLayoutManager(new GridLayoutManager(this, 2));

        // 3. Set the Adapter (THIS WAS MISSING)
        CityAdapter adapter = new CityAdapter(this, cities);
        recyclerCities.setAdapter(adapter);
    }
}