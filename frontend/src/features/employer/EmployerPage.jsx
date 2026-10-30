import { useEffect, useState } from "react";
import { platformRequest, mapOpportunity, fileHref } from "../../api/platform.js";
import { PageHeading, Stat, EmptyState } from "../../components/WorkspaceUI.jsx";
import { GigProposalPanel } from "../projects/ProjectPages.jsx";

export function EmployerOverview({ user, onNavigate, setMessage }) {
    const [listings, setListings] = useState([]);
    const [stats, setStats] = useState(null);
    const [applications, setApplications] = useState([]);
    const [activity, setActivity] = useState([]);
    const [inbox, setInbox] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    useEffect(() => {
        let alive = true;
        async function load() {
            try {
                const [rows, summary, notifications, conversations] = await Promise.all([
                    platformRequest("/opportunities/mine"), platformRequest("/opportunities/mine/stats"),
                    platformRequest("/notifications"), platformRequest("/messages/direct/conversations"),
                ]);
                const mapped = rows.map(mapOpportunity);
                const applicantGroups = await Promise.all(mapped.map((item) =>
                    platformRequest(`/opportunities/${item.id}/applications`).catch(() => [])));
                if (!alive) return;
                setListings(mapped); setStats(summary);
                setApplications(applicantGroups.flat().map((application) => ({ ...application,
                    listingTitle: mapped.find((item) => item.id === application.opportunityId)?.title || application.opportunityTitle,
                })));
                setActivity(notifications.slice(0, 3)); setInbox(conversations.slice(0, 3));
                setLoading(false);
            } catch (requestError) { if (alive) { setError(requestError.message); setLoading(false); } }
        }
        load();
        return () => { alive = false; };
    }, []);
    const activeListings = listings.filter((item) => item.status === "Active");
    const reviewQueue = [...applications]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5);
    async function updateApplicant(application, status) {
        try {
            await platformRequest(`/applications/${application.id}/status`, { method: "PATCH", body: JSON.stringify({ status }) });
            setApplications((rows) => rows.map((item) => item.id === application.id ? { ...item, status } : item));
            const verb = status === "SELECTED" ? "accepted" : status === "REJECTED" ? "declined" : "shortlisted";
            setMessage?.(`Application ${verb}. The candidate received a notification${status === "SELECTED" || status === "REJECTED" ? " and a message" : ""}.`);
            const [notifications, summary] = await Promise.all([platformRequest("/notifications"), platformRequest("/opportunities/mine/stats")]);
            setActivity(notifications.slice(0, 3)); setStats(summary);
        } catch (requestError) { setError(requestError.message); }
    }
    return <>
        <div className="welcome-banner employer-welcome">
            <div><p className="eyebrow">RECRUITER WORKSPACE</p><h1>Welcome, {user.fullName?.split(" ")[0] || "recruiter"}.</h1>
                <p>Post opportunities, find local talent, and keep every applicant moving.</p></div>
            <button className="button light-button" onClick={() => onNavigate("Employer tools")}>+ Post an opportunity</button>
        </div>
        {error && <div className="notice error">{error}</div>}
        <div className="stat-grid">
            <Stat label="Active opportunities" value={loading ? "…" : stats?.activeListings ?? activeListings.length} icon="▣" />
            <Stat label="Applications to review" value={loading ? "…" : applications.filter((item) => ["SUBMITTED", "SHORTLISTED"].includes(String(item.status))).length} icon="▤" />
            <Stat label="Gig proposals" value={loading ? "…" : stats?.proposals ?? 0} icon="✉" />
            <Stat label="Company status" value="Manage profile →" icon="✓" onClick={() => onNavigate("Employer tools")} />
        </div>
        <div className="recruiter-shortcuts">
            <button className="panel recruiter-shortcut" onClick={() => onNavigate("Find candidates")}><span>♙</span><strong>Find candidates</strong><small>Search by the skills and location your role needs.</small><b>Search talent →</b></button>
            <button className="panel recruiter-shortcut" onClick={() => onNavigate("Review applications")}><span>▤</span><strong>Review applicants</strong><small>Shortlist, accept, or reject applicants.</small><b>Open application queue →</b></button>
            <button className="panel recruiter-shortcut" onClick={() => onNavigate("Messages")}><span>✉</span><strong>Messages</strong><small>Continue candidate and freelancer conversations.</small><b>Open inbox →</b></button>
        </div>
        <div className="section-title"><div><p className="eyebrow">HIRING PIPELINE</p><h2>Your active opportunities</h2></div><button className="text-button" onClick={() => onNavigate("Employer tools")}>Manage all →</button></div>
        <div className="panel">{activeListings.slice(0, 5).map((item) => <div className="table-row" key={item.id}><div><strong>{item.title}</strong><span>{item.company} · {item.location} · {item.type}</span></div><span className="status-pill">{item.status}</span><button className="text-button" onClick={() => onNavigate("Review applications")}>View applicants →</button></div>)}{!activeListings.length && loading && <EmptyState text="Loading your opportunities…" />}{!activeListings.length && !loading && !error && <EmptyState text="No active opportunities yet. Post one to start your hiring pipeline." />}</div>
        <div className="section-title"><div><p className="eyebrow">NEXT STEPS</p><h2>Applications to review</h2></div><button className="text-button" onClick={() => onNavigate("Review applications")}>Open application queue →</button></div>
        <div className="panel">{reviewQueue.map((application) => { const pending = ["SUBMITTED", "SHORTLISTED"].includes(String(application.status)); return <div className="table-row applicant-row" key={application.id}><div><strong>{application.applicantName}</strong><span>{application.listingTitle} · Applied {new Date(application.createdAt).toLocaleDateString()}</span><span className="status-pill">{String(application.status).replaceAll("_", " ")}</span></div>{pending && <><button className="button secondary small-button" onClick={() => updateApplicant(application, "SHORTLISTED")}>Shortlist</button><button className="button primary small-button" onClick={() => updateApplicant(application, "SELECTED")}>Accept</button><button className="text-button danger-text" onClick={() => updateApplicant(application, "REJECTED")}>Decline</button></>}</div>; })}{!reviewQueue.length && <EmptyState text={loading ? "Loading applications…" : "No applications have arrived yet. New applications will appear here and in notifications."} />}</div>
        <div className="recruiter-shortcuts">
            <button className="panel recruiter-shortcut" onClick={() => onNavigate("Notifications")}><span>♧</span><strong>Notifications</strong><small>{activity[0]?.title || "Application updates will appear here."}</small><b>View notifications →</b></button>
            <button className="panel recruiter-shortcut" onClick={() => onNavigate("Messages")}><span>✉</span><strong>Messages</strong><small>{inbox[0] ? `${inbox[0].fullName}: ${inbox[0].lastMessage || "Open conversation"}` : "Connect directly with candidates."}</small><b>Open inbox →</b></button>
        </div>
    </>;
}

export function EmployerApplicationsPage({ setMessage, onMessage }) {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let alive = true;
        async function load() {
            try {
                const listings = await platformRequest("/opportunities/mine");
                const groups = await Promise.all(listings.map(async (listing) => {
                    const applicants = await platformRequest(`/opportunities/${listing.id}/applications`);
                    return applicants.map((application) => ({ ...application, listingTitle: listing.title, listingCompany: listing.company }));
                }));
                if (alive) setApplications(groups.flat().sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
            } catch (requestError) { if (alive) setError(requestError.message); }
            finally { if (alive) setLoading(false); }
        }
        load();
        return () => { alive = false; };
    }, []);

    async function review(application, status) {
        try {
            await platformRequest(`/applications/${application.id}/status`, {
                method: "PATCH", body: JSON.stringify({ status }),
            });
            setApplications((rows) => rows.map((row) => row.id === application.id ? { ...row, status } : row));
            const label = status === "SELECTED" ? "accepted" : status === "REJECTED" ? "declined" : "shortlisted";
            setMessage?.(`Application ${label}. The candidate received an update${status === "SELECTED" || status === "REJECTED" ? " and a message" : ""}.`);
        } catch (requestError) { setError(requestError.message); }
    }

    return <>
        <PageHeading eyebrow="HIRING WORKSPACE" title="Review applications" copy="Review candidate details and update every application from one queue." />
        {error && <div className="notice error">{error}</div>}
        <div className="panel">
            {applications.map((application) => {
                const pending = ["SUBMITTED", "SHORTLISTED"].includes(String(application.status));
                return <article className="application-review-card" key={application.id}>
                    <div className="section-title"><div><h2>{application.applicantName}</h2><p className="muted">{application.listingTitle} · {application.listingCompany} · Applied {new Date(application.createdAt).toLocaleDateString()}</p></div><span className="status-pill">{String(application.status).replaceAll("_", " ")}</span></div>
                    <p>{application.coverLetter || "No cover letter provided."}</p>
                    <div className="application-resume-row">{application.resumeUrl ? <><span className="status-pill">Resume uploaded</span><a className="text-button" href={fileHref(application.resumeUrl)} target="_blank" rel="noreferrer">Open resume</a></> : <><span className="status-pill warning">No resume attached</span><span className="muted small">Message the candidate if you need a copy.</span></>}</div>
                    <div className="candidate-actions">{onMessage && <button className="button secondary small-button" onClick={() => onMessage(application)}>Message candidate</button>}{pending && <><button className="button secondary small-button" onClick={() => review(application, "SHORTLISTED")}>Shortlist</button><button className="button primary small-button" onClick={() => review(application, "SELECTED")}>Accept</button><button className="text-button danger-text" onClick={() => review(application, "REJECTED")}>Decline</button></>}</div>
                </article>;
            })}
            {loading && <EmptyState text="Loading applications…" />}
            {!loading && !applications.length && !error && <EmptyState text="No applications have arrived yet. Applications for your active opportunities will appear here." />}
        </div>
    </>;
}

export function EmployerPage({ listings, setListings, user, setMessage }) {
    const [showForm, setShowForm] = useState(false); const [editingId, setEditingId] = useState(null); const [title, setTitle] = useState(""); const [location, setLocation] = useState(""); const [type, setType] = useState("Full-time"); const [company, setCompany] = useState(""); const [budget, setBudget] = useState(""); const [description, setDescription] = useState(""); const [category, setCategory] = useState("Technology"); const [skills, setSkills] = useState(""); const [deadline, setDeadline] = useState(""); const [requiredExperienceYears, setRequiredExperienceYears] = useState(0); const [latitude, setLatitude] = useState(""); const [longitude, setLongitude] = useState(""); const [mine, setMine] = useState([]); const [stats, setStats] = useState(null); const [jobCategories, setJobCategories] = useState([]); const [error, setError] = useState("");
    const refresh = () => { platformRequest("/opportunities/mine").then((rows) => { const mapped = rows.map(mapOpportunity); setMine(mapped); setListings((all) => [...mapped, ...all.filter((x) => x.demo)]); }).catch((e) => setError(e.message)); platformRequest("/opportunities/mine/stats").then(setStats).catch(() => {}); platformRequest("/categories?scope=JOB").then(setJobCategories).catch(() => {}); };
    useEffect(() => { refresh(); }, []);
    async function createListing(event) { event.preventDefault(); try { const kind = type === "Internship" ? "INTERNSHIP" : type === "Freelance" ? "GIG" : "JOB"; const payload = { title, company: company || user.fullName, location: location || "Remote", category, kind, type, budget: budget || "To be discussed", description, skills: skills.split(",").map((s) => s.trim()).filter(Boolean), deadline: deadline || null, latitude: latitude === "" ? null : Number(latitude), longitude: longitude === "" ? null : Number(longitude), requiredExperienceYears: Number(requiredExperienceYears) }; const saved = await platformRequest(editingId ? `/opportunities/${editingId}` : "/opportunities", { method: editingId ? "PUT" : "POST", body: JSON.stringify(payload) }); const mapped = mapOpportunity(saved); setMine((all) => editingId ? all.map((x) => x.id === editingId ? mapped : x) : [mapped, ...all]); setListings((all) => editingId ? all.map((x) => x.id === editingId ? mapped : x) : [mapped, ...all]); setShowForm(false); setEditingId(null); setTitle(""); setMessage(editingId ? "Opportunity updated." : "Opportunity posted successfully."); } catch (e) { setError(e.message); } }
    function editListing(item) { setEditingId(item.id); setTitle(item.title); setCompany(item.company); setLocation(item.location); setType(item.type); setBudget(item.budget); setDescription(item.description); setCategory(item.category); setSkills((item.skills || []).join(", ")); setDeadline(item.deadline || ""); setRequiredExperienceYears(item.requiredExperienceYears || 0); setLatitude(item.latitude ?? ""); setLongitude(item.longitude ?? ""); setShowForm(true); }
    async function deleteListing(item) { if (!window.confirm(`Delete “${item.title}”?`)) return; try { await platformRequest(`/opportunities/${item.id}`, { method: "DELETE" }); setMine((all) => all.filter((x) => x.id !== item.id)); setListings((all) => all.filter((x) => x.id !== item.id)); } catch (e) { setError(e.message); } }
    async function changeStatus(item) { try { const updated = await platformRequest(`/opportunities/${item.id}/status?active=${item.status !== "Active"}`, { method: "PATCH" }); const mapped = mapOpportunity(updated); setMine((all) => all.map((x) => x.id === item.id ? mapped : x)); setListings((all) => all.map((x) => x.id === item.id ? mapped : x)); } catch (e) { setError(e.message); } }
    async function loadApplicants(listingId) { try { const rows = await platformRequest(`/opportunities/${listingId}/applications`); setMine((all) => all.map((x) => x.id === listingId ? { ...x, applicants: rows } : x)); } catch (e) { setError(e.message); } }
    async function status(applicationId, value, listingId) { try { await platformRequest(`/applications/${applicationId}/status`, { method: "PATCH", body: JSON.stringify({ status: value }) }); await loadApplicants(listingId); platformRequest("/opportunities/mine/stats").then(setStats).catch(() => {}); setMessage(value === "SELECTED" ? "Applicant accepted. A message and notification were sent." : value === "REJECTED" ? "Applicant rejected. A message and notification were sent." : `Applicant status updated to ${value.toLowerCase().replaceAll("_", " ")}.`); } catch (e) { setError(e.message); } }
    return <><PageHeading eyebrow="HIRING WORKSPACE" title="Employer tools" copy="Post jobs and gigs, review applicants, and manage your listings." />{error && <div className="notice error">{error}</div>}{stats && <div className="stat-grid"><Stat label="Total listings" value={stats.totalListings} icon="Listings"/><Stat label="Active listings" value={stats.activeListings} icon="Active"/><Stat label="Applications" value={stats.applications} icon="Applications"/><Stat label="Gig proposals" value={stats.proposals} icon="Proposals"/></div>}<div className="section-title"><h2>Your listings</h2><button className="button primary" onClick={() => { setEditingId(null); setShowForm(!showForm); }}>+ Post an opportunity</button></div>{showForm && <form className="panel post-form" onSubmit={createListing}><h2>{editingId ? "Edit opportunity" : "Post an opportunity"}</h2><label>Opportunity title<input value={title} onChange={(e) => setTitle(e.target.value)} required maxLength="160" /></label><label>Company name<input value={company} onChange={(e) => setCompany(e.target.value)} maxLength="160" /></label><label>Location<input value={location} onChange={(e) => setLocation(e.target.value)} required /></label><div className="form-grid"><label>Latitude (optional, for distance search)<input type="number" min="-90" max="90" step="any" value={latitude} onChange={(e) => setLatitude(e.target.value)} placeholder="e.g. 18.5204" /></label><label>Longitude (optional, for distance search)<input type="number" min="-180" max="180" step="any" value={longitude} onChange={(e) => setLongitude(e.target.value)} placeholder="e.g. 73.8567" /></label></div><label>Category<input list="job-category-options" value={category} onChange={(e) => setCategory(e.target.value)} required /></label><datalist id="job-category-options">{jobCategories.map((item) => <option key={item.id} value={item.name} />)}</datalist><label>Type<select value={type} onChange={(e) => setType(e.target.value)}><option>Full-time</option><option>Part-time</option><option>Remote</option><option>Internship</option><option>Freelance</option></select></label><label>Salary or budget<input value={budget} onChange={(e) => setBudget(e.target.value)} required /></label><label>Required experience (years)<input type="number" min="0" max="50" value={requiredExperienceYears} onChange={(e) => setRequiredExperienceYears(e.target.value)} /></label><label>Application deadline<input type="date" value={deadline} onChange={(e) => setDeadline(e.target.value)} /></label><label>Skills, separated by commas<input value={skills} onChange={(e) => setSkills(e.target.value)} /></label><label>Description<textarea value={description} onChange={(e) => setDescription(e.target.value)} rows="4" required maxLength="5000" /></label><button className="button primary">{editingId ? "Save changes" : "Create listing"}</button>{editingId && <button type="button" className="button secondary" onClick={() => { setEditingId(null); setShowForm(false); }}>Cancel</button>}</form>}
        <div className="panel">{mine.map((item) => <div key={item.id}><div className="table-row"><div><strong>{item.title}</strong><span>{item.location} · {item.type}</span></div><span className="status-pill">{item.status}</span><button className="text-button" onClick={() => changeStatus(item)}>{item.status === "Active" ? "Close" : "Reopen"}</button><button className="text-button" onClick={() => editListing(item)}>Edit</button><button className="text-button danger-text" onClick={() => deleteListing(item)}>Delete</button><button className="text-button" onClick={() => loadApplicants(item.id)}>Applicants</button></div>{item.applicants?.map((a) => <div className="table-row applicant-row" key={a.id}><div><strong>{a.applicantName}</strong><span>{a.applicantEmail} · {a.status === "SELECTED" ? "Accepted" : a.status}</span><span>{a.coverLetter || "No cover letter"}</span></div><a className="text-button" href={fileHref(a.resumeUrl) || "#"} target="_blank" rel="noreferrer">Resume</a><select aria-label={`Update ${a.applicantName} status`} value={a.status} onChange={(e) => status(a.id, e.target.value, item.id)}>{["SUBMITTED","SHORTLISTED","REJECTED","SELECTED","PROJECT_IN_PROGRESS","COMPLETED"].map((s) => <option key={s} value={s}>{s === "SELECTED" ? "Accept applicant" : s === "REJECTED" ? "Reject applicant" : s.replaceAll("_", " ")}</option>)}</select></div>)}</div>)}{!mine.length && <EmptyState text="Your posted opportunities will appear here." />}</div>
        <CompanyProfileEditor setMessage={setMessage} />
        <CompanyVerification setMessage={setMessage} />
        <div className="section-title"><h2>Proposals on your gigs</h2></div>
        {mine.filter((item) => item.kind === "Gig").map((item) => <GigProposalPanel key={item.id} opportunity={item} />)}
    </>;
}

function CompanyProfileEditor({ setMessage }) {
    const empty = { companyName: "", tagline: "", description: "", website: "", linkedinUrl: "", industry: "", headquarters: "", logoUrl: "", employeeCount: "" };
    const [profile, setProfile] = useState(empty); const [verified, setVerified] = useState(false); const [error, setError] = useState(""); const [importing, setImporting] = useState(false);
    useEffect(() => { platformRequest("/companies/profile/me").then((value) => { setProfile(value); setVerified(value.verified); }).catch(() => {}); }, []);
    async function save(event) { event.preventDefault(); setError(""); try { const result = await platformRequest("/companies/profile/me", { method: "PUT", body: JSON.stringify(profile) }); setProfile(result); setVerified(result.verified); setMessage("Company profile saved."); } catch (e) { setError(e.message); } }
    async function importFromWebsite() {
        if (!profile.website?.trim()) { setError("Enter the company website first."); return; }
        setImporting(true); setError("");
        try {
            const imported = await platformRequest("/companies/import", { method: "POST", body: JSON.stringify({ website: profile.website }) });
            const fields = ["companyName", "tagline", "description", "website", "linkedinUrl", "industry", "headquarters", "logoUrl", "employeeCount"];
            setProfile((current) => fields.reduce((next, field) => {
                if (!next[field]?.trim() && imported[field]) next[field] = imported[field];
                return next;
            }, { ...current }));
            setMessage("Imported public website details. Review them, then save the company profile.");
        } catch (e) { setError(e.message); }
        finally { setImporting(false); }
    }
    return <form className="panel post-form" onSubmit={save}><h2>Company profile {verified && <span className="status-pill">Verified</span>}</h2>{error && <div className="notice error">{error}</div>}
        <label>Company name<input required maxLength="160" value={profile.companyName || ""} onChange={(e) => setProfile({ ...profile, companyName: e.target.value })} /></label>
        <label>Tagline<input maxLength="180" value={profile.tagline || ""} onChange={(e) => setProfile({ ...profile, tagline: e.target.value })} /></label>
        <label>Industry<input maxLength="120" value={profile.industry || ""} onChange={(e) => setProfile({ ...profile, industry: e.target.value })} /></label>
        <label>Headquarters<input maxLength="120" value={profile.headquarters || ""} onChange={(e) => setProfile({ ...profile, headquarters: e.target.value })} /></label>
        <label>Company size<input maxLength="50" placeholder="e.g. 11–50 employees" value={profile.employeeCount || ""} onChange={(e) => setProfile({ ...profile, employeeCount: e.target.value })} /></label>
        <label>Website<input type="url" maxLength="500" value={profile.website || ""} onChange={(e) => setProfile({ ...profile, website: e.target.value })} /></label>
        <button type="button" className="button secondary" onClick={importFromWebsite} disabled={importing}>{importing ? "Importing…" : "Import details from website"}</button>
        <small>Imports public page title, description, logo, and any LinkedIn company link it publishes. Review the fields before saving.</small>
        <label>LinkedIn company page<input type="url" maxLength="500" value={profile.linkedinUrl || ""} onChange={(e) => setProfile({ ...profile, linkedinUrl: e.target.value })} placeholder="https://www.linkedin.com/company/..." /></label>
        <label>Logo URL<input type="url" maxLength="500" value={profile.logoUrl || ""} onChange={(e) => setProfile({ ...profile, logoUrl: e.target.value })} /></label>
        <label>About the company<textarea maxLength="3000" rows="4" value={profile.description || ""} onChange={(e) => setProfile({ ...profile, description: e.target.value })} /></label>
        <button className="button primary">Save company profile</button></form>;
}

function CompanyVerification({ setMessage }) {
    const [form, setForm] = useState({ companyName: "", registrationNumber: "", website: "", evidenceUrl: "" }); const [verification, setVerification] = useState(null); const [error, setError] = useState("");
    useEffect(() => { platformRequest("/companies/verification/me").then(setVerification).catch(() => {}); }, []);
    async function submit(event) { event.preventDefault(); try { const result = await platformRequest("/companies/verification", { method: "POST", body: JSON.stringify(form) }); setVerification(result); setMessage("Company verification request submitted."); setError(""); } catch (e) { setError(e.message); } }
    return <form className="panel post-form" onSubmit={submit}><h2>Employer verification</h2>{error && <div className="notice error">{error}</div>}{verification && <p className="status-pill">Status: {verification.status}{verification.reviewNote ? ` · ${verification.reviewNote}` : ""}</p>}<label>Company name<input required value={form.companyName || verification?.companyName || ""} onChange={(e) => setForm({ ...form, companyName: e.target.value })} /></label><label>Registration number<input value={form.registrationNumber} onChange={(e) => setForm({ ...form, registrationNumber: e.target.value })} /></label><label>Company website<input type="url" value={form.website} onChange={(e) => setForm({ ...form, website: e.target.value })} /></label><label>Evidence URL<input type="url" value={form.evidenceUrl} onChange={(e) => setForm({ ...form, evidenceUrl: e.target.value })} /></label><button className="button secondary">Submit verification</button></form>;
}
