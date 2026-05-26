package com.krishimitra.advisory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AdvisoryController {

    private final AdvisoryService advisoryService;

    @PostMapping("/advisory/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, Object> request) {
        UUID farmerId = UUID.fromString((String) request.get("farmer_id"));
        String cropType = (String) request.get("crop_type");
        String stage = (String) request.get("stage");
        String language = (String) request.getOrDefault("language", "marathi");
        String question = (String) request.get("question");

        log.info("Advisory chat: farmerId={}, crop={}, language={}", farmerId, cropType, language);

        Map<String, Object> result = advisoryService.getAdvisory(farmerId, cropType, stage, language, question);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/advisories")
    public ResponseEntity<?> getAdvisories(
            @RequestParam("farmer_id") UUID farmerId,
            @RequestParam(value = "unread", defaultValue = "false") boolean unreadOnly) {

        log.info("Fetching advisories: farmerId={}, unreadOnly={}", farmerId, unreadOnly);

        List<Advisory> advisories;
        if (unreadOnly) {
            advisories = advisoryService.getUnreadAdvisories(farmerId);
        } else {
            advisories = advisoryService.getAllAdvisories(farmerId);
        }

        long unreadCount = advisoryService.getUnreadCount(farmerId);

        Map<String, Object> response = Map.of(
                "unread_count", unreadCount,
                "advisories", advisories
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/advisories/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable UUID id) {
        log.info("Marking advisory as read: id={}", id);
        advisoryService.markAsRead(id);
        return ResponseEntity.ok(Map.of("id", id, "is_read", true));
    }
}