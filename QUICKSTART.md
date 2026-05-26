# 🚀 KrishiMitra — 5-Minute Quickstart

## Prerequisites
- Docker & Docker Compose installed
- Node.js 18+ (for mobile dev only)

---

## Step 1: Get API Keys (2 min)

### Anthropic API Key
1. Go to: https://console.anthropic.com
2. Create account → API Keys → Create Key
3. Copy key

### OpenWeather API Key
1. Go to: https://openweathermap.org/api
2. Sign up → API Keys
3. Copy key (free tier is sufficient)

---

## Step 2: Configure Environment (1 min)

```bash
# Create .env file
cp .env.example .env

# Edit .env and paste your keys
nano .env  # or use your favorite editor
```

Example `.env`:
```
ANTHROPIC_API_KEY=sk-ant-api03-...
OPENWEATHER_API_KEY=abc123...
```

---

## Step 3: Start Services (2 min)

```bash
# Build and start all services
docker-compose up --build

# Wait for services to start (30-60 seconds)
# You should see:
# ✅ krishimitra-postgres    | ready to accept connections
# ✅ krishimitra-backend     | Started KrishiMitraApplication
# ✅ krishimitra-ai          | Uvicorn running on http://0.0.0.0:8000
```

---

## Step 4: Verify (30 sec)

```bash
# Test AI service
curl http://localhost:8000/health
# Expected: {"status":"healthy","service":"krishimitra-ai"}

# Test backend (will fail without Firebase config, but shows it's running)
curl http://localhost:8080/api/v1/auth/login -X POST \
  -H "Content-Type: application/json" \
  -d '{"mobile":"9876543210"}'
```

---

## ⚠️ Firebase Setup (Required for FCM)

**The services are running, but notifications won't work until you add Firebase credentials.**

Follow detailed instructions in `README_SETUP.md` Step 1.

Quick version:
1. Create Firebase project at https://console.firebase.google.com
2. Download `google-services.json` → `mobile/android/app/`
3. Download `krishimitra-firebase-adminsdk.json` → `backend/src/main/resources/`
4. Restart backend: `docker-compose restart spring-backend`

---

## 🧪 Test Disease Detection

```bash
# Assuming you have a leaf image
curl -X POST http://localhost:8000/ai/disease-detect \
  -F "file=@path/to/leaf.jpg" \
  -F "crop_type=soybean" \
  -F "language=marathi" \
  -F "farmer_id=123e4567-e89b-12d3-a456-426614174000"

# Expected: JSON with disease diagnosis in Marathi
```

---

## 📱 Mobile App Setup

```bash
cd mobile

# Install dependencies
npm install

# Start Expo
npm start

# Run on Android
npm run android

# Run on iOS
npm run ios
```

**Note**: You'll need `google-services.json` for the app to build successfully.

---

## 🐛 Common Issues

### "Firebase initialization failed"
- Ensure `krishimitra-firebase-adminsdk.json` is in `backend/src/main/resources/`
- Restart backend: `docker-compose restart spring-backend`

### "Permission denied" on API calls
- Security is disabled for `/auth/*` endpoints
- Other endpoints need JWT (not implemented yet for hackathon MVP)
- For testing, temporarily open all endpoints in `SecurityConfig.java`

### "Connection refused" to database
- Wait 10 seconds for PostgreSQL to fully start
- Check logs: `docker-compose logs postgres`

---

## 📊 Accessing Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Backend API | http://localhost:8080 | N/A |
| AI Service | http://localhost:8000 | N/A |
| AI Docs | http://localhost:8000/docs | N/A |
| PostgreSQL | localhost:5432 | User: `krishi`, Pass: `krishi123`, DB: `krishimitra` |

---

## 🎯 Next Steps

1. ✅ Services running
2. ⏩ Add Firebase config (see `README_SETUP.md`)
3. ⏩ Develop missing CRUD endpoints (see `TASKS.md`)
4. ⏩ Build mobile screens (see `TASKS.md`)
5. ⏩ Test end-to-end flow

---

## 📚 Documentation

- **Full Architecture**: `README.md`
- **Setup Guide**: `README_SETUP.md`
- **Task Breakdown**: `TASKS.md`
- **Scaffold Overview**: `SCAFFOLD_COMPLETE.md`

---

## 🎉 You're Ready!

The backend and AI services are now running. Add Firebase credentials and start building! 🚀
