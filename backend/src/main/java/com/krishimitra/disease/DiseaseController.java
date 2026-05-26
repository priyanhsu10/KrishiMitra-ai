package com.krishimitra.disease;

import com.krishimitra.notification.NotificationService;
import com.krishimitra.notification.NotifyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

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
            @RequestParam("file") byte[] file,
            @RequestParam("farmer_id") UUID farmerId,
            @RequestParam(value = "crop_id", required = false) UUID cropId,
            @RequestParam("crop_type") String cropType,
            @RequestParam(value = "language", defaultValue = "marathi") String language) {

        log.info("Disease detection requested: farmerId={}, crop={}", farmerId, cropType);
        Map<String, Object> result = diseaseService.detectAndSave(file, farmerId, cropId, cropType, language);
        return ResponseEntity.ok(result);
    }
}