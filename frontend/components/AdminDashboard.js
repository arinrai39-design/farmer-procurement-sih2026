import { useEffect, useMemo, useState } from "react";
import { BarChart, Bar, CartesianGrid, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis, Cell } from "recharts";
import { CheckCircle2, FastForward, IndianRupee, PlayCircle, ShieldCheck, UserCheck } from "lucide-react";
import { api } from "../services/api";
import StatusBadge from "./StatusBadge";

const colors = ["#23684a", "#d69a2d", "#2f78a0", "#a33f49"];

export default function AdminDashboard() {
  const [queue, setQueue] = useState([]);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState("");

  async function load() {
    try {
      const [queueData, dashboardData] = await Promise.all([api.queue(1), api.adminDashboard(1)]);
      setQueue(queueData);
      setStats(dashboardData);
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => { load(); }, []);

  async function action(work) {
    setError("");
    try {
      await work();
      await load();
    } catch (err) {
      setError(err.message);
    }
  }

  const cropData = useMemo(() => Object.entries(stats?.cropWiseQuantity || {}).map(([name, quantity]) => ({ name, quantity })), [stats]);
  const paymentData = useMemo(() => Object.entries(stats?.paymentStatus || {}).map(([name, value]) => ({ name, value })), [stats]);

  return (
    <main className="dashboard admin">
      <section className="titleRow">
        <div><p className="eyebrow">Admin Dashboard</p><h1>Lucknow Procurement Centre</h1></div>
        <button className="primary" onClick={() => action(() => api.callNext(1))}><FastForward size={18} />Call Next Farmer</button>
      </section>
      {error && <p className="error">{error}</p>}
      <section className="kpiGrid">
        <div className="metric"><UserCheck /><span>Today's Farmers</span><strong>{stats?.todaysFarmers || 0}</strong></div>
        <div className="metric"><PlayCircle /><span>Waiting</span><strong>{stats?.waiting || 0}</strong></div>
        <div className="metric"><ShieldCheck /><span>Processing</span><strong>{stats?.processing || 0}</strong></div>
        <div className="metric"><CheckCircle2 /><span>Completed</span><strong>{stats?.completed || 0}</strong></div>
        <div className="metric"><IndianRupee /><span>Pending Payments</span><strong>Rs {stats?.pendingPayments || 0}</strong></div>
      </section>
      <section className="split">
        <article className="panel chartPanel">
          <h2>Crop-wise Quantity</h2>
          <ResponsiveContainer height={220}>
            <BarChart data={cropData}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip />
              <Bar dataKey="quantity" fill="#23684a" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </article>
        <article className="panel chartPanel">
          <h2>Payment Status</h2>
          <ResponsiveContainer height={220}>
            <PieChart>
              <Pie data={paymentData} dataKey="value" nameKey="name" outerRadius={82} label>
                {paymentData.map((_, i) => <Cell key={i} fill={colors[i % colors.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </article>
      </section>
      <section className="panel tablePanel">
        <h2>Today's Queue</h2>
        <div className="tableScroll">
          <table>
            <thead><tr><th>Token</th><th>Farmer</th><th>Crop</th><th>Quantity</th><th>Slot</th><th>Status</th><th>Payment</th><th>Action</th></tr></thead>
            <tbody>
              {queue.map((row) => (
                <tr key={row.id}>
                  <td><b>{row.tokenNumber}</b></td>
                  <td>{row.farmerName}<small>{row.mobile}</small></td>
                  <td>{row.crop}</td>
                  <td>{row.quantityKg} kg</td>
                  <td>{row.slot}</td>
                  <td><StatusBadge value={row.status} /></td>
                  <td><StatusBadge value={row.paymentStatus} /></td>
                  <td className="rowActions">
                    <button onClick={() => action(() => api.setStatus(row.id, "ARRIVED"))}>Arrived</button>
                    <button onClick={() => action(() => api.setStatus(row.id, "VERIFICATION"))}>Verify</button>
                    <button onClick={() => action(() => api.setStatus(row.id, "PROCUREMENT"))}>Procure</button>
                    <button onClick={() => action(() => api.setStatus(row.id, "COMPLETED"))}>Complete</button>
                    <button onClick={() => action(() => api.setPayment(row.id, "PAID"))}>Paid</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </main>
  );
}
