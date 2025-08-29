import { useParams, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";

type User = { id: string; username?: string; firstName?: string; lastName?: string; email: string };
type CoursePreview = { id: string; title: string };
type EnrollmentRequest = {
    id: string;
    state: "PENDING" | "APPROVED" | "REJECTED";
    user: User;
    course: CoursePreview;
};

type Page<T> = {
    content: T[];
    number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean;
};

export default function CourseEnrollmentRequests() {
    const { id } = useParams<{ id: string }>();
    const queryClient = useQueryClient();

    const listQ = useQuery({
        queryKey: ["course-requests", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${id}/enrollment-requests`, {
                params: { pageNumber: 0, pageSize: 50, sortBy: "id", direction: "DESC" },
            });
            return (data?.data ?? data) as Page<EnrollmentRequest>;
        },
        enabled: !!id,
    });

    const acceptMutation = useMutation({
        mutationFn: async (reqId: string) => {
            const { data } = await api.post(`/api/enrollment-requests/${reqId}/accept`, {});
            return data?.data ?? data;
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course-requests", id] }),
    });

    const rejectMutation = useMutation({
        mutationFn: async (reqId: string) => {
            const { data } = await api.post(`/api/enrollment-requests/${reqId}/reject`, {});
            return data?.data ?? data;
        },
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ["course-requests", id] }),
    });

    if (listQ.isLoading) {
        return (
            <div className="p-6">
                <div className="h-8 w-48 animate-pulse rounded bg-gray-200" />
                <div className="mt-4 space-y-2">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="h-16 animate-pulse rounded-2xl border bg-white" />
                    ))}
                </div>
            </div>
        );
    }

    if (listQ.error) {
        return (
            <div className="p-6">
                <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
                    Unable to load requests (you might need CREATOR/ADMIN).{" "}
                    <Link to={`/courses/${id}`} className="underline">Back to Course</Link>
                </div>
            </div>
        );
    }

    const rows = listQ.data?.content ?? [];

    return (
        <div className="p-6">
            <div className="mb-4 flex items-center justify-between">
                <h1 className="text-2xl font-semibold">Enrollment Requests</h1>
                <Link to={`/courses/${id}`} className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">
                    Back to Course
                </Link>
            </div>

            {rows.length === 0 ? (
                <div className="rounded-2xl border bg-white p-10 text-center text-gray-600">
                    No requests yet.
                </div>
            ) : (
                <div className="overflow-hidden rounded-2xl border bg-white">
                    <table className="w-full text-sm">
                        <thead className="bg-gray-50">
                        <tr className="[&>th]:px-4 [&>th]:py-2 text-left text-gray-600">
                            <th>User</th>
                            <th>Email</th>
                            <th>Status</th>
                            <th className="text-right">Actions</th>
                        </tr>
                        </thead>
                        <tbody>
                        {rows.map((r) => (
                            <tr key={r.id} className="border-t">
                                <td className="[&>div]:px-4 [&>div]:py-2">
                                    <div className="font-medium">{r.user.username || `${r.user.firstName ?? ""} ${r.user.lastName ?? ""}`.trim() || "User"}</div>
                                </td>
                                <td className="px-4 py-2 text-gray-600">{r.user.email}</td>
                                <td className="px-4 py-2">
                                    <span className="rounded-full bg-black/5 px-2.5 py-1 text-xs font-medium">{r.state}</span>
                                </td>
                                <td className="px-4 py-2">
                                    <div className="flex justify-end gap-2">
                                        <button
                                            disabled={acceptMutation.isPending}
                                            className="rounded-lg border px-3 py-1.5 text-sm hover:bg-gray-50 disabled:opacity-60"
                                            onClick={() => acceptMutation.mutate(r.id)}
                                        >
                                            Accept
                                        </button>
                                        <button
                                            disabled={rejectMutation.isPending}
                                            className="rounded-lg border px-3 py-1.5 text-sm hover:bg-gray-50 disabled:opacity-60"
                                            onClick={() => rejectMutation.mutate(r.id)}
                                        >
                                            Reject
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
    );
}
