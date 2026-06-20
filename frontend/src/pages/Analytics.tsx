import { useEffect, useMemo, useState } from "react";
import {
  CartesianGrid, Cell, Line, LineChart, Pie, PieChart, ResponsiveContainer,
  Tooltip, XAxis, YAxis,
} from "recharts";
import { Card, CardBody, CardHeader } from "../components/Card";
import { Badge } from "../components/Badge";
import { api } from "../api/client";

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
  totalTasks: number;
  delayedTasks: number;
  tasksByStatus: Record<string, number>;
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
  DEV: "rgb(var(--role-dev))",
  QA: "rgb(var(--role-qa))",
  DEVOPS: "rgb(var(--role-devops))",
  DESIGNER: "rgb(var(--role-designer))",
  PM: "rgb(var(--role-pm))",
};
const FALLBACK_COLOR = "rgb(var(--fg-muted))";

const STATUS_COLORS: Record<string, string> = {
  PLANNING: "rgb(var(--info))",
  IN_PROGRESS: "rgb(var(--success))",
  COMPLETED: "rgb(var(--fg-muted))",
  CANCELLED: "rgb(var(--danger))",
};

const TASK_COLORS: Record<string, string> = {
  TODO: "rgb(var(--info))",
  IN_PROGRESS: "rgb(var(--success))",
  DONE: "rgb(var(--fg-muted))",
  BLOCKED: "rgb(var(--danger))",
};

const MAX_RANGE_DAYS = 30;

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
function daysBetween(from: string, to: string) {
  const a = new Date(from + "T00:00:00");
  const b = new Date(to + "T00:00:00");
  return Math.round((b.getTime() - a.getTime()) / 86400000);
}

export function Analytics() {
  const [kpis, setKpis] = useState<Kpis | null>(null);
  const [history, setHistory] = useState<History | null>(null);
  const [from, setFrom] = useState(daysAgoISO(30));
  const [to, setTo] = useState(todayISO());
  const [errRange, setErrRange] = useState<string | null>(null);

  useEffect(() => {
    api.get<Kpis>("/kpis").then((r) => setKpis(r.data)).catch(() => setKpis(null));
  }, []);

  useEffect(() => {
    const days = daysBetween(from, to);
    if (days < 0) { setErrRange("The 'to' date cannot be before 'from'."); return; }
    if (days > MAX_RANGE_DAYS) { setErrRange(`The max range is ${MAX_RANGE_DAYS} days.`); return; }
    setErrRange(null);
    api.get<History>(`/kpis/history?from=${from}&to=${to}`)
      .then((r) => setHistory(r.data))
      .catch(() => setHistory(null));
  }, [from, to]);

  const utiPct = Math.round((kpis?.utilizationPercentage ?? 0) * 100);
  const down = kpis?.status === "datos no disponibles";

  const rolesData = kpis?.resourcesByRole
    ? Object.entries(kpis.resourcesByRole).map(([name, value]) => ({ name, value }))
    : [];
  const totalResources = rolesData.reduce((a, r) => a + r.value, 0);

  const statusData = kpis?.projectsByStatus
    ? Object.entries(kpis.projectsByStatus).map(([name, value]) => ({ name, value }))
    : [];
  const totalProjects = statusData.reduce((a, e) => a + e.value, 0);

  const tasksData = kpis?.tasksByStatus
    ? Object.entries(kpis.tasksByStatus).map(([name, value]) => ({ name, value }))
    : [];

  return (
    <div className="space-y-5 max-w-[1200px]">
      <div>
        <h1 className="text-lg font-semibold text-fg">Analytics</h1>
        <p className="text-[13px] text-fg-muted mt-0.5">
          Operational indicators: utilization, projects and task load.
        </p>
      </div>

      {down && (
        <Card><CardBody><p className="text-[13px] text-warning">Analytics service is down — showing zeros.</p></CardBody></Card>
      )}

      <DateRange from={from} to={to} onFrom={setFrom} onTo={setTo} error={errRange}
                 onReset={() => { setFrom(daysAgoISO(30)); setTo(todayISO()); }} />

      <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
        <KpiCard label="Active projects" value={String(kpis?.activeProjects ?? 0)} />
        <KpiCard label="Utilization" value={`${utiPct}%`} valueColor={utiPct >= 80 ? "text-warning" : "text-fg"} />
        <KpiCard label="Active resources" value={String(kpis?.totalActiveResources ?? 0)} />
        <KpiCard label="Total tasks" value={String(kpis?.totalTasks ?? 0)} />
      </div>

      <EvolutionChart history={history} from={from} to={to} />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-3">
        <Card>
          <CardHeader title="Tasks" subtitle="Workload by status" right={
            (kpis?.delayedTasks ?? 0) > 0 ? <Badge tone="danger">{kpis?.delayedTasks} overdue</Badge> : <Badge tone="success">on track</Badge>
          } />
          <CardBody>
            {tasksData.length === 0 ? (
              <p className="text-[13px] text-fg-muted">No tasks.</p>
            ) : (
              <StackedBar
                segments={tasksData.map((e) => ({ key: e.name, value: e.value, color: TASK_COLORS[e.name] ?? FALLBACK_COLOR }))}
                total={tasksData.reduce((a, e) => a + e.value, 0)}
              />
            )}
          </CardBody>
        </Card>

        <Card>
          <CardHeader title="Capacity" subtitle="Weekly hours" />
          <CardBody>
            <div className="grid grid-cols-3 divide-x divide-border">
              {[
                { label: "Total", value: kpis?.totalWeeklyCapacityHours ?? "—", suffix: "h" },
                { label: "Average", value: kpis?.avgHoursPerResource != null ? kpis.avgHoursPerResource.toFixed(1) : "—", suffix: "h/res" },
                { label: "Delayed projects", value: kpis?.delayedProjects ?? "—", suffix: "" },
              ].map((m, i) => (
                <div key={m.label} className={i === 0 ? "pr-4" : "px-4"}>
                  <div className="text-[11px] uppercase tracking-wider text-fg-muted">{m.label}</div>
                  <div className="mt-1.5 flex items-baseline gap-1.5">
                    <span className="font-mono text-xl text-fg leading-none">{m.value}</span>
                    {m.suffix && <span className="text-[11px] text-fg-muted">{m.suffix}</span>}
                  </div>
                </div>
              ))}
            </div>
          </CardBody>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-3">
        <Card className="lg:col-span-2">
          <CardHeader title="Resources by role" subtitle="Active talent distribution" />
          <CardBody>
            {rolesData.length === 0 ? (
              <p className="text-[13px] text-fg-muted">No data.</p>
            ) : (
              <div className="flex items-center gap-6">
                <div className="relative w-[200px] h-[200px] shrink-0">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={rolesData} dataKey="value" nameKey="name" innerRadius={62} outerRadius={92} paddingAngle={2} stroke="none">
                        {rolesData.map((r, i) => <Cell key={i} fill={ROLE_COLORS[r.name] ?? FALLBACK_COLOR} />)}
                      </Pie>
                      <Tooltip contentStyle={{ background: "rgb(var(--surface))", border: "1px solid rgb(var(--border))", borderRadius: 6, fontSize: 12, color: "rgb(var(--fg))" }} />
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
                        <span className="w-2 h-2 rounded-full shrink-0" style={{ background: ROLE_COLORS[r.name] ?? FALLBACK_COLOR }} />
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
          <CardHeader title="Projects by status" subtitle="Portfolio grouped" />
          <CardBody>
            {statusData.length === 0 ? (
              <p className="text-[13px] text-fg-muted">No data.</p>
            ) : (
              <StackedBar
                segments={statusData.map((e) => ({ key: e.name, value: e.value, color: STATUS_COLORS[e.name] ?? FALLBACK_COLOR }))}
                total={totalProjects}
              />
            )}
          </CardBody>
        </Card>
      </div>
    </div>
  );
}

function KpiCard({ label, value, valueColor }: { label: string; value: string; valueColor?: string }) {
  return (
    <Card>
      <CardBody className="space-y-3">
        <div className="text-[11px] uppercase tracking-wider font-medium text-fg-muted">{label}</div>
        <div className={`font-mono text-[28px] leading-none ${valueColor ?? "text-fg"}`}>{value}</div>
      </CardBody>
    </Card>
  );
}

function DateRange({
  from, to, onFrom, onTo, onReset, error,
}: {
  from: string; to: string; onFrom: (s: string) => void; onTo: (s: string) => void; onReset: () => void; error: string | null;
}) {
  const days = daysBetween(from, to);
  const dur = days >= 0 ? `${days + 1} day${days === 0 ? "" : "s"}` : "—";
  return (
    <div className="bg-surface border border-border rounded-md px-4 py-3 flex flex-col md:flex-row md:items-center md:justify-between gap-3">
      <div className="flex items-center gap-3 flex-wrap">
        <div className="text-[11px] uppercase tracking-wider text-fg-muted">Period</div>
        <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
          <span>From</span>
          <input type="date" value={from} max={to || todayISO()} onChange={(e) => onFrom(e.target.value)}
                 className="bg-bg border border-border rounded px-2 py-1 text-[12px] text-fg focus:outline-none focus:border-accent/60" />
        </label>
        <label className="flex items-center gap-1.5 text-[12px] text-fg-muted">
          <span>To</span>
          <input type="date" value={to} min={from} max={todayISO()} onChange={(e) => onTo(e.target.value)}
                 className="bg-bg border border-border rounded px-2 py-1 text-[12px] text-fg focus:outline-none focus:border-accent/60" />
        </label>
        <span className="font-mono text-[11px] text-fg-subtle">{dur}</span>
      </div>
      <div className="flex items-center gap-3">
        {error && <span className="text-[12px] text-danger">{error}</span>}
        <button type="button" onClick={onReset}
                className="text-[12px] text-fg-muted hover:text-fg hover:bg-surface2 px-2.5 py-1 rounded transition-colors">
          Reset
        </button>
      </div>
    </div>
  );
}

function EvolutionChart({ history, from, to }: { history: History | null; from: string; to: string }) {
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
      <CardHeader title="Period evolution" subtitle={`Utilization and active projects · ${from} → ${to}`} />
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
                <XAxis dataKey="label" tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                       axisLine={{ stroke: "rgb(var(--border))" }} tickLine={false} minTickGap={20} />
                <YAxis yAxisId="left" tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                       axisLine={{ stroke: "rgb(var(--border))" }} tickLine={false} width={36} domain={[0, 100]} tickFormatter={(v) => `${v}%`} />
                <YAxis yAxisId="right" orientation="right" tick={{ fill: "rgb(var(--fg-muted))", fontSize: 11 }}
                       axisLine={{ stroke: "rgb(var(--border))" }} tickLine={false} width={28} allowDecimals={false} />
                <Tooltip contentStyle={{ background: "rgb(var(--surface))", border: "1px solid rgb(var(--border))", borderRadius: 6, fontSize: 12, color: "rgb(var(--fg))" }}
                         labelStyle={{ color: "rgb(var(--fg-muted))" }}
                         formatter={(value: number, name: string) => name === "utilization" ? [`${value}%`, "Utilization"] : [value, "Active projects"]} />
                <Line yAxisId="left" type="monotone" dataKey="utilization" stroke="rgb(var(--accent))" strokeWidth={2} dot={false} activeDot={{ r: 3 }} />
                <Line yAxisId="right" type="monotone" dataKey="projects" stroke="rgb(var(--info))" strokeWidth={2} dot={false} activeDot={{ r: 3 }} />
              </LineChart>
            </ResponsiveContainer>
            <div className="flex items-center gap-4 mt-2 text-[11px] text-fg-muted">
              <span className="flex items-center gap-1.5"><span className="w-2.5 h-0.5 rounded" style={{ background: "rgb(var(--accent))" }} /> Utilization (%)</span>
              <span className="flex items-center gap-1.5"><span className="w-2.5 h-0.5 rounded" style={{ background: "rgb(var(--info))" }} /> Active projects</span>
            </div>
          </div>
        )}
      </CardBody>
    </Card>
  );
}

function StackedBar({ segments, total }: { segments: { key: string; value: number; color: string }[]; total: number }) {
  if (total === 0) return <p className="text-[13px] text-fg-muted">No data.</p>;
  return (
    <div className="space-y-3">
      <div className="flex h-2 rounded overflow-hidden bg-surface2">
        {segments.map((s) => {
          const pct = (s.value / total) * 100;
          return <div key={s.key} style={{ width: `${pct}%`, background: s.color }} title={`${s.key}: ${s.value} (${pct.toFixed(1)}%)`} />;
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
