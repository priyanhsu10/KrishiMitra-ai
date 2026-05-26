from fastapi import APIRouter
from pydantic import BaseModel
from typing import Optional
import logging

from services import llm_service, notify_service

logger = logging.getLogger(__name__)
router = APIRouter()

class AdvisoryRequest(BaseModel):
    farmer_id: str
    crop_type: str
    stage: str
    language: str = "marathi"
    question: str
    crop_id: Optional[str] = None
    weather_summary: Optional[str] = "No weather data"

@router.post("/advisory")
async def get_advisory(request: AdvisoryRequest):
    """
    Get AI-powered crop advisory in the specified language.
    Triggers notification if priority is high.
    """
    try:
        result = await llm_service.get_crop_advisory(
            crop_type=request.crop_type,
            stage=request.stage,
            language=request.language,
            question=request.question,
            weather_summary=request.weather_summary
        )
        
        # Trigger notification if high priority
        notification_sent = False
        if result.get("priority") == "high":
            await notify_service.trigger_notification(
                farmer_id=request.farmer_id,
                crop_id=request.crop_id,
                alert_type=result.get("alert_type", "general"),
                message_en=result["advice"],
                message_mr=result["advice"],  # LLM already returned in requested language
                priority="high"
            )
            notification_sent = True
            logger.info(f"High-priority advisory notification triggered for farmer {request.farmer_id}")
        
        return {
            **result,
            "notification_sent": notification_sent
        }
    
    except Exception as e:
        logger.error(f"Advisory generation failed: {e}")
        return {
            "error": str(e),
            "advice": "Unable to generate advisory. Please try again.",
            "priority": "low"
        }
