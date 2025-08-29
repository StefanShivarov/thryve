import { useMemo, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";

type Course = {
    id: number | string;
    title: string;
    description?: string;
    imageUrl?: string;
    category?: string;
    level?: "Beginner" | "Intermediate" | "Advanced" | string;
};
type Resource = { id: number | string; name: string; url: string };
type Section = { id: number | string; title: string; textContent?: string; orderNumber: number; resources?: Resource[] };
type Assignment = { id: number | string; title: string; description?: string; deadline?: string; totalPoints?: number };
type Page<T> = { content: T[]; number: number; size: number; totalElements: number; totalPages: number; first: boolean; last: boolean };
type User = { id: string; email: string; username?: string; firstName?: string; lastName?: string };

export default function CourseDetail() {
    const { id } = useParams<{ id: string }>();
    const [tab, setTab] = useState<"overview" | "sections" | "assignments">("overview");
    const queryClient = useQueryClient();

    const courseQ = useQuery({
        queryKey: ["course", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${id}`);
            return (data?.data ?? data) as Course;
        },
        enabled: !!id,
    });

    const sectionsQ = useQuery({
        queryKey: ["sections", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${id}/sections`, {
                params: { pageNumber: 0, pageSize: 100, sortBy: "orderNumber", direction: "ASC" },
            });
            return (data?.data ?? data) as Page<Section>;
        },
        enabled: !!id,
    });

    const assignmentsQ = useQuery({
        queryKey: ["assignments", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${id}/assignments`, {
                params: { pageNumber: 0, pageSize: 50, sortBy: "deadline", direction: "ASC" },
            });
            return (data?.data ?? data) as Page<Assignment>;
        },
        enabled: !!id,
    });

    const course = courseQ.data;
    const sections = sectionsQ.data?.content ?? [];
    const assignments = assignmentsQ.data?.content ?? [];
    const lessonCount = sections.length;
    const assignmentCount = assignments.length;

    const handleShare = async () => {
        const url = window.location.href;
        try {
            if (navigator.share) {
                await navigator.share({ title: course?.title ?? "Course", url });
                return;
            }
            await navigator.clipboard.writeText(url);
            alert("Link copied to clipboard!");
        } catch {
            const ta = document.createElement("textarea");
            ta.value = url;
            document.body.appendChild(ta);
            ta.select();
            document.execCommand("copy");
            ta.remove();
            alert("Link copied to clipboard!");
        }
    };

    const enrollMutation = useMutation({
        mutationFn: async () => {
            let me: User | undefined;
            try {
                const meRes = await api.get("/api/users/me");
                me = (meRes.data?.data ?? meRes.data) as User;
            } catch {}
            if (!me?.id) {
                const token = localStorage.getItem("accessToken") ?? "";
                const email = token.split(".")[1] ? JSON.parse(atob(token.split(".")[1]))?.sub : undefined;
                const usersRes = await api.get("/api/users", {
                    params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
                });
                const page = usersRes.data?.data ?? usersRes.data;
                const list: User[] = page?.content ?? page ?? [];
                me = list.find((u) => u.email === email);
            }
            if (!me?.id) throw new Error("USER_NOT_FOUND");
            const { data } = await api.post(`/api/enrollment-requests`, { userId: me.id, courseId: id });
            return data?.data ?? data;
        },
        onSuccess: () => {
            alert("Request sent! An instructor/admin will review it.");
            queryClient.invalidateQueries({ queryKey: ["course-requests", id] });
        },
        onError: (err: any) => {
            const status = err?.response?.status;
            const msg =
                err?.response?.data?.message ||
                (status === 401
                    ? "Please sign in again."
                    : status === 403
                        ? "You don’t have permission to request enrollment for this course."
                        : status === 409
                            ? "You’ve already requested enrollment."
                            : "An unexpected error occurred!");
            alert(msg);
        },
    });

    const handleEnrollClick = () => enrollMutation.mutate();

    const gradients = [
        "from-indigo-500 via-blue-500 to-purple-500",
        "from-fuchsia-500 via-pink-500 to-rose-500",
        "from-emerald-500 via-teal-500 to-cyan-500",
        "from-amber-500 via-orange-500 to-red-500",
    ];
    const gradient = gradients[
        useMemo(() => {
            const s = String(id ?? "0");
            let sum = 0;
            for (let i = 0; i < s.length; i++) sum += s.charCodeAt(i);
            return sum % gradients.length;
        }, [id])
        ];

    const Badge = ({ children }: { children: React.ReactNode }) => (
        <span className="inline-flex items-center rounded-full bg-black/5 px-2.5 py-1 text-xs font-medium text-gray-800">{children}</span>
    );
    const Pill = ({ active, children, onClick }: { active: boolean; children: React.ReactNode; onClick: () => void }) => (
        <button onClick={onClick} className={"rounded-full px-4 py-2 text-sm font-medium transition " + (active ? "bg-black text-white shadow" : "bg-white text-gray-700 border hover:bg-gray-50")}>{children}</button>
    );

    if (courseQ.isLoading) {
        return (
            <div className="p-6">
                <div className="h-40 animate-pulse rounded-3xl bg-gray-200" />
                <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-3">
                    {Array.from({ length: 6 }).map((_, i) => (
                        <div key={i} className="h-28 animate-pulse rounded-2xl border bg-white" />
                    ))}
                </div>
            </div>
        );
    }
    if (courseQ.error || !course) {
        return (
            <div className="p-6">
                <div className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700">
                    Couldn’t load this course. <Link to="/courses" className="underline">Back to Courses</Link>
                </div>
            </div>
        );
    }

    return (
        <div className="pb-10">
            <div className="relative">
                <div className={`h-40 w-full bg-gradient-to-br ${gradient}`} />
                {course.imageUrl && (
                    <img
                        src={course.imageUrl}
                        alt=""
                        className="absolute inset-0 h-40 w-full object-cover opacity-25 mix-blend-multiply"
                        onError={(e) => {
                            (e.currentTarget as HTMLImageElement).style.display = "none";
                        }}
                    />
                )}
                <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,0.25)_90%)]" />
                <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                    <div className="mb-2 text-sm opacity-90">
                        <Link to="/courses" className="underline underline-offset-2">← Back to Courses</Link>
                    </div>
                    <h1 className="text-2xl font-semibold">{course.title}</h1>
                    <div className="mt-2 flex flex-wrap items-center gap-2">
                        {course.category && <Badge>{course.category}</Badge>}
                        {course.level && <Badge>{course.level}</Badge>}
                        <Badge>{lessonCount} lessons</Badge>
                        <Badge>{assignmentCount} assignments</Badge>
                    </div>
                </div>
            </div>

            <div className="px-6">
                <div className="mt-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                    <div className="flex items-center gap-2">
                        <Pill active={tab === "overview"} onClick={() => setTab("overview")}>Overview</Pill>
                        <Pill active={tab === "sections"} onClick={() => setTab("sections")}>Sections</Pill>
                        <Pill active={tab === "assignments"} onClick={() => setTab("assignments")}>Assignments</Pill>
                    </div>

                    <div className="flex items-center gap-2">
                        <Link to="/courses" className="rounded-lg border bg-white px-4 py-2 text-sm font-medium hover:bg-gray-50">All Courses</Link>
                        <Link to={`/courses/${id}/requests`} className="rounded-lg border bg-white px-4 py-2 text-sm font-medium hover:bg-gray-50">Requests</Link>
                        <button
                            className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white shadow hover:opacity-90 disabled:opacity-60"
                            onClick={handleEnrollClick}
                            disabled={enrollMutation.isPending}
                        >
                            {enrollMutation.isPending ? "Requesting…" : "Request access"}
                        </button>
                    </div>
                </div>

                {tab === "overview" && (
                    <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-[2fr_1fr]">
                        <div className="rounded-2xl border bg-white p-6">
                            <h2 className="text-lg font-semibold">About this course</h2>
                            <p className="mt-2 text-gray-700 whitespace-pre-wrap">{course.description || "No description available."}</p>
                        </div>

                        <div className="space-y-6">
                            <div className="rounded-2xl border bg-white p-6">
                                <h3 className="text-sm font-semibold text-gray-700">Stats</h3>
                                <div className="mt-3 grid grid-cols-2 gap-3 text-sm">
                                    <div className="rounded-xl border bg-gray-50 p-3">
                                        <div className="text-gray-500">Lessons</div>
                                        <div className="text-lg font-semibold">{lessonCount}</div>
                                    </div>
                                    <div className="rounded-xl border bg-gray-50 p-3">
                                        <div className="text-gray-500">Assignments</div>
                                        <div className="text-lg font-semibold">{assignmentCount}</div>
                                    </div>
                                </div>
                            </div>

                            <div className="rounded-2xl border bg-white p-6">
                                <h3 className="text-sm font-semibold text-gray-700">Actions</h3>
                                <div className="mt-3 flex flex-wrap gap-2">
                                    <button className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50" onClick={handleShare}>Share</button>
                                    <button
                                        className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50 disabled:opacity-60"
                                        onClick={handleEnrollClick}
                                        disabled={enrollMutation.isPending}
                                    >
                                        {enrollMutation.isPending ? "Requesting…" : "Request access"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {tab === "sections" && (
                    <div className="mt-6 space-y-4">
                        {sectionsQ.isLoading ? (
                            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                {Array.from({ length: 6 }).map((_, i) => (
                                    <div key={i} className="h-28 animate-pulse rounded-2xl border bg-white" />
                                ))}
                            </div>
                        ) : sections.length === 0 ? (
                            <div className="rounded-2xl border bg-white p-10 text-center text-gray-600">No sections yet.</div>
                        ) : (
                            <ul className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                {sections.map((s) => (
                                    <li key={s.id} className="overflow-hidden rounded-2xl border bg-white shadow-sm">
                                        <div className="flex items-start justify-between p-4">
                                            <div>
                                                <div className="text-sm text-gray-500">Lesson {s.orderNumber}</div>
                                                <div className="mt-1 text-base font-semibold">{s.title}</div>
                                            </div>
                                        </div>
                                        {s.textContent && (
                                            <div className="border-t px-4 py-3 text-sm text-gray-700 whitespace-pre-wrap">{s.textContent}</div>
                                        )}
                                        {s.resources && s.resources.length > 0 && (
                                            <div className="border-t bg-gray-50 px-4 py-3">
                                                <div className="text-xs font-semibold text-gray-600">Resources</div>
                                                <ul className="mt-2 space-y-2">
                                                    {s.resources.map((r) => (
                                                        <li key={r.id}>
                                                            <a href={r.url} target="_blank" rel="noreferrer" className="inline-flex items-center gap-2 rounded-lg border bg-white px-3 py-1.5 text-xs font-medium hover:bg-gray-50">
                                                                <span className="truncate">{r.name}</span>
                                                            </a>
                                                        </li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}

                {tab === "assignments" && (
                    <div className="mt-6">
                        {assignmentsQ.isLoading ? (
                            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                {Array.from({ length: 4 }).map((_, i) => (
                                    <div key={i} className="h-28 animate-pulse rounded-2xl border bg-white" />
                                ))}
                            </div>
                        ) : assignments.length === 0 ? (
                            <div className="rounded-2xl border bg-white p-10 text-center text-gray-600">No assignments yet.</div>
                        ) : (
                            <ul className="space-y-3">
                                {assignments.map((a) => (
                                    <li key={a.id} className="rounded-2xl border bg-white p-4 shadow-sm">
                                        <div className="flex flex-col gap-1 md:flex-row md:items-center md:justify-between">
                                            <div>
                                                <div className="text-base font-semibold">{a.title}</div>
                                                {a.description && <div className="text-sm text-gray-600">{a.description}</div>}
                                            </div>
                                            <div className="mt-2 flex items-center gap-2 md:mt-0">
                                                {a.totalPoints != null && (
                                                    <span className="rounded-full bg-black/5 px-2.5 py-1 text-xs font-medium">{a.totalPoints} pts</span>
                                                )}
                                                {a.deadline && (
                                                    <span className="rounded-full bg-black/5 px-2.5 py-1 text-xs font-medium">Due {new Date(a.deadline).toLocaleString()}</span>
                                                )}
                                                <button className="rounded-lg border bg-white px-3 py-1.5 text-sm font-medium hover:bg-gray-50" onClick={() => alert("TODO: open assignment")}>Open</button>
                                            </div>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
