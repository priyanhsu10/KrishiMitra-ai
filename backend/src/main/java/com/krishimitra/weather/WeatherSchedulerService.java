package com.krishimitra.weather;

import com.krishimitra.advisory.Advisory;
import com.krishimitra.advisory.AdvisoryRepository;
import com.krishimitra.crop.Crop;
import com.krishimitra.crop.CropRepository;
import com.krishimitra.farm.Farm;
import com.krishimitra.farm.FarmRepository;
import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Daily scheduler that checks weather for all registered farms.
 * 
 * Automated: runs every day at 6 AM via @Scheduled(cron = "0 0 6 * * ?").
 * Manual:   POST /api/v1/weather/schedule/run-now (for hackathon demo).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherSchedulerService {

    private final FarmerRepository farmerRepository;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final AdvisoryRepository advisoryRepository;
    private final WeatherService weatherService;

    // Track the most recent run (for the status endpoint)
    private final Map<String, Object> lastRun = new ConcurrentHashMap<>();

    /**
     * Daily automatic weather check — runs every morning at 06:00.
     * Inspects all farmer farms for the next 7‑day weather outlook
     * and creates advisories for extreme weather events.
     */
    @Scheduled(cron = "0 0 6 * * ?")   // Every day at 6:00 AM
    @Transactional
    public void runDailyWeatherAnalysis() {
        doRun("AUTOMATIC (6 AM cron)");
    }

    /**
     * Manual trigger — called via REST API for demo.
     * Performs exactly the same logic as the automatic run.
     */
    @Transactional
    public String runWeatherCheckNow() {
        return doRun("MANUAL (API trigger)");
    }

    // ── shared logic ──────────────────────────────────────────────────────────

    private String doRun(String triggerType) {
        LocalDateTime start = LocalDateTime.now();
        log.info("🌦️ ====== STARTING weather check [{}] at {} ======", triggerType, start);

        // Find all farmers, look for those with farms
        List<Farmer> allFarmers = farmerRepository.findAll();

        int totalFarmersWithFarms = 0;
        int totalAdvisoriesGenerated = 0;
        int highPriorityCount = 0;
        int errorCount = 0;

        for (Farmer farmer : allFarmers) {
            try {
                List<Farm> farms = farmRepository.findByFarmerId(farmer.getId());
                if (farms.isEmpty()) {
                    continue; // no farms registered → skip
                }

                Farm farm = farms.get(0);  // use the first / primary farm
                totalFarmersWithFarms++;

                // Get the most recent crop (for advisory context)
                List<Crop> crops = cropRepository.findByFarmId(farm.getId());
                String cropType = "general";
                UUID cropId = null;
                String stage = "vegetative";

                if (!crops.isEmpty()) {
                    Crop latestCrop = crops.get(crops.size() - 1);
                    cropType = latestCrop.getCropType();
                    cropId = latestCrop.getId();
                    stage = latestCrop.getStage();
                }

                log.debug("Checking weather for farmerId={}, farmId={}, crop={}", 
                    farmer.getId(), farm.getId(), cropType);

                // Call the existing WeatherService (calls Python AI, triggers /notify if needed)
                WeatherResponse weatherResp = weatherService.getWeatherAdvisory(
                    farmer.getId(), farm.getId()
                );

                // Check if a new advisory was created by the /notify endpoint
                List<Advisory> recentAdvisories = advisoryRepository
                    .findByFarmerIdAndCreatedAtAfterOrderByCreatedAtDesc(
                        farmer.getId(), LocalDateTime.now().minusMinutes(5));

                if (!recentAdvisories.isEmpty()) {
                    totalAdvisoriesGenerated += recentAdvisories.size();
                    Advisory latest = recentAdvisories.get(0);
                    if ("high".equalsIgnoreCase(latest.getPriority())) {
                        highPriorityCount++;
                    }
                    log.info("📢 Advisory for farmer {}: priority={}, type={}, crop={}", 
                        farmer.getId(), latest.getPriority(), latest.getAlertType(), cropType);
                } else {
                    log.debug("No advisory generated for farmer {}", farmer.getId());
                }

            } catch (Exception e) {
                errorCount++;
                log.error("❌ Error processing farmer {}: {}", farmer.getId(), e.getMessage(), e);
            }
        }

        // Record the run
        lastRun.put("triggerType", triggerType);
        lastRun.put("startedAt", start.toString());
        lastRun.put("completedAt", LocalDateTime.now().toString());
        lastRun.put("farmersChecked", totalFarmersWithFarms);
        lastRun.put("advisoriesGenerated", totalAdvisoriesGenerated);
        lastRun.put("highPriority", highPriorityCount);
        lastRun.put("errors", errorCount);

        String summary = String.format(
            "Weather check complete: %d farmers, %d advisories, %d high-priority, %d errors",
            totalFarmersWithFarms, totalAdvisoriesGenerated, highPriorityCount, errorCount
        );

        log.info("📊 ====== {} ======", summary);
        return summary;
    }

    /** Read-only status of the last scheduler run (for demo/monitoring). */
    public Map<String, Object> getLastRunStatus() {
        return Map.copyOf(lastRun);
    }
}