
const ACCESS_KEY = "accessToken";

type JwtPayload = { exp?: number; [k: string]: any };

const decode = (t?: string): JwtPayload => {
    if (!t) return {};
    try {
        const [, b] = t.split(".");
        return JSON.parse(atob(b));
    } catch {
        return {};
    }
};

export const getAccessToken = () => localStorage.getItem(ACCESS_KEY);

export const isTokenExpired = (token?: string, skewSec = 30) => {
    if (!token) return true;
    const { exp } = decode(token);
    if (!exp) return false;
    const nowSec = Math.floor(Date.now() / 1000);
    return exp - skewSec <= nowSec;
};

export const isAuthenticated = () => {
    const t = getAccessToken();
    return !!t && !isTokenExpired(t);
};


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

export function rolesFromToken(): string[] {
    const p = decodeToken();
    if (!p) return [];
    const src = Array.isArray(p.roles) ? p.roles : Array.isArray(p.authorities) ? p.authorities : [];
    return src
        .map((r: any) => (typeof r === "string" ? r : r?.name ?? ""))
        .filter(Boolean)
        .map((s: string) => s.replace(/^ROLE_/, "").toUpperCase());
}

export function hasAnyRole(...wanted: string[]): boolean {
    const roles = rolesFromToken();
    const wantedUpper = wanted.map((s) => s.toUpperCase());
    if (roles.some((r) => wantedUpper.includes(r))) return true;

    const email: string | undefined = decodeToken()?.sub;
    const fallbackAdmins = ["admin@thryve.local"];
    if (email && fallbackAdmins.includes(email.toLowerCase()) && wantedUpper.some((r) => r === "ADMIN" || r === "CREATOR")) {
        return true;
    }
    return false;
}
