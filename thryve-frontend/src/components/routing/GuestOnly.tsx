import { Navigate, Outlet, useLocation } from "react-router-dom";
import { isAuthenticated } from "../../lib/auth";

export default function GuestOnly() {
    const location = useLocation();
    if (isAuthenticated()) {
        const to = (location.state as any)?.from ?? "/";
        return <Navigate to={to} replace />;
    }
    return <Outlet />;
}
