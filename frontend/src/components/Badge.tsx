import type { ReactNode } from "react";

type Tone = "neutral" | "success" | "warning" | "danger" | "info";

const tonos: Record<Tone, string> = {
  neutral: "bg-slate-100 text-slate-700 ring-slate-200",
  success: "bg-emerald-50 text-emerald-700 ring-emerald-200",
  warning: "bg-amber-50 text-amber-700 ring-amber-200",
  danger:  "bg-rose-50 text-rose-700 ring-rose-200",
  info:    "bg-indigo-50 text-indigo-700 ring-indigo-200",
};

export function Badge({ tone = "neutral", children }: { tone?: Tone; children: ReactNode }) {
  return (
    <span className={`inline-flex items-center rounded-full ring-1 ring-inset px-2 py-0.5 text-xs font-medium ${tonos[tone]}`}>
      {children}
    </span>
  );
}
