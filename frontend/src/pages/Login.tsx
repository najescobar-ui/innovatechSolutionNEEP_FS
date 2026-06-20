import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "../components/Button";
import { Field, TextInput } from "../components/Modal";
import { useAuth } from "../auth/useAuth";

export function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!username.trim() || !password) {
      setErr("Ingresa usuario y contraseña.");
      return;
    }
    setSending(true);
    setErr(null);
    try {
      await login(username.trim(), password);
      navigate("/", { replace: true });
    } catch {
      setErr("Credenciales inválidas.");
      setSending(false);
    }
  }

  return (
    <AuthShell title="Iniciar sesión" subtitle="Accede a tu panel de Innovatech.">
      <form onSubmit={submit}>
        <Field label="Usuario (email)">
          <TextInput value={username} onChange={(e) => setUsername(e.target.value)} placeholder="ana@empresa.cl" autoFocus />
        </Field>
        <Field label="Contraseña">
          <TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••" />
        </Field>
        {err && <p className="mt-1 mb-2 text-[12px] text-danger">{err}</p>}
        <Button variant="primary" size="md" className="w-full mt-2" disabled={sending}>
          {sending ? "Ingresando..." : "Ingresar"}
        </Button>
      </form>
      <p className="mt-4 text-[12px] text-fg-muted text-center">
        ¿No tienes cuenta?{" "}
        <Link to="/register" className="text-accent hover:underline">Regístrate</Link>
      </p>
    </AuthShell>
  );
}

export function AuthShell({ title, subtitle, children }: { title: string; subtitle: string; children: React.ReactNode }) {
  return (
    <div className="h-full flex items-center justify-center bg-bg text-fg px-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-6">
          <img src="/logoLuffy.png" alt="InnovaTech" className="w-10 h-10 object-contain mb-2 select-none" draggable={false} />
          <span className="font-brand text-[16px] font-semibold">Innova<span className="text-accent">Tech</span></span>
        </div>
        <div className="bg-surface border border-border rounded-md p-5">
          <h1 className="text-[15px] font-medium text-fg">{title}</h1>
          <p className="text-[12px] text-fg-muted mt-0.5 mb-4">{subtitle}</p>
          {children}
        </div>
      </div>
    </div>
  );
}
