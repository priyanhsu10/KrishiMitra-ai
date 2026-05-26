package com.krishimitra.farm;

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
@Table(name = "farms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Farm {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "farmer_id")
    private UUID farmerId;
    
    private String name;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;
    
    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;
    
    @Column(name = "area_acres", precision = 6, scale = 2)
    private BigDecimal areaAcres;
    
    @Column(name = "soil_type")
    private String soilType;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
