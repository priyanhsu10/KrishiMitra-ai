package com.krishimitra.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class RegisterTokenRequest {
    @NotNull
    private UUID farmerId;
    
    @NotBlank
    private String fcmToken;
}
