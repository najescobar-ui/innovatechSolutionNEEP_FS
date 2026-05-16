import { useEffect } from "react";
import type { ReactNode } from "react";
import { X } from "lucide-react";

type Props = {
  open: boolean;
  onClose: () => void;
  title: string;
  children: ReactNode;
  footer?: ReactNode;
};

export function Modal({ open, onClose, title, children, footer }: Props) {
  useEffect(() => {
    if (!open) return;
    const onEsc = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", onEsc);
    // bloquear scroll del body mientras esta abierto
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onEsc);
      document.body.style.overflow = prev;
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        className="w-full max-w-md bg-surface border border-border rounded-md shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="px-4 py-3 border-b border-border flex items-center justify-between">
          <h2 className="text-[14px] font-medium text-fg">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            className="text-fg-muted hover:text-fg hover:bg-surface2 rounded p-1 transition-colors"
            aria-label="Cerrar"
          >
            <X size={16} />
          </button>
        </div>
        <div className="px-4 py-3">{children}</div>
        {footer && (
          <div className="px-4 py-3 border-t border-border flex items-center justify-end gap-2">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}

// Helpers de form para mantener un estilo consistente.
export function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="block mb-3 last:mb-0">
      <span className="block text-[11px] uppercase tracking-wider text-fg-muted mb-1">{label}</span>
      {children}
    </label>
  );
}

const baseInput =
  "w-full px-2.5 py-1.5 text-[13px] bg-bg border border-border rounded text-fg placeholder:text-fg-subtle focus:outline-none focus:border-accent/60 transition-colors";

export function TextInput(props: React.InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} className={`${baseInput} ${props.className ?? ""}`} />;
}

export function SelectInput(props: React.SelectHTMLAttributes<HTMLSelectElement>) {
  return <select {...props} className={`${baseInput} ${props.className ?? ""}`} />;
}

export function TextArea(props: React.TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={`${baseInput} resize-none ${props.className ?? ""}`} />;
}

// Confirmacion para acciones destructivas. tono=danger marca el boton primario en rojo.
type ConfirmProps = {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void | Promise<void>;
  title: string;
  message: React.ReactNode;
  confirmText?: string;
  cancelText?: string;
  tone?: "danger" | "primary";
  busy?: boolean;
  error?: string | null;
};

export function ConfirmDialog({
  open, onClose, onConfirm, title, message,
  confirmText = "Confirmar", cancelText = "Cancelar",
  tone = "danger", busy = false, error = null,
}: ConfirmProps) {
  const btnCls = tone === "danger"
    ? "bg-danger text-fg hover:bg-danger/90"
    : "bg-accent text-accent-fg hover:bg-accent/90";

  return (
    <Modal
      open={open}
      onClose={busy ? () => {} : onClose}
      title={title}
      footer={
        <>
          <button
            type="button"
            onClick={onClose}
            disabled={busy}
            className="inline-flex items-center justify-center gap-1.5 rounded font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed px-3 py-1.5 text-[13px] text-fg-muted hover:bg-surface2 hover:text-fg"
          >
            {cancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={busy}
            className={`inline-flex items-center justify-center gap-1.5 rounded font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed px-3 py-1.5 text-[13px] ${btnCls}`}
          >
            {busy ? "..." : confirmText}
          </button>
        </>
      }
    >
      <div className="text-[13px] text-fg-muted">{message}</div>
      {error && <p className="mt-3 text-[12px] text-danger">{error}</p>}
    </Modal>
  );
}
