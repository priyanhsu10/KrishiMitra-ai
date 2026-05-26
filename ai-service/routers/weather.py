from fastapi import APIRouter, Query
from typing import Optional
import logging

from services import weather_service, notify_service

logger = logging.getLogger(__name__)
router = APIRouter()

@router.get("/weather")
async def get_weather_advisory(
    lat: float = Query(...),
    lon: float = Query(...),
    crop: str = Query(...),
    stage: str = Query(...),
    lang: str = Query(default="marathi"),
    farmer_id: str = Query(...),
    crop_id: Optional[str] = Query(None)
):
    """
    Get weather forecast and AI-generated advisory.
    Always triggers notification if rain > 20mm or extreme temperature.
    """
    try:
        # Fetch weather data
        weather_data = await weather_service.fetch_weather(lat, lon)
        
        # Generate localized advisory
        advisory = await weather_service.generate_weather_advisory(
            weather_data=weather_data,
            crop_type=crop,
            stage=stage,
            language=lang
        )
        
        # Trigger notification for significant weather events
        should_notify = (
            weather_data["rainfall_mm"] > 20 or
            weather_data["temperature"] > 42 or
            weather_data["temperature"] < 5
        )
        
        if should_notify:
            await notify_service.trigger_notification(
                farmer_id=farmer_id,
                crop_id=crop_id,
                alert_type="weather",
                message_en=advisory["advice"],
                message_mr=advisory["advice"],
                priority="high"
            )
            logger.info(f"Weather alert triggered for farmer {farmer_id}")
        
        return {
            **weather_data,
            "advice": advisory["advice"],
            "priority": advisory["priority"],
            "notification_sent": should_notify
        }
    
    except Exception as e:
        logger.error(f"Weather advisory failed: {e}")
        return {
            "error": str(e),
            "advice": "Unable to fetch weather data. Please try again.",
            "priority": "low"
        }
