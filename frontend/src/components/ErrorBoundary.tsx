import { Component } from "react";
import type { ReactNode } from "react";

type Props = { children: ReactNode };
type State = { error: Error | null };

/** Evita que un error de render en una vista tumbe toda la app (pantalla negra). */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null };

  static getDerivedStateFromError(error: Error): State {
    return { error };
  }

  render() {
    if (this.state.error) {
      return (
        <div className="h-full flex flex-col items-center justify-center gap-3 text-fg p-6 text-center">
          <p className="text-[14px] font-medium">Algo salió mal en esta vista.</p>
          <p className="text-[12px] text-fg-muted max-w-md break-words">{this.state.error.message}</p>
          <a
            href="/"
            className="mt-2 inline-flex items-center rounded px-3 py-1.5 text-[13px] font-medium bg-accent text-accent-fg hover:bg-accent/90 transition-colors"
          >
            Volver al inicio
          </a>
        </div>
      );
    }
    return this.props.children;
  }
}
