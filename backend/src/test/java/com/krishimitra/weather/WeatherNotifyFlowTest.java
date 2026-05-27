package com.krishimitra.weather;

import com.krishimitra.advisory.Advisory;
import com.krishimitra.advisory.AdvisoryRepository;
import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import com.krishimitra.notification.NotificationService;
import com.krishimitra.notification.NotifyRequest;
import com.krishimitra.notification.NotifyResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the POST /notify internal path:
 *   Python AI → Spring NotificationService → INSERT advisories → (optional FCM)
 *
 * Firebase is disabled in test profile (no credentials file),
 * so fcmSent will always be false — but advisory must always be saved.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherNotifyFlowTest {

    @Autowired NotificationService notificationService;
    @Autowired AdvisoryRepository advisoryRepository;
    @Autowired FarmerRepository farmerRepository;

    static UUID farmerId;

    @BeforeAll
    static void seedFarmer(@Autowired FarmerRepository repo) {
        Farmer f = Farmer.builder()
            .mobile("9000000001")
            .name("Test Farmer")
            .language("marathi")
            .build();
        farmerId = repo.save(f).getId();
    }

    // ── Test 1: Advisory always saved regardless of FCM ──────────────────────

    @Test
    @Order(1)
    @DisplayName("NOTIFY — advisory saved to DB even when Firebase is absent")
    void advisory_alwaysSavedToDb() {
        long before = advisoryRepository.count();

        NotifyRequest req = NotifyRequest.builder()
            .farmerId(farmerId)
            .alertType("weather")
            .messageEn("Rain 35mm expected. Delay fertilizer.")
            .messageMr("येत्या ४८ तासांत पाऊस. खत टाकणे टाळा.")
            .priority("high")
            .build();

        NotifyResponse resp = notificationService.sendAlert(req);

        assertThat(resp.isSaved()).isTrue();
        assertThat(resp.getAdvisoryId()).isNotNull();
        assertThat(advisoryRepository.count()).isEqualTo(before + 1);
    }

    // ── Test 2: Advisory fields persisted correctly ───────────────────────────

    @Test
    @Order(2)
    @DisplayName("NOTIFY — advisory persisted with correct fields")
    void advisory_fieldsCorrect() {
        NotifyRequest req = NotifyRequest.builder()
            .farmerId(farmerId)
            .alertType("weather")
            .messageEn("Extreme heat 45°C. Irrigate immediately.")
            .messageMr("अतिउष्णता ४५°C. ताबडतोब पाणी द्या.")
            .priority("high")
            .build();

        NotifyResponse resp = notificationService.sendAlert(req);

        Advisory saved = advisoryRepository.findById(resp.getAdvisoryId()).orElseThrow();
        assertThat(saved.getAlertType()).isEqualTo("weather");
        assertThat(saved.getMessageEn()).isEqualTo("Extreme heat 45°C. Irrigate immediately.");
        assertThat(saved.getMessageMr()).isEqualTo("अतिउष्णता ४५°C. ताबडतोब पाणी द्या.");
        assertThat(saved.getPriority()).isEqualTo("high");
        assertThat(saved.getIsRead()).isFalse();
        assertThat(saved.getFcmSent()).isFalse();  // no Firebase in test
        assertThat(saved.getFarmerId()).isEqualTo(farmerId);
    }

    // ── Test 3: Medium priority advisory also saved ───────────────────────────

    @Test
    @Order(3)
    @DisplayName("NOTIFY — medium priority advisory also saved (no push but polling picks it up)")
    void advisory_mediumPriority_saved() {
        NotifyRequest req = NotifyRequest.builder()
            .farmerId(farmerId)
            .alertType("weather")
            .messageEn("Light drizzle expected. Good time to apply fertilizer.")
            .messageMr("हलका पाऊस अपेक्षित. खत देण्यास चांगली वेळ.")
            .priority("medium")
            .build();

        NotifyResponse resp = notificationService.sendAlert(req);

        assertThat(resp.isSaved()).isTrue();
        Advisory saved = advisoryRepository.findById(resp.getAdvisoryId()).orElseThrow();
        assertThat(saved.getPriority()).isEqualTo("medium");
    }

    // ── Test 4: Farmer with no FCM token — advisory saved, fcmSent=false ─────

    @Test
    @Order(4)
    @DisplayName("NOTIFY — farmer without FCM token → advisory saved, fcmSent=false, no exception")
    void advisory_noFcmToken_savedGracefully() {
        Farmer noTokenFarmer = farmerRepository.save(
            Farmer.builder().mobile("9000000002").language("marathi").build()
        );

        NotifyRequest req = NotifyRequest.builder()
            .farmerId(noTokenFarmer.getId())
            .alertType("weather")
            .messageEn("Heavy rain coming.")
            .messageMr("जोरदार पाऊस येणार.")
            .priority("high")
            .build();

        NotifyResponse resp = notificationService.sendAlert(req);

        assertThat(resp.isSaved()).isTrue();
        assertThat(resp.isFcmSent()).isFalse();  // no token available
    }

    // ── Test 5: advisory accessible via GET /advisories polling ───────────────

    @Test
    @Order(5)
    @DisplayName("NOTIFY → POLL — saved advisory is found by unread polling query")
    void advisory_visibleToPolling() {
        // Send notification
        NotifyRequest req = NotifyRequest.builder()
            .farmerId(farmerId)
            .alertType("weather")
            .messageEn("Rain alert for polling test")
            .messageMr("पोलिंग चाचणीसाठी पाऊस सूचना")
            .priority("high")
            .build();
        NotifyResponse resp = notificationService.sendAlert(req);

        // Simulate polling: fetch unread advisories for this farmer
        List<Advisory> unread = advisoryRepository
            .findByFarmerIdAndIsReadOrderByCreatedAtDesc(farmerId, false);

        assertThat(unread).isNotEmpty();
        assertThat(unread.stream().anyMatch(a -> a.getId().equals(resp.getAdvisoryId()))).isTrue();
    }

    // ── Test 6: cropId optional (null is valid) ───────────────────────────────

    @Test
    @Order(6)
    @DisplayName("NOTIFY — cropId is optional (null allowed in weather alerts)")
    void advisory_cropIdNull_savedOk() {
        NotifyRequest req = NotifyRequest.builder()
            .farmerId(farmerId)
            .cropId(null)     // Weather alerts come before crop is resolved
            .alertType("weather")
            .messageEn("Weather alert without crop context.")
            .messageMr("पीक माहितीशिवाय हवामान सूचना.")
            .priority("high")
            .build();

        NotifyResponse resp = notificationService.sendAlert(req);
        assertThat(resp.isSaved()).isTrue();

        Advisory saved = advisoryRepository.findById(resp.getAdvisoryId()).orElseThrow();
        assertThat(saved.getCropId()).isNull();
    }
}
