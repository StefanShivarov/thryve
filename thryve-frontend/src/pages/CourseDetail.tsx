import { useMemo, useState, useEffect } from "react";
import { useParams, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import AppHeader from "../components/AppHeader";
import { hasAnyRole } from "../lib/auth";
import SectionCard from "../components/sections/SectionCard";
import AssignmentsPanel from "../components/assignments/AssignmentsPanel";

export type Course = {
    id: number | string;
    title: string;
    description?: string;
    imageUrl?: string;
};

type Section = { id: number | string; title: string; textContent?: string; orderNumber: number };
type Assignment = { id: number | string; title: string; description?: string; deadline?: string; totalPoints?: number };

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

async function resolveUserIdFresh(): Promise<string | null> {

    try {
        const meRes = await api.get("/api/users/me");
        const me: User = meRes.data?.data ?? meRes.data;
        if (me?.id) return me.id;
    } catch {}

    const email = getTokenEmail();
    if (!email) return null;
    try {
        const res = await api.get("/api/users", {
            params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
        });
        const page: Page<User> = res.data?.data ?? res.data;
        const found = (page?.content || []).find((u) => u.email === email);
        return found?.id ?? null;
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
    const fresh = await resolveUserIdFresh();
    if (fresh) localStorage.setItem(cacheKey, fresh);
    return fresh;
}

function EnrollmentRequestButton({ courseId }: { courseId: string | number }) {
    const canManage = hasAnyRole("CREATOR", "ADMIN");
    const qc = useQueryClient();
    const [userId, setUserId] = useState<string | null>(null);
    const [signal, setSignal] = useState<{ kind: "ok" | "err"; text: string } | null>(null);


    useEffect(() => {
        resolveUserId().then(setUserId);
    }, []);


    useEffect(() => {
        const i = setInterval(() => {
            resolveUserId().then((uid) => setUserId((prev) => (prev !== uid ? uid : prev)));
        }, 5000);
        return () => clearInterval(i);
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

    const enrollmentsQ = useQuery({
        queryKey: ["my-enrollments", userId, courseId],
        queryFn: async () => {
            const { data } = await api.get(`/api/enrollments`, {
                params: { userId, courseId },
            });
            return data?.data ?? data;
        },
        enabled: !!userId && !!courseId,
        staleTime: 10_000,
    })

    const existingForCourse = useMemo(() => {
        const items = myReqQ.data?.content ?? [];
        return items.find((r) => String(r.courseId ?? r.course?.id) === String(courseId));
    }, [myReqQ.data, courseId]);

    const isPending = existingForCourse?.state === "PENDING";
    const isAccepted = existingForCourse?.state === "ACCEPTED" || (enrollmentsQ.data?.content ?? []).length > 0;

    const createMut = useMutation({
        mutationFn: async () => {
            const currentUserId = await resolveUserId();
            if (!currentUserId) throw { response: { status: 401 } };
            const { data } = await api.post(
                `/api/courses/${courseId}/enrollment-requests`,
                null,
                { params: { userId: currentUserId } }
            );
            return data?.data ?? data;
        },
        onSuccess: () => {
            setSignal({ kind: "ok", text: "Request sent." });
            qc.invalidateQueries({ queryKey: ["my-enrollment-requests", userId!] });
            setTimeout(() => setSignal(null), 2500);
        },
        onError: (e: any) => {
            const s = e?.response?.status;
            const msg =
                e?.response?.data?.message ||
                (s === 401 ? "Please sign in." : s === 403 ? "Not allowed." : s === 409 ? "Request already exists." : "Failed to send request.");
            setSignal({ kind: "err", text: msg });
            setTimeout(() => setSignal(null), 3000);
        },
    });

    if (canManage || isAccepted) return null;

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
                title={isAccepted ? "Already enrolled." : isPending ? "Request pending." : "Request enrollment."}
            >
                {isAccepted ? "Enrolled" : isPending ? "Pending…" : createMut.isPending ? "Sending…" : "Request enrollment"}
            </button>
            {signal && <div className={(signal.kind === "ok" ? "text-emerald-700" : "text-red-700") + " text-xs"}>{signal.text}</div>}
        </div>
    );
}

export default function CourseDetail() {
    const canManage = hasAnyRole("CREATOR", "ADMIN");
    const { id } = useParams<{ id: string }>();
    const [tab, setTab] = useState<"overview" | "sections" | "assignments">("overview");
    const [showEditCourse, setShowEditCourse] = useState(false);
    const [editCourse, setEditCourse] = useState<Partial<Course>>({});
    const [courseSaved, setCourseSaved] = useState(false);
    const [showCreateSection, setShowCreateSection] = useState(false);
    const [newSection, setNewSection] = useState<Partial<Section>>({ title: "", textContent: "", orderNumber: 1 });
    const [showEditSection, setShowEditSection] = useState(false);
    const [sectionEditing, setSectionEditing] = useState<Section | null>(null);
    const [editSection, setEditSection] = useState<Partial<Section>>({});
    const [showEnrolled, setShowEnrolled] = useState(false);
    const [enrolledError, setEnrolledError] = useState<string | null>(null);
    const qc = useQueryClient();

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
        queryKey: ["assignments-count", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/courses/${id}/assignments`, {
                params: { pageNumber: 0, pageSize: 1, sortBy: "deadline", direction: "ASC" },
            });
            return (data?.data ?? data) as Page<Assignment>;
        },
        enabled: !!id,
    });

    const enrolledQ = useQuery({
        queryKey: ["course-enrollments", id],
        queryFn: async () => {
            const { data } = await api.get(`/api/enrollments`, {
                params: { courseId: id, pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" },
            });
            return data?.data ?? data;
        },
        enabled: !!id && showEnrolled,
        onError: (e: any) => {
            const msg = e?.response?.data?.message || "Failed to load enrolled users.";
            setEnrolledError(msg);
        },
    });

    const enrolledUsers = useMemo(() => {
        const raw: any = enrolledQ.data;
        const list: any[] = raw?.content ?? raw ?? [];
        return list.map((item) => {
            const u = item?.user ?? item;
            return {
                id: u?.id,
                email: u?.email,
                username: u?.username,
                firstName: u?.firstName,
                lastName: u?.lastName,
                joinedAt: item?.createdAt ?? item?.enrolledAt ?? null,
            } as {
                id?: string;
                email?: string;
                username?: string;
                firstName?: string;
                lastName?: string;
                joinedAt?: string | null;
            };
        });
    }, [enrolledQ.data]);

    const course = courseQ.data;
    const sections = sectionsQ.data?.content ?? [];
    const lessonCount = sections.length;
    const assignmentCount = assignmentsQ.data?.totalElements ?? 0;

    const updateCourseMut = useMutation({
        mutationFn: async (payload: Partial<Course>) => {
            const body: any = {};
            if (payload.title != null) body.title = payload.title;
            if (payload.description != null) body.description = payload.description;
            if (payload.imageUrl != null) body.imageUrl = payload.imageUrl;
            const { data } = await api.patch(`/api/courses/${id}`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setShowEditCourse(false);
            setCourseSaved(true);
            setTimeout(() => setCourseSaved(false), 2500);
            qc.invalidateQueries({ queryKey: ["course", id] });
            qc.invalidateQueries({ queryKey: ["courses"] });
        },
    });

    const createSectionMut = useMutation({
        mutationFn: async (payload: Partial<Section>) => {
            const body: any = {
                title: payload.title?.trim(),
                textContent: payload.textContent ?? "",
                orderNumber: Number(payload.orderNumber ?? 1),
            };
            const { data } = await api.post(`/api/courses/${id}/sections`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setShowCreateSection(false);
            setNewSection({ title: "", textContent: "", orderNumber: (sections?.length ?? 0) + 1 });
            qc.invalidateQueries({ queryKey: ["sections", id] });
        },
    });

    const updateSectionMut = useMutation({
        mutationFn: async (args: { sectionId: string | number; payload: Partial<Section> }) => {
            const body: any = {};
            if (args.payload.title != null) body.title = args.payload.title;
            if (args.payload.textContent != null) body.textContent = args.payload.textContent;
            if (args.payload.orderNumber != null) body.orderNumber = Number(args.payload.orderNumber);
            const { data } = await api.patch(`/api/sections/${args.sectionId}`, body);
            return data?.data ?? data;
        },
        onSuccess: () => {
            setShowEditSection(false);
            setSectionEditing(null);
            setEditSection({});
            qc.invalidateQueries({ queryKey: ["sections", id] });
        },
    });

    const deleteSectionMut = useMutation({
        mutationFn: async (sectionId: string | number) => {
            const { data } = await api.delete(`/api/sections/${sectionId}`);
            return data?.data ?? data;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["sections", id] });
        },
    });

    const gradients = [
        "from-indigo-500 via-blue-500 to-purple-500",
        "from-fuchsia-500 via-pink-500 to-rose-500",
        "from-emerald-500 via-teal-500 to-cyan-500",
        "from-amber-500 via-orange-500 to-red-500",
    ];
    const gradient =
        gradients[
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

    const Pill = ({
                      active,
                      children,
                      onClick,
                  }: {
        active: boolean;
        children: React.ReactNode;
        onClick: () => void;
    }) => (
        <button
            onClick={onClick}
            className={"rounded-full px-4 py-2 text-sm font-medium transition " + (active ? "bg-black text-white shadow" : "bg-white text-gray-700 border hover:bg-gray-50")}
        >
            {children}
        </button>
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
        <div className="min-h-screen bg-gray-50">
            <AppHeader />
            <div className="pb-10">
                <div className="relative">
                    <div className={`h-40 w-full bg-gradient-to-br ${gradient}`} />
                    {course.imageUrl && (
                        <img
                            src={course.imageUrl}
                            alt=""
                            className="absolute inset-0 h-40 w-full object-cover opacity-25 mix-blend-multiply"
                            onError={(e) => ((e.currentTarget as HTMLImageElement).style.display = "none")}
                        />
                    )}
                    <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,0.25)_90%)]" />
                    <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                        <div className="mb-2 text-sm opacity-90">
                            <Link to="/courses" className="underline underline-offset-2">← Back to Courses</Link>
                        </div>
                        <div className="flex items-center justify-between">
                            <h1 className="text-2xl font-semibold">{course.title}</h1>
                            {}
                            {canManage ? (
                                <button
                                    className="rounded-lg border bg-white/90 px-4 py-2 text-sm font-medium text-gray-900 hover:bg-white"
                                    onClick={() => {
                                        setEditCourse({ title: course.title, description: course.description, imageUrl: course.imageUrl });
                                        setShowEditCourse(true);
                                    }}
                                >
                                    Edit
                                </button>
                            ) : (
                                id && <EnrollmentRequestButton courseId={id} />
                            )}
                        </div>
                        <div className="mt-2 flex flex-wrap items-center gap-2">
                            <Badge>{lessonCount} lessons</Badge>
                            <Badge>{assignmentCount} assignments</Badge>
                        </div>
                    </div>
                </div>

                <div className="px-6">
                    {courseSaved && (
                        <div className="mt-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-700">Course updated successfully.</div>
                    )}

                    <div className="mt-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                        <div className="flex items-center gap-2">
                            <Pill active={tab === "overview"} onClick={() => setTab("overview")}>Overview</Pill>
                            <Pill active={tab === "sections"} onClick={() => setTab("sections")}>Sections</Pill>
                            <Pill active={tab === "assignments"} onClick={() => setTab("assignments")}>Assignments</Pill>
                        </div>

                        <div className="flex items-center gap-2">
                            <Link to="/courses" className="rounded-lg border bg-white px-4 py-2 text-sm font-medium hover:bg-gray-50">All Courses</Link>
                            {}
                            {canManage && (
                                <Link to={`/courses/${id}/requests`} className="rounded-lg border bg-white px-4 py-2 text-sm font-medium hover:bg-gray-50">
                                    Requests
                                </Link>
                            )}
                            {canManage && (
                                <button
                                    className="rounded-lg border bg-white px-4 py-2 text-sm font-medium hover:bg-gray-50"
                                    onClick={() => {
                                        setEnrolledError(null);
                                        setShowEnrolled(true);
                                    }}
                                >
                                    Enrolled
                                </button>
                            )}
                        </div>
                    </div>

                    {tab === "overview" && (
                        <div className="mt-6 grid grid-cols-1 gap-6 md:grid-cols-[2fr_1fr]">
                            <div className="rounded-2xl border bg-white p-6">
                                <h2 className="text-lg font-semibold">About this course</h2>
                                <p className="mt-2 whitespace-pre-wrap text-gray-700">{course.description || "No description available."}</p>
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
                                        <button
                                            className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50"
                                            onClick={async () => {
                                                const url = window.location.href;
                                                try {
                                                    if (navigator.share) await navigator.share({ title: course?.title ?? "Course", url });
                                                    else {
                                                        await navigator.clipboard.writeText(url);
                                                        alert("Link copied to clipboard!");
                                                    }
                                                } catch {
                                                    const ta = document.createElement("textarea");
                                                    ta.value = url;
                                                    document.body.appendChild(ta);
                                                    ta.select();
                                                    document.execCommand("copy");
                                                    ta.remove();
                                                    alert("Link copied to clipboard!");
                                                }
                                            }}
                                        >
                                            Share
                                        </button>
                                        {}
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === "sections" && (
                        <div className="mt-6">
                            {canManage && (
                                <div className="mb-3">
                                    <button
                                        className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white shadow hover:opacity-90"
                                        onClick={() => {
                                            setNewSection({ title: "", textContent: "", orderNumber: (sections?.length ?? 0) + 1 });
                                            setShowCreateSection(true);
                                        }}
                                    >
                                        + Add section
                                    </button>
                                </div>
                            )}

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
                                        <SectionCard
                                            key={s.id}
                                            section={s}
                                            canManage={canManage}
                                            onEdit={() => {
                                                setSectionEditing(s);
                                                setEditSection({
                                                    title: s.title,
                                                    textContent: s.textContent ?? "",
                                                    orderNumber: s.orderNumber,
                                                });
                                                setShowEditSection(true);
                                            }}
                                            onDelete={() => {
                                                if (confirm("Delete this section?")) deleteSectionMut.mutate(s.id);
                                            }}
                                        />
                                    ))}
                                </ul>
                            )}
                        </div>
                    )}

                    {tab === "assignments" && id && <AssignmentsPanel courseId={id} />}
                </div>
            </div>

            {showEditCourse && course && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Edit course</h2>
                            <button onClick={() => setShowEditCourse(false)} className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50">Close</button>
                        </div>

                        {updateCourseMut.isError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">Failed to save changes.</div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                const payload: Partial<Course> = {
                                    title: editCourse.title ?? course.title,
                                    description: editCourse.description ?? course.description,
                                    imageUrl: editCourse.imageUrl ?? course.imageUrl,
                                };
                                updateCourseMut.mutate(payload);
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Title</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editCourse.title ?? ""}
                                    onChange={(e) => setEditCourse((f) => ({ ...f, title: e.target.value }))}
                                    placeholder={course.title}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Description</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={3}
                                    value={editCourse.description ?? ""}
                                    onChange={(e) => setEditCourse((f) => ({ ...f, description: e.target.value }))}
                                    placeholder={course.description || ""}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Image URL</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editCourse.imageUrl ?? ""}
                                    onChange={(e) => setEditCourse((f) => ({ ...f, imageUrl: e.target.value }))}
                                    placeholder={course.imageUrl || ""}
                                />
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button type="button" onClick={() => setShowEditCourse(false)} className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
                                <button type="submit" disabled={updateCourseMut.isPending} className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
                                    {updateCourseMut.isPending ? "Saving…" : "Save"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showCreateSection && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Add section</h2>
                            <button onClick={() => setShowCreateSection(false)} className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50">Close</button>
                        </div>

                        {createSectionMut.isError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">Failed to create section.</div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                if (!newSection.title?.trim()) return;
                                createSectionMut.mutate({
                                    title: newSection.title.trim(),
                                    textContent: newSection.textContent ?? "",
                                    orderNumber: Number(newSection.orderNumber || 1),
                                });
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Title</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={newSection.title ?? ""}
                                    onChange={(e) => setNewSection((f) => ({ ...f, title: e.target.value }))}
                                    required
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Text content</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={4}
                                    value={newSection.textContent ?? ""}
                                    onChange={(e) => setNewSection((f) => ({ ...f, textContent: e.target.value }))}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Order number</label>
                                <input
                                    type="number"
                                    min={1}
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={newSection.orderNumber ?? 1}
                                    onChange={(e) => setNewSection((f) => ({ ...f, orderNumber: Number(e.target.value) }))}
                                />
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button type="button" onClick={() => setShowCreateSection(false)} className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
                                <button type="submit" disabled={createSectionMut.isPending} className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
                                    {createSectionMut.isPending ? "Creating…" : "Create"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {showEditSection && sectionEditing && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Edit section</h2>
                            <button onClick={() => setShowEditSection(false)} className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50">Close</button>
                        </div>

                        {updateSectionMut.isError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">Failed to save section.</div>
                        )}

                        <form
                            className="space-y-4"
                            onSubmit={(e) => {
                                e.preventDefault();
                                updateSectionMut.mutate({
                                    sectionId: sectionEditing.id,
                                    payload: {
                                        title: editSection.title ?? sectionEditing.title,
                                        textContent: editSection.textContent ?? sectionEditing.textContent,
                                        orderNumber: editSection.orderNumber ?? sectionEditing.orderNumber,
                                    },
                                });
                            }}
                        >
                            <div>
                                <label className="block text-sm font-medium">Title</label>
                                <input
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editSection.title ?? ""}
                                    onChange={(e) => setEditSection((f) => ({ ...f, title: e.target.value }))}
                                    placeholder={sectionEditing.title}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Text content</label>
                                <textarea
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    rows={4}
                                    value={editSection.textContent ?? ""}
                                    onChange={(e) => setEditSection((f) => ({ ...f, textContent: e.target.value }))}
                                    placeholder={sectionEditing.textContent || ""}
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium">Order number</label>
                                <input
                                    type="number"
                                    min={1}
                                    className="mt-1 w-full rounded-lg border px-3 py-2"
                                    value={editSection.orderNumber ?? sectionEditing.orderNumber ?? 1}
                                    onChange={(e) => setEditSection((f) => ({ ...f, orderNumber: Number(e.target.value) }))}
                                />
                            </div>

                            <div className="mt-4 flex items-center justify-end gap-2">
                                <button type="button" onClick={() => setShowEditSection(false)} className="rounded-lg border bg-white px-4 py-2 text-sm hover:bg-gray-50">Cancel</button>
                                <button type="submit" disabled={updateSectionMut.isPending} className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60">
                                    {updateSectionMut.isPending ? "Saving…" : "Save"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {canManage && showEnrolled && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
                    <div className="w-full max-w-2xl rounded-2xl border bg-white p-6">
                        <div className="mb-4 flex items-center justify-between">
                            <h2 className="text-lg font-semibold">Enrolled users</h2>
                            <button onClick={() => setShowEnrolled(false)} className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50">Close</button>
                        </div>

                        {enrolledError && (
                            <div className="mb-3 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700">{enrolledError}</div>
                        )}

                        {enrolledQ.isLoading ? (
                            <div className="grid gap-2">
                                {Array.from({ length: 6 }).map((_, i) => (
                                    <div key={i} className="h-10 animate-pulse rounded-lg border bg-gray-100" />
                                ))}
                            </div>
                        ) : (enrolledUsers?.length ?? 0) === 0 ? (
                            <div className="rounded-xl border bg-gray-50 p-6 text-center text-gray-600">No users enrolled yet.</div>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full text-sm">
                                    <thead>
                                    <tr className="text-left text-gray-600">
                                        <th className="px-3 py-2">Name</th>
                                        <th className="px-3 py-2">Username</th>
                                        <th className="px-3 py-2">Email</th>
                                        <th className="px-3 py-2">Enrolled</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {enrolledUsers.map((u, i) => {
                                        const full = (u.firstName || u.lastName) ? `${u.firstName ?? ""} ${u.lastName ?? ""}`.trim() : "";
                                        return (
                                            <tr key={`${u.id ?? i}`} className="border-t">
                                                <td className="px-3 py-2">{full || "—"}</td>
                                                <td className="px-3 py-2">{u.username || "—"}</td>
                                                <td className="px-3 py-2">{u.email || "—"}</td>
                                                <td className="px-3 py-2">{u.joinedAt ? new Date(String(u.joinedAt).replace(" ", "T")).toLocaleString() : "—"}</td>
                                            </tr>
                                        );
                                    })}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}
