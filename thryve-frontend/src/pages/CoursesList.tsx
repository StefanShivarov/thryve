import { useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { api } from "../lib/api";
import CourseCard, { Course } from "../components/CourseCard";
import LoadingSkeleton from "../components/LoadingSkeleton";
import { Button, Input, Select } from "../components/UI";
import {Link} from "react-router-dom";

type Page<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export default function CoursesList() {
  // server-driven paging/sorting
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(12);
  const [sortBy, setSortBy] = useState<"id" | "title">("id");
  const [direction, setDirection] = useState<"ASC" | "DESC">("ASC");

  // client-side filtering (simple title contains)
  const [search, setSearch] = useState("");

  const { data, isLoading, isFetching, error, refetch } = useQuery({
    queryKey: ["courses", page, pageSize, sortBy, direction],
    queryFn: async () => {
      const res = await api.get("/api/courses", { params: { pageNumber: page, pageSize, sortBy, direction } });
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

  return (
      <div className="p-6">
        {/* Toolbar */}
        <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-2xl font-semibold">Courses</h1>

            <p className="text-sm text-gray-600">Browse and open a course to view its sections.</p>
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
                  onChange={(e) => { setSortBy(e.target.value as "id" | "title"); setPage(0); }}
                  aria-label="Sort by"
              >
                <option value="id">Sort: ID</option>
                <option value="title">Sort: Title</option>
              </Select>

              <Select
                  value={direction}
                  onChange={(e) => { setDirection(e.target.value as "ASC" | "DESC"); setPage(0); }}
                  aria-label="Sort direction"
              >
                <option value="ASC">Asc</option>
                <option value="DESC">Desc</option>
              </Select>

              <Select
                  value={String(pageSize)}
                  onChange={(e) => { setPageSize(Number(e.target.value)); setPage(0); }}
                  aria-label="Page size"
              >
                <option value="6">6 / page</option>
                <option value="12">12 / page</option>
                <option value="24">24 / page</option>
              </Select>
              <Link to="/logout" className="px-3 py-2 rounded-lg border inline-block">
                Logout
              </Link>
            </div>
          </div>
        </div>

        {/* States */}
        {error && (
            <div className="mb-4 rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
              Failed to load courses.
              <Button className="ml-3 border-red-300 hover:bg-red-100" onClick={() => refetch()}>
                Retry
              </Button>
            </div>
        )}

        {/* Loading skeleton */}
        {(isLoading || (isFetching && !data)) && (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {Array.from({ length: pageSize }).map((_, i) => <LoadingSkeleton key={i} />)}
            </div>
        )}

        {/* Content */}
        {!isLoading && data && (
            <>
              {filtered.length === 0 ? (
                  <div className="rounded-2xl border bg-white p-10 text-center">
                    <div className="text-lg font-medium">No courses found</div>
                    <p className="mt-1 text-sm text-gray-600">
                      Try clearing the search or changing sort options.
                    </p>
                  </div>
              ) : (
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                    {filtered.map((course) => (
                        <CourseCard key={course.id} course={course} />
                    ))}
                  </div>
              )}

              {/* Pagination */}
              <div className="mt-6 flex flex-col items-center justify-between gap-3 sm:flex-row">
                <div className="text-sm text-gray-600">
                  Page <span className="font-medium">{(data.number ?? 0) + 1}</span> of{" "}
                  <span className="font-medium">{data.totalPages || 1}</span> •{" "}
                  <span className="font-medium">{data.totalElements || filtered.length}</span> total
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
  );
}
