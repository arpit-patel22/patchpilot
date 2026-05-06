// PatchPilot — Metrics, How it works, Features, KB, CTA, Footer
import {
  IPencil, ISparkles, ICheck, ITarget,
  IBars, IDatabase, IArrowRight, IGithub,
  ILinkedIn, IMail
} from "./Icons";
import { InView } from "./Hero";

const METRICS = [
  { num: "8", unit: " KB", label: "curated articles seeded" },
  { num: "1.4", unit: "s", label: "average diagnosis time" },
  { num: "100", unit: "%", label: "open source · MIT" },
  { num: "3", unit: "x", label: "ranked diagnoses, not one" },
];

export const Metrics = () => (
  <section className="metrics" style={{ padding: 0 }}>
    <div className="container" style={{ padding: "48px 32px" }}>
      <InView className="metrics-grid" variant="stagger" threshold={0.2}>
        {METRICS.map((m, i) => (
          <div key={i} className="metric">
            <div className="metric-num">
              {m.num}<span className="unit">{m.unit}</span>
            </div>
            <div className="metric-label">{m.label}</div>
          </div>
        ))}
      </InView>
    </div>
  </section>
);

const STEPS = [
  {
    icon: <IPencil size={18} />,
    title: "Describe the error",
    desc: "Paste your stack trace, installer log, or just type what went wrong. PatchPilot accepts messy input.",
  },
  {
    icon: <ISparkles size={18} />,
    title: "AI diagnoses root cause",
    desc: "Claude searches the curated KB first, then reasons about your specific OS and software combination.",
  },
  {
    icon: <ICheck size={18} />,
    title: "Apply step-by-step fix",
    desc: "Get ordered remediation steps with copy-ready commands. Every ticket is saved for the team.",
  },
];

export const HowItWorks = () => (
  <section id="how">
    <div className="container">
      <div className="section-head">
        <span className="eyebrow">/ workflow</span>
        <h2 className="section-title">From error message to fix in three steps.</h2>
        <p className="section-sub">
          Built for the moment a technician opens a ticket and wants to be helpful in
          the next ninety seconds.
        </p>
      </div>
      <InView className="steps" variant="scale" threshold={0.15}>
        {STEPS.map((s, i) => (
          <div key={i} className="step">
            <div className="step-num">STEP {String(i + 1).padStart(2, "0")}</div>
            <div className="step-icon">{s.icon}</div>
            <h3>{s.title}</h3>
            <p>{s.desc}</p>
            {i < STEPS.length - 1 && <div className="step-connector" />}
          </div>
        ))}
      </InView>
    </div>
  </section>
);

const FEATURES = [
  {
    icon: <ITarget size={16} />,
    title: "Grounded diagnoses",
    desc: "Pulls from a curated knowledge base before consulting Claude — so answers cite real KB articles, not hallucinations.",
    link: "Read the RAG approach →",
  },
  {
    icon: <IBars size={16} />,
    title: "Probability-ranked causes",
    desc: "Top 3 causes ranked by likelihood, not just one guess. Pick the right diagnosis on the first try, even when symptoms overlap.",
    link: "See ranking model →",
  },
  {
    icon: <IDatabase size={16} />,
    title: "Persistent ticket history",
    desc: "Every diagnosis is saved as a ticket you can revisit, share with teammates, and use to train PatchPilot on your stack.",
    link: "Tour the ticket view →",
  },
];

export const Features = () => (
  <section id="features" style={{ paddingTop: 32 }}>
    <div className="container">
      <div className="section-head">
        <span className="eyebrow">/ features</span>
        <h2 className="section-title">Why technicians keep it open in a tab.</h2>
        <p className="section-sub">
          Three opinionated decisions that make PatchPilot different from pasting your error into a chat window.
        </p>
      </div>
      <div className="features">
        {FEATURES.map((f, i) => (
          <div key={i} className="feature">
            <div className="feature-icon">{f.icon}</div>
            <h3>{f.title}</h3>
            <p>{f.desc}</p>
            <a className="feature-link" href="#">{f.link}</a>
          </div>
        ))}
      </div>
    </div>
  </section>
);

const KB_ARTICLES = [
  {
    cat: "windows",
    catLabel: "Windows",
    title: "Resolving MSI Error 1603 during silent installs",
    desc: "Clean the installer cache, reset TrustedInstaller, and re-run with verbose logging.",
    id: "KB-0124",
  },
  {
    cat: "macos",
    catLabel: "macOS",
    title: "Gatekeeper blocking signed packages on 14.4+",
    desc: "Re-staple notarization tickets and verify the developer ID chain with spctl.",
    id: "KB-0089",
  },
  {
    cat: "linux",
    catLabel: "Linux",
    title: "apt: held broken packages on Ubuntu 24.04 LTS",
    desc: "Identify dependency conflicts with aptitude and selectively downgrade the offending lib.",
    id: "KB-0151",
  },
  {
    cat: "network",
    catLabel: "Network",
    title: "Corporate proxy intercepting NPM cert chain",
    desc: "Trust the MITM root cert, set strict-ssl, and configure registry fallback.",
    id: "KB-0203",
  },
];

export const KBPreview = () => (
  <section id="kb" style={{ paddingTop: 32 }}>
    <div className="container">
      <div className="section-head" style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", flexWrap: "wrap", gap: 16 }}>
        <div>
          <span className="eyebrow">/ knowledge base</span>
          <h2 className="section-title">Eight articles. Real fixes. Always growing.</h2>
          <p className="section-sub">
            Hand-curated from the install errors that actually show up in tickets — not
            scraped from random forum threads.
          </p>
        </div>
        <a className="btn btn-secondary" href="#">
          Browse all articles
          <IArrowRight size={14} />
        </a>
      </div>
      <div className="kb-row">
        {KB_ARTICLES.map((a) => (
          <a key={a.id} href="#" className="kb">
            <span className={"kb-cat " + a.cat}>{a.catLabel}</span>
            <h4>{a.title}</h4>
            <p>{a.desc}</p>
            <div className="kb-foot">
              <span>{a.id}</span>
              <span className="arrow">View →</span>
            </div>
          </a>
        ))}
      </div>
    </div>
  </section>
);

export const CTABand = () => (
  <section style={{ paddingTop: 32 }}>
    <div className="container">
      <InView className="cta-band">
        <span className="eyebrow">/ get started</span>
        <h2>Stop debugging install errors alone.</h2>
        <p>Let PatchPilot do the diagnostic work — so your queue clears and your evenings come back.</p>
        <div className="hero-ctas">
          <a className="btn btn-primary btn-large" href="#">
            Try PatchPilot free
            <IArrowRight size={15} />
          </a>
          <a className="btn btn-secondary btn-large" href="#">
            <IGithub size={14} />
            Star on GitHub
          </a>
        </div>
        <div className="hero-meta" style={{ marginTop: 24 }}>
          <span>No card required</span>
          <span className="pip" />
          <span>Self-host in 5 minutes</span>
          <span className="pip" />
          <span>MIT licensed</span>
        </div>
      </InView>
    </div>
  </section>
);

export const Footer = () => (
  <footer className="footer">
    <div className="container">
      <div className="footer-row">
      <div className="footer-brand">
          <div className="row1">
            <img src="/logo-full.png" alt="PatchPilot" className="footer-logo-img" />
          </div>
          <div>Built by Arpit Patel · Computer Programming, Humber College</div>
          <div className="mono" style={{ fontSize: 11, color: "var(--text-tertiary)", marginTop: 4 }}>
            © 2026 · v0.4.2 · last deploy 6h ago
          </div>
        </div>
        <div className="footer-links">
          <a href="#"><IGithub size={14} />GitHub</a>
          <a href="#"><ILinkedIn size={14} />LinkedIn</a>
          <a href="#"><IMail size={14} />Email</a>
        </div>
      </div>
      <div className="footer-fine">
        <span>Powered by Anthropic Claude · Spring Boot · React · MySQL</span>
        <span>Made in Toronto</span>
      </div>
    </div>
  </footer>
);