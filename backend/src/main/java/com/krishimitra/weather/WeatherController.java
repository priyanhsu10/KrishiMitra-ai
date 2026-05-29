package com.krishimitra.weather;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * GET /api/v1/weather?farmer_id={uuid}[&farm_id={uuid}]
 * POST /api/v1/weather/schedule/run-now   ← manual trigger (hackathon demo)
 * GET  /api/v1/weather/schedule/status    ← last-run summary
 *
 * Flow:
 *   mobile ──▶ Spring Boot ──▶ farm lookup (lat/lon) ──▶ Python AI /ai/weather
 *                                                              │
 *                                               OpenWeatherMap + LLM advisory
 *                                                              │
 *                                          if rain>20mm → POST /notify → FCM push
 *                                                              │
 *   mobile ◀── { weather_summary, advice_mr, temperature, ... } ◀──
 */
@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherSchedulerService weatherSchedulerService;

    // ── farmer-requested weather ──────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(
            @RequestParam("farmer_id") UUID farmerId,
            @RequestParam(value = "farm_id", required = false) UUID farmId,
            @RequestParam(value = "language", defaultValue = "marathi") String language) {

        log.info("Weather request: farmerId={}, farmId={}, lang={}", farmerId, farmId, language);

        try {
            WeatherResponse response = weatherService.getWeatherAdvisory(farmerId, farmId, language);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Bad request for weather: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Weather endpoint error: {}", e.getMessage(), e);
            // Return degraded response instead of 500
            WeatherResponse fallback = WeatherResponse.builder()
                .weatherSummary("Weather service error")
                .adviceMr("हवामान माहिती मिळवण्यात अडचण आली. पुन्हा प्रयत्न करा.")
                .adviceEn("Failed to fetch weather data. Please try again.")
                .alertType("weather")
                .priority("low")
                .notificationSent(false)
                .build();
            return ResponseEntity.ok(fallback);
        }
    }

    // ── scheduler / demo endpoints ────────────────────────────────────────────

    /**
     * Manual trigger — runs the daily weather check NOW.
     * POST /api/v1/weather/schedule/run-now
     *
     * Use this for:
     *   • Hackathon demo (show 12-hr forecast → 7-day outlook instantly)
     *   • Testing   (simulate a scheduled run on demand)
     */
    @PostMapping("/schedule/run-now")
    public ResponseEntity<String> triggerSchedulerNow() {
        log.info("Manual scheduler trigger via API");
        String result = weatherSchedulerService.runWeatherCheckNow();
        return ResponseEntity.ok(result);
    }

    /**
     * Read-only status of the most recent scheduler run.
     * GET /api/v1/weather/schedule/status
     */
    @GetMapping("/schedule/status")
    public ResponseEntity<Map<String, Object>> schedulerStatus() {
        return ResponseEntity.ok(weatherSchedulerService.getLastRunStatus());
    }
}
