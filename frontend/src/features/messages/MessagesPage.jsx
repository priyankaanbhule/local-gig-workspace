import { useEffect, useState } from "react";
import { platformRequest } from "../../api/platform.js";
import { PageHeading, EmptyState } from "../../components/WorkspaceUI.jsx";

export function MessagesPage({ user, directRecipient }) {
    const [conversations, setConversations] = useState([]);
    const [selectedId, setSelectedId] = useState("");
    const [messages, setMessages] = useState([]);
    const [body, setBody] = useState("");
    const [error, setError] = useState("");
    const isEmployer = ["EMPLOYER", "ADMIN"].includes(String(user.role).toUpperCase());

    useEffect(() => {
        let alive = true;
        async function loadConversations() {
            try {
                const [directRows, projects] = await Promise.all([
                    platformRequest("/messages/direct/conversations"),
                    platformRequest("/projects/me"),
                ]);
                let applications = [];
                if (isEmployer) {
                    const listings = await platformRequest("/opportunities/mine");
                    const groups = await Promise.all(listings.map((listing) =>
                        platformRequest(`/opportunities/${listing.id}/applications`)));
                    applications = groups.flat();
                } else applications = await platformRequest("/applications/me");

                const direct = directRows.map((item) => ({
                    id: `DIRECT-${item.userId}`, type: "direct", entityId: item.userId,
                    label: `${item.fullName} · Direct message`, unreadCount: item.unreadCount,
                    lastMessage: item.lastMessage,
                }));
                const applicationThreads = applications.map((application) => ({
                    id: `APPLICATION-${application.id}`, type: "application", entityId: application.id,
                    label: `${isEmployer ? application.applicantName : application.company} · ${application.opportunityTitle}`,
                }));
                const projectThreads = projects.map((project) => ({
                    id: `PROJECT-${project.id}`, type: "project", entityId: project.id,
                    label: `${project.title} · ${isEmployer ? project.freelancerName : project.employerName}`,
                }));
                let available = [...direct, ...applicationThreads, ...projectThreads];
                if (directRecipient && !available.some((item) => item.id === `DIRECT-${directRecipient.userId}`)) {
                    available = [{ id: `DIRECT-${directRecipient.userId}`, type: "direct",
                        entityId: directRecipient.userId, label: `${directRecipient.fullName} · Direct message` }, ...available];
                }
                if (!alive) return;
                setConversations(available);
                setSelectedId((current) => directRecipient ? `DIRECT-${directRecipient.userId}`
                    : current && available.some((item) => item.id === current) ? current : available[0]?.id || "");
                setError("");
            } catch (requestError) {
                if (alive) setError(requestError.message);
            }
        }
        loadConversations();
        const timer = window.setInterval(loadConversations, 15000);
        return () => { alive = false; window.clearInterval(timer); };
    }, [isEmployer, directRecipient]);

    useEffect(() => {
        const selected = conversations.find((item) => item.id === selectedId);
        if (!selected) { setMessages([]); return; }
        const path = selected.type === "project" ? `/messages/projects/${selected.entityId}`
            : selected.type === "direct" ? `/messages/direct?userId=${selected.entityId}`
                : `/messages?applicationId=${selected.entityId}`;
        let alive = true;
        platformRequest(path).then((rows) => { if (alive) setMessages(rows); })
            .catch((requestError) => { if (alive) setError(requestError.message); });
        return () => { alive = false; };
    }, [selectedId, conversations]);

    async function send(event) {
        event.preventDefault();
        if (!body.trim()) return;
        const selected = conversations.find((item) => item.id === selectedId);
        if (!selected) return;
        try {
            const path = selected.type === "project" ? `/messages/projects/${selected.entityId}`
                : selected.type === "direct" ? "/messages/direct" : "/messages";
            const payload = selected.type === "project" ? { body }
                : selected.type === "direct" ? { recipientId: Number(selected.entityId), body }
                    : { applicationId: Number(selected.entityId), body };
            const sent = await platformRequest(path, { method: "POST", body: JSON.stringify(payload) });
            setMessages((items) => [...items, sent]);
            setBody("");
            setError("");
            if (selected.type === "direct") {
                setConversations((rows) => rows.map((item) => item.id === selected.id
                    ? { ...item, lastMessage: sent.body } : item));
            }
        } catch (requestError) { setError(requestError.message); }
    }

    return <><PageHeading eyebrow="INBOX" title="Messages" copy="Talk with applicants, recruiters, clients, and freelancers in one inbox." />
        {error && <div className="notice error">{error}</div>}
        {!conversations.length ? <div className="panel"><EmptyState text="No conversations yet. Find a candidate or apply to an opportunity to start a conversation." /></div>
            : <div className="panel messages-workspace">
                <label>Conversation<select value={selectedId} onChange={(event) => setSelectedId(event.target.value)}>
                    {conversations.map((conversation) => <option key={conversation.id} value={conversation.id}>{conversation.label}{conversation.unreadCount ? ` · ${conversation.unreadCount} new` : ""}</option>)}
                </select></label>
                <div className="message-thread" aria-live="polite">
                    {messages.map((item) => { const mine = item.senderId === user.id; return <div className={`message-line ${mine ? "mine" : ""}`} key={`${selectedId}-${item.id}`}><p className={`message-bubble ${mine ? "mine" : ""}`}><strong>{item.senderName}</strong><br />{item.body}<small>{new Date(item.createdAt).toLocaleString()}</small></p></div>; })}
                    {!messages.length && <EmptyState text="Start the conversation with a short, clear message." />}
                </div>
                <form className="message-compose" onSubmit={send}><input aria-label="Message" value={body} onChange={(event) => setBody(event.target.value)} maxLength="2000" placeholder="Write a message" /><button className="button primary" disabled={!body.trim()}>Send</button></form>
            </div>}
    </>;
}
