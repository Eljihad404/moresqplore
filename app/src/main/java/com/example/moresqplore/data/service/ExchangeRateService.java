package com.example.moresqplore.data.service;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for fetching real-time exchange rates
 * Uses ExchangeRate-API.io (Free tier: 1500 requests/month)
 */
public class ExchangeRateService {

    private static final String TAG = "ExchangeRateService";

    // TODO: Replace with your API key from https://www.exchangerate-api.com/
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    // Cache rates for 24 hours to minimize API calls
    private Map<String, Double> cachedRates = new HashMap<>();
    private long lastUpdate = 0;
    private static final long CACHE_DURATION = 24 * 60 * 60 * 1000; // 24 hours

    // Fallback fixed rates (used if API fails)
    private static final Map<String, Double> FALLBACK_RATES = new HashMap<String, Double>() {
        {
            put("MAD", 1.0);
            put("USD", 10.0); // 1 USD = 10 MAD
            put("EUR", 11.0); // 1 EUR = 11 MAD
            put("GBP", 13.0); // 1 GBP = 13 MAD
        }
    };

    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ExchangeRateCallback {
        void onSuccess(Map<String, Double> rates);

        void onFailure(Exception e);
    }

    /**
     * Fetch latest exchange rates for MAD (Moroccan Dirham)
     */
    public void fetchRates(ExchangeRateCallback callback) {
        // Check cache first
        if (isCacheValid()) {
            Log.d(TAG, "Using cached exchange rates");
            mainHandler.post(() -> callback.onSuccess(cachedRates));
            return;
        }

        // Check if API key is configured
        if ("YOUR_API_KEY_HERE".equals(API_KEY)) {
            Log.w(TAG, "API key not configured, using fallback rates");
            cachedRates = new HashMap<>(FALLBACK_RATES);
            mainHandler.post(() -> callback.onSuccess(cachedRates));
            return;
        }

        // Fetch from API in background thread
        new Thread(() -> {
            try {
                Log.d(TAG, "Fetching exchange rates from API...");

                String urlString = BASE_URL + API_KEY + "/latest/MAD";
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    throw new Exception("API returned code: " + responseCode);
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                // Parse JSON response
                JSONObject json = new JSONObject(response.toString());

                // Check if request was successful
                String result = json.getString("result");
                if (!"success".equals(result)) {
                    throw new Exception("API request failed: " + result);
                }

                JSONObject rates = json.getJSONObject("conversion_rates");

                // Extract rates we need (convert to MAD base)
                Map<String, Double> rateMap = new HashMap<>();
                rateMap.put("MAD", 1.0);
                rateMap.put("USD", 1.0 / rates.getDouble("USD"));
                rateMap.put("EUR", 1.0 / rates.getDouble("EUR"));
                rateMap.put("GBP", 1.0 / rates.getDouble("GBP"));

                cachedRates = rateMap;
                lastUpdate = System.currentTimeMillis();

                Log.d(TAG, "Exchange rates updated successfully");
                Log.d(TAG, "1 USD = " + String.format("%.2f", rateMap.get("USD")) + " MAD");
                Log.d(TAG, "1 EUR = " + String.format("%.2f", rateMap.get("EUR")) + " MAD");
                Log.d(TAG, "1 GBP = " + String.format("%.2f", rateMap.get("GBP")) + " MAD");

                mainHandler.post(() -> callback.onSuccess(rateMap));

            } catch (Exception e) {
                Log.e(TAG, "Error fetching exchange rates: " + e.getMessage());

                // Use fallback rates
                cachedRates = new HashMap<>(FALLBACK_RATES);

                mainHandler.post(() -> {
                    Log.w(TAG, "Using fallback exchange rates");
                    callback.onSuccess(cachedRates);
                });
            }
        }).start();
    }

    /**
     * Check if cached rates are still valid
     */
    private boolean isCacheValid() {
        return !cachedRates.isEmpty() &&
                (System.currentTimeMillis() - lastUpdate) < CACHE_DURATION;
    }

    /**
     * Convert amount to MAD
     */
    public double convertToMAD(double amount, String fromCurrency) {
        Double rate = cachedRates.get(fromCurrency);
        if (rate == null) {
            // Use fallback rate
            rate = FALLBACK_RATES.getOrDefault(fromCurrency, 1.0);
        }
        return amount * rate;
    }

    /**
     * Convert amount from MAD to target currency
     */
    public double convertFromMAD(double amountInMAD, String targetCurrency) {
        Double rate = cachedRates.get(targetCurrency);
        if (rate == null) {
            // Use fallback rate
            rate = FALLBACK_RATES.getOrDefault(targetCurrency, 1.0);
        }
        return amountInMAD / rate;
    }

    /**
     * Get current exchange rate for a currency (to MAD)
     */
    public double getRate(String currency) {
        return cachedRates.getOrDefault(currency,
                FALLBACK_RATES.getOrDefault(currency, 1.0));
    }

    /**
     * Check if rates are loaded
     */
    public boolean hasRates() {
        return !cachedRates.isEmpty();
    }
}
