import { useAuth } from "../auth/useAuth";
import { Badge } from "./Badge";
import { Button } from "./Button";

function iniciales(nombre: string) {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

const tonoRol: Record<string, "info" | "success" | "warning"> = {
  PM: "info",
  DEV: "success",
  DIR: "warning",
};

export function Topbar() {
  const { fullName, username, roles, logout } = useAuth();
  const rolPrincipal = roles[0] ?? "";

  return (
    <header className="h-14 bg-white border-b border-slate-200 flex items-center justify-end gap-4 px-6">
      <div className="flex items-center gap-3">
        <div className="text-right leading-tight">
          <div className="text-sm font-medium text-slate-900">{fullName || username}</div>
          <div className="text-xs text-slate-500">@{username}</div>
        </div>
        <div className="w-9 h-9 rounded-full bg-indigo-600 text-white text-sm font-semibold flex items-center justify-center">
          {iniciales(fullName || username)}
        </div>
        {rolPrincipal && <Badge tone={tonoRol[rolPrincipal] ?? "neutral"}>{rolPrincipal}</Badge>}
      </div>
      <Button variant="outline" onClick={logout}>Salir</Button>
    </header>
  );
}
