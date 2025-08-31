import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import CourseCard, { Course } from "../components/CourseCard";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { Button, Input, Select } from "../components/UI";
import AppHeader from "../components/AppHeader";
import { hasAnyRole } from "../lib/auth";

type Page<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

type CourseCreate = {
  title: string;
  description: string;
  imageUrl: string;
};

const canManage = hasAnyRole("CREATOR", "ADMIN");

export default function CoursesList() {
  const qc = useQueryClient();

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(12);
  const [sortBy, setSortBy] = useState<"id" | "title">("id");
  const [direction, setDirection] = useState<"ASC" | "DESC">("ASC");
  const [search, setSearch] = useState("");

  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState<CourseCreate>({ title: "", description: "", imageUrl: "" });
  const [formError, setFormError] = useState<string | null>(null);

  const { data, isLoading, isFetching, error, refetch } = useQuery({
    queryKey: ["courses", page, pageSize, sortBy, direction],
    queryFn: async () => {
      const res = await api.get("/api/courses", {
        params: { pageNumber: page, pageSize, sortBy, direction },
      });
      return (res.data?.data ?? res.data) as Page<Course>;
    },
    keepPreviousData: true,
  });

  const filtered = useMemo(() => {
    if (!data?.content) return [];
    const q = search.trim().toLowerCase();
    if (!q) return data.content;
    return data.content.filter((c) => c.title?.toLowerCase().includes(q));
  }, [data, search]);

  const createMut = useMutation({
    mutationFn: async (payload: CourseCreate) => {
      const { data } = await api.post("/api/courses", payload);
      return data?.data ?? data;
    },
    onSuccess: () => {
      setShowCreate(false);
      setForm({ title: "", description: "", imageUrl: "" });
      setFormError(null);
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
    onError: (e: any) => {
      const msg =
          e?.response?.data?.message ||
          e?.response?.data?.error ||
          e?.message ||
          "Failed to create course";
      setFormError(msg);
    },
  });

  const deleteMut = useMutation({
    mutationFn: async (id: string | number) => {
      const { data } = await api.delete(`/api/courses/${id}`);
      return data?.data ?? data;
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["courses"] });
    },
  });

  return (
      <div className="min-h-screen bg-gray-50">
        <AppHeader />
        <div className="p-6">
          <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div>
              <h1 className="text-2xl font-semibold">Courses</h1>
              <p className="text-sm text-gray-600">
                Browse and open a course to view its sections.
              </p>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <div className="flex items-center gap-2">
                <Input
                    placeholder="Search by title…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    aria-label="Search courses"
                />
              </div>

              <div className="flex items-center gap-2">
                <Select
                    value={sortBy}
                    onChange={(e) => {
                      setSortBy(e.target.value as "id" | "title");
                      setPage(0);
                    }}
                    aria-label="Sort by"
                >
                  <option value="id">Sort: ID</option>
                  <option value="title">Sort: Title</option>
                </Select>

                <Select
                    value={direction}
                    onChange={(e) => {
                      setDirection(e.target.value as "ASC" | "DESC");
                      setPage(0);
                    }}
                    aria-label="Sort direction"
                >
                  <option value="ASC">Asc</option>
                  <option value="DESC">Desc</option>
                </Select>

                <Select
                    value={String(pageSize)}
                    onChange={(e) => {
                      setPageSize(Number(e.target.value));
                      setPage(0);
                    }}
                    aria-label="Page size"
                >
                  <option value="6">6 / page</option>
                  <option value="12">12 / page</option>
                  <option value="24">24 / page</option>
                </Select>

                {canManage && (
                    <Button onClick={() => { setFormError(null); setShowCreate(true); }} className="ml-1">
                      Add course
                    </Button>
                )}
              </div>
            </div>
          </div>

          {error && (
              <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
                Failed to load courses.
                <Button className="ml-3 border-red-300 hover:bg-red-100" onClick={() => refetch()}>
                  Retry
                </Button>
              </div>
          )}

          {(isLoading || (isFetching && !data)) && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {Array.from({ length: pageSize }).map((_, i) => (
                    <LoadingSkeleton key={i} />
                ))}
              </div>
          )}

          {!isLoading && data && (
              <>
                {filtered.length === 0 ? (
                    <div className="rounded-2xl border bg-white p-10 text-center">
                      <div className="text-lg font-medium">No courses found</div>
                      <p className="mt-1 text-sm text-gray-600">Try clearing the search or changing sort options.</p>
                    </div>
                ) : (
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                      {filtered.map((course) => (
                          <div key={course.id} className="relative">
                            <CourseCard course={course} />
                            {canManage && (
                                <div className="absolute right-2 top-2 flex gap-2">
                                  <Link
                                      to={`/courses/${course.id}/requests`}
                                      className="rounded-md border bg-white px-2 py-1 text-xs hover:bg-gray-50"
                                  >
                                    Manage requests
                                  </Link>
                                  <button
                                      onClick={() => {
                                        if (window.confirm("Delete this course?")) deleteMut.mutate(course.id);
                                      }}
                                      disabled={deleteMut.isPending}
                                      className="rounded-md border bg-white px-2 py-1 text-xs hover:bg-gray-50 disabled:opacity-60"
                                  >
                                    Delete
                                  </button>
                                </div>
                            )}
                          </div>
                      ))}
                    </div>
                )}

                <div className="mt-6 flex flex-col items-center justify-between gap-3 sm:flex-row">
                  <div className="text-sm text-gray-600">
                    Page <span className="font-medium">{(data.number ?? 0) + 1}</span> of{" "}
                    <span className="font-medium">{data.totalPages || 1}</span> •{" "}
                    <span className="font-medium">{data.totalElements ?? filtered.length}</span> total
                  </div>

                  <div className="flex items-center gap-2">
                    <Button
                        className="min-w-[84px]"
                        disabled={data.first || isFetching}
                        onClick={() => setPage((p) => Math.max(0, p - 1))}
                    >
                      Prev
                    </Button>
                    <Button
                        className="min-w-[84px]"
                        disabled={data.last || isFetching}
                        onClick={() => setPage((p) => p + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </>
          )}
        </div>

        {showCreate && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
              <div className="w-full max-w-lg rounded-2xl border bg-white p-6">
                <div className="mb-4 flex items-center justify-between">
                  <h2 className="text-lg font-semibold">Add course</h2>
                  <button
                      onClick={() => {
                        setShowCreate(false);
                        setFormError(null);
                      }}
                      className="rounded-md border bg-white px-3 py-1 text-sm hover:bg-gray-50"
                  >
                    Close
                  </button>
                </div>

                {formError && (
                    <div className="mb-3 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                      {formError}
                    </div>
                )}

                <form
                    className="space-y-4"
                    noValidate
                    onSubmit={(e) => {
                      e.preventDefault();
                      setFormError(null);
                      const t = form.title.trim();
                      const d = form.description.trim();
                      const u = form.imageUrl.trim();
                      if (!t || t.length < 2) {
                        setFormError("Title must be at least 2 characters.");
                        return;
                      }
                      if (!d) {
                        setFormError("Description is required.");
                        return;
                      }
                      if (!/^https?:\/\//i.test(u)) {
                        setFormError("Image URL must start with http or https.");
                        return;
                      }
                      createMut.mutate({ title: t, description: d, imageUrl: u });
                    }}
                >
                  <div>
                    <label className="block text-sm font-medium">Title</label>
                    <input
                        className="mt-1 w-full rounded-lg border px-3 py-2"
                        value={form.title}
                        onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
                        required
                        minLength={2}
                        maxLength={100}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium">Description</label>
                    <textarea
                        className="mt-1 w-full rounded-lg border px-3 py-2"
                        rows={3}
                        value={form.description}
                        onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
                        required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium">Image URL</label>
                    <input
                        className="mt-1 w-full rounded-lg border px-3 py-2"
                        type="text"
                        value={form.imageUrl}
                        onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))}
                        required
                    />
                  </div>

                  <div className="mt-4 flex items-center justify-end gap-2">
                    <button
                        type="button"
                        onClick={() => {
                          setShowCreate(false);
                          setFormError(null);
                        }}
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
      </div>
  );
}
