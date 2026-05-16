# Frontend — InnovaTech

Aplicación React 18.3 + Vite + TypeScript empaquetada con el estándar NPM. Tema dark inspirado en Linear/Vercel/Supabase. Se autentica contra Keycloak por OAuth2 (PKCE) y consume el backend a través del API Gateway.

## Stack

- **React** 18.3 + **TypeScript** 5.x
- **Vite** 5 (build) / **Nginx Alpine** (serve en contenedor)
- **Tailwind CSS** 3.4 (con CSS variables propias para tema)
- **Recharts** 2.13 (charts del dashboard)
- **keycloak-js** 24.0.5 (login PKCE)
- **axios** 1.7 (cliente HTTP con interceptor de refresh token)
- **react-router-dom** 6.26
- **lucide-react** (icons)

## Estructura

```
frontend/
├── public/
│   └── logoLuffy.png          # logo de la marca
├── src/
│   ├── api/                   # cliente axios con interceptor de token
│   ├── auth/                  # provider Keycloak + hook useAuth
│   ├── components/            # Card, Badge, Button, Sidebar, Topbar,
│   │                          # Modal, Dropdown, Checkbox, Layout
│   ├── pages/                 # Dashboard, Proyectos, Recursos
│   ├── App.tsx                # routing
│   ├── main.tsx               # entry point
│   ├── index.css              # tema + reset (CSS variables RGB)
│   └── vite-env.d.ts          # tipos de import.meta.env
├── Dockerfile                 # multi-stage Node build → Nginx
├── nginx.conf                 # SPA history mode + caching
├── package.json
├── tailwind.config.ts
├── vite.config.ts
├── tsconfig.json
└── tsconfig.node.json
```

## Variables de entorno

Vite inyecta variables con prefijo `VITE_*` en build-time. **Importante**: el bundle corre en el navegador del host, por eso las URLs son `localhost` (no nombres internos de la red Docker).

| Variable | Default | Descripción |
|----------|---------|-------------|
| `VITE_API_BASE` | `http://localhost:9000/api` | Endpoint del API Gateway |
| `VITE_KEYCLOAK_URL` | `http://localhost:8080` | Servidor Keycloak |
| `VITE_KEYCLOAK_REALM` | `innovatech` | Realm a usar |
| `VITE_KEYCLOAK_CLIENT_ID` | `innovatech-frontend` | Client ID del realm |

Copia `.env.example` a `.env.local` para overrides locales (no se versiona).

## Ejecutar y probar

### En desarrollo (hot reload)

```bash
cd frontend
npm install
npm run dev
```

La app queda en `http://localhost:3000` con HMR de Vite. Requiere que Keycloak (`:8080`) y el API Gateway (`:9000`) estén arriba — lo más simple es levantar el resto del stack con Docker Compose y solo el frontend en dev.

### En contenedor (build estático servido por Nginx)

Desde la raíz del repo:

```bash
docker compose up -d --build frontend
```

Queda en `http://localhost:3000`. El bundle ya quedó construido con las `VITE_*` por defecto. Para overrides:

```bash
VITE_API_BASE=http://192.168.1.10:9000/api docker compose up -d --build frontend
```

(rebuild obligatorio: las vars son build-time, no runtime).

### Scripts disponibles

| Script | Acción |
|--------|--------|
| `npm run dev` | Servidor Vite con HMR en :3000 |
| `npm run build` | Type-check (`tsc -b`) + bundle de producción en `dist/` |
| `npm run preview` | Sirve el `dist/` localmente para verificar |

## Vistas implementadas

| Ruta | Vista | Roles |
|------|-------|-------|
| `/` | Dashboard adaptado por rol (PM / DEV / DIR) | PM, DEV, DIR |
| `/proyectos` | Tabla densa con CRUD parcial | DIR puede crear/editar/borrar |
| `/recursos` | Tabla densa con CRUD parcial | DIR puede crear/editar/borrar |
| `/analitica` | Oculta (próxima iteración) | — |

## Patrones aplicados en el código

- **Provider pattern** (`AuthProvider`) para inyectar sesión Keycloak.
- **Factory Method en backend** que devuelve el DTO de dashboard por rol — el frontend solo renderiza la variante correcta.
- **Compound components** en `Modal` (`Modal` + `Field` + `TextInput` + `SelectInput` + `TextArea` + `ConfirmDialog`).
- **Render prop** en `Dropdown` (`trigger` y `children` reciben funciones).
- **Optimistic UI / refetch tras mutación** en Proyectos y Recursos.

## Notas

- Los inputs `type="date"` usan validación nativa del navegador. El filtro de fechas del Dashboard valida en cliente que el rango no supere 30 días antes de pegarle al backend (que también valida y responde 400).
- Las opacidades de Tailwind (`bg-success/10`, `text-info/60`) funcionan porque las CSS variables están en formato RGB triplete y se exponen con `rgb(var(--x) / <alpha-value>)`.
