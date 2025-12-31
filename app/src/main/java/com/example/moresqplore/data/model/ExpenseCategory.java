package com.example.moresqplore.data.model;

/**
 * Enum representing expense categories
 */
public enum ExpenseCategory {
    FOOD("Food", "🍽️", "#E67E22"),
    TRANSPORT("Transport", "🚗", "#3498DB"),
    ACCOMMODATION("Accommodation", "🏨", "#9B59B6"),
    ACTIVITIES("Activities", "🎭", "#E74C3C"),
    SHOPPING("Shopping", "🛍️", "#F39C12"),
    OTHER("Other", "📌", "#95A5A6");

    private final String displayName;
    private final String icon;
    private final String colorHex;

    ExpenseCategory(String displayName, String icon, String colorHex) {
        this.displayName = displayName;
        this.icon = icon;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public String getColorHex() {
        return colorHex;
    }

    public String getIconWithName() {
        return icon + " " + displayName;
    }
}
