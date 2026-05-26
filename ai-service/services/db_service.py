"""
Database service for direct PostgreSQL access from Python AI.
Used by the AI service to read/write enriched data directly to Postgres.
"""
import os
import logging
from typing import Optional, Dict, List
import asyncpg
from contextlib import asynccontextmanager

logger = logging.getLogger(__name__)

DB_URL = os.getenv("DB_URL", "postgresql://krishi:krishi123@localhost:5432/krishimitra")

_pool: Optional[asyncpg.Pool] = None


async def get_pool() -> asyncpg.Pool:
    """Get or create the asyncpg connection pool."""
    global _pool
    if _pool is None:
        _pool = await asyncpg.create_pool(DB_URL, min_size=2, max_size=10)
        logger.info("Database connection pool created")
    return _pool


async def close_pool():
    """Close the database connection pool."""
    global _pool
    if _pool:
        await _pool.close()
        _pool = None
        logger.info("Database connection pool closed")


async def get_farmer_farms(farmer_id: str) -> List[Dict]:
    """Get all farms for a farmer with their crops."""
    pool = await get_pool()
    async with pool.acquire() as conn:
        rows = await conn.fetch("""
            SELECT 
                f.id as farm_id, f.name as farm_name,
                f.latitude, f.longitude, f.area_acres, f.soil_type,
                c.id as crop_id, c.crop_type, c.stage, c.sowing_date
            FROM farms f
            LEFT JOIN crops c ON c.farm_id = f.id
            WHERE f.farmer_id = $1::uuid
        """, farmer_id)
        
        farms = {}
        for row in rows:
            fid = str(row['farm_id'])
            if fid not in farms:
                farms[fid] = {
                    "farm_id": fid,
                    "name": row['farm_name'],
                    "latitude": float(row['latitude']) if row['latitude'] else None,
                    "longitude": float(row['longitude']) if row['longitude'] else None,
                    "area_acres": float(row['area_acres']) if row['area_acres'] else None,
                    "soil_type": row['soil_type'],
                    "crops": []
                }
            if row['crop_id']:
                farms[fid]['crops'].append({
                    "crop_id": str(row['crop_id']),
                    "crop_type": row['crop_type'],
                    "stage": row['stage'],
                    "sowing_date": str(row['sowing_date']) if row['sowing_date'] else None
                })
        
        return list(farms.values())


async def get_farmer_fcm_token(farmer_id: str) -> Optional[str]:
    """Get the FCM token for a farmer."""
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow(
            "SELECT fcm_token FROM farmers WHERE id = $1::uuid",
            farmer_id
        )
        return row['fcm_token'] if row else None


async def save_disease_report(
    farmer_id: str,
    crop_id: Optional[str],
    diagnosis: str,
    diagnosis_mr: str,
    confidence: float,
    remedy_en: str,
    remedy_mr: str,
    severity: str,
    image_url: Optional[str] = None
) -> str:
    """Save a disease report and return its ID."""
    pool = await get_pool()
    async with pool.acquire() as conn:
        row = await conn.fetchrow("""
            INSERT INTO disease_reports (farmer_id, crop_id, image_url, diagnosis, 
                                         diagnosis_mr, confidence, remedy_en, remedy_mr, severity)
            VALUES ($1::uuid, $2::uuid, $3, $4, $5, $6, $7, $8, $9)
            RETURNING id
        """, farmer_id, crop_id, image_url, diagnosis, diagnosis_mr,
             confidence, remedy_en, remedy_mr, severity)
        
        report_id = str(row['id'])
        logger.info(f"Disease report saved: {report_id}")
        return report_id