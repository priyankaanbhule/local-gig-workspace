import { useEffect, useState } from "react";
import { uploadAuthenticatedFile, fileHref } from "../../api/platform.js";
import { authRequest } from "../../api/auth.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";
const PROFILE_API = "/api/profiles/me";

export function ProfilePage({ user, profile, setProfile, onSessionExpired }) {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [loadAttempt, setLoadAttempt] = useState(0);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setError("");
        fetch(PROFILE_API, { credentials: "include" })
            .then(async (response) => {
                if (!response.ok) {
                    const body = await response.json().catch(() => ({}));
                    const requestError = new Error(body.detail || body.message || `Could not load profile (HTTP ${response.status}).`);
                    requestError.status = response.status;
                    throw requestError;
                }
                return response.json();
            })
            .then((data) => {
                if (!active) return;
                setProfile({
                    ...data,
                    skills: (data.skills || []).join(", "),
                    education: (data.education || []).join("\n"),
                    experience: (data.experience || []).join("\n"),
                    projects: (data.projects || []).join("\n"),
                    certifications: (data.certifications || []).join("\n"),
                    preferredRoles: (data.preferredRoles || []).join(", "),
                    preferredLocations: (data.preferredLocations || []).join(", "),
                    expectedSalary: data.expectedSalary ?? "",
                });
            })
            .catch(async (requestError) => {
                if (!active) return;
                if (requestError.status === 401 || requestError.status === 403) {
                    try {
                        await authRequest("/me");
                        if (active) setError(`Your session is active, but the profile request was denied (HTTP ${requestError.status}). Retry once; if it persists, check the backend run console for the /api/profiles/me request.`);
                    } catch {
                        if (active) {
                            setError("Your session expired. Sign in again to continue.");
                            onSessionExpired();
                        }
                    }
                    return;
                }
                setError(requestError instanceof TypeError
                    ? "Could not reach the profile API. Check that the backend is running on port 8081."
                    : requestError.message);
            })
            .finally(() => { if (active) setLoading(false); });
        return () => { active = false; };
    }, [setProfile, loadAttempt]);

    function update(event) {
        setProfile((current) => ({ ...current, [event.target.name]: event.target.value }));
    }

    async function save(event) {
        event.preventDefault();
        setError("");
        setNotice("");
        setSaving(true);
        const list = (value) => String(value || "").split(/[,\\n]/).map((item) => item.trim()).filter(Boolean);
        const payload = {
            headline: profile.headline || "",
            summary: profile.summary || "",
            phone: profile.phone || "",
            city: profile.city || "",
            country: profile.country || "",
            skills: list(profile.skills),
            education: list(profile.education),
            experience: list(profile.experience),
            projects: list(profile.projects),
            certifications: list(profile.certifications),
            preferredRoles: list(profile.preferredRoles),
            preferredLocations: list(profile.preferredLocations),
            expectedSalary: profile.expectedSalary ? Number(profile.expectedSalary) : null,
            portfolioUrl: profile.portfolioUrl || "",
            resumeUrl: profile.resumeUrl || "",
            profilePictureUrl: profile.profilePictureUrl || "",
            available: profile.available !== false,
        };

        try {
            const response = await fetch(PROFILE_API, {
                method: "PUT",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            if (!response.ok) {
                const body = await response.json().catch(() => ({}));
                throw new Error(body.detail || body.message || `Profile could not be saved (HTTP ${response.status}).`);
            }
            setNotice("Profile saved.");
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSaving(false);
        }
    }

    async function uploadFile(event, category, field) {
        const file = event.target.files?.[0];
        if (!file) return;
        setError(""); setNotice("");
        try {
            const uploaded = await uploadAuthenticatedFile(file, category);
            setProfile((current) => ({ ...current, [field]: uploaded.url }));
            setNotice(`${category === "RESUME" ? "Resume" : "Profile picture"} uploaded. Save your profile to keep the file link.`);
        } catch (requestError) { setError(requestError.message); }
        event.target.value = "";
    }

    if (loading) return <div className="panel">Loading profile…</div>;
    if (!profile) return (
        <>
            <PageHeading eyebrow="YOUR PROFILE" title="My profile" copy="Your profile could not be loaded." />
            <div className="panel post-form">
                <div className="notice error">{error || "Profile information is unavailable."}</div>
                <button className="button primary" onClick={() => setLoadAttempt((attempt) => attempt + 1)}>Retry profile load</button>
            </div>
        </>
    );

    const fields = [
        ["phone", "Phone"], ["city", "City"], ["country", "Country"],
        ["expectedSalary", "Expected salary"],
    ];

    return (
        <>
            <PageHeading eyebrow="YOUR PROFILE" title="Build your profile" copy="Help employers and clients see what you can do." />
            <form className="panel profile-form" onSubmit={save}>
                <div className="profile-identity"><div className="avatar">{profile?.profilePictureUrl ? <img src={fileHref(profile.profilePictureUrl)} crossOrigin="use-credentials" alt="Profile" /> : user.fullName?.[0] || "U"}</div><div><strong>{user.fullName}</strong><span>{user.email}</span></div></div>
                {error && <div className="notice error">{error}</div>}
                {notice && <div className="notice success">{notice}</div>}
                <label>Professional headline<input name="headline" value={profile?.headline || ""} onChange={update} maxLength="120" placeholder="Junior web developer" /></label>
                <label>About you<textarea name="summary" value={profile?.summary || ""} onChange={update} rows="4" maxLength="2000" /></label>
                <div className="form-grid">
                    {fields.map(([name, label]) => <label key={name}>{label}<input name={name} type={name === "expectedSalary" ? "number" : "text"} min={name === "expectedSalary" ? "0" : undefined} value={profile?.[name] ?? ""} onChange={update} /></label>)}
                </div>
                {[
                    ["skills", "Skills (comma separated)"], ["education", "Education (one item per line)"],
                    ["experience", "Work experience (one item per line)"], ["projects", "Projects (one item per line)"],
                    ["certifications", "Certifications (one item per line)"], ["preferredRoles", "Preferred roles"],
                    ["preferredLocations", "Preferred locations"],
                ].map(([name, label]) => <label key={name}>{label}<textarea name={name} value={profile?.[name] || ""} onChange={update} rows="2" /></label>)}
                <label>Portfolio URL<input name="portfolioUrl" type="url" value={profile?.portfolioUrl || ""} onChange={update} placeholder="https://..." /></label>
                <label>Resume URL<input name="resumeUrl" type="url" value={profile?.resumeUrl || ""} onChange={update} placeholder="https://..." /></label>
                <label>Upload resume (PDF, DOC, DOCX; up to 5 MB)<input type="file" accept=".pdf,.doc,.docx" onChange={(event) => uploadFile(event, "RESUME", "resumeUrl")} /></label>
                {profile?.resumeUrl && <a className="text-button" href={fileHref(profile.resumeUrl)} target="_blank" rel="noreferrer">Open uploaded resume</a>}
                <label>Upload profile picture (PNG, JPG, WEBP; up to 2 MB)<input type="file" accept=".png,.jpg,.jpeg,.webp" onChange={(event) => uploadFile(event, "PROFILE_IMAGE", "profilePictureUrl")} /></label>
                <label className="checkbox-label"><input name="available" type="checkbox" checked={profile?.available !== false} onChange={(event) => setProfile((current) => ({ ...current, available: event.target.checked }))} /> Available for opportunities</label>
                <button className="button primary" disabled={saving}>{saving ? "Saving…" : "Save profile"}</button>
            </form>
        </>
    );
}
