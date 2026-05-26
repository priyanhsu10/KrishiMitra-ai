package com.krishimitra.notification;

import com.google.firebase.messaging.*;
import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import com.krishimitra.advisory.Advisory;
import com.krishimitra.advisory.AdvisoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final FirebaseMessaging firebaseMessaging;
    private final AdvisoryRepository advisoryRepository;
    private final FarmerRepository farmerRepository;

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
            String fcmToken = farmerRepository.findById(req.getFarmerId())
                .map(Farmer::getFcmToken)
                .orElse(null);

            if (fcmToken != null && !fcmToken.isBlank()) {
                Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                        .setTitle("🌾 KrishiMitra — " + alertTypeLabel(req.getAlertType()))
                        .setBody(req.getMessageMr())   // always Marathi for push
                        .build())
                    .putData("alert_type", req.getAlertType())
                    .putData("priority", req.getPriority())
                    .putData("advisory_id", advisory.getId().toString())
                    .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(req.getPriority().equals("high")
                            ? AndroidConfig.Priority.HIGH
                            : AndroidConfig.Priority.NORMAL)
                        .build())
                    .build();

                String response = firebaseMessaging.send(message);
                log.info("FCM push sent successfully: messageId={}", response);
                
                fcmSent = true;
                advisory.setFcmSent(true);
                advisoryRepository.save(advisory);
            } else {
                log.warn("No FCM token for farmer {}", req.getFarmerId());
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

    private String alertTypeLabel(String alertType) {
        return switch (alertType) {
            case "weather"    -> "हवामान सूचना";
            case "disease"    -> "रोग सूचना";
            case "irrigation" -> "सिंचन वेळ";
            case "fertilizer" -> "खत सल्ला";
            case "market"     -> "बाजार भाव";
            default           -> "सल्ला";
        };
    }
}
