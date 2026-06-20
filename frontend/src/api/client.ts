import axios from "axios";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
});

/* Adjunta el access token (lo refresca proactivamente el AuthProvider). */
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("kc_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
