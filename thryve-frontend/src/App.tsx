import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import CoursesList from "./pages/CoursesList";
import CourseDetail from "./pages/CourseDetail";
import ProtectedRoute from "./components/ProtectedRoute";
import CourseEnrollmentRequests from "./pages/CourseEnrollmentRequests";
import Logout from "./pages/Logout";
import Register from "./pages/Register";

export default function App() {
    return (
        <Routes>
            <Route path="/register" element={<Register />} />

            <Route path="/login" element={<Login />} />
            <Route path="/logout" element={<Logout />} />   {/* <-- add this line */}

            <Route element={<ProtectedRoute />}>
                <Route path="/" element={<Dashboard />} />
                <Route path="/courses" element={<CoursesList />} />
                <Route path="/courses/:id" element={<CourseDetail />} />
                <Route path="/courses/:id/requests" element={<CourseEnrollmentRequests />} /> {/* new */}
            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
