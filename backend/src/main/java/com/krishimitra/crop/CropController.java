package com.krishimitra.crop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class CropController {

    private final CropRepository cropRepository;
    private final CropTimelineService timelineService;

    /**
     * Create a new crop on a farm.
     * POST /api/v1/crops
     */
    @PostMapping("/crops")
    public ResponseEntity<?> createCrop(@RequestBody Map<String, Object> request) {
        UUID farmId = UUID.fromString((String) request.get("farm_id"));
        String cropType = (String) request.get("crop_type");
        String sowingDateStr = (String) request.get("sowing_date");
        String stage = (String) request.get("stage");
        String language = (String) request.get("language");
        if (language == null) language = "mr";

        log.info("Creating crop: type={}, farmId={}, stage={}, language={}", cropType, farmId, stage, language);

        LocalDate sowingDate = LocalDate.parse(sowingDateStr);

        Crop crop = Crop.builder()
            .farmId(farmId)
            .cropType(cropType)
            .sowingDate(sowingDate)
            .stage(stage != null ? stage : "germination")
            .build();

        crop = cropRepository.save(crop);

        // Generate AI-assisted timeline synchronously
        timelineService.generateTimeline(crop, language);

        return ResponseEntity.ok(Map.of(
            "id", crop.getId(),
            "farm_id", crop.getFarmId(),
            "crop_type", crop.getCropType(),
            "sowing_date", crop.getSowingDate().toString(),
            "stage", crop.getStage(),
            "created", true
        ));
    }

    /**
     * Get timeline for a crop.
     */
    @GetMapping("/crops/{id}/timeline")
    public ResponseEntity<?> getCropTimeline(@PathVariable("id") UUID cropId) {
        log.info("Fetching timeline for crop: id={}", cropId);
        List<CropTimelineItem> timeline = timelineService.getTimeline(cropId);
        return ResponseEntity.ok(Map.of("crop_id", cropId, "timeline", timeline));
    }

    /**
     * Get crops for a farm.
     * GET /api/v1/crops?farm_id={}
     */
    @GetMapping("/crops")
    public ResponseEntity<?> getCrops(@RequestParam("farm_id") UUID farmId) {
        log.info("Fetching crops for farm: id={}", farmId);

        List<Crop> crops = cropRepository.findByFarmId(farmId);

        List<Map<String, Object>> cropList = crops.stream()
            .map(c -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", c.getId());
                map.put("farm_id", c.getFarmId());
                map.put("crop_type", c.getCropType());
                map.put("sowing_date", c.getSowingDate().toString());
                map.put("stage", c.getStage());
                
                // Get estimated harvest date from timeline
                List<CropTimelineItem> timeline = timelineService.getTimeline(c.getId());
                if (!timeline.isEmpty()) {
                    LocalDate harvestDate = timeline.get(timeline.size() - 1).getEstimatedDate();
                    map.put("estimated_harvest_date", harvestDate.toString());
                    
                    // Calculate progress based on dates in database
                    long totalDays = ChronoUnit.DAYS.between(c.getSowingDate(), harvestDate);
                    if (totalDays > 0) {
                        long passedDays = ChronoUnit.DAYS.between(c.getSowingDate(), LocalDate.now());
                        int progress = (int) Math.min(100, Math.max(0, (passedDays * 100) / totalDays));
                        map.put("growth_progress", progress);
                    }
                }
                
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("crops", cropList, "count", cropList.size()));
    }
}
