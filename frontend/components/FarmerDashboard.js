import { useEffect, useState } from "react";
import { BellRing, Clock3, IndianRupee, Route } from "lucide-react";
import { api } from "../services/api";
import StatusBadge from "./StatusBadge";

const flow = ["Slot Booked", "Waiting", "Arrived", "Verification", "Procurement", "Completed"];
const statusIndex = { WAITING: 1, CALLED: 1, ARRIVED: 2, VERIFICATION: 3, PROCUREMENT: 4, COMPLETED: 5 };

export default function FarmerDashboard({ session }) {
  const [booking, setBooking] = useState(null);
  const [notes, setNotes] = useState([]);
  const [error, setError] = useState("");

  async function load() {
    if (!session?.farmerId) return;
    try {
      const [bookings, notifications] = await Promise.all([
        api.myBookings(),
        api.notifications(session.userId)
      ]);
      setBooking(bookings[0]);
      setNotes(notifications);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => { load(); const t = setInterval(load, 5000); return () => clearInterval(t); }, [session?.farmerId]);

  if (!session) return <main className="emptyState">Please login as a farmer to track your booking.</main>;
  if (!booking) return <main className="emptyState">{error || "No active booking yet. Book a slot to begin tracking."}</main>;

  const currentStep = statusIndex[booking.status] ?? 1;
  return (
    <main className="dashboard">
      <section className="titleRow">
        <div><p className="eyebrow">Farmer Dashboard</p><h1>{session.name}</h1></div>
        <StatusBadge value={booking.status} />
      </section>
      <section className="kpiGrid">
        <div className="metric"><Route /><span>Token</span><strong>{booking.tokenNumber}</strong></div>
        <div className="metric"><Clock3 /><span>Queue Position</span><strong>{booking.queuePosition}</strong><small>{booking.peopleAhead} farmers ahead</small></div>
        <div className="metric"><BellRing /><span>Estimated Wait</span><strong>{booking.estimatedWait}</strong><small>{booking.congestion} · {booking.confidence} confidence</small></div>
        <div className="metric"><IndianRupee /><span>Payment</span><strong><StatusBadge value={booking.paymentStatus} /></strong></div>
      </section>
      <section className="split">
        <article className="panel">
          <h2>Current Booking</h2>
          <div className="infoGrid">
            <span>Farmer ID</span><b>{booking.farmerCode}</b>
            <span>Centre</span><b>{booking.centre}</b>
            <span>Crop</span><b>{booking.crop}</b>
            <span>Quantity</span><b>{booking.quantityKg} kg</b>
            <span>Slot</span><b>{booking.date} · {booking.slot}</b>
            <span>Amount</span><b>Rs {booking.amount || 0}</b>
            <span>Service Avg</span><b>{Number(booking.averageServiceMinutes || 0).toFixed(1)} min · {booking.activeCounters} counters</b>
          </div>
        </article>
        <article className="panel">
          <h2>Procurement Timeline</h2>
          <div className="timeline">
            {flow.map((label, i) => <div key={label} className={i < currentStep ? "done" : i === currentStep ? "now" : ""}><span />{label}</div>)}
          </div>
        </article>
      </section>
      <section className="panel">
        <h2>Notifications</h2>
        <div className="notifications">{notes.slice(0, 5).map((n) => <p key={n.id}>{n.message}</p>)}</div>
      </section>
    </main>
  );
}
