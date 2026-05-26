# KrishiMitra AI - Setup Guide

## Quick Start (3-day Hackathon)

### Prerequisites
- Docker & Docker Compose
- Node.js 18+ (for mobile development)
- Java 17+ & Maven (for backend development)
- Python 3.11+ (for AI service development)

### Step 1: Firebase Setup (Santosh - Day 1 Morning, 30 min)

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create project: `krishimitra-hackathon`
3. Add Android app:
   - Package name: `com.krishimitra`
   - Download `google-services.json` → place in `mobile/android/app/`
4. Go to Project Settings → Service Accounts → Generate new private key
5. Download `krishimitra-firebase-adminsdk.json`
6. Place in `backend/src/main/resources/`
7. **IMPORTANT**: Add both files to `.gitignore` (already done)

### Step 2: Environment Variables

Create `.env` file in project root:

```bash
cp .env.example .env
```

Edit `.env` and add your API keys:
- `ANTHROPIC_API_KEY`: Get from [console.anthropic.com](https://console.anthropic.com)
- `OPENWEATHER_API_KEY`: Get from [openweathermap.org](https://openweathermap.org/api) (free tier)

### Step 3: Start Services

```bash
# Start all services with Docker Compose
docker-compose up --build

# Services will be available at:
# - Backend API: http://localhost:8080
# - AI Service: http://localhost:8000
# - PostgreSQL: localhost:5432
```

### Step 4: Verify Setup

```bash
# Check backend health
curl http://localhost:8080/actuator/health

# Check AI service health
curl http://localhost:8000/health

# Check database
docker exec -it krishimitra-postgres psql -U krishi -d krishimitra -c "\dt"
```

### Step 5: Mobile App Setup (Juhi - Day 1)

```bash
cd mobile

# Install dependencies
npm install

# Install Firebase (already in package.json)
npm install @react-native-firebase/app @react-native-firebase/messaging

# iOS setup
cd ios && pod install && cd ..

# Start development
npm start

# Run on Android
npm run android

# Run on iOS
npm run ios
```

### Step 6: Test FCM Push Notifications

```bash
# 1. Register a farmer and get FCM token (mobile app will do this)

# 2. Manually trigger a notification to test
curl -X POST http://localhost:8080/api/v1/notify \
  -H "Content-Type: application/json" \
  -d '{
    "farmer_id": "YOUR_FARMER_UUID",
    "crop_id": null,
    "alert_type": "weather",
    "message_en": "Heavy rain expected in 48 hours",
    "message_mr": "येत्या ४८ तासांत मुसळधार पाऊस अपेक्षित आहे",
    "priority": "high"
  }'

# 3. Check if push notification appears on phone
```

## Development Workflow

### Backend (Priyanshu)
```bash
cd backend
./mvnw spring-boot:run
```

### AI Service (Akshit)
```bash
cd ai-service
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload
```

### Mobile App (Juhi + Santosh)
```bash
cd mobile
npm start
```

## Database Migrations

Flyway migrations are automatically applied on startup.

To create a new migration:
```bash
# Create file: backend/src/main/resources/db/migration/V2__description.sql
# Restart backend to apply
```

## API Testing

### Mock OTP Login
```bash
# Step 1: Request OTP (any mobile number)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210"}'

# Step 2: Verify with mock OTP (always 123456)
curl -X POST http://localhost:8080/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d '{"mobile": "9876543210", "otp": "123456"}'
```

### Disease Detection
```bash
curl -X POST http://localhost:8000/ai/disease-detect \
  -F "file=@leaf.jpg" \
  -F "crop_type=soybean" \
  -F "language=marathi" \
  -F "farmer_id=YOUR_FARMER_UUID"
```

## Common Issues

### Firebase Initialization Failed
- Ensure `krishimitra-firebase-adminsdk.json` is in `backend/src/main/resources/`
- Check file permissions
- Restart Spring Boot service

### FCM Push Not Received
- Verify FCM token is registered: check `farmers.fcm_token` in database
- Check Firebase project settings
- Ensure `google-services.json` is in correct location
- Test with manual notification trigger (see Step 6 above)

### Database Connection Failed
- Ensure PostgreSQL container is running: `docker ps`
- Check connection string in `application.yml`
- Verify database exists: `docker exec -it krishimitra-postgres psql -U krishi -l`

## Project Structure

```
krishimitra/
├── backend/              # Spring Boot API
├── ai-service/           # Python FastAPI + AI
├── mobile/               # React Native app
├── docker-compose.yml    # All services
└── README.md             # Main documentation
```

## Team Responsibilities

- **Priyanshu (Backend)**: Spring Boot, NotificationService, FCM integration
- **Akshit (AI/Python)**: Disease detection, LLM advisory, notification triggers
- **Juhi (Frontend)**: React Native screens, FCM setup, UI/UX
- **Santosh (Full-stack/Infra)**: Docker, Firebase setup, polling fallback, Redux

## Demo Checklist (Day 3)

- [ ] Disease detection working with Claude Vision
- [ ] FCM push notifications delivering successfully
- [ ] In-app polling fallback functioning (bell badge updates)
- [ ] Weather alerts triggering automatically
- [ ] Mandi prices displaying
- [ ] Advisory chat responding in Marathi
- [ ] All services running via Docker Compose
- [ ] Demo script prepared (see README.md)
- [ ] Phone with app installed and FCM configured

Good luck! 🌾🚀
