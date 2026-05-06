// PatchPilot — Real, working diagnose form wired to the backend
import React from "react";
import { ISparkles, ITarget, ITerminal } from "./Icons";
import { diagnose } from "../api";

const OS_OPTIONS = [
  { id: "Windows 11", label: "Windows" },
  { id: "macOS", label: "macOS" },
  { id: "Linux", label: "Linux" },
];

const SEVERITY_OPTIONS = [
  { id: "CURIOUS", label: "Curious" },
  { id: "INCONVENIENT", label: "Inconvenient" },
  { id: "BLOCKING", label: "Blocking" },
];

export const DiagnoseSection = () => {
  // Form state
  const [software, setSoftware] = React.useState("");
  const [softwareVersion, setSoftwareVersion] = React.useState("");
  const [osVersion, setOsVersion] = React.useState("Windows 11");
  const [errorMessage, setErrorMessage] = React.useState("");
  const [userAttempts, setUserAttempts] = React.useState("");
  const [severity, setSeverity] = React.useState("INCONVENIENT");

  // Async state
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState(null);
  const [result, setResult] = React.useState(null);
  const [elapsedMs, setElapsedMs] = React.useState(null);

  const canSubmit = software.trim() && errorMessage.trim() && !loading;

  const handleSubmit = async () => {
    if (!canSubmit) return;

    setLoading(true);
    setError(null);
    setResult(null);
    setElapsedMs(null);

    const startedAt = performance.now();

    try {
      const response = await diagnose({
        software: software.trim(),
        softwareVersion: softwareVersion.trim() || "unknown",
        osVersion,
        errorMessage: errorMessage.trim(),
        userAttempts: userAttempts.trim() || "none",
        severity,
      });
      setElapsedMs(Math.round(performance.now() - startedAt));
      setResult(response);
    } catch (err) {
      console.error("Diagnose failed:", err);
      if (err.response) {
        setError(`Backend error (${err.response.status}): ${err.response.data?.message || "unknown error"}`);
      } else if (err.request) {
        setError("Could not reach the backend. Is it running on http://localhost:8080?");
      } else {
        setError(err.message || "Something went wrong.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if ((e.metaKey || e.ctrlKey) && e.key === "Enter") {
      e.preventDefault();
      handleSubmit();
    }
  };

  // Cleanly extract typed fields from the response
  const diagnosis = result?.diagnosis;
  const causes = diagnosis?.causes || [];
  const tryThisFirst = diagnosis?.tryThisFirst;

  return (
    <section id="diagnose" style={{ paddingTop: 32 }}>
      <div className="container">
        <div className="section-head center" style={{ textAlign: "center" }}>
          <span className="eyebrow">/ try it now</span>
          <h2 className="section-title">Diagnose a real installation failure.</h2>
          <p className="section-sub" style={{ margin: "0 auto" }}>
            Paste your error below. Claude will analyze it against the curated KB
            and return ranked root causes with fix steps.
          </p>
        </div>

        <div className="diagnose-wrap" onKeyDown={handleKeyDown}>
          {/* LEFT: form */}
          <div className="pp-form" style={{ borderRight: "1px solid var(--border)" }}>
            <div className="pp-form-head">
              <div className="pp-form-title">
                New diagnosis
                <small>· live API call</small>
              </div>
              <span
                className="mono"
                style={{ fontSize: 11, color: loading ? "var(--amber)" : "var(--emerald)" }}
              >
                {loading ? "● analyzing..." : "● ready"}
              </span>
            </div>

            <div className="field">
              <label>Software *</label>
              <input
                className="field-input"
                placeholder="e.g. Adobe Acrobat DC"
                value={software}
                onChange={(e) => setSoftware(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="field">
              <label>Software version</label>
              <input
                className="field-input"
                placeholder="e.g. 23.001.20143"
                value={softwareVersion}
                onChange={(e) => setSoftwareVersion(e.target.value)}
                disabled={loading}
              />
            </div>

            <div className="field">
              <label>Operating system</label>
              <div className="os-row">
                {OS_OPTIONS.map((o) => (
                  <button
                    key={o.id}
                    className={"os-pill " + (osVersion === o.id ? "active" : "")}
                    onClick={() => setOsVersion(o.id)}
                    disabled={loading}
                    type="button"
                  >
                    {o.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="field">
              <label>Severity</label>
              <div className="os-row">
                {SEVERITY_OPTIONS.map((s) => (
                  <button
                    key={s.id}
                    className={"os-pill " + (severity === s.id ? "active" : "")}
                    onClick={() => setSeverity(s.id)}
                    disabled={loading}
                    type="button"
                  >
                    {s.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="field">
              <label>Error message / log *</label>
              <textarea
                className="field-input"
                placeholder="Paste your error message, stack trace, or installer log here..."
                value={errorMessage}
                onChange={(e) => setErrorMessage(e.target.value)}
                disabled={loading}
                style={{ minHeight: 140 }}
              />
            </div>

            <div className="field">
              <label>What have you tried?</label>
              <textarea
                className="field-input"
                placeholder="e.g. Tried running as admin, restarted the installer..."
                value={userAttempts}
                onChange={(e) => setUserAttempts(e.target.value)}
                disabled={loading}
                style={{ minHeight: 60 }}
              />
            </div>

            <button
              className="btn btn-primary"
              style={{ width: "100%", justifyContent: "center" }}
              onClick={handleSubmit}
              disabled={!canSubmit}
              type="button"
            >
              <ISparkles size={14} />
              {loading ? "Diagnosing..." : "Diagnose"}
              <span className="mono" style={{ fontSize: 11, opacity: 0.65, marginLeft: 4 }}>
                ⌘↵
              </span>
            </button>
          </div>

          {/* RIGHT: results */}
          <div className="pp-results">
            <div className="pp-results-head">
              <div className="pp-results-title">
                <ITarget size={14} style={{ color: "var(--accent)" }} />
                {result ? "Probable causes" : "Awaiting diagnosis"}
                {causes.length > 0 && (
                  <span
                    className="mono"
                    style={{ fontSize: 11, color: "var(--text-tertiary)", fontWeight: 400, marginLeft: 4 }}
                  >
                    {causes.length} ranked
                  </span>
                )}
              </div>
              {elapsedMs && (
                <div className="runtime">
                  resolved in <b>{(elapsedMs / 1000).toFixed(1)}s</b> · grounded ✓
                </div>
              )}
            </div>

            {/* ERROR STATE */}
            {error && (
              <div
                className="diag-card"
                style={{
                  borderColor: "rgba(251, 113, 133, 0.4)",
                  background: "rgba(251, 113, 133, 0.06)",
                }}
              >
                <div className="diag-rank" style={{ color: "var(--rose)" }}>
                  ERROR
                </div>
                <div className="diag-title">{error}</div>
              </div>
            )}

            {/* LOADING STATE */}
            {loading && (
              <div className="diag-card">
                <div className="diag-rank">CALLING CLAUDE...</div>
                <div className="diag-title">
                  Analyzing your error against the knowledge base. This typically takes 2-5 seconds.
                </div>
                <div className="prob-bar">
                  <div className="prob-bar-fill" style={{ width: "100%", animation: "drift 1.5s ease-in-out infinite alternate" }} />
                </div>
              </div>
            )}

            {/* EMPTY STATE */}
            {!loading && !error && !result && (
              <div
                className="diag-card"
                style={{ borderStyle: "dashed", opacity: 0.6, cursor: "default" }}
              >
                <div className="diag-rank">NO DIAGNOSIS YET</div>
                <div className="diag-title">
                  Fill out the form and click <b>Diagnose</b> to send your error to the AI for analysis.
                </div>
              </div>
            )}

            {/* TRY THIS FIRST callout */}
            {tryThisFirst && (
              <div
                style={{
                  padding: 16,
                  border: "1px solid rgba(52, 211, 153, 0.3)",
                  borderRadius: 10,
                  background: "rgba(52, 211, 153, 0.06)",
                  display: "flex",
                  gap: 12,
                  alignItems: "flex-start",
                }}
              >
                <ISparkles size={16} style={{ color: "var(--accent)", flexShrink: 0, marginTop: 2 }} />
                <div>
                  <div className="diag-rank" style={{ color: "var(--accent)" }}>TRY THIS FIRST</div>
                  <div className="diag-title" style={{ marginTop: 4 }}>{tryThisFirst}</div>
                </div>
              </div>
            )}

            {/* RANKED CAUSES with their fix steps */}
            {causes.map((cause, i) => (
              <div
                key={i}
                className={"diag-card " + (i === 0 ? "primary" : "")}
              >
                <div className="diag-top">
                  <div style={{ minWidth: 0 }}>
                    <div className="diag-rank">
                      #{String(i + 1).padStart(2, "0")} {i === 0 && "· most likely"}
                    </div>
                    <div className="diag-title">{cause.name}</div>
                  </div>
                  <div className="diag-prob">{cause.probability}%</div>
                </div>
                <div className="prob-bar">
                  <div className="prob-bar-fill" style={{ width: `${cause.probability}%` }} />
                </div>
                {cause.explanation && (
                  <div style={{ fontSize: 13, color: "var(--text-secondary)", lineHeight: 1.55 }}>
                    {cause.explanation}
                  </div>
                )}
                {cause.steps && cause.steps.length > 0 && (
                  <details style={{ marginTop: 4 }}>
                    <summary
                      style={{
                        cursor: "pointer",
                        fontSize: 12,
                        color: "var(--accent)",
                        fontFamily: "JetBrains Mono, monospace",
                        letterSpacing: "0.04em",
                        textTransform: "uppercase",
                      }}
                    >
                      <ITerminal size={11} style={{ marginRight: 6, verticalAlign: "middle" }} />
                      View {cause.steps.length} fix steps
                    </summary>
                    <ol style={{ margin: "10px 0 0", paddingLeft: 20, color: "var(--text-secondary)", fontSize: 13, lineHeight: 1.6 }}>
                      {cause.steps.map((step, j) => (
                        <li key={j} style={{ marginBottom: 4 }}>{step}</li>
                      ))}
                    </ol>
                  </details>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
};