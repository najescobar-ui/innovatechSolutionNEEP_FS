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

// Minimo 8, con mayuscula, minuscula y al menos un caracter especial.
const PWD_RE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$/;
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

/** Formatea el RUT a medida que se escribe: 16.820.250-4 */
function formatRut(raw: string): string {
  const clean = raw.replace(/[^0-9kK]/g, "").toUpperCase().slice(0, 9);
  if (clean.length <= 1) return clean;
  const body = clean.slice(0, -1);
  const dv = clean.slice(-1);
  return body.replace(/\B(?=(\d{3})+(?!\d))/g, ".") + "-" + dv;
}

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
    // Validacion en el front (UX inmediata); el backend revalida con Jakarta.
    if (!firstName.trim() || !lastName.trim() || !email.trim() || !rut.trim() || !password) {
      setErr("Completa todos los campos.");
      return;
    }
    if (!EMAIL_RE.test(email.trim())) {
      setErr("El email no es válido (ej: nombre@empresa.cl).");
      return;
    }
    if (!PWD_RE.test(password)) {
      setErr("La contraseña debe tener mínimo 8 caracteres, con mayúscula, minúscula y un carácter especial.");
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
      // Muestra el motivo real que devuelve el backend (validacion o Keycloak).
      setErr(e?.response?.data?.message ?? "No se pudo crear la cuenta. Revisa los datos e intenta de nuevo.");
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
          <TextInput value={rut} onChange={(e) => setRut(formatRut(e.target.value))} placeholder="12.345.678-9" inputMode="numeric" />
        </Field>
        <div className="grid grid-cols-2 gap-3">
          <Field label="Contraseña">
            <TextInput type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
          </Field>
          <Field label="Perfil">
            <SelectInput value={role} onChange={(e) => setRole(e.target.value)}>
              {ROLES.map((r) => <option key={r.v} value={r.v}>{r.l}</option>)}
            </SelectInput>
          </Field>
        </div>
        <p className="text-[11px] text-fg-subtle -mt-1 mb-1">
          Mínimo 8 caracteres, con mayúscula, minúscula y un carácter especial (.,-{`{}`}+…).
        </p>
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
