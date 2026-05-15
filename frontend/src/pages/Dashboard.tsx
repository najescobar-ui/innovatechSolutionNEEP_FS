import { useEffect, useState } from "react";
import {
  AlertOctagon, AlertTriangle, Briefcase, CheckCircle2, Clock, FolderKanban,
  Gauge, Layers, ListChecks, Sparkles, TrendingUp, Users,
} from "lucide-react";
import {
  Bar, BarChart, Cell, Legend, Pie, PieChart, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from "recharts";
import type { LucideIcon } from "lucide-react";
import { Card, CardBody, CardHeader } from "../components/Card";
import { Badge } from "../components/Badge";
import { api } from "../api/client";
import { useAuth } from "../auth/useAuth";

type Dashboard = {
  role: "PM" | "DEV" | "DIR";
  proyectosSupervisados?: number;
  tareasEnRiesgo?: number;
  proximosHitos?: string[];
  tareasAsignadas?: number;
  tareasPendientes?: number;
  proyectosEnCurso?: string[];
  proyectosActivos?: number;
  porcentajeUtilizacion?: number;
  alertasGlobales?: number;
};

type Kpis = {
  status: string;
  proyectosActivos: number;
  proyectosAtrasados: number;
  totalRecursosActivos: number;
  capacidadSemanalTotalHoras: number;
  promedioHorasPorRecurso: number;
  recursosPorRol: Record<string, number>;
  proyectosPorEstado: Record<string, number>;
};

// Paleta consistente para los charts
const COLORS = ["#6366f1", "#10b981", "#f59e0b", "#ef4444", "#06b6d4", "#8b5cf6"];

export function Dashboard() {
  const { fullName, username, roles } = useAuth();
  const [data, setData] = useState<Dashboard | null>(null);
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    api.get<Dashboard>("/dashboard")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error desconocido"));

    // Los KPIs los usa el DIR. Para PM/DEV no hace falta cargarlos.
    if (roles.includes("DIR")) {
      api.get<Kpis>("/kpis").then((r) => setKpis(r.data)).catch(() => {});
    }
  }, [roles.join(",")]);

  return (
    <div className="space-y-6 max-w-6xl">
      <Hero fullName={fullName} username={username} roles={roles} />

      {err && (
        <Card>
          <CardBody>
            <p className="text-rose-700 text-sm">No se pudo cargar el dashboard: {err}</p>
          </CardBody>
        </Card>
      )}

      {!err && !data && <SkeletonGrid />}

      {data?.role === "PM" && <DashboardPM data={data} />}
      {data?.role === "DEV" && <DashboardDev data={data} />}
      {data?.role === "DIR" && <DashboardDir data={data} kpis={kpis} />}
    </div>
  );
}

function Hero({ fullName, username, roles }: { fullName: string; username: string; roles: string[] }) {
  const rol = roles[0] ?? "";
  const subtitulo: Record<string, string> = {
    PM: "Tu cartera de proyectos y los hitos del proximo trimestre.",
    DEV: "Tu carga de trabajo y los proyectos en los que estas asignado.",
    DIR: "Indicadores ejecutivos y salud general de la operacion.",
  };
  return (
    <div className="flex items-start justify-between gap-6">
      <div>
        <h1 className="text-2xl font-semibold text-slate-900">
          Hola, {fullName || username}
        </h1>
        <p className="text-sm text-slate-500 mt-1 max-w-xl">
          {subtitulo[rol] ?? "Bienvenido a Innovatech."}
        </p>
      </div>
      <div className="flex items-center gap-2 shrink-0">
        <Badge tone="info">@{username}</Badge>
        {rol && <Badge tone="neutral">Rol: {rol}</Badge>}
      </div>
    </div>
  );
}

function MetricCard({
  Icon, label, value, sub, tone = "indigo", progress,
}: {
  Icon: LucideIcon;
  label: string;
  value: number | string;
  sub?: string;
  tone?: "indigo" | "emerald" | "amber" | "rose";
  progress?: { pct: number; label?: string };
}) {
  const ringTone: Record<string, string> = {
    indigo: "bg-indigo-50 text-indigo-600",
    emerald: "bg-emerald-50 text-emerald-600",
    amber: "bg-amber-50 text-amber-600",
    rose: "bg-rose-50 text-rose-600",
  };
  const barTone: Record<string, string> = {
    indigo: "bg-indigo-500",
    emerald: "bg-emerald-500",
    amber: "bg-amber-500",
    rose: "bg-rose-500",
  };
  return (
    <Card>
      <CardBody className="space-y-3">
        <div className="flex items-start justify-between">
          <div>
            <div className="text-xs font-medium uppercase tracking-wide text-slate-500">{label}</div>
            <div className="mt-1 text-3xl font-semibold text-slate-900">{value}</div>
            {sub && <div className="text-xs text-slate-500 mt-1">{sub}</div>}
          </div>
          <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${ringTone[tone]}`}>
            <Icon size={20} />
          </div>
        </div>
        {progress && (
          <div>
            <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
              <div
                className={`h-full ${barTone[tone]} transition-all`}
                style={{ width: `${Math.min(100, Math.max(0, progress.pct))}%` }}
              />
            </div>
            {progress.label && <div className="text-xs text-slate-500 mt-1.5">{progress.label}</div>}
          </div>
        )}
      </CardBody>
    </Card>
  );
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
      {[0, 1, 2].map((i) => (
        <Card key={i}>
          <CardBody>
            <div className="h-3 w-24 bg-slate-200 rounded animate-pulse" />
            <div className="h-8 w-16 bg-slate-200 rounded mt-3 animate-pulse" />
          </CardBody>
        </Card>
      ))}
    </div>
  );
}

function DashboardPM({ data }: { data: Dashboard }) {
  const total = data.proyectosSupervisados ?? 0;
  const enRiesgo = data.tareasEnRiesgo ?? 0;
  const pctRiesgo = total === 0 ? 0 : Math.round((enRiesgo / total) * 100);
  const cumplimiento = Math.max(0, 100 - pctRiesgo);
  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <MetricCard
          Icon={FolderKanban}
          label="Proyectos supervisados"
          value={total}
          sub="En tu cartera actual"
          tone="indigo"
        />
        <MetricCard
          Icon={AlertTriangle}
          label="Tareas en riesgo"
          value={enRiesgo}
          sub={`${pctRiesgo}% del total`}
          tone="amber"
          progress={{ pct: pctRiesgo, label: `${pctRiesgo}% requiere atencion` }}
        />
        <MetricCard
          Icon={CheckCircle2}
          label="Cumplimiento"
          value={`${cumplimiento}%`}
          sub="Tareas dentro de plazo"
          tone="emerald"
          progress={{ pct: cumplimiento }}
        />
      </div>

      <Card>
        <CardHeader title="Proximos hitos" subtitle="Lo que viene en las proximas semanas" />
        <CardBody className="!py-0">
          <ul className="divide-y divide-slate-100">
            {(data.proximosHitos ?? []).map((h, i) => (
              <li key={i} className="py-3 flex items-center justify-between text-sm">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-md bg-indigo-50 text-indigo-600 flex items-center justify-center">
                    <Sparkles size={16} />
                  </div>
                  <span className="text-slate-700">{h}</span>
                </div>
                <Badge tone="info">proximo</Badge>
              </li>
            ))}
            {(data.proximosHitos ?? []).length === 0 && (
              <li className="py-6 text-sm text-slate-500 text-center">Sin hitos cargados.</li>
            )}
          </ul>
        </CardBody>
      </Card>
    </>
  );
}

function DashboardDev({ data }: { data: Dashboard }) {
  const asignadas = data.tareasAsignadas ?? 0;
  const pendientes = data.tareasPendientes ?? 0;
  const completadas = Math.max(0, asignadas - pendientes);
  const pctCompletadas = asignadas === 0 ? 0 : Math.round((completadas / asignadas) * 100);
  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <MetricCard
          Icon={ListChecks}
          label="Tareas asignadas"
          value={asignadas}
          sub="Total en tu cola"
          tone="indigo"
        />
        <MetricCard
          Icon={Clock}
          label="Pendientes"
          value={pendientes}
          sub={`${asignadas - pendientes} ya completadas`}
          tone="amber"
        />
        <MetricCard
          Icon={TrendingUp}
          label="Progreso"
          value={`${pctCompletadas}%`}
          sub="Tareas completadas"
          tone="emerald"
          progress={{ pct: pctCompletadas }}
        />
      </div>

      <Card>
        <CardHeader title="Proyectos en curso" subtitle="Donde estas asignado actualmente" />
        <CardBody>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {(data.proyectosEnCurso ?? []).map((p, i) => (
              <div
                key={i}
                className="border border-slate-200 rounded-md p-4 flex items-center gap-3 hover:bg-slate-50 transition-colors"
              >
                <div className="w-9 h-9 rounded-md bg-emerald-50 text-emerald-600 flex items-center justify-center">
                  <Layers size={18} />
                </div>
                <div className="min-w-0">
                  <div className="text-sm font-medium text-slate-900 truncate">{p}</div>
                  <div className="text-xs text-slate-500">en curso</div>
                </div>
              </div>
            ))}
            {(data.proyectosEnCurso ?? []).length === 0 && (
              <div className="col-span-2 py-6 text-sm text-slate-500 text-center">
                No estas asignado a ningun proyecto.
              </div>
            )}
          </div>
        </CardBody>
      </Card>
    </>
  );
}

function DashboardDir({ data, kpis }: { data: Dashboard; kpis: Kpis | null }) {
  const utiPct = Math.round((data.porcentajeUtilizacion ?? 0) * 100);

  const rolesData = kpis?.recursosPorRol
    ? Object.entries(kpis.recursosPorRol).map(([name, value]) => ({ name, value }))
    : [];
  const estadosData = kpis?.proyectosPorEstado
    ? Object.entries(kpis.proyectosPorEstado).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <MetricCard
          Icon={Briefcase}
          label="Proyectos activos"
          value={data.proyectosActivos ?? 0}
          sub="En cartera global"
          tone="indigo"
        />
        <MetricCard
          Icon={Gauge}
          label="Utilizacion"
          value={`${utiPct}%`}
          sub="Capacidad asignada"
          tone="emerald"
          progress={{ pct: utiPct }}
        />
        <MetricCard
          Icon={AlertOctagon}
          label="Alertas globales"
          value={data.alertasGlobales ?? 0}
          sub="Requieren accion"
          tone="rose"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        <Card className="lg:col-span-2">
          <CardHeader title="Recursos por rol" subtitle="Distribucion de talento activo" />
          <CardBody>
            {rolesData.length === 0 ? (
              <p className="text-sm text-slate-500">Cargando KPIs...</p>
            ) : (
              <ResponsiveContainer width="100%" height={260}>
                <PieChart>
                  <Pie
                    data={rolesData}
                    dataKey="value"
                    nameKey="name"
                    innerRadius={60}
                    outerRadius={95}
                    paddingAngle={2}
                  >
                    {rolesData.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={32} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Capacidad" subtitle="Horas semanales" />
          <CardBody className="space-y-4">
            <div>
              <div className="text-xs font-medium uppercase tracking-wide text-slate-500">Total</div>
              <div className="mt-1 text-3xl font-semibold text-slate-900">
                {kpis?.capacidadSemanalTotalHoras ?? "—"}
                <span className="text-base text-slate-500 font-normal"> h</span>
              </div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wide text-slate-500">Promedio</div>
              <div className="mt-1 text-2xl font-semibold text-slate-900">
                {kpis?.promedioHorasPorRecurso?.toFixed(1) ?? "—"}
                <span className="text-sm text-slate-500 font-normal"> h/recurso</span>
              </div>
            </div>
            <div>
              <div className="text-xs font-medium uppercase tracking-wide text-slate-500">Recursos activos</div>
              <div className="mt-1 flex items-center gap-2">
                <Users size={16} className="text-slate-400" />
                <span className="text-xl font-semibold text-slate-900">{kpis?.totalRecursosActivos ?? "—"}</span>
              </div>
            </div>
          </CardBody>
        </Card>
      </div>

      <Card>
        <CardHeader title="Proyectos por estado" subtitle="Cartera global agrupada" />
        <CardBody>
          {estadosData.length === 0 ? (
            <p className="text-sm text-slate-500">Cargando KPIs...</p>
          ) : (
            <ResponsiveContainer width="100%" height={Math.max(180, estadosData.length * 50)}>
              <BarChart data={estadosData} layout="vertical" margin={{ left: 20 }}>
                <XAxis type="number" allowDecimals={false} stroke="#94a3b8" fontSize={12} />
                <YAxis type="category" dataKey="name" stroke="#94a3b8" fontSize={12} width={110} />
                <Tooltip cursor={{ fill: "rgba(99,102,241,0.05)" }} />
                <Bar dataKey="value" radius={[0, 6, 6, 0]}>
                  {estadosData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardBody>
      </Card>
    </>
  );
}
