import { NavLink } from "react-router-dom";
import { LayoutDashboard, FolderKanban, Users, BarChart3 } from "lucide-react";

const items = [
  { to: "/",          label: "Dashboard", Icon: LayoutDashboard },
  { to: "/proyectos", label: "Proyectos", Icon: FolderKanban },
  { to: "/recursos",  label: "Recursos",  Icon: Users },
  { to: "/analitica", label: "Analitica", Icon: BarChart3 },
];

export function Sidebar() {
  return (
    <aside className="w-60 shrink-0 bg-slate-900 text-slate-100 flex flex-col">
      <div className="h-14 flex items-center px-6 border-b border-slate-800">
        <span className="font-semibold tracking-tight">Innovatech</span>
      </div>
      <nav className="flex-1 px-3 py-4 space-y-1">
        {items.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/"}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-md px-3 py-2 text-sm transition-colors ${
                isActive
                  ? "bg-slate-800 text-white"
                  : "text-slate-300 hover:bg-slate-800/60 hover:text-white"
              }`
            }
          >
            <Icon size={16} className="shrink-0" />
            <span>{label}</span>
          </NavLink>
        ))}
      </nav>
      <div className="px-6 py-3 text-xs text-slate-500 border-t border-slate-800">
        v0.1.0 — dev
      </div>
    </aside>
  );
}
