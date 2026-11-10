import axios from "axios";
export const baseURL = (
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080"
).replace(/\/$/, "");
export const api = axios.create({
  baseURL,
  withCredentials: true,
  timeout: 15000,
});
let token;
export async function refreshCsrf() {
  token = (await api.get("/api/auth/csrf")).data.token;
}
api.interceptors.request.use(async (config) => {
  if (!["get", "head", "options"].includes(config.method)) {
    if (!token) await refreshCsrf();
    config.headers["X-CSRF-TOKEN"] = token;
  }
  return config;
});
api.interceptors.response.use(
  (r) => r,
  (error) => {
    if (error.response?.status === 401)
      window.dispatchEvent(new Event("session-expired"));
    return Promise.reject(error);
  },
);
export function errorMessage(e) {
  return (
    e.response?.data?.message ||
    (e.code === "ECONNABORTED"
      ? "The request timed out. Please retry."
      : "Cannot connect to the server. Check that the backend is running.")
  );
}
export function imageURL(path) {
  return path ? baseURL + path : "";
}
// Load every page for relation selectors, independently of the visible table pagination.
export async function allRecords(resource) {
  let page = 0,
    rows = [],
    result;
  do {
    result = (
      await api.get(`/api/${resource}`, { params: { page, size: 100 } })
    ).data;
    rows.push(...result.content);
    page++;
  } while (page < result.totalPages);
  return rows;
}
