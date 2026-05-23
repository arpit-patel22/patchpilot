import React from "react";
import { createPortal } from "react-dom";
import ReactMarkdown from "react-markdown";

/**
 * Fullscreen modal for a single KB article.
 * Rendered via React portal so it escapes parent stacking contexts.
 *
 * Mirrors the Mermaid modal pattern:
 *   - ESC to close
 *   - Click backdrop to close
 *   - Body scroll lock while open
 *   - Top-right close button
 *
 * Props:
 *   article   — KB article detail object, or null
 *   loading   — true while fetch is in flight
 *   error     — Error object if fetch failed
 *   onClose   — callback to close the modal
 */
export default function KbArticleModal({ article, loading, error, onClose }) {
  // ESC key closes
  React.useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  // Body scroll lock while open
  React.useEffect(() => {
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, []);

  const onBackdropClick = (e) => {
    if (e.target === e.currentTarget) onClose();
  };

  const modalContent = (
    <div
      className="kb-modal-backdrop"
      onClick={onBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-label="Knowledge base article"
    >
      <button
        type="button"
        className="kb-modal-close"
        onClick={onClose}
        aria-label="Close article"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth="2.5"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>

      <div className="kb-modal-content">
        {loading && (
          <div className="kb-modal-state">
            <div className="kb-modal-spinner" aria-hidden="true" />
            <p>Loading article...</p>
          </div>
        )}

        {error && !loading && (
          <div className="kb-modal-state">
            <p className="kb-modal-error-title">Couldn't load this article</p>
            <p className="kb-modal-error-body">
              Check that the backend is running, then try again.
            </p>
          </div>
        )}

        {!loading && !error && !article && (
          <div className="kb-modal-state">
            <p className="kb-modal-error-title">Article not found</p>
            <p className="kb-modal-error-body">
              This article may have been removed or never existed.
            </p>
          </div>
        )}

        {!loading && !error && article && (
          <article className="kb-modal-article">
            <header className="kb-modal-header">
              <span className={`kb-modal-badge kb-badge-${article.category?.toLowerCase()}`}>
                {article.category?.replace(/_/g, " ")}
              </span>
              <h2 className="kb-modal-title">{article.title}</h2>
              <p className="kb-modal-meta">
                {article.software}
                {article.osTarget ? ` • ${article.osTarget}` : ""}
              </p>
            </header>

            <div className="kb-modal-section">
              <h3 className="kb-modal-section-title">Root cause</h3>
              <p className="kb-modal-prose">{article.rootCause}</p>
            </div>

            <div className="kb-modal-section">
              <h3 className="kb-modal-section-title">Resolution steps</h3>
              <div className="kb-modal-markdown">
                <ReactMarkdown>{article.resolutionSteps}</ReactMarkdown>
              </div>
                </div>
          </article>
        )}
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
}