const PLATFORM_API = "/api";
export async function platformRequest(path, options = {}) {
    const response = await fetch(`${PLATFORM_API}${path}`, {
        ...options,
        credentials: "include",
        headers: { "Content-Type": "application/json", ...options.headers },
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.detail || data.message || `Request failed (HTTP ${response.status}).`);
    return data;
}

export async function uploadAuthenticatedFile(file, category) {
    const form = new FormData();
    form.append("file", file);
    const response = await fetch(`${PLATFORM_API}/files?category=${encodeURIComponent(category)}`, {
        method: "POST", credentials: "include", body: form,
    });
    const data = await response.json().catch(() => ({}));
    if (!response.ok) throw new Error(data.message || data.detail || "Upload failed.");
    return data;
}

export function mapOpportunity(value) {
    return {
        ...value,
        kind: String(value.kind || "JOB").toLowerCase() === "internship" ? "Internship"
            : String(value.kind || "JOB").toLowerCase() === "gig" ? "Gig" : "Job",
        status: String(value.status || "ACTIVE").toUpperCase() === "ACTIVE" ? "Active" : "Closed",
        posted: value.createdAt ? new Date(value.createdAt).toLocaleDateString() : "Recently",
        skills: value.skills || [],
        demo: false,
    };
}

export function fileHref(path) {
    return path?.startsWith("/api/") ? `${PLATFORM_API}${path}` : path;
}

