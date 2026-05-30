import base64
import os
import json
import logging
from typing import Dict
from openai import OpenAI
import re

logger = logging.getLogger(__name__)

# OpenRouter client configured with Claude model
# Initialise OpenRouter client. If the API key is missing, raise a clear error.
api_key = os.getenv("OPENROUTER_API_KEY")
if not api_key:
    raise EnvironmentError("OPENROUTER_API_KEY not set in environment. Please add it to your .env file.")
client = OpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=api_key,
    default_headers={
        "HTTP-Referer": os.getenv("SITE_URL", "https://krishimitra.app"),
        "X-Title": os.getenv("SITE_NAME", "KrishiMitra AI"),
    }
)

DISEASE_PROMPT = """
You are a senior plant pathologist and agricultural extension expert specializing in Maharashtra, India.
Your diagnosis directly affects a farmer's livelihood, income, and crop survival.
A wrong diagnosis can lead to crop loss, wasted money on incorrect pesticides, and financial hardship.
Therefore, accuracy, honesty about uncertainty, and safe recommendations are paramount.

Crop type: {crop_type}

Analyze the uploaded leaf image carefully. Consider:
- Common diseases affecting this crop in Maharashtra's agro-climatic zones (Vidarbha, Marathwada, Konkan, Western Ghats, Pune plateau)
- Seasonal context: Kharif (June–Oct), Rabi (Nov–Mar), Summer crops
- Symptoms visible in the image: lesion shape/color, pattern spread, necrosis, chlorosis, fungal bodies, water-soaking
- Diseases that are visually similar (differential diagnosis) to avoid misidentification
- Whether the image quality is sufficient to make a confident diagnosis

Verification standard before responding:
- Is the symptom pattern consistent with the named disease? (leaf spot pattern, color, margin definition)
- Is this disease known to occur in Maharashtra for this crop?
- Does severity match what is visible in the image?
- Are the remedies you suggest approved and available in India (CIB&RC registered)?
- Are dosages within safe limits as per label recommendations?
- If two diseases look similar, lower confidence and note ambiguity in cause field

Respond ONLY in valid JSON (no markdown, no explanation):

{{
  "disease": "English disease name",
  "disease_mr": "Disease name in {target_lang}",
  "confidence": 0.85,
  "severity": "high",
  "cause_en": "Brief cause in English",
  "cause_mr": "Brief cause in {target_lang}",
  "remedy_en": "Specific remedy with dosage in English e.g. 5ml Profenofos per litre, spray in evening",
  "remedy_mr": "Specific remedy with dosage in {target_lang}",
  "consult_expert": true
}}

Rules:
- confidence: 0.0 to 1.0
- severity: low | medium | high
- If image is unclear, blurry, or symptom is ambiguous, set confidence < 0.5
- If two diseases match equally, pick the more common one for Maharashtra and lower confidence to ≤ 0.6
- Never recommend unsafe chemical quantities; follow CIB&RC label rates
- Remedies must be India-registered products available in local agro-shops
- Include timing (morning/evening spray), water volume, and repeat interval in remedy
- Set consult_expert: true whenever confidence < 0.7 or disease is uncommon
- Be specific about timing and dosage — a farmer's crop depends on this advice
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

        # Map language codes to names for the AI
        lang_map = {
            "mr": "Marathi",
            "hi": "Hindi",
            "en": "English",
            "marathi": "Marathi",
            "hindi": "Hindi",
            "english": "English"
        }
        target_lang = lang_map.get(language.lower(), language)

        system_msg = f"You are a plant pathology expert. You must provide disease analysis in BOTH English and {target_lang}. It is CRITICAL that 'disease_mr', 'cause_mr', and 'remedy_mr' are written entirely in {target_lang} script. NEVER copy English text into these fields."

        response = client.chat.completions.create(
            model="anthropic/claude-sonnet-4-5",
            max_tokens=1500,
            messages=[
                {
                    "role": "system",
                    "content": system_msg
                },
                {
                    "role": "user",
                    "content": [
                        {
                            "type": "image_url",
                            "image_url": {
                                "url": f"data:image/jpeg;base64,{b64}"
                            }
                        },
                        {
                            "type": "text",
                            "text": DISEASE_PROMPT.format(crop_type=crop_type, target_lang=target_lang)
                        }
                    ]
                }
            ]
        )

        # Parse JSON response robustly
        # The LLM may wrap the JSON in markdown fences or include extra text.
        # Extract the JSON block using regex and strip any leftover markdown.
        raw_content = response.choices[0].message.content
        print(raw_content)
        if isinstance(raw_content, str):
            # Remove possible markdown code fences (``` or ```json)
            cleaned = re.sub(r"```(?:json)?\s*|```", "", raw_content, flags=re.IGNORECASE).strip()
            # Find the first JSON object
            match = re.search(r"\{.*\}", cleaned, re.DOTALL)
            json_str = match.group(0) if match else cleaned
            print("after cleaning ")
            print(json_str)
        else:
            print("coming json format directly --")
            json_str = str(raw_content)
        result = json.loads(json_str)
        logger.info(f"Disease analysis complete: {result.get('disease')} (confidence: {result.get('confidence')})")
        
        return result

    except json.JSONDecodeError as e:
        logger.error(f"Failed to parse vision response: {e}")
        return {
            "disease": "Analysis Error",
            "disease_mr": "विश्लेषण त्रुटी",
            "confidence": 0.0,
            "severity": "low",
            "cause_en": "Unable to analyze image",
            "cause_mr": "कृपया स्पष्ट प्रतिमेसह पुन्हा प्रयत्न करा",
            "remedy_en": "Please try again with a clearer image",
            "remedy_mr": "कृपया स्पष्ट प्रतिमेसह पुन्हा प्रयत्न करा",
            "consult_expert": True
        }
    except Exception as e:
        logger.error(f"Vision analysis failed: {e}")
        raise
