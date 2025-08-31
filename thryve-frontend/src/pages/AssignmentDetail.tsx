// src/pages/AssignmentDetail.tsx
import { useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AppHeader from "../components/AppHeader";
import { api } from "../lib/api";
import { hasAnyRole } from "../lib/auth";

/* ===== Types ===== */
type Assignment = {
    id: string;
    title: string;
    description?: string;
    deadline?: string;
    totalPoints: number;
    course?: { id: string; title?: string; imageUrl?: string };
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

type User = { id: string; email?: string; username?: string; firstName?: string; lastName?: string };

type Submission = {
    id: string;
    submissionUrl: string;
    feedback?: string;
    comment?: string;
    grade?: number;
    user?: User;
};

/* ===== Helpers ===== */
function formatLocal(iso?: string) {
    if (!iso) return "";
    const d = new Date(String(iso).replace(" ", "T"));
    return Number.isNaN(d.getTime()) ? String(iso) : d.toLocaleString();
}
const MIN_OPTIONAL_TEXT = 10;

/** Decode email (sub) from JWT access token */
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

/** Resolve current user id (tries /me first, then searches by email) */
async function resolveUserId(): Promise<string | null> {
    // cache to avoid repeated list scans
    const cached = localStorage.getItem("userId");
    if (cached) return cached;

    // 1) Try /me
    try {
        const meRes = await api.get("/api/users/me");
        const me: User = meRes.data?.data ?? meRes.data;
        if (me?.id) {
            localStorage.setItem("userId", me.id);
            return me.id;
        }
    } catch {}

    // 2) Fallback: decode email and scan /users
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

/* ===== Page ===== */
export default function AssignmentDetail() {
    const canManage = hasAnyRole("CREATOR", "ADMIN");
    const { courseId, assignmentId } = useParams<{ courseId: string; assignmentId: string }>();
    const navigate = useNavigate();
    const qc = useQueryClient();

    /* Load assignment (via listing) */
    const assignmentQ = useQuery({
        queryKey: ["assignment", courseId, assignmentId],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${courseId}/assignments`, {
                params: { pageNumber: 0, pageSize: 500, sortBy: "deadline", direction: "ASC" },
            });
            const page = (data?.data ?? data) as Page<Assignment>;
            const found = page.content.find((a) => String(a.id) === String(assignmentId));
            if (!found) throw new Error("ASSIGNMENT_NOT_FOUND");
            return found;
        },
        enabled: !!courseId && !!assignmentId,
    });

    /* Submissions (only for teachers/admins) */
    const submissionsQ = useQuery({
        queryKey: ["submissions", assignmentId],
        queryFn: async () => {
            const { data } = await api.get(`/api/assignments/${assignmentId}/submissions`, {
                params: { pageNumber: 0, pageSize: 500, sortBy: "id", direction: "ASC" },
            });
            return (data?.data ?? data) as Page<Submission>;
        },
        enabled: !!assignmentId && canManage,
    });

    const assignment = assignmentQ.data;

    /* ===== Create submission (students + staff) ===== */
    const [createDraft, setCreateDraft] = useState<{ url: string; comment: string }>({
        url: "",
        comment: "",
    });
    const [createError, setCreateError] = useState<string | null>(null);
    const [createOk, setCreateOk] = useState<string | null>(null);

    const createMut = useMutation({
        mutationFn: async (payload: { userId: string; submissionUrl: string; comment?: string }) => {
            const { data } = await api.post(`/api/assignments/${assignmentId}/submissions`, payload);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setCreateDraft({ url: "", comment: "" });
            setCreateError(null);
            setCreateOk("Submission sent!");
            setTimeout(() => setCreateOk(null), 2500);
            qc.invalidateQueries({ queryKey: ["submissions", assignmentId] });
        },
        onError: (err: any) => {
            const msg =
                err?.response?.data?.message ||
                (err?.response?.status === 400
                    ? "Invalid input."
                    : err?.response?.status === 409
                        ? "A submission already exists or URL must be unique."
                        : "Failed to submit.");
            setCreateOk(null);
            setCreateError(msg);
        },
    });

    async function handleSubmit() {
        setCreateError(null);
        const url = (createDraft.url || "").trim();
        if (!/^https?:\/\/\S+$/i.test(url)) {
            setCreateError("Please provide a valid URL (starting with http or https).");
            return;
        }
        const userId = await resolveUserId();
        if (!userId) {
            setCreateError("Could not determine current user.");
            return;
        }
        const comment = (createDraft.comment || "").trim();
        const payload: { userId: string; submissionUrl: string; comment?: string } = {
            userId,
            submissionUrl: url,
        };
        if (comment.length >= MIN_OPTIONAL_TEXT) payload.comment = comment;
        createMut.mutate(payload);
    }

    function canSubmit(): boolean {
        const urlOk = /^https?:\/\/\S+$/i.test((createDraft.url || "").trim());
        const c = (createDraft.comment || "").trim();
        const commentOk = c.length === 0 || c.length >= MIN_OPTIONAL_TEXT;
        return urlOk && commentOk && !createMut.isPending;
    }

    /* ===== Grade / edit / delete (teachers/admin) ===== */
    const [gradeDraft, setGradeDraft] = useState<Record<string, { grade?: string; feedback?: string }>>({});
    const [gradeError, setGradeError] = useState<string | null>(null);
    const [gradeOk, setGradeOk] = useState<string | null>(null);

    const gradeMut = useMutation({
        mutationFn: async (args: { id: string; grade?: number; feedback?: string }) => {
            const body: any = {};
            if (args.grade != null) body.grade = args.grade;
            if ((args.feedback || "").trim().length >= MIN_OPTIONAL_TEXT) body.feedback = args.feedback?.trim();
            const { data } = await api.patch(`/api/submissions/${args.id}/grade`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setGradeError(null);
            setGradeOk("Saved grade.");
            setTimeout(() => setGradeOk(null), 2000);
            qc.invalidateQueries({ queryKey: ["submissions", assignmentId] });
        },
        onError: (err: any) => setGradeError(err?.response?.data?.message || "Failed to save grade."),
    });

    const [editMetaId, setEditMetaId] = useState<string | null>(null);
    const [editMetaDraft, setEditMetaDraft] = useState<{ url: string; comment: string }>({ url: "", comment: "" });
    const [editError, setEditError] = useState<string | null>(null);

    const updateMetaMut = useMutation({
        mutationFn: async (args: { id: string; submissionUrl?: string; comment?: string }) => {
            const body: any = {};
            if (args.submissionUrl != null) body.submissionUrl = args.submissionUrl;
            const c = (args.comment || "").trim();
            if (c.length >= MIN_OPTIONAL_TEXT) body.comment = c;
            const { data } = await api.patch(`/api/submissions/${args.id}`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setEditError(null);
            setEditMetaId(null);
            qc.invalidateQueries({ queryKey: ["submissions", assignmentId] });
        },
        onError: (err: any) => setEditError(err?.response?.data?.message || "Failed to save."),
    });

    const [deleteError, setDeleteError] = useState<string | null>(null);
    const deleteMut = useMutation({
        mutationFn: async (id: string) => {
            const { data } = await api.delete(`/api/submissions/${id}`);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setDeleteError(null);
            qc.invalidateQueries({ queryKey: ["submissions", assignmentId] });
        },
        onError: (err: any) => setDeleteError(err?.response?.data?.message || "Failed to delete submission."),
    });

    /* Banner gradient */
    const gradients = [
        "from-indigo-500 via-blue-500 to-purple-500",
        "from-fuchsia-500 via-pink-500 to-rose-500",
        "from-emerald-500 via-teal-500 to-cyan-500",
        "from-amber-500 via-orange-500 to-red-500",
    ];
    const gradient = useMemo(() => {
        const s = String(assignmentId ?? "0");
        let sum = 0;
        for (let i = 0; i < s.length; i++) sum += s.charCodeAt(i);
        return gradients[sum % gradients.length];
    }, [assignmentId]);

    /* ===== Loading / error states ===== */
    if (assignmentQ.isLoading) {
        return (
            <div className="min-h-screen bg-gray-50">
                <AppHeader />
                <div className="p-6">
                    <div className="h-40 animate-pulse rounded-3xl bg-gray-200" />
                    <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
                        {Array.from({ length: 4 }).map((_, i) => (
                            <div key={i} className="h-28 animate-pulse rounded-2xl border bg-white" />
                        ))}
                    </div>
                </div>
            </div>
        );
    }

    if (assignmentQ.error || !assignment) {
        return (
            <div className="min-h-screen bg-gray-50">
                <AppHeader />
                <div className="p-6">
                    <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
                        Couldn’t load this assignment.{" "}
                        <button className="underline" onClick={() => navigate(-1)}>
                            Go back
                        </button>
                    </div>
                </div>
            </div>
        );
    }

    /* ===== UI ===== */
    return (
        <div className="min-h-screen bg-gray-50">
            <AppHeader />
            <div className="pb-10">
                {/* Header banner */}
                <div className="relative">
                    <div className={`h-40 w-full bg-gradient-to-br ${gradient}`} />
                    <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,0.3)_90%)]" />
                    <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                        <div className="mb-2 text-sm opacity-90">
                            <Link to={`/courses/${courseId}`} className="underline underline-offset-2">
                                ← Back to Course
                            </Link>
                        </div>
                        <div className="flex items-center justify-between">
                            <h1 className="text-2xl font-semibold">{assignment.title}</h1>
                        </div>
                        <div className="mt-2 flex flex-wrap items-center gap-2 text-sm">
              <span className="inline-flex items-center rounded-full bg-black/10 px-2.5 py-1 font-medium">
                {assignment.totalPoints} pts
              </span>
                            {assignment.deadline && (
                                <span className="inline-flex items-center rounded-full bg-black/10 px-2.5 py-1 font-medium">
                  Due {formatLocal(assignment.deadline)}
                </span>
                            )}
                        </div>
                    </div>
                </div>

                {/* Body */}
                <div className="px-6">
                    {/* Description */}
                    <div className="mt-6 rounded-2xl border bg-white p-6">
                        <h2 className="text-lg font-semibold">Instructions</h2>
                        <p className="mt-2 whitespace-pre-wrap text-gray-700">
                            {assignment.description || "No description provided."}
                        </p>
                    </div>

                    {/* Submit section */}
                    <div className="mt-6 rounded-2xl border bg-white p-6">
                        <h3 className="text-base font-semibold">Submit your work</h3>
                        <p className="mt-1 text-sm text-gray-600">
                            Paste a public URL (e.g., Cloudinary link). You can add an optional comment (min{" "}
                            {MIN_OPTIONAL_TEXT} characters).
                        </p>

                        {createError && (
                            <div className="mt-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                {createError}
                            </div>
                        )}
                        {createOk && (
                            <div className="mt-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm text-emerald-700">
                                {createOk}
                            </div>
                        )}

                        <form
                            className="mt-4 grid grid-cols-1 gap-3 md:grid-cols-[2fr_1fr] md:items-end"
                            onSubmit={(e) => {
                                e.preventDefault();
                                handleSubmit();
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Submission URL</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    placeholder="https://..."
                                    value={createDraft.url}
                                    onChange={(e) => setCreateDraft((d) => ({ ...d, url: e.target.value }))}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Comment (optional)</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    placeholder={`At least ${MIN_OPTIONAL_TEXT} characters if provided`}
                                    value={createDraft.comment}
                                    onChange={(e) => setCreateDraft((d) => ({ ...d, comment: e.target.value }))}
                                />
                                {createDraft.comment.trim().length > 0 &&
                                    createDraft.comment.trim().length < MIN_OPTIONAL_TEXT && (
                                        <div className="mt-1 text-xs text-red-600">
                                            Comment must be at least {MIN_OPTIONAL_TEXT} characters if you include one.
                                        </div>
                                    )}
                            </div>
                            <div className="md:col-span-2 flex items-center justify-end">
                                <button
                                    type="submit"
                                    disabled={!canSubmit()}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {createMut.isPending ? "Submitting…" : "Submit"}
                                </button>
                            </div>
                        </form>
                    </div>

                    {/* Submissions table (teachers/admin only) */}
                    {canManage && (
                        <div className="mt-6 rounded-2xl border bg-white p-6">
                            <div className="flex items-center justify-between">
                                <h3 className="text-base font-semibold">All submissions</h3>
                                <Link
                                    to={`/courses/${courseId}/assignments`}
                                    className="text-sm underline decoration-transparent hover:decoration-inherit"
                                >
                                    View all assignments
                                </Link>
                            </div>

                            {gradeError && (
                                <div className="mt-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                    {gradeError}
                                </div>
                            )}
                            {gradeOk && (
                                <div className="mt-3 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm text-emerald-700">
                                    {gradeOk}
                                </div>
                            )}
                            {deleteError && (
                                <div className="mt-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                    {deleteError}
                                </div>
                            )}

                            {submissionsQ?.isLoading ? (
                                <div className="mt-4 grid grid-cols-1 gap-2">
                                    {Array.from({ length: 5 }).map((_, i) => (
                                        <div key={i} className="h-12 animate-pulse rounded-xl border bg-gray-100" />
                                    ))}
                                </div>
                            ) : (submissionsQ?.data?.content?.length ?? 0) === 0 ? (
                                <div className="mt-4 rounded-xl border bg-gray-50 p-6 text-center text-gray-600">
                                    No submissions yet.
                                </div>
                            ) : (
                                <div className="mt-4 overflow-x-auto">
                                    <table className="min-w-full text-sm">
                                        <thead>
                                        <tr className="text-left text-gray-600">
                                            <th className="px-3 py-2">Student</th>
                                            <th className="px-3 py-2">URL</th>
                                            <th className="px-3 py-2">Comment</th>
                                            <th className="px-3 py-2">Grade</th>
                                            <th className="px-3 py-2">Feedback</th>
                                            <th className="px-3 py-2" />
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {submissionsQ?.data?.content?.map((s) => {
                                            const g = gradeDraft[s.id] ?? {};
                                            const name =
                                                s.user?.firstName || s.user?.lastName
                                                    ? `${s.user?.firstName ?? ""} ${s.user?.lastName ?? ""}`.trim()
                                                    : s.user?.username || s.user?.email || s.user?.id || "Unknown";
                                            return (
                                                <tr key={s.id} className="border-t">
                                                    <td className="px-3 py-2">{name}</td>
                                                    <td className="px-3 py-2 max-w-[320px]">
                                                        <a
                                                            className="truncate underline decoration-transparent hover:decoration-inherit"
                                                            href={s.submissionUrl}
                                                            target="_blank"
                                                            rel="noreferrer"
                                                            title={s.submissionUrl}
                                                        >
                                                            {s.submissionUrl}
                                                        </a>
                                                    </td>
                                                    <td className="px-3 py-2">{s.comment ?? ""}</td>
                                                    <td className="px-3 py-2">
                                                        <input
                                                            type="number"
                                                            min={0}
                                                            max={assignment.totalPoints ?? 100}
                                                            step="0.5"
                                                            className="w-24 rounded border px-2 py-1"
                                                            value={g.grade ?? (s.grade ?? "")}
                                                            onChange={(e) =>
                                                                setGradeDraft((prev) => ({
                                                                    ...prev,
                                                                    [s.id]: { ...prev[s.id], grade: e.target.value },
                                                                }))
                                                            }
                                                        />
                                                    </td>
                                                    <td className="px-3 py-2">
                                                        <input
                                                            className="w-56 rounded border px-2 py-1"
                                                            placeholder={`Optional, min ${MIN_OPTIONAL_TEXT} chars`}
                                                            value={g.feedback ?? (s.feedback ?? "")}
                                                            onChange={(e) =>
                                                                setGradeDraft((prev) => ({
                                                                    ...prev,
                                                                    [s.id]: { ...prev[s.id], feedback: e.target.value },
                                                                }))
                                                            }
                                                        />
                                                    </td>
                                                    <td className="px-3 py-2">
                                                        <div className="flex items-center gap-2">
                                                            <button
                                                                className="rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50 disabled:opacity-60"
                                                                onClick={() => {
                                                                    const raw = gradeDraft[s.id]?.grade ?? `${s.grade ?? ""}`;
                                                                    const num = raw === "" ? undefined : parseFloat(String(raw));
                                                                    if (num != null && Number.isNaN(num)) {
                                                                        setGradeError("Please enter a valid grade.");
                                                                        return;
                                                                    }
                                                                    const fb = gradeDraft[s.id]?.feedback;
                                                                    const payload: { id: string; grade?: number; feedback?: string } = { id: s.id };
                                                                    if (num != null) payload.grade = num;
                                                                    if ((fb || "").trim().length >= MIN_OPTIONAL_TEXT) payload.feedback = fb!.trim();
                                                                    gradeMut.mutate(payload);
                                                                }}
                                                                disabled={gradeMut.isPending}
                                                            >
                                                                {gradeMut.isPending ? "Saving…" : "Save grade"}
                                                            </button>
                                                            <button
                                                                className="rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50"
                                                                onClick={() => {
                                                                    setEditMetaId(s.id);
                                                                    setEditMetaDraft({ url: s.submissionUrl, comment: s.comment ?? "" });
                                                                    setEditError(null);
                                                                }}
                                                            >
                                                                Edit
                                                            </button>
                                                            <button
                                                                className="rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50 disabled:opacity-60"
                                                                onClick={() => deleteMut.mutate(s.id)}
                                                                disabled={deleteMut.isPending}
                                                            >
                                                                Delete
                                                            </button>
                                                        </div>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            {/* Edit submission modal */}
            {canManage && editMetaId && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Edit submission</h2>
                            <button
                                onClick={() => setEditMetaId(null)}
                                className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50"
                            >
                                Close
                            </button>
                        </div>

                        {editError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                {editError}
                            </div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                setEditError(null);
                                const url = (editMetaDraft.url || "").trim();
                                if (!/^https?:\/\/\S+$/i.test(url)) {
                                    setEditError("Please provide a valid URL (starting with http or https).");
                                    return;
                                }
                                const c = (editMetaDraft.comment || "").trim();
                                updateMetaMut.mutate({
                                    id: editMetaId,
                                    submissionUrl: url,
                                    comment: c.length >= MIN_OPTIONAL_TEXT ? c : undefined,
                                });
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Submission URL</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editMetaDraft.url}
                                    onChange={(e) => setEditMetaDraft((d) => ({ ...d, url: e.target.value }))}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Comment (optional)</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editMetaDraft.comment}
                                    onChange={(e) => setEditMetaDraft((d) => ({ ...d, comment: e.target.value }))}
                                    placeholder={`At least ${MIN_OPTIONAL_TEXT} characters if provided`}
                                />
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button
                                    type="button"
                                    onClick={() => setEditMetaId(null)}
                                    className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={updateMetaMut.isPending}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {updateMetaMut.isPending ? "Saving…" : "Save"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
