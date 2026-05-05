import React from "react";
import { Nav, Hero } from "./components/Hero";
import { Metrics, HowItWorks, Features, KBPreview, CTABand, Footer } from "./components/Sections";

function App() {
  const [scrolled, setScrolled] = React.useState(false);

  React.useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 30);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  return (
    <>
      <Nav scrolled={scrolled} />
      <Hero gradient="aurora" />
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