import { useState } from "react";
import { platformRequest } from "../api/platform.js";
import ResumeMatchPanel from "../features/opportunities/ResumeMatchPanel.jsx";

export function PageHeading({ eyebrow, title, copy }) {
    return (
        <div className="page-heading">
            <p className="eyebrow">{eyebrow}</p>
            <h1>{title}</h1>
            <p className="muted">{copy}</p>
        </div>
    );
}

export function Stat({ label, value, icon }) {
    return <div className="stat-card"><span className="stat-icon">{icon}</span><span className="muted">{label}</span><strong>{value}</strong></div>;
}

export function ListingCard({ item, saved, applied, onSave, onApply }) {
    const [reportNotice, setReportNotice] = useState("");
    const [similar, setSimilar] = useState(null);
    const [companyProfile, setCompanyProfile] = useState(null);
    const [followingCompany, setFollowingCompany] = useState(false);
    async function report() {
        if (item.demo) return setReportNotice("Sample listings cannot be reported.");
        const reason = window.prompt("Why are you reporting this listing?");
        if (!reason?.trim()) return;
        try {
            await platformRequest("/reports", { method: "POST", body: JSON.stringify({ targetType: "LISTING", targetId: item.id, reason: reason.trim(), details: "" }) });
            setReportNotice("Report submitted for admin review.");
        } catch (e) { setReportNotice(e.message); }
    }
    async function showSimilar() {
        if (item.demo) return setReportNotice("Similar recommendations are available for published listings.");
        try { setSimilar(await platformRequest(`/opportunities/${item.id}/similar`)); } catch (e) { setReportNotice(e.message); }
    }
    async function showCompany() {
        try {
            const [profile, followedIds] = await Promise.all([
                platformRequest(`/companies/${item.employerId}/profile`),
                platformRequest("/companies/following"),
            ]);
            setCompanyProfile(profile);
            setFollowingCompany(followedIds.includes(item.employerId));
        } catch (e) { setReportNotice(e.message); }
    }
    async function toggleCompanyFollow() {
        try {
            await platformRequest(`/companies/${item.employerId}/follow`, {
                method: followingCompany ? "DELETE" : "PUT",
            });
            setFollowingCompany(!followingCompany);
        } catch (e) { setReportNotice(e.message); }
    }
    return (
        <article className="listing-card">
            <div className="company-mark">{item.company.slice(0, 1)}</div>
            <div className="listing-main">
                <div className="listing-title-row">
                    <div><span className="listing-kind">{item.kind}</span><h3>{item.title}</h3><p>{item.company}</p></div>
                    <button className={`save-button ${saved ? "is-saved" : ""}`} onClick={onSave} title="Save listing">{saved ? "★" : "☆"}</button>
                </div>
                <div className="listing-meta"><span>⌖ {item.location}</span>{Number.isFinite(item.distanceKm) && <span>{item.distanceKm.toFixed(1)} km away</span>}<span>◷ {item.type}</span><span>₹ {item.budget}</span>{Number(item.requiredExperienceYears) > 0 && <span>{item.requiredExperienceYears}+ years’ experience</span>}<span>{item.posted}</span></div>
                <p className="listing-description">{item.description}</p>
                <div className="listing-bottom">
                    <div className="tag-row">{item.skills.map((skill) => <span className="tag" key={skill}>{skill}</span>)}</div>
                    <button className="button primary small-button" onClick={onApply}>{applied ? "Applied" : item.kind === "Gig" ? "Send proposal" : "Apply"}</button>
                </div>
                <div className="listing-report">{item.employerId && <button className="text-button" onClick={showCompany}>Company profile</button>}<button className="text-button" onClick={showSimilar}>Similar roles</button><button className="text-button" onClick={report}>Report listing</button>{reportNotice && <small>{reportNotice}</small>}</div>
                {companyProfile && <div className="similar-list company-profile-card">
                    <div className="section-title"><strong>{companyProfile.companyName} {companyProfile.verified ? "✓ Verified" : ""}</strong>
                        <button className="button secondary small-button" onClick={toggleCompanyFollow}>{followingCompany ? "Following" : "Follow company"}</button>
                    </div>
                    {companyProfile.tagline && <span>{companyProfile.tagline}</span>}
                    {companyProfile.industry && <span>Industry: {companyProfile.industry}</span>}
                    {companyProfile.headquarters && <span>Headquarters: {companyProfile.headquarters}</span>}
                    {companyProfile.employeeCount && <span>Team size: {companyProfile.employeeCount}</span>}
                    {companyProfile.description && <p>{companyProfile.description}</p>}
                    {companyProfile.website && <a href={companyProfile.website} target="_blank" rel="noreferrer">Company website</a>}
                    {companyProfile.linkedinUrl && <a href={companyProfile.linkedinUrl} target="_blank" rel="noreferrer">LinkedIn company page</a>}
                </div>}
                {similar && <div className="similar-list"><strong>Similar opportunities</strong>{similar.map((match) => <span key={match.id}>{match.title} · {match.company}</span>)}{!similar.length && <small>No similar opportunities found.</small>}</div>}
                {!item.demo && <ResumeMatchPanel opportunity={item} />}
            </div>
        </article>
    );
}

export function EmptyState({ text }) {
    return <div className="empty-state"><span>✦</span><p>{text}</p></div>;
}

