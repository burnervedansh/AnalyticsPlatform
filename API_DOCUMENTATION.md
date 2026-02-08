# API Documentation

Complete API reference for all components in the analytics platform.

---

## Backend API (Analytics Service)

**Base URL:** `http://localhost:8080`

### 1. Event Ingestion

**Endpoint:** `POST /api/events`

**Description:** Ingest user events for analytics processing.

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "timestamp": "2024-03-15T14:30:00Z",
  "user_id": "usr_123",
  "event_type": "page_view",
  "page_url": "/home",
  "session_id": "sess_456"
}
```

**Field Descriptions:**
- `timestamp` (string, required): ISO 8601 timestamp
- `user_id` (string, required): Unique user identifier
- `event_type` (string, required): Type of event (e.g., page_view, click)
- `page_url` (string, required): URL of the page
- `session_id` (string, required): Session identifier

**Success Response (201 Created):**
```json
{
  "status": "success",
  "eventId": "evt_789",
  "message": "Event ingested successfully"
}
```

**Error Responses:**

**400 Bad Request** - Invalid request body
```json
{
  "error": "Validation Error",
  "message": "Invalid event data",
  "status": 400,
  "timestamp": "2024-03-15T14:30:00Z",
  "validationErrors": {
    "user_id": "User ID is required"
  }
}
```

**429 Too Many Requests** - Rate limit exceeded
```json
{
  "error": "Rate Limit Exceeded",
  "message": "Too many events. Maximum 100 events per second.",
  "status": 429,
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Use Case:** Event generator sends user activity events to this endpoint for storage and processing.

---

### 2. Active Users Count

**Endpoint:** `GET /api/analytics/active-users`

**Description:** Get count of active users in the last 5 minutes.

**Request:** No parameters required.

**Success Response (200 OK):**
```json
{
  "activeUsers": 850,
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Field Descriptions:**
- `activeUsers` (number): Count of unique users active in last 5 minutes
- `timestamp` (string): Response generation timestamp

**Error Responses:**

**500 Internal Server Error** - Redis connection failure
```json
{
  "activeUsers": 0,
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Use Case:** Dashboard displays this metric to show current user activity.

---

### 3. Top Pages

**Endpoint:** `GET /api/analytics/top-pages`

**Description:** Get most visited pages in the last 15 minutes.

**Query Parameters:**
- `limit` (optional, default: 5): Number of top pages to return (1-100)

**Example Request:**
```
GET /api/analytics/top-pages?limit=10
```

**Success Response (200 OK):**
```json
{
  "pages": [
    {
      "url": "/products/electronics",
      "views": 1250
    },
    {
      "url": "/home",
      "views": 980
    },
    {
      "url": "/cart",
      "views": 650
    }
  ],
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Field Descriptions:**
- `pages` (array): List of page view counts
  - `url` (string): Page URL
  - `views` (number): Number of views
- `timestamp` (string): Response generation timestamp

**Error Responses:**

**500 Internal Server Error** - Redis connection failure
```json
{
  "pages": [],
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Use Case:** Dashboard displays a bar chart of most popular pages.

---

### 4. Active Sessions (Single User)

**Endpoint:** `GET /api/analytics/active-sessions`

**Description:** Get active sessions for a specific user.

**Query Parameters:**
- `userId` (required): User ID to query

**Example Request:**
```
GET /api/analytics/active-sessions?userId=usr_123
```

**Success Response (200 OK):**
```json
{
  "userId": "usr_123",
  "activeSessions": 2,
  "sessions": [
    "sess_456",
    "sess_789"
  ],
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Field Descriptions:**
- `userId` (string): Requested user ID
- `activeSessions` (number): Count of active sessions
- `sessions` (array): List of session IDs
- `timestamp` (string): Response generation timestamp

**Error Responses:**

**400 Bad Request** - Missing userId parameter
```
HTTP 400 Bad Request
```

**Use Case:** Query sessions for a specific user.

---

### 5. Recent Active Sessions

**Endpoint:** `GET /api/analytics/recent-sessions`

**Description:** Get recent users with active sessions (sorted by session count).

**Query Parameters:**
- `limit` (optional, default: 5): Number of users to return (1-50)

**Example Request:**
```
GET /api/analytics/recent-sessions?limit=5
```

**Success Response (200 OK):**
```json
{
  "users": [
    {
      "userId": "usr_123",
      "activeSessions": 3,
      "sessions": ["sess_1", "sess_2", "sess_3"]
    },
    {
      "userId": "usr_456",
      "activeSessions": 2,
      "sessions": ["sess_4", "sess_5"]
    }
  ],
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Field Descriptions:**
- `users` (array): List of users with sessions
  - `userId` (string): User identifier
  - `activeSessions` (number): Session count
  - `sessions` (array): List of session IDs
- `timestamp` (string): Response generation timestamp

**Error Responses:**

**500 Internal Server Error** - Redis connection failure
```json
{
  "users": [],
  "timestamp": "2024-03-15T14:30:00Z"
}
```

**Use Case:** Dashboard displays table of users with most active sessions.

---

### 6. Health Check

**Endpoint:** `GET /actuator/health`

**Description:** Check if backend service is running.

**Success Response (200 OK):**
```json
{
  "status": "UP"
}
```

**Use Case:** Monitoring and deployment health checks.

---

## Event Generator API

**Base URL:** `http://localhost:8081`

The event generator is an internal service that sends events to the backend. It does not expose public APIs for external consumption.

**Configuration:**
- Events per second: 100
- User pool size: 800-1000 (changes every 5 seconds)
- Target: `POST http://localhost:8080/api/events`

---

## Frontend API (React Dashboard)

**Base URL:** `http://localhost:5173`

The frontend is a single-page React application that consumes the Backend API.

### Internal API Client

**Location:** `src/services/analyticsApi.js`

**Functions:**

1. **getActiveUsers()**
   - Calls: `GET /api/analytics/active-users`
   - Returns: `{ activeUsers, timestamp }`

2. **getTopPages(limit)**
   - Calls: `GET /api/analytics/top-pages?limit={limit}`
   - Returns: `{ pages, timestamp }`

3. **getActiveSessions(userId)**
   - Calls: `GET /api/analytics/active-sessions?userId={userId}`
   - Returns: `{ userId, activeSessions, sessions, timestamp }`

4. **getRecentSessions(limit)**
   - Calls: `GET /api/analytics/recent-sessions?limit={limit}`
   - Returns: `{ users, timestamp }`

**Error Handling:**
All functions throw errors on failure. The dashboard displays:
```
"Failed to fetch analytics data. Make sure the backend is running."
```

**Auto-Refresh:**
- Interval: 30 seconds
- All metrics refresh automatically

---

## Rate Limiting

**Backend Event Ingestion:**
- **Limit:** 100 events per second
- **Burst Capacity:** 200 events
- **Algorithm:** Token bucket (Bucket4j)
- **Response:** HTTP 429 when limit exceeded

---

## CORS Configuration

**Allowed Origins:**
- `http://localhost:3000`
- `http://localhost:5173`
- `http://frontend:3000`

**Allowed Methods:**
- GET, POST, PUT, DELETE, OPTIONS

**Allowed Headers:**
- All headers allowed
