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
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Optional;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials.path:classpath:krishimitra-firebase-adminsdk.json}")
    private Resource credentialsResource;

    @Bean
    public Optional<FirebaseApp> firebaseApp() {
        log.info("Attempting to initialize Firebase with resource: {}", credentialsResource);
        try {
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase already initialized, using existing app.");
                return Optional.of(FirebaseApp.getInstance());
            }

            // Check if the resource exists
            if (credentialsResource == null || !credentialsResource.exists()) {
                String path = "unknown";
                try {
                    path = credentialsResource != null ? credentialsResource.getFile().getAbsolutePath() : "null";
                } catch (Exception e) {
                    path = credentialsResource != null ? credentialsResource.getDescription() : "null";
                }
                log.error("Firebase credentials file NOT FOUND at: {}. Push notifications disabled.", path);
                return Optional.empty();
            }

            log.info("Loading Firebase credentials from: {}", credentialsResource.getURL());
            GoogleCredentials credentials = GoogleCredentials
                .fromStream(credentialsResource.getInputStream());

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully from {}", credentialsResource.getFilename());
            return Optional.of(app);
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Bean
    public Optional<FirebaseMessaging> firebaseMessaging(Optional<FirebaseApp> firebaseApp) {
        if (firebaseApp.isPresent()) {
            return Optional.of(FirebaseMessaging.getInstance(firebaseApp.get()));
        }
        return Optional.empty();
    }
}
