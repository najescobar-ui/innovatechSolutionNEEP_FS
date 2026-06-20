#!/usr/bin/env bash
# Despliega el stack de Innovatech en el cluster Kubernetes local (Docker Desktop).
# Requiere: Kubernetes habilitado en Docker Desktop e imagenes innovatech/*:dev construidas
# (docker compose build).
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
REALM="$ROOT/backend/docker/keycloak/realm-export.json"
NODE="desktop-control-plane"   # nodo del cluster kind de Docker Desktop

# El cluster (kind) tiene su propio containerd: NO ve el daemon de Docker.
# Cargamos las imagenes locales al cluster (postgres/keycloak se pullean si faltan).
echo ">> Cargando imagenes al cluster ($NODE)"
for img in postgres:16 quay.io/keycloak/keycloak:24.0 \
  innovatech/eureka-server:dev innovatech/api-gateway:dev innovatech/bff:dev \
  innovatech/ms-projects:dev innovatech/ms-resources:dev innovatech/ms-analytics:dev \
  innovatech/frontend:dev; do
  if docker image inspect "$img" >/dev/null 2>&1; then
    docker save "$img" | docker exec -i "$NODE" ctr -n k8s.io images import - >/dev/null 2>&1 \
      && echo "   cargada $img" || echo "   (no se pudo cargar $img)"
  fi
done

echo ">> ingress-nginx"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.11.3/deploy/static/provider/cloud/deploy.yaml
kubectl -n ingress-nginx rollout status deploy/ingress-nginx-controller --timeout=180s || true

echo ">> Namespace innovatech"
kubectl create namespace innovatech --dry-run=client -o yaml | kubectl apply -f -

echo ">> ConfigMap del realm de Keycloak"
kubectl -n innovatech create configmap keycloak-realm \
  --from-file=realm-export.json="$REALM" \
  --dry-run=client -o yaml | kubectl apply -f -

echo ">> Aplicando manifiestos (kustomize overlay local)"
kubectl apply -k "$ROOT/backend/k8s/overlays/local"

echo ">> Estado:"
kubectl -n innovatech get pods,svc,ingress
echo ">> Acceso: front http://innovatech.localhost | gateway http://localhost:9000 | keycloak http://localhost:8080"
