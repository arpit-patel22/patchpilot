import React from "react";
import { createPortal } from "react-dom";
import ReactMarkdown from "react-markdown";

export default function FeatureModal({ modal, onClose }) {
  React.useEffect(() => {
    const onKey = (e) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

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
      className="feature-modal-backdrop"
      onClick={onBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-label={modal.title}
    >
      <button
        type="button"
        className="feature-modal-close"
        onClick={onClose}
        aria-label="Close"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth="2.5"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>

      <div className="feature-modal-content">
        <div className="feature-modal-article">
          <header className="feature-modal-header">
            <span className="feature-modal-eyebrow">/ {modal.eyebrow}</span>
            <h2 className="feature-modal-title">{modal.title}</h2>
          </header>

          <div className="feature-modal-body">
            <ReactMarkdown>{modal.body}</ReactMarkdown>
          </div>

          <footer className="feature-modal-stack">
            <span className="feature-modal-stack-label">Tech</span>
            <ul className="feature-modal-stack-list">
              {modal.stack.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </footer>
        </div>
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
}
