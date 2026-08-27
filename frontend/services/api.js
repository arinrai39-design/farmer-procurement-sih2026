import { getSession } from "../utils/storage";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE || "http://localhost:8080/api";

async function request(path, options = {}) {
  const session = getSession();
  const authHeaders = session?.token ? { Authorization: `Bearer ${session.token}` } : {};
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...authHeaders, ...(options.headers || {}) },
    cache: "no-store"
  });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "Server error" }));
    throw new Error(error.message || "Request failed");
  }
  return response.json();
}

export const api = {
  login: (payload) => request("/auth/login", { method: "POST", body: JSON.stringify(payload) }),
  register: (payload) => request("/auth/register", { method: "POST", body: JSON.stringify(payload) }),
  centres: () => request("/centres"),
  crops: () => request("/crops"),
  availableSlots: (centreId, date) => request(`/slots/available?centreId=${centreId}&date=${date}`),
  bookSlot: (payload) => request("/slots/book", { method: "POST", body: JSON.stringify(payload) }),
  farmerBookings: (farmerId) => request(`/farmers/${farmerId}/bookings`),
  myBookings: () => request("/bookings/my"),
  cancelBooking: (id, reason = "") => request(`/bookings/${id}/cancel`, { method: "PUT", body: JSON.stringify({ reason }) }),
  rescheduleBooking: (id, payload) => request(`/bookings/${id}/reschedule`, { method: "PUT", body: JSON.stringify(payload) }),
  queue: (centreId = 1) => request(`/admin/queue?centreId=${centreId}`),
  callNext: (centreId = 1) => request(`/queue/next?centreId=${centreId}`, { method: "PUT" }),
  setStatus: (id, status) => request(`/procurements/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) }),
  completeProcurement: (id, payload) => request(`/officer/bookings/${id}/complete`, { method: "PUT", body: JSON.stringify(payload) }),
  setPayment: (id, status) => request(`/payments/${id}/status`, { method: "PUT", body: JSON.stringify({ status }) }),
  notifications: (userId) => request(`/notifications/${userId}`),
  adminDashboard: (centreId = 1) => request(`/admin/dashboard?centreId=${centreId}`),
  audit: () => request("/admin/audit")
};
