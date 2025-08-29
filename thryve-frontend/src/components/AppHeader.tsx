import { Link } from "react-router-dom";
import { useEffect, useRef, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";

function IconBell(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path
                d="M15 17H9m8-6a5 5 0 10-10 0c0 3-1 4-2 5h14c-1-1-2-2-2-5zM12 21a2 2 0 01-2-2h4a2 2 0 01-2 2z"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    );
}
function IconLogOut(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path
                d="M15 17l5-5-5-5M20 12H9m3 7v2a1 1 0 01-1 1H6a2 2 0 01-2-2V4a2 2 0 012-2h5a1 1 0 011 1v2"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    );
}

function getAuth() {
    const t = localStorage.getItem("accessToken");
    if (!t) return { authed: false, initial: "U" };
    try {
        const [, b] = t.split(".");
        const p = JSON.parse(atob(b || ""));
        const email: string = p?.sub || "";
        const name = (p?.name || "").trim();
        const initial = (name || email || "U").charAt(0).toUpperCase();
        return { authed: true, initial };
    } catch {
        return { authed: false, initial: "U" };
    }
}

type Notification = {
    id: string;
    title?: string;
    message?: string;
    read?: boolean;
    createdAt?: string;
    senderName?: string | null;
    senderEmail?: string | null;
};
type Page<T> = { content: T[] };

export default function AppHeader() {
    const { initial } = getAuth();
    const [open, setOpen] = useState(false);
    const popRef = useRef<HTMLDivElement>(null);
    const qc = useQueryClient();

    const countQ = useQuery({
        queryKey: ["notifications", "me", "unread-count"],
        queryFn: async () => {
            const { data } = await api.get("/api/notifications/me/unread-count");
            return (data?.data ?? data ?? 0) as number;
        },
        refetchInterval: 30000,
    });

    const listQ = useQuery({
        queryKey: ["notifications", "me", "list"],
        queryFn: async () => {
            const { data } = await api.get("/api/notifications/me", {
                params: { pageNumber: 0, pageSize: 20, sortBy: "createdAt", direction: "DESC" },
            });
            return (data?.data ?? data) as Page<Notification>;
        },
        enabled: open,
    });

    const markRead = useMutation({
        mutationFn: async (id: string) => {
            await api.post(`/api/notifications/${id}/read`);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["notifications"] });
        },
    });

    const delOne = useMutation({
        mutationFn: async (id: string) => {
            await api.delete(`/api/notifications/${id}`);
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["notifications"] });
        },
    });

    const clearMine = useMutation({
        mutationFn: async () => {
            await api.delete("/api/notifications/me");
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["notifications"] });
        },
    });

    useEffect(() => {
        function onDoc(e: MouseEvent) {
            if (popRef.current && !popRef.current.contains(e.target as Node)) setOpen(false);
        }
        if (open) document.addEventListener("mousedown", onDoc);
        return () => document.removeEventListener("mousedown", onDoc);
    }, [open]);

    const unread = countQ.data ?? 0;
    const items = listQ.data?.content ?? [];

    return (
        <div className="sticky top-0 z-40 border-b bg-white/90 backdrop-blur">
            <div className="mx-auto flex h-14 max-w-screen-2xl items-center justify-between px-4">
                <div className="flex items-center">
                    <Link
                        to="/"
                        className="inline-flex items-center rounded-lg px-2 py-1 text-sm font-semibold text-gray-900 hover:bg-gray-100"
                    >
                        Home
                    </Link>
                </div>

                <div className="flex items-center justify-end gap-3">
                    <div className="relative" ref={popRef}>
                        <button
                            onClick={() => setOpen((v) => !v)}
                            className="relative inline-flex h-9 w-9 items-center justify-center rounded-full border bg-white hover:bg-gray-50"
                            aria-haspopup="true"
                            aria-expanded={open}
                            aria-label="Notifications"
                        >
                            <IconBell className="h-5 w-5 text-gray-700" />
                            {unread > 0 && (
                                <span className="absolute -right-1 -top-1 inline-flex h-5 min-w-[20px] items-center justify-center rounded-full bg-red-600 px-1 text-xs font-medium text-white">
                  {unread > 99 ? "99+" : unread}
                </span>
                            )}
                        </button>

                        {open && (
                            <div className="absolute right-0 top-12 w-[380px] max-w-[90vw] rounded-2xl border bg-white shadow-lg">
                                <div className="flex items-center justify-between border-b px-4 py-3">
                                    <div className="text-sm font-semibold">Notifications</div>
                                    <button
                                        onClick={() => clearMine.mutate()}
                                        disabled={clearMine.isPending || listQ.isLoading || items.length === 0}
                                        className="text-xs text-gray-600 hover:underline disabled:opacity-50"
                                    >
                                        Clear all
                                    </button>
                                </div>

                                <div className="max-h-[70vh] overflow-auto p-2">
                                    {listQ.isLoading && (
                                        <div className="space-y-2 p-2">
                                            <div className="h-16 animate-pulse rounded-xl border bg-white" />
                                            <div className="h-16 animate-pulse rounded-xl border bg-white" />
                                        </div>
                                    )}

                                    {listQ.error && (
                                        <div className="m-2 rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                                            Failed to load notifications.
                                        </div>
                                    )}

                                    {!listQ.isLoading && items.length === 0 && (
                                        <div className="p-4 text-center text-sm text-gray-600">No notifications.</div>
                                    )}

                                    {items.length > 0 && (
                                        <ul className="space-y-2">
                                            {items.map((n) => (
                                                <li
                                                    key={n.id}
                                                    className={`rounded-xl border bg-white p-3 ${!n.read ? "ring-1 ring-indigo-100" : ""}`}
                                                >
                                                    <div className="flex items-start justify-between gap-3">
                                                        <div className="min-w-0">
                                                            <div className="flex items-center gap-2">
                                                                <div className="truncate text-sm font-semibold">
                                                                    {n.title || "Notification"}
                                                                </div>
                                                                {!n.read && <span className="h-2 w-2 rounded-full bg-indigo-600" />}
                                                            </div>

                                                            {(n.senderName || n.senderEmail) && (
                                                                <div className="mt-0.5 text-[11px] text-gray-500">
                                                                    From: {n.senderName || n.senderEmail}
                                                                </div>
                                                            )}

                                                            {n.message && (
                                                                <div className="mt-1 line-clamp-2 text-xs text-gray-700">{n.message}</div>
                                                            )}

                                                            {n.createdAt && (
                                                                <div className="mt-1 text-[11px] text-gray-500">
                                                                    {new Date(n.createdAt).toLocaleString()}
                                                                </div>
                                                            )}
                                                        </div>

                                                        <div className="flex shrink-0 flex-col gap-1">
                                                            {!n.read && (
                                                                <button
                                                                    onClick={() => markRead.mutate(n.id)}
                                                                    disabled={markRead.isPending}
                                                                    className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50 disabled:opacity-60"
                                                                >
                                                                    Mark read
                                                                </button>
                                                            )}
                                                            <button
                                                                onClick={() => delOne.mutate(n.id)}
                                                                disabled={delOne.isPending}
                                                                className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50 disabled:opacity-60"
                                                            >
                                                                Remove
                                                            </button>
                                                        </div>
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>

                    <Link
                        to="/profile"
                        className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-gray-900 text-white font-semibold"
                        aria-label="Profile"
                    >
                        {initial}
                    </Link>
                    <Link
                        to="/logout"
                        className="inline-flex h-9 items-center gap-2 rounded-lg border bg-white px-3 text-sm font-medium hover:bg-gray-50"
                        aria-label="Logout"
                    >
                        <IconLogOut className="h-4 w-4 text-gray-700" />
                        Logout
                    </Link>
                </div>
            </div>
        </div>
    );
}
