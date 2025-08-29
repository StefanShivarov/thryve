import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { api } from "../lib/api";

const schema = z.object({
  email: z.string().email(),
  password: z.string().min(6),
});
type FormValues = z.infer<typeof schema>;

export default function Login() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormValues>({
    resolver: zodResolver(schema),
  });

  const onSubmit = async (values: FormValues) => {
    try {
      const { data } = await api.post("/api/auth/login", values);

      // Your backend returns flat: { accessToken, refreshToken }
      // but handle both flat and { data: {...} } just in case
      const tokens = data?.data ?? data;

      if (!tokens?.accessToken) {
        console.error("Unexpected login response:", data);
        alert("Login failed: no accessToken in response.");
        return;
      }

      localStorage.setItem("accessToken", tokens.accessToken);
      if (tokens.refreshToken) localStorage.setItem("refreshToken", tokens.refreshToken);

      window.location.href = "/";
    } catch (err: any) {
      console.error("Login error:", err?.response?.data ?? err);
      alert(`Login error: ${err?.response?.status ?? ""} ${JSON.stringify(err?.response?.data ?? {})}`);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 p-6">
      <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-sm bg-white p-6 rounded-2xl shadow">
        <h1 className="text-2xl font-semibold mb-4">Sign in</h1>
        <label className="block mb-2">
          <span className="text-sm">Email</span>
          <input className="mt-1 w-full border rounded-lg p-2" type="email" {...register("email")} />
          {errors.email && <p className="text-red-600 text-sm">{errors.email.message}</p>}
        </label>
        <label className="block mb-4">
          <span className="text-sm">Password</span>
          <input className="mt-1 w-full border rounded-lg p-2" type="password" {...register("password")} />
          {errors.password && <p className="text-red-600 text-sm">{errors.password.message}</p>}
        </label>
        <button disabled={isSubmitting} className="w-full py-2 rounded-lg bg-black text-white">
          {isSubmitting ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}
