import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AppHeader from "../components/AppHeader";
import { api } from "../lib/api";
import { hasAnyRole } from "../lib/auth";

type User = {
    id: string;
    email: string;
    username?: string;
    firstName?: string;
    lastName?: string;
};

type Course = { id: string; title: string };

type EnrollmentRequest = {
    id: string;
    message?: string;
    createdAt?: string;
    user?: User;
    course?: Course;
    userId?: string;
    courseId?: string;
};

type Page<T> = {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
};


const isStaff = hasAnyRole("CREATOR", "ADMIN");
const MIN_NOTE = 0;

function decodeEmail(): string | null {
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
    const cached = localStorage.getItem("userId");
    if (cached) return cached;

    try {
        const meRes = await api.get("/api/users/me");
        const me: User = meRes.data?.data ?? meRes.data;
        if (me?.id) {
            localStorage.setItem("userId", me.id);
            return me.id;
        }
    } catch {}

    const email = decodeEmail();
    if (!email) return null;
    try {
        const listRes = await api.get("/api/users", {
            params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
        });
        const page: Page<User> = listRes.data?.data ?? listRes.data;
        const found = (page?.content || []).find((u) => u.email === email);
        if (found?.id) {
            localStorage.setItem("userId", found.id);
            return found.id;
        }
    } catch {}
    return null;
}

function fullName(u?: User) {
    if (!u) return "";
    const name = `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim();
    return name || u.username || u.email || u.id;
}


export default function CourseRequestsPage() {
    const { id: courseId } = useParams<{ id: string }>();
    const qc = useQueryClient();

    const requestsQ = useQuery({
        queryKey: ["course-requests", courseId],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${courseId}/enrollment-requests`, {
                params: { pageNumber: 0, pageSize: 200, sortBy: "createdAt", direction: "DESC" },
            });
            return (data?.data ?? data) as Page<EnrollmentRequest>;
        },
        enabled: !!courseId && isStaff,
    });


    const [note, setNote] = useState("");
    const [banner, setBanner] = useState<{ kind: "ok" | "err"; text: string } | null>(null);


    const createReqMut = useMutation({
        mutationFn: async (payload: { userId: string; courseId: string; message?: string }) => {
            const { data } = await api.post(`/api/enrollment-requests`, payload);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setBanner({ kind: "ok", text: "Request sent! An instructor/admin will review it." });
            setNote("");
            if (isStaff) qc.invalidateQueries({ queryKey: ["course-requests", courseId] });
        },
        onError: (err: any) => {
            const status = err?.response?.status;
            const msg =
                err?.response?.data?.message ||
                (status === 409
                    ? "You’ve already requested access."
                    : status === 403
                        ? "You don’t have permission."
                        : "Failed to send request.");
            setBanner({ kind: "err", text: msg });
        },
    });

    async function submitRequest() {
        setBanner(null);
        const userId = await resolveUserId();
        if (!userId || !courseId) {
            setBanner({ kind: "err", text: "Could not determine your account or course." });
            return;
        }
        const trimmed = note.trim();
        if (trimmed.length < MIN_NOTE) {
            setBanner({ kind: "err", text: `Please add a brief note (min ${MIN_NOTE} characters).` });
            return;
        }
        createReqMut.mutate({ userId, courseId, message: trimmed || undefined });
    }


    const acceptMut = useMutation({
        mutationFn: async (req: EnrollmentRequest) => {

            const uId = req.userId ?? req.user?.id;
            const cId = req.courseId ?? req.course?.id ?? courseId;
            if (!uId || !cId) throw new Error("MISSING_IDS");

            await api.post(`/api/enrollments`, { userId: uId, courseId: cId });

            await api.delete(`/api/enrollment-requests/${req.id}`);

        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["course-requests", courseId] }),
    });

    const rejectMut = useMutation({
        mutationFn: async (reqId: string) => {
            await api.delete(`/api/enrollment-requests/${reqId}`);
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["course-requests", courseId] }),
    });

    const gradients = [
        "from-indigo-500 via-blue-500 to-purple-500",
        "from-fuchsia-500 via-pink-500 to-rose-500",
        "from-emerald-500 via-teal-500 to-cyan-500",
        "from-amber-500 via-orange-500 to-red-500",
    ];
    const gradient = useMemo(() => gradients[(courseId || "0").split("").reduce((s, ch) => s + ch.charCodeAt(0), 0) % gradients.length], [courseId]);

    return (
        <div className="min-h-screen bg-gray-50">
            <AppHeader />
            <div className="pb-10">
                {}
                <div className="relative">
                    <div className={`h-40 w-full bg-gradient-to-br ${gradient}`} />
                    <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,0.25)_90%)]" />
                    <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                        <div className="mb-2 text-sm opacity-90">
                            <Link to={`/courses/${courseId}`} className="underline underline-offset-2">
                                ← Back to Course
                            </Link>
                        </div>
                        <div className="flex items-center justify-between">
                            <h1 className="text-2xl font-semibold">Enrollment requests</h1>
                        </div>
                    </div>
                </div>

                <div className="px-6 mt-6 grid grid-cols-1 gap-6 md:grid-cols-[2fr_1fr]">
                    {}
                    {isStaff ? (
                        <div className="rounded-2xl border bg-white p-6">
                            <h2 className="text-lg font-semibold">Pending requests</h2>

                            {requestsQ.isLoading ? (
                                <div className="mt-4 grid gap-2">
                                    {Array.from({ length: 6 }).map((_, i) => (
                                        <div key={i} className="h-12 animate-pulse rounded-xl border bg-gray-100" />
                                    ))}
                                </div>
                            ) : (requestsQ.data?.content?.length ?? 0) === 0 ? (
                                <div className="mt-4 rounded-xl border bg-gray-50 p-6 text-center text-gray-600">
                                    No pending requests.
                                </div>
                            ) : (
                                <div className="mt-4 overflow-x-auto">
                                    <table className="min-w-full text-sm">
                                        <thead>
                                        <tr className="text-left text-gray-600">
                                            <th className="px-3 py-2">Student</th>
                                            <th className="px-3 py-2">Email</th>
                                            <th className="px-3 py-2">Note</th>
                                            <th className="px-3 py-2">Requested</th>
                                            <th className="px-3 py-2" />
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {requestsQ.data!.content.map((r) => (
                                            <tr key={r.id} className="border-t">
                                                <td className="px-3 py-2">{fullName(r.user)}</td>
                                                <td className="px-3 py-2">{r.user?.email ?? ""}</td>
                                                <td className="px-3 py-2 max-w-[420px]">
                                                    <span title={r.message}>{r.message || "—"}</span>
                                                </td>
                                                <td className="px-3 py-2">{r.createdAt ? new Date(r.createdAt).toLocaleString() : "—"}</td>
                                                <td className="px-3 py-2">
                                                    <div className="flex items-center gap-2">
                                                        <button
                                                            className="rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50 disabled:opacity-60"
                                                            onClick={() => acceptMut.mutate(r)}
                                                            disabled={acceptMut.isPending}
                                                        >
                                                            {acceptMut.isPending ? "Accepting…" : "Accept"}
                                                        </button>
                                                        <button
                                                            className="rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50 disabled:opacity-60"
                                                            onClick={() => {
                                                                if (confirm("Reject (delete) this request?")) rejectMut.mutate(r.id);
                                                            }}
                                                            disabled={rejectMut.isPending}
                                                        >
                                                            {rejectMut.isPending ? "Rejecting…" : "Reject"}
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    ) : (
                        <div className="rounded-2xl border bg-white p-6">
                            <h2 className="text-lg font-semibold">Request access</h2>
                            {banner && (
                                <div
                                    className={`mt-3 rounded-xl px-4 py-2 text-sm ${
                                        banner.kind === "ok"
                                            ? "border border-emerald-200 bg-emerald-50 text-emerald-700"
                                            : "border border-red-200 bg-red-50 text-red-700"
                                    }`}
                                >
                                    {banner.text}
                                </div>
                            )}
                            <p className="mt-2 text-sm text-gray-600">
                                Send an access request to the instructors of this course.
                            </p>
                            <div className="mt-3">
                                <label className="block text-sm font-medium">Note (optional)</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={3}
                                    value={note}
                                    onChange={(e) => setNote(e.target.value)}
                                    placeholder="Brief message to the instructor (optional)"
                                />
                            </div>
                            <div className="mt-4 flex items-center justify-end">
                                <button
                                    onClick={submitRequest}
                                    disabled={createReqMut.isPending}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {createReqMut.isPending ? "Sending…" : "Send request"}
                                </button>
                            </div>
                        </div>
                    )}

                    <div className="space-y-6">
                        <div className="rounded-2xl border bg-white p-6">
                            <h3 className="text-sm font-semibold text-gray-700">Quick links</h3>
                            <div className="mt-3 flex flex-col gap-2">
                                <Link to={`/courses/${courseId}`} className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">
                                    Course overview
                                </Link>
                                <Link to={`/courses`} className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">
                                    All courses
                                </Link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
