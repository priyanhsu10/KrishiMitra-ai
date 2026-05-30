package com.krishimitra.notification;

import com.google.firebase.messaging.*;
import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import com.krishimitra.advisory.Advisory;
import com.krishimitra.advisory.AdvisoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NotificationService {

    private final Optional<FirebaseMessaging> firebaseMessaging;
    private final AdvisoryRepository advisoryRepository;
    private final FarmerRepository farmerRepository;

    public NotificationService(Optional<FirebaseMessaging> firebaseMessaging,
                               AdvisoryRepository advisoryRepository,
                               FarmerRepository farmerRepository) {
        this.firebaseMessaging = firebaseMessaging;
        this.advisoryRepository = advisoryRepository;
        this.farmerRepository = farmerRepository;
    }

    @Transactional
    public NotifyResponse sendAlert(NotifyRequest req) {
        // Step 1: Always save advisory to DB
        Advisory advisory = Advisory.builder()
            .farmerId(req.getFarmerId())
            .cropId(req.getCropId())
            .alertType(req.getAlertType())
            .messageEn(req.getMessageEn())
            .messageMr(req.getMessageMr())
            .messageHi(req.getMessageHi())
            .priority(req.getPriority())
            .isRead(false)
            .fcmSent(false)
            .build();
        
        advisory = advisoryRepository.save(advisory);
        log.info("Advisory saved: id={}, type={}, priority={}", advisory.getId(), req.getAlertType(), req.getPriority());

        // Step 2: FCM push — best effort, never fail the whole request
        boolean fcmSent = false;
        try {
            Farmer farmer = farmerRepository.findById(req.getFarmerId()).orElse(null);
            if (farmer == null) {
                log.warn("Farmer NOT FOUND in database for ID: {}", req.getFarmerId());
                return NotifyResponse.builder().advisoryId(advisory.getId()).saved(true).fcmSent(false).build();
            }

            String fcmToken = farmer.getFcmToken();
            boolean hasToken = fcmToken != null && !fcmToken.isBlank();
            boolean hasFirebase = firebaseMessaging.isPresent();

            if (hasToken && hasFirebase) {
                String language = farmer.getLanguage() != null ? farmer.getLanguage() : "mr";
                String body = req.getMessageMr(); // default
                if ("en".equalsIgnoreCase(language) || "english".equalsIgnoreCase(language)) {
                    body = req.getMessageEn();
                } else if (("hi".equalsIgnoreCase(language) || "hindi".equalsIgnoreCase(language)) && req.getMessageHi() != null) {
                    body = req.getMessageHi();
                }

                Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                        .setTitle("🌾 KrishiMitra — " + alertTypeLabel(req.getAlertType(), language))
                        .setBody(body)
                        .build())
                    .putData("alert_type", req.getAlertType())
                    .putData("priority", req.getPriority())
                    .putData("advisory_id", advisory.getId().toString())
                    .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(req.getPriority().equalsIgnoreCase("high")
                            ? AndroidConfig.Priority.HIGH
                            : AndroidConfig.Priority.NORMAL)
                        .build())
                    .build();

                String response = firebaseMessaging.get().send(message);
                log.info("FCM push sent successfully for farmer {}: messageId={}", req.getFarmerId(), response);
                
                fcmSent = true;
                advisory.setFcmSent(true);
                advisoryRepository.save(advisory);
            } else {
                if (!hasToken) {
                    log.warn("FCM Token is missing/blank for farmer: {}", req.getFarmerId());
                }
                if (!hasFirebase) {
                    log.error("Firebase Messaging is NOT INITIALIZED. Check backend logs for startup errors.");
                }
            }
        } catch (Exception e) {
            // Don't throw — advisory is saved, in-app polling will catch it
            log.warn("FCM push failed for farmer {}: {}", req.getFarmerId(), e.getMessage());
        }

        return NotifyResponse.builder()
            .advisoryId(advisory.getId())
            .saved(true)
            .fcmSent(fcmSent)
            .build();
    }

    private String alertTypeLabel(String alertType, String language) {
        boolean isEnglish = "en".equalsIgnoreCase(language) || "english".equalsIgnoreCase(language);
        return switch (alertType) {
            case "weather"    -> isEnglish ? "Weather Alert" : "हवामान सूचना";
            case "disease"    -> isEnglish ? "Disease Alert" : "रोग सूचना";
            case "irrigation" -> isEnglish ? "Irrigation Time" : "सिंचन वेळ";
            case "fertilizer" -> isEnglish ? "Fertilizer Advice" : "खत सल्ला";
            case "market"     -> isEnglish ? "Market Price" : "बाजार भाव";
            default           -> isEnglish ? "Advisory" : "सल्ला";
        };
    }
}
