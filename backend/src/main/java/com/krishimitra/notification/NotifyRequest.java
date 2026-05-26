package com.krishimitra.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyRequest {
    
    @NotNull
    private UUID farmerId;
    
    private UUID cropId;
    
    @NotBlank
    private String alertType;  // weather | disease | irrigation | fertilizer | market
    
    @NotBlank
    private String messageEn;
    
    private String messageMr;
    
    private String messageHi;
    
    @NotBlank
    private String priority;  // high | medium | low
}
