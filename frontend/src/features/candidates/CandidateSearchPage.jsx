import { useState } from "react";
import { platformRequest, fileHref } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function CandidateSearchPage({ onMessage }) {
    const [q, setQ] = useState("");
    const [location, setLocation] = useState("");
    const [availableOnly, setAvailableOnly] = useState(false);
    const [items, setItems] = useState([]);
    const [searched, setSearched] = useState(false);
    const [error, setError] = useState("");

    async function loadCandidates(event) {
        event.preventDefault();
        setError("");
        try {
            const params = new URLSearchParams();
            if (q.trim()) params.set("q", q.trim());
            if (location.trim()) params.set("location", location.trim());
            params.set("available", String(availableOnly));
            setItems(await platformRequest(`/candidates/search?${params}`));
            setSearched(true);
        } catch (e) { setError(e.message); }
    }

    async function report(person) {
        const reason = window.prompt(`Report ${person.fullName}. Please provide a reason:`);
        if (!reason?.trim()) return;
        try {
            await platformRequest("/reports", { method: "POST", body: JSON.stringify({ targetType: "USER", targetId: person.userId, reason: reason.trim(), details: "" }) });
            setError("Profile report submitted for admin review.");
        } catch (e) { setError(e.message); }
    }

    return <>
        <PageHeading eyebrow="TALENT SEARCH" title="Find candidates" copy="Search candidate profiles using the skills, role, or location you need." />
        {error && <div className="notice error">{error}</div>}
        <form className="filters" onSubmit={loadCandidates}>
            <input aria-label="Skills, role, or keyword" placeholder="Skills, role, or keyword" value={q} onChange={(e) => setQ(e.target.value)} />
            <input aria-label="Location" placeholder="Location" value={location} onChange={(e) => setLocation(e.target.value)} />
            <label className="checkbox-filter"><input type="checkbox" checked={availableOnly} onChange={(e) => setAvailableOnly(e.target.checked)} /> Available only</label>
            <button className="button primary">Search candidates</button>
        </form>
        <div className="section-title"><h2>{searched ? `${items.length} candidates` : "Candidate profiles"}</h2><span className="muted small">Profiles show candidate-provided information</span></div>
        {!searched && <div className="panel"><EmptyState text="Enter any skills, role, or location to find matching candidates." /></div>}
        <div className="candidate-grid">{items.map((person) => <article className="panel candidate-card" key={person.id}>
            <div className="candidate-card-heading"><div className="avatar">{(person.fullName || "C").slice(0, 1).toUpperCase()}</div><div><strong>{person.fullName}</strong><span>{person.headline || "Candidate"}</span><span>{[person.city, person.country].filter(Boolean).join(", ") || "Location not listed"}</span>{person.sampleProfile && <span className="status-pill">Demo profile</span>}</div><span className={`status-pill ${person.available ? "" : "warning"}`}>{person.available ? "Available" : "Not available"}</span></div>
            <p>{person.summary || "No profile summary provided."}</p><div className="tag-row">{(person.skills || []).map((skill) => <span className="tag" key={skill}>{skill}</span>)}</div>
            {person.preferredRoles?.length > 0 && <p><strong>Interested in:</strong> {person.preferredRoles.join(", ")}</p>}
            {person.experience?.length > 0 && <details><summary>Work experience</summary><ul>{person.experience.map((item) => <li key={item}>{item}</li>)}</ul></details>}
            <div className="candidate-actions">{person.portfolioUrl && <a className="text-button" href={fileHref(person.portfolioUrl)} target="_blank" rel="noreferrer">Portfolio</a>}{person.resumeUrl && <a className="text-button" href={fileHref(person.resumeUrl)} target="_blank" rel="noreferrer">Resume</a>}<button className="button secondary small-button" onClick={() => onMessage?.(person)}>Message candidate</button><button className="text-button danger-text" onClick={() => report(person)}>Report profile</button></div>
        </article>)}{searched && !items.length && <div className="panel"><EmptyState text="No profiles match those filters. Try removing a filter." /></div>}</div>
    </>;
}
