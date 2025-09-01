import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/api";
import { hasAnyRole } from "../../lib/auth";

export type Assignment = {
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

/** Convert API ISO -> <input type="datetime-local"> value (yyyy-MM-ddTHH:mm) */
function toInputValue(iso?: string): string {
    if (!iso) return "";
    const match = iso.match(
        /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::\d{2}(?:\.\d{1,9})?)?$/
    );
    if (match) {
        const [, y, MM, dd, HH, mm] = match;
        return `${y}-${MM}-${dd}T${HH}:${mm}`;
    }
    const d = new Date(iso);
    if (Number.isNaN(d.getTime())) return "";
    const pad = (n: number) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
        d.getHours()
    )}:${pad(d.getMinutes())}`;
}

/** Ensure we send exactly yyyy-MM-dd'T'HH:mm:ss (no timezone, no millis) */
function toApiLocalDateTimeSeconds(value: string): string {
    if (!value) return "";
    // Typical value from datetime-local is yyyy-MM-ddTHH:mm
    const m1 = value.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})$/);
    if (m1) return `${m1[1]}:00`;

    const m2 = value.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})$/);
    if (m2) return m2[1];

    const stripped = value.replace(/Z|[+\-]\d{2}:\d{2}$/, "");
    const m3 = stripped.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})$/);
    if (m3) return `${m3[1]}:00`;
    const m4 = stripped.match(/^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})$/);
    if (m4) return m4[1];

    const d = new Date(value);
    if (!Number.isNaN(d.getTime())) {
        const pad = (n: number) => String(n).padStart(2, "0");
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(
            d.getHours()
        )}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
    }
    throw new Error("BAD_DATETIME");
}

export default function AssignmentsPanel({ courseId }: { courseId: string }) {
    const canManage = hasAnyRole("CREATOR", "ADMIN");
    const qc = useQueryClient();
    const navigate = useNavigate();

    const [showCreate, setShowCreate] = useState(false);
    const [showEdit, setShowEdit] = useState(false);
    const [draft, setDraft] = useState<Partial<Assignment>>({
        title: "",
        description: "",
        deadline: "",
        totalPoints: 100,
    });
    const [editing, setEditing] = useState<Assignment | null>(null);

    const q = useQuery({
        queryKey: ["assignments", courseId],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${courseId}/assignments`, {
                params: { pageNumber: 0, pageSize: 100, sortBy: "deadline", direction: "ASC" },
            });
            return (data?.data ?? data) as Page<Assignment>;
        },
    });


    const createMut = useMutation({
        mutationFn: async (payload: {
            title: string;
            description: string;
            deadline: string; // yyyy-MM-ddTHH:mm:ss
            totalPoints: number;
        }) => {
            const { data } = await api.post(`/api/courses/${courseId}/assignments`, payload);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setShowCreate(false);
            setDraft({ title: "", description: "", deadline: "", totalPoints: 100 });
            qc.invalidateQueries({ queryKey: ["assignments", courseId] });
        },
    });

    const updateMut = useMutation({
        mutationFn: async (args: { id: string; patch: Partial<Assignment> }) => {
            const body: any = {};
            if (args.patch.title != null) body.title = args.patch.title;
            if (args.patch.description != null) body.description = args.patch.description;
            if (args.patch.deadline != null) body.deadline = args.patch.deadline;
            if (args.patch.totalPoints != null) body.totalPoints = args.patch.totalPoints;
            const { data } = await api.patch(`/api/assignments/${args.id}`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setShowEdit(false);
            setEditing(null);
            qc.invalidateQueries({ queryKey: ["assignments", courseId] });
        },
    });

    const deleteMut = useMutation({
        mutationFn: async (id: string) => {
            const { data } = await api.delete(`/api/assignments/${id}`);
            return data?.data ?? data;
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["assignments", courseId] }),
    });

    const items = q.data?.content ?? [];

    return (
        <div className="mt-6">
            {canManage && (
                <div className="mb-3">
                    <button
                        className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white shadow hover:opacity-90"
                        onClick={() => {
                            setDraft({ title: "", description: "", deadline: "", totalPoints: 100 });
                            setShowCreate(true);
                        }}
                    >
                        + Add assignment
                    </button>
                </div>
            )}

            {q.isLoading ? (
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                    {Array.from({ length: 4 }).map((_, i) => (
                        <div key={i} className="h-28 animate-pulse rounded-2xl border bg-white" />
                    ))}
                </div>
            ) : items.length === 0 ? (
                <div className="rounded-2xl border bg-white p-10 text-center text-gray-600">
                    No assignments yet.
                </div>
            ) : (
                <ul className="space-y-3">
                    {items.map((a) => (
                        <li key={a.id} className="rounded-2xl border bg-white p-4 shadow-sm">
                            <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                                <div className="min-w-0">
                                    <Link
                                        to={`/courses/${courseId}/assignments/${a.id}`}
                                        className="text-base font-semibold truncate underline decoration-transparent hover:decoration-inherit"
                                    >
                                        {a.title}
                                    </Link>
                                    {a.description && (
                                        <div className="text-sm text-gray-600 line-clamp-2">{a.description}</div>
                                    )}
                                    <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-gray-500">
                                        {a.totalPoints != null && (
                                            <span className="rounded-full bg-black/5 px-2.5 py-1 font-medium">
                        {a.totalPoints} pts
                      </span>
                                        )}
                                        {a.deadline && (
                                            <span className="rounded-full bg-black/5 px-2.5 py-1 font-medium">
                        Due {new Date(a.deadline).toLocaleString()}
                      </span>
                                        )}
                                    </div>
                                </div>

                                <div className="mt-2 flex shrink-0 items-center gap-2 md:mt-0">
                                    <button
                                        className="rounded-lg border bg-white px-3 py-1.5 text-sm font-medium hover:bg-gray-50"
                                        onClick={() =>
                                            navigate(`/courses/${courseId}/assignments/${a.id}`)
                                        }
                                    >
                                        Open
                                    </button>

                                    {canManage && (
                                        <>
                                            <button
                                                className="rounded-lg border bg-white px-3 py-1.5 text-sm font-medium hover:bg-gray-50"
                                                onClick={() => {
                                                    setEditing(a);
                                                    setDraft({
                                                        title: a.title,
                                                        description: a.description,
                                                        deadline: toInputValue(a.deadline),
                                                        totalPoints: a.totalPoints,
                                                    });
                                                    setShowEdit(true);
                                                }}
                                            >
                                                Edit
                                            </button>
                                            <button
                                                className="rounded-lg border bg-white px-3 py-1.5 text-sm font-medium hover:bg-gray-50 disabled:opacity-60"
                                                onClick={() => {
                                                    if (confirm("Delete this assignment?")) deleteMut.mutate(a.id);
                                                }}
                                                disabled={deleteMut.isPending}
                                            >
                                                Delete
                                            </button>
                                        </>
                                    )}
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            )}

            {showCreate && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Add assignment</h2>
                            <button
                                onClick={() => setShowCreate(false)}
                                className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50"
                            >
                                Close
                            </button>
                        </div>

                        {createMut.isError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                Failed to create assignment.
                            </div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                try {
                                    const title = (draft.title ?? "").trim();
                                    const description = (draft.description ?? "").trim();
                                    const deadline = toApiLocalDateTimeSeconds(draft.deadline || "");
                                    const totalPoints = Number(draft.totalPoints ?? 0);
                                    if (!title || !description || !deadline) return;
                                    createMut.mutate({ title, description, deadline, totalPoints });
                                } catch {
                                    alert("Please select a valid deadline.");
                                }
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Title</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={draft.title ?? ""}
                                    onChange={(e) => setDraft((d) => ({ ...d, title: e.target.value }))}
                                    minLength={2}
                                    maxLength={100}
                                    required
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium">Description</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={4}
                                    value={draft.description ?? ""}
                                    onChange={(e) => setDraft((d) => ({ ...d, description: e.target.value }))}
                                    minLength={10}
                                    maxLength={1000}
                                    required
                                />
                            </div>

                            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                <div>
                                    <label className="block text-sm font-medium">Deadline</label>
                                    <input
                                        type="datetime-local"
                                        className="mt-1 w-full rounded-lg border px-3 py-2"
                                        value={draft.deadline ?? ""}
                                        onChange={(e) => setDraft((d) => ({ ...d, deadline: e.target.value }))}
                                        required
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium">Total points</label>
                                    <input
                                        type="number"
                                        min={1}
                                        step="1"
                                        className="mt-1 w-full rounded-lg border px-3 py-2"
                                        value={draft.totalPoints ?? 100}
                                        onChange={(e) =>
                                            setDraft((d) => ({ ...d, totalPoints: Number(e.target.value) }))
                                        }
                                        required
                                    />
                                </div>
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button
                                    type="button"
                                    onClick={() => setShowCreate(false)}
                                    className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={createMut.isPending}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {createMut.isPending ? "Creating…" : "Create"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showEdit && editing && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Edit assignment</h2>
                            <button
                                onClick={() => setShowEdit(false)}
                                className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50"
                            >
                                Close
                            </button>
                        </div>

                        {updateMut.isError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">
                                Failed to save assignment.
                            </div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                const patch: Partial<Assignment> = {
                                    title: draft.title ?? editing.title,
                                    description: draft.description ?? editing.description,
                                    deadline: draft.deadline
                                        ? toApiLocalDateTimeSeconds(draft.deadline)
                                        : editing.deadline,
                                    totalPoints: draft.totalPoints ?? editing.totalPoints,
                                };
                                updateMut.mutate({ id: editing.id, patch });
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Title</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={draft.title ?? editing.title ?? ""}
                                    onChange={(e) => setDraft((d) => ({ ...d, title: e.target.value }))}
                                    minLength={2}
                                    maxLength={100}
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-medium">Description</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={4}
                                    value={draft.description ?? editing.description ?? ""}
                                    onChange={(e) => setDraft((d) => ({ ...d, description: e.target.value }))}
                                    minLength={10}
                                    maxLength={1000}
                                />
                            </div>

                            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                <div>
                                    <label className="block text-sm font-medium">Deadline</label>
                                    <input
                                        type="datetime-local"
                                        className="mt-1 w-full rounded-lg border px-3 py-2"
                                        value={draft.deadline ?? toInputValue(editing.deadline)}
                                        onChange={(e) => setDraft((d) => ({ ...d, deadline: e.target.value }))}
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-medium">Total points</label>
                                    <input
                                        type="number"
                                        min={1}
                                        step="1"
                                        className="mt-1 w-full rounded-lg border px-3 py-2"
                                        value={draft.totalPoints ?? editing.totalPoints ?? 100}
                                        onChange={(e) =>
                                            setDraft((d) => ({ ...d, totalPoints: Number(e.target.value) }))
                                        }
                                    />
                                </div>
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button
                                    type="button"
                                    onClick={() => {
                                        setShowEdit(false);
                                        setEditing(null);
                                        setDraft({ title: "", description: "", deadline: "", totalPoints: 100 });
                                    }}
                                    className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={updateMut.isPending}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {updateMut.isPending ? "Saving…" : "Save"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
