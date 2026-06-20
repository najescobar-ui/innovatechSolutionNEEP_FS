import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthProvider";
import { useAuth } from "./auth/useAuth";
import { Layout } from "./components/Layout";
import { Dashboard } from "./pages/Dashboard";
import { Projects } from "./pages/Projects";
import { Resources } from "./pages/Resources";
import { Tasks } from "./pages/Tasks";
import { Analytics } from "./pages/Analytics";
import { Login } from "./pages/Login";
import { Register } from "./pages/Register";

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

  return (
    <BrowserRouter>
      <Routes>
        {!authenticated ? (
          <>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="*" element={<Navigate to="/login" replace />} />
          </>
        ) : (
          <Route element={<Layout />}>
            <Route path="/" element={<Dashboard />} />
            <Route path="/projects" element={<Projects />} />
            <Route path="/resources" element={<Resources />} />
            <Route path="/tasks" element={<Tasks />} />
            <Route path="/analytics" element={<Analytics />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        )}
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
