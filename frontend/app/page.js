"use client";

import { useEffect, useState } from "react";
import AdminDashboard from "../components/AdminDashboard";
import AuthPanel from "../components/AuthPanel";
import BookingPage from "../components/BookingPage";
import FarmerDashboard from "../components/FarmerDashboard";
import Header from "../components/Header";
import Landing from "../components/Landing";
import { clearSession, getSession } from "../utils/storage";

export default function Home() {
  const [view, setView] = useState("landing");
  const [session, setSessionState] = useState(null);

  useEffect(() => {
    const saved = getSession();
    if (saved) setSessionState(saved);
  }, []);

  function handleAuth(nextSession) {
    setSessionState(nextSession);
    setView(nextSession.role === "ADMIN" ? "admin" : "farmer");
  }

  function logout() {
    clearSession();
    setSessionState(null);
    setView("landing");
  }

  return (
    <>
      <Header view={view} setView={setView} session={session} onLogout={logout} />
      {view === "landing" && <Landing setView={setView} />}
      {view === "login" && <AuthPanel onAuth={handleAuth} />}
      {view === "booking" && <BookingPage session={session} setView={setView} />}
      {view === "farmer" && <FarmerDashboard session={session} />}
      {view === "admin" && <AdminDashboard />}
      <footer className="footer">
        <b>SIH26032</b>
        <span>Reduced waiting time · Transparent queue · Faster payment visibility</span>
      </footer>
    </>
  );
}
