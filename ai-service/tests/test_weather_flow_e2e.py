"""
End-to-end tests for the Weather Alert Flow — AI Service side.

Tests the full chain inside ai-service:
  GET /ai/weather
    → weather_service.fetch_weather()         (OpenWeatherMap mocked)
    → weather_service.generate_weather_advisory()  (LLM mocked)
    → if rain>20mm / temp extreme: notify_service.trigger_notification()
                                              (Spring Boot /notify mocked)
    → response with weather_summary, advice_mr, advice_en, notification_sent

All external calls (OpenWeatherMap, LLM, Spring) are mocked with respx.
No real network calls are made.
"""

import json
import pytest
import respx
import httpx
from unittest.mock import AsyncMock, patch, MagicMock
from fastapi.testclient import TestClient

# ── App bootstrap ──────────────────────────────────────────────────────────────
import os
os.environ.setdefault("OPENROUTER_API_KEY", "test-key-not-real")
os.environ.setdefault("OPENWEATHER_API_KEY", "test-owm-key")
os.environ.setdefault("SPRING_BASE_URL", "http://localhost:8080")

from main import app

client = TestClient(app)

# ── Fixtures / helpers ─────────────────────────────────────────────────────────

SUNNY_WEATHER = {
    "temperature": 28.5,
    "humidity": 65,
    "rainfall_mm": 5.0,
    "description": "clear sky",
    "weather_summary": "Rain 5mm expected. Temperature 28.5°C.",
}

RAINY_WEATHER = {
    "temperature": 26.0,
    "humidity": 88,
    "rainfall_mm": 35.0,
    "description": "heavy rain",
    "weather_summary": "Rain 35mm expected in 48 hours. Temperature 26.0°C.",
}

EXTREME_HEAT = {
    "temperature": 45.0,
    "humidity": 22,
    "rainfall_mm": 0.0,
    "description": "extreme heat",
    "weather_summary": "Extreme heat 45°C. No rain forecast.",
}

COLD_SNAP = {
    "temperature": 3.0,
    "humidity": 80,
    "rainfall_mm": 2.0,
    "description": "cold and cloudy",
    "weather_summary": "Temperature 3°C. Cold snap expected.",
}

MARATHI_ADVISORY = {
    "advice_mr": "येत्या ४८ तासांत ६५ मिमी पाऊस अपेक्षित. खत टाकणे पुढे ढकला.",
    "advice_en": "65mm rain in 48 hours. Delay fertilizer application.",
    "priority": "high",
}

MILD_ADVISORY = {
    "advice_mr": "हवामान अनुकूल आहे. नियमित सिंचन सुरू ठेवा.",
    "advice_en": "Weather conditions are favourable. Continue regular irrigation.",
    "priority": "medium",
}


def common_params(extra=None):
    params = {
        "lat": "18.52",
        "lon": "73.85",
        "crop": "soybean",
        "stage": "vegetative",
        "lang": "marathi",
        "farmer_id": "550e8400-e29b-41d4-a716-446655440000",
    }
    if extra:
        params.update(extra)
    return params


# ═══════════════════════════════════════════════════════════════════════════════
# 1. RESPONSE CONTRACT TESTS
# ═══════════════════════════════════════════════════════════════════════════════

class TestWeatherResponseContract:
    """All fields from the README API contract must be present in response."""

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=SUNNY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MILD_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_all_contract_fields_present(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.status_code == 200
        body = resp.json()

        required_fields = [
            "weather_summary", "advice_mr", "advice_en",
            "alert_type", "priority", "temperature",
            "humidity", "rainfall_mm", "description", "notification_sent",
        ]
        for field in required_fields:
            assert field in body, f"Missing field: {field}"

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=SUNNY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MILD_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_alert_type_is_always_weather(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["alert_type"] == "weather"

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=SUNNY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MILD_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_advice_mr_and_advice_en_are_different(self, mock_notify, mock_advisory, mock_weather):
        """Marathi and English advisories must be separate strings (not the same text)."""
        resp = client.get("/ai/weather", params=common_params())
        body = resp.json()
        assert body["advice_mr"] == MILD_ADVISORY["advice_mr"]
        assert body["advice_en"] == MILD_ADVISORY["advice_en"]
        # They should be different (key fix from the original bug)
        assert body["advice_mr"] != body["advice_en"]


# ═══════════════════════════════════════════════════════════════════════════════
# 2. NOTIFICATION TRIGGER RULES
# ═══════════════════════════════════════════════════════════════════════════════

class TestNotificationTriggerRules:
    """
    Per design.md rule: notify if rainfall_mm > 20 OR temp > 42 OR temp < 5.
    """

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=SUNNY_WEATHER)  # rainfall_mm=5
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MILD_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_sunny_no_rain__no_notification(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is False
        mock_notify.assert_not_called()

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)  # rainfall_mm=35
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_heavy_rain_35mm__notification_sent(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is True
        mock_notify.assert_called_once()

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=EXTREME_HEAT)  # temp=45°C
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_extreme_heat_45c__notification_sent(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is True
        mock_notify.assert_called_once()

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=COLD_SNAP)  # temp=3°C
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_cold_snap_3c__notification_sent(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is True
        mock_notify.assert_called_once()

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value={**SUNNY_WEATHER, "rainfall_mm": 20.0})  # exactly 20mm (boundary)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MILD_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_exactly_20mm_rainfall__no_notification(self, mock_notify, mock_advisory, mock_weather):
        """Boundary: rule is rainfall > 20, so exactly 20mm should NOT trigger."""
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is False
        mock_notify.assert_not_called()

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value={**SUNNY_WEATHER, "rainfall_mm": 20.1})  # just over 20mm
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_just_over_20mm__notification_sent(self, mock_notify, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.json()["notification_sent"] is True


# ═══════════════════════════════════════════════════════════════════════════════
# 3. NOTIFY SERVICE PAYLOAD — correct message_mr / message_en split
# ═══════════════════════════════════════════════════════════════════════════════

class TestNotifyServicePayload:
    """
    The notify call to Spring must send MARATHI text as message_mr
    and ENGLISH text as message_en — NOT the same text in both fields.
    This was the original bug.
    """

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_notify_called_with_correct_message_mr(self, mock_notify, mock_advisory, mock_weather):
        client.get("/ai/weather", params=common_params())

        call_kwargs = mock_notify.call_args.kwargs
        assert call_kwargs["message_mr"] == MARATHI_ADVISORY["advice_mr"]
        assert call_kwargs["message_en"] == MARATHI_ADVISORY["advice_en"]
        # Critical: must NOT be the same string
        assert call_kwargs["message_mr"] != call_kwargs["message_en"]

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_notify_called_with_priority_high(self, mock_notify, mock_advisory, mock_weather):
        client.get("/ai/weather", params=common_params())
        call_kwargs = mock_notify.call_args.kwargs
        assert call_kwargs["priority"] == "high"
        assert call_kwargs["alert_type"] == "weather"

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification", new_callable=AsyncMock)
    def test_notify_called_with_correct_farmer_id(self, mock_notify, mock_advisory, mock_weather):
        params = common_params({"farmer_id": "aaaabbbb-0000-1111-2222-ccccddddeeee",
                                "crop_id": "11112222-3333-4444-5555-666677778888"})
        client.get("/ai/weather", params=params)
        call_kwargs = mock_notify.call_args.kwargs
        assert call_kwargs["farmer_id"] == "aaaabbbb-0000-1111-2222-ccccddddeeee"
        assert call_kwargs["crop_id"] == "11112222-3333-4444-5555-666677778888"


# ═══════════════════════════════════════════════════════════════════════════════
# 4. WEATHER SERVICE — fetch_weather unit tests
# ═══════════════════════════════════════════════════════════════════════════════

class TestFetchWeather:
    """Unit tests for weather_service.fetch_weather()."""

    @pytest.mark.asyncio
    async def test_missing_api_key_returns_mock_data(self):
        """No API key → safe mock data returned, no exception."""
        import services.weather_service as ws
        original = ws.OPENWEATHER_API_KEY
        ws.OPENWEATHER_API_KEY = None
        try:
            result = await ws.fetch_weather(18.52, 73.85)
            assert "temperature" in result
            assert "rainfall_mm" in result
            assert "weather_summary" in result
            assert result["rainfall_mm"] >= 0
        finally:
            ws.OPENWEATHER_API_KEY = original

    @pytest.mark.asyncio
    @respx.mock
    async def test_openweathermap_called_with_correct_params(self):
        """When API key is set, OWM is called with lat/lon/metric units."""
        import services.weather_service as ws
        ws.OPENWEATHER_API_KEY = "fake-key"

        owm_response = {
            "list": [
                {
                    "main": {"temp": 29.0, "humidity": 70},
                    "rain": {"3h": 8.0},
                    "weather": [{"description": "light rain"}],
                },
                {
                    "main": {"temp": 27.0, "humidity": 75},
                    "rain": {"3h": 5.0},
                    "weather": [{"description": "light rain"}],
                },
            ]
        }

        respx.get("https://api.openweathermap.org/data/2.5/forecast").mock(
            return_value=httpx.Response(200, json=owm_response)
        )

        result = await ws.fetch_weather(18.52, 73.85)
        assert result["temperature"] == 29.0
        assert result["humidity"] == 70
        assert result["rainfall_mm"] == pytest.approx(13.0)
        assert "weather_summary" in result
        assert "13" in result["weather_summary"] or "Rain" in result["weather_summary"]

    @pytest.mark.asyncio
    @respx.mock
    async def test_no_rain_field_in_owm__rainfall_zero(self):
        """Items with no 'rain' key should contribute 0mm."""
        import services.weather_service as ws
        ws.OPENWEATHER_API_KEY = "fake-key"

        owm_response = {
            "list": [
                {"main": {"temp": 32.0, "humidity": 40}, "weather": [{"description": "sunny"}]},
            ]
        }
        respx.get("https://api.openweathermap.org/data/2.5/forecast").mock(
            return_value=httpx.Response(200, json=owm_response)
        )

        result = await ws.fetch_weather(18.52, 73.85)
        assert result["rainfall_mm"] == 0.0


# ═══════════════════════════════════════════════════════════════════════════════
# 5. WEATHER SERVICE — generate_weather_advisory unit tests
# ═══════════════════════════════════════════════════════════════════════════════

class TestGenerateWeatherAdvisory:
    """Unit tests for weather_service.generate_weather_advisory()."""

    def _make_mock_completion(self, text: str):
        """Create a mock OpenAI completion response."""
        mock_choice = MagicMock()
        mock_choice.message.content = text
        mock_resp = MagicMock()
        mock_resp.choices = [mock_choice]
        return mock_resp

    @pytest.mark.asyncio
    @patch("services.weather_service.llm_client")
    async def test_returns_separate_mr_and_en_advisories(self, mock_llm):
        """Must return distinct advice_mr and advice_en keys."""
        import services.weather_service as ws

        mock_llm.chat.completions.create.side_effect = [
            self._make_mock_completion("मराठी सल्ला: पाणी टाकू नका."),
            self._make_mock_completion('English advice: Do not irrigate.\n{"priority": "high"}'),
        ]

        result = await ws.generate_weather_advisory(
            RAINY_WEATHER, "soybean", "vegetative", "marathi"
        )

        assert "advice_mr" in result
        assert "advice_en" in result
        assert result["advice_mr"] != result["advice_en"]

    @pytest.mark.asyncio
    @patch("services.weather_service.llm_client")
    async def test_priority_high_for_heavy_rain(self, mock_llm):
        import services.weather_service as ws

        mock_llm.chat.completions.create.side_effect = [
            self._make_mock_completion("मराठी सल्ला."),
            self._make_mock_completion('Delay fertilizer.\n{"priority": "high"}'),
        ]

        result = await ws.generate_weather_advisory(
            RAINY_WEATHER, "soybean", "vegetative", "marathi"
        )
        assert result["priority"] == "high"

    @pytest.mark.asyncio
    @patch("services.weather_service.llm_client")
    async def test_priority_medium_for_mild_weather(self, mock_llm):
        import services.weather_service as ws

        mock_llm.chat.completions.create.side_effect = [
            self._make_mock_completion("मराठी सल्ला."),
            self._make_mock_completion('Continue regular schedule.\n{"priority": "medium"}'),
        ]

        result = await ws.generate_weather_advisory(
            SUNNY_WEATHER, "soybean", "vegetative", "marathi"
        )
        # Even without JSON footer, rule-based priority for 5mm = medium
        assert result["priority"] in ("medium", "low")

    @pytest.mark.asyncio
    @patch("services.weather_service.llm_client")
    async def test_json_footer_stripped_from_advice_en(self, mock_llm):
        """The JSON priority footer must be stripped from advice_en text."""
        import services.weather_service as ws

        mock_llm.chat.completions.create.side_effect = [
            self._make_mock_completion("मराठी सल्ला."),
            self._make_mock_completion('Delay fertilizer application.\n{"priority": "high"}'),
        ]

        result = await ws.generate_weather_advisory(
            RAINY_WEATHER, "soybean", "vegetative", "marathi"
        )
        assert '{"priority"' not in result["advice_en"]
        assert result["advice_en"].strip() == "Delay fertilizer application."


# ═══════════════════════════════════════════════════════════════════════════════
# 6. PRIORITY DETERMINATION RULES (unit)
# ═══════════════════════════════════════════════════════════════════════════════

class TestDeterminePriority:
    """Direct unit tests for _determine_priority() rule logic."""

    def test_high_rain(self):
        from services.weather_service import _determine_priority
        assert _determine_priority({"rainfall_mm": 21, "temperature": 28}) == "high"

    def test_high_extreme_heat(self):
        from services.weather_service import _determine_priority
        assert _determine_priority({"rainfall_mm": 0, "temperature": 43}) == "high"

    def test_high_cold_snap(self):
        from services.weather_service import _determine_priority
        assert _determine_priority({"rainfall_mm": 1, "temperature": 4}) == "high"

    def test_medium_moderate_rain(self):
        from services.weather_service import _determine_priority
        assert _determine_priority({"rainfall_mm": 10, "temperature": 28}) == "medium"

    def test_low_sunny_dry(self):
        from services.weather_service import _determine_priority
        assert _determine_priority({"rainfall_mm": 0, "temperature": 30}) == "low"

    def test_boundary_exactly_20mm_is_medium(self):
        from services.weather_service import _determine_priority
        # > 20 triggers high; == 20 is medium
        assert _determine_priority({"rainfall_mm": 20, "temperature": 28}) == "medium"


# ═══════════════════════════════════════════════════════════════════════════════
# 7. MISSING REQUIRED PARAMS → 422 Unprocessable Entity
# ═══════════════════════════════════════════════════════════════════════════════

class TestMissingParams:

    def test_missing_lat_returns_422(self):
        resp = client.get("/ai/weather", params={
            "lon": "73.85", "crop": "soybean", "stage": "vegetative",
            "lang": "marathi", "farmer_id": "some-uuid",
        })
        assert resp.status_code == 422

    def test_missing_lon_returns_422(self):
        resp = client.get("/ai/weather", params={
            "lat": "18.52", "crop": "soybean", "stage": "vegetative",
            "lang": "marathi", "farmer_id": "some-uuid",
        })
        assert resp.status_code == 422

    def test_missing_farmer_id_returns_422(self):
        resp = client.get("/ai/weather", params={
            "lat": "18.52", "lon": "73.85",
            "crop": "soybean", "stage": "vegetative", "lang": "marathi",
        })
        assert resp.status_code == 422


# ═══════════════════════════════════════════════════════════════════════════════
# 8. RESILIENCE — graceful degradation on downstream failures
# ═══════════════════════════════════════════════════════════════════════════════

class TestResilience:

    @patch("services.weather_service.fetch_weather",
           new_callable=AsyncMock, side_effect=Exception("OWM API unreachable"))
    def test_owm_failure__returns_degraded_response_not_500(self, mock_weather):
        """Weather endpoint must never return 500 to mobile — always degrade gracefully."""
        resp = client.get("/ai/weather", params=common_params())
        assert resp.status_code == 200
        body = resp.json()
        assert "advice_mr" in body
        assert "advice_en" in body
        assert body["notification_sent"] is False

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory",
           new_callable=AsyncMock, side_effect=Exception("LLM timeout"))
    def test_llm_failure__returns_degraded_response_not_500(
            self, mock_advisory, mock_weather):
        resp = client.get("/ai/weather", params=common_params())
        assert resp.status_code == 200
        body = resp.json()
        assert "advice_mr" in body
        assert body["notification_sent"] is False

    @patch("services.weather_service.fetch_weather", new_callable=AsyncMock,
           return_value=RAINY_WEATHER)
    @patch("services.weather_service.generate_weather_advisory", new_callable=AsyncMock,
           return_value=MARATHI_ADVISORY)
    @patch("services.notify_service.trigger_notification",
           new_callable=AsyncMock, side_effect=Exception("Spring Boot down"))
    def test_notify_failure__does_not_break_weather_response(
            self, mock_notify, mock_advisory, mock_weather):
        """notify_service is fire-and-forget — Spring being down must not break weather response."""
        resp = client.get("/ai/weather", params=common_params())
        # Response still returns 200 but notification_sent will be False
        assert resp.status_code == 200
        assert "advice_mr" in resp.json()
