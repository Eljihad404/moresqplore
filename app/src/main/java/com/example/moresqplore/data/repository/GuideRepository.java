package com.example.moresqplore.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.data.model.Guide;
import com.example.moresqplore.data.model.GuideBooking;
import com.example.moresqplore.data.model.GuideReview;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository for managing guide marketplace data
 */
public class GuideRepository {

    private static volatile GuideRepository instance;

    private final FirebaseFirestore firestore;
    private final CollectionReference guidesRef;
    private final CollectionReference bookingsRef;
    private final CollectionReference reviewsRef;

    private final MutableLiveData<List<Guide>> guides = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    private GuideRepository() {
        this.firestore = FirebaseFirestore.getInstance();
        this.guidesRef = firestore.collection("guides");
        this.bookingsRef = firestore.collection("guide_bookings");
        this.reviewsRef = firestore.collection("guide_reviews");
    }

    public static GuideRepository getInstance() {
        if (instance == null) {
            synchronized (GuideRepository.class) {
                if (instance == null) {
                    instance = new GuideRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<Guide>> getGuides() {
        return guides;
    }

    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    /**
     * Fetch guides (uses mock data for MVP)
     */
    public void fetchGuides(String city, OnGuidesLoadedListener listener) {
        isLoading.postValue(true);

        // Generate mock guides
        List<Guide> mockGuides = generateMockGuides(city);
        guides.postValue(mockGuides);
        isLoading.postValue(false);

        if (listener != null) {
            listener.onSuccess(mockGuides);
        }
    }

    /**
     * Search guides by name or specialization
     */
    public void searchGuides(String query, OnGuidesLoadedListener listener) {
        List<Guide> allGuides = generateMockGuides(null);
        List<Guide> filtered = new ArrayList<>();

        String queryLower = query.toLowerCase();
        for (Guide guide : allGuides) {
            if (guide.getName().toLowerCase().contains(queryLower) ||
                    guide.getSpecializationsString().toLowerCase().contains(queryLower) ||
                    guide.getCity().toLowerCase().contains(queryLower)) {
                filtered.add(guide);
            }
        }

        guides.postValue(filtered);
        if (listener != null) {
            listener.onSuccess(filtered);
        }
    }

    /**
     * Create a booking
     */
    public void createBooking(GuideBooking booking, OnBookingListener listener) {
        bookingsRef.add(booking)
                .addOnSuccessListener(documentReference -> {
                    booking.setId(documentReference.getId());
                    if (listener != null) {
                        listener.onSuccess(booking);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Submit a review
     */
    public void submitReview(GuideReview review, OnReviewListener listener) {
        reviewsRef.add(review)
                .addOnSuccessListener(documentReference -> {
                    review.setId(documentReference.getId());
                    if (listener != null) {
                        listener.onSuccess(review);
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e);
                    }
                });
    }

    /**
     * Generate mock guides for MVP
     */
    private List<Guide> generateMockGuides(String filterCity) {
        List<Guide> mockGuides = new ArrayList<>();

        // Hassan - Marrakech Expert
        Guide hassan = new Guide("Hassan El Fassi", "Marrakech");
        hassan.setId("guide_1");
        hassan.setBio(
                "Passionate about sharing Marrakech's rich history and vibrant culture. Born and raised in the medina.");
        hassan.setLanguages(Arrays.asList("Arabic", "French", "English"));
        hassan.setSpecializations(Arrays.asList("City Tours", "Food Tours", "Historical Sites"));
        hassan.setHourlyRate(200);
        hassan.setDayRate(1200);
        hassan.setRating(4.9);
        hassan.setTotalReviews(127);
        hassan.setTotalBookings(245);
        hassan.setVerified(true);
        hassan.calculateBadge();
        mockGuides.add(hassan);

        // Fatima - Desert Guide
        Guide fatima = new Guide("Fatima Zahra", "Merzouga");
        fatima.setId("guide_2");
        fatima.setBio("Expert desert guide with 10+ years experience. Specializing in authentic Sahara experiences.");
        fatima.setLanguages(Arrays.asList("Arabic", "English", "Spanish"));
        fatima.setSpecializations(Arrays.asList("Desert Tours", "Camel Treks", "Stargazing"));
        fatima.setHourlyRate(0);
        fatima.setDayRate(1500);
        fatima.setRating(4.8);
        fatima.setTotalReviews(89);
        fatima.setTotalBookings(156);
        fatima.setVerified(true);
        fatima.calculateBadge();
        mockGuides.add(fatima);

        // Youssef - Food & Culture
        Guide youssef = new Guide("Youssef Bennani", "Marrakech");
        youssef.setId("guide_3");
        youssef.setBio("Chef and cultural ambassador. Let me show you the real flavors of Morocco!");
        youssef.setLanguages(Arrays.asList("Arabic", "French", "English", "Italian"));
        youssef.setSpecializations(Arrays.asList("Cooking Classes", "Market Tours", "Food Tours"));
        youssef.setHourlyRate(250);
        youssef.setDayRate(1400);
        youssef.setRating(5.0);
        youssef.setTotalReviews(45);
        youssef.setTotalBookings(78);
        youssef.setVerified(true);
        youssef.calculateBadge();
        mockGuides.add(youssef);

        // Amina - Fes Medina
        Guide amina = new Guide("Amina Idrissi", "Fes");
        amina.setId("guide_4");
        amina.setBio("Fes native with deep knowledge of artisan crafts and medieval architecture.");
        amina.setLanguages(Arrays.asList("Arabic", "French", "German"));
        amina.setSpecializations(Arrays.asList("Historical Tours", "Artisan Workshops", "Architecture"));
        amina.setHourlyRate(180);
        amina.setDayRate(1000);
        amina.setRating(4.7);
        amina.setTotalReviews(93);
        amina.setTotalBookings(167);
        amina.setVerified(true);
        amina.calculateBadge();
        mockGuides.add(amina);

        // Omar - Adventure Tours
        Guide omar = new Guide("Omar Aziz", "Agadir");
        omar.setId("guide_5");
        omar.setBio("Adventure enthusiast offering thrilling experiences along Morocco's coast and mountains.");
        omar.setLanguages(Arrays.asList("Arabic", "English", "French"));
        omar.setSpecializations(Arrays.asList("Hiking", "Surfing", "Quad Biking", "Adventure Sports"));
        omar.setHourlyRate(300);
        omar.setDayRate(1800);
        omar.setRating(4.9);
        omar.setTotalReviews(156);
        omar.setTotalBookings(289);
        omar.setVerified(true);
        omar.calculateBadge();
        mockGuides.add(omar);

        // Filter by city if specified
        if (filterCity != null && !filterCity.isEmpty()) {
            List<Guide> filtered = new ArrayList<>();
            for (Guide guide : mockGuides) {
                if (guide.getCity().equalsIgnoreCase(filterCity)) {
                    filtered.add(guide);
                }
            }
            return filtered;
        }

        return mockGuides;
    }

    // Callback interfaces
    public interface OnGuidesLoadedListener {
        void onSuccess(List<Guide> guides);

        void onFailure(Exception e);
    }

    public interface OnBookingListener {
        void onSuccess(GuideBooking booking);

        void onFailure(Exception e);
    }

    public interface OnReviewListener {
        void onSuccess(GuideReview review);

        void onFailure(Exception e);
    }
}
