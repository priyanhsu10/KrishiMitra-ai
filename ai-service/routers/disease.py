from fastapi import APIRouter, UploadFile, File, Form
from typing import Optional
import logging

from services import vision_service, notify_service

logger = logging.getLogger(__name__)
router = APIRouter()

@router.post("/disease-detect")
async def detect_disease(
    file: UploadFile = File(...),
    crop_type: str = Form(...),
    language: str = Form(default="marathi"),
    farmer_id: str = Form(...),
    crop_id: Optional[str] = Form(None)
):
    """
    Analyze plant disease from leaf image using Claude Vision.
    Automatically triggers notification if severity is high.
    """
    try:
        # Read image
        image_bytes = await file.read()
        
        # Analyze with Claude Vision
        result = await vision_service.analyze_disease(
            image_bytes=image_bytes,
            crop_type=crop_type,
            language=language
        )
        
        # Trigger notification if high severity and confident
        if result.get("severity") == "high" and result.get("confidence", 0) > 0.70:
            await notify_service.trigger_notification(
                farmer_id=farmer_id,
                crop_id=crop_id,
                alert_type="disease",
                message_en=f"Disease detected: {result['disease']}. {result['remedy_en']}",
                message_mr=f"रोग आढळला: {result['disease_mr']}. {result['remedy_mr']}",
                priority="high"
            )
            logger.info(f"High-priority disease notification triggered for farmer {farmer_id}")
        
        return {
            **result,
            "notification_sent": result.get("severity") == "high" and result.get("confidence", 0) > 0.70
        }
    
    except Exception as e:
        logger.error(f"Disease detection failed: {e}")
        return {
            "error": str(e),
            "disease": "Analysis Failed",
            "disease_mr": "विश्लेषण अयशस्वी",
            "confidence": 0.0
        }
