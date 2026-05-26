import httpx
import os
import logging

logger = logging.getLogger(__name__)

SPRING_BASE_URL = os.getenv("SPRING_BASE_URL", "http://localhost:8080")

async def trigger_notification(
    farmer_id: str,
    crop_id: str,
    alert_type: str,
    message_en: str,
    message_mr: str,
    priority: str,
    message_hi: str = None
):
    """
    Call Spring Boot /notify endpoint. Fire-and-forget, never raise.
    This allows Spring Boot to handle FCM push and DB save atomically.
    """
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            response = await client.post(
                f"{SPRING_BASE_URL}/api/v1/notify",
                json={
                    "farmer_id": farmer_id,
                    "crop_id": crop_id,
                    "alert_type": alert_type,
                    "message_en": message_en,
                    "message_mr": message_mr,
                    "message_hi": message_hi,
                    "priority": priority,
                }
            )
            if response.status_code == 200:
                logger.info(f"Notification triggered successfully for farmer {farmer_id}")
            else:
                logger.warning(f"Notification trigger failed: {response.status_code}")
    except Exception as e:
        logger.error(f"[WARN] Notification trigger failed: {e}")
        # don't raise — AI response already sent to Spring Boot
