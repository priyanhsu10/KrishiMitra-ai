package com.krishimitra.disease;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiseaseReportRepository extends JpaRepository<DiseaseReport, UUID> {
    List<DiseaseReport> findByFarmerIdOrderByCreatedAtDesc(UUID farmerId);
    List<DiseaseReport> findByCropIdOrderByCreatedAtDesc(UUID cropId);
}
