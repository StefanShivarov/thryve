export function decodeToken(): any {
    const t = localStorage.getItem("accessToken");
    if (!t) return null;
    try {
        const [, b] = t.split(".");
        return JSON.parse(atob(b || ""));
    } catch {
        return null;
    }
}
export function hasAnyRole(...roles: string[]): boolean {
    const p = decodeToken();
    if (!p) return false;
    const wanted = new Set(roles.map(r => r.toUpperCase()));
    const arr: string[] = Array.isArray(p.roles) ? p.roles : Array.isArray(p.authorities) ? p.authorities : [];
    if (arr.length) {
        const norm = arr.map((r) => String(r).replace(/^ROLE_/, "").toUpperCase());
        return norm.some((r) => wanted.has(r));
    }
    const scope: string = typeof p.scope === "string" ? p.scope : "";
    if (scope) {
        const norm = scope.split(/\s+/).map(s => s.replace(/^ROLE_/, "").toUpperCase());
        return norm.some((r) => wanted.has(r));
    }
    const role: string = typeof p.role === "string" ? p.role : "";
    if (role) {
        const norm = role.replace(/^ROLE_/, "").toUpperCase();
        return wanted.has(norm);
    }
    return false;
}
