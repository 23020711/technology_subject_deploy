# PriceHawk - Deployment Guide for Railway

## Architecture Overview

This is a full-stack application with:
- **Backend**: Spring Boot 3.3.5 (Java 17) - API Server
- **Frontend**: React + Vite + TypeScript + TailwindCSS - SPA

## Prerequisites

- [Railway Account](https://railway.app) (with GitHub connected)
- [Supabase Account](https://supabase.com) for PostgreSQL database

## Services Required

### 1. Database (Supabase PostgreSQL)
Create a PostgreSQL database on Supabase. You'll need:
- `SUPABASE_URL` - Database connection URL
- `SUPABASE_API_KEY` - API key from Supabase

### 2. Railway Services

#### Backend Service
- **Runtime**: Java 17 with Maven
- **Port**: 8080
- **Health Check**: `/actuator/health`

#### Frontend Service
- **Runtime**: Node.js 20 (build) + Nginx (serve)
- **Port**: 80

## Environment Variables

### Backend Service Variables

```env
# Database (Supabase PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-1-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.astkanfsacxriwprspqr
SPRING_DATASOURCE_PASSWORD=your-supabase-password

# Redis (Railway Redis)
REDIS_HOST=your-redis-host.railway.app
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# Elasticsearch (Elastic Cloud)
ELASTICSEARCH_URI=https://9fd5ddb89d8f4941b93d478fcfb98350.asia-southeast1.gcp.elastic-cloud.com:443
ELASTICSEARCH_USERNAME=elastic
ELASTICSEARCH_PASSWORD=wDQ7RMbkTjH9YcQ8rEsZxYqv
ELASTICSEARCH_ENABLED=true

# Supabase Auth
SUPABASE_URL=https://astkanfsacxriwprspqr.supabase.co
SUPABASE_API_KEY=your-supabase-anon-key

# AI Service (Gemini)
AI_BASE_URL=https://generativelanguage.googleapis.com
AI_API_KEY=your-gemini-api-key
AI_MODEL=gemini-2.5-flash

# Email (Resend)
RESEND_API_KEY=your-resend-api-key

# Affiliate
ACCESSTRADE_API_KEY=your-accesstrade-api-key
```

### Frontend Service Variables

```env
# API Base URL (leave empty if using proxy)
VITE_API_BASE_URL=

# Enable API calls
VITE_USE_TRENDING_API=true
```

## Deployment Steps

### 1. Fork/Clone Repository
```bash
git clone https://github.com/your-username/pricehawk.git
cd pricehawk
```

### 2. Create Railway Project
1. Go to [Railway Dashboard](https://railway.app/dashboard)
2. Click "New Project" → "Deploy from GitHub repo"
3. Select your repository

### 3. Configure Backend Service
1. In Railway dashboard, click on the backend service
2. Go to "Settings" → "Health Check"
3. Set:
   - **Path**: `/actuator/health`
   - **Port**: `8080`
4. Go to "Variables" and add environment variables:
   - Add all backend environment variables from above
   - **Important**: Add `RAILWAY_PRIVATE_DOMAIN=backend` for service discovery

### 4. Configure Frontend Service
1. In Railway dashboard, click on the frontend service
2. Go to "Settings" → "Health Check"
3. Set:
   - **Path**: `/`
   - **Port**: `80`
4. Go to "Variables" and add:
   - `VITE_API_BASE_URL=` (leave empty for relative paths)
   - `VITE_USE_TRENDING_API=true`
5. Add a Nixpacks or Dockerfile build command if needed

### 5. Set Up Networking
1. Go to the frontend service settings
2. Under "Networking", add a public domain
3. Note the public URL (e.g., `https://frontend.up.railway.app`)

### 6. Configure Backend CORS
Update the backend's CORS configuration to allow requests from your frontend URL.

## Important Notes

### Service Discovery
Railway services can communicate via internal DNS:
- Backend: `http://backend:8080`
- Frontend: `http://frontend:80`

The nginx configuration proxies `/api/` requests to the backend service.

### Health Checks
The backend must expose `/actuator/health` for Railway to detect successful deployment.

### Database Migration
Flyway runs automatically on startup. Ensure the database is accessible before deploying.

### Performance Considerations
- Set appropriate memory limits for Java JVM
- Consider using Redis for session/cache management
- Monitor Elasticsearch usage if enabled

## Troubleshooting

### Backend won't start
- Check logs: `railway logs -s backend`
- Verify database credentials
- Check if port 8080 is correct

### Frontend shows 502/504
- Check backend is healthy
- Verify nginx proxy configuration
- Check environment variables

### Database connection issues
- Verify Supabase connection string
- Check IP whitelist settings
- Ensure SSL mode is configured

## Useful Railway Commands

```bash
# View logs
railway logs -s backend
railway logs -s frontend

# Open Railway shell
railway shell

# Redeploy
railway up -s backend
railway up -s frontend

# Check status
railway status
```

## Local Development

For local development with Railway services:

```bash
# Connect to Railway PostgreSQL locally
railway run --attach

# Or use local PostgreSQL and update application.yml
```
