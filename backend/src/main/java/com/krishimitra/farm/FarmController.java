package com.krishimitra.farm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class FarmController {

    private final FarmRepository farmRepository;

    /**
     * Create a new farm.
     * POST /api/v1/farms
     */
    @PostMapping("/farms")
    public ResponseEntity<?> createFarm(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        UUID farmerId = UUID.fromString((String) request.get("farmer_id"));
        BigDecimal latitude = new BigDecimal(String.valueOf(request.get("latitude")));
        BigDecimal longitude = new BigDecimal(String.valueOf(request.get("longitude")));
        BigDecimal areaAcres = new BigDecimal(String.valueOf(request.get("area_acres")));
        String soilType = (String) request.get("soil_type");

        log.info("Creating farm: name={}, farmerId={}, area={} acres", name, farmerId, areaAcres);

        Farm farm = Farm.builder()
            .farmerId(farmerId)
            .name(name)
            .latitude(latitude)
            .longitude(longitude)
            .areaAcres(areaAcres)
            .soilType(soilType)
            .build();

        farm = farmRepository.save(farm);

        return ResponseEntity.ok(Map.of(
            "id", farm.getId(),
            "name", farm.getName(),
            "farmer_id", farm.getFarmerId(),
            "area_acres", farm.getAreaAcres(),
            "soil_type", farm.getSoilType(),
            "created", true
        ));
    }

    /**
     * Get farms for a farmer.
     * GET /api/v1/farms?farmer_id={}
     */
    @GetMapping("/farms")
    public ResponseEntity<?> getFarms(@RequestParam("farmer_id") UUID farmerId) {
        log.info("Fetching farms for farmer: id={}", farmerId);

        List<Farm> farms = farmRepository.findByFarmerId(farmerId);

        List<Map<String, Object>> farmList = farms.stream()
            .map(f -> Map.<String, Object>of(
                "id", f.getId(),
                "farmer_id", f.getFarmerId(),
                "name", f.getName(),
                "latitude", f.getLatitude(),
                "longitude", f.getLongitude(),
                "area_acres", f.getAreaAcres(),
                "soil_type", f.getSoilType()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("farms", farmList, "count", farmList.size()));
    }
}
