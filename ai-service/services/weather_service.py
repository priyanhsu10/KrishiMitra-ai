import httpx
import os
import json
import logging
from typing import Dict
from pathlib import Path
import yaml

logger = logging.getLogger(__name__)

OPENWEATHER_API_KEY = os.getenv("OPENWEATHER_API_KEY")
OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast"

# Load Marathi advisory YAML templates
_TEMPLATE_PATH = Path(__file__).parent.parent / "prompts" / "marathi_advisory.yaml"
with _TEMPLATE_PATH.open(encoding="utf-8") as _f:
    _TEMPLATES = yaml.safe_load(_f)

# Import the shared OpenRouter client
from services.llm_service import client as llm_client


async def fetch_weather(lat: float, lon: float) -> Dict:
    """
    Fetch 48-hour weather forecast from OpenWeatherMap.
    Falls back to mock data if API key is not set.
    """
    if not OPENWEATHER_API_KEY:
        logger.warning("OPENWEATHER_API_KEY not set — returning mock weather data for demo")
        return {
            "temperature": 28.5,
            "humidity": 72,
            "rainfall_mm": 35.0,
            "description": "Moderate rain expected in 48 hours",
            "weather_summary": "Rain 35mm expected in 48 hours",
        }

    try:
        async with httpx.AsyncClient(timeout=10.0) as http_client:
            response = await http_client.get(
                OPENWEATHER_URL,
                params={
                    "lat": lat,
                    "lon": lon,
                    "appid": OPENWEATHER_API_KEY,
                    "units": "metric",
                    "cnt": 16,  # 48 hours at 3-hour intervals
                },
            )
            response.raise_for_status()
            data = response.json()

            rainfall_mm = round(
                sum(item.get("rain", {}).get("3h", 0) for item in data.get("list", [])),
                2,
            )

            first = data["list"][0] if data.get("list") else {}
            temperature = first.get("main", {}).get("temp", 0)
            humidity = first.get("main", {}).get("humidity", 0)
            description = first.get("weather", [{}])[0].get("description", "")

            weather_summary = (
                f"Rain {rainfall_mm}mm expected in 48 hours. "
                f"Temperature {temperature}°C, Humidity {humidity}%."
            )

            return {
                "temperature": temperature,
                "humidity": humidity,
                "rainfall_mm": rainfall_mm,
                "description": description,
                "weather_summary": weather_summary,
            }

    except Exception as e:
        logger.error(f"Weather fetch failed: {e}")
        raise


async def generate_weather_advisory(
    weather_data: Dict,
    crop_type: str,
    stage: str,
    language: str,
) -> Dict:
    """
    Generate localized weather advisory based on requested language.
    """
    forecast_summary = (
        f"Temperature: {weather_data['temperature']}°C, "
        f"Humidity: {weather_data['humidity']}%, "
        f"Rainfall (48h): {weather_data['rainfall_mm']}mm, "
        f"Conditions: {weather_data.get('description', 'N/A')}"
    )

    # Normalize language name
    lang_map = {
        "mr": "Marathi",
        "marathi": "Marathi",
        "hi": "Hindi",
        "hindi": "Hindi",
        "en": "English",
        "english": "English"
    }
    target_lang = lang_map.get(language.lower(), "Marathi")

    async def get_advisory(lang: str):
        system_prompt = _TEMPLATES["weather_advisory"]["system"].format(language=lang)
        user_prompt = _TEMPLATES["weather_advisory"]["user_prompt"].format(
            crop_type=crop_type,
            stage=stage,
            forecast=forecast_summary,
            language=lang
        )

        response = llm_client.chat.completions.create(
            model="openai/gpt-4",
            max_tokens=500,
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_prompt},
            ],
        )
        text = response.choices[0].message.content.strip()

        advice = text
        priority = _determine_priority(weather_data)

        # Try to extract JSON priority from the last line
        lines = text.split("\n")
        try:
            last_line = lines[-1].strip()
            if last_line.startswith("{"):
                parsed = json.loads(last_line)
                priority = parsed.get("priority", priority)
                advice = "\n".join(lines[:-1]).strip()
        except:
            pass

        return advice, priority

    try:
        # 1. Generate primary advisory in requested language
        advice_primary, priority = await get_advisory(target_lang)

        # 2. Map to mr/en fields for backward compatibility with Spring Boot/Mobile
        advice_mr = advice_primary if target_lang == "Marathi" else ""
        advice_en = advice_primary if target_lang == "English" else ""

        # 3. Fill missing fields if necessary
        if not advice_mr:
            # If primary isn't Marathi, and we need Marathi (e.g. for background alerts), generate it or use fallback
            # For efficiency in hackathon, if target is Hindi, we'll put it in 'advice' and mobile will handle it.
            pass

        if not advice_en:
            # Always have English for fallback
            advice_en, _ = await get_advisory("English")

        if target_lang == "Marathi" and not advice_mr:
             advice_mr = advice_primary

        return {
            "advice": advice_primary,
            "advice_mr": advice_mr or advice_primary if target_lang == "Marathi" else "",
            "advice_en": advice_en,
            "priority": priority,
        }

    except Exception as e:
        logger.error(f"Weather advisory LLM call failed: {e}")
        raise


def _determine_priority(weather_data: Dict) -> str:
    rainfall = weather_data.get("rainfall_mm", 0)
    temp = weather_data.get("temperature", 25)

    if rainfall > 20 or temp > 42 or temp < 5:
        return "high"
    if rainfall > 5:
        return "medium"
    return "low"
