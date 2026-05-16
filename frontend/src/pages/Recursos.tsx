import { useEffect, useMemo, useState } from "react";
import { Copy, MoreHorizontal, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { Badge } from "../components/Badge";
import { Button } from "../components/Button";
import { Checkbox } from "../components/Checkbox";
import { Dropdown, DropdownItem } from "../components/Dropdown";
import { ConfirmDialog, Field, Modal, SelectInput, TextArea, TextInput } from "../components/Modal";
import { api } from "../api/client";
import { useAuth } from "../auth/useAuth";

type Recurso = {
  id: number;
  nombre: string;
  email: string;
  rol: string;
  horasSemanales: number;
  competencias: string | null;
  activo: boolean;
};

type Resp = { status: string; items: Recurso[] };

const ROLES = ["TODOS", "DEV", "QA", "DEVOPS", "DESIGNER", "PM"] as const;
const ROLES_CREAR = ["DEV", "QA", "DEVOPS", "DESIGNER", "PM"];

const ROLE_VAR: Record<string, string> = {
  DEV:      "var(--role-dev)",
  QA:       "var(--role-qa)",
  DEVOPS:   "var(--role-devops)",
  DESIGNER: "var(--role-designer)",
  PM:       "var(--role-pm)",
};

function RolBadge({ rol }: { rol: string }) {
  const v = ROLE_VAR[rol];
  if (!v) return <Badge tone="neutral">{rol}</Badge>;
  return (
    <span
      className="inline-flex items-center rounded px-1.5 py-0.5 text-[11px] font-medium"
      style={{ background: `rgb(${v} / 0.12)`, color: `rgb(${v})` }}
    >
      {rol}
    </span>
  );
}

function iniciales(nombre: string) {
  return nombre
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

export function Recursos() {
  const { roles } = useAuth();
  const esDirector = roles.includes("DIR");

  const [data, setData] = useState<Resp | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [rol, setRol] = useState<string>("TODOS");
  const [soloActivos, setSoloActivos] = useState(false);
  const [modalAbierto, setModalAbierto] = useState(false);

  const [aEditar, setAEditar] = useState<Recurso | null>(null);

  const [seleccionados, setSeleccionados] = useState<Set<number>>(new Set());
  const [confirmBulk, setConfirmBulk] = useState(false);
  const [errBulk, setErrBulk] = useState<string | null>(null);
  const [borrandoBulk, setBorrandoBulk] = useState(false);

  const cargar = () => {
    api.get<Resp>("/recursos")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error"));
  };

  useEffect(cargar, []);

  function toggleUno(id: number, checked: boolean) {
    setSeleccionados((prev) => {
      const next = new Set(prev);
      if (checked) next.add(id);
      else next.delete(id);
      return next;
    });
  }

  async function confirmarBorrarBulk() {
    if (seleccionados.size === 0) return;
    setBorrandoBulk(true);
    setErrBulk(null);
    const ids = Array.from(seleccionados);
    const results = await Promise.allSettled(
      ids.map((id) => api.delete(`/recursos/${id}`)),
    );
    const fallos = results.filter((r) => r.status === "rejected").length;
    setBorrandoBulk(false);
    if (fallos > 0) {
      setErrBulk(`${fallos} de ${ids.length} no se pudieron borrar`);
      cargar();
      return;
    }
    setConfirmBulk(false);
    setSeleccionados(new Set());
    cargar();
  }

  const filtrados = useMemo(() => {
    if (!data) return [];
    const txt = q.trim().toLowerCase();
    return data.items.filter((r) => {
      const matchTxt = !txt
        || r.nombre.toLowerCase().includes(txt)
        || r.email.toLowerCase().includes(txt)
        || (r.competencias ?? "").toLowerCase().includes(txt);
      const matchRol = rol === "TODOS" || r.rol === rol;
      const matchActivo = !soloActivos || r.activo;
      return matchTxt && matchRol && matchActivo;
    });
  }, [data, q, rol, soloActivos]);

  const todosVisiblesSeleccionados = filtrados.length > 0 && filtrados.every((r) => seleccionados.has(r.id));
  const algunoVisibleSeleccionado = filtrados.some((r) => seleccionados.has(r.id));

  function toggleTodos(checked: boolean) {
    setSeleccionados((prev) => {
      const next = new Set(prev);
      if (checked) filtrados.forEach((r) => next.add(r.id));
      else filtrados.forEach((r) => next.delete(r.id));
      return next;
    });
  }

  return (
    <div className="space-y-4 max-w-[1200px]">
      <header className="flex items-end justify-between gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-wider text-fg-muted">Equipo</div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="font-mono text-lg text-fg">{data ? data.items.length : "—"}</span>
            <span className="text-[13px] text-fg-muted">
              recursos
              {data && filtrados.length !== data.items.length && ` · ${filtrados.length} visibles`}
            </span>
            {data?.status === "datos no disponibles" && (
              <Badge tone="warning">fuente caida</Badge>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          {esDirector && seleccionados.size > 0 && (
            <button
              type="button"
              onClick={() => { setErrBulk(null); setConfirmBulk(true); }}
              style={{ background: "rgb(var(--danger))", color: "#000" }}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-[13px] font-medium rounded hover:opacity-90 transition-opacity"
            >
              <Trash2 size={14} />
              Borrar ({seleccionados.size})
            </button>
          )}
          <Button variant="primary" size="md" onClick={() => setModalAbierto(true)}>
            <Plus size={14} />
            <span>Nuevo recurso</span>
          </Button>
        </div>
      </header>

      <div className="bg-surface border border-border rounded-md overflow-hidden">
        <div className="px-3 py-2 border-b border-border flex flex-col md:flex-row md:items-center gap-2 md:justify-between">
          <div className="relative md:w-72">
            <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-fg-subtle pointer-events-none" />
            <input
              type="text"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Buscar por nombre, email o skill"
              className="w-full pl-8 pr-3 py-1.5 text-[13px] bg-bg border border-border rounded text-fg placeholder:text-fg-subtle focus:outline-none focus:border-accent/60 transition-colors"
            />
          </div>

          <div className="flex flex-wrap items-center gap-1">
            {ROLES.map((r) => {
              const active = rol === r;
              return (
                <button
                  key={r}
                  onClick={() => setRol(r)}
                  className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                    active
                      ? "bg-surface2 text-fg"
                      : "text-fg-muted hover:bg-surface2 hover:text-fg"
                  }`}
                >
                  {r === "TODOS" ? "Todos" : r}
                </button>
              );
            })}
            <span className="mx-1 h-4 w-px bg-border" />
            <button
              onClick={() => setSoloActivos((v) => !v)}
              className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                soloActivos
                  ? "bg-surface2 text-fg"
                  : "text-fg-muted hover:bg-surface2 hover:text-fg"
              }`}
              title="Filtrar solo activos"
            >
              Solo activos
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead className="text-[11px] uppercase tracking-wider text-fg-muted">
              <tr className="border-b border-border">
                {esDirector && (
                  <th className="px-3 py-2 w-8">
                    <Checkbox
                      checked={todosVisiblesSeleccionados}
                      indeterminate={algunoVisibleSeleccionado}
                      onChange={toggleTodos}
                      ariaLabel="Seleccionar todos"
                    />
                  </th>
                )}
                <th className="text-left font-medium px-4 py-2 w-12">#</th>
                <th className="text-left font-medium px-4 py-2">Persona</th>
                <th className="text-left font-medium px-4 py-2 w-28">Rol</th>
                <th className="text-left font-medium px-4 py-2 w-20">Hrs/sem</th>
                <th className="text-left font-medium px-4 py-2">Competencias</th>
                <th className="text-left font-medium px-4 py-2 w-20">Estado</th>
                <th className="w-10" />
                {esDirector && <th className="w-24" />}
              </tr>
            </thead>
            <tbody>
              {err && (
                <tr>
                  <td colSpan={esDirector ? 9 : 7} className="px-4 py-6 text-center text-danger">
                    No se pudo cargar la lista: {err}
                  </td>
                </tr>
              )}
              {!err && !data && (
                <tr>
                  <td colSpan={esDirector ? 9 : 7} className="px-4 py-6 text-center text-fg-muted">
                    Cargando...
                  </td>
                </tr>
              )}
              {data && filtrados.length === 0 && (
                <tr>
                  <td colSpan={esDirector ? 9 : 7} className="px-4 py-6 text-center text-fg-muted">
                    No hay recursos con esos filtros.
                  </td>
                </tr>
              )}
              {filtrados.map((r) => (
                <tr
                  key={r.id}
                  className={`group border-b border-border last:border-0 hover:bg-surface2 transition-colors ${
                    !r.activo ? "opacity-60" : ""
                  } ${seleccionados.has(r.id) ? "bg-surface2" : ""}`}
                >
                  {esDirector && (
                    <td className="px-3 py-2">
                      <Checkbox
                        checked={seleccionados.has(r.id)}
                        onChange={(c) => toggleUno(r.id, c)}
                        ariaLabel={`Seleccionar ${r.nombre}`}
                      />
                    </td>
                  )}
                  <td className="px-4 py-2 font-mono text-fg-subtle">{r.id}</td>
                  <td className="px-4 py-2">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-full bg-surface2 text-fg text-[11px] font-semibold flex items-center justify-center shrink-0">
                        {iniciales(r.nombre)}
                      </div>
                      <div className="min-w-0">
                        <div className="text-fg truncate">{r.nombre}</div>
                        <div className="text-[11px] text-fg-muted truncate">{r.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-2">
                    <RolBadge rol={r.rol} />
                  </td>
                  <td className="px-4 py-2 font-mono text-fg-muted text-[12px]">
                    {r.horasSemanales}h
                  </td>
                  <td className="px-4 py-2 text-fg-muted">
                    <div className="line-clamp-1">{r.competencias || "—"}</div>
                  </td>
                  <td className="px-4 py-2">
                    {r.activo ? (
                      <Badge tone="success">activo</Badge>
                    ) : (
                      <Badge tone="neutral">inactivo</Badge>
                    )}
                  </td>
                  <td className="px-2 py-2 text-right">
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity inline-block">
                      <Dropdown
                        trigger={({ toggle }) => (
                          <button
                            type="button"
                            onClick={toggle}
                            className="text-fg-muted hover:text-fg hover:bg-bg rounded p-1 transition-colors"
                            aria-label="Acciones"
                          >
                            <MoreHorizontal size={14} />
                          </button>
                        )}
                      >
                        {(close) => (
                          <>
                            <DropdownItem
                              icon={<Copy size={13} />}
                              onClick={() => {
                                navigator.clipboard.writeText(r.email);
                                close();
                              }}
                            >
                              Copiar email
                            </DropdownItem>
                            <DropdownItem
                              icon={<Copy size={13} />}
                              onClick={() => {
                                navigator.clipboard.writeText(String(r.id));
                                close();
                              }}
                            >
                              Copiar ID
                            </DropdownItem>
                          </>
                        )}
                      </Dropdown>
                    </div>
                  </td>
                  {esDirector && (
                    <td className="px-3 py-2 text-right">
                      {r.activo ? (
                        <button
                          type="button"
                          onClick={() => setAEditar(r)}
                          className="inline-flex items-center gap-1.5 px-2 py-1 rounded text-[12px] bg-info/15 text-info hover:bg-info/25 transition-colors"
                        >
                          <Pencil size={12} />
                          Editar
                        </button>
                      ) : (
                        <span className="text-[11px] text-fg-subtle">—</span>
                      )}
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <NuevoRecursoModal
        open={modalAbierto}
        onClose={() => setModalAbierto(false)}
        onCreado={() => {
          setModalAbierto(false);
          cargar();
        }}
      />

      <EditarRecursoModal
        recurso={aEditar}
        onClose={() => setAEditar(null)}
        onGuardado={() => {
          setAEditar(null);
          cargar();
        }}
      />

      <ConfirmDialog
        open={confirmBulk}
        onClose={() => { setConfirmBulk(false); setErrBulk(null); }}
        onConfirm={confirmarBorrarBulk}
        title="Borrar varios recursos"
        confirmText={`Borrar ${seleccionados.size}`}
        busy={borrandoBulk}
        error={errBulk}
        message={
          <>
            Vas a eliminar definitivamente <strong className="text-fg">{seleccionados.size}</strong>{" "}
            {seleccionados.size === 1 ? "recurso" : "recursos"}. Esta accion no se puede deshacer.
          </>
        }
      />
    </div>
  );
}

function NuevoRecursoModal({
  open, onClose, onCreado,
}: {
  open: boolean;
  onClose: () => void;
  onCreado: () => void;
}) {
  const [nombre, setNombre] = useState("");
  const [email, setEmail] = useState("");
  const [rol, setRol] = useState("DEV");
  const [horas, setHoras] = useState("40");
  const [competencias, setCompetencias] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setNombre("");
    setEmail("");
    setRol("DEV");
    setHoras("40");
    setCompetencias("");
    setErr(null);
    setEnviando(false);
  }, [open]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!nombre.trim() || !email.trim()) {
      setErr("Nombre y email son obligatorios.");
      return;
    }
    const hrs = parseInt(horas, 10);
    if (Number.isNaN(hrs) || hrs <= 0) {
      setErr("Horas semanales debe ser un numero positivo.");
      return;
    }
    setEnviando(true);
    setErr(null);
    try {
      await api.post("/recursos", {
        nombre: nombre.trim(),
        email: email.trim(),
        rol,
        horasSemanales: hrs,
        competencias: competencias.trim() || null,
      });
      onCreado();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "no se pudo crear");
      setEnviando(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="Nuevo recurso"
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={enviando}>Cancelar</Button>
          <Button variant="primary" onClick={submit} disabled={enviando}>
            {enviando ? "Creando..." : "Crear"}
          </Button>
        </>
      }
    >
      <form onSubmit={submit}>
        <Field label="Nombre">
          <TextInput value={nombre} onChange={(e) => setNombre(e.target.value)} placeholder="Nombre completo" autoFocus />
        </Field>
        <Field label="Email">
          <TextInput type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="persona@innovatech.cl" />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Rol">
            <SelectInput value={rol} onChange={(e) => setRol(e.target.value)}>
              {ROLES_CREAR.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Horas/semana">
            <TextInput type="number" min={1} max={60} value={horas} onChange={(e) => setHoras(e.target.value)} />
          </Field>
        </div>
        <Field label="Competencias">
          <TextArea
            value={competencias}
            onChange={(e) => setCompetencias(e.target.value)}
            rows={2}
            placeholder="Ej: Java, Spring, Postgres"
          />
        </Field>
        {err && (
          <p className="mt-3 text-[12px] text-danger">{err}</p>
        )}
      </form>
    </Modal>
  );
}

function EditarRecursoModal({
  recurso, onClose, onGuardado,
}: {
  recurso: Recurso | null;
  onClose: () => void;
  onGuardado: () => void;
}) {
  const [enviando, setEnviando] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!recurso) return;
    setErr(null);
    setEnviando(false);
  }, [recurso]);

  async function marcarInactivo() {
    if (!recurso) return;
    setEnviando(true);
    setErr(null);
    try {
      await api.patch(`/recursos/${recurso.id}`, { activo: false });
      onGuardado();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "no se pudo guardar");
      setEnviando(false);
    }
  }

  return (
    <Modal
      open={!!recurso}
      onClose={onClose}
      title={recurso ? `Editar ${recurso.nombre}` : "Editar recurso"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={enviando}>Cancelar</Button>
          <Button variant="primary" onClick={marcarInactivo} disabled={enviando}>
            {enviando ? "Guardando..." : "Marcar como inactivo"}
          </Button>
        </>
      }
    >
      <p className="text-[13px] text-fg-muted">
        Vas a marcar a <strong className="text-fg">{recurso?.nombre}</strong> como{" "}
        <strong className="text-fg">inactivo</strong>. El recurso seguira en la BDD pero no contara
        para los KPIs ni aparecera en filtros de "solo activos".
      </p>
      {err && <p className="mt-3 text-[12px] text-danger">{err}</p>}
    </Modal>
  );
}
