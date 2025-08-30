import { useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import { clearTokens /*, api */ } from "../lib/api";

export default function Logout() {
    const navigate = useNavigate();
    const location = useLocation();
    const queryClient = useQueryClient();

    useEffect(() => {
        (async () => {
            try {

            } finally {
                clearTokens();
                queryClient.clear();
                const params = new URLSearchParams(location.search);
                const returnTo = params.get("returnTo") || "/login";
                navigate(returnTo, { replace: true });
            }
        })();
    }, [navigate, location.search, queryClient]);

    return (
        <div className="min-h-screen flex items-center justify-center p-6">
            <div className="text-center">
                <div className="text-2xl font-semibold mb-2">Signing you out…</div>
                <p className="text-gray-600">Please wait.</p>
            </div>
        </div>
    );
}
