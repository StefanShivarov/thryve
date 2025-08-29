import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL;

export const api = axios.create({
  baseURL: API_URL,
  withCredentials: true, // keep true; safe if backend doesn't use cookies
});

const getAccess = () => localStorage.getItem("accessToken");
const getRefresh = () => localStorage.getItem("refreshToken");

const setTokens = (access?: string, refresh?: string) => {
  if (access) localStorage.setItem("accessToken", access);
  if (refresh) localStorage.setItem("refreshToken", refresh);
};

export const clearTokens = () => {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
};

api.interceptors.request.use((config) => {
  const t = getAccess();
  if (t) (config.headers ||= {}).Authorization = `Bearer ${t}`;
  return config;
});

let refreshing = false;
let queue: ((t: string | null) => void)[] = [];

api.interceptors.response.use(
  (r) => r,
  async (err) => {
    const orig: any = err.config || {};
    if (err.response?.status === 401 && !orig._retry) {
      if (refreshing) {
        return new Promise((resolve, reject) => {
          queue.push((newT) => {
            if (!newT) return reject(err);
            orig.headers = orig.headers || {};
            orig.headers.Authorization = `Bearer ${newT}`;
            resolve(api(orig));
          });
        });
      }
      orig._retry = true;
      refreshing = true;
      try {
        const rt = getRefresh();
        if (!rt) throw new Error("No refresh token");
        const { data } = await axios.post(`${API_URL}/api/auth/refresh-token`, { refreshToken: rt }, { withCredentials: true });
        setTokens(data?.accessToken, data?.refreshToken);
        queue.forEach(cb => cb(data?.accessToken ?? null));
        queue = [];
        return api(orig);
      } catch (e) {
        queue.forEach(cb => cb(null));
        queue = [];
        clearTokens();
        window.location.href = "/login";
        throw e;
      } finally {
        refreshing = false;
      }
    }
    throw err;
  }
);
