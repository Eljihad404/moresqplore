package com.example.moresqplore.data.model;

/**
 * Enum representing supported booking providers
 */
public enum BookingProvider {
    // Hotels
    BOOKING_COM("Booking.com", "https://www.booking.com", "HOTEL"),
    AIRBNB("Airbnb", "https://www.airbnb.com", "HOTEL"),
    AGODA("Agoda", "https://www.agoda.com", "HOTEL"),
    HOTELS_COM("Hotels.com", "https://www.hotels.com", "HOTEL"),
    EXPEDIA("Expedia", "https://www.expedia.com", "HOTEL"),

    // Flights
    ROYAL_AIR_MAROC("Royal Air Maroc", "https://www.royalairmaroc.com", "FLIGHT"),
    AIR_ARABIA("Air Arabia", "https://www.airarabia.com", "FLIGHT"),
    SKYSCANNER("Skyscanner", "https://www.skyscanner.com", "FLIGHT"),

    // Activities
    GETYOURGUIDE("GetYourGuide", "https://www.getyourguide.com", "ACTIVITY"),
    VIATOR("Viator", "https://www.viator.com", "ACTIVITY"),
    KLOOK("Klook", "https://www.klook.com", "ACTIVITY");

    private final String displayName;
    private final String baseUrl;
    private final String itemType;

    BookingProvider(String displayName, String baseUrl, String itemType) {
        this.displayName = displayName;
        this.baseUrl = baseUrl;
        this.itemType = itemType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getItemType() {
        return itemType;
    }

    /**
     * Get logo resource name (to be created in drawable)
     */
    public String getLogoResourceName() {
        return "ic_provider_" + name().toLowerCase();
    }
}
