import { useState } from "react";
import { authRequest } from "../../api/auth.js";
import { PageHeading } from "../../components/WorkspaceUI.jsx";

export function AccountSettings({ onLogout }) {
    const [notice, setNotice] = useState(""); const [error, setError] = useState(""); const [busy, setBusy] = useState(false);
    async function changePassword(event) { event.preventDefault(); const submittedForm = event.currentTarget; const data = new FormData(submittedForm); setBusy(true); setError(""); setNotice(""); try { await authRequest("/change-password", { method: "POST", body: JSON.stringify({ currentPassword: data.get("currentPassword"), newPassword: data.get("newPassword") }) }); setNotice("Password changed."); submittedForm.reset(); } catch (e) { setError(e.message); } finally { setBusy(false); } }
    async function deactivate() { if (!window.confirm("Deactivate your LocalGig account? You will be signed out.")) return; try { await authRequest("/account", { method: "DELETE" }); await onLogout(); } catch (e) { setError(e.message); } }
    return <><PageHeading eyebrow="ACCOUNT" title="Account settings" copy="Change your password or deactivate your account." />{notice && <div className="notice success">{notice}</div>}{error && <div className="notice error">{error}</div>}<form className="panel post-form" onSubmit={changePassword}><h2>Change password</h2><label>Current password<input type="password" name="currentPassword" autoComplete="current-password" required /></label><label>New password<input type="password" name="newPassword" minLength="8" autoComplete="new-password" required /></label><button className="button primary" disabled={busy}>{busy ? "Saving…" : "Change password"}</button></form><div className="panel danger-zone"><h2>Deactivate account</h2><p>Your account will be disabled and this browser session will end.</p><button className="button secondary" onClick={deactivate}>Deactivate account</button></div></>;
}
