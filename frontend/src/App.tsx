import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthProvider";
import { useAuth } from "./auth/useAuth";
import { Layout } from "./components/Layout";
import { Dashboard } from "./pages/Dashboard";
import { Projects } from "./pages/Projects";
import { Resources } from "./pages/Resources";
import { Tasks } from "./pages/Tasks";
import { Analytics } from "./pages/Analytics";

function Loading() {
  return (
    <div className="h-full flex items-center justify-center text-slate-500 text-sm">
      Loading session...
    </div>
  );
}

function AppRoutes() {
  const { ready, authenticated } = useAuth();
  if (!ready) return <Loading />;
  if (!authenticated) return <Loading />; /* login-required ya redirigio, no deberia caer aca */

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/projects" element={<Projects />} />
          <Route path="/resources" element={<Resources />} />
          <Route path="/tasks" element={<Tasks />} />
          <Route path="/analytics" element={<Analytics />} />
          {/* cualquier ruta vieja o futura cae al dashboard mientras tanto */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}
