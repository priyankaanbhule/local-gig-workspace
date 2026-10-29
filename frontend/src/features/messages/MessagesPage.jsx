import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function MessagesPage({ user }) {
    const [conversations, setConversations] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    const [messages, setMessages] = useState([]);
    const [body, setBody] = useState("");
    const [error, setError] = useState("");
    const isEmployer = ["EMPLOYER", "ADMIN"].includes(String(user.role).toUpperCase());
    useEffect(() => {
        const load = async () => {
            try {
                let apps = [];
                if (isEmployer) {
                    const listings = await platformRequest("/opportunities/mine");
                    const groups = await Promise.all(listings.map((listing) => platformRequest(`/opportunities/${listing.id}/applications`)));
                    apps = groups.flat();
                } else apps = await platformRequest("/applications/me");
                const projects = await platformRequest("/projects/me");
                const applicationConversations = apps.map((application) => ({
                    id: `APPLICATION-${application.id}`,
                    type: "application",
                    entityId: application.id,
                    label: `${isEmployer ? application.applicantName : application.company} · ${application.opportunityTitle}`,
                }));
                const projectConversations = projects.map((project) => ({
                    id: `PROJECT-${project.id}`,
                    type: "project",
                    entityId: project.id,
                    label: `${project.title} · ${isEmployer ? project.freelancerName : project.employerName}`,
                }));
                const available = [...projectConversations, ...applicationConversations];
                setConversations(available);
                if (available.length) setSelectedId(available[0].id);
            } catch (e) { setError(e.message); }
        };
        load();
    }, [isEmployer]);
    useEffect(() => {
        const selected = conversations.find((conversation) => conversation.id === selectedId);
        if (!selected) { setMessages([]); return; }
        const path = selected.type === "project"
            ? `/messages/projects/${selected.entityId}`
            : `/messages?applicationId=${selected.entityId}`;
        platformRequest(path).then(setMessages).catch((e) => setError(e.message));
    }, [selectedId, conversations]);
    async function send(event) {
        event.preventDefault();
        if (!body.trim()) return;
        try {
            const selected = conversations.find((conversation) => conversation.id === selectedId);
            if (!selected) return;
            const path = selected.type === "project" ? `/messages/projects/${selected.entityId}` : "/messages";
            const payload = selected.type === "project"
                ? { body }
                : { applicationId: Number(selected.entityId), body };
            const sent = await platformRequest(path, { method: "POST", body: JSON.stringify(payload) });
            setMessages((items) => [...items, sent]); setBody(""); setError("");
        } catch (e) { setError(e.message); }
    }
    return <><PageHeading eyebrow="INBOX" title="Messages" copy="Conversations are linked to applications and project assignments." />
        {error && <div className="notice error">{error}</div>}
        {!conversations.length ? <div className="panel"><EmptyState text="Your application and project conversations will appear here." /></div> : <div className="panel">
            <label>Conversation<select value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>{conversations.map((conversation) => <option key={conversation.id} value={conversation.id}>{conversation.label}</option>)}</select></label>
            <div className="message-thread">{messages.map((item) => <p className={`message-bubble ${item.senderId === user.id ? "mine" : ""}`} key={`${selectedId}-${item.id}`}><strong>{item.senderName}</strong><br />{item.body}</p>)}</div>
            <form className="message-compose" onSubmit={send}><input value={body} onChange={(e) => setBody(e.target.value)} maxLength="2000" placeholder="Write a message" /><button className="button primary">Send</button></form>
        </div>}</>;
}
