import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, Stat, EmptyState } from "../../components/WorkspaceUI.jsx";

export function PaymentsPage({ user }) {
    const [items, setItems] = useState([]); const [completed, setCompleted] = useState([]); const [projectId, setProjectId] = useState(""); const [amount, setAmount] = useState(""); const [error, setError] = useState("");
    const refresh = async () => {
        try {
            const records = await platformRequest("/payments"); setItems(records);
            if (["EMPLOYER", "ADMIN"].includes(String(user.role).toUpperCase())) {
                const projects = await platformRequest("/projects/me");
                const eligible = projects.filter((project) => project.status === "COMPLETED" && String(project.opportunityKind).toUpperCase() === "GIG" && !records.some((record) => record.projectId === project.id));
                setCompleted(eligible); if (eligible.length && !eligible.some((project) => String(project.id) === projectId)) setProjectId(String(eligible[0].id));
            }
        } catch (e) { setError(e.message); }
    };
    useEffect(() => { refresh(); }, []);
    async function create(event) { event.preventDefault(); try { await platformRequest("/payments/projects", { method: "POST", body: JSON.stringify({ projectId: Number(projectId), amount: Number(amount) }) }); setAmount(""); await refresh(); } catch (e) { setError(e.message); } }
    async function markPaid(item) { try { const path = item.projectId ? `/payments/projects/${item.id}/mark-paid` : `/payments/${item.id}/mark-paid`; await platformRequest(path, { method: "PATCH" }); await refresh(); } catch (e) { setError(e.message); } }
    return <><PageHeading eyebrow="FREELANCE WORKSPACE" title="Payment records" copy="Simulated invoices and project payment status; this platform does not process money." />{error && <div className="notice error">{error}</div>}{completed.length > 0 && <form className="panel post-form" onSubmit={create}><h2>Create simulated invoice</h2><label>Completed gig<select value={projectId} onChange={(e) => setProjectId(e.target.value)}>{completed.map((project) => <option key={project.id} value={project.id}>{project.title} · {project.freelancerName}</option>)}</select></label><label>Amount (₹)<input type="number" min="0.01" step="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)} /></label><button className="button primary">Create invoice</button></form>}<div className="stat-grid"><Stat label="Projects tracked" value={items.length} icon="▤"/><Stat label="Pending" value={items.filter((x) => x.status !== "PAID").length} icon="◷"/><Stat label="Paid" value={items.filter((x) => x.status === "PAID").length} icon="✓"/></div><div className="panel"><h2>Payment history</h2>{items.map((item) => <div className="table-row" key={`${item.projectId || item.applicationId}-${item.id}`}><div><strong>{item.projectTitle}</strong><span>{item.invoiceNumber} · {new Date(item.createdAt).toLocaleDateString()} · Freelancer receives ₹{item.freelancerAmount}</span></div><span className={`status-pill ${item.status === "PAID" ? "" : "warning"}`}>{item.status} · ₹{item.amount}</span>{item.status !== "PAID" && <button className="text-button" onClick={() => markPaid(item)}>Mark paid</button>}</div>)}{!items.length && !error && <EmptyState text="Payment records appear here after an eligible gig is completed." />}</div></>;
}
