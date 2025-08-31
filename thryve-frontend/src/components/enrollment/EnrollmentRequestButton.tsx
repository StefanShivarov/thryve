import { useEffect, useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../../lib/api";
import { hasAnyRole } from "../../lib/auth";

type Page<T> = {
    content: T[];
    number: number;
    size: number;
    totalElements: number;
    totalPages: number;
    first: boolean;
    last: boolean;
};

type User = { id: string; email?: string };

type EnrollmentRequest = {
    id: string;
    state?: "PENDING" | "ACCEPTED" | "REJECTED";
    course?: { id?: string | number };
    courseId?: string | number;
};

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
        const res = await api.get("/api/users", {
            params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
        });
        const page: Page<User> = res.data?.data ?? res.data;
        const found = (page?.content || []).find((u) => u.email === email);
        if (found?.id) {
            localStorage.setItem("userId", found.id);
            return found.id;
        }
    } catch {}
    return null;
}

export default function EnrollmentRequestButton({ courseId }: { courseId: string | number }) {
    const canManage = hasAnyRole("CREATOR", "ADMIN");
    const qc = useQueryClient();
    const [userId, setUserId] = useState<string | null>(null);
    const [signal, setSignal] = useState<{ kind: "ok" | "err"; text: string } | null>(null);

    useEffect(() => {
        resolveUserId().then(setUserId);
    }, []);

    const myReqQ = useQuery({
        queryKey: ["my-enrollment-requests", userId],
        queryFn: async () => {
            const { data } = await api.get(`/api/users/${userId}/enrollment-requests`, {
                params: { pageNumber: 0, pageSize: 500, sortBy: "id", direction: "DESC" },
            });
            return (data?.data ?? data) as Page<EnrollmentRequest>;
        },
        enabled: !!userId,
        staleTime: 10_000,
    });

    const existingForCourse = useMemo(() => {
        const items = myReqQ.data?.content ?? [];
        return items.find((r) => String(r.courseId ?? r.course?.id) === String(courseId));
    }, [myReqQ.data, courseId]);

    const isPending = existingForCourse?.state === "PENDING";
    const isAccepted = existingForCourse?.state === "ACCEPTED";

    const createMut = useMutation({
        mutationFn: async () => {
            if (!userId) throw { response: { status: 401 } };
            const { data } = await api.post(
                `/api/courses/${courseId}/enrollment-requests`,
                null,
                { params: { userId } }
            );
            return data?.data ?? data;
        },
        onSuccess: () => {
            setSignal({ kind: "ok", text: "Request sent." });
            qc.invalidateQueries({ queryKey: ["my-enrollment-requests", userId] });
            setTimeout(() => setSignal(null), 2500);
        },
        onError: (e: any) => {
            const s = e?.response?.status;
            const msg =
                e?.response?.data?.message ||
                (s === 401
                    ? "Please sign in."
                    : s === 403
                        ? "Not allowed."
                        : s === 409
                            ? "Request already exists."
                            : "Failed to send request.");
            setSignal({ kind: "err", text: msg });
            setTimeout(() => setSignal(null), 3000);
        },
    });

    if (canManage) return null;

    const disabled = !userId || createMut.isPending || isPending || isAccepted;

    return (
        <div className="flex flex-col items-end gap-1">
            <button
                type="button"
                onClick={() => createMut.mutate()}
                disabled={disabled}
                className={
                    "rounded-lg px-4 py-2 text-sm font-medium text-white shadow focus:outline-none disabled:opacity-60 " +
                    (disabled ? "bg-violet-400" : "bg-violet-600 hover:bg-violet-700")
                }
                title={
                    isAccepted ? "Already enrolled." : isPending ? "Request pending." : "Request enrollment."
                }
            >
                {isAccepted ? "Enrolled" : isPending ? "Pending…" : createMut.isPending ? "Sending…" : "Request enrollment"}
            </button>

            {signal && (
                <div
                    className={
                        "text-xs " +
                        (signal.kind === "ok" ? "text-emerald-700" : "text-red-700")
                    }
                >
                    {signal.text}
                </div>
            )}
        </div>
    );
}
