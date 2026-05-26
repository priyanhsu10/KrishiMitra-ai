package com.krishimitra.disease;

import com.krishimitra.notification.NotificationService;
import com.krishimitra.notification.NotifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiseaseService {

    private final WebClient aiServiceClient;
    private final DiseaseReportRepository diseaseReportRepository;
    private final NotificationService notificationService;

    public Map<String, Object> detectAndSave(byte[] imageBytes, UUID farmerId, UUID cropId,
                                              String cropType, String language) {
        // Step 1: Forward to Python AI service
        Map<String, Object> aiResult = callAiService(imageBytes, farmerId, cropId, cropType, language);

        // Step 2: Save disease report
        DiseaseReport report = DiseaseReport.builder()
                .farmerId(farmerId)
                .cropId(cropId)
                .diagnosis((String) aiResult.get("disease"))
                .diagnosisMr((String) aiResult.get("disease_mr"))
                .confidence(new BigDecimal(String.valueOf(aiResult.get("confidence"))))
                .remedyEn((String) aiResult.get("remedy_en"))
                .remedyMr((String) aiResult.get("remedy_mr"))
                .severity((String) aiResult.get("severity"))
                .build();

        report = diseaseReportRepository.save(report);

        // Step 3: Trigger notification if high severity
        boolean notificationSent = false;
        String severity = (String) aiResult.get("severity");
        double confidence = Double.parseDouble(String.valueOf(aiResult.get("confidence")));

        if ("high".equals(severity) && confidence > 0.70) {
            notificationService.sendAlert(NotifyRequest.builder()
                    .farmerId(farmerId)
                    .cropId(cropId)
                    .alertType("disease")
                    .messageEn("Disease detected: " + aiResult.get("disease") + ". " + aiResult.get("remedy_en"))
                    .messageMr("रोग आढळला: " + aiResult.get("disease_mr") + ". " + aiResult.get("remedy_mr"))
                    .priority("high")
                    .build());
            notificationSent = true;
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("report_id", report.getId());
        response.put("disease", aiResult.get("disease"));
        response.put("disease_mr", aiResult.get("disease_mr"));
        response.put("confidence", aiResult.get("confidence"));
        response.put("severity", aiResult.get("severity"));
        response.put("remedy_en", aiResult.get("remedy_en"));
        response.put("remedy_mr", aiResult.get("remedy_mr"));
        response.put("notification_sent", notificationSent);
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callAiService(byte[] imageBytes, UUID farmerId, UUID cropId,
                                               String cropType, String language) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "leaf.jpg";
                }
            });
            body.add("crop_type", cropType);
            body.add("language", language);
            body.add("farmer_id", farmerId.toString());
            if (cropId != null) {
                body.add("crop_id", cropId.toString());
            }

            return aiServiceClient.post()
                    .uri("/ai/disease-detect")
                    .contentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(body))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

        } catch (Exception e) {
            log.error("AI service call failed: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("disease", "Analysis Unavailable");
            fallback.put("disease_mr", "विश्लेषण उपलब्ध नाही");
            fallback.put("confidence", 0.0);
            fallback.put("severity", "low");
            fallback.put("remedy_en", "Please try again later");
            fallback.put("remedy_mr", "कृपया नंतर प्रयत्न करा");
            return fallback;
        }
    }
}