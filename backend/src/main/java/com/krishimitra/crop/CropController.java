package com.krishimitra.crop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

        log.info("Creating crop: type={}, farmId={}, stage={}", cropType, farmId, stage);

        LocalDate sowingDate = LocalDate.parse(sowingDateStr);

        Crop crop = Crop.builder()
            .farmId(farmId)
            .cropType(cropType)
            .sowingDate(sowingDate)
            .stage(stage != null ? stage : "germination")
            .build();

        crop = cropRepository.save(crop);

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
     * Get crops for a farm.
     * GET /api/v1/crops?farm_id={}
     */
    @GetMapping("/crops")
    public ResponseEntity<?> getCrops(@RequestParam("farm_id") UUID farmId) {
        log.info("Fetching crops for farm: id={}", farmId);

        List<Crop> crops = cropRepository.findByFarmId(farmId);

        List<Map<String, Object>> cropList = crops.stream()
            .map(c -> Map.<String, Object>of(
                "id", c.getId(),
                "farm_id", c.getFarmId(),
                "crop_type", c.getCropType(),
                "sowing_date", c.getSowingDate().toString(),
                "stage", c.getStage()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("crops", cropList, "count", cropList.size()));
    }
}
