package com.krishimitra.advisory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvisoryRepository extends JpaRepository<Advisory, UUID> {
    List<Advisory> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);
    List<Advisory> findByFarmerIdAndIsReadOrderByCreatedAtDesc(UUID farmerId, Boolean isRead);
    long countByFarmerIdAndIsRead(UUID farmerId, Boolean isRead);
}
