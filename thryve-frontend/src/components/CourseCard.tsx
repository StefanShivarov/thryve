import { Link } from "react-router-dom";

export type Course = {
    id: string | number;
    title: string;
    description?: string;
    imageUrl?: string;
};

export default function CourseCard({ course }: { course: Course }) {
    return (
        <Link
            to={`/courses/${course.id}`}
            className="group block overflow-hidden rounded-2xl border bg-white shadow-sm hover:shadow-md"
        >
            <div className="h-32 w-full bg-gray-100">
                {course.imageUrl ? (
                    <img
                        src={course.imageUrl}
                        alt=""
                        className="h-32 w-full object-cover"
                        onError={(e) => {
                            (e.currentTarget as HTMLImageElement).style.display = "none";
                        }}
                    />
                ) : null}
            </div>
            <div className="p-4">
                <div className="line-clamp-1 text-base font-semibold">{course.title}</div>
                {course.description && (
                    <p className="mt-1 line-clamp-2 text-sm text-gray-600">{course.description}</p>
                )}
                <div className="mt-3 text-sm font-medium text-indigo-600">Open →</div>
            </div>
        </Link>
    );
}
