# PatchPilot

A diagnostic tool for IT support work. Paste a Windows installation error, get three ranked causes with fix steps. Answers come from a small KB of real install failures first — Claude fills in the gaps, not the other way around.

**Live:** [patchpilot.online](https://patchpilot.online) · [GitHub](https://github.com/arpit-patel22/patchpilot)

---

### Built with

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_18-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![React](https://img.shields.io/badge/React_19-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Vite](https://img.shields.io/badge/Vite_8-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![TailwindCSS](https://img.shields.io/badge/Tailwind_v4-38B2AC?style=for-the-badge&logo=tailwind-css&logoColor=white)
![Anthropic](https://img.shields.io/badge/Claude_Sonnet_4.5-D97757?style=for-the-badge&logo=anthropic&logoColor=white)

### Deployed on

![AWS](https://img.shields.io/badge/AWS_Elastic_Beanstalk-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![AWS CloudFront](https://img.shields.io/badge/AWS_CloudFront-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS Amplify](https://img.shields.io/badge/AWS_Amplify-FF9900?style=for-the-badge&logo=awsamplify&logoColor=white)

### Status

![Live](https://img.shields.io/badge/status-live-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blue?style=for-the-badge)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=for-the-badge)

---

## The problem it solves

Paste "MSI Error 1603" into ChatGPT and you get one confident answer that's wrong about half the time. Real install failures have multiple plausible causes, and the symptom alone rarely tells you which one you're looking at.

PatchPilot runs a small RAG pipeline instead. When you submit an error, the backend fuzzy-matches it against eight hand-written KB articles covering the install failures that actually show up in tickets. Matching articles get injected into Claude's prompt as context. Claude returns three causes ranked by probability, each with step-by-step fixes that reference the articles used to generate them.

Eight articles is a deliberately small KB — I wrote them by hand rather than scraping forums. The pipeline doesn't care if there are 8 or 800.

---

## Stack

| Layer | Tech |
|---|---|
| Backend | Java 21, Spring Boot 4.0.6, Spring Data JPA, Hibernate, Bucket4j |
| Frontend | React 19, Vite 8, Tailwind CSS v4, Axios, React Markdown |
| Database | PostgreSQL 18 via AWS RDS |
| AI | Anthropic Claude Sonnet 4.5 |
| Infrastructure | AWS Elastic Beanstalk, AWS RDS, AWS CloudFront, AWS Amplify |
| Domain | patchpilot.online (Namecheap → AWS Route 53) |

---

## Architecture

```
User
  → React frontend (AWS Amplify)
  → CloudFront (HTTPS termination + CDN)
  → Spring Boot API (AWS Elastic Beanstalk, t3.micro)
  → KB fuzzy-match (RDS PostgreSQL)
  → Claude Sonnet 4.5 (Anthropic API, KB-injected prompt)
  → Ranked diagnosis returned to user
  → Ticket saved to Postgres
```

CloudFront sits in front of the EB backend to solve the HTTPS/mixed-content problem — Amplify serves HTTPS, EB runs HTTP, CloudFront bridges them.

---

## What it does

Submit a software name, OS, error message, and severity level (CURIOUS / INCONVENIENT / BLOCKING). The backend:

1. Fuzzy-matches the error against the KB
2. Injects matching articles into Claude's system prompt
3. Asks Claude for three ranked causes with fix steps
4. Saves the result as a ticket in Postgres
5. Returns the diagnosis with a ticket ID

Each diagnosis includes a "Try this first" quick fix, three probability-ranked causes with percentage scores, expandable fix steps per cause, and a ticket ID for reference.

The diagnose endpoint is rate-limited to five requests per IP per hour via Bucket4j. `ForwardedHeaderFilter` is registered so Spring reads real client IPs from CloudFront's forwarded headers instead of the internal EB address.

---

## Knowledge base

Eight articles covering the failures that actually show up in tickets:

- MSI Error 1603 (catch-all fatal installer error)
- Missing VC++ Redistributable
- Antivirus / SmartScreen blocking installer execution
- Insufficient disk space
- UAC elevation / administrator permissions
- Conflicting older version still installed
- .NET Framework version mismatch
- Windows Installer service not running (Error 1719)

macOS and Linux coverage is planned for v2.

---

## Security

- CORS locked to `patchpilot.online` — no wildcard
- Rate limiting via Bucket4j (5 req/IP/hour on `/api/diagnose`)
- `ForwardedHeaderFilter` — real client IP from CloudFront, not internal proxy IP
- Security headers on all responses: `X-Frame-Options`, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`, `X-XSS-Protection`
- DB credentials and API keys in environment variables only — nothing in code or git history
- RDS not publicly accessible — only reachable from the EB security group
- Separate Spring profiles for dev and prod (SQL logging off, DDL validate-only in prod)
- Anthropic API budget capped — auto-reload disabled

---

## API endpoints

**Public:**
```
GET  /api/health
POST /api/diagnose        (rate-limited 5/IP/hour)
GET  /api/kb              (?category=...&limit=N)
GET  /api/kb/{id}
```

**Disabled pending auth:**
```
GET   /api/tickets
GET   /api/tickets/{id}
PATCH /api/tickets/{id}/status
```

---

## Running locally

You need Java 21, Node 20+, PostgreSQL 18, and an Anthropic API key.

**Backend:**
```bash
cd backend
cp .env.example .env
# set DB_HOST, DB_NAME, DB_USERNAME, DB_PASSWORD, ANTHROPIC_API_KEY
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd frontend
npm install
npm run dev
```

Backend runs on 8080, frontend on 5173. `VITE_API_BASE_URL` defaults to `http://localhost:8080/api` if not set.

---

## Project structure

```
patchpilot/
  backend/
    src/main/java/ai/patchpilot/api/
      controller/     REST endpoints
      service/        Claude integration, business logic
      dto/            Request/response types
      model/          JPA entities
      config/         CORS, rate limiting
      exception/      Global error handling
    src/main/resources/
      application.properties
      application-dev.properties
      application-prod.properties
    pom.xml
  frontend/
    src/
      components/     React components
      api.js          Axios client
      featureModalsContent.js
    amplify.yml       Amplify build + security headers config
    package.json
    vite.config.js
  README.md
  .gitignore
```

---

## Design decisions

**RAG over fine-tuning.** Fine-tuning on eight articles would be expensive and you'd retrain every time the KB changes. RAG keeps each answer traceable to a real document and lets the KB grow without touching the model.

**Diagnosis JSON stored as TEXT.** A diagnosis is three nested causes, each with probability, explanation, fix steps, and KB references. The shape was still moving when I built this — storing raw JSON in a TEXT column and parsing to typed DTOs at the API boundary was the practical call. Normalized schema is the right move once the format settles.

**Custom severity labels.** CURIOUS, INCONVENIENT, BLOCKING beats low/medium/high for IT triage. Those words actually tell you what to do next.

**Ticket endpoints disabled.** The list and detail endpoints return submitted error logs and AI diagnoses with no auth in front of them. Disabling them was safer than shipping them open. They come back when JWT auth is in.

**CloudFront as HTTPS bridge.** Amplify serves the frontend over HTTPS. EB runs HTTP. Browsers block HTTPS pages calling HTTP APIs. CloudFront in front of EB solves this without needing a load balancer (which would cost ~$16/month and break the free tier).

---

## Roadmap

- JWT auth on ticket endpoints
- Ticket dashboard UI (pagination API already wired up)
- macOS and Linux KB articles
- Vector embeddings on KB articles instead of fuzzy keyword matching
- `ForwardedHeaderFilter` trust config refinement once EB proxy IPs are stable

---

## About

Built by Arpit Patel — Computer Programming student at Humber College, Toronto. Portfolio project.

[GitHub](https://github.com/arpit-patel22/patchpilot) · [LinkedIn](https://www.linkedin.com/in/arpit-patel-dev) · [Live Demo](https://patchpilot.online)