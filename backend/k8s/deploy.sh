#!/usr/bin/env bash
# Despliega el stack de Innovatech en el cluster Kubernetes local (Docker Desktop).
# Requiere: Kubernetes habilitado en Docker Desktop e imagenes innovatech/*:dev construidas
# (docker compose build).
set -euo pipefail

# --- Cluster selection -----------------------------------------------------
# To avoid deploying by mistake into whatever cluster the shell currently
# points at (e.g. a remote one via $KUBECONFIG), this script always works
# against an explicit kubeconfig. The path is resolved in this order:
#   1) first argument:           ./deploy.sh /path/to/kubeconfig
#   2) DEPLOY_KUBECONFIG env var
#   3) interactive prompt (only when running in a terminal)
#   4) default ~/.kube/config (Docker Desktop)
KUBECONFIG_PATH="${1:-${DEPLOY_KUBECONFIG:-}}"
if [[ -z "$KUBECONFIG_PATH" ]]; then
  if [[ -t 0 ]]; then
    read -r -p "kubeconfig path [${HOME}/.kube/config]: " KUBECONFIG_PATH
  fi
  KUBECONFIG_PATH="${KUBECONFIG_PATH:-$HOME/.kube/config}"
fi

if [[ ! -f "$KUBECONFIG_PATH" ]]; then
  echo "ABORTING: kubeconfig '$KUBECONFIG_PATH' does not exist." >&2
  echo "Usage: $0 [path-to-kubeconfig]   (or export DEPLOY_KUBECONFIG=...)" >&2
  exit 1
fi

# Pin every kubectl call to THIS kubeconfig; ignores any $KUBECONFIG or active
# context inherited from the shell session.
export KUBECONFIG="$KUBECONFIG_PATH"

# Context guard: only ever deploy into the local Docker Desktop cluster.
# Override with DEPLOY_CONTEXT if your local context has a different name.
EXPECTED_CTX="${DEPLOY_CONTEXT:-docker-desktop}"
CURRENT_CTX="$(kubectl config current-context 2>/dev/null || true)"
if [[ "$CURRENT_CTX" != "$EXPECTED_CTX" ]]; then
  echo "ABORTING: active context is '$CURRENT_CTX', expected '$EXPECTED_CTX'." >&2
  echo "  kubeconfig: $KUBECONFIG_PATH" >&2
  echo "  If your local context is named differently: DEPLOY_CONTEXT=<name> $0 ..." >&2
  echo "  Or switch it: KUBECONFIG='$KUBECONFIG_PATH' kubectl config use-context $EXPECTED_CTX" >&2
  exit 1
fi
echo ">> kubeconfig: $KUBECONFIG_PATH | context: $CURRENT_CTX"
# ---------------------------------------------------------------------------

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
