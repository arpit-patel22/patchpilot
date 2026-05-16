# PatchPilot — Local Setup

## Environment Variables Required

### Backend (set in your shell, or as Maven runtime profile)

| Variable | Purpose | Local Default |
|---|---|---|
| `ANTHROPIC_API_KEY` | Claude API access | (required, no default) |
| `MYSQL_PASSWORD` | MySQL DB password | `1234` (dev fallback) |

### Frontend (set in `.env.development` for local, Vercel dashboard for prod)

| Variable | Purpose | Local Default |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API URL | `http://localhost:8080/api` |

## PowerShell setup (Windows)

```powershell
$env:ANTHROPIC_API_KEY = "sk-ant-..."
$env:MYSQL_PASSWORD = "your_password_here"
cd backend
.\mvnw.cmd spring-boot:run
```

## Deployment

Backend: Render (Docker). Set MYSQL_PASSWORD, ANTHROPIC_API_KEY, CORS_ALLOWED_ORIGINS in Render dashboard.
Frontend: Vercel. Set VITE_API_BASE_URL in Vercel dashboard pointing to Render backend URL.
