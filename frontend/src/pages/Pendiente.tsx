import { Card, CardBody } from "../components/Card";

// Placeholder para las pantallas que vienen en siguientes etapas.
export function Pendiente({ titulo }: { titulo: string }) {
  return (
    <div className="space-y-6 max-w-3xl">
      <header>
        <h1 className="text-2xl font-semibold text-slate-900">{titulo}</h1>
        <p className="text-sm text-slate-500 mt-1">Esta vista se implementa en la proxima etapa.</p>
      </header>
      <Card>
        <CardBody>
          <p className="text-sm text-slate-600">Aun no hay nada que mostrar aca.</p>
        </CardBody>
      </Card>
    </div>
  );
}
