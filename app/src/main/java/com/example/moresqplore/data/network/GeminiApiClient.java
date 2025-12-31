package com.example.moresqplore.data.network;

import com.example.moresqplore.BuildConfig;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.util.Log;

/**
 * Singleton API client for Google Gemini.
 * Provides centralized configuration and access to the Gemini service.
 */
public class GeminiApiClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/";
    private static final int CONNECT_TIMEOUT = 30;
    private static final int READ_TIMEOUT = 60;
    private static final int WRITE_TIMEOUT = 30;

    private static volatile Retrofit retrofit = null;
    private static volatile GeminiService geminiService = null;
    private static String apiKey = null;

    private GeminiApiClient() {
        // Private constructor prevents instantiation
    }

    /**
     * Initializes the Gemini API client with the provided API key.
     * This method should be called once during app startup.
     *
     * @param key The Gemini API key
     */
    public static synchronized void initialize(String key) {
        if (key != null && !key.isEmpty()) {
            apiKey = key;
        }
        Log.d("GeminiApiClient", "Gemini API Client initialized with key: " +
                (apiKey != null ? "****" + apiKey.substring(apiKey.length() - 4) : "null"));
    }

    /**
     * Returns the Retrofit instance for Gemini API.
     * Creates the instance if it doesn't exist.
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            synchronized (GeminiApiClient.class) {
                if (retrofit == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(createOkHttpClient())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    /**
     * Returns the Gemini service interface.
     * Creates the service if it doesn't exist.
     */
    public static GeminiService getGeminiService() {
        if (geminiService == null) {
            synchronized (GeminiApiClient.class) {
                if (geminiService == null) {
                    geminiService = getRetrofitInstance().create(GeminiService.class);
                }
            }
        }
        return geminiService;
    }

    /**
     * Returns the API key.
     * Priority: 1) Initialize method, 2) BuildConfig, 3) Fallback
     */
    public static String getApiKey() {
        String buildConfigKey = BuildConfig.GEMINI_API_KEY;
        Log.d("GeminiApiClient", "API Key from BuildConfig: " + (buildConfigKey != null && !buildConfigKey.isEmpty() ? "EXISTS" : "EMPTY"));

        if (buildConfigKey != null && !buildConfigKey.isEmpty()) {
            return buildConfigKey;
        }

        throw new IllegalStateException("API key not configured");
    }

    /**
     * Creates a configured OkHttpClient with logging and timeouts.
     */
    private static OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS);

        // Add logging interceptor for debug builds
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    /**
     * Clears the cached instances and API key.
     * Useful for testing or configuration changes.
     */
    @SuppressWarnings("unused")
    public static synchronized void reset() {
        synchronized (GeminiApiClient.class) {
            retrofit = null;
            geminiService = null;
            apiKey = null;
        }
    }
}