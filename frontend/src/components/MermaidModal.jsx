import React from "react";
import { createPortal } from "react-dom";

/**
 * Fullscreen modal for the Mermaid architecture diagram.
 * Rendered via React portal directly into document.body so it escapes
 * any parent stacking contexts (transforms, contain, overflow:hidden).
 *
 * Chunk A: open/close behavior, no zoom yet.
 *
 * Props:
 *   svg     — the rendered Mermaid SVG string
 *   onClose — callback to close the modal
 */
export default function MermaidModal({ svg, onClose }) {
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
      className="mermaid-modal-backdrop"
      onClick={onBackdropClick}
      role="dialog"
      aria-modal="true"
      aria-label="Architecture diagram"
    >
      <button
        type="button"
        className="mermaid-modal-close"
        onClick={onClose}
        aria-label="Close diagram"
      >
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" strokeWidth="2.5"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
          <line x1="18" y1="6" x2="6" y2="18" />
          <line x1="6" y1="6" x2="18" y2="18" />
        </svg>
      </button>

      <div
        className="mermaid-modal-content"
        dangerouslySetInnerHTML={{ __html: svg }}
      />
    </div>
  );

  return createPortal(modalContent, document.body);
}