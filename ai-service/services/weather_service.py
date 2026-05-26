import httpx
import os
import logging
from typing import Dict
from services.llm_service import client

logger = logging.getLogger(__name__)

OPENWEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY")
OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast"

async def fetch_weather(lat: float, lon: float) -> Dict:
    """
    Fetch weather forecast from OpenWeatherMap.
    Returns 48-hour forecast data.
    """
    if not OPENWEATHER_API_KEY:
        logger.warning("OpenWeatherMap API key not set, returning mock data")
        return {
            "temperature": 28.5,
            "humidity": 72,
            "rainfall_mm": 35.0,
            "description": "Moderate rain expected"
        }
    
    try:
        async with httpx.AsyncClient() as http_client:
            response = await http_client.get(
                OPENWEATHER_URL,
                params={
                    "lat": lat,
                    "lon": lon,
                    "appid": OPENWEATHER_API_KEY,
                    "units": "metric",
                    "cnt": 16  # 48 hours (3-hour intervals)
                }
            )
            data = response.json()
            
            # Calculate rainfall sum
            rainfall = sum(
                item.get("rain", {}).get("3h", 0) 
                for item in data.get("list", [])
            )
            
            first_item = data["list"][0] if data.get("list") else {}
            
            return {
                "temperature": first_item.get("main", {}).get("temp", 0),
                "humidity": first_item.get("main", {}).get("humidity", 0),
                "rainfall_mm": rainfall,
                "description": first_item.get("weather", [{}])[0].get("description", "")
            }
    
    except Exception as e:
        logger.error(f"Weather fetch failed: {e}")
        raise

async def generate_weather_advisory(
    weather_data: Dict,
    crop_type: str,
    stage: str,
    language: str
) -> Dict:
    """
    Generate localized weather advisory using LLM.
    Returns advice in the specified language with priority.
    """
    prompt = f"""
    Weather forecast for {crop_type} ({stage} stage):
    - Temperature: {weather_data['temperature']}°C
    - Humidity: {weather_data['humidity']}%
    - Rainfall (48h): {weather_data['rainfall_mm']}mm
    
    Generate farming advice in {language} (2-3 sentences).
    Focus on: irrigation, fertilizer timing, disease risk.
    
    End with JSON: {{"priority": "high|medium|low"}}
    """
    
    try:
        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=300,
            messages=[{"role": "user", "content": prompt}]
        )
        
        text = response.content[0].text
        
        # Determine priority based on rainfall
        priority = "high" if weather_data["rainfall_mm"] > 20 else "medium"
        
        return {
            "advice": text,
            "priority": priority
        }
    
    except Exception as e:
        logger.error(f"Weather advisory generation failed: {e}")
        raise
