// PatchPilot — Nav, Hero, Product preview
import React from "react";
import {
  ITarget, IGithub, IArrowRight, IPlay,
  ISearch, ISparkles, IBook, IShield,
  ICheck, ITerminal
} from "./Icons";

const NAV_LINKS = [
  { href: "#how", label: "How it works" },
  { href: "#features", label: "Features" },
  { href: "#kb", label: "Knowledge base" },
  { href: "#docs", label: "Docs" },
  { href: "#changelog", label: "Changelog" }
];

export const Nav = ({ scrolled }) => {
  const [open, setOpen] = React.useState(false);
  return (
    <nav className={"nav " + (scrolled ? "nav-blur" : "")}>
      <div className="nav-pill">
        <a href="/" className="nav-brand">
          <span className="nav-mark" style={{ ringWidth: 1, borderRadius: "10px" }}>
            <ITarget size={18} style={{ color: "#04121C", strokeWidth: 2.25 }} />
          </span>
          <span className="nav-brand-text">
            PatchPilot
            <span className="pulse-dot" />
          </span>
        </a>
        <div className="nav-links">
          {NAV_LINKS.map((l, i) =>
            <a key={l.href} href={l.href} className={i === 0 ? "active" : ""}>{l.label}</a>
          )}
        </div>
        <div className="nav-right">
          <a className="btn btn-ghost" href="#" aria-label="GitHub" style={{ width: 40, height: 40, padding: 0, justifyContent: "center", borderRadius: 9999 }}>
            <IGithub size={18} />
          </a>
          <a className="btn btn-primary" href="#" style={{ borderRadius: 9999 }}>
            <span className="nav-cta-text">Try PatchPilot</span>
            <IArrowRight size={14} />
          </a>
          <button className="nav-hamburger" aria-label="Open menu" onClick={() => setOpen((o) => !o)}>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round">
              {open ?
                <>
                  <path d="M6 6l12 12" />
                  <path d="M18 6L6 18" />
                </> :
                <>
                  <path d="M3 6h18" />
                  <path d="M3 12h18" />
                  <path d="M3 18h18" />
                </>
              }
            </svg>
          </button>
        </div>
      </div>
      {open &&
        <div className="nav-mobile-menu">
          {NAV_LINKS.map((l) =>
            <a key={l.href} href={l.href} onClick={() => setOpen(false)}>{l.label}</a>
          )}
        </div>
      }
    </nav>
  );
};

// IntersectionObserver-driven entrance — replaces Framer Motion's whileInView
export const InView = ({ children, className = "", as: Tag = "div", variant = "init", once = true, threshold = 0.15, ...rest }) => {
  const ref = React.useRef(null);
  const [seen, setSeen] = React.useState(false);

  React.useEffect(() => {
    if (!ref.current) return;

    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    if (reduced) {
      setSeen(true);
      return;
    }

    const obs = new IntersectionObserver((entries) => {
      entries.forEach((e) => {
        if (e.isIntersecting) {
          setSeen(true);
          if (once) obs.disconnect();
        } else if (!once) {
          setSeen(false);
        }
      });
    }, { threshold });

    obs.observe(ref.current);
    return () => obs.disconnect();
  }, [once, threshold]);

  const variantClass = variant === "stagger" ? "in-view-stagger" : variant === "scale" ? "in-view-scale" : "in-view-init";

  return (
    <Tag ref={ref} className={`${variantClass} ${seen ? "in-view" : ""} ${className}`} {...rest}>
      {children}
    </Tag>
  );
};




export const Hero = ({ gradient }) =>
  <section className="hero" data-gradient={gradient}>
    <div className="hero-grid" />
    <div className="hero-mesh">
      <div className="blob-3" />
    </div>

    <div className="container hero-content">
      <div className="badge anim-badge">
        <span className="dot" />
        <span><strong>AI-powered IT support</strong></span>
        <span className="sep">·</span>
        <span>Now in beta</span>
      </div>

      <h1 className="hero-title anim-title">
        Diagnose installation failures<br />
        in <span className="accent-word">seconds</span>, not hours.
      </h1>

      <p className="hero-sub anim-sub">
        PatchPilot is an AI copilot for IT support teams. Describe your error in plain
        language — get probability-ranked diagnoses and step-by-step fixes.
      </p>

      <div className="hero-ctas anim-ctas">
        <a className="btn btn-primary btn-large" href="#">
          Try it free
          <IArrowRight size={15} />
        </a>
        <a className="btn btn-secondary btn-large" href="#">
          <IPlay size={13} />
          Watch demo
        </a>
      </div>

      <div className="hero-meta anim-meta">
        <span>Powered by Claude</span>
        <span className="pip" />
        <span>8 KB articles seeded</span>
        <span className="pip" />
        <span>Open source</span>
      </div>
    </div>

    <InView className="container preview-wrap" threshold={0.05}>
      <ProductPreviewInner />
    </InView>
  </section>;

const OS_OPTIONS = [
  { id: "win", label: "Windows" },
  { id: "mac", label: "macOS" },
  { id: "lnx", label: "Linux" }
];

const SAMPLE_ERROR = `Error 1603: Fatal error during installation.
MSI package "Adobe Acrobat DC.msi" failed to register
with installer service.

[18:44:02] Action ended 18:44:02: InstallFinalize. Return value 3.`;

const DIAGNOSES = [
  { rank: "01", primary: true, title: "Stale MSI installer cache blocking re-registration", prob: 87, meta: ["KB-0124", "Windows 11", "fixes: 4"] },
  { rank: "02", title: "Insufficient TrustedInstaller permissions on %ProgramData%", prob: 41, meta: ["KB-0089", "Permissions", "fixes: 2"] },
  { rank: "03", title: "Conflicting prior install — Reader DC residue not removed", prob: 23, meta: ["KB-0151", "Conflict", "fixes: 3"] }
];

const ProductPreviewInner = () => {
  const [os, setOs] = React.useState("win");
  const [animated, setAnimated] = React.useState(false);

  React.useEffect(() => {
    const t = setTimeout(() => setAnimated(true), 800);
    return () => clearTimeout(t);
  }, []);

  return (
    <>
      <div className="preview-glow" />
      <div className="preview">
        <div className="preview-bar">
          <div className="dots"><span /><span /><span /></div>
          <div className="url">patchpilot.app  /  diagnose  /  ticket-2049</div>
          <div style={{ width: 38, display: "flex", justifyContent: "flex-end", color: "var(--text-tertiary)" }}>
            <ISearch size={13} />
          </div>
        </div>
        <div className="preview-body">
          <div className="pp-form">
            <div className="pp-form-head">
              <div className="pp-form-title">
                New diagnosis
                <small>· ticket #2049</small>
              </div>
              <span className="mono" style={{ fontSize: 11, color: "var(--emerald)" }}>● live</span>
            </div>
            <div className="field">
              <label>Software</label>
              <input className="field-input" defaultValue="Adobe Acrobat DC 23.001.20143" />
            </div>
            <div className="field">
              <label>Operating system</label>
              <div className="os-row">
                {OS_OPTIONS.map((o) =>
                  <button key={o.id} className={"os-pill " + (os === o.id ? "active" : "")} onClick={() => setOs(o.id)}>
                    {o.label}
                  </button>
                )}
              </div>
            </div>
            <div className="field">
              <label>Error message / log</label>
              <textarea className="field-input" defaultValue={SAMPLE_ERROR} />
            </div>
            <button className="btn btn-primary" style={{ width: "100%", justifyContent: "center" }}>
              <ISparkles size={14} />
              Diagnose
              <span className="mono" style={{ fontSize: 11, opacity: 0.65, marginLeft: 4 }}>⌘↵</span>
            </button>
          </div>

          <div className="pp-results">
            <div className="pp-results-head">
              <div className="pp-results-title">
                <ITarget size={14} style={{ color: "var(--accent)" }} />
                Probable causes
                <span className="mono" style={{ fontSize: 11, color: "var(--text-tertiary)", fontWeight: 400, marginLeft: 4 }}>
                  3 ranked
                </span>
              </div>
              <div className="runtime">
                resolved in <b>1.4s</b> · grounded ✓
              </div>
            </div>
            {DIAGNOSES.map((d) =>
              <div key={d.rank} className={"diag-card " + (d.primary ? "primary" : "")}>
                <div className="diag-top">
                  <div style={{ minWidth: 0 }}>
                    <div className="diag-rank">#{d.rank} {d.primary && "· most likely"}</div>
                    <div className="diag-title">{d.title}</div>
                  </div>
                  <div className="diag-prob">{d.prob}%</div>
                </div>
                <div className="prob-bar">
                  <div className="prob-bar-fill" style={{ width: animated ? `${d.prob}%` : "0%" }} />
                </div>
                <div className="diag-meta">
                  {d.meta.map((m, i) =>
                    <span key={i}>
                      {i === 0 && <IBook size={11} />}
                      {i === 1 && <IShield size={11} />}
                      {i === 2 && <ICheck size={11} />}
                      {m}
                    </span>
                  )}
                </div>
              </div>
            )}
            <div style={{ display: "flex", gap: 8, marginTop: "auto", paddingTop: 4 }}>
              <button className="btn btn-secondary" style={{ flex: 1, justifyContent: "center", height: 36, fontSize: 13 }}>
                <ITerminal size={13} />
                View fix steps
              </button>
              <button className="btn btn-ghost" style={{ height: 36, padding: "0 14px", border: "1px solid var(--border)" }}>
                Save ticket
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  );
};