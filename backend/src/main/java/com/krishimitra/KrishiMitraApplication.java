package com.krishimitra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables @Scheduled methods for the daily weather advisory job and Caching for performance.
 */
@SpringBootApplication
@EnableScheduling
@EnableCaching
public class KrishiMitraApplication {
    public static void main(String[] args) {
        SpringApplication.run(KrishiMitraApplication.class, args);
    }
}
