import anthropic
import os
import json
import logging
from typing import Dict

logger = logging.getLogger(__name__)

client = anthropic.Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))

ADVISORY_PROMPT = """
You are KrishiMitra AI, an expert agricultural advisor for Indian farmers.
Always respond in {language} (Marathi/Hindi/English as specified).
Be concise, practical, and use simple language farmers understand.
Do NOT give unsafe advice. Recommend expert consultation for serious disease.

Context:
- Crop: {crop_type}
- Stage: {stage}
- Location: Maharashtra
- Weather: {weather_summary}

Farmer's question: {question}

Respond in 3-4 sentences. Include:
1. What the problem likely is
2. Specific action (e.g. "5kg urea per acre")
3. When to act (timeline)

Also return a JSON footer on the last line:
{{"alert_type": "fertilizer|disease|irrigation|weather|market", "priority": "high|medium|low"}}
"""

async def get_crop_advisory(
    crop_type: str,
    stage: str,
    language: str,
    question: str,
    weather_summary: str = "No weather data available"
) -> Dict:
    """
    Get LLM-powered crop advisory in the specified language.
    Returns advice text and alert classification.
    """
    try:
        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=500,
            messages=[{
                "role": "user",
                "content": ADVISORY_PROMPT.format(
                    language=language,
                    crop_type=crop_type,
                    stage=stage,
                    weather_summary=weather_summary,
                    question=question
                )
            }]
        )

        text = response.content[0].text
        
        # Extract JSON footer if present
        lines = text.strip().split('\n')
        advice_text = text
        alert_info = {"alert_type": "general", "priority": "low"}
        
        try:
            # Try to parse last line as JSON
            last_line = lines[-1].strip()
            if last_line.startswith('{'):
                alert_info = json.loads(last_line)
                advice_text = '\n'.join(lines[:-1]).strip()
        except:
            pass

        return {
            "advice": advice_text,
            "alert_type": alert_info.get("alert_type", "general"),
            "priority": alert_info.get("priority", "low")
        }

    except Exception as e:
        logger.error(f"LLM advisory failed: {e}")
        raise
