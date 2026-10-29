import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function JobAlertsPage() {
    const [items, setItems] = useState([]); const [keywords, setKeywords] = useState(""); const [location, setLocation] = useState(""); const [skills, setSkills] = useState(""); const [error, setError] = useState("");
    const refresh = () => platformRequest("/job-alerts").then(setItems).catch((e) => setError(e.message));
    useEffect(() => { refresh(); }, []);
    async function create(event) { event.preventDefault(); try { await platformRequest("/job-alerts", { method: "POST", body: JSON.stringify({ keywords, location, skills: skills.split(",").map((s) => s.trim()).filter(Boolean) }) }); setKeywords(""); setLocation(""); setSkills(""); await refresh(); } catch (e) { setError(e.message); } }
    async function remove(id) { try { await platformRequest(`/job-alerts/${id}`, { method: "DELETE" }); setItems((all) => all.filter((item) => item.id !== id)); } catch (e) { setError(e.message); } }
    return <><PageHeading eyebrow="PERSONALIZED SEARCH" title="Job alerts" copy="Get in-app notifications when new listings match your interests." />{error && <div className="notice error">{error}</div>}<form className="panel post-form" onSubmit={create}><label>Keywords<input value={keywords} onChange={(e) => setKeywords(e.target.value)} placeholder="Frontend developer" /></label><label>Location<input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="Pune or Remote" /></label><label>Skills, separated by commas<input value={skills} onChange={(e) => setSkills(e.target.value)} /></label><button className="button primary">Create alert</button></form><div className="panel">{items.map((item) => <div className="table-row" key={item.id}><div><strong>{item.keywords || "Any opportunity"}</strong><span>{item.location || "Any location"} · {(item.skills || []).join(", ")}</span></div><button className="text-button danger-text" onClick={() => remove(item.id)}>Remove</button></div>)}{!items.length && <EmptyState text="Create an alert to hear about matching opportunities." />}</div></>;
}
