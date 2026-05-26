package com.krishimitra.advisory;

import com.krishimitra.notification.NotificationService;
import com.krishimitra.notification.NotifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdvisoryService {

    private final WebClient aiServiceClient;
    private final AdvisoryRepository advisoryRepository;
    private final NotificationService notificationService;

    @SuppressWarnings("unchecked")
    public Map<String, Object> getAdvisory(UUID farmerId, String cropType, String stage,
                                            String language, String question) {
        // Step 1: Call Python AI service
        Map<String, Object> aiResult;
        try {
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("farmer_id", farmerId.toString());
            aiRequest.put("crop_type", cropType);
            aiRequest.put("stage", stage);
            aiRequest.put("language", language);
            aiRequest.put("question", question);

            aiResult = aiServiceClient.post()
                    .uri("/ai/advisory")
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            log.error("AI advisory call failed: {}", e.getMessage());
            return Map.of(
                    "advice", "Service temporarily unavailable. Please try again.",
                    "alert_type", "general",
                    "priority", "low",
                    "notification_sent", false
            );
        }

        // Step 2: Save as advisory record
        String alertType = (String) aiResult.getOrDefault("alert_type", "general");
        String priority = (String) aiResult.getOrDefault("priority", "low");
        String advice = (String) aiResult.get("advice");

        Advisory advisory = Advisory.builder()
                .farmerId(farmerId)
                .alertType(alertType)
                .messageEn(advice)
                .messageMr(advice)
                .priority(priority)
                .isRead(false)
                .fcmSent(false)
                .build();
        advisoryRepository.save(advisory);

        // Step 3: Notify if high priority
        boolean notificationSent = false;
        if ("high".equals(priority)) {
            notificationService.sendAlert(NotifyRequest.builder()
                    .farmerId(farmerId)
                    .alertType(alertType)
                    .messageEn(advice)
                    .messageMr(advice)
                    .priority("high")
                    .build());
            notificationSent = true;
        }

        Map<String, Object> response = new HashMap<>(aiResult);
        response.put("notification_sent", notificationSent);
        return response;
    }

    public List<Advisory> getAllAdvisories(UUID farmerId) {
        return advisoryRepository.findByFarmerIdOrderByCreatedAtDesc(farmerId);
    }

    public List<Advisory> getUnreadAdvisories(UUID farmerId) {
        return advisoryRepository.findByFarmerIdAndIsReadOrderByCreatedAtDesc(farmerId, false);
    }

    public long getUnreadCount(UUID farmerId) {
        return advisoryRepository.countByFarmerIdAndIsRead(farmerId, false);
    }

    public void markAsRead(UUID advisoryId) {
        advisoryRepository.findById(advisoryId).ifPresent(advisory -> {
            advisory.setIsRead(true);
            advisoryRepository.save(advisory);
            log.info("Advisory marked as read: id={}", advisoryId);
        });
    }
}