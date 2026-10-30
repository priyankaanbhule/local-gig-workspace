import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

const seekerPreview = [
    { title: "Application accepted", body: "Northstar Digital would like to connect with you about the Java Developer opportunity.", age: "Today · 10:24 AM", tone: "success" },
    { title: "Application update", body: "Your application for Frontend Developer at Cedar Labs was not selected. Keep your profile updated for future roles.", age: "Yesterday · 4:40 PM" },
    { title: "Application update", body: "The Product Support Associate role at BrightPath has moved forward with another candidate.", age: "Yesterday · 11:15 AM" },
];

const recruiterPreview = [
    { title: "New application received", body: "A candidate applied for your Software Engineer opportunity. Review their profile and resume.", age: "Today · 10:18 AM", action: true },
    { title: "Applications need review", body: "Two applications are waiting in your hiring pipeline.", age: "Today · 9:05 AM", action: true },
    { title: "Candidate shortlisted", body: "A shortlisted candidate is ready for your next step: connect, accept, or decline.", age: "Yesterday · 3:32 PM", action: true },
];

export function NotificationsPage({ onUnreadChange, user, onNavigate }) {
    const [items, setItems] = useState([]); const [error, setError] = useState("");
    const isEmployer = ["EMPLOYER", "ADMIN"].includes(String(user?.role).toUpperCase());
    useEffect(() => {
        let alive = true;
        const refresh = () => platformRequest("/notifications").then((rows) => {
            if (!alive) return;
            setItems(rows); onUnreadChange(rows.filter((item) => !item.read).length); setError("");
        }).catch((e) => { if (alive) setError(e.message); });
        refresh(); const timer = window.setInterval(refresh, 15000);
        return () => { alive = false; window.clearInterval(timer); };
    }, [onUnreadChange]);
    async function markRead(id) {
        try { const updated = await platformRequest(`/notifications/${id}/read`, { method: "PATCH" }); setItems((rows) => { const next = rows.map((item) => item.id === id ? updated : item); onUnreadChange(next.filter((item) => !item.read).length); return next; }); }
        catch (e) { setError(e.message); }
    }
    const preview = isEmployer ? recruiterPreview : seekerPreview;
    return <><PageHeading eyebrow="UPDATES" title="Notifications" copy="Application updates, matching roles, and reminders." />{error && <div className="notice error">{error}</div>}
        <div className="panel"><div className="section-title"><h2>Your activity</h2><span className="muted small">Live account updates</span></div>{items.map((item) => <div className="notification-row" key={item.id}><span className={`notification-dot ${item.read ? "read" : ""}`} /><div><strong>{item.title}</strong><p>{item.body}</p><small>{new Date(item.createdAt).toLocaleString()}</small></div>{!item.read && <button className="text-button" onClick={() => markRead(item.id)}>Mark read</button>}</div>)}{!items.length && !error && <EmptyState text="New updates will appear here." />}</div>
        {!!preview.length && <div className="panel sample-review-panel"><div className="section-title"><div><p className="eyebrow">PRESENTATION PREVIEW</p><h2>{isEmployer ? "Hiring activity" : "Application activity"}</h2></div><span className="status-pill">Preview</span></div><p className="muted">Illustrative activity for the walkthrough. These entries are separate from your saved account notifications.</p>{preview.map((item) => <div className="notification-row" key={item.title + item.age}><span className={`notification-dot ${item.tone === "success" ? "" : "read"}`} /><div><strong>{item.title}</strong><p>{item.body}</p><small>{item.age}</small></div>{item.action && <button className="text-button" onClick={() => onNavigate?.("Review applications")}>Review applications →</button>}</div>)}</div>}
    </>;
}
