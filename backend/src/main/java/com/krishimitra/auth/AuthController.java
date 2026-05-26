package com.krishimitra.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.initiateLogin(request.getMobile()));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtp(request.getMobile(), request.getOtp()));
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@Valid @RequestBody RegisterTokenRequest request) {
        return ResponseEntity.ok(authService.registerFcmToken(request.getFarmerId(), request.getFcmToken()));
    }
}
