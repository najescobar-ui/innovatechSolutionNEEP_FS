import { useEffect, useMemo, useState } from "react";
import { Copy, MoreHorizontal, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { Badge } from "../components/Badge";
import { Button } from "../components/Button";
import { Checkbox } from "../components/Checkbox";
import { Dropdown, DropdownItem } from "../components/Dropdown";
import { ConfirmDialog, Field, Modal, SelectInput, TextArea, TextInput } from "../components/Modal";
import { api } from "../api/client";
import { useAuth } from "../auth/useAuth";

type Project = {
  id: number;
  name: string;
  description: string | null;
  status: string;
  startDate: string | null;
  plannedEndDate: string | null;
  ownerId: string | null;
};

type Resp = { status: string; items: Project[] };

type Tone = "info" | "success" | "neutral" | "danger" | "warning";

const STATUSES: { value: string; label: string }[] = [
  { value: "ALL",         label: "All" },
  { value: "PLANNING",    label: "Planning" },
  { value: "IN_PROGRESS", label: "In progress" },
  { value: "COMPLETED",   label: "Completed" },
  { value: "CANCELLED",   label: "Cancelled" },
];

const STATUSES_CREATE = ["PLANNING", "IN_PROGRESS", "COMPLETED", "CANCELLED"];

const TONE_BADGE: Record<string, Tone> = {
  PLANNING: "info",
  IN_PROGRESS: "success",
  COMPLETED: "neutral",
  CANCELLED: "danger",
};

const fmt = new Intl.DateTimeFormat("en-US", { day: "2-digit", month: "short", year: "numeric" });
function formatDate(iso: string | null) {
  if (!iso) return "—";
  return fmt.format(new Date(iso));
}

export function Projects() {
  const { roles } = useAuth();
  const isDirector = roles.includes("DIR");

  const [data, setData] = useState<Resp | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("ALL");
  const [modalOpen, setModalOpen] = useState(false);

  /* edicion fila a fila */
  const [toEdit, setToEdit] = useState<Project | null>(null);

  /* seleccion multiple para borrado en lote (solo DIR) */
  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [confirmBulk, setConfirmBulk] = useState(false);
  const [errBulk, setErrBulk] = useState<string | null>(null);
  const [deletingBulk, setDeletingBulk] = useState(false);

  const load = () => {
    api.get<Resp>("/projects")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error"));
  };

  useEffect(load, []);

  function toggleOne(id: number, checked: boolean) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (checked) next.add(id);
      else next.delete(id);
      return next;
    });
  }

  async function confirmDeleteBulk() {
    if (selected.size === 0) return;
    setDeletingBulk(true);
    setErrBulk(null);
    const ids = Array.from(selected);
    const results = await Promise.allSettled(
      ids.map((id) => api.delete(`/projects/${id}`)),
    );
    const fails = results.filter((r) => r.status === "rejected").length;
    setDeletingBulk(false);
    if (fails > 0) {
      setErrBulk(`${fails} of ${ids.length} could not be deleted`);
      load();
      return;
    }
    setConfirmBulk(false);
    setSelected(new Set());
    load();
  }

  const filtered = useMemo(() => {
    if (!data) return [];
    const txt = q.trim().toLowerCase();
    return data.items.filter((p) => {
      const matchTxt = !txt || p.name.toLowerCase().includes(txt) || (p.description ?? "").toLowerCase().includes(txt);
      const matchStatus = status === "ALL" || p.status === status;
      return matchTxt && matchStatus;
    });
  }, [data, q, status]);

  const allVisibleSelected = filtered.length > 0 && filtered.every((p) => selected.has(p.id));
  const someVisibleSelected = filtered.some((p) => selected.has(p.id));

  function toggleAll(checked: boolean) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (checked) filtered.forEach((p) => next.add(p.id));
      else filtered.forEach((p) => next.delete(p.id));
      return next;
    });
  }

  return (
    <div className="space-y-4 max-w-[1200px]">
      <header className="flex items-end justify-between gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-wider text-fg-muted">Portfolio</div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="font-mono text-lg text-fg">{data ? data.items.length : "—"}</span>
            <span className="text-[13px] text-fg-muted">
              projects
              {data && filtered.length !== data.items.length && ` · ${filtered.length} visible`}
            </span>
            {data?.status === "datos no disponibles" && (
              <Badge tone="warning">source down</Badge>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          {isDirector && selected.size > 0 && (
            <button
              type="button"
              onClick={() => { setErrBulk(null); setConfirmBulk(true); }}
              style={{ background: "rgb(var(--danger))", color: "#000" }}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 text-[13px] font-medium rounded hover:opacity-90 transition-opacity"
            >
              <Trash2 size={14} />
              Delete ({selected.size})
            </button>
          )}
          <Button variant="primary" size="md" onClick={() => setModalOpen(true)}>
            <Plus size={14} />
            <span>New project</span>
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
              placeholder="Search by name or description"
              className="w-full pl-8 pr-3 py-1.5 text-[13px] bg-bg border border-border rounded text-fg placeholder:text-fg-subtle focus:outline-none focus:border-accent/60 transition-colors"
            />
          </div>
          <div className="flex flex-wrap gap-1">
            {STATUSES.map((e) => {
              const active = status === e.value;
              return (
                <button
                  key={e.value}
                  onClick={() => setStatus(e.value)}
                  className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                    active
                      ? "bg-surface2 text-fg"
                      : "text-fg-muted hover:bg-surface2 hover:text-fg"
                  }`}
                >
                  {e.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead className="text-[11px] uppercase tracking-wider text-fg-muted">
              <tr className="border-b border-border">
                {isDirector && (
                  <th className="px-3 py-2 w-8">
                    <Checkbox
                      checked={allVisibleSelected}
                      indeterminate={someVisibleSelected}
                      onChange={toggleAll}
                      ariaLabel="Select all"
                    />
                  </th>
                )}
                <th className="text-left font-medium px-4 py-2 w-12">#</th>
                <th className="text-left font-medium px-4 py-2">Project</th>
                <th className="text-left font-medium px-4 py-2 w-36">Status</th>
                <th className="text-left font-medium px-4 py-2 w-28">Start</th>
                <th className="text-left font-medium px-4 py-2 w-28">Planned end</th>
                <th className="text-left font-medium px-4 py-2 w-32">Owner</th>
                <th className="w-10" />
                {isDirector && <th className="w-24" />}
              </tr>
            </thead>
            <tbody>
              {err && (
                <tr>
                  <td colSpan={isDirector ? 9 : 7} className="px-4 py-6 text-center text-danger">
                    Could not load the list: {err}
                  </td>
                </tr>
              )}
              {!err && !data && (
                <tr>
                  <td colSpan={isDirector ? 9 : 7} className="px-4 py-6 text-center text-fg-muted">
                    Loading...
                  </td>
                </tr>
              )}
              {data && filtered.length === 0 && (
                <tr>
                  <td colSpan={isDirector ? 9 : 7} className="px-4 py-6 text-center text-fg-muted">
                    No projects match these filters.
                  </td>
                </tr>
              )}
              {filtered.map((p) => (
                <tr
                  key={p.id}
                  className={`group border-b border-border last:border-0 hover:bg-surface2 transition-colors ${
                    selected.has(p.id) ? "bg-surface2" : ""
                  }`}
                >
                  {isDirector && (
                    <td className="px-3 py-2">
                      <Checkbox
                        checked={selected.has(p.id)}
                        onChange={(c) => toggleOne(p.id, c)}
                        ariaLabel={`Select ${p.name}`}
                      />
                    </td>
                  )}
                  <td className="px-4 py-2 font-mono text-fg-subtle">{p.id}</td>
                  <td className="px-4 py-2">
                    <div className="text-fg">{p.name}</div>
                    {p.description && (
                      <div className="text-[11px] text-fg-muted mt-0.5 line-clamp-1">{p.description}</div>
                    )}
                  </td>
                  <td className="px-4 py-2">
                    <Badge tone={TONE_BADGE[p.status] ?? "neutral"}>
                      {p.status.replace(/_/g, " ").toLowerCase()}
                    </Badge>
                  </td>
                  <td className="px-4 py-2 text-fg-muted font-mono text-[12px]">
                    {formatDate(p.startDate)}
                  </td>
                  <td className="px-4 py-2 text-fg-muted font-mono text-[12px]">
                    {formatDate(p.plannedEndDate)}
                  </td>
                  <td className="px-4 py-2 text-fg-muted">
                    {p.ownerId ? (
                      <span className="font-mono text-[12px]">{p.ownerId}</span>
                    ) : (
                      <span className="text-fg-subtle">—</span>
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
                            aria-label="Actions"
                          >
                            <MoreHorizontal size={14} />
                          </button>
                        )}
                      >
                        {(close) => (
                          <DropdownItem
                            icon={<Copy size={13} />}
                            onClick={() => {
                              navigator.clipboard.writeText(String(p.id));
                              close();
                            }}
                          >
                            Copy ID
                          </DropdownItem>
                        )}
                      </Dropdown>
                    </div>
                  </td>
                  {isDirector && (
                    <td className="px-3 py-2 text-right">
                      <button
                        type="button"
                        onClick={() => setToEdit(p)}
                        className="inline-flex items-center gap-1.5 px-2 py-1 rounded text-[12px] bg-info/15 text-info hover:bg-info/25 transition-colors"
                      >
                        <Pencil size={12} />
                        Edit
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <NewProjectModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreated={() => {
          setModalOpen(false);
          load();
        }}
      />

      <EditProjectModal
        project={toEdit}
        onClose={() => setToEdit(null)}
        onSaved={() => {
          setToEdit(null);
          load();
        }}
      />

      <ConfirmDialog
        open={confirmBulk}
        onClose={() => { setConfirmBulk(false); setErrBulk(null); }}
        onConfirm={confirmDeleteBulk}
        title="Delete several projects"
        confirmText={`Delete ${selected.size}`}
        busy={deletingBulk}
        error={errBulk}
        message={
          <>
            You are about to permanently delete <strong className="text-fg">{selected.size}</strong>{" "}
            {selected.size === 1 ? "project" : "projects"}. This action cannot be undone.
          </>
        }
      />
    </div>
  );
}

function NewProjectModal({
  open, onClose, onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState("PLANNING");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [ownerId, setOwnerId] = useState("");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setName("");
    setDescription("");
    setStatus("PLANNING");
    setStartDate("");
    setEndDate("");
    setOwnerId("");
    setErr(null);
    setSending(false);
  }, [open]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim()) {
      setErr("Name is required.");
      return;
    }
    setSending(true);
    setErr(null);
    try {
      await api.post("/projects", {
        name: name.trim(),
        description: description.trim() || null,
        status,
        startDate: startDate || null,
        plannedEndDate: endDate || null,
        ownerId: ownerId.trim() || null,
      });
      onCreated();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "could not create");
      setSending(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title="New project"
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={sending}>Cancel</Button>
          <Button variant="primary" onClick={submit} disabled={sending}>
            {sending ? "Creating..." : "Create"}
          </Button>
        </>
      }
    >
      <form onSubmit={submit}>
        <Field label="Name">
          <TextInput value={name} onChange={(e) => setName(e.target.value)} placeholder="Ex: Acme Migration" autoFocus />
        </Field>
        <Field label="Description">
          <TextArea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
            placeholder="Short description"
          />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Status">
            <SelectInput value={status} onChange={(e) => setStatus(e.target.value)}>
              {STATUSES_CREATE.map((s) => (
                <option key={s} value={s}>{s.replace(/_/g, " ").toLowerCase()}</option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Owner ID">
            <TextInput value={ownerId} onChange={(e) => setOwnerId(e.target.value)} placeholder="optional" />
          </Field>
          <Field label="Start date">
            <TextInput type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
          </Field>
          <Field label="Planned end">
            <TextInput type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
          </Field>
        </div>
        {err && (
          <p className="mt-3 text-[12px] text-danger">{err}</p>
        )}
      </form>
    </Modal>
  );
}

function EditProjectModal({
  project, onClose, onSaved,
}: {
  project: Project | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [status, setStatus] = useState("PLANNING");
  const [ownerId, setOwnerId] = useState("");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!project) return;
    setStatus(project.status);
    setOwnerId(project.ownerId ?? "");
    setErr(null);
    setSending(false);
  }, [project]);

  async function submit() {
    if (!project) return;
    setSending(true);
    setErr(null);
    try {
      await api.patch(`/projects/${project.id}`, {
        status,
        ownerId: ownerId.trim(),
      });
      onSaved();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "could not save");
      setSending(false);
    }
  }

  return (
    <Modal
      open={!!project}
      onClose={onClose}
      title={project ? `Edit "${project.name}"` : "Edit"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={sending}>Cancel</Button>
          <Button variant="primary" onClick={submit} disabled={sending}>
            {sending ? "Saving..." : "Save"}
          </Button>
        </>
      }
    >
      <Field label="Status">
        <SelectInput value={status} onChange={(e) => setStatus(e.target.value)}>
          {STATUSES_CREATE.map((s) => (
            <option key={s} value={s}>{s.replace(/_/g, " ").toLowerCase()}</option>
          ))}
        </SelectInput>
      </Field>
      <Field label="Owner ID">
        <TextInput
          value={ownerId}
          onChange={(e) => setOwnerId(e.target.value)}
          placeholder="empty to remove owner"
        />
      </Field>
      {err && <p className="mt-3 text-[12px] text-danger">{err}</p>}
    </Modal>
  );
}
