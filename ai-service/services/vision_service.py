import anthropic
import base64
import os
import json
import logging
from typing import Dict

logger = logging.getLogger(__name__)

client = anthropic.Anthropic(api_key=os.getenv("ANTHROPIC_API_KEY"))

DISEASE_PROMPT = """
You are an expert plant pathologist for Indian agriculture (Maharashtra region).
Crop type: {crop_type}
Analyze this leaf image and respond ONLY in valid JSON (no markdown, no explanation):

{{
  "disease": "English disease name",
  "disease_mr": "रोगाचे नाव मराठीत",
  "confidence": 0.85,
  "severity": "high",
  "cause": "Brief cause",
  "remedy_en": "Specific remedy with dosage e.g. 5ml Profenofos per litre, spray in evening",
  "remedy_mr": "मराठीत उपाय (specific with dosage)",
  "consult_expert": true
}}

Rules:
- confidence: 0.0 to 1.0
- severity: low | medium | high
- If unclear image, set confidence < 0.5
- Never recommend unsafe chemical quantities
- Be specific about timing and dosage
"""

async def analyze_disease(
    image_bytes: bytes,
    crop_type: str,
    language: str = "marathi"
) -> Dict:
    """
    Analyze plant disease using Claude Vision API.
    Returns structured disease diagnosis with remedies.
    """
    try:
        b64 = base64.standard_b64encode(image_bytes).decode("utf-8")

        response = client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=1000,
            messages=[{
                "role": "user",
                "content": [
                    {
                        "type": "image",
                        "source": {
                            "type": "base64",
                            "media_type": "image/jpeg",
                            "data": b64
                        }
                    },
                    {
                        "type": "text",
                        "text": DISEASE_PROMPT.format(crop_type=crop_type)
                    }
                ]
            }]
        )

        # Parse JSON response
        result = json.loads(response.content[0].text)
        logger.info(f"Disease analysis complete: {result.get('disease')} (confidence: {result.get('confidence')})")
        
        return result

    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse vision response: {e}")
        return {
            "disease": "Analysis Error",
            "disease_mr": "विश्लेषण त्रुटी",
            "confidence": 0.0,
            "severity": "low",
            "cause": "Unable to analyze image",
            "remedy_en": "Please try again with a clearer image",
            "remedy_mr": "कृपया स्पष्ट प्रतिमेसह पुन्हा प्रयत्न करा",
            "consult_expert": True
        }
    except Exception as e:
        logger.error(f"Vision analysis failed: {e}")
        raise
