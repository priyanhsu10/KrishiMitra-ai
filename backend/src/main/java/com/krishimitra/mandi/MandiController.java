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

    @GetMapping("/mandi")
    public ResponseEntity<?> getMandiPrices(
            @RequestParam("crop") String crop,
            @RequestParam(value = "state", defaultValue = "Maharashtra") String state) {

        log.info("Mandi price request: crop={}, state={}", crop, state);

        try {
            Map<String, Object> result = mandiService.getMandiPrices(crop, state);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Mandi endpoint error: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "crop", crop,
                "state", state,
                "prices", java.util.Collections.emptyList(),
                "advice_mr", "बाजार भाव उपलब्ध नाही. पुन्हा प्रयत्न करा.",
                "best_time_to_sell", "unknown"
            ));
        }
    }
}