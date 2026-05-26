package com.krishimitra.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyRequest {
    @NotBlank
    private String mobile;
    
    @NotBlank
    private String otp;
}
