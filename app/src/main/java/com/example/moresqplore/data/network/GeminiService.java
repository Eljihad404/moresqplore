package com.example.moresqplore.data.network;
// app/src/main/java/com/example/moresqplore/data/network/GeminiService.java
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for Google Gemini API communication.
 * Supports both text-only and multimodal inputs for the travel assistant.
 */
public interface GeminiService {

    /**
     * Generates content using the Gemini Pro model.
     * This is the primary endpoint for text-based conversations about Morocco tourism.
     *
     * @param key    API key for authentication
     * @param request The generation request containing user message and context
     * @return Call object containing the generated response
     */
    @POST("v1beta/models/{model}:generateContent")
    Call<GeminiModels.GenerateContentResponse> generateContent(
            @Path("model") String model,
            @Query("key") String key,
            @Body GeminiModels.GenerateContentRequest request
    );

    /**
     * Generates content with streaming enabled for real-time responses.
     * Useful for showing partial responses as they are generated.
     */
    @POST("v1beta/models/{model}:streamGenerateContent")
    Call<retrofit2.Response<okhttp3.ResponseBody>> streamGenerateContent(
            @Path("model") String model,
            @Query("key") String key,
            @Body GeminiModels.GenerateContentRequest request
    );
}