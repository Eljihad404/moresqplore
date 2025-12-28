// app/src/main/java/com/example/moresqplore/utils/MoroccoAIHelper.java
package com.example.moresqplore.utils;

import com.example.moresqplore.data.model.Place;
import java.util.Arrays;
import java.util.List;

/**
 * Helper class for generating Morocco-specific AI prompts.
 * Provides structured context about locations, transportation, and cultural information.
 */
public class MoroccoAIHelper {

    /**
     * Major Moroccan cities with their coordinates and characteristics.
     */
    public static class MoroccanCity {
        public final String name;
        public final double latitude;
        public final double longitude;
        public final String region;
        public final String description;

        public MoroccanCity(String name, double latitude, double longitude,
                            String region, String description) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.region = region;
            this.description = description;
        }
    }

    /**
     * Predefined cities with their information.
     */
    public static final List<MoroccanCity> CITIES = Arrays.asList(
            new MoroccanCity("Marrakech", 31.6295, -7.9811, "Marrakech-Safi",
                    "The Red City, known for its vibrant souks, stunning gardens, and historic medina"),
            new MoroccanCity("Fes", 34.0181, -5.0078, "Fès-Meknès",
                    "The cultural capital with the world's largest car-free medieval city"),
            new MoroccanCity("Casablanca", 33.5731, -7.5898, "Casablanca-Settat",
                    "Modern economic hub home to the magnificent Hassan II Mosque"),
            new MoroccanCity("Chefchaouen", 35.1689, -5.2636, "Tangier-Tetouan-Al Hoceima",
                    "The Blue Pearl, nestled in the Rif Mountains"),
            new MoroccanCity("Essaouira", 31.5125, -9.7700, "Marrakech-Safi",
                    "Charming coastal city with a fortified medina and excellent seafood"),
            new MoroccanCity("Rabat", 33.9731, -6.8375, "Rabat-Salé-Kénitra",
                    "The capital city with beautiful Islamic architecture"),
            new MoroccanCity("Meknes", 33.8935, -5.5472, "Fès-Meknès",
                    "Imperial city known for its grand palaces and ancient ruins"),
            new MoroccanCity("Ouarzazate", 30.9186, -6.9373, "Drâa-Tafilalet",
                    "Gateway to the Sahara and famous film studio location"),
            new MoroccanCity("Tangier", 35.7595, -5.8340, "Tangier-Tetouan-Al Hoceima",
                    "Strategic city at the entrance to the Mediterranean"),
            new MoroccanCity("Agadir", 30.4278, -9.5981, "Souss-Massa",
                    "Modern beach resort city on the Atlantic coast")
    );

    /**
     * Transportation options in Morocco.
     */
    public static class TransportInfo {
        public final String name;
        public final String description;
        public final double avgSpeedKmh;
        public final String typicalRoute;

        public TransportInfo(String name, String description, double avgSpeedKmh, String typicalRoute) {
            this.name = name;
            this.description = description;
            this.avgSpeedKmh = avgSpeedKmh;
            this.typicalRoute = typicalRoute;
        }
    }

    public static final List<TransportInfo> TRANSPORT_OPTIONS = Arrays.asList(
            new TransportInfo("ONCF Train",
                    "Comfortable and reliable train network connecting major cities",
                    100, "Casablanca-Rabat-Fes-Marrakech"),
            new TransportInfo("CTM Bus",
                    "Premium bus service with AC and comfortable seats",
                    75, "Inter-city routes nationwide"),
            new TransportInfo("Supratours",
                    "State-run bus company, affordable and reliable",
                    70, "Tourist routes between cities"),
            new TransportInfo("Grand Taxi",
                    "Shared taxis operating on fixed routes",
                    50, "Regional and intercity shared transport"),
            new TransportInfo("Petit Taxi",
                    "Metered taxis within cities",
                    30, "Urban transport in Casablanca, Rabat, Marrakech"),
            new TransportInfo("Rental Car",
                    "International agencies available at airports",
                    80, "Freedom to explore at own pace")
    );

    /**
     * Generates a comprehensive context prompt for the AI.
     */
    public static String generateContextPrompt() {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are Atlas, an expert guide for Morocco. Here is useful context:\n\n");

        prompt.append("Major Cities:\n");
        for (MoroccanCity city : CITIES) {
            prompt.append(String.format("- %s (%s): %s\n",
                    city.name, city.region, city.description));
        }

        prompt.append("\nTransportation Options:\n");
        for (TransportInfo transport : TRANSPORT_OPTIONS) {
            prompt.append(String.format("- %s: %s (Avg speed: %.0f km/h)\n",
                    transport.name, transport.description, transport.avgSpeedKmh));
        }

        prompt.append("\nUseful Tips:\n");
        prompt.append("- Currency: Moroccan Dirham (MAD), 1 USD ≈ 10 MAD\n");
        prompt.append("- Languages: Arabic, French, English widely spoken\n");
        prompt.append("- Best seasons: Spring (Mar-May) and Fall (Sep-Nov)\n");
        prompt.append("- Dress modestly when visiting religious sites\n");

        return prompt.toString();
    }

    /**
     * Gets a city by name.
     */
    public static MoroccanCity getCityByName(String name) {
        for (MoroccanCity city : CITIES) {
            if (city.name.equalsIgnoreCase(name)) {
                return city;
            }
        }
        return null;
    }

    /**
     * Calculates estimated travel time between cities.
     */
    public static int estimateTravelTimeHours(String fromCity, String toCity, String transportMode) {
        MoroccanCity from = getCityByName(fromCity);
        MoroccanCity to = getCityByName(toCity);

        if (from == null || to == null) {
            return -1;
        }

        // Calculate distance
        double lat1 = Math.toRadians(from.latitude);
        double lat2 = Math.toRadians(to.latitude);
        double lon1 = Math.toRadians(from.longitude);
        double lon2 = Math.toRadians(to.longitude);

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distanceKm = 6371 * c;

        // Get speed for transport mode
        double speed = 60; // Default
        for (TransportInfo transport : TRANSPORT_OPTIONS) {
            if (transport.name.toLowerCase().contains(
                    transportMode != null ? transportMode.toLowerCase() : "bus")) {
                speed = transport.avgSpeedKmh;
                break;
            }
        }

        return (int) Math.ceil(distanceKm / speed);
    }
}