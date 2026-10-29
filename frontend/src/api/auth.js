const AUTH_API = "/api/auth";

export async function authRequest(path, options = {}) {
    const response = await fetch(`${AUTH_API}${path}`, {
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
            data.message || data.detail || "The request failed. Please try again."
        );
    }

    return data;
}
