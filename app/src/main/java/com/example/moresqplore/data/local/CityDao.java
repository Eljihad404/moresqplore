package com.example.moresqplore.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.example.moresqplore.data.model.City;

@Dao
public interface CityDao {
    @Query("SELECT * FROM cities WHERE name = :cityName LIMIT 1")
    City getCity(String cityName);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCity(City city);
}
