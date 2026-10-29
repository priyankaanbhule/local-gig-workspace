import { useEffect, useState } from "react";

const API = "/api/profiles/me";

const emptyProfile = {
    fullName: "",
    email: "",
    headline: "",
    summary: "",
    phone: "",
    city: "",
    country: "",
    skills: [],
    education: [],
    experience: [],
    projects: [],
    certifications: [],
    preferredRoles: [],
    preferredLocations: [],
    expectedSalary: "",
    portfolioUrl: "",
    resumeUrl: "",
};

function listToText(items = []) {
    return Array.isArray(items) ? items.join("\n") : "";
}

function textToList(text) {
    return text
        .split("\n")
        .map((item) => item.trim())
        .filter(Boolean);
}

async function apiRequest(options = {}) {
    const response = await fetch(API, {
        ...options,
        credentials: "include",
        headers: {
            "Content-Type": "application/json",
            ...options.headers,
        },
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
        throw new Error(
            data.message || data.detail || "Could not load or save your profile."
        );
    }

    return data;
}

export default function ProfileForm({ user, onLogout }) {
    const [profile, setProfile] = useState(emptyProfile);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    useEffect(() => {
        apiRequest()
            .then((data) => {
                setProfile({
                    ...emptyProfile,
                    ...data,
                    skills: listToText(data.skills),
                    education: listToText(data.education),
                    experience: listToText(data.experience),
                    projects: listToText(data.projects),
                    certifications: listToText(data.certifications),
                    preferredRoles: listToText(data.preferredRoles),
                    preferredLocations: listToText(data.preferredLocations),
                    expectedSalary: data.expectedSalary ?? "",
                });
            })
            .catch((requestError) => setError(requestError.message))
            .finally(() => setLoading(false));
    }, []);

    function updateField(event) {
        const { name, value } = event.target;
        setProfile((current) => ({ ...current, [name]: value }));
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setNotice("");
        setSaving(true);

        const payload = {
            headline: profile.headline,
            summary: profile.summary,
            phone: profile.phone,
            city: profile.city,
            country: profile.country,
            skills: textToList(profile.skills),
            education: textToList(profile.education),
            experience: textToList(profile.experience),
            projects: textToList(profile.projects),
            certifications: textToList(profile.certifications),
            preferredRoles: textToList(profile.preferredRoles),
            preferredLocations: textToList(profile.preferredLocations),
            expectedSalary: profile.expectedSalary
                ? Number(profile.expectedSalary)
                : null,
            portfolioUrl: profile.portfolioUrl,
            resumeUrl: profile.resumeUrl,
        };

        try {
            const savedProfile = await apiRequest({
                method: "PUT",
                body: JSON.stringify(payload),
            });

            setProfile((current) => ({
                ...current,
                ...savedProfile,
                skills: listToText(savedProfile.skills),
                education: listToText(savedProfile.education),
                experience: listToText(savedProfile.experience),
                projects: listToText(savedProfile.projects),
                certifications: listToText(savedProfile.certifications),
                preferredRoles: listToText(savedProfile.preferredRoles),
                preferredLocations: listToText(savedProfile.preferredLocations),
                expectedSalary: savedProfile.expectedSalary ?? "",
            }));

            setNotice("Your profile has been saved.");
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSaving(false);
        }
    }

    if (loading) {
        return <main className="loading-screen">Loading your profile…</main>;
    }

    return (
        <main className="profile-page">
            <header className="profile-topbar">
                <a className="brand" href="/">
                    <span className="brand-mark">L</span>
                    LocalGig
                </a>

                <button className="button button-dark" onClick={onLogout}>
                    Sign out
                </button>
            </header>

            <section className="profile-content">
                <div className="profile-heading">
                    <p className="eyebrow">YOUR LOCALGIG PROFILE</p>
                    <h1>Build your profile</h1>
                    <p>Add details to help employers and clients understand your experience.</p>
                </div>

                <form className="profile-card" onSubmit={handleSubmit}>
                    {error && <div className="message error-message">{error}</div>}
                    {notice && <div className="message success-message">{notice}</div>}

                    <div className="profile-account">
                        <strong>{profile.fullName || user?.fullName}</strong>
                        <span>{profile.email || user?.email}</span>
                    </div>

                    <label htmlFor="headline">Professional headline</label>
                    <input
                        id="headline"
                        name="headline"
                        value={profile.headline}
                        onChange={updateField}
                        maxLength="120"
                        placeholder="For example: Junior web developer"
                    />

                    <label htmlFor="summary">About you</label>
                    <textarea
                        id="summary"
                        name="summary"
                        value={profile.summary}
                        onChange={updateField}
                        maxLength="2000"
                        rows="4"
                        placeholder="Write a short introduction."
                    />

                    <div className="profile-grid">
                        <div>
                            <label htmlFor="phone">Phone</label>
                            <input
                                id="phone"
                                name="phone"
                                value={profile.phone}
                                onChange={updateField}
                                maxLength="30"
                            />
                        </div>
                        <div>
                            <label htmlFor="city">City</label>
                            <input
                                id="city"
                                name="city"
                                value={profile.city}
                                onChange={updateField}
                                maxLength="100"
                            />
                        </div>
                        <div>
                            <label htmlFor="country">Country</label>
                            <input
                                id="country"
                                name="country"
                                value={profile.country}
                                onChange={updateField}
                                maxLength="100"
                            />
                        </div>
                        <div>
                            <label htmlFor="expectedSalary">Expected salary</label>
                            <input
                                id="expectedSalary"
                                name="expectedSalary"
                                type="number"
                                min="0"
                                value={profile.expectedSalary}
                                onChange={updateField}
                                placeholder="Amount"
                            />
                        </div>
                    </div>

                    <label htmlFor="skills">Skills</label>
                    <textarea
                        id="skills"
                        name="skills"
                        value={profile.skills}
                        onChange={updateField}
                        rows="3"
                        placeholder={"One skill per line, for example:\nJava\nReact\nPostgreSQL"}
                    />

                    <label htmlFor="education">Education</label>
                    <textarea
                        id="education"
                        name="education"
                        value={profile.education}
                        onChange={updateField}
                        rows="3"
                        placeholder="One education entry per line"
                    />

                    <label htmlFor="experience">Work experience</label>
                    <textarea
                        id="experience"
                        name="experience"
                        value={profile.experience}
                        onChange={updateField}
                        rows="3"
                        placeholder="One experience entry per line"
                    />

                    <label htmlFor="projects">Projects</label>
                    <textarea
                        id="projects"
                        name="projects"
                        value={profile.projects}
                        onChange={updateField}
                        rows="3"
                        placeholder="One project per line"
                    />

                    <label htmlFor="certifications">Certifications</label>
                    <textarea
                        id="certifications"
                        name="certifications"
                        value={profile.certifications}
                        onChange={updateField}
                        rows="3"
                        placeholder="One certification per line"
                    />

                    <label htmlFor="preferredRoles">Preferred job roles</label>
                    <textarea
                        id="preferredRoles"
                        name="preferredRoles"
                        value={profile.preferredRoles}
                        onChange={updateField}
                        rows="2"
                        placeholder="One preferred role per line"
                    />

                    <label htmlFor="preferredLocations">Preferred locations</label>
                    <textarea
                        id="preferredLocations"
                        name="preferredLocations"
                        value={profile.preferredLocations}
                        onChange={updateField}
                        rows="2"
                        placeholder="One location per line"
                    />

                    <label htmlFor="portfolioUrl">Portfolio URL</label>
                    <input
                        id="portfolioUrl"
                        name="portfolioUrl"
                        type="url"
                        value={profile.portfolioUrl}
                        onChange={updateField}
                        maxLength="500"
                        placeholder="https://..."
                    />

                    <label htmlFor="resumeUrl">Resume URL</label>
                    <input
                        id="resumeUrl"
                        name="resumeUrl"
                        type="url"
                        value={profile.resumeUrl}
                        onChange={updateField}
                        maxLength="500"
                        placeholder="https://..."
                    />

                    <button className="button button-dark profile-save" disabled={saving}>
                        {saving ? "Saving…" : "Save profile"}
                    </button>
                </form>
            </section>
        </main>
    );
}
