import { useEffect, useMemo, useState } from "react";
import { Pencil, Plus, Search, Trash2 } from "lucide-react";
import { Badge } from "../components/Badge";
import { Button } from "../components/Button";
import { ConfirmDialog, Field, Modal, SelectInput, TextArea, TextInput } from "../components/Modal";
import { api } from "../api/client";

type Task = {
  id: number;
  projectId: number;
  title: string;
  description: string | null;
  status: string;
  assigneeResourceId: number | null;
  estimatedHours: number;
  dueDate: string | null;
};

type TasksResp = { status: string; items: Task[] };
type Project = { id: number; name: string };
type Resource = { id: number; name: string; role: string };

type Tone = "info" | "success" | "neutral" | "danger" | "warning";

const STATUSES: { value: string; label: string }[] = [
  { value: "ALL",         label: "All" },
  { value: "TODO",        label: "To do" },
  { value: "IN_PROGRESS", label: "In progress" },
  { value: "DONE",        label: "Done" },
  { value: "BLOCKED",     label: "Blocked" },
];

const STATUSES_FORM = ["TODO", "IN_PROGRESS", "DONE", "BLOCKED"];

const TONE_BADGE: Record<string, Tone> = {
  TODO: "neutral",
  IN_PROGRESS: "info",
  DONE: "success",
  BLOCKED: "danger",
};

const fmt = new Intl.DateTimeFormat("en-US", { day: "2-digit", month: "short", year: "numeric" });
function formatDate(iso: string | null) {
  if (!iso) return "—";
  return fmt.format(new Date(iso + "T00:00:00"));
}
function isOverdue(t: Task) {
  if (!t.dueDate || t.status === "DONE") return false;
  return new Date(t.dueDate + "T00:00:00") < new Date(new Date().toDateString());
}

export function Tasks() {
  const [data, setData] = useState<TasksResp | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [resources, setResources] = useState<Resource[]>([]);
  const [err, setErr] = useState<string | null>(null);
  const [q, setQ] = useState("");
  const [status, setStatus] = useState("ALL");
  const [projectFilter, setProjectFilter] = useState("ALL");

  const [modalOpen, setModalOpen] = useState(false);
  const [toEdit, setToEdit] = useState<Task | null>(null);
  const [toDelete, setToDelete] = useState<Task | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [errDelete, setErrDelete] = useState<string | null>(null);

  const load = () => {
    api.get<TasksResp>("/tasks")
      .then((r) => setData(r.data))
      .catch((e) => setErr(e.message ?? "error"));
  };

  useEffect(() => {
    load();
    api.get<{ items: Project[] }>("/projects").then((r) => setProjects(r.data.items ?? [])).catch(() => {});
    api.get<Resource[]>("/resources").then((r) => setResources(r.data ?? [])).catch(() => {});
  }, []);

  const projectName = (id: number) => projects.find((p) => p.id === id)?.name ?? `#${id}`;
  const resourceName = (id: number | null) =>
    id == null ? null : resources.find((r) => r.id === id)?.name ?? `#${id}`;

  const filtered = useMemo(() => {
    if (!data) return [];
    const txt = q.trim().toLowerCase();
    return data.items.filter((t) => {
      const matchTxt = !txt || t.title.toLowerCase().includes(txt) || (t.description ?? "").toLowerCase().includes(txt);
      const matchStatus = status === "ALL" || t.status === status;
      const matchProject = projectFilter === "ALL" || String(t.projectId) === projectFilter;
      return matchTxt && matchStatus && matchProject;
    });
  }, [data, q, status, projectFilter]);

  async function confirmDelete() {
    if (!toDelete) return;
    setDeleting(true);
    setErrDelete(null);
    try {
      await api.delete(`/tasks/${toDelete.id}`);
      setToDelete(null);
      load();
    } catch (e: any) {
      setErrDelete(e?.message ?? "could not delete");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <div className="space-y-4 max-w-[1200px]">
      <header className="flex items-end justify-between gap-4">
        <div>
          <div className="text-[11px] uppercase tracking-wider text-fg-muted">Work</div>
          <div className="flex items-center gap-2 mt-0.5">
            <span className="font-mono text-lg text-fg">{data ? data.items.length : "—"}</span>
            <span className="text-[13px] text-fg-muted">
              tasks
              {data && filtered.length !== data.items.length && ` · ${filtered.length} visible`}
            </span>
            {data?.status === "datos no disponibles" && <Badge tone="warning">source down</Badge>}
          </div>
        </div>
        <Button variant="primary" size="md" onClick={() => setModalOpen(true)}>
          <Plus size={14} />
          <span>New task</span>
        </Button>
      </header>

      <div className="bg-surface border border-border rounded-md overflow-hidden">
        <div className="px-3 py-2 border-b border-border flex flex-col md:flex-row md:items-center gap-2 md:justify-between">
          <div className="relative md:w-72">
            <Search size={14} className="absolute left-2.5 top-1/2 -translate-y-1/2 text-fg-subtle pointer-events-none" />
            <input
              type="text"
              value={q}
              onChange={(e) => setQ(e.target.value)}
              placeholder="Search by title or description"
              className="w-full pl-8 pr-3 py-1.5 text-[13px] bg-bg border border-border rounded text-fg placeholder:text-fg-subtle focus:outline-none focus:border-accent/60 transition-colors"
            />
          </div>
          <div className="flex items-center gap-2 flex-wrap">
            <select
              value={projectFilter}
              onChange={(e) => setProjectFilter(e.target.value)}
              className="text-[12px] bg-bg border border-border rounded px-2 py-1 text-fg focus:outline-none focus:border-accent/60"
            >
              <option value="ALL">All projects</option>
              {projects.map((p) => (
                <option key={p.id} value={String(p.id)}>{p.name}</option>
              ))}
            </select>
            <div className="flex flex-wrap gap-1">
              {STATUSES.map((e) => {
                const active = status === e.value;
                return (
                  <button
                    key={e.value}
                    onClick={() => setStatus(e.value)}
                    className={`text-[12px] px-2.5 py-1 rounded transition-colors ${
                      active ? "bg-surface2 text-fg" : "text-fg-muted hover:bg-surface2 hover:text-fg"
                    }`}
                  >
                    {e.label}
                  </button>
                );
              })}
            </div>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-[13px]">
            <thead className="text-[11px] uppercase tracking-wider text-fg-muted">
              <tr className="border-b border-border">
                <th className="text-left font-medium px-4 py-2 w-12">#</th>
                <th className="text-left font-medium px-4 py-2">Task</th>
                <th className="text-left font-medium px-4 py-2 w-44">Project</th>
                <th className="text-left font-medium px-4 py-2 w-32">Status</th>
                <th className="text-left font-medium px-4 py-2 w-36">Assignee</th>
                <th className="text-left font-medium px-4 py-2 w-16">Hrs</th>
                <th className="text-left font-medium px-4 py-2 w-28">Due</th>
                <th className="w-24" />
              </tr>
            </thead>
            <tbody>
              {err && (
                <tr><td colSpan={8} className="px-4 py-6 text-center text-danger">Could not load the list: {err}</td></tr>
              )}
              {!err && !data && (
                <tr><td colSpan={8} className="px-4 py-6 text-center text-fg-muted">Loading...</td></tr>
              )}
              {data && filtered.length === 0 && (
                <tr><td colSpan={8} className="px-4 py-6 text-center text-fg-muted">No tasks match these filters.</td></tr>
              )}
              {filtered.map((t) => (
                <tr key={t.id} className="group border-b border-border last:border-0 hover:bg-surface2 transition-colors">
                  <td className="px-4 py-2 font-mono text-fg-subtle">{t.id}</td>
                  <td className="px-4 py-2">
                    <div className="text-fg">{t.title}</div>
                    {t.description && <div className="text-[11px] text-fg-muted mt-0.5 line-clamp-1">{t.description}</div>}
                  </td>
                  <td className="px-4 py-2 text-fg-muted">{projectName(t.projectId)}</td>
                  <td className="px-4 py-2">
                    <Badge tone={TONE_BADGE[t.status] ?? "neutral"}>
                      {t.status.replace(/_/g, " ").toLowerCase()}
                    </Badge>
                  </td>
                  <td className="px-4 py-2 text-fg-muted">
                    {resourceName(t.assigneeResourceId) ?? <span className="text-fg-subtle">unassigned</span>}
                  </td>
                  <td className="px-4 py-2 font-mono text-fg-muted text-[12px]">{t.estimatedHours}</td>
                  <td className="px-4 py-2 font-mono text-[12px]">
                    <span className={isOverdue(t) ? "text-danger" : "text-fg-muted"}>{formatDate(t.dueDate)}</span>
                  </td>
                  <td className="px-3 py-2 text-right">
                    <div className="opacity-0 group-hover:opacity-100 transition-opacity inline-flex items-center gap-1">
                      <button
                        type="button"
                        onClick={() => setToEdit(t)}
                        className="inline-flex items-center gap-1.5 px-2 py-1 rounded text-[12px] bg-info/15 text-info hover:bg-info/25 transition-colors"
                      >
                        <Pencil size={12} /> Edit
                      </button>
                      <button
                        type="button"
                        onClick={() => { setErrDelete(null); setToDelete(t); }}
                        className="text-fg-muted hover:text-danger hover:bg-bg rounded p-1 transition-colors"
                        aria-label="Delete"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <TaskModal
        open={modalOpen}
        task={null}
        projects={projects}
        resources={resources}
        onClose={() => setModalOpen(false)}
        onSaved={() => { setModalOpen(false); load(); }}
      />
      <TaskModal
        open={!!toEdit}
        task={toEdit}
        projects={projects}
        resources={resources}
        onClose={() => setToEdit(null)}
        onSaved={() => { setToEdit(null); load(); }}
      />

      <ConfirmDialog
        open={!!toDelete}
        onClose={() => { setToDelete(null); setErrDelete(null); }}
        onConfirm={confirmDelete}
        title="Delete task"
        confirmText="Delete"
        busy={deleting}
        error={errDelete}
        message={
          <>You are about to permanently delete <strong className="text-fg">{toDelete?.title}</strong>. This action cannot be undone.</>
        }
      />
    </div>
  );
}

function TaskModal({
  open, task, projects, resources, onClose, onSaved,
}: {
  open: boolean;
  task: Task | null;
  projects: Project[];
  resources: Resource[];
  onClose: () => void;
  onSaved: () => void;
}) {
  const editing = !!task;
  const [projectId, setProjectId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [status, setStatus] = useState("TODO");
  const [assignee, setAssignee] = useState("");
  const [estimatedHours, setEstimatedHours] = useState("0");
  const [dueDate, setDueDate] = useState("");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    if (!open) return;
    setProjectId(task ? String(task.projectId) : (projects[0] ? String(projects[0].id) : ""));
    setTitle(task?.title ?? "");
    setDescription(task?.description ?? "");
    setStatus(task?.status ?? "TODO");
    setAssignee(task?.assigneeResourceId != null ? String(task.assigneeResourceId) : "");
    setEstimatedHours(String(task?.estimatedHours ?? 0));
    setDueDate(task?.dueDate ?? "");
    setErr(null);
    setSending(false);
  }, [open, task, projects]);

  async function submit(e?: React.FormEvent) {
    e?.preventDefault();
    if (!editing && !projectId) { setErr("Project is required."); return; }
    if (!title.trim()) { setErr("Title is required."); return; }
    setSending(true);
    setErr(null);
    const body = {
      ...(editing ? {} : { projectId: Number(projectId) }),
      title: title.trim(),
      description: description.trim() || null,
      status,
      assigneeResourceId: assignee ? Number(assignee) : null,
      estimatedHours: Number(estimatedHours) || 0,
      dueDate: dueDate || null,
    };
    try {
      if (editing) await api.patch(`/tasks/${task!.id}`, body);
      else await api.post("/tasks", body);
      onSaved();
    } catch (e: any) {
      setErr(e?.response?.data?.message ?? e?.message ?? "could not save");
      setSending(false);
    }
  }

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={editing ? `Edit "${task?.title}"` : "New task"}
      footer={
        <>
          <Button variant="ghost" onClick={onClose} disabled={sending}>Cancel</Button>
          <Button variant="primary" onClick={() => submit()} disabled={sending}>
            {sending ? "Saving..." : editing ? "Save" : "Create"}
          </Button>
        </>
      }
    >
      <form onSubmit={submit}>
        <Field label="Project">
          <SelectInput value={projectId} onChange={(e) => setProjectId(e.target.value)} disabled={editing}>
            {projects.length === 0 && <option value="">No projects</option>}
            {projects.map((p) => (
              <option key={p.id} value={String(p.id)}>{p.name}</option>
            ))}
          </SelectInput>
        </Field>
        <Field label="Title">
          <TextInput value={title} onChange={(e) => setTitle(e.target.value)} placeholder="Ex: Implement login" autoFocus />
        </Field>
        <Field label="Description">
          <TextArea value={description} onChange={(e) => setDescription(e.target.value)} rows={2} placeholder="Short description" />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Status">
            <SelectInput value={status} onChange={(e) => setStatus(e.target.value)}>
              {STATUSES_FORM.map((s) => (
                <option key={s} value={s}>{s.replace(/_/g, " ").toLowerCase()}</option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Assignee">
            <SelectInput value={assignee} onChange={(e) => setAssignee(e.target.value)}>
              <option value="">Unassigned</option>
              {resources.map((r) => (
                <option key={r.id} value={String(r.id)}>{r.name} ({r.role})</option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Estimated hours / week">
            <TextInput type="number" min="0" value={estimatedHours} onChange={(e) => setEstimatedHours(e.target.value)} />
          </Field>
          <Field label="Due date">
            <TextInput type="date" value={dueDate} onChange={(e) => setDueDate(e.target.value)} />
          </Field>
        </div>
        {err && <p className="mt-3 text-[12px] text-danger">{err}</p>}
      </form>
    </Modal>
  );
}
