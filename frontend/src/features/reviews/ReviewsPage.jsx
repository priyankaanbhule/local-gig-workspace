import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, Stat, EmptyState } from "../../components/WorkspaceUI.jsx";

export function ReviewsPage({ user }) {
    const [projects, setProjects] = useState([]); const [reviews, setReviews] = useState([]); const [reviewedProjectIds, setReviewedProjectIds] = useState([]); const [projectId, setProjectId] = useState(""); const [rating, setRating] = useState(5); const [body, setBody] = useState(""); const [notice, setNotice] = useState("");
    useEffect(() => {
        Promise.all([
            platformRequest("/projects/me"),
            platformRequest("/reviews/me/projects"),
            platformRequest(`/reviews/users/${user.id}`),
        ]).then(([allProjects, authored, received]) => {
            const completed = allProjects.filter((project) => project.status === "COMPLETED");
            const reviewedIds = authored.map((review) => review.projectId);
            const available = completed.filter((project) => !reviewedIds.includes(project.id));
            setProjects(available);
            setReviewedProjectIds(reviewedIds);
            setReviews(received);
            if (available.length) setProjectId(String(available[0].id));
        }).catch((e) => setNotice(e.message));
    }, [user]);
    async function submit(event) {
        event.preventDefault();
        try {
            await platformRequest("/reviews/projects", { method: "POST", body: JSON.stringify({ projectId: Number(projectId), rating, body }) });
            setNotice("Your review was saved."); setBody("");
            const remainingProjects = projects.filter((project) => project.id !== Number(projectId));
            setProjects(remainingProjects);
            setProjectId(String(remainingProjects[0]?.id || ""));
            setReviewedProjectIds((all) => [...all, Number(projectId)]);
            setReviews(await platformRequest(`/reviews/users/${user.id}`));
        } catch (e) { setNotice(e.message); }
    }
    const averageRating = reviews.length ? reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length : 0;
    return <><PageHeading eyebrow="TRUST AND FEEDBACK" title="Reviews" copy="Rate the other participant after a project is complete." />{notice && <div className="notice">{notice}</div>}{reviews.length > 0 && <div className="stat-grid"><Stat label="Average rating" value={`${averageRating.toFixed(1)} / 5`} icon="★"/><Stat label="Reviews received" value={reviews.length} icon="✦"/></div>}<form className="panel review-panel" onSubmit={submit}><h2>Leave a review</h2>{projects.length ? <><label>Completed project<select value={projectId} onChange={(e) => setProjectId(e.target.value)}>{projects.map((project) => <option key={project.id} value={project.id}>{project.title} · {(["EMPLOYER", "ADMIN"].includes(String(user.role).toUpperCase()) ? project.freelancerName : project.employerName)}</option>)}</select></label><label>Rating<select value={rating} onChange={(e) => setRating(Number(e.target.value))}>{[5,4,3,2,1].map((n) => <option value={n} key={n}>{n} stars</option>)}</select></label><label>Written feedback<textarea rows="3" value={body} onChange={(e) => setBody(e.target.value)} maxLength="2000" /></label><button className="button primary">Submit review</button></> : <EmptyState text={reviewedProjectIds.length ? "You have reviewed your completed projects." : "Reviews become available after a project is marked completed."} />}</form><div className="panel"><h2>Reviews about you</h2>{reviews.map((review) => <div className="notification-row" key={`${review.projectId || review.applicationId}-${review.id}`}><div><strong>{"★".repeat(review.rating)} · {review.reviewerName}</strong><p>{review.body}</p><small>{new Date(review.createdAt).toLocaleDateString()}</small></div></div>)}{!reviews.length && <EmptyState text="No reviews yet." />}</div></>;
}
