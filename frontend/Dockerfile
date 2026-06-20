# ---- build ----
FROM node:20-alpine AS build
WORKDIR /build

# deps primero para aprovechar cache
COPY package.json package-lock.json ./
RUN npm ci

# fuente
COPY tsconfig.json tsconfig.node.json vite.config.ts tailwind.config.ts postcss.config.js index.html ./
COPY src ./src
COPY public ./public

# Vite inyecta las VITE_* en build-time. Los defaults apuntan al host
# (localhost) porque el bundle corre en el navegador, no dentro de la red Docker.
ARG VITE_API_BASE=http://localhost:9000/api
ARG VITE_KEYCLOAK_URL=http://localhost:8080
ARG VITE_KEYCLOAK_REALM=innovatech
ARG VITE_KEYCLOAK_CLIENT_ID=innovatech-frontend
ENV VITE_API_BASE=${VITE_API_BASE} \
    VITE_KEYCLOAK_URL=${VITE_KEYCLOAK_URL} \
    VITE_KEYCLOAK_REALM=${VITE_KEYCLOAK_REALM} \
    VITE_KEYCLOAK_CLIENT_ID=${VITE_KEYCLOAK_CLIENT_ID}

RUN npm run build

# ---- runtime ----
FROM nginxinc/nginx-unprivileged:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html
EXPOSE 8080
