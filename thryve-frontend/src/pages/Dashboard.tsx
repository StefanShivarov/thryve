import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import AppHeader from "../components/AppHeader";
import { api } from "../lib/api";

function IconBookOpen(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M12 19c-2-1.5-5-1.5-7 0V6c2-1.5 5-1.5 7 0m0 13c2-1.5 5-1.5 7 0V6c-2-1.5-5-1.5-7 0m0 13V6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconUser(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM5.5 20a6.5 6.5 0 0113 0" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconChevronRight(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}

function avatarFromToken() {
    const t = localStorage.getItem("accessToken");
    try {
        const [, b] = (t || "").split(".");
        const p = JSON.parse(atob(b));
        const email: string = p?.sub || "";
        const name = (p?.name || "").trim();
        const label = name || email || "User";
        const initial = label.charAt(0).toUpperCase() || "U";
        return { label, initial };
    } catch {
        return { label: "User", initial: "U" };
    }
}

type Page<T> = {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
};
type EnrollmentRequest = { id: string; state?: "PENDING" | "ACCEPTED" | "REJECTED"; courseId?: string | number; course?: { id?: string | number } };
type User = { id: string; email?: string };

function getTokenEmail(): string | null {
    const t = localStorage.getItem("accessToken");
    if (!t) return null;
    try {
        const [, b] = t.split(".");
        const p = JSON.parse(atob(b || ""));
        return p?.sub || null;
    } catch {
        return null;
    }
}

async function resolveUserId(): Promise<string | null> {
    const email = getTokenEmail();
    if (!email) return null;
    const cacheKey = `userId:${email}`;
    const cached = localStorage.getItem(cacheKey);
    if (cached) return cached;

    try {
        const meRes = await api.get("/api/users/me");
        const me: User = meRes.data?.data ?? meRes.data;
        if (me?.id) {
            localStorage.setItem(cacheKey, me.id);
            return me.id;
        }
    } catch {}

    try {
        const res = await api.get("/api/users", {
            params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
        });
        const page: Page<User> = res.data?.data ?? res.data;
        const found = (page?.content || []).find((u) => u.email === email);
        if (found?.id) {
            localStorage.setItem(cacheKey, found.id);
            return found.id;
        }
    } catch {}

    return null;
}

export default function Dashboard() {
    const { label, initial } = avatarFromToken();

    const [userId, setUserId] = useState<string | null>(null);
    useEffect(() => {
        resolveUserId().then(setUserId);
    }, []);

    const pendingQ = useQuery({
        queryKey: ["my-enrollment-requests", userId],
        enabled: !!userId,
        queryFn: async () => {
            const { data } = await api.get(`/api/users/${userId}/enrollment-requests`, {
                params: { pageNumber: 0, pageSize: 500, sortBy: "id", direction: "DESC" },
            });
            const page: Page<EnrollmentRequest> = data?.data ?? data;
            const items = page?.content ?? [];
            return items.filter((r) => r.state === "PENDING").length;
        },
        staleTime: 10_000,
    });

    const pendingCount = pendingQ.data ?? 0;

    return (
        <div className="min-h-screen bg-gray-50">
            <AppHeader />

            <div className="relative">
                <div className="h-36 w-full bg-gradient-to-r from-indigo-500 via-sky-500 to-teal-500" />
                <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,.25)_95%)]" />
                <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <div className="text-sm opacity-90">Welcome back</div>
                            <h1 className="text-2xl font-semibold">{label}</h1>
                        </div>
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-white text-gray-800 font-semibold">
                            {initial}
                        </div>
                    </div>
                </div>
            </div>

            <div className="px-6 py-6">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    <Link to="/courses" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-indigo-50 p-3">
                                <IconBookOpen className="h-6 w-6 text-indigo-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Browse</div>
                                <div className="flex items-center gap-2">
                                    <div className="text-lg font-semibold">Courses</div>
                                    {pendingCount > 0 && (
                                        <span className="rounded-full bg-violet-100 px-2.5 py-0.5 text-xs font-medium text-violet-700">
                      {pendingCount} pending
                    </span>
                                    )}
                                </div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">Find courses and see details.</p>
                    </Link>

                    <Link to="/profile" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-sky-50 p-3">
                                <IconUser className="h-6 w-6 text-sky-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Account</div>
                                <div className="text-lg font-semibold">Profile</div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">View and edit your details.</p>
                    </Link>
                </div>
            </div>
        </div>
    );
}
