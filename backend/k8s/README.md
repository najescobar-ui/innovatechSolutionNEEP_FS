# Kubernetes — Innovatech Solutions

Despliegue del stack completo en un cluster de Kubernetes (probado en el Kubernetes
integrado de **Docker Desktop**). Manifiestos organizados con **Kustomize**
(`base/` + `overlays/local/`).

## Requisitos
1. **Docker Desktop con Kubernetes habilitado**: Settings → Kubernetes → *Enable Kubernetes* → Apply.
2. **Imágenes locales construidas** (el cluster de Docker Desktop comparte el daemon):
   ```bash
   docker compose build        # genera innovatech/*:dev
   ```

## Desplegar
```bash
./backend/k8s/deploy.sh
```
o manualmente:
```bash
kubectl create namespace innovatech --dry-run=client -o yaml | kubectl apply -f -
kubectl -n innovatech create configmap keycloak-realm \
  --from-file=realm-export.json=backend/docker/keycloak/realm-export.json \
  --dry-run=client -o yaml | kubectl apply -f -
kubectl apply -k backend/k8s/overlays/local
```

## Estado
```bash
kubectl -n innovatech get pods,svc
kubectl -n innovatech rollout status deploy/api-gateway
```

## Acceso
| Componente | URL | Expuesto por |
|------------|-----|--------------|
| Frontend | http://innovatech.localhost | **Ingress** (ingress-nginx) |
| API Gateway | http://localhost:9000 | Service LoadBalancer |
| Keycloak | http://localhost:8080 | Service LoadBalancer |

> `*.localhost` resuelve a `127.0.0.1` y el ingress-nginx se publica en `localhost:80`, por eso el front queda en `http://innovatech.localhost`. El bundle del navegador sigue llamando al gateway (`:9000`) y a Keycloak (`:8080`).

> **Nota (kind):** el Kubernetes de Docker Desktop corre como kind y tiene su propio containerd — **no** ve las imágenes del daemon Docker. Por eso `deploy.sh` carga las imágenes locales al cluster (`docker save … | ctr images import`) antes de aplicar.

## Arquitectura desplegada
- **Database per Service**: 4 PostgreSQL (projects, resources, analytics, keycloak) con su PVC.
- **Keycloak** (realm importado desde ConfigMap), **Eureka**, **API Gateway**, **BFF**, **ms-projects/resources/analytics**, **Frontend**.
- Config no sensible en `ConfigMap innovatech-config`; credenciales en `Secret innovatech-secrets`.
- Los `Service` usan los mismos nombres que los hostnames del `docker-compose`, así la config de las apps no cambia entre Compose y K8s.

## Borrar
```bash
kubectl delete -k backend/k8s/overlays/local
kubectl -n innovatech delete configmap keycloak-realm
```
