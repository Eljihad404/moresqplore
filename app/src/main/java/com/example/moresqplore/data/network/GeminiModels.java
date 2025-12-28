// app/src/main/java/com/example/moresqplore/data/network/GeminiModels.java
package com.example.moresqplore.data.network;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Request and response models for Google Gemini API.
 * Implements the complete JSON structure required by Google's Generative Language API.
 */
public class GeminiModels {

    /**
     * Request body for content generation.
     * Contains the input content, generation configuration, and safety settings.
     */
    public static class GenerateContentRequest {
        @SerializedName("contents")
        private List<Content> contents;

        @SerializedName("generationConfig")
        private GenerationConfig generationConfig;

        @SerializedName("safetySettings")
        private List<SafetySetting> safetySettings;

        @SerializedName("systemInstruction")
        private Content systemInstruction;

        public GenerateContentRequest() {
            this.contents = new ArrayList<>();
            this.generationConfig = new GenerationConfig();
            this.safetySettings = new ArrayList<>();
        }

        public GenerateContentRequest(List<Content> contents) {
            this();
            this.contents = contents;
        }

        // Getters and Setters
        public List<Content> getContents() { return contents; }
        public void setContents(List<Content> contents) { this.contents = contents; }

        public GenerationConfig getGenerationConfig() { return generationConfig; }
        public void setGenerationConfig(GenerationConfig generationConfig) {
            this.generationConfig = generationConfig;
        }

        public List<SafetySetting> getSafetySettings() { return safetySettings; }
        public void setSafetySettings(List<SafetySetting> safetySettings) {
            this.safetySettings = safetySettings;
        }

        public Content getSystemInstruction() { return systemInstruction; }
        public void setSystemInstruction(Content systemInstruction) {
            this.systemInstruction = systemInstruction;
        }
    }

    /**
     * Represents a piece of content in the conversation.
     * Each content object has a role and a list of parts.
     */
    public static class Content {
        @SerializedName("role")
        private String role; // "user" or "model"

        @SerializedName("parts")
        private List<Part> parts;

        public Content() {
            this.parts = new ArrayList<>();
        }

        public Content(String role, String text) {
            this();
            this.role = role;
            this.parts.add(new Part(text));
        }

        public static Content user(String text) {
            return new Content("user", text);
        }

        public static Content model(String text) {
            return new Content("model", text);
        }

        // Getters and Setters
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public List<Part> getParts() { return parts; }
        public void setParts(List<Part> parts) { this.parts = parts; }

        public void addPart(Part part) {
            this.parts.add(part);
        }
    }

    /**
     * A part of the content, containing text or other media types.
     * For text-based conversations, this contains the text string.
     */
    public static class Part {
        @SerializedName("text")
        private String text;

        public Part() {}

        public Part(String text) {
            this.text = text;
        }

        public static Part text(String text) {
            return new Part(text);
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }

    /**
     * Configuration for content generation.
     * Controls output length, temperature, and other generation parameters.
     */
    public static class GenerationConfig {
        @SerializedName("candidateCount")
        private Integer candidateCount;

        @SerializedName("stopSequences")
        private List<String> stopSequences;

        @SerializedName("maxOutputTokens")
        private Integer maxOutputTokens;

        @SerializedName("temperature")
        private Double temperature;

        @SerializedName("topP")
        private Double topP;

        @SerializedName("topK")
        private Integer topK;

        public GenerationConfig() {
            this.candidateCount = 1;
            this.maxOutputTokens = 512;
            this.temperature = 0.7;
            this.topP = 0.9;
            this.topK = 40;
        }

        // Getters and Setters
        public Integer getCandidateCount() { return candidateCount; }
        public void setCandidateCount(Integer candidateCount) {
            this.candidateCount = candidateCount;
        }

        public List<String> getStopSequences() { return stopSequences; }
        public void setStopSequences(List<String> stopSequences) {
            this.stopSequences = stopSequences;
        }

        public Integer getMaxOutputTokens() { return maxOutputTokens; }
        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Double getTopP() { return topP; }
        public void setTopP(Double topP) { this.topP = topP; }

        public Integer getTopK() { return topK; }
        public void setTopK(Integer topK) { this.topK = topK; }
    }

    /**
     * Safety setting to filter inappropriate content.
     * Multiple categories can be configured with different thresholds.
     */
    public static class SafetySetting {
        @SerializedName("category")
        private String category;

        @SerializedName("threshold")
        private String threshold;

        public SafetySetting() {}

        public SafetySetting(String category, String threshold) {
            this.category = category;
            this.threshold = threshold;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getThreshold() { return threshold; }
        public void setThreshold(String threshold) { this.threshold = threshold; }
    }

    /**
     * Response from the Gemini API containing generated content.
     */
    public static class GenerateContentResponse {
        @SerializedName("error")
        private ApiError error;

        @SerializedName("candidates")
        private List<Candidate> candidates;

        public boolean isSuccessful() {
            return error == null && candidates != null && !candidates.isEmpty();
        }

        public String getFirstCandidateText() {
            if (candidates != null && !candidates.isEmpty()) {
                Candidate candidate = candidates.get(0);
                if (candidate.getContent() != null &&
                        candidate.getContent().getParts() != null &&
                        !candidate.getContent().getParts().isEmpty()) {
                    return candidate.getContent().getParts().get(0).getText();
                }
            }
            return "";
        }

        public ApiError getError() { return error; }
        public void setError(ApiError error) { this.error = error; }

        public List<Candidate> getCandidates() { return candidates; }
        public void setCandidates(List<Candidate> candidates) {
            this.candidates = candidates;
        }
    }

    /**
     * A generated response candidate from the model.
     */
    public static class Candidate {
        @SerializedName("content")
        private Content content;

        @SerializedName("finishReason")
        private String finishReason;

        @SerializedName("index")
        private Integer index;

        @SerializedName("safetyRatings")
        private List<SafetyRating> safetyRatings;

        public Content getContent() { return content; }
        public void setContent(Content content) { this.content = content; }

        public String getFinishReason() { return finishReason; }
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }

        public List<SafetyRating> getSafetyRatings() { return safetyRatings; }
        public void setSafetyRatings(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }
    }

    /**
     * Safety rating for generated content.
     */
    public static class SafetyRating {
        @SerializedName("category")
        private String category;

        @SerializedName("probability")
        private String probability;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getProbability() { return probability; }
        public void setProbability(String probability) {
            this.probability = probability;
        }
    }

    /**
     * API error details.
     */
    public static class ApiError {
        @SerializedName("code")
        private Integer code;

        @SerializedName("message")
        private String message;

        @SerializedName("status")
        private String status;

        public Integer getCode() { return code; }
        public void setCode(Integer code) { this.code = code; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}