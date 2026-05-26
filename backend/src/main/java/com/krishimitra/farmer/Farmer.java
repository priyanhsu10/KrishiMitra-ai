package com.krishimitra.farmer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "farmers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farmer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String mobile;
    
    private String name;
    
    private String language;
    
    private String village;
    
    private String state;
    
    @Column(name = "fcm_token", length = 500)
    private String fcmToken;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
