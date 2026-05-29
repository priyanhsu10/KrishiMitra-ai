package com.krishimitra.crop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CropTimelineService {

    private final WebClient aiServiceClient;
    private final CropTimelineRepository timelineRepository;

    @SuppressWarnings("unchecked")
    public void generateTimeline(Crop crop) {
        log.info("Generating AI-assisted timeline for crop: {} sown on {}", crop.getCropType(), crop.getSowingDate());

        try {
            Map<String, Object> aiRequest = new HashMap<>();
            aiRequest.put("crop_type", crop.getCropType());
            aiRequest.put("sowing_date", crop.getSowingDate().toString());

            // Step 1: Call AI service for timeline stages
            Map<String, Object> aiResult = aiServiceClient.post()
                    .uri("/ai/crop-timeline")
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (aiResult != null && aiResult.containsKey("stages")) {
                List<Map<String, Object>> stages = (List<Map<String, Object>>) aiResult.get("stages");
                
                List<CropTimelineItem> timelineItems = stages.stream().map(stageData -> {
                    return CropTimelineItem.builder()
                            .cropId(crop.getId())
                            .stage((String) stageData.get("stage"))
                            .description((String) stageData.get("description"))
                            .estimatedDate(LocalDate.parse((String) stageData.get("estimated_date")))
                            .completed(false)
                            .build();
                }).collect(Collectors.toList());

                timelineRepository.saveAll(timelineItems);
                log.info("Successfully generated {} timeline items for crop {}", timelineItems.size(), crop.getId());
            }

        } catch (Exception e) {
            log.error("Failed to generate AI crop timeline: {}. Using fallback.", e.getMessage());
            generateFallbackTimeline(crop);
        }
    }

    private void generateFallbackTimeline(Crop crop) {
        // Simple fallback based on common crop durations if AI fails
        List<CropTimelineItem> fallbackItems = new ArrayList<>();
        LocalDate start = crop.getSowingDate();

        fallbackItems.add(createItem(crop.getId(), "Planting", "Initial sowing of seeds", start));
        fallbackItems.add(createItem(crop.getId(), "Germination", "Seeds beginning to sprout", start.plusDays(10)));
        fallbackItems.add(createItem(crop.getId(), "Vegetative", "Rapid growth of leaves and stems", start.plusDays(30)));
        fallbackItems.add(createItem(crop.getId(), "Flowering", "Crops beginning to flower", start.plusDays(60)));
        fallbackItems.add(createItem(crop.getId(), "Maturity", "Crop reaching full size", start.plusDays(90)));
        fallbackItems.add(createItem(crop.getId(), "Harvest", "Ready for market", start.plusDays(110)));

        timelineRepository.saveAll(fallbackItems);
    }

    private CropTimelineItem createItem(UUID cropId, String stage, String desc, LocalDate date) {
        return CropTimelineItem.builder()
                .cropId(cropId)
                .stage(stage)
                .description(desc)
                .estimatedDate(date)
                .completed(false)
                .build();
    }

    public List<CropTimelineItem> getTimeline(UUID cropId) {
        return timelineRepository.findByCropIdOrderByEstimatedDateAsc(cropId);
    }
}
