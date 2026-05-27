"""
Pytest configuration for ai-service tests.
Sets required env vars before any module-level code runs.
"""
import os

# Must be set before importing main.py or llm_service.py
os.environ.setdefault("OPENROUTER_API_KEY", "test-key-not-real")
os.environ.setdefault("OPENWEATHER_API_KEY", "test-owm-key")
os.environ.setdefault("SPRING_BASE_URL", "http://localhost:8080")
