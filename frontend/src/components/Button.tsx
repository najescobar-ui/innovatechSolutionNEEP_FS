import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "ghost" | "outline";
  size?: "sm" | "md";
};

export function Button({ variant = "primary", size = "md", className = "", ...rest }: Props) {
  const sizes = {
    sm: "px-2.5 py-1 text-xs",
    md: "px-3 py-1.5 text-[13px]",
  };
  const styles: Record<string, string> = {
    primary: "bg-accent text-accent-fg hover:bg-accent/90",
    ghost:   "text-fg-muted hover:bg-surface2 hover:text-fg",
    outline: "border border-border text-fg hover:bg-surface2",
  };
  const base = "inline-flex items-center justify-center gap-1.5 rounded font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed";
  return <button className={`${base} ${sizes[size]} ${styles[variant]} ${className}`} {...rest} />;
}
