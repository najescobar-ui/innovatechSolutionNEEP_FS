import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { Button } from "../components/Button";
import { Field, SelectInput, TextInput } from "../components/Modal";
import { useAuth } from "../auth/useAuth";
import { api } from "../api/client";
import { AuthShell } from "./Login";

const ROLES = [
  { v: "DEV", l: "Desarrollador (DEV)" },
  { v: "PM", l: "Project Manager (PM)" },
  { v: "DIR", l: "Directivo (DIR)" },
];

export function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [rut, setRut] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("DEV");
  const [sending, setSending] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!firstName.trim() || !lastName.trim() || !email.trim() || !rut.trim() || !password) {
      setErr("Completa todos los campos.");
      return;
    }
    setSending(true);
    setErr(null);
    try {
      await api.post("/auth/register", {
        firstName: firstName.trim(),
        lastName: lastName.trim(),
        email: email.trim(),
        rut: rut.trim(),
        password,
        role,
      });
      await login(email.trim(), password);
      navigate("/", { replace: true });
    } catch (e: any) {
      const status = e?.response?.status;
      if (status === 409) setErr("El email ya está registrado.");
      else setErr(e?.response?.data?.message ?? "No se pudo crear la cuenta.");
      setSending(false);
    }
  }

  return (
    <AuthShell title="Crear cuenta" subtitle="Regístrate y elige tu perfil de trabajo.">
      <form onSubmit={submit}>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Nombre">
            <TextInput value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="Ana" autoFocus />
          </Field>
          <Field label="Apellido">
            <TextInput value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Díaz" />
          </Field>
        </div>
        <Field label="Email">
          <TextInput type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="ana@empresa.cl" />
        </Field>
        <Field label="RUT">
          <TextInput value={rut} onChange={(e) => setRut(e.target.value)} placeholder="12.345.678-9" />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Contraseña">
            <TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••" />
          </Field>
          <Field label="Perfil">
            <SelectInput value={role} onChange={(e) => setRole(e.target.value)}>
              {ROLES.map((r) => <option key={r.v} value={r.v}>{r.l}</option>)}
            </SelectInput>
          </Field>
        </div>
        {err && <p className="mt-1 mb-2 text-[12px] text-danger">{err}</p>}
        <Button variant="primary" size="md" className="w-full mt-2" disabled={sending}>
          {sending ? "Creando..." : "Crear cuenta"}
        </Button>
      </form>
      <p className="mt-4 text-[12px] text-fg-muted text-center">
        ¿Ya tienes cuenta?{" "}
        <Link to="/login" className="text-accent hover:underline">Inicia sesión</Link>
      </p>
    </AuthShell>
  );
}
