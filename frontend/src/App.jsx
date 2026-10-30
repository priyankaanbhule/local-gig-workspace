import { useEffect, useMemo, useState } from "react";
import "./App.css";
import { platformRequest, uploadAuthenticatedFile, mapOpportunity, fileHref } from "./api/platform.js";
import { authRequest } from "./api/auth.js";
import RecommendationsPage from "./features/recommendations/RecommendationsPage.jsx";
import HelpPage from "./features/help/HelpPage.jsx";
import { PageHeading, Stat, ListingCard, EmptyState } from "./components/WorkspaceUI.jsx";
import { ProfilePage } from "./features/profiles/ProfilePage.jsx";
import { AccountSettings } from "./features/auth/AccountSettings.jsx";
import { JobAlertsPage } from "./features/alerts/JobAlertsPage.jsx";
import { CandidateSearchPage } from "./features/candidates/CandidateSearchPage.jsx";
import { MessagesPage } from "./features/messages/MessagesPage.jsx";
import { NotificationsPage } from "./features/notifications/NotificationsPage.jsx";
import { ReviewsPage } from "./features/reviews/ReviewsPage.jsx";
import { ProposalsPage, ProjectsPage } from "./features/projects/ProjectPages.jsx";
import { EmployerApplicationsPage, EmployerOverview, EmployerPage } from "./features/employer/EmployerPage.jsx";
import { ModerationPage } from "./features/admin/ModerationPage.jsx";
import { PaymentsPage } from "./features/payments/PaymentsPage.jsx";

const PROFILE_API = "/api/profiles/me";
const PLATFORM_API = "/api";

const starterListings = [
    {
        id: 1,
        title: "Junior Frontend Developer",
        company: "BrightPixel Studio",
        location: "Pune",
        category: "Technology",
        type: "Full-time",
        kind: "Job",
        budget: "₹4–6 LPA",
        posted: "Today",
        description: "Build responsive web experiences with a small product team.",
        skills: ["React", "JavaScript", "CSS"],
        status: "Active",
    },
    {
        id: 2,
        title: "Social Media Intern",
        company: "GreenLeaf Foods",
        location: "Mumbai",
        category: "Marketing",
        type: "Internship",
        kind: "Internship",
        budget: "₹12,000 / month",
        posted: "Yesterday",
        description: "Help plan content, create posts, and track campaign results.",
        skills: ["Writing", "Canva", "Social media"],
        status: "Active",
    },
    {
        id: 3,
        title: "Design a local café website",
        company: "Client: Bean & Bloom",
        location: "Remote",
        category: "Design",
        type: "Freelance",
        kind: "Gig",
        budget: "₹15,000 fixed",
        posted: "2 days ago",
        description: "Create a welcoming five-page website for a neighborhood café.",
        skills: ["Figma", "Web design", "HTML"],
        status: "Active",
    },
    {
        id: 4,
        title: "Part-time Customer Support",
        company: "CareDesk",
        location: "Pimpri-Chinchwad",
        category: "Customer Support",
        type: "Part-time",
        kind: "Job",
        budget: "₹18,000 / month",
        posted: "3 days ago",
        description: "Help customers over chat and email during evening hours.",
        skills: ["Communication", "Email", "Support"],
        status: "Active",
    },
];

function readSaved(key, fallback) {
    try {
        const value = localStorage.getItem(key);
        return value ? JSON.parse(value) : fallback;
    } catch {
        return fallback;
    }
}

function AuthScreen() {
    const [resetToken, setResetToken] = useState(() => {
        const hash = window.location.hash.slice(1).replace(/^reset\??/, "");
        return new URLSearchParams(hash).get("token") || "";
    });
    const [mode, setMode] = useState(() => resetToken ? "reset" : "login");
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");

    useEffect(() => {
        if (resetToken) window.history.replaceState({}, "", window.location.pathname);
    }, [resetToken]);

    async function handleSubmit(event) {
        event.preventDefault();
        const submittedForm = event.currentTarget;
        setBusy(true);
        setError("");
        setNotice("");

        const form = new FormData(submittedForm);
        const registering = mode === "register";
        const payload = registering
            ? {
                fullName: form.get("fullName"),
                email: form.get("email"),
                password: form.get("password"),
                role: form.get("role"),
            }
            : mode === "forgot"
                ? { email: form.get("email") }
                : mode === "reset"
                    ? { token: form.get("token"), newPassword: form.get("newPassword") }
                        : {
                email: form.get("email"),
                password: form.get("password"),
            };

        try {
            const endpoint = registering ? "/register" : mode === "forgot" ? "/forgot-password"
                : mode === "reset" ? "/reset-password" : "/login";
            await authRequest(endpoint, {
                method: "POST",
                body: JSON.stringify(payload),
            });

            if (registering) {
                setMode("login");
                setNotice("Account created. You can sign in now.");
                submittedForm.reset();
            } else if (mode === "forgot") {
                setNotice("If an account exists for that email, a password reset link has been sent. If email is not configured locally, check the Spring Boot console for the link.");
                submittedForm.reset();
            } else if (mode === "reset") {
                setMode("login");
                setResetToken("");
                setNotice("Password updated. Sign in with your new password.");
                submittedForm.reset();
            } else {
                window.location.reload();
            }
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setBusy(false);
        }
    }

    return (
        <main className="auth-page">
            <section className="auth-intro">
                <a className="brand" href="/">
                    <span className="brand-mark">L</span> LocalGig
                </a>
                <div>
                    <p className="eyebrow">GOOD WORK, CLOSER TO HOME</p>
                    <h1>
                        Find work that
                        <br />
                        <em>works for you.</em>
                    </h1>
                    <p className="muted">
                        Explore local jobs, internships, and flexible freelance gigs.
                    </p>
                </div>
                <p className="muted">Jobs · Internships · Freelance gigs</p>
            </section>

            <section className="auth-side">
                <form className="auth-card" onSubmit={handleSubmit}>
                    <p className="eyebrow">{mode === "login" ? "WELCOME BACK" : mode === "register" ? "GET STARTED" : "ACCOUNT HELP"}</p>
                    <h2>{mode === "login" ? "Sign in" : mode === "register" ? "Create your account" : mode === "forgot" ? "Reset password" : "Choose a new password"}</h2>
                    <p className="muted">
                        {mode === "login"
                            ? "Enter your account details to continue."
                            : mode === "register" ? "Join LocalGig and explore opportunities."
                                : mode === "forgot" ? "Enter your account email and we’ll send a secure reset link."
                                    : "Choose a new password for your account."}
                    </p>

                    {notice && <div className="notice success">{notice}</div>}
                    {error && <div className="notice error">{error}</div>}

                    {mode === "register" && (
                        <>
                            <label htmlFor="fullName">Full name</label>
                            <input id="fullName" name="fullName" autoComplete="name" required />
                        </>
                    )}

                    {mode !== "reset" && <><label htmlFor="email">Email address</label><input id="email" name="email" type="email" autoComplete="email" required /> </>}

                    {["login", "register"].includes(mode) && <><label htmlFor="password">Password</label><input
                        id="password"
                        name="password"
                        type="password"
                        minLength="8"
                        autoComplete={mode === "login" ? "current-password" : "new-password"}
                        required
                    /></>}

                    {mode === "reset" && <>
                        {resetToken ? <input type="hidden" name="token" value={resetToken} readOnly /> : <>
                            <label htmlFor="token">Reset token</label><input id="token" name="token" required />
                            <p className="muted small">Paste the reset token from the email, or from the backend console if local email is not configured.</p>
                        </>}
                        <label htmlFor="newPassword">New password</label>
                        <input id="newPassword" name="newPassword" type="password" minLength="8" autoComplete="new-password" required />
                    </>}

                    {mode === "register" && (
                        <>
                            <label htmlFor="role">I’m joining as</label>
                            <select id="role" name="role" defaultValue="JOB_SEEKER">
                                <option value="JOB_SEEKER">Job seeker</option>
                                <option value="EMPLOYER">Employer / recruiter</option>
                            </select>
                        </>
                    )}

                    <button className="button primary full" disabled={busy}>
                        {busy ? "Please wait…" : mode === "login" ? "Sign in" : mode === "register" ? "Create account" : mode === "forgot" ? "Send reset link" : "Update password"}
                    </button>

                    <p className="auth-switch">
                        {mode === "login" ? "New to LocalGig?" : mode === "register" ? "Already have an account?" : "Return to sign in"}{" "}
                        <button
                            className="text-button"
                            type="button"
                            onClick={() => {
                                setMode(mode === "login" ? "register" : "login");
                                setError("");
                                setNotice("");
                            }}
                        >
                            {mode === "login" ? "Create an account" : "Sign in"}
                        </button>
                    </p>
                    {mode === "login" && <div className="auth-extra"><button className="text-button" type="button" onClick={() => { setMode("forgot"); setNotice(""); setError(""); }}>Forgot password?</button></div>}
                    {mode === "forgot" && <p className="auth-switch">Have a reset token? <button className="text-button" type="button" onClick={() => { setMode("reset"); setNotice(""); }}>Enter token</button></p>}
                </form>
            </section>
        </main>
    );
}

function Workspace({ user, onLogout }) {
    const [page, setPage] = useState("Overview");
    const [unreadCount, setUnreadCount] = useState(0);
    const [directRecipient, setDirectRecipient] = useState(null);
    const [query, setQuery] = useState("");
    const [queryDraft, setQueryDraft] = useState("");
    const [location, setLocation] = useState("");
    const [locationDraft, setLocationDraft] = useState("");
    const [kind, setKind] = useState("All");
    const [type, setType] = useState("All");
    const [sort, setSort] = useState("newest");
    const [pageIndex, setPageIndex] = useState(0);
    const [pageTotal, setPageTotal] = useState(0);
    const [resultCount, setResultCount] = useState(0);
    const [nearbyRows, setNearbyRows] = useState(null);
    const [nearbyRadius, setNearbyRadius] = useState("25");
    const [listings, setListings] = useState(() =>
        readSaved("lg-listings", starterListings)
    );
    const [searchResults, setSearchResults] = useState([]);
    const [searchFailed, setSearchFailed] = useState(false);
    const [savedIds, setSavedIds] = useState(() => readSaved("lg-saved", []));
    const [savedListings, setSavedListings] = useState([]);
    const [applications, setApplications] = useState(() =>
        readSaved("lg-applications", [])
    );
    const [proposals, setProposals] = useState([]);
    const [profile, setProfile] = useState(null);
    const [message, setMessage] = useState("");
    const [applyTarget, setApplyTarget] = useState(null);
    const [coverLetter, setCoverLetter] = useState("");
    const [resumeUrl, setResumeUrl] = useState("");
    const [applyBusy, setApplyBusy] = useState(false);
    const [aiDraftBusy, setAiDraftBusy] = useState(false);
    const [applyError, setApplyError] = useState("");
    const [bidAmount, setBidAmount] = useState("");
    const [estimatedDays, setEstimatedDays] = useState("7");

    useEffect(() => {
        platformRequest("/profiles/me").then(setProfile).catch(() => {});
        platformRequest("/saved")
            .then((items) => { const mapped = items.map(mapOpportunity); setSavedListings(mapped); setSavedIds(mapped.map((item) => item.id)); })
            .catch(() => {});
        platformRequest("/applications/me")
            .then((items) => setApplications(items.map((item) => ({
                id: item.id,
                listingId: item.opportunityId,
                title: item.opportunityTitle,
                company: item.company,
                status: String(item.status).replaceAll("_", " ").toLowerCase(),
                createdAt: item.createdAt,
            }))))
            .catch(() => {});
        if (["JOB_SEEKER", "FREELANCER"].includes(String(user.role).toUpperCase())) {
            platformRequest("/proposals/me").then(setProposals).catch(() => {});
        }
    }, []);

    useEffect(() => {
        let alive = true;
        const refreshUnread = () => platformRequest("/notifications")
            .then((items) => { if (alive) setUnreadCount(items.filter((item) => !item.read).length); })
            .catch(() => {});
        refreshUnread();
        const timer = window.setInterval(refreshUnread, 15000);
        return () => { alive = false; window.clearInterval(timer); };
    }, []);

    useEffect(() => {
        const params = new URLSearchParams({
            q: query,
            location,
            kind: kind === "All" ? "" : kind.toUpperCase(),
            type: type === "All" ? "" : type,
            sort,
            page: String(pageIndex),
            size: "10",
        });
        platformRequest(`/opportunities/search?${params}`)
            .then((result) => {
                const pageListings = result.content.map(mapOpportunity);
                setSearchFailed(false);
                setSearchResults(pageListings);
                setResultCount(result.totalElements || 0);
                setListings((current) => {
                    const byId = new Map(current.filter((item) => !item.demo).map((item) => [item.id, item]));
                    pageListings.forEach((item) => byId.set(item.id, item));
                    return [...byId.values(), ...current.filter((item) => item.demo)];
                });
                setPageTotal(result.totalPages || 0);
            })
            .catch(() => {
                setSearchFailed(true);
                setSearchResults([]);
                setResultCount(0);
                setListings((current) => current.some((item) => item.demo)
                    ? current
                    : starterListings.map((item) => ({ ...item, demo: true })));
                setPageTotal(0);
            });
    }, [query, location, kind, type, sort, pageIndex]);

    useEffect(() => localStorage.setItem("lg-listings", JSON.stringify(listings)), [listings]);
    useEffect(() => localStorage.setItem("lg-saved", JSON.stringify(savedIds)), [savedIds]);
    useEffect(
        () => localStorage.setItem("lg-applications", JSON.stringify(applications)),
        [applications]
    );

    const filteredListings = useMemo(() => {
        const search = query.trim().toLowerCase();
        const source = searchFailed ? listings.filter((item) => item.demo) : searchResults;
        return source.filter((item) => {
            const matchesText =
                !search ||
                [item.title, item.company, item.location, item.category, item.description, ...item.skills]
                    .join(" ")
                    .toLowerCase()
                    .includes(search);
            const matchesLocation =
                !location || item.location.toLowerCase().includes(location.toLowerCase());
            const matchesKind = kind === "All" || item.kind === kind;
            const matchesType = type === "All" || item.type?.toLowerCase() === type.toLowerCase();
            return matchesText && matchesLocation && matchesKind && matchesType && item.status === "Active";
        });
    }, [searchResults, listings, query, location, kind, type, pageTotal]);

    async function toggleSaved(id) {
        const item = listings.find((listing) => listing.id === id);
        const removing = savedIds.includes(id);
        try {
            if (!item?.demo) {
                await platformRequest(`/saved/${id}`, { method: removing ? "DELETE" : "PUT" });
            }
            setSavedIds((current) => removing
                ? current.filter((savedId) => savedId !== id)
                : [...current, id]);
            setSavedListings((current) => removing
                ? current.filter((saved) => saved.id !== id)
                : item && !current.some((saved) => saved.id === id) ? [item, ...current] : current);
        } catch (requestError) {
            setMessage(requestError.message);
        }
    }

    function applyToListing(item) {
        if (applications.some((application) => application.listingId === item.id)
            || proposals.some((proposal) => proposal.opportunityId === item.id)) {
            setMessage("You have already applied to this opportunity.");
            return;
        }
        if (item.demo) {
            setMessage("This is a sample listing. An employer must post it before you can apply.");
            return;
        }
        setApplyTarget(item);
        setCoverLetter("");
        setResumeUrl(profile?.resumeUrl || "");
        setBidAmount("");
        setEstimatedDays("7");
        setApplyError("");
    }

    async function submitApplication(event) {
        event.preventDefault();
        if (!applyTarget) return;
        setApplyBusy(true);
        setApplyError("");
        try {
            if (applyTarget.kind === "Gig") {
                const proposal = await platformRequest(`/opportunities/${applyTarget.id}/proposals`, {
                    method: "POST",
                    body: JSON.stringify({ coverLetter, bidAmount: Number(bidAmount), estimatedDays: Number(estimatedDays) }),
                });
                setProposals((current) => [proposal, ...current]);
                setMessage("Your proposal was submitted to the client.");
            } else {
                const application = await platformRequest(`/opportunities/${applyTarget.id}/applications`, {
                    method: "POST",
                    body: JSON.stringify({ coverLetter, resumeUrl }),
                });
                setApplications((current) => [{
                    id: application.id,
                    listingId: application.opportunityId,
                    title: application.opportunityTitle,
                    company: application.company,
                    status: "submitted",
                }, ...current]);
                setMessage("Your application was submitted.");
            }
            setApplyTarget(null);
        } catch (requestError) {
            setApplyError(requestError.message);
        } finally {
            setApplyBusy(false);
        }
    }

    async function draftWithAi() {
        if (!applyTarget) return;
        setAiDraftBusy(true);
        setApplyError("");
        try {
            const result = await platformRequest("/ai/cover-letter", {
                method: "POST",
                body: JSON.stringify({ opportunityId: applyTarget.id }),
            });
            setCoverLetter(result.draft);
        } catch (requestError) {
            setApplyError(requestError.message);
        } finally {
            setAiDraftBusy(false);
        }
    }

    async function withdrawApplication(application) {
        try {
            await platformRequest(`/applications/${application.id}`, { method: "DELETE" });
            setApplications((current) => current.filter((item) => item.id !== application.id));
            setMessage("Application withdrawn.");
        } catch (requestError) {
            setMessage(requestError.message);
        }
    }

    function searchNearby() {
        if (!navigator.geolocation) return setMessage("Location search is unavailable in this browser.");
        navigator.geolocation.getCurrentPosition(async ({ coords }) => {
            try {
                const matches = await platformRequest(`/opportunities/nearby?latitude=${coords.latitude}&longitude=${coords.longitude}&radiusKm=${nearbyRadius}`);
                setNearbyRows(matches);
                setMessage(`${matches.length} opportunities found within ${nearbyRadius} km.`);
            } catch (requestError) { setMessage(requestError.message); }
        }, () => setMessage("Allow location access in your browser to search nearby opportunities."));
    }

    function chooseWorkType(value) {
        setNearbyRows(null);
        setPageIndex(0);
        if (value === "Internship") {
            setKind("Internship");
            setType("Internship");
        } else if (value === "Freelance") {
            setKind("Gig");
            setType("Freelance");
        } else {
            setKind("All");
            setType(value);
        }
    }

    const storedRole = String(user.role || "JOB_SEEKER").toUpperCase();
    const role = storedRole === "FREELANCER" ? "JOB_SEEKER" : storedRole;
    const seekerNav = [
        ["Overview", "⌂"],
        ["Find work", "⌕"],
        ["Recommended", "✦"],
        ["My applications", "▤"],
        ["My proposals", "✉"],
        ["My projects", "▣"],
        ["Saved", "☆"],
        ["Job alerts", "♧"],
        ["My profile", "◉"],
        ["Account settings", "⚙"],
        ["Messages", "✉"],
        ["Notifications", "♧"],
        ["Reviews", "★"],
        ["Help", "?"],
    ];
    const recruiterNav = [
        ["Overview", "⌂"], ["Review applications", "✓"], ["Employer tools", "▣"], ["Find candidates", "♙"],
        ["Messages", "✉"], ["Notifications", "♧"], ["Reviews", "★"],
        ["My profile", "◉"], ["Account settings", "⚙"], ["Help", "?"],
    ];
    const nav = (role === "EMPLOYER" ? recruiterNav : seekerNav)
        .filter(([name]) => name !== "Recommended" || ["JOB_SEEKER", "FREELANCER", "ADMIN"].includes(role))
        .filter(([name]) => name !== "My proposals" || ["JOB_SEEKER", "FREELANCER", "ADMIN"].includes(role))
        .filter(([name]) => name !== "My projects" || ["JOB_SEEKER", "FREELANCER", "EMPLOYER", "ADMIN"].includes(role));
    if (role === "ADMIN") nav.push(["Find candidates", "♙"], ["Employer tools", "▣"], ["Admin moderation", "⚑"], ["Reports & payments", "₹"]);

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <a className="brand sidebar-brand" href="#">
                    <span className="brand-mark">L</span> LocalGig
                </a>
                <p className="sidebar-label">WORKSPACE</p>
                <nav>
                    {nav.map(([name, icon]) => (
                        <button
                            key={name}
                            className={`nav-link ${page === name ? "active" : ""}`}
                            onClick={() => {
                                setPage(name);
                                setMessage("");
                            }}
                        >
                            <span>{icon}</span>{name}
                            {name === "Notifications" && unreadCount > 0 && <span className="notification-count">{unreadCount > 99 ? "99+" : unreadCount}</span>}
                        </button>
                    ))}
                </nav>
                <div className="sidebar-user">
                    <div className="avatar">{(user.fullName || "U").slice(0, 1).toUpperCase()}</div>
                    <div className="sidebar-user-copy">
                        <strong>{user.fullName}</strong>
                        <span>{role.replaceAll("_", " ")}</span>
                    </div>
                    <button className="icon-button" title="Sign out" onClick={onLogout}>↪</button>
                </div>
            </aside>

            <main className="main-area">
                <header className="topbar">
                    <div>
                        <span className="muted small">LOCALGIG WORKSPACE</span>
                        <strong>{page}</strong>
                    </div>
                    <div className="topbar-right">
                        <span className="status-pill"><i /> Platform demo</span>
                        <button className="avatar small-avatar" onClick={() => setPage("My profile")}>
                            {(user.fullName || "U").slice(0, 1).toUpperCase()}
                        </button>
                    </div>
                </header>

                <section className="page-content">
                    {message && (
                        <div className="notice success dismissible">
                            {message}
                            <button onClick={() => setMessage("")}>×</button>
                        </div>
                    )}

                    {page === "Overview" && role === "EMPLOYER" && <EmployerOverview user={user} onNavigate={setPage} setMessage={setMessage} />}
                    {page === "Overview" && role !== "EMPLOYER" && (
                        <Overview
                            user={user}
                            listings={filteredListings}
                            applications={applications}
                            proposals={proposals}
                            savedIds={savedIds}
                            profile={profile}
                            onNavigate={setPage}
                            onSave={toggleSaved}
                            onApply={applyToListing}
                        />
                    )}

            {page === "Find work" && (
                        <>
                            <section className="discovery-search-card" aria-label="Search opportunities">
                                <form className="discovery-search-form" onSubmit={(event) => {
                                    event.preventDefault();
                                    setQuery(queryDraft.trim());
                                    setLocation(locationDraft.trim());
                                    setPageIndex(0);
                                    setNearbyRows(null);
                                }}>
                                    <label className="discovery-field">
                                        <span className="field-icon" aria-hidden="true">⌕</span>
                                        <span className="field-copy"><input aria-label="Search by job title, skill, or company" placeholder="Job title, skill, or company" value={queryDraft} onChange={(event) => setQueryDraft(event.target.value)} /></span>
                                    </label>
                                    <label className="discovery-field">
                                        <span className="field-icon" aria-hidden="true">⌖</span>
                                        <span className="field-copy"><input aria-label="Search by location" placeholder="City or area" value={locationDraft} onChange={(event) => setLocationDraft(event.target.value)} /></span>
                                    </label>
                                    <label className="discovery-field work-type-field">
                                        <span className="field-icon" aria-hidden="true">▦</span>
                                        <span className="field-copy"><select aria-label="Filter by work type" value={type} onChange={(event) => chooseWorkType(event.target.value)}><option value="All">Any work type</option><option value="Full-time">Full-time</option><option value="Part-time">Part-time</option><option value="Remote">Remote</option><option value="Internship">Internship</option><option value="Freelance">Freelance gig</option></select></span>
                                    </label>
                                    <button className="button primary discovery-submit" type="submit"><span aria-hidden="true">⌕</span> Search</button>
                                </form>
                                <div className="search-tools-row">
                                    <strong>{nearbyRows ? nearbyRows.length : resultCount} <span>matches</span></strong>
                                    <div className="search-tools-actions">
                                        <select aria-label="Sort results" title="Sort results" value={sort} onChange={(event) => { setSort(event.target.value); setPageIndex(0); }}><option value="newest">Newest</option><option value="relevance">Relevant</option><option value="salary">Salary / budget</option></select>
                                        <select aria-label="Nearby search distance" title="Nearby search distance" value={nearbyRadius} onChange={(event) => setNearbyRadius(event.target.value)}><option value="5">5 km</option><option value="10">10 km</option><option value="25">25 km</option><option value="50">50 km</option><option value="100">100 km</option><option value="250">250 km</option></select>
                                        <button type="button" className="nearby-button" onClick={searchNearby}><span aria-hidden="true">◎</span> Near me</button>
                                        {(query || location || type !== "All" || nearbyRows) && <button type="button" className="clear-search-button" onClick={() => {
                                            setQuery(""); setQueryDraft(""); setLocation(""); setLocationDraft("");
                                            setKind("All"); setType("All"); setSort("newest"); setPageIndex(0); setNearbyRows(null);
                                        }}>Clear search</button>}
                                    </div>
                                </div>
                            </section>
                            <div className="listing-list">
                                {(nearbyRows ? nearbyRows.map((row) => ({ ...mapOpportunity(row.opportunity), distanceKm: row.distanceKm })) : filteredListings).map((item) => (
                                    <ListingCard
                                        key={item.id}
                                        item={item}
                                        saved={savedIds.includes(item.id)}
                                        applied={applications.some((application) => application.listingId === item.id) || proposals.some((proposal) => proposal.opportunityId === item.id)}
                                        onSave={() => toggleSaved(item.id)}
                                        onApply={() => applyToListing(item)}
                                    />
                                ))}
                                {!(nearbyRows ? nearbyRows.length : filteredListings.length) && <EmptyState text="No matching opportunities. Try another search." />}
                            </div>
                            {pageTotal > 1 && <div className="pagination"><button className="button secondary" disabled={pageIndex === 0} onClick={() => setPageIndex((p) => p - 1)}>Previous</button><span>Page {pageIndex + 1} of {pageTotal}</span><button className="button secondary" disabled={pageIndex + 1 >= pageTotal} onClick={() => setPageIndex((p) => p + 1)}>Next</button></div>}
                        </>
                    )}

                    {page === "My applications" && (
                        <>
                            <PageHeading eyebrow="YOUR ACTIVITY" title="My applications" copy="Track the opportunities you have applied to." />
                            <div className="panel">
                                {applications.length ? applications.map((application) => (
                                    <div className="table-row" key={application.listingId}>
                                        <div><strong>{application.title}</strong><span>{application.company}</span></div>
                                        <span className="status-pill">{application.status}</span>
                                        {application.status !== "withdrawn" && application.status !== "completed" && application.status !== "rejected" && <button className="button secondary small-button" onClick={() => withdrawApplication(application)}>Withdraw</button>}
                                    </div>
                                )) : <EmptyState text="You haven’t applied to anything yet. Browse opportunities to get started." />}
                            </div>
                        </>
                    )}

                    {page === "My proposals" && <ProposalsPage proposals={proposals} setProposals={setProposals} />}
                    {page === "My projects" && <ProjectsPage user={user} />}

                    {page === "Saved" && (
                        <>
                            <PageHeading eyebrow="BOOKMARKS" title="Saved opportunities" copy="Listings you bookmarked for later." />
                            <div className="listing-list">
                                {savedListings.filter((item) => savedIds.includes(item.id)).map((item) => (
                                    <ListingCard key={item.id} item={item} saved onSave={() => toggleSaved(item.id)}
                                                 applied={applications.some((a) => a.listingId === item.id) || proposals.some((proposal) => proposal.opportunityId === item.id)}
                                                 onApply={() => applyToListing(item)} />
                                ))}
                                {!savedIds.length && <EmptyState text="Your saved listings will appear here." />}
                            </div>
                        </>
                    )}

                    {page === "Job alerts" && <JobAlertsPage />}
                    {page === "Find candidates" && <CandidateSearchPage onMessage={(person) => { setDirectRecipient({ userId: person.userId, fullName: person.fullName }); setPage("Messages"); }} />}
                    {page === "Recommended" && <RecommendationsPage onApply={applyToListing} savedIds={savedIds} onSave={toggleSaved} applications={applications} proposals={proposals} />}
                    {page === "Help" && <HelpPage />}

                    {page === "My profile" && (
                        <ProfilePage user={user} profile={profile} setProfile={setProfile} onSessionExpired={onLogout} />
                    )}
                    {page === "Account settings" && <AccountSettings onLogout={onLogout} />}

                    {page === "Messages" && <MessagesPage user={user} directRecipient={directRecipient} onDirectRecipientHandled={() => setDirectRecipient(null)} />}
                    {page === "Notifications" && <NotificationsPage user={user} onUnreadChange={setUnreadCount} onNavigate={setPage} />}
                    {page === "Reviews" && <ReviewsPage user={user} />}
                    {page === "Employer tools" && (
                        <EmployerPage listings={listings} setListings={setListings} user={user} setMessage={setMessage} />
                    )}
                    {page === "Review applications" && <EmployerApplicationsPage setMessage={setMessage} onMessage={(application) => { setDirectRecipient({ userId: application.applicantId, fullName: application.applicantName }); setPage("Messages"); }} />}
                    {page === "Admin moderation" && <ModerationPage />}
                    {page === "Reports & payments" && <PaymentsPage user={user} />}
                </section>
            </main>
            {applyTarget && <div className="dialog-backdrop" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget && !applyBusy) setApplyTarget(null); }}>
                <form className="application-dialog" role="dialog" aria-modal="true" aria-labelledby="application-dialog-title" onSubmit={submitApplication}>
                    <button className="dialog-close" type="button" aria-label="Close" disabled={applyBusy} onClick={() => setApplyTarget(null)}>×</button>
                    <p className="eyebrow">{applyTarget.kind === "Gig" ? "FREELANCER PROPOSAL" : "APPLICATION"}</p>
                    <h2 id="application-dialog-title">{applyTarget.kind === "Gig" ? `Propose for ${applyTarget.title}` : `Apply to ${applyTarget.title}`}</h2>
                    <p className="muted">{applyTarget.company} · {applyTarget.location}</p>
                    {applyError && <div className="notice error">{applyError}</div>}
                    {!applyTarget.demo && ["JOB_SEEKER", "FREELANCER"].includes(String(user.role).toUpperCase()) && <div className="ai-draft-tools">
                        <button className="button secondary small-button" type="button" onClick={draftWithAi} disabled={aiDraftBusy || applyBusy}>{aiDraftBusy ? "Drafting…" : "Draft with AI"}</button>
                        <small>Shares your profile summary and this listing with OpenAI to create a draft. Review it before submitting.</small>
                    </div>}
                    <label>{applyTarget.kind === "Gig" ? "Proposal message" : "Cover letter"}<textarea value={coverLetter} onChange={(event) => setCoverLetter(event.target.value)} rows="5" maxLength="3000" placeholder={applyTarget.kind === "Gig" ? "Explain how you will deliver this project." : "Briefly tell the employer why you are a good fit."} /></label>
                    {applyTarget.kind === "Gig" ? <div className="form-grid"><label>Your bid (₹)<input type="number" min="0.01" step="0.01" required value={bidAmount} onChange={(event) => setBidAmount(event.target.value)} /></label><label>Estimated days<input type="number" min="1" max="365" required value={estimatedDays} onChange={(event) => setEstimatedDays(event.target.value)} /></label></div> : <>
                    <label>Resume link (optional)<input type="url" value={resumeUrl} onChange={(event) => setResumeUrl(event.target.value)} placeholder="Paste a resume URL or upload a file below" /></label>
                    <label>Upload resume (PDF, DOC, DOCX; up to 5 MB)<input type="file" accept=".pdf,.doc,.docx" disabled={applyBusy} onChange={async (event) => {
                        const file = event.target.files?.[0];
                        if (!file) return;
                        setApplyError("");
                        try {
                            const uploaded = await uploadAuthenticatedFile(file, "RESUME");
                            setResumeUrl(uploaded.url);
                        } catch (error) {
                            setApplyError(error.message);
                        } finally {
                            event.target.value = "";
                        }
                    }} /></label>
                    </>}
                    <div className="dialog-actions"><button className="button secondary" type="button" disabled={applyBusy} onClick={() => setApplyTarget(null)}>Cancel</button><button className="button primary" disabled={applyBusy}>{applyBusy ? "Submitting…" : applyTarget.kind === "Gig" ? "Submit proposal" : "Submit application"}</button></div>
                </form>
            </div>}
        </div>
    );
}

function Overview({ user, listings, applications, proposals, savedIds, profile, onNavigate, onSave, onApply }) {
    const profileFields = profile ? [profile.headline, profile.city, profile.summary, profile.skills?.length, profile.education?.length, profile.experience?.length, profile.resumeUrl].filter(Boolean).length : 0;
    const profilePercent = profile ? Math.round((profileFields / 7) * 100) : 0;
    const upcoming = listings.filter((item) => item.deadline && new Date(`${item.deadline}T23:59:59`) >= new Date()).sort((a, b) => a.deadline.localeCompare(b.deadline)).slice(0, 3);
    const recentApplications = [...applications].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)).slice(0, 3);
    return (
        <>
            <div className="welcome-banner">
                <div>
                    <p className="eyebrow">YOUR NEXT STEP STARTS HERE</p>
                    <h1>Good to see you, {user.fullName?.split(" ")[0] || "there"}.</h1>
                    <p>Explore local work and keep your search moving.</p>
                </div>
                <button className="button light-button" onClick={() => onNavigate("Find work")}>Explore work <span>→</span></button>
            </div>

            <div className="stat-grid">
                <Stat label="Applications" value={applications.length} icon="▤" />
                <Stat label="Saved listings" value={savedIds.length} icon="☆" />
                <Stat label="New opportunities" value={listings.length} icon="✦" />
                <Stat label="Profile complete" value={`${profilePercent}%`} icon="◉" />
            </div>

            <div className="section-title">
                <div><p className="eyebrow">PICKED FOR YOU</p><h2>Recent opportunities</h2></div>
                <button className="text-button" onClick={() => onNavigate("Find work")}>Browse all →</button>
            </div>
            <div className="listing-list">
                {listings.slice(0, 3).map((item) => (
                    <ListingCard key={item.id} item={item} saved={savedIds.includes(item.id)}
                                 applied={applications.some((a) => a.listingId === item.id) || proposals.some((proposal) => proposal.opportunityId === item.id)}
                                 onSave={() => onSave(item.id)} onApply={() => onApply(item)} />
                ))}
            </div>
            <div className="dashboard-columns">
                <section className="panel"><div className="section-title"><h2>Upcoming deadlines</h2><button className="text-button" onClick={() => onNavigate("Find work")}>Browse work →</button></div>{upcoming.length ? upcoming.map((item) => <div className="table-row" key={item.id}><div><strong>{item.title}</strong><span>{item.company} · Apply by {new Date(`${item.deadline}T00:00:00`).toLocaleDateString()}</span></div></div>) : <EmptyState text="No upcoming listing deadlines on this page." />}</section>
                <section className="panel"><div className="section-title"><h2>Recent activity</h2><button className="text-button" onClick={() => onNavigate("My applications")}>Applications →</button></div>{recentApplications.length ? recentApplications.map((item) => <div className="table-row" key={item.listingId}><div><strong>{item.title}</strong><span>{item.company} · {item.status}</span></div></div>) : <EmptyState text="Your applications and status updates will appear here." />}</section>
            </div>
        </>
    );
}

export default function App() {
    const [user, setUser] = useState(null);
    const [checking, setChecking] = useState(true);

    useEffect(() => {
        authRequest("/me")
            .then(setUser)
            .catch(() => setUser(null))
            .finally(() => setChecking(false));
    }, []);

    async function logout() {
        try {
            await authRequest("/logout", { method: "POST" });
        } catch {
            // Sign out locally if the backend session has already ended.
        }
        setUser(null);
    }

    if (checking) return <main className="loading-screen">Loading LocalGig…</main>;
    return user ? <Workspace user={user} onLogout={logout} /> : <AuthScreen />;
}

