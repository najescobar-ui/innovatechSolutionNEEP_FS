import { createContext, useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import { keycloak } from "./keycloak";

export type AuthCtx = {
  ready: boolean;
  authenticated: boolean;
  username: string;
  fullName: string;
  roles: string[];
  token: string | undefined;
  logout: () => void;
};

// Default mientras Keycloak inicializa
const initial: AuthCtx = {
  ready: false,
  authenticated: false,
  username: "",
  fullName: "",
  roles: [],
  token: undefined,
  logout: () => {},
};

export const AuthContext = createContext<AuthCtx>(initial);

const ROLES_VALIDOS = ["PM", "DEV", "DIR"];

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthCtx>(initial);
  // En StrictMode el effect corre 2 veces, este flag evita un doble init.
  const initStarted = useRef(false);

  useEffect(() => {
    if (initStarted.current) return;
    initStarted.current = true;

    keycloak
      .init({
        onLoad: "login-required",
        pkceMethod: "S256",
        checkLoginIframe: false,
      })
      .then((authenticated) => {
        const tp = keycloak.tokenParsed as any;
        const realmRoles: string[] = tp?.realm_access?.roles ?? [];
        const roles = realmRoles.filter((r) => ROLES_VALIDOS.includes(r));
        setState({
          ready: true,
          authenticated,
          username: tp?.preferred_username ?? "",
          fullName: [tp?.given_name, tp?.family_name].filter(Boolean).join(" ") || (tp?.preferred_username ?? ""),
          roles,
          token: keycloak.token,
          logout: () => keycloak.logout({ redirectUri: window.location.origin }),
        });
      })
      .catch((err) => {
        // Si falla el init, dejamos la pantalla "cargando" mostrando algo util.
        console.error("Keycloak init fallo:", err);
        setState((s) => ({ ...s, ready: true }));
      });

    // Refresh automatico del token un poco antes de que expire.
    keycloak.onTokenExpired = () => {
      keycloak.updateToken(30).then((refreshed) => {
        if (refreshed) {
          setState((s) => ({ ...s, token: keycloak.token }));
        }
      });
    };
  }, []);

  return <AuthContext.Provider value={state}>{children}</AuthContext.Provider>;
}
