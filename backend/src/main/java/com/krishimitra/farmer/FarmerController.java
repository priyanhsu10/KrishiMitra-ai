package com.krishimitra.farmer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FarmerController {

    private final FarmerRepository farmerRepository;

    /**
     * Create or update farmer profile.
     * POST /api/v1/farmers
     */
    @PostMapping("/farmers")
    public ResponseEntity<?> createOrUpdateFarmer(@RequestBody Map<String, Object> request) {
        // Mobile sends: { name, language, village?, state? }
        // authToken has the farmer's mobile → we look up by the authenticated farmer_id
        // For hackathon, we accept farmer_id from a header or request body
        String name = (String) request.get("name");
        String language = (String) request.get("language");
        String village = (String) request.get("village");
        String state = (String) request.get("state");

        log.info("Creating/updating farmer: name={}, language={}, village={}", name, language, village);

        // In production, extract farmer_id from JWT token.
        // For hackathon, accept farmer_id directly since it's returned by /auth/verify.
        UUID farmerId = UUID.fromString((String) request.get("farmer_id"));

        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found: " + farmerId));

        if (name != null) farmer.setName(name);
        if (language != null) farmer.setLanguage(language);
        if (village != null) farmer.setVillage(village);
        if (state != null) farmer.setState(state);

        farmer = farmerRepository.save(farmer);

        return ResponseEntity.ok(Map.of(
            "id", farmer.getId(),
            "mobile", farmer.getMobile(),
            "name", farmer.getName(),
            "language", farmer.getLanguage(),
            "village", farmer.getVillage(),
            "state", farmer.getState(),
            "saved", true
        ));
    }

    /**
     * Get farmer profile with farms.
     * GET /api/v1/farmers/{id}
     */
    @GetMapping("/farmers/{id}")
    public ResponseEntity<?> getFarmer(@PathVariable UUID id) {
        log.info("Fetching farmer: id={}", id);

        Optional<Farmer> farmerOpt = farmerRepository.findById(id);
        if (farmerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Farmer farmer = farmerOpt.get();
        return ResponseEntity.ok(Map.of(
            "id", farmer.getId(),
            "mobile", farmer.getMobile(),
            "name", farmer.getName(),
            "language", farmer.getLanguage(),
            "village", farmer.getVillage(),
            "state", farmer.getState(),
            "fcm_token", farmer.getFcmToken() != null ? farmer.getFcmToken() : ""
        ));
    }

    /**
     * Update FCM token for push notifications.
     * PATCH /api/v1/farmers/{id}/fcm-token
     */
    @PatchMapping("/farmers/{id}/fcm-token")
    public ResponseEntity<?> updateFcmToken(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        String fcmToken = request.get("fcm_token");
        log.info("Updating FCM token for farmer {}: tokenLength={}", id, fcmToken != null ? fcmToken.length() : 0);

        Farmer farmer = farmerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Farmer not found: " + id));

        farmer.setFcmToken(fcmToken);
        farmerRepository.save(farmer);

        return ResponseEntity.ok(Map.of("success", true));
    }
}
