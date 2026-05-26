package com.krishimitra.advisory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "advisories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Advisory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "crop_id")
    private UUID cropId;
    
    @Column(name = "farmer_id")
    private UUID farmerId;
    
    @Column(name = "alert_type")
    private String alertType;
    
    @Column(name = "message_en", columnDefinition = "TEXT")
    private String messageEn;
    
    @Column(name = "message_mr", columnDefinition = "TEXT")
    private String messageMr;
    
    @Column(name = "message_hi", columnDefinition = "TEXT")
    private String messageHi;
    
    private String priority;
    
    @Column(name = "is_read")
    private Boolean isRead;
    
    @Column(name = "fcm_sent")
    private Boolean fcmSent;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
