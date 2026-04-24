# Frontend ControlExpedientes

## Desarrollo local

```bash
cd frontend
npm ci
npm run dev
```

La aplicación queda disponible en `http://localhost:5173`.

En desarrollo, Vite redirige `/api` al backend local en `http://localhost:8080`.

## Build local

```bash
cd frontend
npm ci
npm run build
```

## Docker

El frontend cuenta con un `Dockerfile` productivo basado en Nginx.

Cuando se usa junto con `backend/docker-compose.prod.yml`:

- sirve la aplicación en `http://localhost:4173`,
- redirige `/api` al contenedor `backend`,
- y redirige `/ws` al backend para WebSocket.

Para levantar ese stack desde la raíz del repositorio usa:

```bash
docker compose --env-file backend/.env -f backend/docker-compose.prod.yml up --build
```
