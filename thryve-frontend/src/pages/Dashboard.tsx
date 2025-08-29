import { Link } from "react-router-dom";
import AppHeader from "../components/AppHeader";

function IconBookOpen(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M12 19c-2-1.5-5-1.5-7 0V6c2-1.5 5-1.5 7 0m0 13c2-1.5 5-1.5 7 0V6c-2-1.5-5-1.5-7 0m0 13V6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconClipboardList(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M9 5h6M9 9h6M9 13h6M7 3h10a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconBell(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M15 17H9m8-6a5 5 0 10-10 0c0 3-1 4-2 5h14c-1-1-2-2-2-5zM12 21a2 2 0 01-2-2h4a2 2 0 01-2 2z" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconUser(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM5.5 20a6.5 6.5 0 0113 0" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}
function IconChevronRight(props: React.SVGProps<SVGSVGElement>) {
    return (
        <svg viewBox="0 0 24 24" fill="none" {...props}>
            <path d="M9 6l6 6-6 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
    );
}

function avatarFromToken() {
    const t = localStorage.getItem("accessToken");
    try {
        const [, b] = (t || "").split(".");
        const p = JSON.parse(atob(b));
        const email: string = p?.sub || "";
        const name = (p?.name || "").trim();
        const label = name || email || "User";
        const initial = label.charAt(0).toUpperCase() || "U";
        return { label, initial };
    } catch {
        return { label: "User", initial: "U" };
    }
}

export default function Dashboard() {
    const { label, initial } = avatarFromToken();

    return (
        <div className="min-h-screen bg-gray-50">
            <AppHeader />

            <div className="relative">
                <div className="h-36 w-full bg-gradient-to-r from-indigo-500 via-sky-500 to-teal-500" />
                <div className="absolute inset-0 bg-[linear-gradient(to_bottom,rgba(0,0,0,0)_0%,rgba(0,0,0,.25)_95%)]" />
                <div className="absolute bottom-0 left-0 right-0 px-6 pb-5 text-white">
                    <div className="flex items-center justify-between">
                        <div>
                            <div className="text-sm opacity-90">Welcome back</div>
                            <h1 className="text-2xl font-semibold">{label}</h1>
                        </div>
                        <div className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-white text-gray-800 font-semibold">
                            {initial}
                        </div>
                    </div>
                </div>
            </div>

            <div className="px-6 py-6">
                <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                    <Link to="/courses" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-indigo-50 p-3">
                                <IconBookOpen className="h-6 w-6 text-indigo-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Browse</div>
                                <div className="text-lg font-semibold">Courses</div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">Find courses and see details.</p>
                    </Link>

                    <Link to="/assignments" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-emerald-50 p-3">
                                <IconClipboardList className="h-6 w-6 text-emerald-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Your work</div>
                                <div className="text-lg font-semibold">Assignments</div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">Track deadlines and submissions.</p>
                    </Link>

                    <Link to="/notifications" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-amber-50 p-3">
                                <IconBell className="h-6 w-6 text-amber-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Updates</div>
                                <div className="text-lg font-semibold">Notifications</div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">Messages about courses and grades.</p>
                    </Link>
                </div>

                <div className="mt-6 grid grid-cols-1 gap-4 md:grid-cols-2">
                    <Link to="/profile" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center gap-3">
                            <div className="rounded-xl bg-sky-50 p-3">
                                <IconUser className="h-6 w-6 text-sky-600" />
                            </div>
                            <div>
                                <div className="text-sm text-gray-500">Account</div>
                                <div className="text-lg font-semibold">Profile</div>
                            </div>
                            <IconChevronRight className="ml-auto h-5 w-5 text-gray-400 group-hover:text-gray-600" />
                        </div>
                        <p className="mt-3 text-sm text-gray-600">View and edit your details.</p>
                    </Link>

                    <Link to="/courses" className="group rounded-2xl border bg-white p-5 shadow-sm transition hover:shadow-md">
                        <div className="flex items-center justify-between">
                            <div>
                                <div className="text-sm text-gray-500">Get started</div>
                                <div className="text-lg font-semibold">Continue learning</div>
                                <p className="mt-2 text-sm text-gray-600">Jump back into your courses.</p>
                            </div>
                            <div className="rounded-xl bg-gray-50 p-4">
                                <IconBookOpen className="h-8 w-8 text-gray-700" />
                            </div>
                        </div>
                    </Link>
                </div>
            </div>
        </div>
    );
}
