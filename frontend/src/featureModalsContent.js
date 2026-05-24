export const FEATURE_MODALS = {
  rag: {
    title: "How PatchPilot avoids AI hallucinations",
    eyebrow: "RAG approach",
    body: `Most "AI for IT" tools paste your error into Claude or ChatGPT and hope the answer is accurate. They aren't — generic LLM answers often invent registry keys that don't exist, suggest commands for the wrong OS, or cite "Microsoft Knowledge Base article KB-12345" that was never written.

PatchPilot solves this with **Retrieval-Augmented Generation (RAG)**:

1. **Match first, generate second.** When you submit an error, the backend fuzzy-matches your symptoms against an internal knowledge base of 8 hand-curated articles covering real Windows install failures (MSI 1603, VC++ redistributable issues, antivirus blocks, etc.).

2. **Inject context into the prompt.** Matched KB articles are injected into Claude's system prompt as grounding material — Claude doesn't get to answer from training data alone.

3. **Constrained output.** Claude returns three ranked causes with probability percentages and step-by-step fixes that reference the injected KB articles.

The result: Claude's answers cite *real* articles, not invented ones. As the KB grows, the system gets smarter without retraining anything.`,
    stack: ["Spring Boot 4", "PostgreSQL", "Claude Sonnet 4.5", "Anthropic API"],
  },
  ranking: {
    title: "Three ranked causes, not one guess",
    eyebrow: "Ranking model",
    body: `When a Windows install fails, the symptom is rarely the cause. "Error 1603" could be:

- A locked installer file (~40% of cases)
- Antivirus interference (~25%)
- Insufficient disk space (~15%)
- Corrupted Windows Installer service (~10%)
- Something else (~10%)

Most AI tools collapse this into one confident-sounding answer, then are wrong half the time. PatchPilot keeps the uncertainty visible.

Every diagnosis returns:

1. **Three ranked causes**, each with a probability percentage
2. **Severity classification** — \`CURIOUS\`, \`INCONVENIENT\`, or \`BLOCKING\`
3. **Step-by-step fix instructions** for each cause, expandable on demand
4. **References to the KB articles** that grounded each cause

Technicians don't have to guess which cause Claude is most confident about — the model tells them upfront. If cause #1 fails, they try #2 without going back to square one.

Submit a real error in the Diagnose section below to see this in action.`,
    stack: ["Custom DiagnosisResult DTO", "Severity enum", "Probability ranking", "Claude tool-use output"],
  },
  tickets: {
    title: "Persistent ticket history",
    eyebrow: "Ticket view",
    body: `Every diagnosis is saved as a ticket in the backend — error details, the ranked causes Claude returned, severity, and timestamp — using Spring Data JPA against Postgres.

The \`GET /api/tickets\` endpoint returns paginated results that any frontend can render. A polished ticket dashboard view is on the roadmap — the foundation already exists in the API.

**Why this matters:** Most AI tools forget your last query the moment you close the tab. PatchPilot's ticket history means teams can revisit, share, and learn from past diagnoses.

**API endpoint live:**
\`GET /api/tickets?page=0&size=20\` — paginated, returns \`Page<TicketSummary>\``,
    stack: ["Spring Data JPA", "PostgreSQL", "Pageable", "Hibernate"],
  },
};
