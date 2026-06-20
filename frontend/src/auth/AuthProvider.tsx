import { createContext, useEffect, useRef, useState } from "react";
import type { ReactNode } from "react";
import axios from "axios";
import { KC, TOKEN_ENDPOINT } from "./keycloak";

export type AuthCtx = {
  ready: boolean;
  authenticated: boolean;
  username: string;
  fullName: string;
  roles: string[];
  token: string | undefined;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
};

const initial: AuthCtx = {
  ready: false,
  authenticated: false,
  username: "",
  fullName: "",
  roles: [],
  token: undefined,
  login: async () => {},
  logout: () => {},
};

export const AuthContext = createContext<AuthCtx>(initial);

const ROLES_VALIDOS = ["PM", "DEV", "DIR"];
const TOKEN_KEY = "kc_token";
const REFRESH_KEY = "kc_refresh";

/** Decodifica el payload de un JWT (maneja base64url y UTF-8). */
function parseJwt(token: string): any | null {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + c.charCodeAt(0).toString(16).padStart(2, "0"))
        .join(""),
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

type Identity = Pick<AuthCtx, "username" | "fullName" | "roles" | "token">;

function identityFrom(accessToken: string): Identity {
  const tp = parseJwt(accessToken) ?? {};
  const realmRoles: string[] = tp?.realm_access?.roles ?? [];
  return {
    username: tp?.preferred_username ?? "",
    fullName:
      [tp?.given_name, tp?.family_name].filter(Boolean).join(" ") ||
      (tp?.preferred_username ?? ""),
    roles: realmRoles.filter((r) => ROLES_VALIDOS.includes(r)),
    token: accessToken,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [state, setState] = useState<AuthCtx>(initial);
  const refreshTimer = useRef<number | undefined>(undefined);
  const started = useRef(false);

  /** Programa el refresh ~30s antes de que expire el access token. */
  function scheduleRefresh(expiresInSec: number) {
    if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
    const delay = Math.max((expiresInSec - 30) * 1000, 5000);
    refreshTimer.current = window.setTimeout(() => {
      void doRefresh();
    }, delay);
  }

  function applyTokens(access: string, refresh: string, expiresIn: number) {
    localStorage.setItem(TOKEN_KEY, access);
    localStorage.setItem(REFRESH_KEY, refresh);
    const id = identityFrom(access);
    setState((s) => ({ ...s, ready: true, authenticated: true, ...id }));
    scheduleRefresh(expiresIn);
  }

  function clearSession() {
    if (refreshTimer.current) window.clearTimeout(refreshTimer.current);
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_KEY);
    setState((s) => ({
      ...s,
      ready: true,
      authenticated: false,
      username: "",
      fullName: "",
      roles: [],
      token: undefined,
    }));
  }

  async function login(username: string, password: string) {
    const form = new URLSearchParams({
      grant_type: "password",
      client_id: KC.clientId,
      username,
      password,
    });
    const { data } = await axios.post(TOKEN_ENDPOINT, form);
    applyTokens(data.access_token, data.refresh_token, data.expires_in);
  }

  async function doRefresh(): Promise<boolean> {
    const refresh = localStorage.getItem(REFRESH_KEY);
    if (!refresh) {
      clearSession();
      return false;
    }
    try {
      const form = new URLSearchParams({
        grant_type: "refresh_token",
        client_id: KC.clientId,
        refresh_token: refresh,
      });
      const { data } = await axios.post(TOKEN_ENDPOINT, form);
      applyTokens(data.access_token, data.refresh_token, data.expires_in);
      return true;
    } catch {
      clearSession();
      return false;
    }
  }

  useEffect(() => {
    if (started.current) return;
    started.current = true;

    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setState((s) => ({ ...s, ready: true }));
      return;
    }
    const tp = parseJwt(token);
    const notExpired = tp?.exp && tp.exp * 1000 > Date.now() + 5000;
    if (notExpired) {
      setState((s) => ({ ...s, ready: true, authenticated: true, ...identityFrom(token) }));
      scheduleRefresh(tp.exp - Math.floor(Date.now() / 1000));
    } else {
      void doRefresh();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const value: AuthCtx = { ...state, login, logout: clearSession };
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
