package com.example.moresqplore;

import android.app.Application;
import android.util.Log;

import com.example.moresqplore.data.network.GeminiApiClient;

/**
 * Application class for Atlas Explorer app.
 * Initializes global components and SDKs on app startup.
 */
public class MoresQploreApplication extends Application {

    private static final String TAG = "AtlasExplorerApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Gemini API Client
        initializeGeminiClient();

        Log.d(TAG, "Atlas Explorer app initialized");
    }

    /**
     * Initializes the Gemini API client with the API key from BuildConfig.
     * The API key is loaded from local.properties during build time.
     */
    private void initializeGeminiClient() {
        String apiKey = com.example.moresqplore.BuildConfig.GEMINI_API_KEY;

        if (apiKey != null && !apiKey.isEmpty()) {
            GeminiApiClient.initialize(apiKey);
            Log.d(TAG, "Gemini API Client initialized successfully");
        } else {
            Log.w(TAG, "Gemini API key not found. Please add GEMINI_API_KEY to local.properties");
        }
    }
}