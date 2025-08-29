import { Link } from "react-router-dom";

export type Course = {
    id: number | string;
    title: string;
    description?: string;
    imageUrl?: string;
};

function FallbackBanner({ title }: { title: string }) {
    const letter = (title?.trim()?.[0] || "?").toUpperCase();
    return (
        <div className="flex h-32 w-full items-center justify-center rounded-xl bg-gradient-to-br from-gray-100 to-gray-200">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-white shadow">
                <span className="text-xl font-semibold">{letter}</span>
            </div>
        </div>
    );
}

export default function CourseCard({ course }: { course: Course }) {
    return (
        <div className="group relative flex flex-col overflow-hidden rounded-2xl border bg-white shadow-sm transition hover:shadow-md">
            {course.imageUrl ? (
                <img
                    src={course.imageUrl}
                    alt={course.title}
                    className="h-32 w-full object-cover"
                    onError={(e) => { (e.currentTarget as HTMLImageElement).style.display = "none"; }}
                />
            ) : (
                <FallbackBanner title={course.title} />
            )}

            <div className="flex flex-1 flex-col p-4">
                <div className="line-clamp-1 text-base font-semibold">{course.title}</div>
                {course.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-gray-600">{course.description}</p>
                )}

                <div className="mt-4">
                    <Link
                        to={`/courses/${course.id}`}
                        className="inline-flex items-center rounded-lg border px-3 py-2 text-sm font-medium hover:bg-gray-50"
                    >
                        Open
                    </Link>
                </div>
            </div>
        </div>
    );
}
