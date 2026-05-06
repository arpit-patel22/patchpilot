import React from "react";
import { Nav, Hero } from "./components/Hero";
import { DiagnoseSection } from "./components/DiagnoseSection";
import { Metrics, HowItWorks, Features, KBPreview, CTABand, Footer } from "./components/Sections";

function CursorSpotlight() {
  const ref = React.useRef(null);
  const target = React.useRef({ x: -300, y: -300 });
  const current = React.useRef({ x: -300, y: -300 });

  React.useEffect(() => {
    const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const coarse = window.matchMedia("(pointer: coarse)").matches;
    if (reduced || coarse) return;

    let raf;
    const onMove = (e) => {
      target.current.x = e.clientX;
      target.current.y = e.clientY;
    };

    const tick = () => {
      current.current.x += (target.current.x - current.current.x) * 0.12;
      current.current.y += (target.current.y - current.current.y) * 0.12;
      if (ref.current) {
        ref.current.style.transform = `translate3d(${current.current.x - 300}px, ${current.current.y - 300}px, 0)`;
      }
      raf = requestAnimationFrame(tick);
    };

    window.addEventListener("mousemove", onMove, { passive: true });
    raf = requestAnimationFrame(tick);

    // Card-relative spotlight via delegation
    const onCardMove = (e) => {
      const card = e.target.closest(".spot-card, .step, .feature, .kb");
      if (!card) return;
      const r = card.getBoundingClientRect();
      card.style.setProperty("--mx", `${e.clientX - r.left}px`);
      card.style.setProperty("--my", `${e.clientY - r.top}px`);
    };
    document.addEventListener("mousemove", onCardMove, { passive: true });

    return () => {
      window.removeEventListener("mousemove", onMove);
      document.removeEventListener("mousemove", onCardMove);
      cancelAnimationFrame(raf);
    };
  }, []);

  return <div ref={ref} className="cursor-spotlight" aria-hidden />;
}

function App() {
  const [scrolled, setScrolled] = React.useState(false);

  React.useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 30);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

return (
  <>
    <CursorSpotlight />
    <Nav scrolled={scrolled} />
    <Hero gradient="aurora" />
    <DiagnoseSection />
    <Metrics />
    <HowItWorks />
    <Features />
    <KBPreview />
    <CTABand />
    <Footer />
  </>
);
}

export default App;