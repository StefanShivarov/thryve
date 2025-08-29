import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function ProtectedRoute() {
  const hasToken = !!localStorage.getItem("accessToken");
  const location = useLocation();
  if (!hasToken) return <Navigate to="/login" replace state={{ from: location }} />;
  return <Outlet />;
}
