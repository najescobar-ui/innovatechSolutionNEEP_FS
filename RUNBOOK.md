# Runbook — Despliegue en Kubernetes (Docker Desktop)

Guía paso a paso para levantar **todo el stack de Innovatech Solutions** en un computador nuevo,
usando el Kubernetes integrado de **Docker Desktop**. Sirve para **macOS** y **Windows**.

El stack que queda corriendo: Frontend (React), API Gateway, BFF, 3 microservicios, Eureka,
Keycloak y 4 bases PostgreSQL.

---

## 0. Requisitos

- **Docker Desktop** instalado (macOS o Windows).
- **Git** (para clonar el repositorio).
- **Recursos para Docker**: recomendado **6 GB de RAM** o más (son ~16 contenedores).
  En Docker Desktop → *Settings → Resources* podés subir la memoria.
- `kubectl` ya viene incluido con Docker Desktop.

> En este documento, el símbolo `$` indica una línea de comando. No lo copies.

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/najescobar-ui/innovatechSolutionNEEP_FS.git
cd innovatechSolutionNEEP_FS
```

---

## 2. Habilitar Kubernetes en Docker Desktop

Igual en **macOS** y **Windows**:

1. Abrí **Docker Desktop** y esperá a que el motor esté corriendo (icono verde).
2. Entrá a **Settings** (⚙️, arriba a la derecha).
3. Elegí **Kubernetes** en el menú izquierdo.
4. Marcá **Enable Kubernetes** → **Apply & Restart**.
5. Esperá unos minutos. Abajo a la izquierda el indicador de Kubernetes debe quedar **verde** ("Kubernetes running").

Verificá que el clúster responde:

```bash
kubectl get nodes
```

Debe aparecer **un nodo en estado `Ready`**. Anotá su **NAME** (suele ser `desktop-control-plane` o
`docker-desktop`); se usa más abajo.

---

## 3. Construir las imágenes del proyecto

Desde la raíz del repo, con Docker corriendo:

```bash
docker compose build
```

Esto genera las **7 imágenes** `innovatech/<servicio>:dev` (eureka-server, api-gateway, bff,
ms-projects, ms-resources, ms-analytics, frontend). La primera vez tarda varios minutos.

Comprobá que quedaron creadas:

```bash
docker images | grep innovatech
```

---

## 4. Desplegar el stack

### Camino A — macOS / Linux / Git Bash / WSL  (recomendado)

Hay un script que hace todo (carga de imágenes al clúster, ingress, namespace, configmap del realm
y aplicación de los manifiestos):

```bash
./backend/k8s/deploy.sh
```

> En **Windows**, este script (Bash) corre bien desde **Git Bash** o **WSL**. Si solo tenés
> PowerShell, usá el Camino B.

### Camino B — Windows PowerShell (o pasos manuales en cualquier shell)

Ejecutá estos comandos en orden, desde la raíz del repo. Todos son `docker`/`kubectl`, que funcionan
igual en PowerShell, CMD o Bash.

**B.1 — Instalar el Ingress Controller (NGINX):**
```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml
kubectl -n ingress-nginx rollout status deploy/ingress-nginx-controller --timeout=180s
```

**B.2 — Cargar las imágenes al clúster (solo si Docker Desktop usa "kind"):**

El Kubernetes de Docker Desktop reciente corre como **kind**, que tiene su propio almacén de imágenes
y **no ve** las que están en el daemon de Docker. Hay que importárselas. Reemplazá `NODO` por el
nombre del paso 2 (normalmente `desktop-control-plane`):

```bash
docker save postgres:16 | docker exec -i NODO ctr -n k8s.io images import -
docker save quay.io/keycloak/keycloak:24.0 | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/eureka-server:dev | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/api-gateway:dev  | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/bff:dev          | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/ms-projects:dev  | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/ms-resources:dev | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/ms-analytics:dev | docker exec -i NODO ctr -n k8s.io images import -
docker save innovatech/frontend:dev     | docker exec -i NODO ctr -n k8s.io images import -
```

> Si tu Docker Desktop usa el provisioner clásico (`docker-desktop`, kubeadm) en vez de kind, el
> clúster **sí comparte** las imágenes del daemon y este paso B.2 **no es necesario**.

**B.3 — Crear el namespace, el realm de Keycloak y aplicar los manifiestos:**
```bash
kubectl create namespace innovatech --dry-run=client -o yaml | kubectl apply -f -

kubectl -n innovatech create configmap keycloak-realm --from-file=realm-export.json=backend/docker/keycloak/realm-export.json --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -k backend/k8s/overlays/local
```

---

## 5. Esperar a que todo arranque

El stack tarda 1–3 minutos en quedar arriba (las bases inician primero, luego los servicios).

```bash
kubectl -n innovatech get pods
```

Repetí el comando hasta que **los 12 pods** estén en `Running` y `READY 1/1`. Deberías ver:

- `db-projects-0`, `db-resources-0`, `db-analytics-0`, `db-keycloak-0`  (las bases, **StatefulSet**)
- `api-gateway-…`, `bff-…`, `eureka-server-…`, `frontend-…`, `keycloak-…`, `ms-projects-…`,
  `ms-resources-…`, `ms-analytics-…`  (las apps, **Deployment**)

Comprobación rápida del tipo de objeto:
```bash
kubectl -n innovatech get statefulset,deployment
```

---

## 6. Acceder a la aplicación

| Componente | URL | Expuesto por |
|---|---|---|
| **Frontend** | http://innovatech.localhost | Ingress (ingress-nginx) |
| **API Gateway** | http://localhost:9000 | Service LoadBalancer |
| **Keycloak** (admin) | http://localhost:8080 | Service LoadBalancer |

- `*.localhost` resuelve solo a `127.0.0.1` en macOS y Windows modernos, así que
  **http://innovatech.localhost** funciona sin tocar el archivo `hosts`.
- Consola de administración de Keycloak: usuario **`admin`**, contraseña **`admin`**.
- Para usar la app: registrate desde el Frontend (elegí un perfil DEV/PM/DIR) y luego iniciá sesión
  con tu **email** y contraseña.

---

## 7. Problemas comunes

- **Pods en `ImagePullBackOff` o `ErrImagePull`** → faltó cargar las imágenes al clúster (paso B.2).
  Cargalas y reiniciá el deployment afectado: `kubectl -n innovatech rollout restart deploy/<nombre>`.
- **Pods en `Pending`** mucho rato → Docker no tiene RAM suficiente. Subí la memoria en
  *Settings → Resources* y reiniciá Docker Desktop.
- **`http://innovatech.localhost` no carga** → revisá que el Ingress esté listo:
  `kubectl -n ingress-nginx get pods`. Debe estar `Running`.
- **No se puede crear cuenta / login** → el Gateway y Keycloak deben permitir el origen
  `http://innovatech.localhost` (ya viene configurado en este repo: `CORS_ALLOWED_ORIGINS` del
  gateway y los `webOrigins` del cliente `innovatech-frontend` en el realm).
- **Keycloak en CrashLoop** la primera vez → suele resolverse esperando a que `db-keycloak-0`
  termine de iniciar; si persiste, `kubectl -n innovatech delete pod keycloak-…` para reiniciarlo.

---

## 8. Bajar el stack

```bash
kubectl delete -k backend/k8s/overlays/local
kubectl -n innovatech delete configmap keycloak-realm
```

Para borrar también los datos de las bases (los volúmenes):
```bash
kubectl -n innovatech delete pvc --all
```

Y, si querés, apagar Kubernetes desde *Settings → Kubernetes → Enable Kubernetes* (desmarcar).
