package com.krishimitra.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse {

    @JsonProperty("weather_summary")
    private String weatherSummary;

    @JsonProperty("advice_mr")
    private String adviceMr;

    @JsonProperty("advice_en")
    private String adviceEn;

    @JsonProperty("alert_type")
    private String alertType;

    private String priority;

    private Double temperature;

    private Integer humidity;

    @JsonProperty("rainfall_mm")
    private Double rainfallMm;

    private String description;

    @JsonProperty("notification_sent")
    private boolean notificationSent;
}
