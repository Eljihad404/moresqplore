package com.example.moresqplore.data.local;

import androidx.room.TypeConverter;
import com.example.moresqplore.data.model.HistoryEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class DataConverters {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static List<HistoryEvent> fromString(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<HistoryEvent>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<HistoryEvent> list) {
        return gson.toJson(list);
    }

    @TypeConverter
    public static List<com.example.moresqplore.data.model.CityGalleryImage> fromGalleryString(String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<com.example.moresqplore.data.model.CityGalleryImage>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    @TypeConverter
    public static String fromGalleryList(List<com.example.moresqplore.data.model.CityGalleryImage> list) {
        return gson.toJson(list);
    }
}
