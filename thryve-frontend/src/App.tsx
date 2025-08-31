import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import CoursesList from "./pages/CoursesList";
import CourseDetail from "./pages/CourseDetail";
import ProtectedRoute from "./components/ProtectedRoute";
import CourseEnrollmentRequests from "./pages/CourseEnrollmentRequests";
import Logout from "./pages/Logout";
import Register from "./pages/Register";
import Profile from "./pages/Profile";
import AssignmentDetail from "./pages/AssignmentDetail";
import GuestOnly from "./components/routing/GuestOnly";
import CourseRequestsPage from "./pages/CourseRequestsPage";

export default function App() {
    return (
        <Routes>
            <Route element={<GuestOnly />}>
                <Route path="/register" element={<Register />} />
                <Route path="/login" element={<Login />} />
            </Route>



            <Route element={<ProtectedRoute />}>
                <Route path="/logout" element={<Logout />} />
                <Route path="/courses/:id/requests" element={<CourseRequestsPage />} />
                <Route path="/" element={<Dashboard />} />
                <Route path="/courses" element={<CoursesList />} />
                <Route path="/courses/:id" element={<CourseDetail />} />
                <Route path="/courses/:id/requests" element={<CourseEnrollmentRequests />} />
                <Route path="/profile" element={<Profile />} />
                <Route path="/courses/:courseId/assignments/:assignmentId" element={<AssignmentDetail />} />


            </Route>

            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
