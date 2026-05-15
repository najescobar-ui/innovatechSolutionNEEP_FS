import { useEffect, useMemo, useState } from "react";
import { CalendarDays, Search, User2 } from "lucide-react";
import { Card, CardBody } from "../components/Card";
import { Badge } from "../components/Badge";
import { api } from "../api/client";

type Proyecto = {
  id: number;
  nombre: string;
  descripcion: string | null;
  estado: string;
  fechaInicio: string | null;
  fechaFinPlanificada: string | null;
  responsableId: string | null;
};

type Resp = { status: string; items: Proyecto[] };

// orden y tono semantico por estado
const ESTADOS: { value: string; label: string; tone: "info" | "success" | "neutral" | "danger" }[] = [
  { value: "TODOS",         label: "Todos",         tone: "neutral" },
  { value: "PLANIFICACION", label: "Planificacion", tone: "info" },
  { value: "EN_CURSO",      label: "En curso",      tone: "success" },
  { value: "COMPLETADO",    label: "Completado",    tone: "neutral" },
  { value: "CANCELADO",     label: "Cancelado",     tone: "danger" },
];

const TONO_BADGE: Record<string, "info" | "success" | "neutral" | "danger"> = {
  PLANIFICACION: "info",
  EN_CURSO: "success",
  COMPLETADO: "neutral",
  CANCELADO: "danger",
};

const fmt = new Intl.DateTimeFormat("es-CL", { day: "2-digit", month: "short", year: "numeric" });
function formatFecha(iso: string | null) {
  if (!iso) return "—";
  return fmt.format(new Date(iso));
}

export function Proyectos() {
  const [data, setData] = useState<Resp | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [estado, setEstado] = useState("TODOS");

  useEffect(() => {
    api.get<Resp>("/proyectos")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error"));
  }, []);

  const filtrados = useMemo(() => {
    if (!data) return [];
    const txt = q.trim().toLowerCase();
    return data.items.filter((p) => {
      const matchTxt = !txt || p.nombre.toLowerCase().includes(txt) || (p.descripcion ?? "").toLowerCase().includes(txt);
      const matchEstado = estado === "TODOS" || p.estado === estado;
      return matchTxt && matchEstado;
    });
  }, [data, q, estado]);

  return (
    <div className="space-y-6 max-w-6xl">
      <header className="flex items-end justify-between gap-4">
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">Proyectos</h1>
          <p className="text-sm text-slate-500 mt-1">
            {data ? `${data.items.length} en cartera` : "cargando..."}
            {data && filtrados.length !== data.items.length && ` · ${filtrados.length} visibles`}
          </p>
        </div>
        {data?.status === "datos no disponibles" && (
          <Badge tone="warning">fuente caida — datos no disponibles</Badge>
        )}
      </header>

      <Card>
        <div className="px-6 py-3 border-b border-slate-200 flex flex-col md:flex-row md:items-center gap-3 md:justify-between">
          <div className="relative md:w-80">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Buscar por nombre o descripcion"
              className="w-full pl-9 pr-3 py-2 text-sm border border-slate-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            />
          </div>
          <div className="flex flex-wrap gap-2">
            {ESTADOS.map((e) => {
              const active = estado === e.value;
              return (
                <button
                  key={e.value}
                  onClick={() => setEstado(e.value)}
                  className={`text-xs font-medium px-3 py-1.5 rounded-full ring-1 ring-inset transition-colors ${
                    active
                      ? "bg-indigo-600 text-white ring-indigo-600"
                      : "bg-white text-slate-700 ring-slate-300 hover:bg-slate-50"
                  }`}
                >
                  {e.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-slate-50 text-slate-500 text-xs uppercase tracking-wide">
              <tr>
                <th className="text-left font-medium px-6 py-3 w-12">#</th>
                <th className="text-left font-medium px-6 py-3">Proyecto</th>
                <th className="text-left font-medium px-6 py-3">Estado</th>
                <th className="text-left font-medium px-6 py-3">Inicio</th>
                <th className="text-left font-medium px-6 py-3">Fin planif.</th>
                <th className="text-left font-medium px-6 py-3">Responsable</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {err && (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-rose-700">
                    No se pudo cargar la lista: {err}
                  </td>
                </tr>
              )}
              {!err && !data && (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-500">
                    Cargando...
                  </td>
                </tr>
              )}
              {data && filtrados.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-slate-500">
                    No hay proyectos con esos filtros.
                  </td>
                </tr>
              )}
              {filtrados.map((p) => (
                <tr key={p.id} className="hover:bg-slate-50/60 transition-colors">
                  <td className="px-6 py-4 text-slate-400 tabular-nums">{p.id}</td>
                  <td className="px-6 py-4">
                    <div className="font-medium text-slate-900">{p.nombre}</div>
                    {p.descripcion && (
                      <div className="text-xs text-slate-500 mt-0.5 line-clamp-1">{p.descripcion}</div>
                    )}
                  </td>
                  <td className="px-6 py-4">
                    <Badge tone={TONO_BADGE[p.estado] ?? "neutral"}>
                      {p.estado.replace(/_/g, " ").toLowerCase()}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 text-slate-600">
                    <div className="flex items-center gap-1.5">
                      <CalendarDays size={14} className="text-slate-400" />
                      {formatFecha(p.fechaInicio)}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-slate-600">
                    <div className="flex items-center gap-1.5">
                      <CalendarDays size={14} className="text-slate-400" />
                      {formatFecha(p.fechaFinPlanificada)}
                    </div>
                  </td>
                  <td className="px-6 py-4 text-slate-600">
                    {p.responsableId ? (
                      <div className="flex items-center gap-2">
                        <div className="w-6 h-6 rounded-full bg-slate-200 text-slate-600 text-xs flex items-center justify-center">
                          <User2 size={12} />
                        </div>
                        <span className="font-mono text-xs">{p.responsableId}</span>
                      </div>
                    ) : (
                      <span className="text-slate-400">—</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
