# E-Commerce Real-Time Analytics Platform

A real-time analytics system that processes 50+ events per second and provides instant insights through a modern dashboard.

## Overview

This platform simulates and analyzes e-commerce user behavior in real-time, providing metrics on:
- **Active Users** - Count of users active in last 5 minutes (800-1000, updates every 5 seconds)
- **Top Pages** - Most visited pages in last 15 minutes
- **Active Sessions** - Most recent Users with multiple concurrent sessions

## Quick Start

```powershell
cd d:\<YourSetupDir<\
.\start-dev.bat
```

Wait 15-20 seconds, then open: **http://localhost:5173**

## Documentation

- **[SETUP.md](./SETUP.md)** - Prerequisites and installation instructions
- **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)** - Complete API reference for all components
- **[ARCHITECTURE.md](./ARCHITECTURE.md)** - High-level design and data flow
- **[FUTURE_IMPROVEMENTS.md](./FUTURE_IMPROVEMENTS.md)** - Future enhancements and roadmap

## Project Structure

```
<YourSetupDir>/
├── analytics-backend/       # Spring Boot API and processing
├── event-generator/         # Event simulation service
├── analytics-dashboard/     # React frontend
├── start-dev.bat           # Start all services
├── stop-all.bat            # Stop all services
├── SETUP.md                # Setup guide
├── API_DOCUMENTATION.md    # API reference
└── ARCHITECTURE.md         # System design
```

## Tech Stack

| Component | Technology |
|-----------|-----------|
| **Backend** | Java 17, Spring Boot 3.2 |
| **Frontend** | React 18, Vite 5 |
| **Storage** | MongoDB 6.0 |
| **Cache** | Redis 7.0 |
| **Container** | Docker |

## Key Features

- ⚡ **Real-Time Processing** - Metrics updated every 10 seconds
- 🔒 **Rate Limiting** - Token bucket (100 events/sec)
- 📊 **Live Dashboard** - Auto-refresh every 5 seconds
- 🎨 **Modern UI** - Dark theme, responsive design
- 🐳 **Docker Ready** - Containerized infrastructure
- 📝 **Well Documented** - Complete guides and API docs
