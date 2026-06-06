import { useLocation } from "react-router-dom";

const titles: Record<string, string> = {
  "/":          "Dashboard",
  "/projects":  "Projects",
  "/resources": "Resources",
  "/analytics": "Analytics",
};

export function Topbar() {
  const { pathname } = useLocation();
  const title = titles[pathname] ?? pathname.replace("/", "");

  return (
    <header className="h-12 flex items-center justify-between px-6 border-b border-border bg-bg">
      <h1 className="text-[13px] font-medium text-fg">{title}</h1>
      {/* slot derecho reservado: acciones globales (search, notif...) entran aca cuando existan */}
      <div />
    </header>
  );
}
