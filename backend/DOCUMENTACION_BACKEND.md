# Sistema de Control de Expedientes Clínicos - Backend

Bienvenido a la documentación técnica oficial del backend de **ControlExpedientes**. Este sistema ha sido diseñado como una solución robusta, segura y altamente escalable para la gestión de información clínica, utilizando estándares modernos de desarrollo empresarial.

---

## Stack Tecnológico

El núcleo del sistema está construido sobre las siguientes tecnologías:

- **Lenguaje**: Java 21
- **Framework Principal**: Spring Boot 3.3.x (Ecosistema JHipster)
- **Capa de Datos**: Spring Data JPA / Hibernate
- **Base de Datos**: PostgreSQL 
- **Gestión de Esquemas**: Liquibase (Migraciones controladas)
- **Seguridad**: Spring Security + OAuth2 / OpenID Connect (Keycloak por defecto)
- **Caché**: Ehcache / JCache (Optimización de búsquedas y stock)
- **Auditoría**: Spring AOP (Aspectos para seguimiento clínico)
- **Gestión de Dependencias**: Maven

---

## Arquitectura y Flujo de Datos

El backend sigue una arquitectura de capas bien definida:

1.  **Capa de Dominio (Entities)**: Modelado JPA de la base de datos con relaciones optimizadas (`Patient`, `ExpedienteClinico`, `ConsultaMedica`, etc.).
2.  **Capa de Transferencia (DTOs)**: Uso estricto de DTOs para evitar el leak de entidades y aplicar seguridad a nivel de campo (e.g., `PacientePublicDTO` vs `PacienteDTO`).
3.  **Capa de Servicios (Core Logic)**: Orquestación de lógica de negocio, transacciones atómicas y validaciones complejas.
4.  **Capa de Repositorios**: Interacción con DB utilizando JPA Repositories con optimización `@EntityGraph`.
5.  **Capa Web (REST Controllers)**: Endpoints documentados y protegidos por roles.

---

## Seguridad y Roles de Acceso

El sistema implementa un modelo de **Control de Acceso Basado en Roles (RBAC)**:

| Role | Permisos Principales | Visibilidad de Datos |
| :--- | :--- | :--- |
| `ROLE_ADMIN` | Gestión total del sistema y usuarios. | Full Access |
| `ROLE_MEDICO` | Registro de consultas, diagnósticos, recetas y vista de timeline. | Full Access Clínico |
| `ROLE_ENFERMERO` | Toma de signos vitales, gestión básica de pacientes. | Datos Clínicos Básicos |
| `ROLE_RECEPCION` | Registro de pacientes y búsqueda administrativa. | **Solo Datos Públicos** (PacientePublicDTO) |

> [!IMPORTANT]
> La privacidad se garantiza mediante el mapeo selectivo de DTOs según el contexto de seguridad del usuario autenticado.

---

## Lógica de Negocio Crítica

### 1. Gestión Automática de Expedientes
Al registrar un nuevo paciente, el sistema genera automáticamente un **Expediente Clínico** único.
- **Numeración**: Utiliza un patrón secuencial `EXP-{YEAR}-{ID}` para garantizar unicidad y orden cronológico.

### 2. El "Acto Clínico" (Atención Médica)
Se ha implementado un servicio maestro (`AtencionMedicaService`) que orquestra el flujo vital del médico en una sola operación atómica:
- Guarda la **Consulta Médica**.
- Vincula **Signos Vitales**.
- Registra **Diagnósticos** (CIE-10).
- Genera **Recetas** y valida automáticamente el **Stock de Farmacia**.

### 3. Gestión de Inventario Inteligente
El backend protege la disponibilidad de medicamentos:
- **Validación**: Impide recetas si el stock es insuficiente (`InsufficientStockException`).
- **Deducción**: El stock se resta en tiempo real al guardar una receta.
- **Alertas**: Endpoint dedicado para detectar medicamentos próximos a agotarse.

### 4. Auditoría Clínica (AOP)
Para garantizar la integridad y legalidad de la información, se utiliza **Programación Orientada a Aspectos (AOP)** para auditar acciones críticas (modificación de diagnósticos/tratamientos) sin ensuciar la lógica de negocio, guardando quién, cuándo y qué se modificó.

---

## Optimizaciones de Rendimiento

Para manejar grandes volúmenes de datos clínicos, el sistema incluye:

- **Predictive Search Cache**: Las búsquedas de códigos **CIE-10** están cacheadas (`@Cacheable`), reduciendo el tiempo de respuesta de 100ms+ a <5ms.
- **Consistency Management**: El caché de stock se invalida automáticamente (`@CacheEvict`) solo cuando hay movimientos reales de inventario.
- **EntityGraph (N+1 Fix)**: Las consultas complejas (como el Timeline Histórico o el detalle de Paciente) se cargan en una sola consulta SQL optimizada mediante `Left Join Fetch`, evitando el cuello de botella de múltiples peticiones a la DB.

---

## Endpoints Estratégicos

| Ruta | Método | Descripción | Rol Mínimo |
| :--- | :--- | :--- | :--- |
| `/api/atencion-medica/finalizar-consulta` | `POST` | Guarda consulta, signos, diagnósticos y recetas en un solo bloque. | `MEDICO` |
| `/api/expediente-clinicos/{id}/timeline` | `GET` | Retorna la historia clínica completa agrupada cronológicamente. | `MEDICO` |
| `/api/diagnosticos/search?query=...` | `GET` | Búsqueda predictiva CIE-10 optimizada con caché. | `MEDICO` |
| `/api/medicamentos/low-stock?threshold=10` | `GET` | Listado de medicamentos con inventario bajo. | `ADMIN` / `ENFERMERO` |
| `/api/pacientes/{id}/public` | `GET` | Vista segura de paciente para personal no médico. | `RECEPCION` |

---

## Guía de Ejecución

### Requisitos
- Java 21
- Docker (para base de datos y Keycloak).
- Maven 3.8+.

### Pasos para Desarrollo
1.  **Levantar Infraestructura**:
    ```bash
    docker compose -f src/main/docker/services.yml up -d
    docker compose -f src/main/docker/keycloak.yml up -d
    ```
2.  **Ejecutar Backend**:
    ```bash
    ./mvnw
    ```
    O para mayor velocidad sin tests:
    ```bash
    ./mvnw -DskipTests
    ```
