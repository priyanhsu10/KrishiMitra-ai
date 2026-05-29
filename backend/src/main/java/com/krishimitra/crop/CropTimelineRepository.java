package com.krishimitra.crop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CropTimelineRepository extends JpaRepository<CropTimelineItem, UUID> {
    List<CropTimelineItem> findByCropIdOrderByEstimatedDateAsc(UUID cropId);
}
