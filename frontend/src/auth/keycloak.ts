/** Config de Keycloak para login propio por password grant (sin keycloak-js redirect). */
export const KC = {
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
};

export const TOKEN_ENDPOINT =
  `${KC.url}/realms/${KC.realm}/protocol/openid-connect/token`;
