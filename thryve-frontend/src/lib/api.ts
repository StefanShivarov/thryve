import axios, { AxiosError, AxiosRequestConfig } from "axios";

const API_URL = import.meta.env.VITE_API_URL;

// ----- Axios instance -----
export const api = axios.create({
  baseURL: API_URL,
  withCredentials: false,
});

// ----- Token helpers -----
const ACCESS_KEY = "accessToken";
const REFRESH_KEY = "refreshToken";

const getAccess = () => localStorage.getItem(ACCESS_KEY);
const getRefresh = () => localStorage.getItem(REFRESH_KEY);

const setTokens = (access?: string, refresh?: string) => {
  if (access) localStorage.setItem(ACCESS_KEY, access);
  if (refresh) localStorage.setItem(REFRESH_KEY, refresh);
};

export const clearTokens = () => {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
};

// ----- JWT helpers -----
type JwtPayload = { exp?: number; [k: string]: any };
const decode = (t?: string): JwtPayload => {
  if (!t) return {};
  try {
    const [, b] = t.split(".");
    return JSON.parse(atob(b));
  } catch {
    return {};
  }
};
const isExpired = (t?: string, skewSec = 30) => {
  if (!t) return true;
  const { exp } = decode(t);
  if (!exp) return false;
  const nowSec = Math.floor(Date.now() / 1000);
  return exp - skewSec <= nowSec;
};

// ----- Refresh flow (single flight) -----
let refreshing = false;
let waiters: Array<(v: string | null) => void> = [];

const doRefresh = async (): Promise<string> => {
  if (refreshing) {
    return new Promise((resolve, reject) => {
      waiters.push((v) => (v ? resolve(v) : reject(new Error("REFRESH_FAILED"))));
    });
  }
  refreshing = true;
  try {
    const rt = getRefresh();
    if (!rt) throw new Error("NO_REFRESH");
    const { data } = await axios.post(
        `${API_URL}/api/auth/refresh-token`,
        { refreshToken: rt },
        { withCredentials: false }
    );
    const access = data?.accessToken ?? data?.data?.accessToken;
    const refresh = data?.refreshToken ?? data?.data?.refreshToken;
    if (!access) throw new Error("NO_ACCESS_IN_RESPONSE");
    setTokens(access, refresh);
    waiters.forEach((w) => w(access));
    waiters = [];
    return access;
  } catch (e) {
    waiters.forEach((w) => w(null));
    waiters = [];
    clearTokens();
    // For non-login flows we hard-redirect to login when refresh fails
    if (!window.location.pathname.startsWith("/login")) {
      window.location.assign("/login?expired=1");
    }
    throw e;
  } finally {
    refreshing = false;
  }
};

// ----- Small helpers -----
const isAuthEndpoint = (url?: string) => {
  if (!url) return false;
  const u = url.toString();
  return (
      u.includes("/api/auth/login") ||
      u.includes("/api/auth/register") ||
      u.includes("/api/auth/refresh-token")
  );
};

// Add an optional flag to skip auth redirect on specific requests
declare module "axios" {
  export interface AxiosRequestConfig {
    skipAuthRedirect?: boolean;
    _retry?: boolean; // internal flag for our retry logic
  }
}

// ----- Request interceptor -----
api.interceptors.request.use(
    async (config) => {
      let access = getAccess();
      // Proactive refresh if access token is near expiry
      if (access && isExpired(access, 30) && !isAuthEndpoint(config.url ?? "")) {
        try {
          access = await doRefresh();
        } catch {
          // doRefresh handles redirect and token clearing
        }
      }
      access = getAccess();
      if (access) {
        config.headers = config.headers || {};
        (config.headers as any).Authorization = `Bearer ${access}`;
      }
      return config;
    },
    (e) => Promise.reject(e)
);

// ----- Response interceptor -----
api.interceptors.response.use(
    (r) => r,
    async (err: AxiosError) => {
      const status = err.response?.status;
      const cfg = (err.config || {}) as AxiosRequestConfig;
      const url = cfg.url ?? "";

      // If the request is for an auth endpoint, or caller opted out, just surface the error
      if (status === 401 && (isAuthEndpoint(url) || cfg.skipAuthRedirect)) {
        return Promise.reject(err);
      }

      // For other endpoints: try refresh once then retry original request
      if (status === 401 && !cfg._retry) {
        cfg._retry = true;
        try {
          const access = await doRefresh();
          cfg.headers = cfg.headers || {};
          (cfg.headers as any).Authorization = `Bearer ${access}`;
          return api(cfg);
        } catch {
          // doRefresh already cleared tokens and redirected
          return Promise.reject(err);
        }
      }

      return Promise.reject(err);
    }
);
