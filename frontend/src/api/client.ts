import axios from "axios";
import { keycloak } from "../auth/keycloak";

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE,
});

/* Pone el token en cada request. Si esta por expirar, intenta refrescar. */
api.interceptors.request.use(async (config) => {
  if (keycloak.token) {
    try {
      await keycloak.updateToken(15);
    } catch {
      /* si no se pudo refrescar, dejamos que el server responda 401 */
    }
    config.headers.Authorization = `Bearer ${keycloak.token}`;
  }
  return config;
});
