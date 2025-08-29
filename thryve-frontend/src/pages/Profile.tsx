import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, clearTokens } from "../lib/api";
import { Link, useNavigate } from "react-router-dom";
import AppHeader from "../components/AppHeader";

type User = { id: string; email: string; username: string; firstName: string; lastName: string };
type Page<T> = { content: T[] };

const schema = z.object({
    email: z.string().email(),
    username: z.string().min(3).max(50),
    firstName: z.string().min(2).max(50),
    lastName: z.string().min(2).max(50),
});

function decodeEmail(): string | null {
    const t = localStorage.getItem("accessToken");
    if (!t) return null;
    try {
        const [, b] = t.split(".");
        const p = JSON.parse(atob(b || ""));
        return p?.sub || null;
    } catch {
        return null;
    }
}

async function resolveUserId(): Promise<string | null> {
    const cached = localStorage.getItem("userId");
    if (cached) return cached;
    const email = decodeEmail();
    if (!email) return null;
    try {
        const res = await api.get("/api/users", { params: { pageNumber: 0, pageSize: 1000, sortBy: "id", direction: "ASC" } });
        const page: Page<User> = res.data?.data ?? res.data;
        const u = (page?.content || []).find((x) => x.email === email);
        if (u?.id) {
            localStorage.setItem("userId", u.id);
            return u.id;
        }
    } catch {}
    return null;
}

export default function Profile() {
    const navigate = useNavigate();
    const qc = useQueryClient();
    const [saved, setSaved] = useState(false);

    const meQ = useQuery({
        queryKey: ["profile-user"],
        queryFn: async (): Promise<User> => {
            const id = await resolveUserId();
            if (!id) throw new Error("NO_USER_ID");
            const { data } = await api.get(`/api/users/${id}`);
            return (data?.data ?? data) as User;
        },
    });

    const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<z.infer<typeof schema>>({
        resolver: zodResolver(schema),
        mode: "onTouched",
    });

    useEffect(() => {
        if (meQ.data) {
            reset({
                email: meQ.data.email,
                username: meQ.data.username,
                firstName: meQ.data.firstName,
                lastName: meQ.data.lastName,
            });
        }
    }, [meQ.data, reset]);

    useEffect(() => {
        if (saved) {
            const t = setTimeout(() => setSaved(false), 2500);
            return () => clearTimeout(t);
        }
    }, [saved]);

    const updateMutation = useMutation({
        mutationFn: async (v: z.infer<typeof schema>) => {
            const id = localStorage.getItem("userId");
            const { data } = await api.patch(`/api/users/${id}`, {
                username: v.username,
                firstName: v.firstName,
                lastName: v.lastName,
            });
            return data?.data ?? data;
        },
        onSuccess: () => {
            qc.invalidateQueries({ queryKey: ["profile-user"] });
            setSaved(true);
        },
        onError: (e: any) => alert(e?.response?.data?.message || "Update failed"),
    });

    const deleteMutation = useMutation({
        mutationFn: async () => {
            const id = localStorage.getItem("userId");
            const { data } = await api.delete(`/api/users/${id}`);
            return data?.data ?? data;
        },
        onSuccess: () => {
            clearTokens();
            localStorage.removeItem("userId");
            navigate("/register", { replace: true });
        },
        onError: (e: any) => alert(e?.response?.data?.message || "Delete failed"),
    });

    const onSubmit = (v: z.infer<typeof schema>) => updateMutation.mutate(v);

    return (
        <div className="min-h-screen bg-gray-50">
            <AppHeader />
            <div className="p-6">
                {meQ.isLoading && <div className="h-36 animate-pulse rounded-3xl bg-gray-200" />}

                {meQ.error && (
                    <div className="rounded-2xl border border-amber-200 bg-amber-50 p-6 text-amber-800">
                        Couldn’t load your profile. <Link className="underline" to="/login">Login</Link>
                    </div>
                )}

                {meQ.data && (
                    <div className="grid grid-cols-1 gap-6 md:grid-cols-[2fr_1fr]">
                        <div className="rounded-2xl border bg-white p-6">
                            <h2 className="text-lg font-semibold">Your details</h2>

                            {saved && (
                                <div className="mt-3 rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
                                    Changes saved
                                </div>
                            )}

                            <form className="mt-4 space-y-4" onSubmit={handleSubmit(onSubmit)}>
                                <div>
                                    <label className="block text-sm font-medium">Email</label>
                                    <input disabled {...register("email")} className="mt-1 w-full rounded-lg border px-3 py-2 bg-gray-50 text-gray-600" />
                                    {errors.email && <p className="mt-1 text-xs text-red-600">{errors.email.message}</p>}
                                </div>
                                <div>
                                    <label className="block text-sm font-medium">Username</label>
                                    <input {...register("username")} className="mt-1 w-full rounded-lg border px-3 py-2" />
                                    {errors.username && <p className="mt-1 text-xs text-red-600">{errors.username.message}</p>}
                                </div>
                                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                    <div>
                                        <label className="block text-sm font-medium">First name</label>
                                        <input {...register("firstName")} className="mt-1 w-full rounded-lg border px-3 py-2" />
                                        {errors.firstName && <p className="mt-1 text-xs text-red-600">{errors.firstName.message}</p>}
                                    </div>
                                    <div>
                                        <label className="block text-sm font-medium">Last name</label>
                                        <input {...register("lastName")} className="mt-1 w-full rounded-lg border px-3 py-2" />
                                        {errors.lastName && <p className="mt-1 text-xs text-red-600">{errors.lastName.message}</p>}
                                    </div>
                                </div>
                                <button
                                    type="submit"
                                    disabled={isSubmitting || updateMutation.isPending}
                                    className="rounded-lg bg-black px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                                >
                                    {updateMutation.isPending ? "Saving…" : "Save changes"}
                                </button>
                            </form>
                        </div>

                        <div className="space-y-6">
                            <div className="rounded-2xl border bg-white p-6">
                                <h3 className="text-sm font-semibold text-gray-700">Quick links</h3>
                                <div className="mt-3 flex flex-col gap-2">
                                    <Link to="/assignments" className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">Assignments</Link>
                                    <Link to="/notifications" className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">Notifications</Link>
                                    <Link to="/courses" className="rounded-lg border px-3 py-2 text-sm hover:bg-gray-50">Courses</Link>
                                </div>
                            </div>

                            <div className="rounded-2xl border border-red-200 bg-red-50 p-6">
                                <h3 className="text-sm font-semibold text-red-700">Danger zone</h3>
                                <p className="mt-2 text-sm text-red-700">Deleting the account is irreversible.</p>
                                <button
                                    onClick={() => { if (window.confirm("Delete your account?")) deleteMutation.mutate(); }}
                                    disabled={deleteMutation.isPending}
                                    className="mt-3 rounded-lg border border-red-300 bg-white px-3 py-2 text-sm font-medium text-red-700 hover:bg-red-50 disabled:opacity-60"
                                >
                                    {deleteMutation.isPending ? "Deleting…" : "Delete account"}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
