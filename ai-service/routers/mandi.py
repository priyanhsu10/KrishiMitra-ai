from fastapi import APIRouter, Query
import logging

logger = logging.getLogger(__name__)
router = APIRouter()

# Mock mandi prices - in production, integrate with AGMARKNET API
MOCK_PRICES = {
    "soybean": [
        {"mandi": "Pune", "price_per_quintal": 4850, "trend": "rising"},
        {"mandi": "Nashik", "price_per_quintal": 4720, "trend": "stable"},
        {"mandi": "Ahmednagar", "price_per_quintal": 4900, "trend": "rising"}
    ],
    "cotton": [
        {"mandi": "Pune", "price_per_quintal": 7200, "trend": "stable"},
        {"mandi": "Nashik", "price_per_quintal": 7150, "trend": "falling"},
        {"mandi": "Ahmednagar", "price_per_quintal": 7300, "trend": "rising"}
    ],
    "wheat": [
        {"mandi": "Pune", "price_per_quintal": 2100, "trend": "stable"},
        {"mandi": "Nashik", "price_per_quintal": 2080, "trend": "stable"},
        {"mandi": "Ahmednagar", "price_per_quintal": 2120, "trend": "rising"}
    ],
    "onion": [
        {"mandi": "Pune", "price_per_quintal": 1500, "trend": "rising"},
        {"mandi": "Nashik", "price_per_quintal": 1450, "trend": "rising"},
        {"mandi": "Ahmednagar", "price_per_quintal": 1520, "trend": "rising"}
    ]
}

@router.get("/mandi")
async def get_mandi_prices(
    crop: str = Query(...),
    state: str = Query(default="Maharashtra")
):
    """
    Get nearby mandi (market) prices for the specified crop.
    Returns mock data for hackathon - production should use AGMARKNET API.
    """
    try:
        crop_lower = crop.lower()
        prices = MOCK_PRICES.get(crop_lower, [
            {"mandi": "Pune", "price_per_quintal": 5000, "trend": "stable"}
        ])
        
        # Simple advice based on trend
        rising_count = sum(1 for p in prices if p["trend"] == "rising")
        advice_mr = "बाजारातील भाव वाढत आहेत. विक्री करण्यासाठी योग्य वेळ." if rising_count >= 2 else "भाव स्थिर आहेत. आठवडाभरात विक्री करा."
        
        return {
            "crop": crop,
            "state": state,
            "prices": prices,
            "advice_mr": advice_mr,
            "best_time_to_sell": "now" if rising_count >= 2 else "next_week"
        }
    
    except Exception as e:
        logger.error(f"Mandi price fetch failed: {e}")
        return {
            "error": str(e),
            "prices": []
        }
