import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { api } from "../../lib/api";

type Resource = { id: string | number; name: string; url: string };
type Page<T> = { content: T[]; totalElements?: number };

export default function SectionResources({
                                             sectionId,
                                             canManage,
                                         }: {
    sectionId: string | number;
    canManage: boolean;
}) {
    const qc = useQueryClient();
    const [newRes, setNewRes] = useState({ name: "", url: "" });
    const [editingId, setEditingId] = useState<string | number | null>(null);
    const [editForm, setEditForm] = useState({ name: "", url: "" });

    const listQ = useQuery({
        queryKey: ["resources", sectionId],
        queryFn: async () => {
            const { data } = await api.get(`/api/sections/${sectionId}/resources`, {
                params: { pageNumber: 0, pageSize: 200 },
            });
            const raw = data?.data ?? data;
            return (Array.isArray(raw) ? raw : (raw as Page<Resource>).content) as Resource[];
        },
    });

    const createMut = useMutation({
        mutationFn: async (payload: { name: string; url: string }) => {
            const { data } = await api.post(`/api/sections/${sectionId}/resources`, payload);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setNewRes({ name: "", url: "" });
            qc.invalidateQueries({ queryKey: ["resources", sectionId] });
        },
    });

    const updateMut = useMutation({
        mutationFn: async (args: { id: string | number; payload: Partial<Resource> }) => {
            const { data } = await api.patch(`/api/resources/${args.id}`, args.payload);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setEditingId(null);
            qc.invalidateQueries({ queryKey: ["resources", sectionId] });
        },
    });

    const deleteMut = useMutation({
        mutationFn: async (id: string | number) => {
            const { data } = await api.delete(`/api/resources/${id}`);
            return data?.data ?? data;
        },
        onSuccess: () => qc.invalidateQueries({ queryKey: ["resources", sectionId] }),
    });

    return (
        <div className="border-t bg-gray-50 px-4 py-3">
            <div className="mb-2 text-xs font-semibold text-gray-600">Resources</div>

            {listQ.isLoading && <div className="h-16 animate-pulse rounded-xl border bg-white" />}
            {listQ.error && (
                <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                    Failed to load resources.
                </div>
            )}

            {listQ.data && listQ.data.length === 0 && (
                <div className="text-xs text-gray-600">No resources yet.</div>
            )}

            {listQ.data && listQ.data.length > 0 && (
                <ul className="space-y-2">
                    {listQ.data.map((r) => (
                        <li key={r.id} className="flex items-center justify-between rounded-lg border bg-white px-3 py-2">
                            {editingId === r.id ? (
                                <div className="flex w-full items-center gap-2">
                                    <input
                                        className="w-40 rounded border px-2 py-1 text-sm"
                                        value={editForm.name}
                                        onChange={(e) => setEditForm((f) => ({ ...f, name: e.target.value }))}
                                        placeholder="Name"
                                    />
                                    <input
                                        className="flex-1 rounded border px-2 py-1 text-sm"
                                        value={editForm.url}
                                        onChange={(e) => setEditForm((f) => ({ ...f, url: e.target.value }))}
                                        placeholder="https://…"
                                    />
                                    <button
                                        className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50"
                                        onClick={() => updateMut.mutate({ id: r.id, payload: { name: editForm.name, url: editForm.url } })}
                                        disabled={updateMut.isPending}
                                    >
                                        Save
                                    </button>
                                    <button
                                        className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50"
                                        onClick={() => {
                                            setEditingId(null);
                                            setEditForm({ name: "", url: "" });
                                        }}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            ) : (
                                <>
                                    <a href={r.url} target="_blank" rel="noreferrer" className="truncate text-sm font-medium">
                                        {r.name}
                                    </a>
                                    {canManage && (
                                        <div className="flex items-center gap-2">
                                            <button
                                                className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50"
                                                onClick={() => {
                                                    setEditingId(r.id);
                                                    setEditForm({ name: r.name, url: r.url });
                                                }}
                                            >
                                                Edit
                                            </button>
                                            <button
                                                className="rounded-lg border bg-white px-2 py-1 text-xs hover:bg-gray-50 disabled:opacity-60"
                                                onClick={() => deleteMut.mutate(r.id)}
                                                disabled={deleteMut.isPending}
                                            >
                                                Delete
                                            </button>
                                        </div>
                                    )}
                                </>
                            )}
                        </li>
                    ))}
                </ul>
            )}

            {canManage && (
                <form
                    className="mt-3 flex flex-col gap-2 rounded-lg border bg-white p-3 md:flex-row md:items-center"
                    onSubmit={(e) => {
                        e.preventDefault();
                        if (!newRes.name.trim() || !newRes.url.trim()) return;
                        createMut.mutate({ name: newRes.name.trim(), url: newRes.url.trim() });
                    }}
                >
                    <input
                        className="w-full rounded border px-2 py-1 text-sm md:w-44"
                        placeholder="Name"
                        value={newRes.name}
                        onChange={(e) => setNewRes((f) => ({ ...f, name: e.target.value }))}
                    />
                    <input
                        className="w-full flex-1 rounded border px-2 py-1 text-sm"
                        placeholder="https://…"
                        value={newRes.url}
                        onChange={(e) => setNewRes((f) => ({ ...f, url: e.target.value }))}
                    />
                    <button
                        type="submit"
                        className="rounded-lg bg-black px-3 py-1.5 text-xs font-medium text-white disabled:opacity-60"
                        disabled={createMut.isPending}
                    >
                        {createMut.isPending ? "Adding…" : "Add resource"}
                    </button>
                </form>
            )}
        </div>
    );
}
