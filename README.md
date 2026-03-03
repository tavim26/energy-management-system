# Energy Management System

## Descriere

Aplicatie microservicii cu autentificare JWT, control bazat pe roluri si procesare asincrona de date energetice:
- **ADMIN**: CRUD pe utilizatori si dispozitive, asociere devices la users
- **CLIENT**: Vizualizare propriile dispozitive, chat support, notificari real-time

**Assignment 2 Extensions:**
- Device Simulator: Genereaza date de consum la 10 minute
- Monitoring Service: Agregare orara consum energetic
- RabbitMQ: Message-Oriented Middleware pentru data collection si sincronizare

**Assignment 3 Extensions:**
- Customer Support Service: Chatbot inteligent (rule-based + AI-driven cu Google Gemini)
- WebSocket Service: Real-time notifications pentru overconsumption
- User-Specific Notifications: Fiecare user primeste doar notificari pentru propriile dispozitive

## Arhitectura

**Microservicii:**
- User Service (8081) + user_db (5433)
- Device Service (8082) + device_db (5434)  
- Authorization Service (8083) + auth_db (5435)
- Monitoring Service (8084) + monitoring_db (5436)
- Customer Support Service (8086) 
- WebSocket Service (8085) 
- API Gateway (8080)
- Frontend React (3000)

**Message Brokers:**
- RabbitMQ Data Broker (5672) - Device data collection
- RabbitMQ Sync Broker (5673) - Microservice synchronization + Notifications

**Device Simulator:**
- Standalone Java application
- Sends measurements every 10 minutes (or 10 seconds in DEMO mode)
- Format: `{timestamp, device_id, measurement_value}`

**Comunicare:**
- **Synchronous (REST)**: API Gateway -> Microservicii (routing + validare JWT)
- **Asynchronous (RabbitMQ)**:
  - Device Simulator -> Data Broker -> Monitoring Service (Point-to-Point)
  - User/Device Services -> Sync Broker -> All Services (Publish-Subscribe via Fanout Exchange)
  - Monitoring Service -> Sync Broker -> WebSocket Service (Overconsumption Notifications)
- **Real-Time (WebSocket/STOMP)**: WebSocket Service <-> Frontend (User-Specific Topics)

**Event-Based Synchronization:**
- `USER_CREATED`: Auth Service -> Device Service (users_copy)
- `DEVICE_CREATED`: Device Service -> Monitoring Service (device_copy with userId=NULL)
- `DEVICE_ASSIGNED`: Device Service -> Monitoring Service (update userId in device_copy)
- `DEVICE_DELETED`: Device Service -> Monitoring Service (cascade delete hourly_consumption)

## Prerequisite

- **Java 21**
- **Maven 3.8+**
- **Docker Desktop**
- **RabbitMQ** (inclus in docker-compose)
- **Google Gemini API Key** (pentru AI-driven customer support)

## Build si Executie

### 1. Configurare Google Gemini API Key:

Editare `docker-compose.yml`:
```yaml
customer-support-service:
  environment:
    - GEMINI_API_KEY=YOUR_API_KEY_HERE
    - GEMINI_API_URL=https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent
```

Obtine API key gratuit la: https://aistudio.google.com/app/apikey

### 2. Compilare servicii:
```bash
mvn clean package -DskipTests
```

### 3. Compilare Device Simulator (standalone):
```bash
cd device-simulator
mvn clean package
cd ..
```

### 4. Pornire containere:
```bash
docker-compose up -d --build
```

### 5. Verificare status:
```bash
docker-compose ps 
docker-compose logs -f monitoring-service
docker-compose logs -f customer-support-service
docker-compose logs -f websocket-service
docker-compose logs -f rabbitmq-sync
```

### 6. Pornire Device Simulator (local):
```bash
cd device-simulator
java -jar target/device-simulator-1.0.jar

# La prompt selecteaza:
# [1] DEMO MODE - 5 secunde (pentru prezentare)
# [2] NORMAL MODE - 10 minute (production-like)
```

## Acces

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Monitoring Service Swagger**: http://localhost:8084/swagger-ui.html
- **Customer Support Swagger**: http://localhost:8086/swagger-ui.html
- **RabbitMQ Management UI** (Data Broker): http://localhost:15672 (guest/guest)
- **RabbitMQ Management UI** (Sync Broker): http://localhost:15673 (guest/guest)

## Testing Assignment 3 Features

### 1. Customer Support Chatbot

**Test Rule-Based Response:**
1. Login ca CLIENT in frontend
2. Deschide chat-ul Customer Support
3. Trimite mesaj: "parola" sau "consum" sau "dispozitiv"
4. Primesti raspuns automat bazat pe reguli predefinite

**Test AI-Driven Response:**
1. Trimite mesaj care NU se potriveste cu regulile: "tipuri de pizza" sau "capitala Frantei"
2. Chatbot-ul foloseste Google Gemini pentru a genera raspuns inteligent
3. Raspunsul este contextual si relevant

**Reguli Predefinite (10+ reguli):**
- Parola / password -> Instructiuni reset parola
- Consum / energie -> Navigare la sectiunea Monitoring
- Dispozitiv / device -> CRUD dispozitive
- Cont / profil / account -> Actualizare profil
- Suport / ajutor / help -> Program disponibilitate
- Admin -> Contact administrator
- Factura / plata -> Sectiunea Billing
- Raport / export -> Export rapoarte
- Notificare / alert -> Notificari supraconsum
- Eroare / error / bug -> Troubleshooting

### 2. Real-Time Overconsumption Notifications

**Test Flow:**
1. Login ca ADMIN, creeaza un device cu `maxConsumption = 1.0` kWh
2. Asigneaza device-ul la un CLIENT (importante pentru user-specific notifications)
3. Logout si login ca CLIENT
4. Porneste Device Simulator in DEMO MODE
5. Selecteaza device-ul creat (ID-ul device-ului)
6. Asteapta ~40 secunde (4 masuratori x 10 secunde)
7. Cand `hourlyConsumption > 1.0 kWh`:
   - CLIENT-ul primeste notificare toast in timp real
   - DOAR client-ul care detine device-ul primeste notificare (user-specific)
   - Alti clienti logati NU primesc notificarea

**Verificare Logs:**
```bash
# Monitoring Service detecteaza overconsumption
docker logs -f monitoring-service | grep OVERCONSUMPTION
# Output: OVERCONSUMPTION ALERT sent for device 1 (user 3)

# WebSocket Service broadcast notificare
docker logs -f websocket-service | grep notification
# Output: Notification sent to user 3 at topic: /topic/notifications/3

# Frontend Console (browser DevTools)
# Output: Notification received: {type: "OVERCONSUMPTION", deviceId: 1, ...}
```

### 3. User-Specific WebSocket Topics


**WebSocket Topics:**
- Clienti se subscribe la: `/topic/notifications/{userId}`
- Monitoring Service include `userId` in notification payload
- WebSocket Service routeaza la topic specific user-ului

## Message Flow

### 1. Data Collection (Point-to-Point):
```
Device Simulator -> device-data-queue -> Monitoring Service
                    (RabbitMQ Data Broker)

Message format: 
{
  "timestamp": "2025-12-07T14:35:22",
  "device_id": 1,
  "measurement_value": 1532.45
}
```

### 2. Microservice Synchronization (Publish-Subscribe):
```
User/Device Service -> sync-exchange (Fanout) -> sync-queue-device
                                               -> sync-queue-monitoring
                                               -> sync-queue-auth

Event types:
- USER_CREATED: {eventType, userId, username, password, role, fullName, address}
- DEVICE_CREATED: {eventType, deviceId, deviceName, maxConsumption}
- DEVICE_ASSIGNED: {eventType, deviceId, userId} (NEW)
- DEVICE_DELETED: {eventType, deviceId}
```

### 3. Overconsumption Notification Flow (NEW):
```
Monitoring Service (detecteaza overconsumption)
    |
    v
notifications-queue (RabbitMQ Sync Broker)
    |
    v
WebSocket Service (consume notification)
    |
    v
/topic/notifications/{userId} (STOMP WebSocket)
    |
    v
Frontend Client (user-specific, toast notification)

Notification format:
{
  "type": "OVERCONSUMPTION",
  "deviceId": 1,
  "userId": 3,
  "consumption": 1.32,
  "limit": 1.0,
  "timestamp": "2025-12-07T14:00:00"
}
```

### 4. Customer Support Chat Flow (NEW):
```
Frontend -> POST /api/support/message
    |
    v
Customer Support Service
    |
    +-- Rule-Based Check (10+ rules)
    |   |
    |   +-- Match found? -> Return predefined response
    |   |
    |   +-- No match? -> Call AI Service
    |
    v
Google Gemini API (gemini-2.5-flash)
    |
    v
AI-Generated Response -> Return to Frontend
```

**Hourly Aggregation Logic:**
- Measurements received every 10 minutes
- Conversion: `kWh = Watts / 6000` (10 minutes = 1/6 hour)
- Truncate timestamp to hour: `14:35:22 -> 14:00:00`
- UPDATE existing record or INSERT new record for each hour
- Check overconsumption: `if (hourlyKwh > device.maxConsumption) -> publish notification`

## Structura Proiectului
```
├── user-management/          # User Service + UserRepository
├── device-management/        # Device Service + DeviceRepository + DEVICE_ASSIGNED event
├── authorization-service/    # Auth Service + JWT generation
├── monitoring-service/       # Monitoring + Overconsumption detection + userId tracking
├── customer-support-service/ # Chatbot (Rule-based + AI Gemini) (NEW)
├── websocket-service/        # WebSocket/STOMP + User-specific topics (NEW)
├── device-simulator/         # Device data generator
├── api-gateway/              # Routing + Authentication/Authorization filters
├── frontend/                 # React app + WebSocket client + Chat UI
├── docker-compose.yml        # Orchestration (includes all services + RabbitMQ)
└── deployment-diagram.png    # Deployment diagram (updated for Assignment 3)
```

## RabbitMQ Configuration

**Data Broker (localhost:5672):**
- Queue: `device-data-queue` (durable)
- Pattern: Point-to-Point
- Producer: Device Simulator
- Consumer: Monitoring Service

**Sync Broker (localhost:5673):**
- Exchange: `sync-exchange` (Fanout, durable)
- Queues:
  - `sync-queue-device` (consumed by Device Service)
  - `sync-queue-monitoring` (consumed by Monitoring Service)
  - `sync-queue-auth` (consumed by Auth Service)
  - `notifications-queue` (consumed by WebSocket Service) (NEW)
- Pattern: Publish-Subscribe (broadcast to all queues)
- Publishers: User Service, Device Service, Auth Service, Monitoring Service

## Database Schema Updates (Assignment 3)

**Monitoring Service - device_copy table:**
```sql
CREATE TABLE device_copy (
    device_id INTEGER PRIMARY KEY,
    max_consumption NUMERIC(10, 2),
    user_id INTEGER  
);
```

**Event Synchronization:**
- `DEVICE_CREATED`: device_copy salvat cu `userId=NULL`
- `DEVICE_ASSIGNED`: device_copy updatat cu `userId` din event payload
- `DEVICE_DELETED`: cascade delete pe hourly_consumption

## Technologies Used

**Backend:**
- Java 21 + Spring Boot 3.2.0
- Spring Web (REST APIs)
- Spring Data JPA (ORM)
- Spring AMQP (RabbitMQ integration)
- Spring WebSocket + STOMP (Real-time communication)
- PostgreSQL (persistent storage)
- RabbitMQ (message broker)
- Google Gemini API (AI-driven chatbot)
- Swagger/OpenAPI (API documentation)

**Frontend:**
- React 18
- Axios (HTTP client)
- SockJS + STOMP.js (WebSocket client)
- React Toastify (notifications UI)
- Tailwind CSS (styling)

**DevOps:**
- Docker + Docker Compose (containerization)
- Maven (build tool)

## Note

- Toate serviciile ruleaza in containere Docker
- Device Simulator ruleaza standalone (local) si se conecteaza la RabbitMQ via localhost:5672
- Datele sunt persistente in Docker volumes
- Frontend foloseste localStorage pentru JWT + userId
- RabbitMQ queues sunt durable (persist after broker restart)
- Hourly aggregation foloseste BigDecimal pentru precizie numerica
- Fanout Exchange broadcast events la toate microserviciile (no routing keys)
- WebSocket notifications sunt user-specific (fiecare user primeste doar propriile notificari)
- Customer Support chatbot foloseste Google Gemini 2.5 Flash model (free tier: 15 requests/minute)
- WebSocket reconnect automat la pierderea conexiunii (5 secunde delay)