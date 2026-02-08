# Setup Guide

Complete setup instructions for the E-Commerce Real-Time Analytics Platform.

## Prerequisites

### Required Software

- **Java 17+** - [Download](https://www.oracle.com/java/technologies/downloads/#java17)
- **Maven 3.9+** - For building Java projects
- **Node.js 18+** and npm - [Download](https://nodejs.org/)
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop/)

### Verify Installation

```powershell
java -version    # Should show Java 17 or higher
mvn -version     # Should show Maven 3.9 or higher
node -version    # Should show Node 18 or higher
npm -version     # Should show npm version
docker --version # Should show Docker version
```

---

## Quick Start (Automated)

### 1. Build Projects (First Time Only)

**Backend:**
```powershell
cd d:\<YourSetupDir>\analytics-backend
mvn clean package -DskipTests
```

**Event Generator:**
```powershell
cd d:\<YourSetupDir>\event-generator
mvn clean package -DskipTests
```

**Frontend:**
```powershell
cd d:\<YourSetupDir>\analytics-dashboard
npm install
```

### 2. Start All Services

```powershell
cd d:\<YourSetupDir>
.\start-dev.bat
```

**What it does:**
1. Starts MongoDB (port 27017)
2. Starts Redis (port 6379)
3. Starts Backend API (port 8080)
4. Starts Event Generator (sends events)
5. Starts Frontend Dashboard (port 5173)

### 3. Access Dashboard

Wait 15-20 seconds, then open: **http://localhost:5173**

### 4. Stop All Services

```powershell
cd d:\<YourSetupDir>
.\stop-all.bat
```

---

## Manual Setup

### Step 1: Build Projects

Follow the build steps from Quick Start above if not already done.

### Step 2: Start Infrastructure

**MongoDB:**
```powershell
docker run -d --name mongodb -p 27017:27017 mongo:6.0
```

**Redis:**
```powershell
docker run -d --name redis -p 6379:6379 redis:7.0-alpine
```

**Verify containers are running:**
```powershell
docker ps
```

### Step 3: Start Backend (Terminal 1)

```powershell
cd d:\<YourSetupDir>\analytics-backend
java -jar target\analytics-backend-1.0.0.jar
```

**Wait for:** `Started AnalyticsApplication in X seconds`

### Step 4: Start Event Generator (Terminal 2)

```powershell
cd d:\<YourSetupDir>tupDir>\event-generator
java -jar target\event-generator-1.0.0.jar
```

**Wait for:** `Event generation started!`

### Step 5: Start Frontend (Terminal 3)

```powershell
cd d:\<YourSetupDir>\analytics-dashboard
npm run dev
```

**Wait for:**
```
VITE v5.x.x ready in XXX ms
➜ Local: http://localhost:5173/
```

### Step 6: Access Dashboard

Open browser: **http://localhost:5173**

---

## Manual Stop

**Frontend:** Press `Ctrl+C` in Terminal 3

**Event Generator:** Press `Ctrl+C` in Terminal 2

**Backend:** Press `Ctrl+C` in Terminal 1

**Docker Containers:**
```powershell
docker stop mongodb redis
```

---

## Rebuilding After Code Changes

### Backend Changes

```powershell
cd d:\<YourSetupDir>\analytics-backend
mvn clean package -DskipTests
# Then restart backend
```

### Event Generator Changes

```powershell
cd d:\<YourSetupDir>\event-generator
mvn clean package -DskipTests
# Then restart event generator
```

### Frontend Changes

Frontend auto-reloads on file changes (no rebuild needed).

---

## Port Reference

| Service | Port | URL |
|---------|------|-----|
| MongoDB | 27017 | localhost:27017 |
| Redis | 6379 | localhost:6379 |
| Backend API | 8080 | http://localhost:8080 |
| Event Generator | 8081 | (Internal) |
| Frontend | 5173 | http://localhost:5173 |

---

## Common Issues

### Docker Not Running

**Error:** `Docker is not running`

**Solution:** Start Docker Desktop and wait for it to fully initialize.

### Port Already in Use

**Check what's using a port:**
```powershell
netstat -ano | findstr :8080
```

**Kill the process:**
```powershell
taskkill /F /PID <process_id>
```

### MongoDB/Redis Containers Stopped

**Check status:**
```powershell
docker ps -a
```

**Restart containers:**
```powershell
docker start mongodb redis
```

### JAR Files Not Found

**Error:** `target\analytics-backend-1.0.0.jar` not found

**Solution:** Build the project first (see Step 1 above).
