import { useState } from "react";
import { api } from "../services/api";
import { setSession } from "../utils/storage";

export default function AuthPanel({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    identifier: "9876501001", password: "farmer123", fullName: "", mobile: "",
    farmerId: "", address: "", village: "", district: "", state: "Uttar Pradesh"
  });

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }));

  async function submit(e) {
    e.preventDefault();
    setError("");
    try {
      const session = mode === "login"
        ? await api.login({ identifier: form.identifier, password: form.password })
        : await api.register(form);
      setSession(session);
      onAuth(session);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="authWrap">
      <form className="panel formPanel" onSubmit={submit}>
        <div className="segmented">
          <button type="button" className={mode === "login" ? "selected" : ""} onClick={() => setMode("login")}>Login</button>
          <button type="button" className={mode === "register" ? "selected" : ""} onClick={() => setMode("register")}>Register</button>
        </div>
        {error && <p className="error">{error}</p>}
        {mode === "login" ? (
          <>
            <label>Mobile Number / Farmer ID<input value={form.identifier} onChange={(e) => update("identifier", e.target.value)} /></label>
            <label>Password<input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} /></label>
            <div className="hint">Demo: farmer `9876501001` / `farmer123`, admin `admin` / `admin123`</div>
          </>
        ) : (
          <>
            {["fullName", "mobile", "farmerId", "address", "village", "district", "state"].map((field) => (
              <label key={field}>{field.replace(/([A-Z])/g, " $1")}<input value={form[field]} onChange={(e) => update(field, e.target.value)} required /></label>
            ))}
            <label>Password<input type="password" value={form.password} onChange={(e) => update("password", e.target.value)} required /></label>
          </>
        )}
        <button className="primary wide">{mode === "login" ? "Login" : "Create Farmer Account"}</button>
      </form>
    </main>
  );
}
