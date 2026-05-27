package com.krishimitra.weather;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.krishimitra.advisory.Advisory;
import com.krishimitra.advisory.AdvisoryRepository;
import com.krishimitra.crop.Crop;
import com.krishimitra.crop.CropRepository;
import com.krishimitra.farm.Farm;
import com.krishimitra.farm.FarmRepository;
import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import com.github.tomakehurst.wiremock.client.WireMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end Weather Alert Flow test.
 *
 * Tests the full chain:
 *   mobile GET /api/v1/weather
 *     → Spring resolves farm lat/lon from DB
 *     → Spring calls WireMock stub (Python AI)
 *     → Python AI stub returns weather + advisory
 *     → Spring maps response and returns to mobile
 *
 * Notification path (POST /notify) is tested separately in WeatherNotifyFlowTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WeatherFlowE2ETest {

    // ── WireMock server (mocks Python AI service) ─────────────────────────────
    static WireMockServer wireMock = new WireMockServer(
        WireMockConfiguration.options().port(8089)
    );

    @DynamicPropertySource
    static void wireProperties(DynamicPropertyRegistry reg) {
        reg.add("ai.service.url", () -> "http://localhost:8089");
        reg.add("wiremock.port", () -> "8089");
    }

    @BeforeAll
    static void startWireMock() {
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void resetWireMock() {
        wireMock.resetAll();
    }

    // ── Spring / test infrastructure ──────────────────────────────────────────
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired FarmerRepository farmerRepository;
    @Autowired FarmRepository farmRepository;
    @Autowired CropRepository cropRepository;
    @Autowired AdvisoryRepository advisoryRepository;

    // Shared state for ordered tests
    static java.util.UUID farmerId;
    static java.util.UUID farmId;
    static java.util.UUID cropId;

    // ── Test 1: Setup seed data ───────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("SETUP — seed farmer, farm, crop in H2")
    void seedDatabase() {
        Farmer farmer = Farmer.builder()
            .mobile("9876543210")
            .name("Ramesh Patil")
            .language("marathi")
            .village("Pune Rural")
            .state("Maharashtra")
            .fcmToken("test-fcm-token-abc123")
            .build();
        farmer = farmerRepository.save(farmer);
        farmerId = farmer.getId();

        Farm farm = Farm.builder()
            .farmerId(farmerId)
            .name("Main Farm")
            .latitude(new BigDecimal("18.520000"))
            .longitude(new BigDecimal("73.850000"))
            .areaAcres(new BigDecimal("5.50"))
            .soilType("black")
            .build();
        farm = farmRepository.save(farm);
        farmId = farm.getId();

        Crop crop = Crop.builder()
            .farmId(farmId)
            .cropType("soybean")
            .sowingDate(LocalDate.of(2025, 6, 1))
            .stage("vegetative")
            .build();
        crop = cropRepository.save(crop);
        cropId = crop.getId();

        assertThat(farmerRepository.findById(farmerId)).isPresent();
        assertThat(farmRepository.findById(farmId)).isPresent();
        assertThat(cropRepository.findById(cropId)).isPresent();
    }

    // ── Test 2: Normal sunny day (no rain) — no notification ─────────────────

    @Test
    @Order(2)
    @DisplayName("E2E — sunny weather (rainfall 5mm) → response OK, notification_sent=false")
    void sunnyWeather_noNotification() throws Exception {
        stubAiWeatherResponse(5.0, 28.5, 65, "clear sky", "medium", false);

        MvcResult result = mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weather_summary").isNotEmpty())
            .andExpect(jsonPath("$.advice_mr").isNotEmpty())
            .andExpect(jsonPath("$.advice_en").isNotEmpty())
            .andExpect(jsonPath("$.alert_type").value("weather"))
            .andExpect(jsonPath("$.priority").value("medium"))
            .andExpect(jsonPath("$.temperature").value(28.5))
            .andExpect(jsonPath("$.humidity").value(65))
            .andExpect(jsonPath("$.rainfall_mm").value(5.0))
            .andExpect(jsonPath("$.notification_sent").value(false))
            .andReturn();

        // Verify AI service was called with correct coordinates
        wireMock.verify(getRequestedFor(urlPathEqualTo("/ai/weather"))
            .withQueryParam("lat",    equalTo("18.52"))
            .withQueryParam("lon",    equalTo("73.85"))
            .withQueryParam("crop",   equalTo("soybean"))
            .withQueryParam("stage",  equalTo("vegetative"))
            .withQueryParam("lang",   equalTo("marathi"))
            .withQueryParam("farmer_id", equalTo(farmerId.toString()))
        );
    }

    // ── Test 3: Heavy rain (>20mm) — notification triggered ──────────────────

    @Test
    @Order(3)
    @DisplayName("E2E — heavy rain (35mm) → notification_sent=true, priority=high")
    void heavyRain_notificationTriggered() throws Exception {
        stubAiWeatherResponse(35.0, 26.0, 85, "heavy rain", "high", true);

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rainfall_mm").value(35.0))
            .andExpect(jsonPath("$.priority").value("high"))
            .andExpect(jsonPath("$.notification_sent").value(true))
            .andExpect(jsonPath("$.advice_mr").value("येत्या ४८ तासांत ६५ मिमी पाऊस अपेक्षित. खत टाकणे पुढे ढकला."))
            .andExpect(jsonPath("$.advice_en").value("65mm rain in 48 hours. Delay fertilizer application."));
    }

    // ── Test 4: Extreme heat (temp > 42°C) — notification triggered ──────────

    @Test
    @Order(4)
    @DisplayName("E2E — extreme heat (45°C) → notification_sent=true, priority=high")
    void extremeHeat_notificationTriggered() throws Exception {
        stubAiWeatherResponse(2.0, 45.0, 30, "extreme heat", "high", true);

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.temperature").value(45.0))
            .andExpect(jsonPath("$.priority").value("high"))
            .andExpect(jsonPath("$.notification_sent").value(true));
    }

    // ── Test 5: explicit farm_id param ───────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("E2E — explicit farm_id param is respected")
    void explicitFarmId_usedForCoordinates() throws Exception {
        stubAiWeatherResponse(10.0, 29.0, 70, "partly cloudy", "medium", false);

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString())
                .param("farm_id",   farmId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.alert_type").value("weather"));

        // Coordinates from the explicit farm should be forwarded
        wireMock.verify(getRequestedFor(urlPathEqualTo("/ai/weather"))
            .withQueryParam("lat", equalTo("18.52"))
            .withQueryParam("lon", equalTo("73.85")));
    }

    // ── Test 6: no farms registered — 400 bad request ────────────────────────

    @Test
    @Order(6)
    @DisplayName("E2E — farmer with no farms → 400 Bad Request")
    void farmerWithNoFarms_returns400() throws Exception {
        Farmer noFarmFarmer = farmerRepository.save(
            Farmer.builder().mobile("1111111111").language("marathi").build()
        );

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", noFarmFarmer.getId().toString()))
            .andExpect(status().isBadRequest());
    }

    // ── Test 7: AI service down → graceful degraded response ─────────────────

    @Test
    @Order(7)
    @DisplayName("E2E — AI service returns 500 → graceful fallback response, no 500 to mobile")
    void aiServiceDown_gracefulFallback() throws Exception {
        wireMock.stubFor(WireMock.get(urlPathEqualTo("/ai/weather"))
            .willReturn(serverError()));

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString()))
            .andExpect(status().isOk())   // never propagate 500 to mobile
            .andExpect(jsonPath("$.advice_mr").isNotEmpty())
            .andExpect(jsonPath("$.advice_en").isNotEmpty())
            .andExpect(jsonPath("$.notification_sent").value(false));
    }

    // ── Test 8: unknown farmer_id → 400 ──────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("E2E — unknown farmer_id → 400 Bad Request")
    void unknownFarmerId_returns400() throws Exception {
        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", java.util.UUID.randomUUID().toString()))
            .andExpect(status().isBadRequest());
    }

    // ── Test 9: full response field contract check ────────────────────────────

    @Test
    @Order(9)
    @DisplayName("E2E — all README-spec fields present in response")
    void responseContractAllFieldsPresent() throws Exception {
        stubAiWeatherResponse(12.0, 31.0, 68, "light rain", "medium", false);

        mockMvc.perform(
            get("/api/v1/weather")
                .param("farmer_id", farmerId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weather_summary").exists())
            .andExpect(jsonPath("$.advice_mr").exists())
            .andExpect(jsonPath("$.advice_en").exists())
            .andExpect(jsonPath("$.alert_type").exists())
            .andExpect(jsonPath("$.priority").exists())
            .andExpect(jsonPath("$.temperature").exists())
            .andExpect(jsonPath("$.humidity").exists())
            .andExpect(jsonPath("$.rainfall_mm").exists())
            .andExpect(jsonPath("$.notification_sent").exists());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Stub the Python AI /ai/weather endpoint to return a controlled payload.
     */
    private void stubAiWeatherResponse(
            double rainfallMm, double temperature, int humidity,
            String description, String priority, boolean notificationSent) {

        String body = String.format("""
            {
              "weather_summary": "Rain %.0fmm expected in 48 hours. Temperature %.1f°C.",
              "advice_mr": "येत्या ४८ तासांत ६५ मिमी पाऊस अपेक्षित. खत टाकणे पुढे ढकला.",
              "advice_en": "65mm rain in 48 hours. Delay fertilizer application.",
              "alert_type": "weather",
              "priority": "%s",
              "temperature": %.1f,
              "humidity": %d,
              "rainfall_mm": %.1f,
              "description": "%s",
              "notification_sent": %b
            }
            """,
            rainfallMm, temperature, priority, temperature,
            humidity, rainfallMm, description, notificationSent
        );

        wireMock.stubFor(WireMock.get(urlPathEqualTo("/ai/weather"))
            .willReturn(okJson(body)));
    }
}
