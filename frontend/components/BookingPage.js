import { useEffect, useState } from "react";
import { CalendarCheck } from "lucide-react";
import { api } from "../services/api";

const today = () => {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
};

export default function BookingPage({ session, setView }) {
  const [centres, setCentres] = useState([]);
  const [crops, setCrops] = useState([]);
  const [slots, setSlots] = useState([]);
  const [error, setError] = useState("");
  const [confirmation, setConfirmation] = useState(null);
  const [form, setForm] = useState({ centreId: 1, cropId: 1, quantityKg: 2500, date: today(), slotId: "" });

  const update = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  useEffect(() => {
    Promise.all([api.centres(), api.crops()]).then(([centreData, cropData]) => {
      setCentres(centreData);
      setCrops(cropData);
    }).catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    api.availableSlots(form.centreId, form.date).then((data) => {
      setSlots(data);
      update("slotId", data.find((slot) => !slot.full)?.id || "");
    }).catch((err) => setError(err.message));
  }, [form.centreId, form.date]);

  async function submit(e) {
    e.preventDefault();
    if (!session?.farmerId) {
      setError("Please login as a farmer before booking a slot.");
      setView("login");
      return;
    }
    try {
      const booking = await api.bookSlot({ ...form, farmerId: session.farmerId, quantityKg: Number(form.quantityKg) });
      setConfirmation(booking);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="bookingPage">
      <section className="titleRow">
        <div><p className="eyebrow">Slot Booking</p><h1>Reserve a procurement visit</h1></div>
      </section>
      <form className="panel bookingForm" onSubmit={submit}>
        {error && <p className="error">{error}</p>}
        <label>Procurement Centre<select value={form.centreId} onChange={(e) => update("centreId", Number(e.target.value))}>{centres.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select></label>
        <label>Crop<select value={form.cropId} onChange={(e) => update("cropId", Number(e.target.value))}>{crops.map((c) => <option key={c.id} value={c.id}>{c.name} · Rs {c.ratePerKg}/kg</option>)}</select></label>
        <label>Quantity (kg)<input type="number" min="1" value={form.quantityKg} onChange={(e) => update("quantityKg", e.target.value)} /></label>
        <label>Date<input type="date" value={form.date} onChange={(e) => update("date", e.target.value)} /></label>
        <div className="slotGrid">
          {slots.map((slot) => (
            <button type="button" key={slot.id} disabled={slot.full} className={form.slotId === slot.id ? "slot selected" : "slot"} onClick={() => update("slotId", slot.id)}>
              <CalendarCheck size={18} />
              <b>{slot.timeRange}</b>
              <span>{slot.full ? "FULL" : `${slot.available} / ${slot.capacity} slots available`}</span>
              <small>{slot.congestion} · {slot.estimatedWaitMinutes} min ETA</small>
              {slot.recommended && <em>Recommended</em>}
            </button>
          ))}
        </div>
        <button className="primary wide" disabled={!form.slotId}>Book Slot</button>
      </form>
      {confirmation && (
        <section className="confirmation">
          <h2>BOOKING CONFIRMED</h2>
          <strong>Token: {confirmation.tokenNumber}</strong>
          <p>{confirmation.date} · {confirmation.slot}</p>
          <p>{confirmation.centre}</p>
          <button className="secondary" onClick={() => setView("farmer")}>Open Farmer Dashboard</button>
        </section>
      )}
    </main>
  );
}
