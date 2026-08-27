import { BarChart3, ClipboardList, Home, LogOut, Sprout, UserRound } from "lucide-react";

export default function Header({ view, setView, session, onLogout }) {
  return (
    <header className="topbar">
      <button className="brand" onClick={() => setView("landing")} title="Home">
        <Sprout size={24} />
        <span>Smart Procurement</span>
      </button>
      <nav>
        <button className={view === "landing" ? "active" : ""} onClick={() => setView("landing")} title="Home"><Home size={18} />Home</button>
        <button onClick={() => setView("booking")} title="Book slot"><ClipboardList size={18} />Book Slot</button>
        <button onClick={() => setView(["ADMIN", "OFFICER"].includes(session?.role) ? "admin" : "farmer")} title="Dashboard"><BarChart3 size={18} />Dashboard</button>
      </nav>
      <div className="userbar">
        {session ? (
          <>
            <span><UserRound size={16} />{session.name}</span>
            <button onClick={onLogout} title="Logout"><LogOut size={17} /></button>
          </>
        ) : (
          <button onClick={() => setView("login")}>Login</button>
        )}
      </div>
    </header>
  );
}
