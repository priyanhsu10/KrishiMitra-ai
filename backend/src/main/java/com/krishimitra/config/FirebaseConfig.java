package com.krishimitra.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private Resource credentialsResource;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        try {
            GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsResource.getInputStream());
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();
            
            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
            return app;
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
            throw e;
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
