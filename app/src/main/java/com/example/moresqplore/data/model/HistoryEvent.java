package com.example.moresqplore.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class HistoryEvent implements Serializable {
    @SerializedName("year")
    private String year;
    
    @SerializedName("event_title")
    private String event; // Mapped to 'event_title' from JSON, kept as 'event' for code compatibility if preferred, or rename.
    
    @SerializedName("event_description")
    private String description;

    public HistoryEvent() {
    }

    public HistoryEvent(String year, String event, String description) {
        this.year = year;
        this.event = event;
        this.description = description;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
