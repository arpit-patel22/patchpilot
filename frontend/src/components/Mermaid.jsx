import React from "react";
import mermaid from "mermaid";

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
  const id = React.useMemo(() => `mermaid-${++idCounter}`, []);

  React.useEffect(() => {
    if (!chart) return;
    let cancelled = false;
    mermaid.render(id, chart)
      .then(({ svg }) => { if (!cancelled) setSvg(svg); })
      .catch((err) => console.error("Mermaid render error:", err));
    return () => { cancelled = true; };
  }, [chart, id]);

  return (
    <div
      ref={ref}
      className={`mermaid-wrap ${className}`}
      dangerouslySetInnerHTML={{ __html: svg }}
    />
  );
};