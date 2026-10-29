import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function NotificationsPage({ onUnreadChange }) {
    const [items, setItems] = useState([]); const [error, setError] = useState("");
    useEffect(() => { platformRequest("/notifications").then((rows) => { setItems(rows); onUnreadChange(rows.filter((item) => !item.read).length); }).catch((e) => setError(e.message)); }, []);
    async function markRead(id) { try { const updated = await platformRequest(`/notifications/${id}/read`, { method: "PATCH" }); setItems((rows) => { const next = rows.map((item) => item.id === id ? updated : item); onUnreadChange(next.filter((item) => !item.read).length); return next; }); } catch (e) { setError(e.message); } }
    return <><PageHeading eyebrow="UPDATES" title="Notifications" copy="Application updates, matching roles, and reminders." />{error && <div className="notice error">{error}</div>}<div className="panel">{items.map((item) => <div className="notification-row" key={item.id}><span className={`notification-dot ${item.read ? "read" : ""}`} /><div><strong>{item.title}</strong><p>{item.body}</p><small>{new Date(item.createdAt).toLocaleString()}</small></div>{!item.read && <button className="text-button" onClick={() => markRead(item.id)}>Mark read</button>}</div>)}{!items.length && !error && <EmptyState text="You’re all caught up." />}</div></>;
}
