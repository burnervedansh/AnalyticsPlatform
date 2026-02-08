# System Architecture

High-level design and data flow for the E-Commerce Real-Time Analytics Platform.

---

## System Overview

```
┌─────────────────┐
│ Event Generator │ (Simulates user activity)
└────────┬────────┘
         │ HTTP POST
         │ 100 events/sec
         ▼
┌─────────────────────────────────────────────────────────┐
│              Backend API (Spring Boot)                  │
│  ┌──────────────┐  ┌─────────────────┐                 │
│  │ Rate Limiter │→ │ Event Ingestion │                 │
│  │ (100 evt/s)  │  │    Service      │                 │
│  └──────────────┘  └────────┬────────┘                 │
│                              │                          │
│                              ▼                          │
│                     ┌────────────────┐                  │
│                     │    MongoDB     │ (Persistent)     │
│                     │  Event Store   │                  │
│                     └────────┬───────┘                  │
│                              │                          │
│                              │ Query (every 10s)        │
│                              ▼                          │
│                     ┌─────────────────┐                 │
│                     │  Real-Time      │                 │
│                     │  Processor      │                 │
│                     │  (@Scheduled)   │                 │
│                     └────────┬────────┘                 │
│                              │                          │
│                              │ Store metrics            │
│                              ▼                          │
│                     ┌────────────────┐                  │
│                     │     Redis      │ (Cache)          │
│                     │  Metrics Store │                  │
│                     └────────┬───────┘                  │
│                              │                          │
│                              │ Read metrics             │
│                              ▼                          │
│                     ┌─────────────────┐                 │
│                     │ Analytics Query │                 │
│                     │    Service      │                 │
│                     └────────┬────────┘                 │
│                              │                          │
└──────────────────────────────┼──────────────────────────┘
                               │ HTTP GET
                               │ JSON response
                               ▼
                      ┌─────────────────┐
                      │ React Dashboard │
                      │  (Auto-refresh  │
                      │   every 5s)     │
                      └─────────────────┘
```

---

## Component Architecture

### 1. Event Generator

**Purpose:** Simulate realistic user activity for testing and demonstration.

**Technology:** Java 17, Spring Boot

**Key Features:**
- Generates 100 events per second
- Dynamic user pool: 800-1000 users (changes every 5 seconds)
- Random session generation: 1-3 sessions per user
- Varied event types: page_view, click, add_to_cart, etc.

**Output:**
- HTTP POST to Backend API
- JSON event payload

---

### 2. Backend API

**Purpose:** Central service for event ingestion, processing, and analytics queries.

**Technology:** Java 17, Spring Boot 3.2

#### 2.1 Rate Limiter

**Algorithm:** Token bucket (Bucket4j)

**Configuration:**
- Capacity: 100 events/second
- Burst: 200 events

**Behavior:**
- Accepts events within limit
- Returns HTTP 429 when exceeded

#### 2.2 Event Ingestion Service

**Responsibilities:**
- Validate incoming events
- Store events in MongoDB
- Return success/error response

**Validation:**
- Required fields: timestamp, user_id, event_type, page_url, session_id
- Timestamp format: ISO 8601

#### 2.3 MongoDB (Event Store)

**Purpose:** Persistent storage for all raw events.

**Schema:**
```
{
  _id: ObjectId,
  userId: String,
  sessionId: String,
  eventType: String,
  pageUrl: String,
  timestamp: Date,
  createdAt: Date
}
```

**Indexes:**
- `createdAt` (for time-based queries)
- `userId` (for user-specific queries)

**Retention:** Events older than 24 hours are cleaned up (scheduled daily at 2 AM).

#### 2.4 Real-Time Processor

**Purpose:** Aggregate raw events into analytics metrics.

**Execution:** `@Scheduled(fixedDelay = 10000)` - Runs every 10 seconds

**Processing Steps:**

1. **Active Users (Last 5 minutes):**
   - Query MongoDB for events in last 5 minutes
   - Count distinct `userId`
   - Store count in Redis: `metrics:active_users`
   - TTL: 5 minutes

2. **Page Views (Last 15 minutes):**
   - Query MongoDB for page_view events in last 15 minutes
   - Group by `pageUrl` and count
   - Store in Redis Hash: `metrics:page_views`
   - TTL: 15 minutes

3. **Active Sessions (Last 5 minutes):**
   - Query MongoDB for events in last 5 minutes
   - Group by `userId`, collect unique `sessionId`
   - Store in Redis Sets: `metrics:sessions:{userId}`
   - TTL: 5 minutes

#### 2.5 Redis (Metrics Cache)

**Purpose:** Fast in-memory storage for pre-calculated metrics.

**Data Structures:**

| Key | Type | Value | TTL |
|-----|------|-------|-----|
| `metrics:active_users` | String | `100` | 5 min |
| `metrics:page_views` | Hash | `{"/home": 150, "/cart": 89}` | 15 min |
| `metrics:sessions:usr_1` | Set | `["sess_1", "sess_2"]` | 5 min |

**Why Redis?**
- Sub-millisecond read latency
- Reduces MongoDB query load
- Automatic expiration (TTL)

#### 2.6 Analytics Query Service

**Purpose:** Serve analytics data to frontend.

**Endpoints:**
- `/api/analytics/active-users` → Read from Redis
- `/api/analytics/top-pages` → Read from Redis Hash
- `/api/analytics/recent-sessions` → Scan Redis keys, aggregate

**Error Handling:**
- Returns empty/zero values on Redis failure
- Logs errors for monitoring

---

### 3. Frontend Dashboard

**Purpose:** Visualize real-time analytics data.

**Technology:** React 18, Vite 5

**Components:**

1. **Active Users Card**
   - Displays count from `/api/analytics/active-users`
   - Large metric display (2.5rem font)

2. **Top Pages Chart**
   - Horizontal bar chart
   - Data from `/api/analytics/top-pages?limit=10`
   - Shows page URL and view count

3. **Active Sessions Table**
   - Lists users with active sessions
   - Data from `/api/analytics/recent-sessions?limit=5`
   - Shows userId, session count, session IDs

**Auto-Refresh:**
- Interval: 30 seconds
- Fetches all metrics simultaneously
- Updates "Last updated" timestamp

---

## Data Flow

### Event Ingestion Flow

```
1. Event Generator creates event
   ↓
2. HTTP POST to /api/events
   ↓
3. Rate Limiter checks capacity
   ↓ (if allowed)
4. Event Ingestion Service validates
   ↓
5. Store in MongoDB
   ↓
6. Return success response
```

---

### Metrics Processing Flow

```
1. Scheduled task triggers (every 10s)
   ↓
2. Query MongoDB for recent events
   ↓
3. Aggregate data:
   - Count distinct users
   - Group page views by URL
   - Collect sessions per user
   ↓
4. Store results in Redis with TTL
   ↓
5. Log completion
```

---

### Dashboard Query Flow

```
1. Frontend timer triggers (every 30s)
   ↓
2. Parallel API calls:
   - GET /api/analytics/active-users
   - GET /api/analytics/top-pages
   - GET /api/analytics/recent-sessions
   ↓
3. Analytics Query Service reads Redis
   ↓
4. Return JSON responses
   ↓
5. Frontend updates UI
   ↓
6. Display "Last updated" timestamp
```

---

## Scalability Considerations

### Current Limits

- **Event ingestion:** 100 events/second (rate limited)
- **Event generation:** 100 events/second
- **Processing frequency:** Every 10 seconds
- **Dashboard refresh:** Every 30 seconds

### Bottlenecks

1. **MongoDB queries** - Real-time processor queries all recent events
2. **Redis key scanning** - `/recent-sessions` scans all session keys
3. **Single instance** - No horizontal scaling

### Scaling Strategies

1. **Increase rate limit** - Adjust Bucket4j configuration
2. **Add MongoDB indexes** - Optimize time-based queries
3. **Use Redis Sorted Sets** - Replace key scanning with sorted queries
4. **Horizontal scaling** - Deploy multiple backend instances with load balancer
5. **Async processing** - Use message queue (Kafka/RabbitMQ) for event ingestion

---

## Technology Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **Event Generation** | Java 17, Spring Boot | Simulate user events |
| **Backend** | Java 17, Spring Boot 3.2 | API and processing |
| **Storage** | MongoDB 6.0 | Persistent event store |
| **Cache** | Redis 7.0 | Fast metrics storage |
| **Frontend** | React 18, Vite 5 | Dashboard UI |
| **Rate Limiting** | Bucket4j | Token bucket algorithm |
| **Containerization** | Docker | MongoDB and Redis |

---

## Deployment Architecture

```
┌──────────────────────────────────────────┐
│           Local Development              │
│                                          │
│  ┌────────────┐  ┌────────────┐         │
│  │  MongoDB   │  │   Redis    │         │
│  │  (Docker)  │  │  (Docker)  │         │
│  └────────────┘  └────────────┘         │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │      Backend API (JAR)             │  │
│  │      Event Generator (JAR)         │  │
│  │      Frontend (npm run dev)        │  │
│  └────────────────────────────────────┘  │
│                                          │
│  All services run on localhost          │
└──────────────────────────────────────────┘
```

**Ports:**
- MongoDB: 27017
- Redis: 6379
- Backend: 8080
- Event Generator: 8081 (internal)
- Frontend: 5173
