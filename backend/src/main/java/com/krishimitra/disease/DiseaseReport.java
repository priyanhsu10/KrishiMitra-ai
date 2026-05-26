package com.krishimitra.disease;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "disease_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "farmer_id")
    private UUID farmerId;
    
    @Column(name = "crop_id")
    private UUID cropId;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(name = "diagnosis_mr", columnDefinition = "TEXT")
    private String diagnosisMr;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal confidence;
    
    @Column(name = "remedy_en", columnDefinition = "TEXT")
    private String remedyEn;
    
    @Column(name = "remedy_mr", columnDefinition = "TEXT")
    private String remedyMr;
    
    private String severity;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
