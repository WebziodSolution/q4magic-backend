package com.q4magic.common.locationExtractorService;

import com.q4magic.common.dto.LocationResultDto;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import java.util.*;

@Service("LocationExtractorService")
public class LocationExtractorService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private String getApiKey() {
        if (openAiApiKey != null && !openAiApiKey.trim().isEmpty()) {
            return openAiApiKey;
        }
        return null;
    }

    public LocationResultDto extractLocations(String text) {
        if (text == null || text.trim().isEmpty()) {
            LocationResultDto dto = new LocationResultDto();
            dto.setStatus("Not Found");
            dto.setExtractedLocations(new ArrayList<>());
            return dto;
        }

        String cleanedText = cleanHtml(text);
        Set<String> extracted = new HashSet<>();
        String apiKey = getApiKey();

        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                JSONObject body = new JSONObject();
                body.put("model", "gpt-4o-mini");

                JSONArray messages = new JSONArray();

                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "You are a precise location extraction assistant. Extract all physical locations, addresses, cities, states, countries, or landmarks from the provided text. Return ONLY a valid JSON object matching this schema: {\"locations\": [\"string\"]}. Do not include markdown code block formatting, explanation, or other text.");
                messages.put(systemMessage);

                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", cleanedText);
                messages.put(userMessage);

                body.put("messages", messages);

                JSONObject responseFormat = new JSONObject();
                responseFormat.put("type", "json_object");
                body.put("response_format", responseFormat);

                HttpResponse<String> response = Unirest.post("https://api.openai.com/v1/chat/completions")
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + apiKey)
                        .body(body.toString())
                        .asString();

                if (response.getStatus() == 200 && response.getBody() != null) {
                    JSONObject responseJson = new JSONObject(response.getBody());
                    JSONArray choices = responseJson.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject choice = choices.getJSONObject(0);
                        JSONObject message = choice.getJSONObject("message");
                        String content = message.getString("content");

                        JSONObject parsedContent = new JSONObject(content);
                        JSONArray locationsArray = parsedContent.getJSONArray("locations");
                        for (int i = 0; i < locationsArray.length(); i++) {
                            String loc = locationsArray.getString(i);
                            if (loc != null && !loc.trim().isEmpty()) {
                                extracted.add(loc.trim());
                            }
                        }
                    }
                } else {
                    System.err.println("OpenAI API call failed with status: " + response.getStatus() + " - " + response.getBody());
                }
            } catch (Exception e) {
                System.err.println("Error calling OpenAI API: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("OpenAI API Key is missing. Skipping location extraction.");
        }

        List<String> extractedList = new ArrayList<>(extracted);
        String status = extractedList.isEmpty() ? "Not Found" : "Location Found";
        LocationResultDto dto = new LocationResultDto();
        dto.setStatus(status);
        dto.setExtractedLocations(extractedList);
        return dto;
    }

    private String cleanHtml(String html) {
        if (html == null) {
            return "";
        }

        String text = html.replaceAll("(?i)</?(address|article|aside|blockquote|canvas|dd|div|dl|dt|fieldset|figcaption|figure|footer|form|h[1-6]|header|hr|li|main|nav|noscript|ol|p|pre|section|table|tfoot|ul|video|br)[^>]*>", " ");
        text = text.replaceAll("<[^>]*>", "");
        text = text.replaceAll("&nbsp;", " ")
                .replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"")
                .replaceAll("&apos;", "'");

        return text.replaceAll("\\s+", " ").trim();
    }
}
