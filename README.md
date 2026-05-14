# ControlExpedientes

Guía rápida para levantar el proyecto completo desde cero.

## Requisitos

### Para ejecutar todo con Docker

- Git
- Docker y Docker Compose

### Para desarrollo local

- Java 21
- Node.js 22+ y npm

## Clonar el repositorio

```bash
git clone https://github.com/MoisesNEY/ControlExpedientes.git
cd ControlExpedientes
```

## Configuración inicial

El stack productivo usa el archivo `backend/.env`, que ya debe existir con las credenciales del proyecto.

## Perfil `prod` (stack completo con Docker)

Desde la raíz del repositorio ejecuta:

```bash
docker-compose up --build
```

Ese comando construye y levanta:

- frontend en `http://localhost:5173`
- backend en `http://localhost:8080`
- Keycloak en `http://localhost:9080`
- PostgreSQL como contenedor interno del stack

Para detener el entorno:

```bash
docker-compose down
```

Para eliminar también los volúmenes persistidos:

```bash
docker-compose down -v
```

## Perfil `dev` (backend local)

### 1. Levantar dependencias del backend

En una terminal:

```bash
cd backend
docker compose -f src/main/docker/postgresql.yml up -d
docker compose -f src/main/docker/keycloak.yml up -d
```

### 2. Ejecutar backend local

En otra terminal:

> Reemplaza `/path/to/jdk-21` por la ruta real de tu instalación de Java 21.

```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
cd backend
./mvnw
```

El backend quedará en `http://localhost:8080`.

### 3. Ejecutar frontend local opcional

Si deseas trabajar también el frontend fuera de Docker:

```bash
cd frontend
npm ci
npm run dev
```

El frontend quedará en `http://localhost:5173`.
