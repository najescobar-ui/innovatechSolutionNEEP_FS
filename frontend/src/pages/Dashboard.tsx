import { useEffect, useMemo, useState } from "react";
import {
  CartesianGrid, Cell, Line, LineChart, Pie, PieChart, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from "recharts";
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
  porcentajeUtilizacion?: number;
  recursosPorRol: Record<string, number>;
  proyectosPorEstado: Record<string, number>;
};

type Historico = {
  status: string;
  utilizacion: { t: string; v: number }[];
  proyectosActivos: { t: string; v: number }[];
  deltas: {
    porcentajeUtilizacion: number | null;
    proyectosActivos: number | null;
    recursosActivos: number | null;
  };
};

const ROLE_COLORS: Record<string, string> = {
  DEV:      "rgb(var(--role-dev))",
  QA:       "rgb(var(--role-qa))",
  DEVOPS:   "rgb(var(--role-devops))",
  DESIGNER: "rgb(var(--role-designer))",
  PM:       "rgb(var(--role-pm))",
};
const FALLBACK_COLOR = "rgb(var(--fg-muted))";

const ESTADO_COLORS: Record<string, string> = {
  PLANIFICACION: "rgb(var(--info))",
  EN_CURSO:      "rgb(var(--success))",
  COMPLETADO:    "rgb(var(--fg-muted))",
  CANCELADO:     "rgb(var(--danger))",
  PAUSADO:       "rgb(var(--warning))",
};

const MAX_RANGO_DIAS = 30;

// Helpers de fecha: trabajamos con YYYY-MM-DD locales para los inputs nativos.
function isoDate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}
function hoyISO() { return isoDate(new Date()); }
function haceDiasISO(n: number) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return isoDate(d);
}
function diasEntre(desde: string, hasta: string): number {
  const a = new Date(desde + "T00:00:00");
  const b = new Date(hasta + "T00:00:00");
  return Math.round((b.getTime() - a.getTime()) / 86400000);
}

export function Dashboard() {
  const { fullName, username, roles } = useAuth();
  const [data, setData] = useState<Dashboard | null>(null);
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [historico, setHistorico] = useState<Historico | null>(null);
  const [err, setErr] = useState<string | null>(null);

  // Rango de fechas para el grafico de evolucion (default: ultimos 30 dias).
  const [desde, setDesde] = useState<string>(haceDiasISO(30));
  const [hasta, setHasta] = useState<string>(hoyISO());
  const [errRango, setErrRango] = useState<string | null>(null);

  useEffect(() => {
    api.get<Dashboard>("/dashboard")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error desconocido"));

    if (roles.includes("DIR")) {
      api.get<Kpis>("/kpis").then((r) => setKpis(r.data)).catch(() => {});
    }
  }, [roles.join(",")]);

  // Refetch historico cuando cambia el rango (con validacion).
  useEffect(() => {
    if (!roles.includes("DIR")) return;
    const dias = diasEntre(desde, hasta);
    if (dias < 0) { setErrRango("La fecha 'hasta' no puede ser anterior a 'desde'."); return; }
    if (dias > MAX_RANGO_DIAS) { setErrRango(`El rango maximo es ${MAX_RANGO_DIAS} dias.`); return; }
    setErrRango(null);
    api.get<Historico>(`/kpis/historico?desde=${desde}&hasta=${hasta}`)
      .then((r) => setHistorico(r.data))
      .catch(() => setHistorico(null));
  }, [roles.join(","), desde, hasta]);

  return (
    <div className="space-y-5 max-w-[1200px]">
      <Hero fullName={fullName} username={username} roles={roles} />

      {err && (
        <Card>
          <CardBody>
            <p className="text-danger text-[13px]">No se pudo cargar el dashboard: {err}</p>
          </CardBody>
        </Card>
      )}

      {!err && !data && <SkeletonGrid />}

      {data?.role === "PM" && <DashboardPM data={data} />}
      {data?.role === "DEV" && <DashboardDev data={data} />}
      {data?.role === "DIR" && (
        <DashboardDir
          data={data}
          kpis={kpis}
          historico={historico}
          desde={desde}
          hasta={hasta}
          onDesde={setDesde}
          onHasta={setHasta}
          errRango={errRango}
          onReset={() => { setDesde(haceDiasISO(30)); setHasta(hoyISO()); }}
        />
      )}
    </div>
  );
}

function Hero({ fullName, username, roles }: { fullName: string; username: string; roles: string[] }) {
  const rol = roles[0] ?? "";
  const subtitulo: Record<string, string> = {
    PM:  "Tu cartera de proyectos y los hitos del proximo trimestre.",
    DEV: "Tu carga de trabajo y los proyectos en los que estas asignado.",
    DIR: "Indicadores ejecutivos y salud general de la operacion.",
  };
  return (
    <div>
      <h1 className="text-lg font-semibold text-fg">
        Hola, {fullName || username}
      </h1>
      <p className="text-[13px] text-fg-muted mt-0.5">
        {subtitulo[rol] ?? "Bienvenido a Innovatech."}
      </p>
    </div>
  );
}

type Delta = { value: number; suffix?: string };
function KpiCard({
  label, value, delta, sparkline, valueColor,
}: {
  label: string;
  value: string;
  delta?: Delta;
  sparkline?: number[];
  valueColor?: string;
}) {
  return (
    <Card>
      <CardBody className="space-y-3">
        <div className="text-[11px] uppercase tracking-wider font-medium text-fg-muted">{label}</div>
        <div className="flex items-baseline gap-2">
          <div className={`font-mono text-[28px] leading-none ${valueColor ?? "text-fg"}`}>
            {value}
          </div>
        </div>
        {delta && <DeltaPill delta={delta} />}
        {sparkline && <Sparkline points={sparkline} />}
      </CardBody>
    </Card>
  );
}

function DeltaPill({ delta }: { delta: Delta }) {
  const positive = delta.value > 0;
  const zero = delta.value === 0;
  const color = zero ? "text-fg-muted" : positive ? "text-success" : "text-danger";
  const arrow = zero ? "→" : positive ? "↑" : "↓";
  return (
    <div className={`flex items-center gap-1 text-[11px] ${color}`}>
      <span>{arrow}</span>
      <span className="font-mono">{positive ? "+" : ""}{delta.value}{delta.suffix ?? ""}</span>
      <span className="text-fg-subtle">vs mes anterior</span>
    </div>
  );
}

function Sparkline({ points }: { points: number[] }) {
  if (points.length < 2) return null;
  const w = 200;
  const h = 28;
  const min = Math.min(...points);
  const max = Math.max(...points);
  const range = max - min || 1;
  const stepX = w / (points.length - 1);
  const d = points
    .map((p, i) => {
      const x = i * stepX;
      const y = h - ((p - min) / range) * h;
      return `${i === 0 ? "M" : "L"}${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(" ");
  const dArea = `${d} L${w},${h} L0,${h} Z`;
  return (
    <svg viewBox={`0 0 ${w} ${h}`} className="w-full h-7 mt-1" preserveAspectRatio="none">
      <path d={dArea} fill="rgb(var(--accent) / 0.12)" />
      <path d={d} fill="none" stroke="rgb(var(--accent))" strokeWidth="1.5" strokeLinejoin="round" strokeLinecap="round" />
    </svg>
  );
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
      {[0, 1, 2].map((i) => (
        <Card key={i}>
          <CardBody>
            <div className="h-2.5 w-20 bg-surface2 rounded animate-pulse" />
            <div className="h-7 w-24 bg-surface2 rounded mt-3 animate-pulse" />
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
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard label="Proyectos supervisados" value={String(total)} />
        <KpiCard
          label="Tareas en riesgo"
          value={String(enRiesgo)}
          valueColor="text-warning"
        />
        <KpiCard
          label="Cumplimiento"
          value={`${cumplimiento}%`}
          valueColor={cumplimiento >= 70 ? "text-success" : "text-warning"}
        />
      </div>

      <Card>
        <CardHeader title="Proximos hitos" subtitle="Lo que viene en las proximas semanas" />
        <CardBody className="!py-0">
          <ul className="divide-y divide-border">
            {(data.proximosHitos ?? []).map((h, i) => (
              <li key={i} className="py-3 flex items-center justify-between text-[13px]">
                <span className="text-fg">{h}</span>
                <Badge tone="info">proximo</Badge>
              </li>
            ))}
            {(data.proximosHitos ?? []).length === 0 && (
              <li className="py-6 text-[13px] text-fg-muted text-center">Sin hitos cargados.</li>
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
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard label="Tareas asignadas" value={String(asignadas)} />
        <KpiCard label="Pendientes" value={String(pendientes)} valueColor="text-warning" />
        <KpiCard
          label="Progreso"
          value={`${pctCompletadas}%`}
          valueColor={pctCompletadas >= 70 ? "text-success" : "text-fg"}
        />
      </div>

      <Card>
        <CardHeader title="Proyectos en curso" subtitle="Donde estas asignado actualmente" />
        <CardBody>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {(data.proyectosEnCurso ?? []).map((p, i) => (
              <div
                key={i}
                className="border border-border rounded px-3 py-2 hover:bg-surface2 transition-colors"
              >
                <div className="text-[13px] font-medium text-fg truncate">{p}</div>
                <div className="text-[11px] text-fg-muted mt-0.5">en curso</div>
              </div>
            ))}
            {(data.proyectosEnCurso ?? []).length === 0 && (
              <div className="col-span-2 py-6 text-[13px] text-fg-muted text-center">
                No estas asignado a ningun proyecto.
              </div>
            )}
          </div>
        </CardBody>
      </Card>
    </>
  );
}

function DashboardDir({
  data, kpis, historico, desde, hasta, onDesde, onHasta, errRango, onReset,
}: {
  data: Dashboard;
  kpis: Kpis | null;
  historico: Historico | null;
  desde: string;
  hasta: string;
  onDesde: (s: string) => void;
  onHasta: (s: string) => void;
  errRango: string | null;
  onReset: () => void;
}) {
  const utiPct = Math.round((data.porcentajeUtilizacion ?? 0) * 100);

  const sparkSeries = historico?.utilizacion?.length
    ? historico.utilizacion.map((p) => Math.round(p.v * 100))
    : undefined;

  const deltaUtil = historico?.deltas?.porcentajeUtilizacion;
  const deltaProy = historico?.deltas?.proyectosActivos;

  const rolesData = kpis?.recursosPorRol
    ? Object.entries(kpis.recursosPorRol).map(([name, value]) => ({ name, value }))
    : [];
  const totalRecursos = rolesData.reduce((acc, r) => acc + r.value, 0);

  const estadosData = kpis?.proyectosPorEstado
    ? Object.entries(kpis.proyectosPorEstado).map(([name, value]) => ({ name, value }))
    : [];
  const totalProyectos = estadosData.reduce((acc, e) => acc + e.value, 0);

  return (
    <>
      <RangoFechas
        desde={desde}
        hasta={hasta}
        onDesde={onDesde}
        onHasta={onHasta}
        onReset={onReset}
        error={errRango}
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard
          label="Proyectos activos"
          value={String(data.proyectosActivos ?? 0)}
          delta={deltaProy != null ? { value: deltaProy } : undefined}
        />
        <KpiCard
          label="Utilizacion"
          value={`${utiPct}%`}
          valueColor={utiPct >= 80 ? "text-warning" : "text-fg"}
          sparkline={sparkSeries}
          delta={deltaUtil != null ? { value: Math.round(deltaUtil * 100), suffix: "pp" } : undefined}
        />
        <KpiCard
          label="Alertas globales"
          value={String(data.alertasGlobales ?? 0)}
          valueColor={(data.alertasGlobales ?? 0) > 0 ? "text-danger" : "text-fg"}
        />
      </div>

      <EvolucionChart historico={historico} desde={desde} hasta={hasta} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-3">
        <Card className="lg:col-span-2">
          <CardHeader title="Recursos por rol" subtitle="Distribucion de talento activo" />
          <CardBody>
            {rolesData.length === 0 ? (
              <p className="text-[13px] text-fg-muted">Cargando KPIs...</p>
            ) : (
              <div className="flex items-center gap-6">
                <div className="relative w-[200px] h-[200px] shrink-0">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie
                        data={rolesData}
                        dataKey="value"
                        nameKey="name"
                        innerRadius={62}
                        outerRadius={92}
                        paddingAngle={2}
                        stroke="none"
                      >
                        {rolesData.map((r, i) => (
                          <Cell key={i} fill={ROLE_COLORS[r.name] ?? FALLBACK_COLOR} />
                        ))}
                      </Pie>
                      <Tooltip
                        contentStyle={{
                          background: "rgb(var(--surface))",
                          border: "1px solid rgb(var(--border))",
                          borderRadius: 6,
                          fontSize: 12,
                          color: "rgb(var(--fg))",
                        }}
                      />
                    </PieChart>
                  </ResponsiveContainer>
                  <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                    <span className="font-mono text-xl text-fg leading-none">{totalRecursos}</span>
                    <span className="text-[10px] uppercase tracking-wider text-fg-muted mt-1">recursos</span>
                  </div>
                </div>

                <ul className="flex-1 space-y-1.5 min-w-0">
                  {rolesData.map((r) => {
                    const pct = totalRecursos === 0 ? 0 : Math.round((r.value / totalRecursos) * 100);
                    return (
                      <li key={r.name} className="flex items-center gap-2.5 text-[13px]">
                        <span
                          className="w-2 h-2 rounded-full shrink-0"
                          style={{ background: ROLE_COLORS[r.name] ?? FALLBACK_COLOR }}
                        />
                        <span className="text-fg flex-1 truncate">{r.name}</span>
                        <span className="font-mono text-fg-muted text-xs">{r.value}</span>
                        <span className="font-mono text-fg-subtle text-xs w-10 text-right">{pct}%</span>
                      </li>
                    );
                  })}
                </ul>
              </div>
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Capacidad" subtitle="Horas semanales" />
          <CardBody>
            <CapacidadRow kpis={kpis} />
          </CardBody>
        </Card>
      </div>

      <Card>
        <CardHeader title="Proyectos por estado" subtitle="Cartera global agrupada" />
        <CardBody>
          {estadosData.length === 0 ? (
            <p className="text-[13px] text-fg-muted">Cargando KPIs...</p>
          ) : (
            <StackedBar
              segments={estadosData.map((e) => ({
                key: e.name,
                value: e.value,
                color: ESTADO_COLORS[e.name] ?? FALLBACK_COLOR,
              }))}
              total={totalProyectos}
            />
          )}
        </CardBody>
      </Card>
    </>
  );
}

function RangoFechas({
  desde, hasta, onDesde, onHasta, onReset, error,
}: {
  desde: string;
  hasta: string;
  onDesde: (s: string) => void;
  onHasta: (s: string) => void;
  onReset: () => void;
  error: string | null;
}) {
  const dias = diasEntre(desde, hasta);
  const dur = dias >= 0 ? `${dias + 1} dia${dias === 0 ? "" : "s"}` : "—";
  return (
    <div className="bg-surface border border-border rounded-md px-4 py-3 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
      <div className="flex items-center gap-3 flex-wrap">
        <div className="text-[11px] uppercase tracking-wider text-fg-muted">Periodo</div>
        <div className="flex items-center gap-2">
          <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
            <span>Desde</span>
            <input
              type="date"
              value={desde}
              max={hasta || hoyISO()}
              onChange={(e) => onDesde(e.target.value)}
              className="bg-bg border border-border rounded px-2 py-1 text-[12px] text-fg focus:outline-none focus:border-accent/60"
            />
          </label>
          <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
            <span>Hasta</span>
            <input
              type="date"
              value={hasta}
              min={desde}
              max={hoyISO()}
              onChange={(e) => onHasta(e.target.value)}
              className="bg-bg border border-border rounded px-2 py-1 text-[12px] text-fg focus:outline-none focus:border-accent/60"
            />
          </label>
        </div>
        <span className="font-mono text-[11px] text-fg-subtle">{dur}</span>
      </div>
      <div className="flex items-center gap-3">
        {error && <span className="text-[12px] text-danger">{error}</span>}
        <button
          type="button"
          onClick={onReset}
          className="text-[12px] text-fg-muted hover:text-fg hover:bg-surface2 px-2.5 py-1 rounded transition-colors"
        >
          Restablecer
        </button>
      </div>
    </div>
  );
}

function EvolucionChart({
  historico, desde, hasta,
}: {
  historico: Historico | null;
  desde: string;
  hasta: string;
}) {
  const dataChart = useMemo(() => {
    if (!historico?.utilizacion?.length) return [];
    return historico.utilizacion.map((p, i) => {
      const proyectos = historico.proyectosActivos?.[i]?.v ?? 0;
      const t = new Date(p.t);
      return {
        label: `${String(t.getDate()).padStart(2, "0")}/${String(t.getMonth() + 1).padStart(2, "0")}`,
        utilizacion: Math.round(p.v * 100),
        proyectos,
      };
    });
  }, [historico]);

  return (
    <Card>
      <CardHeader
        title="Evolucion del periodo"
        subtitle={`Utilizacion y proyectos activos · ${desde} → ${hasta}`}
      />
      <CardBody>
        {dataChart.length === 0 ? (
          <p className="text-[13px] text-fg-muted">
            Sin snapshots en este rango. {historico?.status === "datos no disponibles" && "(servicio caido)"}
          </p>
        ) : (
          <div className="w-full h-[260px]">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={dataChart} margin={{ top: 8, right: 12, bottom: 0, left: -8 }}>
                <CartesianGrid stroke="rgb(var(--border))" strokeDasharray="3 3" vertical={false} />
                <XAxis
                  dataKey="label"
                  tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                  axisLine={{ stroke: "rgb(var(--border))" }}
                  tickLine={false}
                  minTickGap={20}
                />
                <YAxis
                  yAxisId="left"
                  tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                  axisLine={{ stroke: "rgb(var(--border))" }}
                  tickLine={false}
                  width={36}
                  domain={[0, 100]}
                  tickFormatter={(v) => `${v}%`}
                />
                <YAxis
                  yAxisId="right"
                  orientation="right"
                  tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                  axisLine={{ stroke: "rgb(var(--border))" }}
                  tickLine={false}
                  width={28}
                  allowDecimals={false}
                />
                <Tooltip
                  contentStyle={{
                    background: "rgb(var(--surface))",
                    border: "1px solid rgb(var(--border))",
                    borderRadius: 6,
                    fontSize: 12,
                    color: "rgb(var(--fg))",
                  }}
                  labelStyle={{ color: "rgb(var(--fg-muted))" }}
                  formatter={(value: number, name: string) => {
                    if (name === "utilizacion") return [`${value}%`, "Utilizacion"];
                    return [value, "Proyectos activos"];
                  }}
                />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="utilizacion"
                  stroke="rgb(var(--accent))"
                  strokeWidth={2}
                  dot={false}
                  activeDot={{ r: 3 }}
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="proyectos"
                  stroke="rgb(var(--info))"
                  strokeWidth={2}
                  dot={false}
                  activeDot={{ r: 3 }}
                />
              </LineChart>
            </ResponsiveContainer>
            <div className="flex items-center gap-4 mt-2 text-[11px] text-fg-muted">
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 rounded" style={{ background: "rgb(var(--accent))" }} />
                Utilizacion (%)
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 rounded" style={{ background: "rgb(var(--info))" }} />
                Proyectos activos
              </span>
            </div>
          </div>
        )}
      </CardBody>
    </Card>
  );
}

function CapacidadRow({ kpis }: { kpis: Kpis | null }) {
  const items = [
    {
      label: "Total",
      value: kpis?.capacidadSemanalTotalHoras ?? "—",
      suffix: "h",
    },
    {
      label: "Promedio",
      value: kpis?.promedioHorasPorRecurso != null ? kpis.promedioHorasPorRecurso.toFixed(1) : "—",
      suffix: "h/recurso",
    },
    {
      label: "Recursos activos",
      value: kpis?.totalRecursosActivos ?? "—",
      suffix: "",
    },
  ];
  return (
    <div className="grid grid-cols-3 divide-x divide-border">
      {items.map((m, i) => (
        <div key={m.label} className={i === 0 ? "pr-4" : "px-4"}>
          <div className="text-[11px] uppercase tracking-wider text-fg-muted">{m.label}</div>
          <div className="mt-1.5 flex items-baseline gap-1.5">
            <span className="font-mono text-xl text-fg leading-none">{m.value}</span>
            {m.suffix && <span className="text-[11px] text-fg-muted">{m.suffix}</span>}
          </div>
        </div>
      ))}
    </div>
  );
}

function StackedBar({
  segments, total,
}: {
  segments: { key: string; value: number; color: string }[];
  total: number;
}) {
  if (total === 0) return <p className="text-[13px] text-fg-muted">Sin datos.</p>;
  return (
    <div className="space-y-3">
      <div className="flex h-2 rounded overflow-hidden bg-surface2">
        {segments.map((s) => {
          const pct = (s.value / total) * 100;
          return (
            <div
              key={s.key}
              style={{ width: `${pct}%`, background: s.color }}
              title={`${s.key}: ${s.value} (${pct.toFixed(1)}%)`}
            />
          );
        })}
      </div>
      <ul className="grid grid-cols-2 md:grid-cols-4 gap-x-4 gap-y-2">
        {segments.map((s) => {
          const pct = (s.value / total) * 100;
          return (
            <li key={s.key} className="flex items-center gap-2 text-[12px]">
              <span className="w-2 h-2 rounded-full shrink-0" style={{ background: s.color }} />
              <span className="text-fg-muted flex-1 truncate">{s.key.replace(/_/g, " ").toLowerCase()}</span>
              <span className="font-mono text-fg-subtle">{pct.toFixed(0)}%</span>
            </li>
          );
        })}
      </ul>
    </div>
  );
}
