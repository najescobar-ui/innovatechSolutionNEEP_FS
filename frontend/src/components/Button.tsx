import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "ghost" | "outline";
};

export function Button({ variant = "primary", className = "", ...rest }: Props) {
  const base = "inline-flex items-center justify-center rounded-md text-sm font-medium px-4 py-2 transition-colors disabled:opacity-50 disabled:cursor-not-allowed";
  const styles: Record<string, string> = {
    primary: "bg-indigo-600 text-white hover:bg-indigo-700",
    ghost: "text-slate-600 hover:text-slate-900 hover:bg-slate-100",
    outline: "border border-slate-300 text-slate-700 hover:bg-slate-50",
  };
  return <button className={`${base} ${styles[variant]} ${className}`} {...rest} />;
}
