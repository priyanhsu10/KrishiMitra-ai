package com.krishimitra.crop;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "crop_timeline_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropTimelineItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "crop_id")
    private UUID cropId;
    
    private String stage;
    
    @Column(name = "estimated_date")
    private LocalDate estimatedDate;
    
    private String description;
    
    private boolean completed;

    @org.hibernate.annotations.CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private java.time.LocalDateTime createdAt;
}
