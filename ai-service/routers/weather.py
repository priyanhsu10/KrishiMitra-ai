from fastapi import APIRouter, Query, HTTPException
from typing import Optional
import logging

from services import weather_service, notify_service

logger = logging.getLogger(__name__)
router = APIRouter()


@router.get("/weather")
async def get_weather_advisory(
    lat: float = Query(..., description="Farm latitude"),
    lon: float = Query(..., description="Farm longitude"),
    crop: str = Query(..., description="Crop type e.g. soybean, cotton"),
    stage: str = Query(..., description="Crop stage e.g. vegetative, flowering"),
    lang: str = Query(default="marathi", description="Language: marathi | hindi | english"),
    farmer_id: str = Query(..., description="Farmer UUID"),
    crop_id: Optional[str] = Query(None, description="Crop UUID (optional)"),
):
    """
    GET /ai/weather?lat=&lon=&crop=&stage=&lang=&farmer_id=&crop_id=

    Flow:
      1. Fetch 48h forecast from OpenWeatherMap (mock if key missing)
      2. Generate Marathi + English advisories via LLM
      3. If rain > 20mm OR temp extreme → POST /api/v1/notify on Spring Boot
      4. Return full weather response

    Response contract (matches README):
      weather_summary, advice_mr, advice_en, alert_type, priority,
      temperature, humidity, rainfall_mm, description, notification_sent
    """
    try:
        # ── Step 1: Fetch weather forecast ────────────────────────────────────
        weather_data = await weather_service.fetch_weather(lat, lon)
        logger.info(
            f"Weather fetched: temp={weather_data['temperature']}°C, "
            f"rain={weather_data['rainfall_mm']}mm, farmer={farmer_id}"
        )

        # ── Step 2: Generate localised advisories ─────────────────────────────
        advisory = await weather_service.generate_weather_advisory(
            weather_data=weather_data,
            crop_type=crop,
            stage=stage,
            language=lang,
        )
        print(advisory)

        # ── Step 3: Trigger notification for significant weather events ────────
        should_notify = (
            weather_data["rainfall_mm"] > 20
            or weather_data["temperature"] > 42
            or weather_data["temperature"] < 5
        )

        if should_notify:
            await notify_service.trigger_notification(
                farmer_id=farmer_id,
                crop_id=crop_id,
                alert_type="weather",
                message_en=advisory["advice_en"],
                message_mr=advisory["advice_mr"],   # Marathi text to Spring → FCM push body
                priority="high",
            )
            logger.info(f"Weather alert notification triggered for farmer {farmer_id}")

        # ── Step 4: Return full response ──────────────────────────────────────
        return {
            "weather_summary": weather_data.get("weather_summary", ""),
            "advice": advisory.get("advice"),
            "advice_mr": advisory.get("advice_mr", ""),
            "advice_en": advisory.get("advice_en", ""),
            "alert_type": "weather",
            "priority": advisory["priority"],
            "temperature": weather_data["temperature"],
            "humidity": weather_data["humidity"],
            "rainfall_mm": weather_data["rainfall_mm"],
            "description": weather_data.get("description", ""),
            "notification_sent": should_notify,
        }

    except Exception as e:
        logger.error(f"Weather advisory endpoint failed: {e}", exc_info=True)
        # Return degraded response — never 500 the mobile client
        return {
            "weather_summary": "Unable to fetch weather data",
            "advice_mr": "हवामान माहिती मिळवण्यात अडचण आली. पुन्हा प्रयत्न करा.",
            "advice_en": "Unable to fetch weather data. Please try again.",
            "alert_type": "weather",
            "priority": "low",
            "temperature": None,
            "humidity": None,
            "rainfall_mm": None,
            "description": "",
            "notification_sent": False,
            "error": str(e),
        }
