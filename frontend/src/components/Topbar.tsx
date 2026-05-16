import { useLocation } from "react-router-dom";

const titulos: Record<string, string> = {
  "/":          "Dashboard",
  "/proyectos": "Proyectos",
  "/recursos":  "Recursos",
  "/analitica": "Analitica",
};

export function Topbar() {
  const { pathname } = useLocation();
  const titulo = titulos[pathname] ?? pathname.replace("/", "");

  return (
    <header className="h-12 flex items-center justify-between px-6 border-b border-border bg-bg">
      <h1 className="text-[13px] font-medium text-fg">{titulo}</h1>
      {/* slot derecho reservado: acciones globales (search, notif...) entran aca cuando existan */}
      <div />
    </header>
  );
}
