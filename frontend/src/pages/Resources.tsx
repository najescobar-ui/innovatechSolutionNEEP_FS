import { useEffect, useMemo, useState } from "react";
import { Copy, MoreHorizontal, Pencil, Plus, Search, Trash2 } from "lucide-react";
import { Badge } from "../components/Badge";
import { Button } from "../components/Button";
import { Checkbox } from "../components/Checkbox";
import { Dropdown, DropdownItem } from "../components/Dropdown";
import { ConfirmDialog, Field, Modal, SelectInput, TextArea, TextInput } from "../components/Modal";
import { api } from "../api/client";
import { useAuth } from "../auth/useAuth";

type Resource = {
  id: number;
  name: string;
  email: string;
  role: string;
  weeklyHours: number;
  skills: string | null;
  active: boolean;
};

type Resp = { status: string; items: Resource[] };

const ROLES = ["ALL", "DEV", "QA", "DEVOPS", "DESIGNER", "PM"] as const;
const ROLES_CREATE = ["DEV", "QA", "DEVOPS", "DESIGNER", "PM"];

const ROLE_VAR: Record<string, string> = {
  DEV:      "var(--role-dev)",
  QA:       "var(--role-qa)",
  DEVOPS:   "var(--role-devops)",
  DESIGNER: "var(--role-designer)",
  PM:       "var(--role-pm)",
};

function RoleBadge({ role }: { role: string }) {
  const v = ROLE_VAR[role];
  if (!v) return <Badge tone="neutral">{role}</Badge>;
  return (
    <span
      className="inline-flex items-center rounded px-1.5 py-0.5 text-[11px] font-medium"
      style={{ background: `rgb(${v} / 0.12)`, color: `rgb(${v})` }}
    >
      {role}
    </span>
  );
}

function initials(name: string) {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0]?.toUpperCase())
    .join("");
}

export function Resources() {
  const { roles } = useAuth();
  const isDirector = roles.includes("DIR");

  const [data, setData] = useState<Resp | null>(null);
  const [err, setErr] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [role, setRole] = useState<string>("ALL");
  const [onlyActive, setOnlyActive] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);

  const [toEdit, setToEdit] = useState<Resource | null>(null);

  const [selected, setSelected] = useState<Set<number>>(new Set());
  const [confirmBulk, setConfirmBulk] = useState(false);
  const [errBulk, setErrBulk] = useState<string | null>(null);
  const [deletingBulk, setDeletingBulk] = useState(false);

  const load = () => {
    api.get<Resp>("/resources")
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
      ids.map((id) => api.delete(`/resources/${id}`)),
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
    return data.items.filter((r) => {
      const matchTxt = !txt
        || r.name.toLowerCase().includes(txt)
        || r.email.toLowerCase().includes(txt)
        || (r.skills ?? "").toLowerCase().includes(txt);
      const matchRole = role === "ALL" || r.role === role;
      const matchActive = !onlyActive || r.active;
      return matchTxt && matchRole && matchActive;
    });
  }, [data, q, role, onlyActive]);

  const allVisibleSelected = filtered.length > 0 && filtered.every((r) => selected.has(r.id));
  const someVisibleSelected = filtered.some((r) => selected.has(r.id));

  function toggleAll(checked: boolean) {
    setSelected((prev) => {
      const next = new Set(prev);
      if (checked) filtered.forEach((r) => next.add(r.id));
      else filtered.forEach((r) => next.delete(r.id));
      return next;
    });
  }

  return (
    <div className="space-y-4 max-w-[1200px]">
      <header className="flex items-end justify-between gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-wider text-fg-muted">Team</div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="font-mono text-lg text-fg">{data ? data.items.length : "—"}</span>
            <span className="text-[13px] text-fg-muted">
              resources
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
            <span>New resource</span>
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
              placeholder="Search by name, email or skill"
              className="w-full pl-8 pr-3 py-1.5 text-[13px] bg-bg border border-border rounded text-fg placeholder:text-fg-subtle focus:outline-none focus:border-accent/60 transition-colors"
            />
          </div>

          <div className="flex flex-wrap items-center gap-1">
            {ROLES.map((r) => {
              const active = role === r;
              return (
                <button
                  key={r}
                  onClick={() => setRole(r)}
                  className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                    active
                      ? "bg-surface2 text-fg"
                      : "text-fg-muted hover:bg-surface2 hover:text-fg"
                  }`}
                >
                  {r === "ALL" ? "All" : r}
                </button>
              );
            })}
            <span className="mx-1 h-4 w-px bg-border" />
            <button
              onClick={() => setOnlyActive((v) => !v)}
              className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                onlyActive
                  ? "bg-surface2 text-fg"
                  : "text-fg-muted hover:bg-surface2 hover:text-fg"
              }`}
              title="Show only active"
            >
              Active only
            </button>
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
                <th className="text-left font-medium px-4 py-2">Person</th>
                <th className="text-left font-medium px-4 py-2 w-28">Role</th>
                <th className="text-left font-medium px-4 py-2 w-20">Hrs/wk</th>
                <th className="text-left font-medium px-4 py-2">Skills</th>
                <th className="text-left font-medium px-4 py-2 w-20">Status</th>
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
                    No resources match these filters.
                  </td>
                </tr>
              )}
              {filtered.map((r) => (
                <tr
                  key={r.id}
                  className={`group border-b border-border last:border-0 hover:bg-surface2 transition-colors ${
                    !r.active ? "opacity-60" : ""
                  } ${selected.has(r.id) ? "bg-surface2" : ""}`}
                >
                  {isDirector && (
                    <td className="px-3 py-2">
                      <Checkbox
                        checked={selected.has(r.id)}
                        onChange={(c) => toggleOne(r.id, c)}
                        ariaLabel={`Select ${r.name}`}
                      />
                    </td>
                  )}
                  <td className="px-4 py-2 font-mono text-fg-subtle">{r.id}</td>
                  <td className="px-4 py-2">
                    <div className="flex items-center gap-2.5">
                      <div className="w-7 h-7 rounded-full bg-surface2 text-fg text-[11px] font-semibold flex items-center justify-center shrink-0">
                        {initials(r.name)}
                      </div>
                      <div className="min-w-0">
                        <div className="text-fg truncate">{r.name}</div>
                        <div className="text-[11px] text-fg-muted truncate">{r.email}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-4 py-2">
                    <RoleBadge role={r.role} />
                  </td>
                  <td className="px-4 py-2 font-mono text-fg-muted text-[12px]">
                    {r.weeklyHours}h
                  </td>
                  <td className="px-4 py-2 text-fg-muted">
                    <div className="line-clamp-1">{r.skills || "—"}</div>
                  </td>
                  <td className="px-4 py-2">
                    {r.active ? (
                      <Badge tone="success">active</Badge>
                    ) : (
                      <Badge tone="neutral">inactive</Badge>
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
                          <>
                            <DropdownItem
                              icon={<Copy size={13} />}
                              onClick={() => {
                                navigator.clipboard.writeText(r.email);
                                close();
                              }}
                            >
                              Copy email
                            </DropdownItem>
                            <DropdownItem
                              icon={<Copy size={13} />}
                              onClick={() => {
                                navigator.clipboard.writeText(String(r.id));
                                close();
                              }}
                            >
                              Copy ID
                            </DropdownItem>
                          </>
                        )}
                      </Dropdown>
                    </div>
                  </td>
                  {isDirector && (
                    <td className="px-3 py-2 text-right">
                      {r.active ? (
                        <button
                          type="button"
                          onClick={() => setToEdit(r)}
                          className="inline-flex items-center gap-1.5 px-2 py-1 rounded text-[12px] bg-info/15 text-info hover:bg-info/25 transition-colors"
                        >
                          <Pencil size={12} />
                          Edit
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

      <NewResourceModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onCreated={() => {
          setModalOpen(false);
          load();
        }}
      />

      <EditResourceModal
        resource={toEdit}
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
        title="Delete several resources"
        confirmText={`Delete ${selected.size}`}
        busy={deletingBulk}
        error={errBulk}
        message={
          <>
            You are about to permanently delete <strong className="text-fg">{selected.size}</strong>{" "}
            {selected.size === 1 ? "resource" : "resources"}. This action cannot be undone.
          </>
        }
      />
    </div>
  );
}

function NewResourceModal({
  open, onClose, onCreated,
}: {
  open: boolean;
  onClose: () => void;
  onCreated: () => void;
}) {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [role, setRole] = useState("DEV");
  const [hours, setHours] = useState("40");
  const [skills, setSkills] = useState("");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setName("");
    setEmail("");
    setRole("DEV");
    setHours("40");
    setSkills("");
    setErr(null);
    setSending(false);
  }, [open]);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!name.trim() || !email.trim()) {
      setErr("Name and email are required.");
      return;
    }
    const hrs = parseInt(hours, 10);
    if (Number.isNaN(hrs) || hrs <= 0) {
      setErr("Weekly hours must be a positive number.");
      return;
    }
    setSending(true);
    setErr(null);
    try {
      await api.post("/resources", {
        name: name.trim(),
        email: email.trim(),
        role,
        weeklyHours: hrs,
        skills: skills.trim() || null,
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
      title="New resource"
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
          <TextInput value={name} onChange={(e) => setName(e.target.value)} placeholder="Full name" autoFocus />
        </Field>
        <Field label="Email">
          <TextInput type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="person@innovatech.cl" />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Role">
            <SelectInput value={role} onChange={(e) => setRole(e.target.value)}>
              {ROLES_CREATE.map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Hours/week">
            <TextInput type="number" min={1} max={60} value={hours} onChange={(e) => setHours(e.target.value)} />
          </Field>
        </div>
        <Field label="Skills">
          <TextArea
            value={skills}
            onChange={(e) => setSkills(e.target.value)}
            rows={2}
            placeholder="Ex: Java, Spring, Postgres"
          />
        </Field>
        {err && (
          <p className="mt-3 text-[12px] text-danger">{err}</p>
        )}
      </form>
    </Modal>
  );
}

function EditResourceModal({
  resource, onClose, onSaved,
}: {
  resource: Resource | null;
  onClose: () => void;
  onSaved: () => void;
}) {
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!resource) return;
    setErr(null);
    setSending(false);
  }, [resource]);

  async function markInactive() {
    if (!resource) return;
    setSending(true);
    setErr(null);
    try {
      await api.patch(`/resources/${resource.id}`, { active: false });
      onSaved();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "could not save");
      setSending(false);
    }
  }

  return (
    <Modal
      open={!!resource}
      onClose={onClose}
      title={resource ? `Edit ${resource.name}` : "Edit resource"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={sending}>Cancel</Button>
          <Button variant="primary" onClick={markInactive} disabled={sending}>
            {sending ? "Saving..." : "Mark as inactive"}
          </Button>
        </>
      }
    >
      <p className="text-[13px] text-fg-muted">
        You are about to mark <strong className="text-fg">{resource?.name}</strong> as{" "}
        <strong className="text-fg">inactive</strong>. The resource stays in the DB but will not count
        toward KPIs nor appear in "active only" filters.
      </p>
      {err && <p className="mt-3 text-[12px] text-danger">{err}</p>}
    </Modal>
  );
}
