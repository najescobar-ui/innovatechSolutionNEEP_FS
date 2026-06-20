import Keycloak from "keycloak-js";

/** Instancia unica para toda la app (un Keycloak() por documento o se rompe). */
export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});
