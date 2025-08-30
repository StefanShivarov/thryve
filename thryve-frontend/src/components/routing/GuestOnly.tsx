import { Navigate, Outlet, useLocation } from "react-router-dom";
import { isAuthenticated } from "../../lib/auth";

export default function GuestOnly() {
    const location = useLocation();
    if (isAuthenticated()) {
        // If already logged in, push them away from login/register
        // Optionally send them where they tried to come from or home
        const to = (location.state as any)?.from ?? "/";
        return <Navigate to={to} replace />;
    }
    return <Outlet />;
}
