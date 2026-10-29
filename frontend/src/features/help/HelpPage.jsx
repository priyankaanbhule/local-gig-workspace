import { useState } from "react";

export default function HelpPage() {
    const [question, setQuestion] = useState("");
    const [answer, setAnswer] = useState("");
    const faqs = [
        ["How do I apply for a job?", "Open Find work, choose a listing, and select Apply. You can include a cover letter and resume link or upload a resume."],
        ["How do I send a freelance proposal?", "Browse a gig and choose Apply / propose. Enter your proposal, bid amount, and estimated delivery days."],
        ["How do I track an application?", "Open My applications to see employer status updates. Freelancers can track gig proposals under My proposals."],
        ["How do I get verified as an employer?", "Open Employer tools and submit your company verification details. An admin reviews the request."],
        ["How do I change my password?", "Open Account settings and use the Change password form."],
        ["Are payments processed here?", "No. This project records simulated project payment status; it does not charge or transfer real money."],
        ["How are recommendations calculated?", "Recommendations compare your profile skills, preferred role and location against active listings. They are rule-based, not AI."],
    ];
    function ask(event) { event.preventDefault(); const q = question.toLowerCase(); const match = faqs.find(([title, body]) => (title + " " + body).toLowerCase().split(/\W+/).some((word) => word.length > 3 && q.includes(word))); setAnswer(match ? match[1] : "I could not match that question. Try asking about applying, proposals, applications, verification, passwords, payments, or recommendations."); }
    return <><div className="page-heading"><p className="eyebrow">HELP CENTER</p><h1>How can we help?</h1><p className="muted">Quick answers about using LocalGig Workspace.</p></div><form className="filters" onSubmit={ask}><input value={question} onChange={(event) => setQuestion(event.target.value)} placeholder="Ask about applying, proposals, your account…" required/><button className="button primary">Ask</button></form>{answer && <div className="panel"><strong>LocalGig help</strong><p>{answer}</p></div>}<div className="panel">{faqs.map(([title, body]) => <details className="faq-row" key={title}><summary>{title}</summary><p>{body}</p></details>)}</div></>;
}