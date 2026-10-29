import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function ProposalsPage({ proposals, setProposals }) {
    const [error, setError] = useState("");
    async function withdraw(proposal) {
        try {
            await platformRequest(`/proposals/${proposal.id}`, { method: "DELETE" });
            setProposals((items) => items.map((item) => item.id === proposal.id ? { ...item, status: "WITHDRAWN" } : item));
        } catch (requestError) { setError(requestError.message); }
    }
    return <><PageHeading eyebrow="FREELANCER WORKSPACE" title="My proposals" copy="Review your bids, delivery estimates, and client decisions." />{error && <div className="notice error">{error}</div>}<div className="panel">{proposals.map((proposal) => <div className="table-row" key={proposal.id}><div><strong>{proposal.opportunityTitle}</strong><span>{proposal.company} · ₹{proposal.bidAmount} · {proposal.estimatedDays} days</span><span>{proposal.coverLetter}</span></div><span className="status-pill">{proposal.status.replaceAll("_", " ")}</span>{["SUBMITTED", "SHORTLISTED"].includes(proposal.status) && <button className="text-button danger-text" onClick={() => withdraw(proposal)}>Withdraw</button>}</div>)}{!proposals.length && <EmptyState text="Your gig proposals will appear here after you send one." />}</div></>;
}

export function ProjectsPage({ user }) {
    const [projects, setProjects] = useState([]);
    const [error, setError] = useState("");
    const employer = ["EMPLOYER", "ADMIN"].includes(String(user.role).toUpperCase());
    const refresh = () => platformRequest("/projects/me").then(setProjects).catch((requestError) => setError(requestError.message));
    useEffect(() => { refresh(); }, []);
    async function update(project, changes) {
        try {
            const updated = await platformRequest(`/projects/${project.id}/progress`, { method: "PATCH", body: JSON.stringify({ progressPercent: Number(changes.progressPercent), progressNote: changes.progressNote || "", deliverableUrl: changes.deliverableUrl || null }) });
            setProjects((items) => items.map((item) => item.id === updated.id ? updated : item));
            setError("");
        } catch (requestError) { setError(requestError.message); }
    }
    async function complete(project) {
        try {
            const updated = await platformRequest(`/projects/${project.id}/complete`, { method: "PATCH" });
            setProjects((items) => items.map((item) => item.id === updated.id ? updated : item));
            setError("");
        } catch (requestError) { setError(requestError.message); }
    }
    return <><PageHeading eyebrow="PROJECT WORKSPACE" title="My projects" copy="Track delivery progress, share completed work, and confirm project completion." />{error && <div className="notice error">{error}</div>}<div className="project-grid">{projects.map((project) => <ProjectCard key={project.id} project={project} employer={employer} onUpdate={update} onComplete={complete} />)}{!projects.length && !error && <div className="panel"><EmptyState text="Projects appear here when a client accepts your gig proposal." /></div>}</div></>;
}

export function ProjectCard({ project, employer, onUpdate, onComplete }) {
    const [progress, setProgress] = useState(project.progressPercent);
    const [note, setNote] = useState(project.progressNote || "");
    const [deliverable, setDeliverable] = useState(project.deliverableUrl || "");
    return <article className="panel project-card"><div className="section-title"><div><p className="eyebrow">{project.company}</p><h2>{project.title}</h2></div><span className="status-pill">{project.status.replaceAll("_", " ")}</span></div><p className="muted">{employer ? `Freelancer: ${project.freelancerName}` : `Client: ${project.employerName}`} · Agreed ₹{project.agreedBudget}</p><p>Due {new Date(`${project.dueDate}T00:00:00`).toLocaleDateString()}</p><div className="progress-track"><span style={{ width: `${project.progressPercent}%` }} /></div><strong>{project.progressPercent}% complete</strong>{project.progressNote && <p>{project.progressNote}</p>}{project.deliverableUrl && <p><a className="text-button" href={fileHref(project.deliverableUrl)} target="_blank" rel="noreferrer">View submitted deliverable</a></p>}{!employer && project.status !== "COMPLETED" && <form className="project-update" onSubmit={(event) => { event.preventDefault(); onUpdate(project, { progressPercent: progress, progressNote: note, deliverableUrl: deliverable }); }}><label>Progress (%)<input type="number" min="0" max="100" value={progress} onChange={(event) => setProgress(event.target.value)} /></label><label>Progress note<input value={note} maxLength="2000" onChange={(event) => setNote(event.target.value)} placeholder="What have you completed?" /></label><label>Completed work URL (optional)<input type="url" value={deliverable} maxLength="500" onChange={(event) => setDeliverable(event.target.value)} placeholder="Share a link to your work" /></label><button className="button primary">Save progress</button></form>}{employer && project.status === "SUBMITTED" && <button className="button primary" onClick={() => onComplete(project)}>Confirm completion</button>}</article>;
}

export function GigProposalPanel({ opportunity }) {
    const [open, setOpen] = useState(false);
    const [items, setItems] = useState([]);
    const [error, setError] = useState("");
    async function load() {
        try { setItems(await platformRequest(`/opportunities/${opportunity.id}/proposals`)); setOpen(true); setError(""); }
        catch (requestError) { setError(requestError.message); }
    }
    async function review(proposal, status) {
        try {
            const updated = await platformRequest(`/proposals/${proposal.id}/status`, { method: "PATCH", body: JSON.stringify({ status }) });
            setItems((all) => all.map((item) => item.id === updated.id ? updated : item));
            setError("");
        } catch (requestError) { setError(requestError.message); }
    }
    return <section className="panel proposal-panel"><div className="section-title"><div><p className="eyebrow">GIG PROPOSALS</p><h2>{opportunity.title}</h2></div><button className="button secondary" onClick={open ? () => setOpen(false) : load}>{open ? "Hide proposals" : "Review proposals"}</button></div>{error && <div className="notice error">{error}</div>}{open && (items.length ? items.map((proposal) => <div className="table-row" key={proposal.id}><div><strong>{proposal.freelancerName} · ₹{proposal.bidAmount}</strong><span>{proposal.estimatedDays} delivery days · {proposal.status}</span><span>{proposal.coverLetter}</span></div>{["SUBMITTED", "SHORTLISTED"].includes(proposal.status) && <><button className="text-button" onClick={() => review(proposal, "SHORTLISTED")}>Shortlist</button><button className="button primary small-button" onClick={() => review(proposal, "ACCEPTED")}>Accept</button><button className="text-button danger-text" onClick={() => review(proposal, "REJECTED")}>Reject</button></>}</div>) : <EmptyState text="No proposals have been submitted for this gig." />)}</section>;
}
