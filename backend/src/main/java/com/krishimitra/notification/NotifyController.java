package com.krishimitra.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
@Slf4j
public class NotifyController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotifyResponse> sendNotification(@Valid @RequestBody NotifyRequest request) {
        log.info("Notification request received: farmerId={}, type={}, priority={}", 
            request.getFarmerId(), request.getAlertType(), request.getPriority());
        
        NotifyResponse response = notificationService.sendAlert(request);
        return ResponseEntity.ok(response);
    }
}
