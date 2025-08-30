import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { api } from "../lib/api";

const passwordSchema = z
    .string()
    .min(8, "Password must be at least 8 characters long.")
    .regex(/[A-Z]/, "Password must contain at least one uppercase letter.")
    .regex(/[a-z]/, "Password must contain at least one lowercase letter.")
    .regex(/\d/, "Password must contain at least one digit.");

const schema = z.object({
  email: z.string().email("Enter a valid email."),
  password: passwordSchema,
});
type FormValues = z.infer<typeof schema>;

export default function Login() {
  const [serverError, setServerError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    clearErrors,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    mode: "onTouched",
  });

  const onSubmit = async (values: FormValues) => {
    setServerError(null);
    try {
      const { data } = await api.post("/api/auth/login", values,{ skipAuthRedirect: true });
      const tokens = data?.data ?? data;

      if (!tokens?.accessToken) {
        setServerError("Unexpected login response. Please try again.");
        return;
      }
      localStorage.setItem("accessToken", tokens.accessToken);
      if (tokens.refreshToken) localStorage.setItem("refreshToken", tokens.refreshToken);
      window.location.href = "/";
    } catch (err: any) {
      const status = err?.response?.status;
      const payload = err?.response?.data;
      const msg = String(payload?.message ?? "").toLowerCase();

      if (
          status === 401 ||
          /bad credentials|invalid credentials|incorrect/i.test(msg)
      ) {
        setServerError(null);
        setError("password", {
          type: "credentials",
          message: "Incorrect email or password.",
        });
        return;
      }

      // Other errors → compact banner
      const firstDetail =
          (Array.isArray(payload?.errors) && payload.errors[0]) ||
          payload?.message ||
          "An unexpected error occurred!";
      setServerError(`Login error: ${firstDetail}`);
    }
  };

  return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
        <form
            onSubmit={handleSubmit(onSubmit)}
            className="w-full max-w-sm bg-white p-6 rounded-2xl shadow"
            noValidate
        >
          <h1 className="text-2xl font-semibold mb-4">Sign in</h1>

          {serverError && (
              <div
                  className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm text-red-700"
                  aria-live="polite"
              >
                {serverError}
              </div>
          )}

          <label className="block mb-3">
            <span className="text-sm">Email</span>
            <input
                className={`mt-1 w-full border rounded-lg p-2 ${
                    errors.email || errors.password ? "bg-blue-50" : ""
                }`}
                type="email"
                autoComplete="email"
                {...register("email", {
                  onChange: () => clearErrors("password"), // typing again clears creds error
                })}
            />
            {errors.email && (
                <p className="mt-1 text-red-600 text-sm">{errors.email.message}</p>
            )}
          </label>

          <label className="block mb-4">
            <span className="text-sm">Password</span>
            <input
                className="mt-1 w-full border rounded-lg p-2"
                type="password"
                autoComplete="current-password"
                {...register("password", {
                  onChange: () => clearErrors("password"),
                })}
            />
            {errors.password ? (
                <p className="mt-1 text-red-600 text-sm">{errors.password.message}</p>
            ) : (
                <p className="mt-1 text-xs text-gray-500">
                  Must be at least 8 characters and include an uppercase letter, a lowercase letter, and a digit.
                </p>
            )}
          </label>

          <button
              disabled={isSubmitting}
              className="w-full py-2 rounded-lg bg-black text-white disabled:opacity-60"
              type="submit"
          >
            {isSubmitting ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
  );
}
