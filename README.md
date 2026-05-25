# PatchPilot

A diagnostic assistant for IT support technicians. Paste a Windows installation error in plain language and PatchPilot returns three probability-ranked causes with fix steps. The answers are grounded in a small knowledge base of real install failures, not pulled from whatever Claude happens to remember.

## Live demo

Backend on AWS Elastic Beanstalk with RDS PostgreSQL. Frontend on Vercel.

Live URL: _added after deployment_

## Why this exists

If you paste "MSI Error 1603" into ChatGPT, you get one confident answer that's wrong half the time. Real install failures have multiple plausible causes, and the symptom rarely tells you which one you're looking at.

PatchPilot uses a small RAG pipeline to fix that. When a technician submits an error, the backend matches keywords against a knowledge base of eight hand-written articles on common Windows install failures, injects the matches into Claude's prompt as context, and asks Claude for three ranked causes with fix steps. The technician gets useful suggestions instead of one confident guess, and the answers cite articles that actually exist.

Eight articles is a small KB. I wrote them by hand to cover the install failures that show up most often, and the pipeline doesn't care if there are 8 or 800.

## Architecture

```
User
  -> React frontend (Vercel)
  -> Spring Boot API (AWS Elastic Beanstalk)
  -> KB lookup (RDS PostgreSQL)
  -> Anthropic Claude API with KB-injected prompt
  -> Ranked diagnosis returned to user
  -> Ticket saved to Postgres
```

## Tech stack

Backend: Java 21, Spring Boot 4.0.6, Spring Data JPA, PostgreSQL 18, Bucket4j for rate limiting, Maven.

Frontend: React 19, Vite 8, Tailwind CSS v4, Axios, Framer Motion, React Markdown.

AI: Anthropic Claude Sonnet 4.5.

Infrastructure: AWS Elastic Beanstalk, AWS RDS, Vercel.

## What it does

The diagnose endpoint returns three causes ranked by probability. Each cause has a severity (CURIOUS, INCONVENIENT, or BLOCKING) and step-by-step fixes that reference the KB articles used to generate them.

Diagnose is rate-limited to five requests per IP per hour via Bucket4j, which keeps the Anthropic bill bounded if anyone decides to hammer the endpoint.

The 8-article KB covers the install failures that actually show up in tickets: MSI 1603, missing VC++ redist, antivirus blocks, disk space, UAC elevation, leftover conflicting installs, .NET version mismatches, and Windows Installer service issues.

Other details worth knowing: every diagnosis is saved as a ticket through JPA. CORS origins come from an environment variable instead of a wildcard. Dev and prod use separate Spring profiles, so SQL logging is on locally and off in production, and Hibernate auto-migrates the schema in dev but only validates it in prod.

## Project structure

```
patchpilot/
  backend/
    src/main/java/ai/patchpilot/api/
      controller/    REST endpoints
      service/       Claude API integration, business logic
      dto/           Request/response types
      model/         JPA entities
      config/        CORS, rate limiting
      exception/     Centralized error handling
    src/main/resources/
      application.properties
      application-dev.properties
      application-prod.properties
    pom.xml
  frontend/
    src/
      components/
      api.js
      featureModalsContent.js
    package.json
    vite.config.js
  SETUP.md
  README.md
  .gitignore
```

## Running locally

You need Java 21, Node 20+, PostgreSQL 18, and an Anthropic API key.

Backend:

```
cd backend
cp .env.example .env
# fill in DB_NAME, DB_USERNAME, DB_PASSWORD, ANTHROPIC_API_KEY
./mvnw spring-boot:run
```

Frontend:

```
cd frontend
npm install
npm run dev
```

Backend runs on 8080, frontend on 5173. The frontend defaults `VITE_API_BASE_URL` to `http://localhost:8080/api` if you don't set it.

Full setup notes are in SETUP.md.

## API endpoints

Public:
- `GET /api/health` — liveness check
- `POST /api/diagnose` — submit an error, get ranked diagnosis (rate-limited)
- `GET /api/kb?category=...&limit=N` — list KB article summaries
- `GET /api/kb/{id}` — full KB article

Disabled until admin auth is built:
- `GET /api/tickets`
- `GET /api/tickets/{id}`
- `PATCH /api/tickets/{id}/status`

## Design decisions

**RAG over fine-tuning.** Fine-tuning on eight articles would be expensive and you'd have to retrain every time the KB changes. RAG lets the KB grow without retraining and keeps each article traceable to a real document.

**Diagnosis JSON stored as TEXT.** A diagnosis is three nested causes with multiple fix steps and KB references. The shape was still moving when I built this, so I stored the raw JSON in a TEXT column and parse to typed DTOs at the API boundary. A normalized schema is the right call once the format settles down. It hasn't.

**Custom severity labels instead of low/medium/high.** Generic severity scales don't help when you're triaging tickets. I went with CURIOUS, INCONVENIENT, and BLOCKING because those words tell you what to do: look at it later, fix it now, or drop everything because the user can't work.

**Ticket endpoints disabled in production.** The list and detail endpoints return every user's submitted error logs and AI diagnoses, with no auth in front of them. Building admin auth was out of scope for the MVP, so disabling them was the safe move. They come back once auth is in.

## Roadmap

- Admin auth on ticket endpoints so they can come back online
- Trust the cloud proxy's headers properly so the rate limiter uses real client IPs
- More KB articles. macOS and Linux are the obvious next categories
- A ticket dashboard UI that uses the pagination API I already wired up
- Vector embeddings on KB articles instead of fuzzy keyword matching

## About

Built by Arpit Patel. Computer Programming Graduate. This is a portfolio project. GitHub, LinkedIn, and email links are in the footer of the deployed site.