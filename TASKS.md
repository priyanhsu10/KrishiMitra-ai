# KrishiMitra Development Tasks

## Day 1 Morning — Setup & Infrastructure (4 hours)

### Setup (Priyanshu + Santosh) — 1.5 hours
- [ ] Create GitHub repository with branch strategy (main/dev)
- [ ] **Firebase project setup** (Santosh - PRIORITY)
  - [ ] Create Firebase project: `krishimitra-hackathon`
  - [ ] Add Android app, download `google-services.json`
  - [ ] Generate service account key, download `krishimitra-firebase-adminsdk.json`
  - [ ] Place both files in correct locations (see README_SETUP.md)
- [ ] Setup `docker-compose.yml` + PostgreSQL
- [ ] Create `.env` files with API keys
- [ ] Test database connectivity

### Backend Foundation (Priyanshu) — 2 hours
- [ ] Spring Boot project initialization
- [ ] Database schema + Flyway migrations
- [ ] JWT authentication setup
- [ ] Mock OTP login (`123456` always passes)
- [ ] `POST /auth/login` endpoint
- [ ] `POST /auth/verify` endpoint
- [ ] **`POST /auth/register-token` endpoint** (FCM)
- [ ] **`FirebaseConfig.java` — Firebase Admin SDK initialization**
- [ ] Test authentication flow

### AI Service Foundation (Akshit) — 1.5 hours
- [ ] Python FastAPI project initialization
- [ ] Install dependencies (anthropic, httpx, etc.)
- [ ] Create service structure (routers, services)
- [ ] Health check endpoint
- [ ] Test AI service connectivity

### Mobile Foundation (Juhi) — 2 hours
- [ ] React Native project initialization
- [ ] Navigation setup (Stack + Tabs)
- [ ] Redux store configuration
- [ ] **Install Firebase dependencies**
- [ ] **`setupForegroundListener()` in App.tsx**
- [ ] **FCM token registration after login**
- [ ] Basic authentication screens (Login, OTP)

---

## Day 1 Afternoon — Core APIs (4 hours)

### Backend Core APIs (Priyanshu) — 2.5 hours
- [ ] Farmer CRUD endpoints
  - [ ] `POST /api/v1/farmers`
  - [ ] `GET /api/v1/farmers/{id}`
- [ ] Farm CRUD endpoints
  - [ ] `POST /api/v1/farms`
  - [ ] `GET /api/v1/farms?farmer_id={}`
- [ ] Crop CRUD endpoints
  - [ ] `POST /api/v1/crops`
  - [ ] `GET /api/v1/crops?farm_id={}`

### Mobile Core Screens (Juhi) — 2.5 hours
- [ ] Language selection screen
- [ ] Farmer profile screen
- [ ] Add farm screen with GPS picker
- [ ] Add crop screen with dropdown

### Infrastructure (Santosh) — 1.5 hours
- [ ] **`notificationSlice.ts` Redux setup**
- [ ] **`useAdvisoryPolling()` hook implementation**
- [ ] Test Docker build for all services
- [ ] Seed demo data script

---

## Day 2 — AI Features + Notifications (8 hours)

### AI Core Features (Akshit) — 5 hours
- [ ] **Disease Detection** (`/ai/disease-detect`)
  - [ ] Claude Vision integration
  - [ ] Structured JSON response
  - [ ] Save to `disease_reports` table
  - [ ] **Trigger notification if severity=high**
- [ ] **Notification Service** (`services/notify_service.py`)
  - [ ] Implement `trigger_notification()` function
  - [ ] Call Spring Boot `/api/v1/notify`
  - [ ] Error handling (fire-and-forget)
- [ ] **Weather Advisory** (`/ai/weather`)
  - [ ] OpenWeatherMap integration
  - [ ] LLM-generated Marathi advice
  - [ ] **Trigger notification if rain > 20mm**
- [ ] **LLM Advisory** (`/ai/advisory`)
  - [ ] Marathi/Hindi prompt templates
  - [ ] Crop-specific advice
  - [ ] Alert type classification
- [ ] **Mandi Prices** (`/ai/mandi`)
  - [ ] Mock price data for Maharashtra
  - [ ] Trend analysis

### Backend Notification System (Priyanshu) — 3 hours
- [ ] **`NotifyController.java`** — `/api/v1/notify` endpoint
- [ ] **`NotificationService.java`** — Core notification logic
  - [ ] Save advisory to database (always)
  - [ ] Lookup FCM token from `farmers.fcm_token`
  - [ ] Send Firebase Cloud Messaging push
  - [ ] Update `fcm_sent` flag
  - [ ] Error handling (never fail request)
- [ ] `DiseaseController.java` — Forward to Python AI
- [ ] `AdvisoryController.java` — Chat + advisory list
  - [ ] `POST /api/v1/advisory/chat`
  - [ ] `GET /api/v1/advisories?farmer_id={}&unread=true`
  - [ ] `PATCH /api/v1/advisories/{id}/read`
- [ ] `WeatherController.java` — Forward to Python AI
- [ ] `MandiController.java` — Forward to Python AI

### Mobile AI Screens (Juhi) — 3 hours
- [ ] Disease Detection Screen
  - [ ] Camera integration
  - [ ] Upload to backend
  - [ ] Display diagnosis + remedy card
- [ ] Advisory Chat Screen
  - [ ] Text input with language toggle
  - [ ] Chat bubble UI
  - [ ] Display LLM response

### Mobile Notification UI (Santosh) — 2 hours
- [ ] **Bell icon with badge in HomeScreen header**
- [ ] **Notifications screen** (list advisories)
- [ ] **Mark as read functionality**
- [ ] Weather screen (display forecast + advice)
- [ ] Mandi screen (price list)

---

## Day 3 — Polish + Demo (6 hours)

### Demo Preparation (All) — 3 hours
- [ ] Home dashboard design (Juhi)
  - [ ] Quick action grid
  - [ ] Recent advisories feed
- [ ] End-to-end flow testing (All)
- [ ] **FCM demo dry-run** (Priyanshu)
  - [ ] Test POST /notify via Postman
  - [ ] Verify push notification on phone
  - [ ] Test in-app polling fallback
- [ ] Seed realistic demo data (Santosh)
  - [ ] 2 demo farmers
  - [ ] 2 farms (cotton + soybean)
  - [ ] Sample advisories
- [ ] Docker clean build + smoke test (Santosh)

### Demo Materials (Priyanshu + Juhi) — 2 hours
- [ ] Demo script (3-minute version)
- [ ] Presentation slides (problem, solution, tech stack, impact)
- [ ] Record demo video (backup)

### Bug Fixes & Polish (All) — 1 hour
- [ ] UI/UX refinements
- [ ] Error handling improvements
- [ ] Logging cleanup

---

## Critical Path (Must Complete for Demo)

### P0 (Core Demo Features)
1. ✅ Disease detection with Claude Vision
2. ✅ Firebase Cloud Messaging push notifications
3. ✅ Weather alerts (high rainfall)
4. ✅ In-app polling fallback (bell badge)
5. ✅ NotificationService (FCM + DB save)

### P1 (Essential for Flow)
1. Auth flow (login + OTP)
2. Farm + crop registration
3. Advisory chat (LLM in Marathi)
4. Notifications screen (list + mark read)

### P2 (Nice to Have)
1. Mandi prices
2. Home dashboard polish
3. Weather screen design

---

## Testing Checklist (Day 3 Morning)

- [ ] **FCM Push Test**
  1. Login on real device
  2. Register FCM token
  3. Trigger manual notification via Postman
  4. Verify push appears on phone
  5. Check notification persists in DB

- [ ] **Disease Detection Flow**
  1. Upload leaf image
  2. Verify Claude Vision analysis
  3. Check notification triggers for high severity
  4. Verify push + in-app notification

- [ ] **Polling Fallback**
  1. Disable FCM (airplane mode)
  2. Trigger notification from backend
  3. Wait 30 seconds
  4. Verify bell badge updates

- [ ] **Docker Compose**
  1. Stop all services
  2. `docker-compose down -v`
  3. `docker-compose up --build`
  4. Verify all services start successfully

---

## Team Communication

- **Daily Standup**: 10 AM (15 min)
  - What did I complete yesterday?
  - What will I work on today?
  - Any blockers?

- **Integration Points**:
  - Day 1 EOD: Auth + database working
  - Day 2 Morning: Python AI → Spring Boot `/notify` tested
  - Day 2 EOD: FCM push working end-to-end
  - Day 3 Morning: Full demo rehearsal

- **Code Reviews**: Quick PR reviews (< 1 hour turnaround)

---

*Last Updated: Day 1 Morning*
