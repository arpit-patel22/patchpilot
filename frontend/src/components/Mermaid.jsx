import React from "react";
import mermaid from "mermaid";
import MermaidModal from "./MermaidModal";

mermaid.initialize({
  startOnLoad: false,
  theme: "dark",
  themeVariables: {
    background: "transparent",
    primaryColor: "#0F1715",
    primaryTextColor: "#E5E7EB",
    primaryBorderColor: "#34D399",
    lineColor: "#34D399",
    secondaryColor: "#1F2A24",
    tertiaryColor: "#0A0F0D",
    fontFamily: "'Inter', sans-serif",
    fontSize: "13px",
    edgeLabelBackground: "#0A0A0F"
  },
  flowchart: {
    curve: "basis",
    padding: 20,
    nodeSpacing: 50,
    rankSpacing: 60,
    htmlLabels: true
  },
  securityLevel: "loose"
});

let idCounter = 0;

export const Mermaid = ({ chart, className = "" }) => {
  const ref = React.useRef(null);
  const [svg, setSvg] = React.useState("");
  const [isModalOpen, setIsModalOpen] = React.useState(false);
  const id = React.useMemo(() => `mermaid-${++idCounter}`, []);

  React.useEffect(() => {
    if (!chart) return;
    let cancelled = false;
    mermaid.render(id, chart)
      .then(({ svg }) => { if (!cancelled) setSvg(svg); })
      .catch((err) => console.error("Mermaid render error:", err));
    return () => { cancelled = true; };
  }, [chart, id]);

  const openModal = () => {
    if (svg) setIsModalOpen(true);
  };

  const closeModal = () => setIsModalOpen(false);

  return (
    <>
      <div
        ref={ref}
        className={`mermaid-wrap mermaid-clickable ${className}`}
        dangerouslySetInnerHTML={{ __html: svg }}
        onClick={openModal}
        role="button"
        tabIndex={0}
        aria-label="Open diagram in full view"
        onKeyDown={(e) => {
          if (e.key === "Enter" || e.key === " ") {
            e.preventDefault();
            openModal();
          }
        }}
      />
      {isModalOpen && <MermaidModal svg={svg} onClose={closeModal} />}
    </>
  );
};