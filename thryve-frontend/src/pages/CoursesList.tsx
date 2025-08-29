import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "../lib/api";
import { useState } from "react";

type Course = {
  id: number | string;
  title: string;
  description?: string;
  imageUrl?: string;
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

export default function CoursesList() {
  const [page, setPage] = useState(0);
  const { data, isLoading, error } = useQuery({
    queryKey: ["courses", page],
    queryFn: async () => {
      const params = { pageNumber: page, pageSize: 10, sortBy: "id", direction: "ASC" };
      return (await api.get<Page<Course>>("/api/courses", { params })).data;
    },
  });

  if (isLoading) return <div className="p-6">Loading courses…</div>;
  if (error) return <div className="p-6 text-red-600">Failed to load.</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold mb-4">Courses</h1>
      <ul className="space-y-3">
        {data?.content?.map((c) => (
          <li key={c.id} className="border rounded-xl p-4">
            <div className="font-medium">{c.title}</div>
            {c.description && <div className="text-sm text-gray-600">{c.description}</div>}
            <Link to={`/courses/${c.id}`} className="text-sm underline mt-2 inline-block">Open</Link>
          </li>
        ))}
      </ul>
      <div className="flex items-center gap-3 mt-6">
        <button disabled={data?.first} onClick={() => setPage((p) => Math.max(0, p - 1))} className="px-3 py-2 rounded border disabled:opacity-50">Prev</button>
        <span>Page {data ? data.number + 1 : 1} / {data?.totalPages ?? 1}</span>
        <button disabled={data?.last} onClick={() => setPage((p) => p + 1)} className="px-3 py-2 rounded border disabled:opacity-50">Next</button>
      </div>
    </div>
  );
}
