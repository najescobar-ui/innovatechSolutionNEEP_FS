import { BrowserRouter, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthProvider";
import { useAuth } from "./auth/useAuth";
import { Layout } from "./components/Layout";
import { Dashboard } from "./pages/Dashboard";
import { Pendiente } from "./pages/Pendiente";
import { Proyectos } from "./pages/Proyectos";

function Cargando() {
  return (
    <div className="h-full flex items-center justify-center text-slate-500 text-sm">
      Cargando sesion...
    </div>
  );
}

function Rutas() {
  const { ready, authenticated } = useAuth();
  if (!ready) return <Cargando />;
  if (!authenticated) return <Cargando />; // login-required ya redirigio, no deberia caer aca

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/proyectos" element={<Proyectos />} />
          <Route path="/recursos" element={<Pendiente titulo="Recursos" />} />
          <Route path="/analitica" element={<Pendiente titulo="Analitica" />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <Rutas />
    </AuthProvider>
  );
}
