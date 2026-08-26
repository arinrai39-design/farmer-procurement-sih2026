import { ArrowRight, BadgeIndianRupee, Bell, Clock3, MapPinned, ShieldCheck } from "lucide-react";

export default function Landing({ setView }) {
  const features = [
    ["Slot Booking", "Farmers reserve a procurement time before visiting the centre.", Clock3],
    ["Live Queue", "Token position, people ahead, and estimated wait update in real time.", Bell],
    ["Status Tracking", "Clear movement from booking to verification, procurement, and completion.", ShieldCheck],
    ["Payment Visibility", "Simulated payment state and procurement amount stay transparent.", BadgeIndianRupee]
  ];
  return (
    <main>
      <section className="hero">
        <div className="heroCopy">
          <p className="eyebrow">SIH26032 · Farmer Procurement Scheduling</p>
          <h1>Smart Procurement. Less Waiting. Faster Payments.</h1>
          <p>Helping farmers book procurement slots, track queues and monitor procurement payments in real time.</p>
          <div className="actions">
            <button className="primary" onClick={() => setView("booking")}>Book a Slot <ArrowRight size={18} /></button>
            <button className="secondary" onClick={() => setView("farmer")}>Track Booking</button>
          </div>
        </div>
        <div className="heroPanel">
          <div className="tokenCard">
            <span>Current Token</span>
            <strong>A119</strong>
            <p>Queue moving every 5 minutes</p>
          </div>
          <div className="miniQueue">
            {["A120", "A121", "A122", "A123"].map((x, i) => <span key={x} className={i === 0 ? "live" : ""}>{x}</span>)}
          </div>
        </div>
      </section>
      <section className="band">
        <h2>How It Works</h2>
        <div className="steps">
          {["Register farmer", "Choose centre and slot", "Receive token", "Track queue", "Complete procurement", "Monitor payment"].map((step, i) => (
            <div className="step" key={step}><b>{i + 1}</b><span>{step}</span></div>
          ))}
        </div>
      </section>
      <section className="featureGrid">
        {features.map(([title, body, Icon]) => <article key={title}><Icon size={24} /><h3>{title}</h3><p>{body}</p></article>)}
      </section>
      <section className="centres">
        <div><MapPinned size={24} /><h2>Procurement Centres</h2></div>
        <p>Lucknow, Kanpur, and Barabanki centres are preloaded with working hours, daily capacity, slots, and queues.</p>
      </section>
    </main>
  );
}
