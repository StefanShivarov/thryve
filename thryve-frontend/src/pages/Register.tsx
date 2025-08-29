import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { api } from "../lib/api";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

const schema = z.object({
    email: z.string().email(),
    username: z.string().min(3).max(50),
    firstName: z.string().min(2).max(50),
    lastName: z.string().min(2).max(50),
    password: z.string().min(8).regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/),
    confirmPassword: z.string().min(8),
}).refine((v) => v.password === v.confirmPassword, { path: ["confirmPassword"], message: "Passwords must match" });

type FormValues = z.infer<typeof schema>;

export default function Register() {
    const navigate = useNavigate();
    const [serverError, setServerError] = useState<string | null>(null);
    const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
        resolver: zodResolver(schema),
        mode: "onTouched",
    });

    const onSubmit = async (values: FormValues) => {
        setServerError(null);
        try {
            const reg = await api.post("/api/users", {
                email: values.email,
                username: values.username,
                firstName: values.firstName,
                lastName: values.lastName,
                password: values.password,
            });
            const created = reg.data?.data ?? reg.data;
            const createdId = created?.id;
            if (createdId) localStorage.setItem("userId", createdId);

            const loginRes = await api.post("/api/auth/login", {
                email: values.email,
                password: values.password,
            });
            const data = loginRes.data?.data ?? loginRes.data;
            const access = data?.accessToken;
            const refresh = data?.refreshToken;
            if (access) localStorage.setItem("accessToken", access);
            if (refresh) localStorage.setItem("refreshToken", refresh);
            navigate("/", { replace: true });
        } catch (e: any) {
            const msg = e?.response?.data?.message || e?.message || "Registration failed";
            setServerError(msg);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
            <div className="w-full max-w-md rounded-2xl border bg-white p-6 shadow-sm">
                <h1 className="text-2xl font-semibold">Create your account</h1>
                <p className="mt-1 text-sm text-gray-600">You’ll be signed in automatically after successful registration.</p>
                {serverError && <div className="mt-4 rounded-lg border border-red-200 bg-red-50 p-3 text-sm text-red-700">{serverError}</div>}
                <form className="mt-6 space-y-4" onSubmit={handleSubmit(onSubmit)}>
                    <div>
                        <label className="block text-sm font-medium">Email</label>
                        <input {...register("email")} type="email" className="mt-1 w-full rounded-lg border px-3 py-2" />
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
                    <div>
                        <label className="block text-sm font-medium">Password</label>
                        <input {...register("password")} type="password" className="mt-1 w-full rounded-lg border px-3 py-2" />
                        {errors.password && <p className="mt-1 text-xs text-red-600">Password must be 8+ chars with upper, lower, digit.</p>}
                    </div>
                    <div>
                        <label className="block text-sm font-medium">Confirm password</label>
                        <input {...register("confirmPassword")} type="password" className="mt-1 w-full rounded-lg border px-3 py-2" />
                        {errors.confirmPassword && <p className="mt-1 text-xs text-red-600">{errors.confirmPassword.message}</p>}
                    </div>
                    <button type="submit" disabled={isSubmitting} className="mt-2 w-full rounded-lg bg-black px-4 py-2 font-medium text-white disabled:opacity-60">
                        {isSubmitting ? "Creating..." : "Create account"}
                    </button>
                    <div className="text-center text-sm text-gray-600">
                        Already have an account? <a href="/login" className="underline">Sign in</a>
                    </div>
                </form>
            </div>
        </div>
    );
}
