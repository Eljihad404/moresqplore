package com.example.moresqplore.data.service;

import android.util.Log;

import com.example.moresqplore.data.model.Activity;
import com.example.moresqplore.data.model.DayPlan;
import com.example.moresqplore.data.model.Itinerary;
import com.example.moresqplore.Place;
import com.example.moresqplore.data.repository.GeminiChatRepository;
import com.example.moresqplore.data.model.ChatMessage;

import androidx.lifecycle.Observer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * AI-powered itinerary generation service using Gemini
 */
public class ItineraryService {

    private static final String TAG = "ItineraryService";
    private final GeminiChatRepository geminiRepository;

    public ItineraryService() {
        this.geminiRepository = GeminiChatRepository.getInstance();
    }

    /**
     * Generate itinerary using AI
     */
    public void generateItinerary(
            Itinerary itineraryRequest,
            List<Place> availablePlaces,
            OnItineraryGeneratedListener listener) {

        String prompt = buildPrompt(itineraryRequest, availablePlaces);

        Log.d(TAG, "Generating itinerary with AI...");

        // Observe conversation history for AI response
        Observer<List<ChatMessage>> observer = new Observer<List<ChatMessage>>() {
            @Override
            public void onChanged(List<ChatMessage> messages) {
                if (messages != null && !messages.isEmpty()) {
                    ChatMessage lastMessage = messages.get(messages.size() - 1);

                    // Check if it's an assistant response (not user message)
                    if (!lastMessage.isUser()) {
                        geminiRepository.getConversationHistory().removeObserver(this);

                        try {
                            String aiResponse = lastMessage.getContent();
                            Itinerary generatedItinerary = parseAIResponse(aiResponse, itineraryRequest);

                            // Calculate optimization score
                            double score = calculateOptimizationScore(generatedItinerary);
                            generatedItinerary.setOptimizationScore(score);

                            listener.onSuccess(generatedItinerary);
                            Log.d(TAG, "Itinerary generated successfully");

                        } catch (Exception e) {
                            listener.onFailure(e);
                            Log.e(TAG, "Error parsing AI response", e);
                        }
                    }
                }
            }
        };

        geminiRepository.getConversationHistory().observeForever(observer);

        // Send message to Gemini
        geminiRepository.sendMessage(prompt);
    }

    /**
     * Build AI prompt with user preferences and available places
     */
    private String buildPrompt(Itinerary request, List<Place> places) {
        StringBuilder placesJson = new StringBuilder("[");
        for (int i = 0; i < places.size(); i++) {
            Place p = places.get(i);
            placesJson.append(String.format(Locale.US,
                    "{\"name\":\"%s\",\"city\":\"%s\",\"category\":\"%s\"," +
                            "\"description\":\"%s\",\"cost\":%.0f,\"duration\":%d}",
                    p.getName(), p.getCity(), p.getCategory(),
                    p.getDescription(), p.getTicketPrice(), p.getEstimatedVisitDuration()));
            if (i < places.size() - 1)
                placesJson.append(",");
        }
        placesJson.append("]");

        return String.format(Locale.US,
                "You are an expert Morocco travel planner. Create a detailed %d-day itinerary.\n\n" +
                        "USER PREFERENCES:\n" +
                        "- Budget: %.0f MAD total\n" +
                        "- Starting City: %s\n" +
                        "- Interests: %s\n" +
                        "- Travel Style: %s\n\n" +
                        "AVAILABLE PLACES:\n%s\n\n" +
                        "REQUIREMENTS:\n" +
                        "1. Stay within budget (distribute evenly across days)\n" +
                        "2. Include 3-5 activities per day\n" +
                        "3. Mix activity types: visits, meals, experiences\n" +
                        "4. Consider travel time between locations\n" +
                        "5. Respect opening hours and durations\n\n" +
                        "OUTPUT FORMAT (JSON):\n" +
                        "{\n" +
                        "  \"days\": [\n" +
                        "    {\n" +
                        "      \"dayNumber\": 1,\n" +
                        "      \"city\": \"Marrakech\",\n" +
                        "      \"summary\": \"Explore the Red City\",\n" +
                        "      \"activities\": [\n" +
                        "        {\n" +
                        "          \"type\": \"visit\",\n" +
                        "          \"placeName\": \"Jardin Majorelle\",\n" +
                        "          \"startTime\": \"09:00\",\n" +
                        "          \"durationMinutes\": 120,\n" +
                        "          \"cost\": 70,\n" +
                        "          \"description\": \"Morning visit to beautiful gardens\"\n" +
                        "        }\n" +
                        "      ]\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}\n\n" +
                        "Generate the itinerary now:",
                request.getDurationDays(),
                request.getTotalBudget(),
                request.getStartingCity(),
                String.join(", ", request.getInterests()),
                request.getTravelStyle(),
                placesJson.toString());
    }

    /**
     * Parse AI response into Itinerary object
     */
    private Itinerary parseAIResponse(String aiResponse, Itinerary template) throws JSONException {
        // Extract JSON from markdown code blocks if present
        String jsonStr = aiResponse;
        if (jsonStr.contains("```json")) {
            jsonStr = jsonStr.substring(jsonStr.indexOf("```json") + 7);
            jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
        } else if (jsonStr.contains("```")) {
            jsonStr = jsonStr.substring(jsonStr.indexOf("```") + 3);
            jsonStr = jsonStr.substring(0, jsonStr.lastIndexOf("```"));
        }

        JSONObject json = new JSONObject(jsonStr.trim());
        JSONArray daysArray = json.getJSONArray("days");

        List<DayPlan> dayPlans = new ArrayList<>();
        double totalCost = 0;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        for (int i = 0; i < daysArray.length(); i++) {
            JSONObject dayJson = daysArray.getJSONObject(i);

            DayPlan dayPlan = new DayPlan();
            dayPlan.setDayNumber(dayJson.getInt("dayNumber"));
            dayPlan.setCity(dayJson.getString("city"));
            dayPlan.setSummary(dayJson.optString("summary", ""));
            dayPlan.setDate(dateFormat.format(calendar.getTime()));
            dayPlan.setDailyBudget(template.getTotalBudget() / template.getDurationDays());

            JSONArray activitiesArray = dayJson.getJSONArray("activities");
            List<Activity> activities = new ArrayList<>();

            for (int j = 0; j < activitiesArray.length(); j++) {
                JSONObject actJson = activitiesArray.getJSONObject(j);

                Activity activity = new Activity();
                activity.setActivityType(actJson.getString("type"));
                activity.setPlaceName(actJson.getString("placeName"));
                activity.setStartTime(actJson.getString("startTime"));
                activity.setDurationMinutes(actJson.getInt("durationMinutes"));
                activity.setEstimatedCost(actJson.getDouble("cost"));
                activity.setDescription(actJson.optString("description", ""));
                activity.setCity(dayPlan.getCity());

                activities.add(activity);
                totalCost += activity.getEstimatedCost();
            }

            dayPlan.setActivities(activities);
            dayPlans.add(dayPlan);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        template.setDayPlans(dayPlans);
        template.setEstimatedCost(totalCost);

        return template;
    }

    /**
     * Calculate optimization score (0-100)
     */
    private double calculateOptimizationScore(Itinerary itinerary) {
        double score = 100.0;

        // Budget adherence (30 points)
        double budgetUtilization = itinerary.getBudgetUtilization();
        if (budgetUtilization > 100) {
            score -= 30; // Over budget
        } else if (budgetUtilization < 70) {
            score -= (100 - budgetUtilization) * 0.2; // Under-utilized
        }

        // Activity distribution (30 points)
        int totalActivities = itinerary.getTotalActivities();
        int expectedActivities = itinerary.getDurationDays() * 4;
        double activityRatio = (double) totalActivities / expectedActivities;
        if (activityRatio < 0.75 || activityRatio > 1.25) {
            score -= 30;
        }

        // Day balance (20 points)
        for (DayPlan day : itinerary.getDayPlans()) {
            if (day.getActivityCount() < 3 || day.getActivityCount() > 6) {
                score -= 5;
            }
        }

        // Variety (20 points) - checked by activity types

        return Math.max(0, Math.min(100, score));
    }

    public interface OnItineraryGeneratedListener {
        void onSuccess(Itinerary itinerary);

        void onFailure(Exception e);
    }
}
