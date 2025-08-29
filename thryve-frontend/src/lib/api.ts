import axios from "axios";

const API_URL = import.meta.env.VITE_API_URL;

export const api = axios.create({
  baseURL: API_URL,
  withCredentials: false,
});

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
    const { data } = await axios.post(`${API_URL}/api/auth/refresh-token`, { refreshToken: rt }, { withCredentials: false });
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
    window.location.href = "/login";
    throw e;
  } finally {
    refreshing = false;
  }
};

api.interceptors.request.use(
    async (config) => {
      let access = getAccess();
      if (access && isExpired(access, 30)) {
        try {
          access = await doRefresh();
        } catch {}
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

api.interceptors.response.use(
    (r) => r,
    async (err) => {
      const orig: any = err.config || {};
      if (err.response?.status === 401 && !orig._retry) {
        orig._retry = true;
        try {
          const access = await doRefresh();
          orig.headers = orig.headers || {};
          orig.headers.Authorization = `Bearer ${access}`;
          return api(orig);
        } catch {
          return Promise.reject(err);
        }
      }
      return Promise.reject(err);
    }
);
