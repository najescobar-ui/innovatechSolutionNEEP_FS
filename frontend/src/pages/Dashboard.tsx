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
  supervisedProjects?: number;
  tasksAtRisk?: number;
  upcomingMilestones?: string[];
  assignedTasks?: number;
  pendingTasks?: number;
  ongoingProjects?: string[];
  activeProjects?: number;
  utilizationPercentage?: number;
  globalAlerts?: number;
};

type Kpis = {
  status: string;
  activeProjects: number;
  delayedProjects: number;
  totalActiveResources: number;
  totalWeeklyCapacityHours: number;
  avgHoursPerResource: number;
  utilizationPercentage?: number;
  resourcesByRole: Record<string, number>;
  projectsByStatus: Record<string, number>;
};

type History = {
  status: string;
  utilization: { t: string; v: number }[];
  activeProjects: { t: string; v: number }[];
  deltas: {
    utilizationPercentage: number | null;
    activeProjects: number | null;
    activeResources: number | null;
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

const STATUS_COLORS: Record<string, string> = {
  PLANNING:    "rgb(var(--info))",
  IN_PROGRESS: "rgb(var(--success))",
  COMPLETED:   "rgb(var(--fg-muted))",
  CANCELLED:   "rgb(var(--danger))",
  PAUSED:      "rgb(var(--warning))",
};

const MAX_RANGE_DAYS = 30;

/** Helpers de fecha: trabajamos con YYYY-MM-DD locales para los inputs nativos. */
function isoDate(d: Date) {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}
function todayISO() { return isoDate(new Date()); }
function daysAgoISO(n: number) {
  const d = new Date();
  d.setDate(d.getDate() - n);
  return isoDate(d);
}
function daysBetween(from: string, to: string): number {
  const a = new Date(from + "T00:00:00");
  const b = new Date(to + "T00:00:00");
  return Math.round((b.getTime() - a.getTime()) / 86400000);
}

export function Dashboard() {
  const { fullName, username, roles } = useAuth();
  const [data, setData] = useState<Dashboard | null>(null);
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [history, setHistory] = useState<History | null>(null);
  const [err, setErr] = useState<string | null>(null);

  /* Rango de fechas para el grafico de evolucion (default: ultimos 30 dias). */
  const [from, setFrom] = useState<string>(daysAgoISO(30));
  const [to, setTo] = useState<string>(todayISO());
  const [errRange, setErrRange] = useState<string | null>(null);

  useEffect(() => {
    api.get<Dashboard>("/dashboard")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "unknown error"));

    if (roles.includes("DIR")) {
      api.get<Kpis>("/kpis").then((r) => setKpis(r.data)).catch(() => {});
    }
  }, [roles.join(",")]);

  /* Refetch historico cuando cambia el rango (con validacion). */
  useEffect(() => {
    if (!roles.includes("DIR")) return;
    const days = daysBetween(from, to);
    if (days < 0) { setErrRange("The 'to' date cannot be before 'from'."); return; }
    if (days > MAX_RANGE_DAYS) { setErrRange(`The max range is ${MAX_RANGE_DAYS} days.`); return; }
    setErrRange(null);
    api.get<History>(`/kpis/history?from=${from}&to=${to}`)
      .then((r) => setHistory(r.data))
      .catch(() => setHistory(null));
  }, [roles.join(","), from, to]);

  return (
    <div className="space-y-5 max-w-[1200px]">
      <Hero fullName={fullName} username={username} roles={roles} />

      {err && (
        <Card>
          <CardBody>
            <p className="text-danger text-[13px]">Could not load the dashboard: {err}</p>
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
          history={history}
          from={from}
          to={to}
          onFrom={setFrom}
          onTo={setTo}
          errRange={errRange}
          onReset={() => { setFrom(daysAgoISO(30)); setTo(todayISO()); }}
        />
      )}
    </div>
  );
}

function Hero({ fullName, username, roles }: { fullName: string; username: string; roles: string[] }) {
  const role = roles[0] ?? "";
  const subtitle: Record<string, string> = {
    PM:  "Your project portfolio and next quarter's milestones.",
    DEV: "Your workload and the projects you are assigned to.",
    DIR: "Executive indicators and overall health of the operation.",
  };
  return (
    <div>
      <h1 className="text-lg font-semibold text-fg">
        Hi, {fullName || username}
      </h1>
      <p className="text-[13px] text-fg-muted mt-0.5">
        {subtitle[role] ?? "Welcome to Innovatech."}
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
      <span className="text-fg-subtle">vs last month</span>
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
  const total = data.supervisedProjects ?? 0;
  const atRisk = data.tasksAtRisk ?? 0;
  const pctRisk = total === 0 ? 0 : Math.round((atRisk / total) * 100);
  const onTrack = Math.max(0, 100 - pctRisk);
  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard label="Supervised projects" value={String(total)} />
        <KpiCard
          label="Tasks at risk"
          value={String(atRisk)}
          valueColor="text-warning"
        />
        <KpiCard
          label="On track"
          value={`${onTrack}%`}
          valueColor={onTrack >= 70 ? "text-success" : "text-warning"}
        />
      </div>

      <Card>
        <CardHeader title="Upcoming milestones" subtitle="What's coming in the next weeks" />
        <CardBody className="!py-0">
          <ul className="divide-y divide-border">
            {(data.upcomingMilestones ?? []).map((h, i) => (
              <li key={i} className="py-3 flex items-center justify-between text-[13px]">
                <span className="text-fg">{h}</span>
                <Badge tone="info">upcoming</Badge>
              </li>
            ))}
            {(data.upcomingMilestones ?? []).length === 0 && (
              <li className="py-6 text-[13px] text-fg-muted text-center">No milestones loaded.</li>
            )}
          </ul>
        </CardBody>
      </Card>
    </>
  );
}

function DashboardDev({ data }: { data: Dashboard }) {
  const assigned = data.assignedTasks ?? 0;
  const pending = data.pendingTasks ?? 0;
  const completed = Math.max(0, assigned - pending);
  const pctCompleted = assigned === 0 ? 0 : Math.round((completed / assigned) * 100);
  return (
    <>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard label="Assigned tasks" value={String(assigned)} />
        <KpiCard label="Pending" value={String(pending)} valueColor="text-warning" />
        <KpiCard
          label="Progress"
          value={`${pctCompleted}%`}
          valueColor={pctCompleted >= 70 ? "text-success" : "text-fg"}
        />
      </div>

      <Card>
        <CardHeader title="Ongoing projects" subtitle="Where you are currently assigned" />
        <CardBody>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {(data.ongoingProjects ?? []).map((p, i) => (
              <div
                key={i}
                className="border border-border rounded px-3 py-2 hover:bg-surface2 transition-colors"
              >
                <div className="text-[13px] font-medium text-fg truncate">{p}</div>
                <div className="text-[11px] text-fg-muted mt-0.5">in progress</div>
              </div>
            ))}
            {(data.ongoingProjects ?? []).length === 0 && (
              <div className="col-span-2 py-6 text-[13px] text-fg-muted text-center">
                You are not assigned to any project.
              </div>
            )}
          </div>
        </CardBody>
      </Card>
    </>
  );
}

function DashboardDir({
  data, kpis, history, from, to, onFrom, onTo, errRange, onReset,
}: {
  data: Dashboard;
  kpis: Kpis | null;
  history: History | null;
  from: string;
  to: string;
  onFrom: (s: string) => void;
  onTo: (s: string) => void;
  errRange: string | null;
  onReset: () => void;
}) {
  const utiPct = Math.round((data.utilizationPercentage ?? 0) * 100);

  const sparkSeries = history?.utilization?.length
    ? history.utilization.map((p) => Math.round(p.v * 100))
    : undefined;

  const deltaUtil = history?.deltas?.utilizationPercentage;
  const deltaProj = history?.deltas?.activeProjects;

  const rolesData = kpis?.resourcesByRole
    ? Object.entries(kpis.resourcesByRole).map(([name, value]) => ({ name, value }))
    : [];
  const totalResources = rolesData.reduce((acc, r) => acc + r.value, 0);

  const statusData = kpis?.projectsByStatus
    ? Object.entries(kpis.projectsByStatus).map(([name, value]) => ({ name, value }))
    : [];
  const totalProjects = statusData.reduce((acc, e) => acc + e.value, 0);

  return (
    <>
      <DateRange
        from={from}
        to={to}
        onFrom={onFrom}
        onTo={onTo}
        onReset={onReset}
        error={errRange}
      />

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        <KpiCard
          label="Active projects"
          value={String(data.activeProjects ?? 0)}
          delta={deltaProj != null ? { value: deltaProj } : undefined}
        />
        <KpiCard
          label="Utilization"
          value={`${utiPct}%`}
          valueColor={utiPct >= 80 ? "text-warning" : "text-fg"}
          sparkline={sparkSeries}
          delta={deltaUtil != null ? { value: Math.round(deltaUtil * 100), suffix: "pp" } : undefined}
        />
        <KpiCard
          label="Global alerts"
          value={String(data.globalAlerts ?? 0)}
          valueColor={(data.globalAlerts ?? 0) > 0 ? "text-danger" : "text-fg"}
        />
      </div>

      <EvolutionChart history={history} from={from} to={to} />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-3">
        <Card className="lg:col-span-2">
          <CardHeader title="Resources by role" subtitle="Active talent distribution" />
          <CardBody>
            {rolesData.length === 0 ? (
              <p className="text-[13px] text-fg-muted">Loading KPIs...</p>
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
                    <span className="font-mono text-xl text-fg leading-none">{totalResources}</span>
                    <span className="text-[10px] uppercase tracking-wider text-fg-muted mt-1">resources</span>
                  </div>
                </div>

                <ul className="flex-1 space-y-1.5 min-w-0">
                  {rolesData.map((r) => {
                    const pct = totalResources === 0 ? 0 : Math.round((r.value / totalResources) * 100);
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
          <CardHeader title="Capacity" subtitle="Weekly hours" />
          <CardBody>
            <CapacityRow kpis={kpis} />
          </CardBody>
        </Card>
      </div>

      <Card>
        <CardHeader title="Projects by status" subtitle="Global portfolio grouped" />
        <CardBody>
          {statusData.length === 0 ? (
            <p className="text-[13px] text-fg-muted">Loading KPIs...</p>
          ) : (
            <StackedBar
              segments={statusData.map((e) => ({
                key: e.name,
                value: e.value,
                color: STATUS_COLORS[e.name] ?? FALLBACK_COLOR,
              }))}
              total={totalProjects}
            />
          )}
        </CardBody>
      </Card>
    </>
  );
}

function DateRange({
  from, to, onFrom, onTo, onReset, error,
}: {
  from: string;
  to: string;
  onFrom: (s: string) => void;
  onTo: (s: string) => void;
  onReset: () => void;
  error: string | null;
}) {
  const days = daysBetween(from, to);
  const dur = days >= 0 ? `${days + 1} day${days === 0 ? "" : "s"}` : "—";
  return (
    <div className="bg-surface border border-border rounded-md px-4 py-3 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
      <div className="flex items-center gap-3 flex-wrap">
        <div className="text-[11px] uppercase tracking-wider text-fg-muted">Period</div>
        <div className="flex items-center gap-2">
          <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
            <span>From</span>
            <input
              type="date"
              value={from}
              max={to || todayISO()}
              onChange={(e) => onFrom(e.target.value)}
              className="bg-bg border border-border rounded px-2 py-1 text-[12px] text-fg focus:outline-none focus:border-accent/60"
            />
          </label>
          <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
            <span>To</span>
            <input
              type="date"
              value={to}
              min={from}
              max={todayISO()}
              onChange={(e) => onTo(e.target.value)}
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
          Reset
        </button>
      </div>
    </div>
  );
}

function EvolutionChart({
  history, from, to,
}: {
  history: History | null;
  from: string;
  to: string;
}) {
  const dataChart = useMemo(() => {
    if (!history?.utilization?.length) return [];
    return history.utilization.map((p, i) => {
      const projects = history.activeProjects?.[i]?.v ?? 0;
      const t = new Date(p.t);
      return {
        label: `${String(t.getDate()).padStart(2, "0")}/${String(t.getMonth() + 1).padStart(2, "0")}`,
        utilization: Math.round(p.v * 100),
        projects,
      };
    });
  }, [history]);

  return (
    <Card>
      <CardHeader
        title="Period evolution"
        subtitle={`Utilization and active projects · ${from} → ${to}`}
      />
      <CardBody>
        {dataChart.length === 0 ? (
          <p className="text-[13px] text-fg-muted">
            No snapshots in this range. {history?.status === "datos no disponibles" && "(service down)"}
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
                    if (name === "utilization") return [`${value}%`, "Utilization"];
                    return [value, "Active projects"];
                  }}
                />
                <Line
                  yAxisId="left"
                  type="monotone"
                  dataKey="utilization"
                  stroke="rgb(var(--accent))"
                  strokeWidth={2}
                  dot={false}
                  activeDot={{ r: 3 }}
                />
                <Line
                  yAxisId="right"
                  type="monotone"
                  dataKey="projects"
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
                Utilization (%)
              </span>
              <span className="flex items-center gap-1.5">
                <span className="w-2.5 h-0.5 rounded" style={{ background: "rgb(var(--info))" }} />
                Active projects
              </span>
            </div>
          </div>
        )}
      </CardBody>
    </Card>
  );
}

function CapacityRow({ kpis }: { kpis: Kpis | null }) {
  const items = [
    {
      label: "Total",
      value: kpis?.totalWeeklyCapacityHours ?? "—",
      suffix: "h",
    },
    {
      label: "Average",
      value: kpis?.avgHoursPerResource != null ? kpis.avgHoursPerResource.toFixed(1) : "—",
      suffix: "h/resource",
    },
    {
      label: "Active resources",
      value: kpis?.totalActiveResources ?? "—",
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
  if (total === 0) return <p className="text-[13px] text-fg-muted">No data.</p>;
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
