import { useEffect, useRef } from "react";

type Props = {
  checked: boolean;
  indeterminate?: boolean;
  onChange: (checked: boolean) => void;
  ariaLabel?: string;
};

/** Checkbox dark-friendly con soporte indeterminate (necesita ref). */
export function Checkbox({ checked, indeterminate = false, onChange, ariaLabel }: Props) {
  const ref = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (ref.current) ref.current.indeterminate = indeterminate && !checked;
  }, [indeterminate, checked]);

  return (
    <input
      ref={ref}
      type="checkbox"
      checked={checked}
      onChange={(e) => onChange(e.target.checked)}
      onClick={(e) => e.stopPropagation()}
      aria-label={ariaLabel}
      className="w-3.5 h-3.5 rounded border border-border bg-bg accent-accent cursor-pointer align-middle"
    />
  );
}
