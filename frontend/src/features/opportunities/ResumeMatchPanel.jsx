import { useState } from "react";
import { platformRequest } from "../../api/platform.js";

export default function ResumeMatchPanel({ opportunity }) {
    const [resumeText, setResumeText] = useState("");
    const [result, setResult] = useState(null);
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(false);
    async function analyze(event) {
        event.preventDefault(); setBusy(true); setError(""); setResult(null);
        try { setResult(await platformRequest(`/opportunities/${opportunity.id}/match`, { method: "POST", body: JSON.stringify({ resumeText }) })); }
        catch (e) { setError(e.message); }
        finally { setBusy(false); }
    }
    return <details className="resume-match"><summary>Check resume match</summary><form onSubmit={analyze}><label>Paste resume text (up to 20,000 characters)<textarea rows="5" maxLength="20000" value={resumeText} onChange={(e) => setResumeText(e.target.value)} placeholder="Paste your resume text here. Your profile skills and experience are also included." required /></label><button className="button secondary" disabled={busy}>{busy ? "Analyzing…" : "Analyze match"}</button></form>{error && <p className="notice error">{error}</p>}{result && <div className="resume-match-result"><strong>{result.matchPercent}% keyword match</strong><p>{result.guidance}</p><p><strong>Matched skills:</strong> {result.matchedSkills.join(", ") || "None found"}</p><p><strong>Missing listed skills:</strong> {result.missingSkills.join(", ") || "None"}</p><details><summary>Keyword details</summary><p>Matched: {result.matchedKeywords.join(", ") || "None"}</p><p>Not found: {result.missingKeywords.join(", ") || "None"}</p></details></div>}</details>;
}