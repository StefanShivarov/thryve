import { Navigate, Outlet, useLocation } from "react-router-dom";

export default function ProtectedRoute() {
  const location = useLocation();
  const access = localStorage.getItem("accessToken");
  const refresh = localStorage.getItem("refreshToken");
  if (!access && !refresh) {
    return <Navigate to="/register" replace state={{ from: location }} />;
  }
  return <Outlet />;
}
