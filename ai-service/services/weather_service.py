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

# Load Marathi advisory YAML templates (same pattern as llm_service.py)
_TEMPLATE_PATH = Path(__file__).parent.parent / "prompts" / "marathi_advisory.yaml"
with _TEMPLATE_PATH.open(encoding="utf-8") as _f:
    _TEMPLATES = yaml.safe_load(_f)

# Import the shared OpenRouter client (do NOT use anthropic SDK directly — project uses OpenRouter)
from services.llm_service import client as llm_client


async def fetch_weather(lat: float, lon: float) -> Dict:
    """
    Fetch 48-hour weather forecast from OpenWeatherMap.
    Falls back to mock data if API key is not set (safe for demo/hackathon).
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

            # Sum all rainfall across 48h forecast window
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

    except httpx.HTTPStatusError as e:
        logger.error(f"OpenWeatherMap API error: {e.response.status_code} — {e.response.text}")
        raise
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
    Generate localised weather advisory using the shared OpenRouter LLM client.

    Returns:
        advice_mr  — Marathi advisory text
        advice_en  — English advisory text
        priority   — "high" | "medium" | "low"
    """
    forecast_summary = (
        f"Temperature: {weather_data['temperature']}°C, "
        f"Humidity: {weather_data['humidity']}%, "
        f"Rainfall (48h): {weather_data['rainfall_mm']}mm, "
        f"Conditions: {weather_data.get('description', 'N/A')}"
    )

    # ── Marathi advisory (uses YAML template) ────────────────────────────────
    marathi_template = _TEMPLATES["weather_advisory"]["user_prompt"]
    marathi_prompt = marathi_template.format(
        crop_type=crop_type,
        stage=stage,
        forecast=forecast_summary,
    )

    # ── English advisory (inline prompt) ─────────────────────────────────────
    english_prompt = (
        f"You are an expert agricultural advisor for Indian farmers.\n"
        f"Crop: {crop_type} (stage: {stage})\n"
        f"Weather forecast: {forecast_summary}\n\n"
        f"Give practical farming advice in English (2-3 sentences).\n"
        f"Focus on: irrigation, fertilizer timing, disease risk.\n"
        f"End with JSON on the last line: "
        f'{{\"priority\": \"high|medium|low\"}}'
    )

    try:
        # ── Marathi call ──────────────────────────────────────────────────────
        mr_response = llm_client.chat.completions.create(
            model="gpt-4",
            max_tokens=400,
            messages=[
                {
                    "role": "system",
                    "content": _TEMPLATES["weather_advisory"]["system"],
                },
                {"role": "user", "content": marathi_prompt},
            ],
        )
        advice_mr = mr_response.choices[0].message.content.strip()

        # ── English call ──────────────────────────────────────────────────────
        en_response = llm_client.chat.completions.create(
            model="gpt-4",
            max_tokens=300,
            messages=[{"role": "user", "content": english_prompt}],
        )
        en_text = en_response.choices[0].message.content.strip()

        # Extract JSON priority footer from English response if present
        advice_en = en_text
        priority = _determine_priority(weather_data)

        lines = en_text.split("\n")
        try:
            last_line = lines[-1].strip()
            if last_line.startswith("{"):
                parsed = json.loads(last_line)
                priority = parsed.get("priority", priority)
                advice_en = "\n".join(lines[:-1]).strip()
        except (json.JSONDecodeError, IndexError):
            pass

        return {
            "advice_mr": advice_mr,
            "advice_en": advice_en,
            "priority": priority,
        }

    except Exception as e:
        logger.error(f"Weather advisory LLM call failed: {e}")
        raise


def _determine_priority(weather_data: Dict) -> str:
    """Rule-based priority matching README spec."""
    rainfall = weather_data.get("rainfall_mm", 0)
    temp = weather_data.get("temperature", 25)

    if rainfall > 20 or temp > 42 or temp < 5:
        return "high"
    if rainfall > 5:
        return "medium"
    return "low"
