import { Link } from "react-router-dom";

export default function Dashboard() {
    return (
        <div className="p-6">
            <div className="flex items-center justify-between">
                <h1 className="text-2xl font-semibold">Dashboard</h1>

                <Link to="/logout" className="px-3 py-2 rounded-lg border inline-block">
                    Logout
                </Link>
            </div>

            <div className="mt-6 space-x-4">
                <Link to="/courses" className="underline">Go to Courses</Link>
            </div>
        </div>
    );
}
