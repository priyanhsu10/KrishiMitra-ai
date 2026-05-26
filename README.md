# KrishiMitra-ai
# 🌾 KrishiMitra AI — Hackathon MVP Spec (v3 — Final)
**"Smart Farming. Better Harvests. Higher Income."**

---

## Team & Roles

| Member | Role | Owns |
|--------|------|------|
| **Priyanshu** | Backend Lead | Spring Boot API, DB schema, service contracts, NotificationService |
| **Akshit** | AI/Python Lead | FastAPI AI engine, LLM integration, disease detection, notify triggers |
| **Juhi** | Frontend Lead | React Native screens, UI/UX, FCM token registration, foreground listener |
| **Santosh** | Full-stack / Infra | Docker, DB migrations, Redux store, Firebase setup, polling fallback |

---

## MVP Scope (3-day Hackathon)

### ✅ Build These (Demo-able)
1. Farmer registration (mobile + OTP mock + language selection)
2. Farm registration (GPS location, crop type, sowing date)
3. 🌿 **Disease Detection — upload leaf image → AI diagnosis (CORE AI FEATURE)**
4. AI Crop Advisory — LLM-powered chat in Marathi/Hindi
5. Weather Alert — rain/temperature forecast for farm location
6. Mandi Price — show nearby market prices (mock or AGMARKNET)
7. 🔔 **Push Notifications — FCM (wow-factor) + in-app polling fallback**

### ❌ Skip for MVP
- Real OTP (use mock PIN `123456`)
- Satellite monitoring
- Voice STT/TTS (UI button submits text)
- Government scheme engine
- IoT integration

---

## Architecture

```
  [Mobile App]  ──REST──▶  [Spring Boot API :8080]  ──JPA──▶  [PostgreSQL :5432]
                                     │                               ▲
                            Disease Detection                        │
                          (upload leaf image)              write results + advisories
                                     │                               │
                                     ▼                               │
                          [Python AI Service :8000] ────────────────┘
                               │    │    │                │
                               ▼    ▼    ▼                ▼
                          Weather  Mandi  LLM      POST /notify
                           API    Price  Provider  (high-priority alerts)
                                                        │
                                                        ▼
                                              [Spring NotificationService]
                                                   │          │
                                                   ▼          ▼
                                              FCM Push    Save to DB
                                            (Firebase)  (advisories)
                                                   │
                                                   ▼
                                            [Mobile App]
                                          📳 push  🔔 badge
```

**Key design decisions:**
- Mobile app only talks to Spring Boot — Python AI is internal, never called directly from app
- Python AI writes enriched results back to Postgres directly
- Python AI calls `POST /notify` on Spring Boot for every high-priority advisory
- Spring NotificationService handles both FCM push AND DB save atomically
- In-app polling every 30s as FCM fallback — bell badge always stays accurate

---

## Database Schema

### `farmers`
```sql
CREATE TABLE farmers (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  mobile      VARCHAR(15) UNIQUE NOT NULL,
  name        VARCHAR(100),
  language    VARCHAR(20) DEFAULT 'marathi',
  village     VARCHAR(100),
  state       VARCHAR(100),
  fcm_token   VARCHAR(500),          -- Firebase device token, updated on each login
  created_at  TIMESTAMP DEFAULT NOW()
);
```

### `farms`
```sql
CREATE TABLE farms (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  farmer_id   UUID REFERENCES farmers(id) ON DELETE CASCADE,
  name        VARCHAR(100),
  latitude    DECIMAL(10,6),
  longitude   DECIMAL(10,6),
  area_acres  DECIMAL(6,2),
  soil_type   VARCHAR(50),           -- black | red | loamy | sandy
  created_at  TIMESTAMP DEFAULT NOW()
);
```

### `crops`
```sql
CREATE TABLE crops (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  farm_id      UUID REFERENCES farms(id) ON DELETE CASCADE,
  crop_type    VARCHAR(100),          -- cotton | soybean | wheat | onion | sugarcane
  sowing_date  DATE,
  stage        VARCHAR(50),           -- germination | vegetative | flowering | harvest
  created_at   TIMESTAMP DEFAULT NOW()
);
```

### `advisories`
```sql
CREATE TABLE advisories (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  crop_id      UUID REFERENCES crops(id),
  farmer_id    UUID REFERENCES farmers(id),
  alert_type   VARCHAR(50),           -- weather | disease | irrigation | fertilizer | market
  message_en   TEXT,
  message_mr   TEXT,
  message_hi   TEXT,
  priority     VARCHAR(10) DEFAULT 'medium',  -- high | medium | low
  is_read      BOOLEAN DEFAULT FALSE,
  fcm_sent     BOOLEAN DEFAULT FALSE,         -- track push delivery
  created_at   TIMESTAMP DEFAULT NOW()
);
```

### `disease_reports`
```sql
CREATE TABLE disease_reports (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  farmer_id     UUID REFERENCES farmers(id),
  crop_id       UUID REFERENCES crops(id),
  image_url     VARCHAR(500),
  diagnosis     TEXT,
  diagnosis_mr  TEXT,
  confidence    DECIMAL(5,2),
  remedy_en     TEXT,
  remedy_mr     TEXT,
  severity      VARCHAR(20),           -- low | medium | high
  created_at    TIMESTAMP DEFAULT NOW()
);
```

---

## API Contracts

### Spring Boot (port 8080)

---

#### `POST /api/v1/auth/login`
```json
Request:  { "mobile": "9876543210" }
Response: { "otp_sent": true }
```
> Mock: OTP `123456` always passes.

---

#### `POST /api/v1/auth/verify`
```json
Request:  { "mobile": "9876543210", "otp": "123456" }
Response: { "token": "eyJ...", "farmer_id": "uuid", "is_new_user": true }
```

---

#### `POST /api/v1/auth/register-token`
Called immediately after login to register the device for push notifications.
```json
Request:  { "farmer_id": "uuid", "fcm_token": "dJ8k2xP..." }
Response: { "registered": true }
```
Spring saves `fcm_token` to `farmers.fcm_token`. If farmer logs in on a new device, token is updated.

---

#### `POST /api/v1/farmers`
```json
Request:  { "name": "Ramesh Patil", "language": "marathi", "village": "Pune Rural", "state": "Maharashtra" }
Response: { "farmer_id": "uuid", "name": "Ramesh Patil", "language": "marathi" }
```

---

#### `GET /api/v1/farmers/{farmer_id}`
```json
Response: {
  "farmer_id": "uuid",
  "name": "Ramesh Patil",
  "language": "marathi",
  "farms": [{ "farm_id": "uuid", "name": "Main Farm", "area_acres": 5.5 }]
}
```

---

#### `POST /api/v1/farms`
```json
Request:  { "farmer_id": "uuid", "name": "Main Farm", "latitude": 18.52, "longitude": 73.85, "area_acres": 5.5, "soil_type": "black" }
Response: { "farm_id": "uuid", "name": "Main Farm" }
```

---

#### `POST /api/v1/crops`
```json
Request:  { "farm_id": "uuid", "crop_type": "soybean", "sowing_date": "2025-06-01", "stage": "vegetative" }
Response: { "crop_id": "uuid", "crop_type": "soybean", "stage": "vegetative" }
```

---

#### `POST /api/v1/disease/detect`
Spring Boot receives image + metadata, forwards multipart to Python AI, saves result to `disease_reports`, auto-triggers notification if severity is high.
```
Content-Type: multipart/form-data
Fields:
  file       : <image.jpg>
  farmer_id  : uuid
  crop_id    : uuid (optional)
  crop_type  : soybean
```
```json
Response: {
  "report_id": "uuid",
  "disease": "Yellow Mosaic Virus",
  "disease_mr": "पिवळा मोझेक विषाणू",
  "confidence": 0.87,
  "severity": "high",
  "remedy_en": "Remove infected plants. Apply systemic insecticide for whiteflies.",
  "remedy_mr": "संक्रमित झाडे काढून टाका. पांढऱ्या माशींसाठी कीटकनाशक वापरा.",
  "notification_sent": true
}
```

---

#### `POST /api/v1/advisory/chat`
Spring Boot forwards to Python AI, auto-saves advice as advisory record, sends notification if priority is high.
```json
Request: {
  "farmer_id": "uuid",
  "crop_type": "soybean",
  "stage": "vegetative",
  "language": "marathi",
  "question": "माझ्या सोयाबीनला पिवळी पाने येत आहेत"
}
Response: {
  "advice": "हे नत्राच्या कमतरतेचे लक्षण असू शकते. युरिया खत प्रति एकर ५ किलो द्या.",
  "advice_en": "This appears to be nitrogen deficiency. Apply 5kg urea per acre.",
  "alert_type": "fertilizer",
  "priority": "medium",
  "notification_sent": false
}
```

---

#### `GET /api/v1/weather?farmer_id={uuid}`
Spring fetches farm coordinates from DB, calls Python AI weather endpoint.
```json
Response: {
  "weather_summary": "Rain 65mm expected in 48 hours",
  "advice_mr": "येत्या ४८ तासांत ६५ मिमी पाऊस अपेक्षित. खत टाकणे पुढे ढकला.",
  "advice_en": "65mm rain in 48 hours. Delay fertilizer.",
  "alert_type": "weather",
  "priority": "high",
  "temperature": 28.5,
  "humidity": 72,
  "notification_sent": true
}
```

---

#### `GET /api/v1/mandi?crop={crop_type}&state=Maharashtra`
```json
Response: {
  "crop": "Soybean",
  "prices": [
    { "mandi": "Pune", "price_per_quintal": 4850, "trend": "rising" },
    { "mandi": "Nashik", "price_per_quintal": 4720, "trend": "stable" }
  ],
  "advice_mr": "पुढील आठवड्यात भाव वाढण्याची शक्यता. थांबण्याचा विचार करा.",
  "best_time_to_sell": "next_week"
}
```

---

#### `POST /api/v1/notify`
Called internally by Python AI service when it generates any advisory. Not called from mobile.
```json
Request: {
  "farmer_id": "uuid",
  "crop_id": "uuid",
  "alert_type": "weather",
  "message_en": "Rain expected in 48 hours. Delay fertilizer.",
  "message_mr": "येत्या ४८ तासांत पाऊस. खत टाकणे टाळा.",
  "priority": "high"
}
Response: { "advisory_id": "uuid", "fcm_sent": true, "saved": true }
```
Spring saves to `advisories` table, looks up `farmers.fcm_token`, sends FCM push.

---

#### `GET /api/v1/advisories?farmer_id={uuid}&unread=true`
Used by in-app polling fallback every 30s. Also used by Notifications screen.
```json
Response: {
  "unread_count": 3,
  "advisories": [
    {
      "id": "uuid",
      "alert_type": "weather",
      "message": "येत्या ४८ तासांत पाऊस अपेक्षित आहे. खत टाकणे टाळा.",
      "priority": "high",
      "is_read": false,
      "created_at": "2025-06-15T08:00:00Z"
    }
  ]
}
```

---

#### `PATCH /api/v1/advisories/{id}/read`
Mark advisory as read when farmer taps it.
```json
Response: { "id": "uuid", "is_read": true }
```

---

### Python AI Service (port 8000) — internal only, not called by mobile

---

#### `POST /ai/disease-detect`
```
Content-Type: multipart/form-data
Fields: file, crop_type, language, farmer_id, crop_id
```
```json
Response: {
  "disease": "Yellow Mosaic Virus",
  "disease_mr": "पिवळा मोझेक विषाणू",
  "confidence": 0.87,
  "remedy_en": "Remove infected plants. Apply systemic insecticide.",
  "remedy_mr": "संक्रमित झाडे काढून टाका.",
  "severity": "high"
}
```
After returning response, Python AI calls `POST /api/v1/notify` on Spring Boot if severity is `high`.

---

#### `POST /ai/advisory`
```json
Request:  { "crop_type": "soybean", "stage": "vegetative", "language": "marathi", "question": "...", "lat": 18.52, "lon": 73.85 }
Response: { "advice": "...", "advice_en": "...", "alert_type": "fertilizer", "priority": "medium" }
```
If priority is `high`, Python AI calls `POST /api/v1/notify` on Spring Boot after responding.

---

#### `GET /ai/weather?lat={}&lon={}&crop={}&stage={}&lang={}`
Fetches OpenWeatherMap, generates Marathi advisory via LLM.
Always calls `POST /api/v1/notify` if rain > 20mm or temperature extreme detected.

---

#### `GET /ai/mandi?crop={}&state={}`
Fetches AGMARKNET or returns mock prices with LLM trend analysis.

---

## Notification Flow — Complete

```
Python AI generates advisory
         │
         ├── high priority? ──YES──▶ POST /api/v1/notify (Spring)
         │                                    │
         │                          ┌─────────┴──────────┐
         │                          ▼                     ▼
         │                   Save to advisories      Lookup fcm_token
         │                   (always happens)        from farmers table
         │                                                │
         │                                          fcm_token found?
         │                                         YES ──▶ Firebase FCM ──▶ 📳 Phone buzzes
         │                                          NO ──▶ log warning, advisory still saved
         │
         └── any priority ──▶ In-app polling picks it up within 30s ──▶ 🔔 bell badge updates
```

**Rule: notify on these events**
| Event | Trigger condition | Priority |
|-------|-------------------|----------|
| Disease detected | confidence > 0.7 AND severity = high | high |
| Rain forecast | rainfall > 20mm in 48h | high |
| Temperature extreme | temp > 42°C or < 5°C | high |
| Irrigation due | soil moisture model says dry | medium |
| Mandi price spike | price change > 10% | medium |
| Advisory chat reply | always | low (no push) |

---

## Spring Boot — NotificationService.java (Priyanshu)

```java
// pom.xml dependency
// <groupId>com.google.firebase</groupId>
// <artifactId>firebase-admin</artifactId>
// <version>9.2.0</version>

@Service
@Slf4j
public class NotificationService {

    @Autowired private FirebaseMessaging firebaseMessaging;
    @Autowired private AdvisoryRepository advisoryRepository;
    @Autowired private FarmerRepository farmerRepository;

    public NotifyResponse sendAlert(NotifyRequest req) {
        // Step 1: Always save advisory to DB
        Advisory advisory = Advisory.builder()
            .farmerId(req.getFarmerId())
            .cropId(req.getCropId())
            .alertType(req.getAlertType())
            .messageEn(req.getMessageEn())
            .messageMr(req.getMessageMr())
            .priority(req.getPriority())
            .isRead(false)
            .fcmSent(false)
            .build();
        advisory = advisoryRepository.save(advisory);

        // Step 2: FCM push — best effort, never fail the whole request
        boolean fcmSent = false;
        try {
            String fcmToken = farmerRepository.findById(req.getFarmerId())
                .map(Farmer::getFcmToken)
                .orElse(null);

            if (fcmToken != null && !fcmToken.isBlank()) {
                Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                        .setTitle("🌾 KrishiMitra — " + alertTypeLabel(req.getAlertType()))
                        .setBody(req.getMessageMr())   // always Marathi for push
                        .build())
                    .putData("alert_type", req.getAlertType())
                    .putData("priority", req.getPriority())
                    .putData("advisory_id", advisory.getId().toString())
                    .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(req.getPriority().equals("high")
                            ? AndroidConfig.Priority.HIGH
                            : AndroidConfig.Priority.NORMAL)
                        .build())
                    .build();

                firebaseMessaging.send(message);
                fcmSent = true;
                advisory.setFcmSent(true);
                advisoryRepository.save(advisory);
            }
        } catch (Exception e) {
            // Don't throw — advisory is saved, in-app polling will catch it
            log.warn("FCM push failed for farmer {}: {}", req.getFarmerId(), e.getMessage());
        }

        return NotifyResponse.builder()
            .advisoryId(advisory.getId())
            .saved(true)
            .fcmSent(fcmSent)
            .build();
    }

    private String alertTypeLabel(String alertType) {
        return switch (alertType) {
            case "weather"    -> "हवामान सूचना";
            case "disease"    -> "रोग सूचना";
            case "irrigation" -> "सिंचन वेळ";
            case "fertilizer" -> "खत सल्ला";
            case "market"     -> "बाजार भाव";
            default           -> "सल्ला";
        };
    }
}
```

---

## Firebase Setup (Santosh — Day 1 Morning, do first)

```
1. Go to console.firebase.google.com
2. Create project: krishimitra-hackathon
3. Add Android app: package name com.krishimitra
4. Download google-services.json → place in mobile/android/app/
5. Go to Project Settings → Service Accounts → Generate new private key
6. Download krishimitra-firebase-adminsdk.json
7. Place in backend/src/main/resources/
8. Add to application.yml:
   firebase.credentials.path=classpath:krishimitra-firebase-adminsdk.json
```

```java
// FirebaseConfig.java
@Configuration
public class FirebaseConfig {
    @Value("${firebase.credentials.path}")
    private Resource credentialsResource;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        GoogleCredentials credentials = GoogleCredentials
            .fromStream(credentialsResource.getInputStream());
        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
```

---

## React Native — FCM Setup (Juhi — Day 1, ~2 hours)

```bash
npm install @react-native-firebase/app @react-native-firebase/messaging
cd ios && pod install
```

```typescript
// src/services/notifications.ts
import messaging from '@react-native-firebase/messaging';
import { authApi } from '../api/authApi';
import { store } from '../store';
import { addNotification, setUnreadCount } from '../store/notificationSlice';

// Call this right after OTP verify + farmer created
export async function registerFCMToken(farmerId: string) {
  try {
    const authStatus = await messaging().requestPermission();
    const granted =
      authStatus === messaging.AuthorizationStatus.AUTHORIZED ||
      authStatus === messaging.AuthorizationStatus.PROVISIONAL;

    if (granted) {
      const token = await messaging().getToken();
      await authApi.registerToken(farmerId, token);
      console.log('FCM token registered');
    }
  } catch (e) {
    console.warn('FCM registration failed:', e);
    // not fatal — in-app polling will still work
  }
}

// Call once in App.tsx root — handles foreground messages
export function setupForegroundListener() {
  return messaging().onMessage(async remoteMessage => {
    const { alert_type, priority, advisory_id } = remoteMessage.data ?? {};
    // Add to Redux store and show in-app toast
    store.dispatch(addNotification({
      id: advisory_id,
      alertType: alert_type,
      message: remoteMessage.notification?.body ?? '',
      priority,
      isRead: false,
      createdAt: new Date().toISOString(),
    }));
    store.dispatch(setUnreadCount(store.getState().notifications.unreadCount + 1));
  });
}

// Background/quit state — handled automatically by Firebase
// Just need this in index.js:
// messaging().setBackgroundMessageHandler(async () => {});
```

---

## React Native — In-App Polling Fallback (Santosh — 30 min)

```typescript
// src/store/notificationSlice.ts
import { createSlice } from '@reduxjs/toolkit';

const notificationSlice = createSlice({
  name: 'notifications',
  initialState: { items: [], unreadCount: 0 },
  reducers: {
    setAdvisories: (state, action) => {
      state.items = action.payload;
      state.unreadCount = action.payload.filter((a: any) => !a.is_read).length;
    },
    addNotification: (state, action) => {
      state.items.unshift(action.payload);
    },
    setUnreadCount: (state, action) => {
      state.unreadCount = action.payload;
    },
  },
});

export const { setAdvisories, addNotification, setUnreadCount } = notificationSlice.actions;
export default notificationSlice.reducer;
```

```typescript
// In HomeScreen.tsx — polling hook
import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { advisoryApi } from '../api/advisoryApi';
import { setAdvisories } from '../store/notificationSlice';

export function useAdvisoryPolling(farmerId: string) {
  const dispatch = useDispatch();

  useEffect(() => {
    // Fetch immediately on mount
    const fetchAdvisories = async () => {
      try {
        const res = await advisoryApi.getAll(farmerId);
        dispatch(setAdvisories(res.advisories));
      } catch (e) {
        console.warn('Advisory poll failed:', e);
      }
    };

    fetchAdvisories();
    const interval = setInterval(fetchAdvisories, 30000); // every 30s
    return () => clearInterval(interval);
  }, [farmerId]);
}
```

```typescript
// Bell icon in HomeScreen header
<TouchableOpacity onPress={() => navigation.navigate('Notifications')}>
  <View>
    <Icon name="bell" size={24} />
    {unreadCount > 0 && (
      <View style={styles.badge}>
        <Text style={styles.badgeText}>{unreadCount > 9 ? '9+' : unreadCount}</Text>
      </View>
    )}
  </View>
</TouchableOpacity>
```

---

## Python AI — Notify Trigger (Akshit)

```python
# services/notify_service.py
import httpx
import os

SPRING_BASE_URL = os.getenv("SPRING_BASE_URL", "http://spring-backend:8080")

async def trigger_notification(
    farmer_id: str,
    crop_id: str,
    alert_type: str,
    message_en: str,
    message_mr: str,
    priority: str
):
    """Call Spring Boot /notify endpoint. Fire-and-forget, never raise."""
    try:
        async with httpx.AsyncClient(timeout=5.0) as client:
            await client.post(
                f"{SPRING_BASE_URL}/api/v1/notify",
                json={
                    "farmer_id": farmer_id,
                    "crop_id": crop_id,
                    "alert_type": alert_type,
                    "message_en": message_en,
                    "message_mr": message_mr,
                    "priority": priority,
                }
            )
    except Exception as e:
        print(f"[WARN] Notification trigger failed: {e}")
        # don't raise — AI response already sent to Spring Boot


# In disease.py router — call after detection
async def detect_disease(file, crop_type, language, farmer_id, crop_id):
    result = await vision_service.analyze(file, crop_type, language)

    if result["severity"] == "high" and result["confidence"] > 0.70:
        await trigger_notification(
            farmer_id=farmer_id,
            crop_id=crop_id,
            alert_type="disease",
            message_en=f"Disease detected: {result['disease']}. {result['remedy_en']}",
            message_mr=f"रोग आढळला: {result['disease_mr']}. {result['remedy_mr']}",
            priority="high"
        )
    return result


# In weather.py router — call after weather fetch
async def get_weather_advice(lat, lon, crop, stage, lang, farmer_id, crop_id):
    result = await weather_service.fetch_and_advise(lat, lon, crop, stage, lang)

    if result["rainfall_mm"] > 20 or result["priority"] == "high":
        await trigger_notification(
            farmer_id=farmer_id,
            crop_id=crop_id,
            alert_type="weather",
            message_en=result["advice_en"],
            message_mr=result["advice_mr"],
            priority="high"
        )
    return result
```

---

## Docker Compose (Final)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: krishimitra
      POSTGRES_USER: krishi
      POSTGRES_PASSWORD: krishi123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  spring-backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/krishimitra
      DB_USER: krishi
      DB_PASS: krishi123
      AI_SERVICE_URL: http://python-ai:8000
      JWT_SECRET: hackathon_secret_key_2025
      FIREBASE_CREDENTIALS_PATH: classpath:krishimitra-firebase-adminsdk.json
    depends_on:
      - postgres

  python-ai:
    build: ./ai-service
    ports:
      - "8000:8000"
    environment:
      OPENWEATHER_API_KEY: ${OPENWEATHER_API_KEY}
      ANTHROPIC_API_KEY: ${ANTHROPIC_API_KEY}
      DB_URL: postgresql://krishi:krishi123@postgres:5432/krishimitra
      SPRING_BASE_URL: http://spring-backend:8080
    depends_on:
      - postgres
      - spring-backend

volumes:
  postgres_data:
```

---

## Folder Structure (Final)

```
krishimitra/
├── backend/                            # Priyanshu + Santosh
│   ├── src/main/java/com/krishimitra/
│   │   ├── auth/
│   │   │   ├── AuthController.java     ← login, verify, register-token
│   │   │   └── AuthService.java
│   │   ├── farmer/
│   │   ├── farm/
│   │   ├── crop/
│   │   ├── disease/
│   │   │   ├── DiseaseController.java  ← receives image, calls python-ai
│   │   │   └── DiseaseService.java
│   │   ├── advisory/
│   │   │   ├── AdvisoryController.java ← chat + GET advisories + PATCH read
│   │   │   └── AdvisoryService.java
│   │   ├── notification/
│   │   │   ├── NotifyController.java   ← POST /notify (called by python-ai)
│   │   │   ├── NotifyRequest.java
│   │   │   ├── NotifyResponse.java
│   │   │   └── NotificationService.java ← FCM + DB save
│   │   ├── weather/
│   │   ├── mandi/
│   │   └── config/
│   │       ├── SecurityConfig.java
│   │       ├── FirebaseConfig.java     ← Firebase Admin SDK init
│   │       └── WebClientConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── krishimitra-firebase-adminsdk.json   ← DO NOT COMMIT (gitignore)
│   │   └── db/migration/                        ← Flyway scripts
│   └── Dockerfile
│
├── ai-service/                         # Akshit
│   ├── main.py
│   ├── routers/
│   │   ├── disease.py                  ← Claude Vision, triggers notify
│   │   ├── advisory.py                 ← LLM chat, triggers notify on high
│   │   ├── weather.py                  ← OpenWeatherMap, triggers notify
│   │   └── mandi.py
│   ├── services/
│   │   ├── llm_service.py
│   │   ├── vision_service.py
│   │   ├── weather_service.py
│   │   ├── notify_service.py           ← calls POST /api/v1/notify on Spring
│   │   └── db_service.py
│   ├── requirements.txt
│   └── Dockerfile
│
├── mobile/                             # Juhi + Santosh
│   ├── src/
│   │   ├── screens/
│   │   │   ├── DiseaseDetectScreen.tsx
│   │   │   ├── AdvisoryChatScreen.tsx
│   │   │   ├── HomeScreen.tsx          ← polling hook + bell badge
│   │   │   ├── NotificationsScreen.tsx ← advisory list + mark read
│   │   │   ├── WeatherScreen.tsx
│   │   │   ├── MandiScreen.tsx
│   │   │   └── ... (auth + farm screens)
│   │   ├── services/
│   │   │   └── notifications.ts        ← FCM token register + foreground listener
│   │   ├── api/
│   │   │   ├── authApi.ts              ← includes registerToken()
│   │   │   ├── farmerApi.ts
│   │   │   └── advisoryApi.ts          ← getAll(), markRead()
│   │   ├── store/
│   │   │   ├── notificationSlice.ts    ← items, unreadCount
│   │   │   └── farmerSlice.ts
│   │   └── components/
│   │       ├── AdvisoryCard.tsx
│   │       ├── BellIcon.tsx            ← bell + badge count
│   │       └── WeatherWidget.tsx
│   ├── android/app/google-services.json  ← from Firebase console
│   ├── ios/GoogleService-Info.plist      ← from Firebase console
│   └── package.json
│
└── docker-compose.yml
```

---

## GitHub Tasks (Final — All Features)

### Day 1 Morning — Setup & Infrastructure
| Task | Owner |
|------|-------|
| Repo + branch strategy (main/dev) | Priyanshu |
| **Firebase project setup + download both config files** | Santosh |
| docker-compose + postgres + Flyway migrations | Santosh |
| Spring Boot init + JWT auth + mock OTP | Priyanshu |
| `POST /auth/register-token` endpoint | Priyanshu |
| `FirebaseConfig.java` — Firebase Admin SDK init | Priyanshu |
| Python FastAPI init + health check | Akshit |
| React Native init + navigation setup | Juhi |
| **FCM token registration in app after login** | Juhi |
| **`setupForegroundListener()` in App.tsx root** | Juhi |

### Day 1 Afternoon — Core Data APIs
| Task | Owner |
|------|-------|
| Farmer + Farm + Crop CRUD APIs | Priyanshu |
| Language select + Login + OTP + Profile screens | Juhi |
| Add Farm screen with map picker | Juhi |
| Add Crop screen | Santosh |
| **`notificationSlice.ts` + Redux store setup** | Santosh |
| **`useAdvisoryPolling()` hook in HomeScreen** | Santosh |

### Day 2 — AI Features + Notifications
| Task | Owner |
|------|-------|
| **Disease detect `/ai/disease-detect` (Claude Vision)** | Akshit |
| **`notify_service.py` — calls Spring /notify** | Akshit |
| **Trigger notify from disease router (severity=high)** | Akshit |
| **Trigger notify from weather router (rain > 20mm)** | Akshit |
| LLM advisory `/ai/advisory` with Marathi prompts | Akshit |
| Weather endpoint `/ai/weather` | Akshit |
| Mandi endpoint `/ai/mandi` | Akshit |
| **`NotifyController.java` + `NotificationService.java`** | Priyanshu |
| Spring DiseaseController → forwards to python-ai | Priyanshu |
| Spring AdvisoryController + chat endpoint | Priyanshu |
| Spring WeatherController + MandiController | Priyanshu |
| Disease Detect screen (camera + result card) | Juhi |
| Advisory Chat screen | Juhi |
| Weather screen | Santosh |
| Mandi screen | Santosh |
| **Bell icon with badge in HomeScreen header** | Santosh |

### Day 3 — Polish + Demo
| Task | Owner |
|------|-------|
| Home dashboard (advisory cards, quick action grid) | Juhi |
| **Notifications screen (list + mark as read)** | Santosh |
| Seed demo data (2 farmers, 2 farms, cotton + soybean) | Santosh |
| End-to-end flow test | All |
| **FCM demo dry-run: POST /notify via Postman → watch phone buzz** | Priyanshu |
| Docker clean build + smoke test | Santosh |
| Demo script + slides | Priyanshu |
| Demo video recording (backup) | Juhi |

---

## Disease Detection — Implementation (Akshit)

Use Claude/Gemini Vision directly — send base64 image to LLM with a structured prompt.

```python
# services/vision_service.py
import anthropic, base64

client = anthropic.Anthropic()

DISEASE_PROMPT = """
You are an expert plant pathologist for Indian agriculture (Maharashtra region).
Crop type: {crop_type}
Analyze this leaf image and respond ONLY in valid JSON (no markdown, no explanation):

{{
  "disease": "English disease name",
  "disease_mr": "रोगाचे नाव मराठीत",
  "confidence": 0.85,
  "severity": "high",
  "cause": "Brief cause",
  "remedy_en": "Specific remedy with dosage e.g. 5ml Profenofos per litre, spray in evening",
  "remedy_mr": "मराठीत उपाय (specific with dosage)",
  "consult_expert": true
}}

Rules:
- confidence: 0.0 to 1.0
- severity: low | medium | high
- If unclear image, set confidence < 0.5
- Never recommend unsafe chemical quantities
"""

async def analyze(image_bytes: bytes, crop_type: str, language: str) -> dict:
    b64 = base64.standard_b64encode(image_bytes).decode("utf-8")

    response = client.messages.create(
        model="claude-sonnet-4-20250514",
        max_tokens=1000,
        messages=[{
            "role": "user",
            "content": [
                {
                    "type": "image",
                    "source": {"type": "base64", "media_type": "image/jpeg", "data": b64}
                },
                {
                    "type": "text",
                    "text": DISEASE_PROMPT.format(crop_type=crop_type)
                }
            ]
        }]
    )

    import json
    return json.loads(response.content[0].text)
```

---

## LLM Advisory Prompt (Akshit)

```python
ADVISORY_PROMPT = """
You are KrishiMitra AI, an expert agricultural advisor for Indian farmers.
Always respond in {language} (Marathi/Hindi/English as specified).
Be concise, practical, and use simple language farmers understand.
Do NOT give unsafe advice. Recommend expert consultation for serious disease.

Context:
- Crop: {crop_type}
- Stage: {stage}
- Location: Maharashtra
- Weather: {weather_summary}

Farmer's question: {question}

Respond in 3-4 sentences. Include:
1. What the problem likely is
2. Specific action (e.g. "5kg urea per acre")
3. When to act (timeline)

Also return a JSON footer on the last line:
{{"alert_type": "fertilizer|disease|irrigation|weather|market", "priority": "high|medium|low"}}
"""
```

---

## Environment Variables

```bash
# Python AI (.env)
OPENWEATHER_API_KEY=       # free tier at openweathermap.org
ANTHROPIC_API_KEY=         # claude api key (claude-sonnet-4-20250514 supports vision)
SPRING_BASE_URL=http://spring-backend:8080
DB_URL=postgresql://krishi:krishi123@postgres:5432/krishimitra

# Spring Boot (application.yml)
jwt.secret: hackathon_secret_2025
ai.service.url: http://python-ai:8000
firebase.credentials.path: classpath:krishimitra-firebase-adminsdk.json

# React Native (.env)
API_BASE_URL=http://localhost:8080/api/v1
```

---

## Demo Script (3 min)

1. **(20s) Problem**: "140M Indian farmers. No expert guidance. One bad crop = year of income lost."
2. **(15s) App open**: Language → Marathi. Login with mock OTP → Ramesh Patil profile.
3. **(20s) Farm registered**: GPS pin on Pune Rural. Cotton crop added.
4. **(50s) 🌿 Disease Detection** ← MAIN AI MOMENT:
   - "Ramesh notices yellowing on cotton leaves."
   - Open camera → capture leaf → upload
   - Spinner: "AI analyzing..."
   - Result card: "Pink Bollworm attack (87% confidence)"
   - Marathi remedy: "कीटकनाशक फवारणी ४८ तासांत करा"
   - **📳 Phone buzzes simultaneously with Marathi push notification**
5. **(30s) AI Advisory chat**: Type in Marathi → expert advice in Marathi
6. **(20s) Weather + Mandi**: Rain alert card + soybean prices rising in Pune mandi
7. **(15s) Notifications screen**: Bell badge → 2 unread alerts, tap to mark read
8. **(20s) Impact**: "Disease detection alone saves ₹20,000/acre. 140M farmers. This is Day 1."

---

## Quick Test Commands

```bash
# Register FCM token (after login)
curl -X POST http://localhost:8080/api/v1/auth/register-token \
  -H "Content-Type: application/json" \
  -d '{"farmer_id":"uuid","fcm_token":"test-fcm-token"}'

# Trigger notification manually (great for demo dry-run)
curl -X POST http://localhost:8080/api/v1/notify \
  -H "Content-Type: application/json" \
  -d '{"farmer_id":"uuid","alert_type":"disease","message_en":"Disease detected","message_mr":"रोग आढळला","priority":"high"}'

# Check advisories (polling fallback test)
curl "http://localhost:8080/api/v1/advisories?farmer_id=uuid&unread=true"

# Disease detect (Python AI direct)
curl -X POST http://localhost:8000/ai/disease-detect \
  -F "file=@leaf.jpg" -F "crop_type=soybean" -F "language=marathi" -F "farmer_id=uuid"
```

---

*Team KrishiMitra — Priyanshu · Akshit · Juhi · Santosh*
