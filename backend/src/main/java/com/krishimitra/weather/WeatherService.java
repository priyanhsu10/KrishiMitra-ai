package com.krishimitra.weather;

import com.krishimitra.crop.Crop;
import com.krishimitra.crop.CropRepository;
import com.krishimitra.farm.Farm;
import com.krishimitra.farm.FarmRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final WebClient aiServiceClient;

    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    /**
     * Full weather advisory flow with caching to prevent redundant AI service calls.
     */
    @Cacheable(value = "weather", key = "{#farmerId, #farmId, #language}", unless = "#result == null")
    public WeatherResponse getWeatherAdvisory(UUID farmerId, UUID farmId, String language) {

        // Step 1: Resolve farm → get coordinates
        Farm farm = resolveFarm(farmerId, farmId);
        double lat = farm.getLatitude().doubleValue();
        double lon = farm.getLongitude().doubleValue();

        log.info("Fetching weather for farmerId={}, lat={}, lon={}, lang={}", farmerId, lat, lon, language);

        // Step 2: Find most recent crop on this farm (best-effort, fallback to defaults)
        String cropType = "general";
        String stage = "vegetative";
        UUID resolvedCropId = null;

        List<Crop> crops = cropRepository.findByFarmId(farm.getId());
        if (!crops.isEmpty()) {
            Crop latestCrop = crops.get(crops.size() - 1);
            cropType = latestCrop.getCropType();
            stage = latestCrop.getStage();
            resolvedCropId = latestCrop.getId();
            log.debug("Using crop: type={}, stage={}, cropId={}", cropType, stage, resolvedCropId);
        } else {
            log.warn("No crops found for farm {}, using defaults", farm.getId());
        }

        // Step 3: Call Python AI weather endpoint
        // Must be effectively final for use inside the lambda
        final String finalCropType = cropType;
        final String finalStage    = stage;
        final String cropIdStr     = resolvedCropId != null ? resolvedCropId.toString() : "";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> aiResponse = aiServiceClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/ai/weather")
                    .queryParam("lat", lat)
                    .queryParam("lon", lon)
                    .queryParam("crop", finalCropType)
                    .queryParam("stage", finalStage)
                    .queryParam("lang", language)
                    .queryParam("farmer_id", farmerId.toString())
                    .queryParam("crop_id", cropIdStr)
                    .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (aiResponse == null) {
                log.error("Empty response from AI weather service");
                return buildFallbackResponse();
            }

            log.info("Weather advisory received: priority={}, notification_sent={}",
                aiResponse.get("priority"), aiResponse.get("notification_sent"));

            return mapAiResponseToWeatherResponse(aiResponse);

        } catch (WebClientResponseException e) {
            log.error("AI service error: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            return buildFallbackResponse();
        } catch (Exception e) {
            log.error("Weather advisory failed: {}", e.getMessage(), e);
            return buildFallbackResponse();
        }
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private Farm resolveFarm(UUID farmerId, UUID farmId) {
        if (farmId != null) {
            return farmRepository.findById(farmId)
                .orElseThrow(() -> new IllegalArgumentException("Farm not found: " + farmId));
        }
        // Fall back to first farm registered by this farmer
        List<Farm> farms = farmRepository.findByFarmerId(farmerId);
        if (farms.isEmpty()) {
            throw new IllegalArgumentException("No farms found for farmer: " + farmerId);
        }
        return farms.get(0);
    }

    @SuppressWarnings("unchecked")
    private WeatherResponse mapAiResponseToWeatherResponse(Map<String, Object> ai) {
        // AI service returns: weather_summary, advice, advice_mr, advice_en, temperature,
        //                     humidity, rainfall_mm, description, priority, notification_sent
        String weatherSummary = (String) ai.getOrDefault("weather_summary",
            "Weather data retrieved successfully");
        String advice = (String) ai.getOrDefault("advice", "");
        String adviceMr = (String) ai.getOrDefault("advice_mr", "");
        String adviceEn = (String) ai.getOrDefault("advice_en", "");
        String priority  = (String) ai.getOrDefault("priority", "medium");
        boolean notifSent = Boolean.TRUE.equals(ai.get("notification_sent"));

        Double temperature = toDouble(ai.get("temperature"));
        Integer humidity   = toInt(ai.get("humidity"));
        Double rainfall    = toDouble(ai.get("rainfall_mm"));
        String description = (String) ai.getOrDefault("description", "");

        return WeatherResponse.builder()
            .weatherSummary(weatherSummary)
            .advice(advice)
            .adviceMr(adviceMr)
            .adviceEn(adviceEn)
            .alertType("weather")
            .priority(priority)
            .temperature(temperature)
            .humidity(humidity)
            .rainfallMm(rainfall)
            .description(description)
            .notificationSent(notifSent)
            .build();
    }

    private WeatherResponse buildFallbackResponse() {
        return WeatherResponse.builder()
            .weatherSummary("Weather service temporarily unavailable")
            .adviceMr("हवामान सेवा तात्पुरती अनुपलब्ध आहे. कृपया पुन्हा प्रयत्न करा.")
            .adviceEn("Weather service temporarily unavailable. Please try again.")
            .alertType("weather")
            .priority("low")
            .temperature(null)
            .humidity(null)
            .rainfallMm(null)
            .notificationSent(false)
            .build();
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private Integer toInt(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }
}
