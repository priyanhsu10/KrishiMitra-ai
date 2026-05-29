from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Dict
import logging
from services import llm_service

router = APIRouter()
logger = logging.getLogger(__name__)

class TimelineRequest(BaseModel):
    crop_type: str
    sowing_date: str

@router.post("/crop-timeline")
async def get_crop_timeline(request: TimelineRequest):
    """
    Generate an AI-assisted crop timeline with estimated dates and stages.
    """
    try:
        logger.info(f"Generating timeline for {request.crop_type} sown on {request.sowing_date}")

        # Call LLM service to generate stages
        timeline = await llm_service.generate_timeline_data(
            request.crop_type,
            request.sowing_date
        )

        return timeline
    except Exception as e:
        logger.error(f"Error generating crop timeline: {e}")
        raise HTTPException(status_code=500, detail=str(e))
