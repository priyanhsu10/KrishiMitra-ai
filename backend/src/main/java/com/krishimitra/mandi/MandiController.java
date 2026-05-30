package com.krishimitra.mandi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Mandi (market) prices controller.
 * Proxies requests to Python AI /ai/mandi endpoint.
 *
 * GET /api/v1/mandi?crop=soybean&state=Maharashtra
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MandiController {

    private final MandiService mandiService;

    @GetMapping("/mandi/prices")
    public ResponseEntity<?> getMandiPrices(
            @RequestParam("crop") String crop,
            @RequestParam(value = "state", defaultValue = "Maharashtra") String state,
            @RequestParam(value = "language", defaultValue = "mr") String language) {

        log.info("Mandi price request: crop={}, state={}, language={}", crop, state, language);

        try {
            Map<String, Object> result = mandiService.getMandiPrices(crop, state, language);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Mandi endpoint error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "crop", crop,
                "state", state,
                "prices", java.util.Collections.emptyList(),
                "advice_mr", "बाजार भाव उपलब्ध नाही.",
                "advice", "Information unavailable",
                "best_time_to_sell", "unknown"
            ));
        }
    }
}