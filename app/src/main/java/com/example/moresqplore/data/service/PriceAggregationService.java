package com.example.moresqplore.data.service;

import com.example.moresqplore.data.model.BookingProvider;
import com.example.moresqplore.data.model.PriceComparison;
import com.example.moresqplore.data.model.PriceOffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Service for aggregating prices from multiple providers
 * MVP: Uses realistic mock data
 * Future: Will integrate with real booking APIs
 */
public class PriceAggregationService {

    private final Random random = new Random();

    /**
     * Generate mock price comparison for hotels
     */
    public PriceComparison generateHotelPrices(String hotelName, String city,
            String checkIn, String checkOut, int guests) {
        PriceComparison comparison = new PriceComparison(
                PriceComparison.TYPE_HOTEL, hotelName, city);
        comparison.setCheckInDate(checkIn);
        comparison.setCheckOutDate(checkOut);
        comparison.setGuests(guests);

        // Base price depends on city
        double basePrice = getBasePriceForCity(city);

        // Generate offers from hotel providers
        for (BookingProvider provider : BookingProvider.values()) {
            if (provider.getItemType().equals("HOTEL")) {
                PriceOffer offer = generateHotelOffer(provider, basePrice);
                comparison.addOffer(offer);
            }
        }

        return comparison;
    }

    /**
     * Generate mock price comparison for flights
     */
    public PriceComparison generateFlightPrices(String route, String date, int passengers) {
        PriceComparison comparison = new PriceComparison(
                PriceComparison.TYPE_FLIGHT, route, "Morocco");
        comparison.setCheckInDate(date);
        comparison.setGuests(passengers);

        // Base price depends on route
        double basePrice = getBasePriceForRoute(route);

        // Generate offers from flight providers
        for (BookingProvider provider : BookingProvider.values()) {
            if (provider.getItemType().equals("FLIGHT")) {
                PriceOffer offer = generateFlightOffer(provider, basePrice);
                comparison.addOffer(offer);
            }
        }

        return comparison;
    }

    /**
     * Generate mock price comparison for activities
     */
    public PriceComparison generateActivityPrices(String activityName, String city,
            String date, int participants) {
        PriceComparison comparison = new PriceComparison(
                PriceComparison.TYPE_ACTIVITY, activityName, city);
        comparison.setCheckInDate(date);
        comparison.setGuests(participants);

        // Base price depends on activity type
        double basePrice = getBasePriceForActivity(activityName);

        // Generate offers from activity providers
        for (BookingProvider provider : BookingProvider.values()) {
            if (provider.getItemType().equals("ACTIVITY")) {
                PriceOffer offer = generateActivityOffer(provider, basePrice);
                comparison.addOffer(offer);
            }
        }

        return comparison;
    }

    /**
     * Generate a hotel offer with realistic variation
     */
    private PriceOffer generateHotelOffer(BookingProvider provider, double basePrice) {
        // Add provider-specific variation (-15% to +20%)
        double variation = 0.85 + (random.nextDouble() * 0.35);
        double price = basePrice * variation;

        PriceOffer offer = new PriceOffer(
                provider.name(),
                provider.getDisplayName(),
                Math.round(price));

        // Set rating (3.5 to 5.0)
        offer.setRating(3.5 + (random.nextDouble() * 1.5));
        offer.setReviewCount(100 + random.nextInt(900));

        // Generate booking URL
        offer.setBookingUrl(provider.getBaseUrl() + "/morocco");

        // Random special offers
        if (random.nextDouble() < 0.3) {
            String[] offers = {
                    "Free cancellation",
                    "10% off",
                    "Breakfast included",
                    "No prepayment"
            };
            offer.setSpecialOffer(offers[random.nextInt(offers.length)]);
        }

        return offer;
    }

    /**
     * Generate a flight offer with realistic variation
     */
    private PriceOffer generateFlightOffer(BookingProvider provider, double basePrice) {
        // Flights have less variation (-10% to +15%)
        double variation = 0.90 + (random.nextDouble() * 0.25);
        double price = basePrice * variation;

        PriceOffer offer = new PriceOffer(
                provider.name(),
                provider.getDisplayName(),
                Math.round(price));

        // Airlines have higher ratings
        offer.setRating(4.0 + (random.nextDouble() * 1.0));
        offer.setReviewCount(500 + random.nextInt(1500));
        offer.setBookingUrl(provider.getBaseUrl());

        // Random special offers
        if (random.nextDouble() < 0.2) {
            String[] offers = {
                    "Extra baggage",
                    "Flexible dates",
                    "Seat selection included"
            };
            offer.setSpecialOffer(offers[random.nextInt(offers.length)]);
        }

        return offer;
    }

    /**
     * Generate an activity offer with realistic variation
     */
    private PriceOffer generateActivityOffer(BookingProvider provider, double basePrice) {
        // Activities have more variation (-20% to +30%)
        double variation = 0.80 + (random.nextDouble() * 0.50);
        double price = basePrice * variation;

        PriceOffer offer = new PriceOffer(
                provider.name(),
                provider.getDisplayName(),
                Math.round(price));

        offer.setRating(4.0 + (random.nextDouble() * 1.0));
        offer.setReviewCount(50 + random.nextInt(450));
        offer.setBookingUrl(provider.getBaseUrl() + "/morocco");

        // More frequent special offers for activities
        if (random.nextDouble() < 0.4) {
            String[] offers = {
                    "Skip the line",
                    "Free cancellation",
                    "Small group",
                    "Hotel pickup included"
            };
            offer.setSpecialOffer(offers[random.nextInt(offers.length)]);
        }

        return offer;
    }

    /**
     * Get base hotel price for city (per night)
     */
    private double getBasePriceForCity(String city) {
        switch (city.toLowerCase()) {
            case "marrakech":
                return 800;
            case "casablanca":
                return 700;
            case "fes":
            case "fès":
                return 550;
            case "chefchaouen":
                return 450;
            case "essaouira":
                return 600;
            case "agadir":
                return 750;
            case "rabat":
                return 650;
            default:
                return 500;
        }
    }

    /**
     * Get base flight price for route
     */
    private double getBasePriceForRoute(String route) {
        String routeLower = route.toLowerCase();
        if (routeLower.contains("casablanca") && routeLower.contains("marrakech")) {
            return 600;
        } else if (routeLower.contains("casablanca") && routeLower.contains("fes")) {
            return 700;
        } else if (routeLower.contains("tangier") && routeLower.contains("marrakech")) {
            return 850;
        } else if (routeLower.contains("agadir")) {
            return 900;
        }
        return 750; // Default domestic flight
    }

    /**
     * Get base activity price
     */
    private double getBasePriceForActivity(String activityName) {
        String activityLower = activityName.toLowerCase();
        if (activityLower.contains("desert") || activityLower.contains("sahara")) {
            return 950;
        } else if (activityLower.contains("tour") && activityLower.contains("day")) {
            return 350;
        } else if (activityLower.contains("cooking") || activityLower.contains("class")) {
            return 550;
        } else if (activityLower.contains("camel")) {
            return 400;
        } else if (activityLower.contains("quad") || activityLower.contains("atv")) {
            return 650;
        }
        return 300; // Default activity
    }

    /**
     * Sort offers by price (ascending)
     */
    public void sortByPrice(PriceComparison comparison) {
        if (comparison.getOffers() == null)
            return;
        Collections.sort(comparison.getOffers(),
                Comparator.comparingDouble(PriceOffer::getPrice));
    }

    /**
     * Sort offers by rating (descending)
     */
    public void sortByRating(PriceComparison comparison) {
        if (comparison.getOffers() == null)
            return;
        Collections.sort(comparison.getOffers(),
                (o1, o2) -> Double.compare(o2.getRating(), o1.getRating()));
    }

    /**
     * Sort offers by value (best price/rating ratio)
     */
    public void sortByValue(PriceComparison comparison) {
        if (comparison.getOffers() == null)
            return;
        Collections.sort(comparison.getOffers(),
                (o1, o2) -> {
                    double value1 = o1.getRating() / (o1.getPrice() / 100);
                    double value2 = o2.getRating() / (o2.getPrice() / 100);
                    return Double.compare(value2, value1);
                });
    }

    /**
     * Filter offers by price range
     */
    public List<PriceOffer> filterByPriceRange(PriceComparison comparison,
            double minPrice, double maxPrice) {
        List<PriceOffer> filtered = new ArrayList<>();
        if (comparison.getOffers() == null)
            return filtered;

        for (PriceOffer offer : comparison.getOffers()) {
            if (offer.getPrice() >= minPrice && offer.getPrice() <= maxPrice) {
                filtered.add(offer);
            }
        }
        return filtered;
    }

    /**
     * Filter offers by minimum rating
     */
    public List<PriceOffer> filterByRating(PriceComparison comparison, double minRating) {
        List<PriceOffer> filtered = new ArrayList<>();
        if (comparison.getOffers() == null)
            return filtered;

        for (PriceOffer offer : comparison.getOffers()) {
            if (offer.getRating() >= minRating) {
                filtered.add(offer);
            }
        }
        return filtered;
    }
}
