package com.krishimitra.crop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "crops")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Crop {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "farm_id")
    private UUID farmId;
    
    @Column(name = "crop_type")
    private String cropType;
    
    @Column(name = "sowing_date")
    private LocalDate sowingDate;
    
    private String stage;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
