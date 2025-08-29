import { useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";

type Course = {
  id: number | string;
  title: string;
  description?: string;
};

type Section = {
  id: number | string;
  title: string;
  textContent?: string;
  orderNumber: number;
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

export default function CourseDetail() {
  const { id } = useParams<{ id: string }>();

  const courseQ = useQuery({
    queryKey: ["course", id],
    queryFn: async () => (await api.get<Course>(`/api/courses/${id}`)).data,
    enabled: !!id,
  });

  const sectionsQ = useQuery({
    queryKey: ["sections", id],
    queryFn: async () => (await api.get<Page<Section>>(`/api/courses/${id}/sections`, { params: { pageNumber: 0, pageSize: 50, sortBy: "orderNumber", direction: "ASC" } })).data,
    enabled: !!id,
  });

  if (courseQ.isLoading) return <div className="p-6">Loading…</div>;
  if (courseQ.error || !courseQ.data) return <div className="p-6 text-red-600">Not found.</div>;

  return (
    <div className="p-6">
      <h1 className="text-2xl font-semibold mb-2">{courseQ.data.title}</h1>
      <p className="text-gray-700 mb-6">{courseQ.data.description ?? "No description"}</p>

      <h2 className="text-xl font-semibold mb-3">Sections</h2>
      <ul className="space-y-3">
        {sectionsQ.data?.content?.map(s => (
          <li key={s.id} className="border rounded-xl p-4">
            <div className="font-medium">{s.title}</div>
            {s.textContent && <div className="text-sm text-gray-700 whitespace-pre-wrap">{s.textContent}</div>}
          </li>
        ))}
      </ul>
    </div>
  );
}
