# ControlExpedientes

Guía rápida para levantar el proyecto completo desde cero.

## Requisitos

- Git
- Docker y Docker Compose
- Java 21
- Node.js 22+ y npm

## Clonar el repositorio

```bash
git clone https://github.com/MoisesNEY/ControlExpedientes.git
cd ControlExpedientes
```

## Configuración inicial

1. Copia el archivo de ejemplo del backend:

```bash
cp backend/.env.example backend/.env
```

2. Si deseas cambiar credenciales, edita `backend/.env`.

## Perfil `dev` (frontend y backend locales)

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

### 3. Ejecutar frontend local

En otra terminal:

```bash
cd frontend
npm ci
npm run dev
```

El frontend quedará en `http://localhost:5173`.

## Perfil `prod` (frontend y backend dockerizados)

### Opción recomendada: ejecutar desde `backend/`

> Reemplaza `/path/to/jdk-21` por la ruta real de tu instalación de Java 21.

```bash
cd backend
cp .env.example .env
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw -q -ntp -Pprod -DskipTests -Dmodernizer.skip=true package
docker compose --env-file .env -f docker-compose.prod.yml up --build
```

### Opción alternativa: ejecutar desde la raíz del repositorio

```bash
export JAVA_HOME=/path/to/jdk-21
export PATH="$JAVA_HOME/bin:$PATH"
cd backend
./mvnw -q -ntp -Pprod -DskipTests -Dmodernizer.skip=true package
cd ..
docker compose --env-file backend/.env -f backend/docker-compose.prod.yml up --build
```

### URLs del entorno `prod`

- Frontend: `http://localhost:4173`
- Backend: `http://localhost:8080`
- Keycloak: `http://localhost:9080`
- PostgreSQL: `localhost:5434`

### Detener el entorno `prod`

```bash
cd backend
docker compose --env-file .env -f docker-compose.prod.yml down
```

Para eliminar también los volúmenes:

```bash
docker compose --env-file .env -f docker-compose.prod.yml down -v
```

## Por qué `POSTGRES_PASSWORD` no se estaba leyendo

`docker compose` solo toma `.env` automáticamente desde el directorio de trabajo actual. Si ejecutas el comando desde la raíz usando `-f backend/docker-compose.prod.yml`, el archivo `backend/.env` no se carga solo. Por eso ahora:

- existe `backend/.env.example`,
- la documentación usa `--env-file`,
- y el compose define valores por defecto para evitar que el stack falle si falta alguna variable.
