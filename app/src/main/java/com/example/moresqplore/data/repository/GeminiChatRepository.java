package com.example.moresqplore.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moresqplore.BuildConfig;
import com.example.moresqplore.data.model.ChatMessage;
import com.example.moresqplore.data.model.Place;
import com.example.moresqplore.data.network.GeminiApiClient;
import com.example.moresqplore.data.network.GeminiModels;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Chat repository using Google Gemini API for AI-powered travel assistance.
 * Provides conversational interface for Morocco tourism guidance.
 *
 * Features:
 * - Conversation history management
 * - Context-aware responses (location, places, routes)
 * - Error handling with user-friendly messages
 * - System prompt optimization for Morocco tourism
 */
public class GeminiChatRepository {

    private static volatile GeminiChatRepository instance;

    private final MutableLiveData<List<ChatMessage>> conversationHistory =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isTyping = new MutableLiveData<>(false);

    private final Gson gson = new Gson();

    private String conversationId;
    private String currentLanguage = "en"; // Default language
    private final String modelId;

    // Moroccan cities and regions for context
    private static final List<String> MOROCCO_CITIES = List.of(
            "Marrakech", "Fes", "Casablanca", "Rabat", "Chefchaouen",
            "Tangier", "Agadir", "Ouarzazate", "Merzouga", "Zagora",
            "Essaouira", "Asilah", "Tetouan", "Meknes", "El Jadid"
    );

    private static final String SYSTEM_PROMPT =
            "You are Atlas, an expert and friendly AI tour guide specialized in Morocco tourism. " +
                    "Your mission is to provide accurate, helpful, and engaging travel advice that helps " +
                    "tourists discover the magic of Morocco. " +
                    "\n\nCORE KNOWLEDGE AREAS:\n" +
                    "1. MONUMENTS & ATTRACTIONS: Detailed knowledge of landmarks like Hassan II Mosque " +
                    "(Casablanca), Koutoubia Mosque, Bahia Palace, Madrassa Ben Youssef (Marrakech), " +
                    "Volubilis ruins, Chefchaouen's blue streets, Ait Benhaddou kasbah, " +
                    "and the ancient medinas of Fes and Marrakech.\n" +
                    "2. TRANSPORTATION: Expert on Morocco's transport including ONCF train system " +
                    "(high-speed Al Boraq, regional trains), CTM buses, grand taxis, petit taxis, " +
                    "car rentals, and domestic flights. Provide realistic travel times and costs.\n" +
                    "3. CULTURAL ETIQUETTE: Guide on appropriate dress codes for mosques and religious sites, " +
                    "tipping customs (10-15% in restaurants), greeting customs (handshakes, tea offerings), " +
                    "and respectful behavior in local communities.\n" +
                    "4. GASTRONOMY: Moroccan cuisine including tagines, couscous, pastilla, harira soup, " +
                    "mint tea traditions, street food recommendations, and dining etiquette.\n" +
                    "5. HIDDEN GEMS: Off-the-beaten-path destinations like the Draa Valley, " +
                    "Telouet Kasbah, Ouarzazate studios, Tafraout, and rural mountain villages.\n" +
                    "\nRESPONSE STYLE:\n" +
                    "- Be warm, knowledgeable, and culturally respectful\n" +
                    "- Provide specific details (prices in MAD, exact locations, phone numbers when known)\n" +
                    "- Include practical tips relevant to the user's query\n" +
                    "- Suggest specific alternatives when options are unavailable\n" +
                    "- Mention relevant local customs or warnings when applicable\n" +
                    "\nLANGUAGE: Always respond in the same language the user uses. " +
                    "If the user writes in French, respond in French. " +
                    "If in Arabic, respond in Arabic. Default to English only if language is unclear.\n" +
                    "\nIMPORTANT: If you don't know specific information (like exact today's prices), " +
                    "acknowledge this and suggest the user verify with current sources.";

    private static final int MAX_CONTEXT_MESSAGES = 15;
    private static final int MAX_OUTPUT_TOKENS = 1024;
    private static final double TEMPERATURE = 0.7f;
    private static final double TOP_P = 0.9f;
    private static final String DEFAULT_MODEL_ID = "gemini-1.5-flash-latest";

    private GeminiChatRepository() {
        this.conversationId = UUID.randomUUID().toString();
        this.modelId = resolveModelId();
    }

    /**
     * Returns the singleton instance of GeminiChatRepository.
     * Thread-safe implementation with double-checked locking.
     *
     * @return GeminiChatRepository instance
     */
    public static GeminiChatRepository getInstance() {
        if (instance == null) {
            synchronized (GeminiChatRepository.class) {
                if (instance == null) {
                    instance = new GeminiChatRepository();
                }
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance (useful for testing).
     */
    public static void resetInstance() {
        synchronized (GeminiChatRepository.class) {
            instance = null;
        }
    }

    private String resolveModelId() {
        String configModel = BuildConfig.GEMINI_MODEL_ID;
        if (configModel != null) {
            String trimmed = configModel.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return DEFAULT_MODEL_ID;
    }

    // ==================== LIVE DATA GETTERS ====================

    /**
     * Returns LiveData for observing conversation history.
     *
     * @return LiveData containing list of chat messages
     */
    public LiveData<List<ChatMessage>> getConversationHistory() {
        return conversationHistory;
    }

    /**
     * Returns LiveData for observing loading state.
     *
     * @return LiveData containing loading state boolean
     */
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }

    /**
     * Returns LiveData for observing typing indicator state.
     *
     * @return LiveData containing typing state boolean
     */
    public LiveData<Boolean> isTyping() {
        return isTyping;
    }

    /**
     * Returns LiveData for observing error messages.
     *
     * @return LiveData containing error message string
     */
    public LiveData<String> getError() {
        return error;
    }

    /**
     * Returns the current conversation ID.
     *
     * @return String containing conversation ID
     */
    public String getConversationId() {
        return conversationId;
    }

    // ==================== MESSAGE SENDING ====================

    /**
     * Sends a message to the Gemini API and processes the response.
     * This is the core method for all chat interactions.
     *
     * @param userMessage The message text from the user
     */
    public void sendMessage(String userMessage) {
        sendMessage(userMessage, null, null);
    }

    /**
     * Sends a message with context type.
     *
     * @param userMessage  The message text from the user
     * @param contextType  Type of context (e.g., "location", "places", "route")
     */
    public void sendMessage(String userMessage, String contextType) {
        sendMessage(userMessage, contextType, null);
    }

    /**
     * Sends a message to the Gemini API and processes the response.
     * This is the core method for all chat interactions.
     *
     * @param userMessage  The message text from the user
     * @param contextType  Type of context (e.g., "location", "places", "route")
     * @param contextData  Additional context data in JSON format
     */
    public void sendMessage(String userMessage, String contextType, String contextData) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            handleError("Message cannot be empty");
            return;
        }

        String trimmedMessage = userMessage.trim();

        // Add user message to conversation history
        List<ChatMessage> history = conversationHistory.getValue();
        if (history == null) {
            history = new ArrayList<>();
        }

        ChatMessage userMsg = ChatMessage.userMessage(trimmedMessage);
        userMsg.setConversationId(conversationId);
        userMsg.setContextType(contextType);
        userMsg.setContextData(contextData);
        history.add(userMsg);

        conversationHistory.postValue(new ArrayList<>(history));
        isLoading.postValue(true);
        isTyping.postValue(true);

        // Build the conversation context for Gemini
        List<GeminiModels.Content> contents = buildContents(history);

        // Create request
        GeminiModels.GenerateContentRequest request = createRequest(contents);

        // Make API call
        String apiKey = GeminiApiClient.getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            handleError("API key not configured. Please check your app settings.");
            isLoading.postValue(false);
            isTyping.postValue(false);
            return;
        }

        GeminiApiClient.getGeminiService().generateContent(modelId, apiKey, request)
                .enqueue(new Callback<GeminiModels.GenerateContentResponse>() {
                    @Override
                    public void onResponse(
                            Call<GeminiModels.GenerateContentResponse> call,
                            Response<GeminiModels.GenerateContentResponse> response) {
                        isLoading.postValue(false);
                        isTyping.postValue(false);

                        if (response == null) {
                            handleError("Empty response from server");
                            return;
                        }

                        if (response != null && response.isSuccessful() && response.body() != null) {
                            GeminiModels.GenerateContentResponse geminiResponse = response.body();
                            processResponse(geminiResponse);
                        } else {
                            handleUnsuccessfulResponse(response);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<GeminiModels.GenerateContentResponse> call,
                            Throwable t) {
                        isLoading.postValue(false);
                        isTyping.postValue(false);
                        handleError("Network error: " +
                                (t.getMessage() != null ? t.getMessage() : "Unknown error"));
                    }
                });
    }

    /**
     * Creates the API request with all necessary configurations.
     *
     * @param contents List of conversation contents
     * @return GenerateContentRequest configured request
     */
    private GeminiModels.GenerateContentRequest createRequest(List<GeminiModels.Content> contents) {
        GeminiModels.GenerateContentRequest request =
                new GeminiModels.GenerateContentRequest(contents);

        // Set system instruction
        GeminiModels.Content systemInstruction = GeminiModels.Content.model(SYSTEM_PROMPT);
        request.setSystemInstruction(systemInstruction);

        // Configure generation settings
        GeminiModels.GenerationConfig config = new GeminiModels.GenerationConfig();
        config.setMaxOutputTokens(MAX_OUTPUT_TOKENS);
        config.setTemperature(TEMPERATURE);
        config.setTopP(TOP_P);
        request.setGenerationConfig(config);

        // Add safety settings for travel content
        List<GeminiModels.SafetySetting> safetySettings = createSafetySettings();
        request.setSafetySettings(safetySettings);

        return request;
    }

    /**
     * Creates safety settings for the API request.
     *
     * @return List of SafetySetting objects
     */
    private List<GeminiModels.SafetySetting> createSafetySettings() {
        List<GeminiModels.SafetySetting> safetySettings = new ArrayList<>();

        safetySettings.add(new GeminiModels.SafetySetting(
                "HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE"));
        safetySettings.add(new GeminiModels.SafetySetting(
                "HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"));
        safetySettings.add(new GeminiModels.SafetySetting(
                "HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"));
        safetySettings.add(new GeminiModels.SafetySetting(
                "HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"));

        return safetySettings;
    }

    /**
     * Processes the API response and updates conversation history.
     *
     * @param geminiResponse The response from Gemini API
     */
    private void processResponse(GeminiModels.GenerateContentResponse geminiResponse) {
        if (geminiResponse.isSuccessful()) {
            String assistantResponse = geminiResponse.getFirstCandidateText();
            addAssistantMessage(assistantResponse);
        } else {
            handleApiError(geminiResponse.getError());
        }
    }

    private void handleUnsuccessfulResponse(Response<GeminiModels.GenerateContentResponse> response) {
        if (response == null) {
            handleError("Server error: empty response");
            return;
        }

        String fallbackMessage = "Server error: " + response.code() + " " + response.message();
        ResponseBody errorBody = response.errorBody();

        if (errorBody != null) {
            try {
                String errorPayload = errorBody.string();

                if (errorPayload != null && !errorPayload.isEmpty()) {
                    GeminiModels.GenerateContentResponse errorResponse =
                            gson.fromJson(errorPayload, GeminiModels.GenerateContentResponse.class);

                    if (errorResponse != null && errorResponse.getError() != null) {
                        handleApiError(errorResponse.getError());
                        return;
                    }

                    JsonObject root = JsonParser.parseString(errorPayload).getAsJsonObject();

                    if (root.has("error") && root.get("error").isJsonObject()) {
                        JsonObject err = root.getAsJsonObject("error");
                        GeminiModels.ApiError apiError = new GeminiModels.ApiError();

                        if (err.has("code") && !err.get("code").isJsonNull()) {
                            apiError.setCode(err.get("code").getAsInt());
                        }
                        if (err.has("message") && !err.get("message").isJsonNull()) {
                            apiError.setMessage(err.get("message").getAsString());
                        }
                        if (err.has("status") && !err.get("status").isJsonNull()) {
                            apiError.setStatus(err.get("status").getAsString());
                        }

                        handleApiError(apiError);
                        return;
                    }

                    if (root.has("message") && !root.get("message").isJsonNull()) {
                        fallbackMessage = root.get("message").getAsString();
                    }
                }
            } catch (IOException | IllegalStateException parseException) {
                fallbackMessage = fallbackMessage + " (" + parseException.getMessage() + ")";
            } finally {
                errorBody.close();
            }
        }

        handleError(fallbackMessage);
    }

    /**
     * Adds an assistant message to the conversation history.
     *
     * @param responseText The assistant's response text
     */
    private void addAssistantMessage(String responseText) {
        List<ChatMessage> updatedHistory = conversationHistory.getValue();
        if (updatedHistory == null) {
            updatedHistory = new ArrayList<>();
        }

        ChatMessage assistantMsg = ChatMessage.assistantMessage(responseText != null ? responseText :
                "I'm sorry, I couldn't generate a response. Please try again.");
        assistantMsg.setConversationId(conversationId);
        updatedHistory.add(assistantMsg);

        conversationHistory.postValue(new ArrayList<>(updatedHistory));
    }

    // ==================== LOCATION-AWARE METHODS ====================

    /**
     * Sends a message with user's current location context.
     * The AI will use this location to provide relevant recommendations.
     *
     * @param userMessage The user's message
     * @param latitude    Current latitude
     * @param longitude   Current longitude
     */
    public void sendMessageWithLocation(String userMessage, double latitude, double longitude) {
        String locationContext = String.format(
                "📍 Current Location Context: I am at coordinates (%.6f, %.6f) in Morocco. ",
                latitude, longitude
        );
        String fullMessage = locationContext + userMessage;

        String contextData = String.format(
                "{\"latitude\":%.6f,\"longitude\":%.6f}", latitude, longitude);

        sendMessage(fullMessage, "location", contextData);
    }

    /**
     * Finds nearby places and sends with location context.
     *
     * @param userMessage The user's message
     * @param latitude    Current latitude
     * @param longitude   Current longitude
     * @param radiusKm    Search radius in kilometers
     */
    public void sendMessageWithNearbyPlaces(
            String userMessage, double latitude, double longitude, double radiusKm) {

        isLoading.postValue(true);

        // Get nearby places from repository
        PlaceRepository placeRepository = PlaceRepository.getInstance();
        LiveData<List<Place>> nearbyPlacesLiveData =
                placeRepository.fetchNearbyPlaces(latitude, longitude, radiusKm);

        // Initialize observer as final variable
        final androidx.lifecycle.Observer<List<Place>>[] placesObserver =
                new androidx.lifecycle.Observer[]{null};

        // Create and assign the observer
        placesObserver[0] = places -> {
            nearbyPlacesLiveData.removeObserver(placesObserver[0]);
            isLoading.postValue(false);

            if (places != null && !places.isEmpty()) {
                sendMessageWithPlacesAndLocation(
                        userMessage, places, latitude, longitude);
            } else {
                // No nearby places found, send without place context
                sendMessageWithLocation(userMessage, latitude, longitude);
            }
        };

        // Observe and send when data is available
        nearbyPlacesLiveData.observeForever(placesObserver[0]);
    }
    /**
     * Sends message with nearby places and user location.
     *
     * @param userMessage The user's message
     * @param places      List of nearby places
     * @param latitude    User's latitude
     * @param longitude   User's longitude
     */
    private void sendMessageWithPlacesAndLocation(
            String userMessage, List<Place> places, double latitude, double longitude) {

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append(String.format(
                "📍 My current location: (%.6f, %.6f)\n", latitude, longitude));
        contextBuilder.append("🏛️ Nearby places I can visit:\n");

        for (int i = 0; i < places.size() && i < 10; i++) {
            Place p = places.get(i);
            contextBuilder.append(String.format(
                    "%d. %s - %s (%s) - %.1f km away\n",
                    i + 1, p.getName(), p.getCity(),
                    p.getCategory(), p.getDistanceFromUser()));
        }

        contextBuilder.append("\nQuestion: ").append(userMessage);

        String contextData = String.format(
                "{\"latitude\":%.6f,\"longitude\":%.6f,\"placesCount\":%d}",
                latitude, longitude, places.size());

        sendMessage(contextBuilder.toString(), "places_nearby", contextData);
    }

    // ==================== PLACE CONTEXT METHODS ====================

    /**
     * Sends a message with a list of place recommendations.
     *
     * @param userMessage The user's message
     * @param places      List of places to include in context
     */
    public void sendMessageWithPlaces(String userMessage, List<Place> places) {
        if (places == null || places.isEmpty()) {
            sendMessage(userMessage);
            return;
        }

        StringBuilder placeContext = new StringBuilder();
        placeContext.append("🏛️ Available places for consideration:\n");

        for (int i = 0; i < places.size(); i++) {
            Place p = places.get(i);
            placeContext.append(String.format(
                    "• %s (%s, %s) - Rating: %.1f\n",
                    p.getName(), p.getCity(), p.getCategory(),
                    p.getRating() != null ? p.getRating() : 0.0));
        }

        placeContext.append("\nMy question: ").append(userMessage);

        sendMessage(placeContext.toString(), "places", null);
    }

    /**
     * Gets details about a specific place and sends with context.
     *
     * @param userMessage The user's message
     * @param place       The place to get details about
     */
    public void sendMessageWithPlaceDetails(String userMessage, Place place) {
        if (place == null) {
            sendMessage(userMessage);
            return;
        }

        StringBuilder placeContext = new StringBuilder();
        placeContext.append("📍 Place Details:\n");
        placeContext.append(String.format("Name: %s\n", place.getName()));
        placeContext.append(String.format("City: %s\n", place.getCity()));
        placeContext.append(String.format("Category: %s\n", place.getCategory()));

        if (place.getLocation() != null) {
            placeContext.append(String.format("Location: (%.6f, %.6f)\n",
                    place.getLocation().getLatitude(),
                    place.getLocation().getLongitude()));
        }

        placeContext.append(String.format("Rating: %.1f (%d reviews)\n",
                place.getRating() != null ? place.getRating() : 0.0,
                place.getReviewCount() != null ? place.getReviewCount() : 0));

        placeContext.append(String.format("Opening Hours: %s\n",
                place.getOpeningHours() != null ? place.getOpeningHours() : "Not specified"));

        placeContext.append(String.format("Entry: %s\n",
                place.isFreeEntry() ? "Free" :
                        (place.getTicketPrice() != null ?
                                String.format("%.0f MAD", place.getTicketPrice()) : "Check website")));

        if (place.getDescription() != null) {
            placeContext.append(String.format("\nDescription: %s\n", place.getDescription()));
        }

        placeContext.append("\nQuestion: ").append(userMessage);

        sendMessage(placeContext.toString(), "place_details", place.getId());
    }

    // ==================== ROUTE PLANNING METHODS ====================

    /**
     * Builds a route planning prompt with current location and destination.
     *
     * @param userMessage      The user's message
     * @param currentLocation  Current place
     * @param destination      Destination place
     * @param preferredTransport Preferred mode of transport
     */
    public void sendRoutePlanningMessage(
            String userMessage, Place currentLocation, Place destination,
            String preferredTransport) {

        if (currentLocation == null || destination == null) {
            sendMessage(userMessage);
            return;
        }

        StringBuilder routeContext = new StringBuilder();
        routeContext.append("🗺️ Route Planning Request:\n\n");

        routeContext.append("📍 Starting Point:\n");
        routeContext.append(String.format("Place: %s\n", currentLocation.getName()));
        routeContext.append(String.format("City: %s\n", currentLocation.getCity()));
        if (currentLocation.getLocation() != null) {
            routeContext.append(String.format("Coordinates: (%.6f, %.6f)\n",
                    currentLocation.getLocation().getLatitude(),
                    currentLocation.getLocation().getLongitude()));
        }

        routeContext.append("\n🏁 Destination:\n");
        routeContext.append(String.format("Place: %s\n", destination.getName()));
        routeContext.append(String.format("City: %s\n", destination.getCity()));
        if (destination.getLocation() != null) {
            routeContext.append(String.format("Coordinates: (%.6f, %.6f)\n",
                    destination.getLocation().getLatitude(),
                    destination.getLocation().getLongitude()));
        }

        routeContext.append(String.format("\n🚗 Preferred Transport: %s\n",
                preferredTransport != null ? preferredTransport : "Most convenient option"));

        routeContext.append("\n📝 Additional Question: ").append(userMessage);

        // Calculate distance for context
        double distance = calculateDistance(
                currentLocation.getLocation().getLatitude(),
                currentLocation.getLocation().getLongitude(),
                destination.getLocation().getLatitude(),
                destination.getLocation().getLongitude());

        routeContext.append(String.format("\n(Approximate distance: %.1f km)", distance));

        sendMessage(routeContext.toString(), "route_planning", null);
    }

    /**
     * Gets transportation options between two cities.
     *
     * @param fromCity Origin city
     * @param toCity   Destination city
     * @param preferences Additional preferences or questions
     */
    public void getTransportationOptions(String fromCity, String toCity, String preferences) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("🚂 Transportation from %s to %s:\n\n", fromCity, toCity));

        message.append("Please provide detailed information about:\n");
        message.append("1. Train options (ONCF) - times, prices, duration\n");
        message.append("2. Bus options (CTM or other) - times, prices, duration\n");
        message.append("3. Grand taxi options - availability, shared vs private\n");
        message.append("4. Car rental recommendations if applicable\n");
        message.append("5. Domestic flight options if distance is great\n\n");

        if (preferences != null && !preferences.isEmpty()) {
            message.append("My preferences/questions:\n").append(preferences);
        }

        sendMessage(message.toString(), "transportation",
                String.format("{\"from\":\"%s\",\"to\":\"%s\"}", fromCity, toCity));
    }

    // ==================== QUICK ACTION METHODS ====================

    /**
     * Gets quick travel tips about Morocco.
     * Pre-defined message for common travel advice.
     */
    public void getQuickTipsMessage() {
        String message =
                "Give me 5 essential travel tips for Morocco covering:\n" +
                        "1. Transportation tips (trains, buses, taxis)\n" +
                        "2. Tipping customs and amounts\n" +
                        "3. Appropriate dress code\n" +
                        "4. Safety tips for tourists\n" +
                        "5. Must-know local customs\n\n" +
                        "Keep each tip concise but practical with specific advice.";

        sendMessage(message, "tips", null);
    }

    /**
     * Gets restaurant recommendations in a specific city.
     *
     * @param city       The city for recommendations
     * @param cuisineType Type of cuisine (optional)
     */
    public void getRestaurantRecommendations(String city, String cuisineType) {
        String message = String.format(
                "Recommend the best restaurants in %s for %s cuisine. " +
                        "Include:\n" +
                        "- Restaurant names and locations\n" +
                        "- Price range (budget, mid-range, fine dining)\n" +
                        "- Type of experience\n" +
                        "- Signature dishes to try\n" +
                        "- Reservation requirements if any",
                city,
                cuisineType != null ? cuisineType : "traditional Moroccan"
        );

        sendMessage(message, "restaurants", city);
    }

    /**
     * Gets cultural tips for visiting religious or historical sites.
     *
     * @param siteType Type of site (mosque, medina, museum, etc.)
     */
    public void getCulturalTips(String siteType) {
        String message = String.format(
                "What are the cultural etiquette and rules for visiting %s in Morocco? " +
                        "Include information about:\n" +
                        "- Appropriate dress code\n" +
                        "- Photography rules\n" +
                        "- Opening hours and prayer times\n" +
                        "- Expected behavior\n" +
                        "- Entry requirements for foreigners",
                siteType != null ? siteType : "religious and historical sites"
        );

        sendMessage(message, "cultural_tips", siteType);
    }

    /**
     * Gets a complete itinerary suggestion.
     *
     * @param city       Main city to explore
     * @param duration   Number of days
     * @param interests  User interests (shopping, culture, nature, etc.)
     */
    public void getItinerarySuggestion(String city, int duration, String interests) {
        String message = String.format(
                "Create a %d-day itinerary for %s focusing on %s. " +
                        "Include:\n" +
                        "- Day-by-day breakdown with specific attractions\n" +
                        "- Recommended times for visits\n" +
                        "- Meal suggestions with restaurant recommendations\n" +
                        "- Transportation between locations\n" +
                        "- Estimated costs per day",
                duration, city,
                interests != null ? interests : "general sightseeing"
        );

        sendMessage(message, "itinerary",
                String.format("{\"city\":\"%s\",\"days\":%d}", city, duration));
    }

    // ==================== CONVERSATION MANAGEMENT ====================

    /**
     * Builds the contents list for Gemini API from conversation history.
     * Limits to recent messages to stay within context window.
     *
     * @param history Full conversation history
     * @return List of Content objects for the API
     */
    private List<GeminiModels.Content> buildContents(List<ChatMessage> history) {
        List<GeminiModels.Content> contents = new ArrayList<>();

        // Include last N messages for context (Gemini has context window limits)
        int startIndex = Math.max(0, history.size() - MAX_CONTEXT_MESSAGES);

        for (int i = startIndex; i < history.size(); i++) {
            ChatMessage msg = history.get(i);
            String role = msg.isUser() ? "user" : "model";
            GeminiModels.Content content = new GeminiModels.Content(role, msg.getContent());
            contents.add(content);
        }

        return contents;
    }

    /**
     * Clears the conversation history and starts a new conversation.
     */
    public void clearConversation() {
        conversationId = UUID.randomUUID().toString();
        conversationHistory.postValue(new ArrayList<>());
        error.setValue(null);
    }

    /**
     * Sets custom conversation history (for restoring previous sessions).
     *
     * @param messages List of messages to restore
     */
    public void setConversationHistory(List<ChatMessage> messages) {
        conversationHistory.postValue(messages != null ? messages : new ArrayList<>());
    }

    /**
     * Gets a specific message from the conversation history.
     *
     * @param index Index of the message
     * @return ChatMessage at the index or null
     */
    public ChatMessage getMessageAt(int index) {
        List<ChatMessage> history = conversationHistory.getValue();
        if (history != null && index >= 0 && index < history.size()) {
            return history.get(index);
        }
        return null;
    }

    /**
     * Gets the number of messages in the conversation.
     *
     * @return Message count
     */
    public int getMessageCount() {
        List<ChatMessage> history = conversationHistory.getValue();
        return history != null ? history.size() : 0;
    }

    /**
     * Sets the language for responses.
     *
     * @param language Language code (e.g., "en", "fr", "ar")
     */
    public void setLanguage(String language) {
        if (language != null && !language.isEmpty()) {
            this.currentLanguage = language;
        }
    }

    /**
     * Gets the current language setting.
     *
     * @return Current language code
     */
    public String getLanguage() {
        return currentLanguage;
    }

    // ==================== ERROR HANDLING ====================

    /**
     * Handles API error responses.
     *
     * @param apiError The API error object
     */
    private void handleApiError(GeminiModels.ApiError apiError) {
        String errorMessage = "AI Service Error";

        if (apiError != null) {
            if (apiError.getMessage() != null) {
                errorMessage = apiError.getMessage();
            } else if (apiError.getStatus() != null) {
                errorMessage = "API Error: " + apiError.getStatus();
            }
        }

        handleError(errorMessage);
    }

    /**
     * Handles errors with user-friendly messages.
     * Also adds an error message to the conversation.
     *
     * @param errorMessage The error message
     */
    private void handleError(String errorMessage) {
        error.postValue(errorMessage);

        // Add fallback message to conversation
        List<ChatMessage> history = conversationHistory.getValue();
        if (history == null) {
            history = new ArrayList<>();
        }

        String userFriendlyMessage = getUserFriendlyErrorMessage(errorMessage);

        ChatMessage errorMsg = ChatMessage.assistantMessage(userFriendlyMessage);
        errorMsg.setConversationId(conversationId);
        history.add(errorMsg);
        conversationHistory.postValue(new ArrayList<>(history));
    }

    /**
     * Converts technical error messages to user-friendly versions.
     *
     * @param technicalError The technical error message
     * @return User-friendly error message
     */
    private String getUserFriendlyErrorMessage(String technicalError) {
        if (technicalError == null) {
            return "I'm sorry, something went wrong. Please try again.";
        }

        String lowerError = technicalError.toLowerCase();

        if (lowerError.contains("network") || lowerError.contains("connection")) {
            return "🌐 I'm having trouble connecting to the internet. " +
                    "Please check your connection and try again.";
        }

        if (lowerError.contains("rate limit") || lowerError.contains("quota")) {
            return "⏳ I'm receiving too many requests at the moment. " +
                    "Please wait a moment and try again.";
        }

        if (lowerError.contains("api key") || lowerError.contains("unauthorized") ||
            lowerError.contains("forbidden") || lowerError.contains("permission") ||
            lowerError.contains("denied")) {
            return "🔑 There's an authentication issue. " +
                    "Please restart the app or contact support.";
        }

        if (lowerError.contains("timeout") || lowerError.contains("deadline")) {
            return "⏱️ The request timed out. " +
                    "Please try again, and I'll respond as quickly as possible.";
        }

        if (lowerError.contains("not found") || lowerError.contains("does not exist") ||
            lowerError.contains("model")) {
            return "🧭 I'm having trouble reaching the AI model right now. " +
                "Please try again shortly.";
        }

        // Default user-friendly message
        return "😔 I'm sorry, I encountered an issue while processing your request. " +
                "Please try again or rephrase your question.";
    }

    /**
     * Clears the current error message.
     */
    public void clearError() {
        error.setValue(null);
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Calculates the distance between two points using the Haversine formula.
     *
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Checks if Morocco is mentioned in the message.
     *
     * @param message The message to check
     * @return true if Morocco is mentioned
     */
    public boolean isMoroccoRelated(String message) {
        if (message == null) {
            return false;
        }

        String lowerMessage = message.toLowerCase();

        // Check for direct Morocco mentions
        if (lowerMessage.contains("morocco") || lowerMessage.contains("maroc") ||
                lowerMessage.contains("المغرب")) {
            return true;
        }

        // Check for Moroccan cities
        for (String city : MOROCCO_CITIES) {
            if (lowerMessage.contains(city.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the suggested follow-up questions based on the last message.
     *
     * @return List of suggested questions
     */
    public List<String> getSuggestedFollowUps() {
        List<String> suggestions = new ArrayList<>();

        suggestions.add("What are the best places to visit nearby?");
        suggestions.add("How do I get there from my current location?");
        suggestions.add("What should I know about the dress code?");
        suggestions.add("Are there any good restaurants nearby?");
        suggestions.add("What are the opening hours?");

        return suggestions;
    }
}
