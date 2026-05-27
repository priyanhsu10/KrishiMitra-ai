# KrishiMitra AI — System Design & Flow Diagrams

> Auto-generated from README.md  
> 3 Projects: `mobile` (React Native) · `backend` (Spring Boot :8080) · `ai-service` (Python FastAPI :8000)

---

## 1. High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            KrishiMitra System                               │
│                                                                             │
│  ┌─────────────────┐     REST/HTTP      ┌──────────────────────────────┐   │
│  │   mobile/       │ ─────────────────▶ │      backend/                │   │
│  │  React Native   │ ◀───────────────── │   Spring Boot :8080          │   │
│  │  (Juhi/Santosh) │                    │   (Priyanshu/Santosh)        │   │
│  └─────────────────┘                    └──────────┬───────────────────┘   │
│          │                                         │                        │
│          │ FCM Push                       JPA/JDBC │  internal HTTP         │
│          ▼                                         ▼        ▼               │
│  ┌───────────────┐                    ┌──────────────┐  ┌────────────────┐ │
│  │   Firebase    │ ◀────────────────  │  PostgreSQL  │  │   ai-service/  │ │
│  │   FCM Cloud   │   firebase-admin   │  :5432       │  │ Python FastAPI │ │
│  └───────────────┘                    └──────────────┘  │    :8000       │ │
│                                                │  ▲      │  (Akshit)     │ │
│                                       write ◀──┘  └────▶ └───────┬───────┘ │
│                                       results       direct DB     │         │
│                                                                   │         │
│                                                     ┌─────────────▼──────┐  │
│                                                     │  External APIs     │  │
│                                                     │  • OpenWeatherMap  │  │
│                                                     │  • AGMARKNET/mock  │  │
│                                                     │  • Anthropic LLM   │  │
│                                                     └────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

**Key Rules:**
- Mobile **only** talks to Spring Boot — never to Python AI directly
- Python AI writes enriched results back to Postgres directly
- Python AI calls `POST /api/v1/notify` on Spring Boot for high-priority alerts
- Spring Boot forwards disease/advisory/weather/mandi requests to Python AI

---

## 2. Full Request-Response Flow Map

```
mobile                  backend (Spring Boot)           ai-service (Python)      Postgres     Firebase
  │                            │                               │                     │             │
  │── POST /auth/login ────────▶│                               │                     │             │
  │◀─ { otp_sent: true } ──────│                               │                     │             │
  │                            │                               │                     │             │
  │── POST /auth/verify ───────▶│                               │                     │             │
  │◀─ { token, farmer_id } ────│                               │                     │             │
  │                            │                               │                     │             │
  │── POST /auth/register-token▶│                               │                     │             │
  │   { farmer_id, fcm_token } │──── UPDATE farmers.fcm_token ─────────────────────▶│             │
  │◀─ { registered: true } ────│                               │                     │             │
  │                            │                               │                     │             │
  │── POST /farmers ───────────▶│                               │                     │             │
  │◀─ { farmer_id, name } ─────│──── INSERT farmers ───────────────────────────────▶│             │
  │                            │                               │                     │             │
  │── POST /farms ─────────────▶│                               │                     │             │
  │◀─ { farm_id } ─────────────│──── INSERT farms ─────────────────────────────────▶│             │
  │                            │                               │                     │             │
  │── POST /crops ─────────────▶│                               │                     │             │
  │◀─ { crop_id } ─────────────│──── INSERT crops ─────────────────────────────────▶│             │
  │                            │                               │                     │             │
```

---

## 3. Disease Detection Flow (Core AI Feature)

```
mobile                  backend (Spring Boot)        ai-service (Python)      Postgres      Firebase
  │                            │                            │                     │              │
  │── POST /disease/detect ───▶│                            │                     │              │
  │   multipart:               │                            │                     │              │
  │   file, farmer_id,         │── POST /ai/disease-detect▶│                     │              │
  │   crop_id, crop_type       │   multipart forward        │                     │              │
  │                            │                            │── Claude Vision ──▶ │(LLM call)    │
  │                            │                            │◀─ JSON response ─── │              │
  │                            │                            │   { disease,        │              │
  │                            │                            │     confidence,     │              │
  │                            │                            │     severity,       │              │
  │                            │                            │     remedy_mr/en }  │              │
  │                            │                            │                     │              │
  │                            │                            │── if severity=high  │              │
  │                            │                            │   && confidence>0.7 │              │
  │                            │                            │                     │              │
  │                            │                            │── POST /api/v1/notify ─────────────│
  │                            │◀───────────────────────────│   { farmer_id,      │              │
  │                            │   notify trigger           │     alert_type:     │              │
  │                            │                            │     "disease",      │              │
  │                            │                            │     priority:       │              │
  │                            │                            │     "high" }        │              │
  │                            │                            │                     │              │
  │                            │── INSERT disease_reports ──────────────────────▶│              │
  │                            │── INSERT advisories ───────────────────────────▶│              │
  │                            │── SELECT fcm_token ────────────────────────────▶│              │
  │                            │◀─ fcm_token ───────────────────────────────────-│              │
  │                            │── Firebase Admin SDK ──────────────────────────────────────────▶│
  │                            │   send FCM push                                  │              │
  │◀─ Push Notification 📳 ────│────────────────────────────────────────────────────────────────│
  │   "रोग आढळला..."           │                            │                     │              │
  │                            │                            │                     │              │
  │◀──── Response ─────────────│                            │                     │              │
  │   { report_id,             │                            │                     │              │
  │     disease, confidence,   │                            │                     │              │
  │     severity, remedy_mr,   │                            │                     │              │
  │     notification_sent:true}│                            │                     │              │
```

---

## 4. AI Advisory Chat Flow

```
mobile              backend (Spring Boot)         ai-service (Python)        Postgres      Firebase
  │                        │                             │                       │               │
  │── POST /advisory/chat ▶│                             │                       │               │
  │   { farmer_id,         │                             │                       │               │
  │     crop_type,         │── POST /ai/advisory ───────▶│                       │               │
  │     stage,             │   { crop_type, stage,       │                       │               │
  │     language,          │     language, question,     │                       │               │
  │     question }         │     lat, lon }              │                       │               │
  │                        │                             │── LLM (Claude) ──────▶│(LLM call)     │
  │                        │                             │◀─ { advice_mr,        │               │
  │                        │                             │     advice_en,        │               │
  │                        │                             │     alert_type,       │               │
  │                        │                             │     priority }        │               │
  │                        │                             │                       │               │
  │                        │                             │── if priority=high ───│               │
  │                        │                             │── POST /api/v1/notify─│───────────────│
  │                        │◀────────────────────────────│                       │               │
  │                        │── INSERT advisories ────────────────────────────────▶              │
  │                        │── (if high) SELECT fcm_token───────────────────────▶│               │
  │                        │── (if high) Firebase push ─────────────────────────────────────────▶│
  │                        │                             │                       │               │
  │◀── Response ───────────│                             │                       │               │
  │   { advice (Marathi),  │                             │                       │               │
  │     advice_en,         │                             │                       │               │
  │     alert_type,        │                             │                       │               │
  │     priority,          │                             │                       │               │
  │     notification_sent }│                             │                       │               │
```

---

## 5. Weather Alert Flow

```
mobile              backend (Spring Boot)         ai-service (Python)        Postgres      Firebase
  │                        │                             │                       │               │
  │── GET /weather?        │                             │                       │               │
  │   farmer_id=uuid ─────▶│                             │                       │               │
  │                        │── SELECT farm (lat/lon) ───────────────────────────▶│               │
  │                        │◀─ { latitude, longitude } ──────────────────────────│               │
  │                        │                             │                       │               │
  │                        │── GET /ai/weather?──────────▶│                       │               │
  │                        │   lat, lon, crop, stage     │                       │               │
  │                        │                             │── OpenWeatherMap API ▶│(external)     │
  │                        │                             │◀─ forecast data ──────│               │
  │                        │                             │── LLM: Marathi advice▶│(LLM call)     │
  │                        │                             │◀─ advisory generated  │               │
  │                        │                             │                       │               │
  │                        │                             │── if rain > 20mm      │               │
  │                        │                             │   OR temp extreme     │               │
  │                        │                             │── POST /api/v1/notify─│               │
  │                        │◀────────────────────────────│   priority="high"     │               │
  │                        │── INSERT advisories ────────────────────────────────▶              │
  │                        │── SELECT fcm_token ─────────────────────────────────▶              │
  │                        │── Firebase push ────────────────────────────────────────────────────▶│
  │◀─ Push 📳 "पाऊस येणार" │                             │                       │               │
  │                        │                             │                       │               │
  │◀── Response ───────────│                             │                       │               │
  │   { weather_summary,   │                             │                       │               │
  │     advice_mr/en,      │                             │                       │               │
  │     temperature,       │                             │                       │               │
  │     humidity,          │                             │                       │               │
  │     notification_sent }│                             │                       │               │
```

---

## 6. Mandi Price Flow

```
mobile              backend (Spring Boot)         ai-service (Python)        Postgres
  │                        │                             │                       │
  │── GET /mandi?──────────▶│                             │                       │
  │   crop=soybean&        │── GET /ai/mandi?────────────▶│                       │
  │   state=Maharashtra    │   crop, state               │                       │
  │                        │                             │── AGMARKNET/mock ─────▶│(external)
  │                        │                             │◀─ price data           │
  │                        │                             │── LLM: trend analysis ▶│(LLM call)
  │                        │                             │◀─ { prices, advice_mr }│
  │                        │◀────────────────────────────│                       │
  │◀── Response ───────────│                             │                       │
  │   { crop, prices[],    │                             │                       │
  │     advice_mr,         │                             │                       │
  │     best_time_to_sell }│                             │                       │
```

---

## 7. Notification Flow (Complete)

```
                    ┌──────────────────────────────────────────┐
                    │           NOTIFICATION TRIGGER           │
                    │                                          │
                    │  Python AI generates advisory result     │
                    │              │                           │
                    │    ┌─────────▼──────────┐               │
                    │    │  Priority Check     │               │
                    │    └──────┬──────────────┘               │
                    │           │                              │
                    │    HIGH ──┼── MEDIUM/LOW                 │
                    │           │         │                    │
                    │           ▼         ▼                    │
                    │  POST /api/v1/notify  (no push,          │
                    │  (Spring Boot)        polling picks up)  │
                    └──────────────────────────────────────────┘
                                │
          ┌─────────────────────▼────────────────────────┐
          │           NotificationService.java            │
          │                                              │
          │  Step 1: INSERT advisories (ALWAYS)          │
          │          { alert_type, message_en/mr,        │
          │            priority, is_read=false,          │
          │            fcm_sent=false }                  │
          │                                              │
          │  Step 2: SELECT farmers.fcm_token            │
          │               │                             │
          │        ┌──────▼──────┐                      │
          │        │ token found?│                      │
          │        └──┬───────┬──┘                      │
          │          YES      NO                        │
          │           │       │                         │
          │           ▼       ▼                         │
          │    Firebase    log warning                   │
          │    FCM send    (advisory still saved)        │
          │       │                                     │
          │       ▼                                     │
          │  UPDATE advisories.fcm_sent = true          │
          └──────────────────────────────────────────────┘
                    │
          ┌─────────▼──────────┐
          │    Firebase Cloud   │
          │    Messaging (FCM) │
          └─────────┬──────────┘
                    │
          ┌─────────▼──────────┐
          │    mobile app      │
          │                    │
          │  Background/Quit:  │
          │  📳 phone buzzes   │
          │  system tray notif │
          │                    │
          │  Foreground:       │
          │  onMessage() fires │
          │  → Redux dispatch  │
          │  → in-app toast    │
          │  → bell badge +1   │
          └────────────────────┘

         ┌──────────────────────────────────┐
         │        POLLING FALLBACK          │
         │                                  │
         │  HomeScreen mounts               │
         │       │                          │
         │       ▼                          │
         │  GET /advisories?unread=true     │  ◀── every 30 seconds
         │       │                          │
         │       ▼                          │
         │  dispatch(setAdvisories([...]))  │
         │       │                          │
         │       ▼                          │
         │  Bell badge = unread count       │
         └──────────────────────────────────┘
```

---

## 8. Notification Trigger Rules

| Event | Source | Trigger Condition | Priority | FCM Push |
|-------|--------|-------------------|----------|----------|
| Disease detected | Python AI → disease.py | confidence > 0.70 AND severity = high | **high** | ✅ Yes |
| Rain forecast | Python AI → weather.py | rainfall > 20mm in 48h | **high** | ✅ Yes |
| Temperature extreme | Python AI → weather.py | temp > 42°C or < 5°C | **high** | ✅ Yes |
| Irrigation due | Python AI → advisory.py | soil moisture model dry | medium | ❌ No (polling) |
| Mandi price spike | Python AI → mandi.py | price change > 10% | medium | ❌ No (polling) |
| Advisory chat reply | Python AI → advisory.py | always | low | ❌ No (polling) |

---

## 9. POST /api/v1/notify Internal Contract

```
Python AI service                Spring Boot :8080              Postgres         Firebase
      │                                 │                          │                 │
      │── POST /api/v1/notify ─────────▶│                          │                 │
      │   {                             │                          │                 │
      │     farmer_id: "uuid",          │── INSERT advisories ────▶│                 │
      │     crop_id: "uuid",            │◀─ advisory.id ───────────│                 │
      │     alert_type: "weather",      │                          │                 │
      │     message_en: "Rain...",      │── SELECT fcm_token ─────▶│                 │
      │     message_mr: "पाऊस...",     │◀─ "dJ8k2xP..." ──────────│                 │
      │     priority: "high"            │                          │                 │
      │   }                             │── FirebaseMessaging ─────────────────────▶│
      │                                 │   .send(Message)         │                 │
      │                                 │◀─ success ────────────────────────────────│
      │                                 │── UPDATE fcm_sent=true ──▶│                 │
      │◀─ { advisory_id, fcm_sent,      │                          │                 │
      │     saved: true } ─────────────│                          │                 │
```

---

## 10. Auth & Onboarding Flow

```
mobile                              backend (Spring Boot)              Postgres
  │                                          │                             │
  │ 1. Open app → Language selection         │                             │
  │    (Marathi / Hindi / English)           │                             │
  │                                          │                             │
  │── POST /auth/login ─────────────────────▶│                             │
  │   { mobile: "9876543210" }               │── mock: always sends OTP ──▶│
  │◀─ { otp_sent: true } ───────────────────│    (PIN 123456)             │
  │                                          │                             │
  │── POST /auth/verify ────────────────────▶│                             │
  │   { mobile, otp: "123456" }             │── SELECT/INSERT farmer ────▶│
  │◀─ { token, farmer_id, is_new_user } ────│◀─ farmer row ───────────────│
  │                                          │                             │
  │ [if is_new_user] ── POST /farmers ──────▶│                             │
  │   { name, language, village, state }    │── INSERT farmers ──────────▶│
  │◀─ { farmer_id, name, language } ────────│                             │
  │                                          │                             │
  │── POST /auth/register-token ────────────▶│                             │
  │   { farmer_id, fcm_token }              │── UPDATE farmers.fcm_token ▶│
  │◀─ { registered: true } ─────────────────│                             │
  │                                          │                             │
  │── POST /farms ──────────────────────────▶│                             │
  │   { farmer_id, name, lat, lon,          │── INSERT farms ────────────▶│
  │     area_acres, soil_type }             │                             │
  │◀─ { farm_id } ──────────────────────────│                             │
  │                                          │                             │
  │── POST /crops ──────────────────────────▶│                             │
  │   { farm_id, crop_type, sowing_date,    │── INSERT crops ────────────▶│
  │     stage }                              │                             │
  │◀─ { crop_id } ──────────────────────────│                             │
  │                                          │                             │
  │ ──▶ Home Dashboard                       │                             │
```

---

## 11. In-App Advisory Polling & Bell Badge

```
HomeScreen.tsx (mounts)
        │
        ▼
useAdvisoryPolling(farmerId) hook starts
        │
        ├── fetchAdvisories() immediately
        │          │
        │          ▼
        │   GET /api/v1/advisories?farmer_id=uuid&unread=true
        │          │
        │          ▼
        │   dispatch(setAdvisories(res.advisories))
        │          │
        │          ▼
        │   notificationSlice updates:
        │     items = advisories[]
        │     unreadCount = items.filter(!is_read).length
        │
        └── setInterval(fetchAdvisories, 30000ms)
                   │
                   ▼ (every 30s)
           ┌───────────────────────────────────┐
           │  Header Bell Icon                 │
           │                                   │
           │  <BellIcon />                     │
           │    {unreadCount > 0 &&            │
           │      <Badge>{unreadCount}</Badge>}│
           │                                   │
           │  Tap → navigate('Notifications')  │
           └───────────────────────────────────┘
                          │
                          ▼
               NotificationsScreen.tsx
                          │
                 ┌────────▼────────┐
                 │  Advisory List  │
                 │  (sorted by     │
                 │   created_at)   │
                 └────────┬────────┘
                          │  Tap advisory
                          ▼
               PATCH /api/v1/advisories/{id}/read
                          │
                          ▼
               advisory.is_read = true
               unreadCount decrements
```

---

## 12. Component Interaction Map (Cross-Project)

```
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  mobile/src/                                                                         │
│                                                                                      │
│  screens/                     api/                    store/                        │
│  ┌─────────────────┐          ┌────────────────┐     ┌──────────────────────┐       │
│  │ HomeScreen      │─poll────▶│ advisoryApi    │     │ notificationSlice    │       │
│  │ - usePolling()  │◀─data───-│ .getAll()      │────▶│ items, unreadCount   │       │
│  │ - BellBadge     │          │ .markRead()    │     └──────────────────────┘       │
│  └─────────────────┘          └────────────────┘                                    │
│  ┌─────────────────┐          ┌────────────────┐     ┌──────────────────────┐       │
│  │ DiseaseDetect   │─upload──▶│ diseaseApi     │     │ farmerSlice          │       │
│  │ Screen          │◀─result──│ .detect()      │     │ farmer_id, language  │       │
│  └─────────────────┘          └────────────────┘     └──────────────────────┘       │
│  ┌─────────────────┐          ┌────────────────┐                                    │
│  │ AdvisoryChat    │─chat────▶│ advisoryApi    │     services/                      │
│  │ Screen          │◀─advice──│ .chat()        │     ┌──────────────────────┐       │
│  └─────────────────┘          └────────────────┘     │ notifications.ts     │       │
│  ┌─────────────────┐          ┌────────────────┐     │ registerFCMToken()   │       │
│  │ Weather Screen  │─fetch───▶│ weatherApi     │     │ setupForeground      │       │
│  └─────────────────┘          └────────────────┘     │ Listener()           │       │
│  ┌─────────────────┐          ┌────────────────┐     └──────────────────────┘       │
│  │ Mandi Screen    │─fetch───▶│ mandiApi       │                                    │
│  └─────────────────┘          └────────────────┘                                    │
└──────────────────────────────────────────────────────────────────────────────────────┘
                     │ REST :8080
                     ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  backend/src/main/java/com/krishimitra/                                              │
│                                                                                      │
│  auth/          farmer/        farm/          crop/                                  │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐                            │
│  │ Auth     │  │ Farmer   │  │ Farm     │  │ Crop     │                            │
│  │ Controller│  │Controller│  │Controller│  │Controller│                            │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘                            │
│       │              │              │              │                                  │
│  disease/         advisory/      notification/  weather/ mandi/                      │
│  ┌──────────┐  ┌──────────────┐  ┌──────────────────┐  ┌────────────────────┐       │
│  │ Disease  │  │ Advisory     │  │ NotifyController  │  │ Weather / Mandi    │       │
│  │Controller│  │Controller    │  │ POST /notify      │  │ Controllers        │       │
│  │ ─forward─│  │ ─forward─    │  └────────┬──────────┘  │ ─forward to AI─    │       │
│  │ to AI    │  │ to AI        │           │             └────────────────────┘       │
│  └──────────┘  └──────────────┘           ▼                                          │
│                                  NotificationService                                 │
│                                  ┌──────────────────────────────┐                   │
│                                  │ 1. INSERT advisories         │                   │
│                                  │ 2. SELECT fcm_token          │                   │
│                                  │ 3. FirebaseMessaging.send()  │                   │
│                                  │ 4. UPDATE fcm_sent=true      │                   │
│                                  └──────────────────────────────┘                   │
└──────────────────────────────────────────────────────────────────────────────────────┘
                     │ HTTP :8000 (internal only)
                     ▼
┌──────────────────────────────────────────────────────────────────────────────────────┐
│  ai-service/                                                                         │
│                                                                                      │
│  routers/                           services/                                        │
│  ┌───────────┐  ┌──────────────┐   ┌────────────────┐  ┌──────────────────────┐    │
│  │ disease   │  │ advisory     │   │ vision_service  │  │ notify_service       │    │
│  │ .py       │  │ .py          │   │ Claude Vision   │  │ POST /api/v1/notify  │    │
│  └─────┬─────┘  └──────┬───────┘   └────────────────┘  │ → Spring Boot        │    │
│        │               │           ┌────────────────┐   └──────────────────────┘    │
│  ┌───────────┐  ┌──────────────┐   │ llm_service    │   ┌──────────────────────┐    │
│  │ weather   │  │ mandi        │   │ Claude/Gemini  │   │ db_service           │    │
│  │ .py       │  │ .py          │   └────────────────┘   │ direct Postgres      │    │
│  └─────┬─────┘  └──────┬───────┘   ┌────────────────┐   └──────────────────────┘    │
│        │               │           │ weather_service │                              │
│        └───────┬────────┘           │ OpenWeatherMap  │                              │
│                │                   └────────────────┘                              │
│                ▼                                                                     │
│          notify_service  ──── POST /api/v1/notify ─────────────────────────────────▶│
│          (high priority                                                              │
│           events only)                                                               │
└──────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 13. Docker Network Topology

```
docker-compose network: krishimitra_default
                    │
        ┌───────────┼─────────────────────┐
        │           │                     │
        ▼           ▼                     ▼
  postgres:5432  spring-backend:8080  python-ai:8000
        ▲               │                 │
        │               │ JPA/JDBC        │ SQLAlchemy
        └───────────────┴─────────────────┘
                (both write to same DB)

External port mapping:
  localhost:5432  → postgres:5432
  localhost:8080  → spring-backend:8080
  localhost:8000  → python-ai:8000

Environment links:
  spring-backend:
    DB_URL      = jdbc:postgresql://postgres:5432/krishimitra
    AI_SERVICE_URL = http://python-ai:8000

  python-ai:
    DB_URL        = postgresql://krishi:krishi123@postgres:5432/krishimitra
    SPRING_BASE_URL = http://spring-backend:8080
```

---

## 14. Database Entity Relationship

```
farmers
  id (PK) ◀─────────────────────────────────────────────────────────┐
  mobile (UNIQUE)                                                     │
  name                                                                │
  language                                                            │
  fcm_token  ◀─── updated on every login                             │
  created_at                                                          │
      │                                                               │
      │ 1:N                                                           │
      ▼                                                               │
    farms                                                             │
      id (PK) ◀─────────────────────┐                                │
      farmer_id (FK → farmers.id)   │                                │
      name                          │                                │
      latitude, longitude           │                                │
      area_acres                    │                                │
      soil_type                     │                                │
          │                         │                                │
          │ 1:N                     │                                │
          ▼                         │                                │
        crops                       │                                │
          id (PK) ◀────────────┐    │                                │
          farm_id (FK)         │    │                                │
          crop_type            │    │                                │
          sowing_date          │    │                                │
          stage                │    │                                │
              │                │    │                                │
              │ 1:N            │    │                                │
              │                │    │                                │
    ┌─────────┴──────┐   ┌─────┴────┴──────────────────────────┐    │
    ▼                ▼   ▼                                      │    │
disease_reports   advisories                                    │    │
  id (PK)           id (PK)                                     │    │
  farmer_id (FK)────────────────────────────────────────────────┘────┘
  crop_id (FK)──┐   farmer_id (FK)
  image_url     │   crop_id (FK)──┘
  diagnosis     │   alert_type
  diagnosis_mr  │   message_en / message_mr / message_hi
  confidence    │   priority
  remedy_en/mr  │   is_read        ◀─── PATCH /advisories/{id}/read
  severity      │   fcm_sent       ◀─── updated after Firebase push
  created_at    │   created_at
                │
                └── links disease report to crop stage
```

---

## 15. FCM Token Lifecycle

```
App Install
    │
    ▼
Login → OTP verify
    │
    ▼
messaging().requestPermission()
    │
    ├── GRANTED
    │       │
    │       ▼
    │   messaging().getToken()  → "dJ8k2xP..."
    │       │
    │       ▼
    │   POST /auth/register-token
    │   { farmer_id, fcm_token }
    │       │
    │       ▼
    │   UPDATE farmers SET fcm_token = "dJ8k2xP..."
    │   (overwrites on new device login)
    │
    └── DENIED
            │
            ▼
        FCM silent fail
        in-app polling still works ✅

Token refresh (device change):
    │
    ▼
Next login → new getToken() → POST /register-token
→ fcm_token overwritten in DB → new device gets pushes
```

---

## 16. Summary: Cross-Project Action Table

| Action | mobile | backend | ai-service | Postgres | Firebase |
|--------|--------|---------|------------|----------|----------|
| Login / OTP verify | initiates | validates, returns JWT | — | read/write farmer | — |
| Register FCM token | sends token | saves to farmers | — | UPDATE fcm_token | — |
| Create farmer/farm/crop | submits form | inserts record | — | INSERT | — |
| Disease detection | uploads image | forwards to AI, saves result, triggers notify | Claude Vision, POST /notify | INSERT disease_report + advisory | FCM push if severity=high |
| Advisory chat | sends question | forwards to AI, saves advisory | LLM call, POST /notify if high | INSERT advisory | FCM push if high priority |
| Weather alert | requests data | fetches farm coords, calls AI | OpenWeatherMap + LLM, POST /notify | INSERT advisory | FCM push if rain > 20mm |
| Mandi prices | requests data | calls AI endpoint | AGMARKNET/mock + LLM trend | — | — |
| In-app polling | polls every 30s | returns unread advisories | — | SELECT advisories | — |
| Mark advisory read | taps notification | PATCH advisory | — | UPDATE is_read=true | — |
| FCM foreground | onMessage() fires | — | — | — | delivers push |
| FCM background/quit | system notification | — | — | — | delivers push |

---

*Design doc auto-generated from README.md — KrishiMitra Team: Priyanshu · Akshit · Juhi · Santosh*
