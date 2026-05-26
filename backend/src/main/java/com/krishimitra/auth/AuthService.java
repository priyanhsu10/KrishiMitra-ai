package com.krishimitra.auth;

import com.krishimitra.farmer.Farmer;
import com.krishimitra.farmer.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final FarmerRepository farmerRepository;
    private static final String MOCK_OTP = "123456";

    public Map<String, Object> initiateLogin(String mobile) {
        log.info("Login initiated for mobile: {}", mobile);
        
        Map<String, Object> response = new HashMap<>();
        response.put("otp_sent", true);
        response.put("message", "OTP sent successfully (mock OTP: 123456)");
        
        return response;
    }

    public Map<String, Object> verifyOtp(String mobile, String otp) {
        if (!MOCK_OTP.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        Farmer farmer = farmerRepository.findByMobile(mobile)
            .orElseGet(() -> createNewFarmer(mobile));

        // In production, generate JWT token here
        String token = "mock_jwt_token_" + farmer.getId();

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("farmer_id", farmer.getId());
        response.put("is_new_user", farmer.getName() == null);
        response.put("language", farmer.getLanguage());

        log.info("OTP verified for farmer: {}", farmer.getId());
        return response;
    }

    public Map<String, Object> registerFcmToken(UUID farmerId, String fcmToken) {
        Farmer farmer = farmerRepository.findById(farmerId)
            .orElseThrow(() -> new RuntimeException("Farmer not found"));

        farmer.setFcmToken(fcmToken);
        farmerRepository.save(farmer);

        log.info("FCM token registered for farmer: {}", farmerId);

        Map<String, Object> response = new HashMap<>();
        response.put("registered", true);
        return response;
    }

    private Farmer createNewFarmer(String mobile) {
        Farmer farmer = Farmer.builder()
            .mobile(mobile)
            .language("marathi")
            .build();
        
        farmer = farmerRepository.save(farmer);
        log.info("New farmer created: {}", farmer.getId());
        
        return farmer;
    }
}
