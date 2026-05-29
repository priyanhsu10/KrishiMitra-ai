from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import logging

from routers import disease, advisory, weather, mandi, crop

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="KrishiMitra AI Service",
    description="AI-powered agricultural advisory service",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(disease.router, prefix="/ai", tags=["disease"])
app.include_router(advisory.router, prefix="/ai", tags=["advisory"])
app.include_router(weather.router, prefix="/ai", tags=["weather"])
app.include_router(mandi.router, prefix="/ai", tags=["mandi"])
app.include_router(crop.router, prefix="/ai", tags=["crop"])

@app.get("/health")
async def health_check():
    return {"status": "healthy", "service": "krishimitra-ai"}

if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
