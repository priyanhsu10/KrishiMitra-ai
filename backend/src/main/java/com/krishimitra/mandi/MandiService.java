package com.krishimitra.mandi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MandiService {

    private final WebClient aiServiceClient;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMandiPrices(String crop, String state, String language) {
        try {
            Map<String, Object> aiResponse = aiServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/ai/mandi")
                    .queryParam("crop", crop)
                    .queryParam("state", state)
                    .queryParam("lang", language)
                    .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (aiResponse == null) {
                return buildFallbackResponse(crop, state);
            }

            return aiResponse;

        } catch (Exception e) {
            log.error("Mandi AI service call failed: {}", e.getMessage());
            return buildFallbackResponse(crop, state);
        }
    }

    private Map<String, Object> buildFallbackResponse(String crop, String state) {
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("crop", crop);
        fallback.put("state", state);
        fallback.put("prices", Collections.emptyList());
        fallback.put("advice_mr", "बाजार भाव उपलब्ध नाही. पुन्हा प्रयत्न करा.");
        fallback.put("best_time_to_sell", "unknown");
        return fallback;
    }
}