# PatchPilot — Local Setup

## Environment Variables Required

### Backend (set in your shell, or as Maven runtime profile)

| Variable | Purpose | Local Default |
|---|---|---|
| `ANTHROPIC_API_KEY` | Claude API access | (required, no default) |
| `DB_HOST` | Database hostname | `localhost` (set to RDS endpoint in production) |
| `DB_NAME` | PostgreSQL database name | (required) |
| `DB_USERNAME` | PostgreSQL username | (required) |
| `DB_PASSWORD` | PostgreSQL password | (required) |

### Frontend (set in `.env.development` for local, Vercel dashboard for prod)

| Variable | Purpose | Local Default |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API URL | `http://localhost:8080/api` |

## PowerShell setup (Windows)

```powershell
$env:ANTHROPIC_API_KEY = "sk-ant-..."
$env:DB_NAME = "patchpilot"
$env:DB_USERNAME = "your_db_user"
$env:DB_PASSWORD = "your_db_password"
cd backend
.\mvnw.cmd spring-boot:run
```

## Deployment

Backend: Render (Docker). Set `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `ANTHROPIC_API_KEY`, `CORS_ALLOWED_ORIGINS`, and `SPRING_PROFILES_ACTIVE=prod` in Render dashboard.
Frontend: Vercel. Set `VITE_API_BASE_URL` in Vercel dashboard pointing to Render backend URL.
