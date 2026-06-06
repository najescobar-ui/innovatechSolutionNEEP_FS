import { useEffect, useRef, useState } from "react";
import { NavLink } from "react-router-dom";
import {
  LayoutDashboard,
  FolderKanban,
  Users,
  LogOut,
  ChevronsUpDown,
} from "lucide-react";
import { useAuth } from "../auth/useAuth";

// "Analytics" oculta hasta que se libere su vista (proxima iteracion).
const items = [
  { to: "/",          label: "Dashboard", Icon: LayoutDashboard },
  { to: "/projects",  label: "Projects",  Icon: FolderKanban },
  { to: "/resources", label: "Resources", Icon: Users },
];

function initials(name: string) {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

export function Sidebar() {
  return (
    <aside className="w-60 shrink-0 bg-bg text-fg flex flex-col border-r border-border">
      <div className="px-5 pt-4 pb-3 flex flex-col items-start gap-1.5">
        <img
          src="/logoLuffy.png"
          alt="InnovaTech"
          className="w-9 h-9 object-contain select-none"
          draggable={false}
        />
        <span className="font-brand text-[15px] font-semibold text-fg leading-none">
          Innova<span className="text-accent">Tech</span>
        </span>
      </div>

      <nav className="flex-1 px-2 py-2 space-y-0.5">
        {items.map(({ to, label, Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === "/"}
            className={({ isActive }) =>
              `relative flex items-center gap-2.5 rounded px-3 py-1.5 text-[13px] transition-colors ${
                isActive
                  ? "bg-[#1F1F1F] text-fg"
                  : "text-fg-muted hover:bg-surface2 hover:text-fg"
              }`
            }
          >
            {({ isActive }) => (
              <>
                {isActive && (
                  <span className="absolute left-0 top-1.5 bottom-1.5 w-[2px] rounded-r bg-accent" />
                )}
                <Icon size={16} className="shrink-0" />
                <span>{label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      <UserBlock />
    </aside>
  );
}

function UserBlock() {
  const { fullName, username, roles, logout } = useAuth();
  const mainRole = roles[0] ?? "";
  const display = fullName || username || "user";
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!open) return;
    const onClick = (e: MouseEvent) => {
      if (!ref.current?.contains(e.target as Node)) setOpen(false);
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, [open]);

  return (
    <div ref={ref} className="relative p-2 border-t border-border">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="w-full flex items-center gap-2.5 rounded px-2 py-1.5 hover:bg-surface2 transition-colors text-left"
      >
        <div className="w-7 h-7 rounded-full bg-[#1F1F1F] text-fg text-[11px] font-semibold flex items-center justify-center shrink-0">
          {initials(display)}
        </div>
        <div className="flex-1 min-w-0 leading-tight">
          <div className="text-[13px] text-fg truncate">{display}</div>
          <div className="text-[11px] text-fg-muted truncate">
            {mainRole ? `@${username} · ${mainRole}` : `@${username}`}
          </div>
        </div>
        <ChevronsUpDown size={14} className="text-fg-subtle shrink-0" />
      </button>

      {open && (
        <div className="absolute left-2 right-2 bottom-[calc(100%-0.5rem)] mb-2 bg-surface border border-border rounded-md shadow-lg overflow-hidden">
          <button
            type="button"
            onClick={logout}
            className="w-full flex items-center gap-2 px-3 py-2 text-[13px] text-fg-muted hover:bg-surface2 hover:text-fg transition-colors"
          >
            <LogOut size={14} />
            <span>Log out</span>
          </button>
        </div>
      )}
    </div>
  );
}
