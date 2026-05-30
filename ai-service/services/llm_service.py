import os
import json
import logging
import yaml
from pathlib import Path
from typing import Dict

# Load .env if present
try:
    from dotenv import load_dotenv
    # Look for .env in the ai-service directory
    env_path = Path(__file__).parent.parent / '.env'
    if env_path.exists():
        load_dotenv(dotenv_path=str(env_path))
except ImportError:
    pass

from openai import OpenAI

logger = logging.getLogger(__name__)

# Load Marathi prompt templates
TEMPLATE_PATH = Path(__file__).parent.parent / "prompts" / "marathi_advisory.yaml"
with TEMPLATE_PATH.open(encoding='utf-8') as f:
    MARATHI_TEMPLATES = yaml.safe_load(f)

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

def get_advisory_prompt(crop_type: str, stage: str, language: str, question: str, weather_summary: str) -> str:
    """Generate advisory prompt from YAML template."""
    template = MARATHI_TEMPLATES['advisory']['user_prompt']
    return template.format(
        crop_type=crop_type,
        stage=stage,
        language=language,
        question=question,
        weather_summary=weather_summary
    )

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
        prompt = get_advisory_prompt(crop_type, stage, language, question, weather_summary)
        
        response = client.chat.completions.create(
            model="openai/gpt-4",  # OpenRouter model identifier
            max_tokens=500,
            messages=[{
                "role": "user",
                "content": prompt
            }]
        )

        text = response.choices[0].message.content
        
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

async def generate_timeline_data(crop_type: str, sowing_date: str, language: str = "marathi") -> Dict:
    """
    Generates a structured crop growth timeline using LLM in the specified language.
    """
    try:
        # Map common codes to full names for the LLM
        lang_map = {
            "mr": "Marathi",
            "hi": "Hindi",
            "en": "English",
            "marathi": "Marathi",
            "hindi": "Hindi",
            "english": "English"
        }
        target_lang = lang_map.get(language.lower(), language)

        prompt = f"""
        Act as an expert agronomist. Generate a structured crop growth timeline for {crop_type}
        starting from the sowing date of {sowing_date}.
        IMPORTANT: Provide all text fields (stage, description) in {target_lang}.
        Provide exactly 6 key stages from planting to market/harvest.

        For each stage, provide:
        1. stage: Short name of the stage in {target_lang}.
        2. estimated_date: The date in YYYY-MM-DD format based on the sowing date.
        3. description: A one-sentence description in {target_lang} of what happens or what the farmer should do.

        Return the result ONLY as a JSON object with a key 'stages' containing a list of these stage objects.
        """

        response = client.chat.completions.create(
            model="openai/gpt-4o-mini", # Using a faster model for structured tasks
            max_tokens=800,
            response_format={ "type": "json_object" },
            messages=[{
                "role": "user",
                "content": prompt
            }]
        )

        result = json.loads(response.choices[0].message.content)
        return result

    except Exception as e:
        logger.error(f"Timeline generation failed: {e}")
        # Fallback if LLM fails
        return {
            "stages": []
        }
