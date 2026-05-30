package com.krishimitra.disease;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/disease")
@RequiredArgsConstructor
@Slf4j
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> detectDisease(
            @RequestParam("file") MultipartFile file,
            @RequestParam("farmer_id") UUID farmerId,
            @RequestParam(value = "crop_id", required = false) UUID cropId,
            @RequestParam("crop_type") String cropType,
            @RequestParam(value = "language", defaultValue = "mr") String language) throws IOException {

        log.info("Disease detection requested: farmerId={}, crop={}", farmerId, cropType);
        Map<String, Object> result = diseaseService.detectAndSave(file.getBytes(), farmerId, cropId, cropType, language);
        return ResponseEntity.ok(result);
    }
}