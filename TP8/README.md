# Trabajo Práctico N° 8 - Bases de Datos Distribuidas y Conexión entre Capas

**Alumno:** Agustina Dorado<br>
**Docente:** Lic. Gabriel Artaza<br>
**Modalidad:** Individual<br>
**Unidad temática:** Unidad 4 - Bases de Datos Distribuidas y Conexión entre Capas

> La consigna indica entregar dentro de `/TP7` por un error de copia. Este trabajo se entrega en `/TP8`, que corresponde al número de práctico.

---

## 1. Objetivo

Diseñar, implementar y evaluar un esquema de persistencia distribuida con arquitectura de **replicación Copia Primaria / Respaldo-Primario**, y conectar esa capa con un backend mediante un **driver** (`psycopg`) en lugar de un ORM.

Se evalúan tres comportamientos:

1. **Separación de cargas**: escrituras al nodo primario, lecturas al nodo secundario.
2. **Propagación de datos**: una inserción en el primario aparece en la réplica.
3. **Tolerancia a fallas**: caída del primario, detección de la excepción, promoción manual de la réplica y reorientación del backend.

---

## 2. Arquitectura

```text
                    ┌──────────────────────────┐
DBeaver :5432  ────▶│ db-primary   :5432       │
FastAPI POST   ────▶│ Primario                 │
                    │ lectura + escritura      │
                    │ wal_level=replica        │
                    └────────────┬─────────────┘
                                 │ WAL / streaming replication
                                 ▼
                    ┌──────────────────────────┐
DBeaver :5433  ◀────│ db-replica   :5433       │◀──── FastAPI GET
                    │ hot_standby              │
                    │ solo lectura             │
                    └──────────────────────────┘

FastAPI :8001  ── POST /eventos ──▶ :5432 primario
FastAPI :8001  ── GET  /eventos ──▶ :5433 réplica
```

La base se llama `tp8` en ambos nodos. En DBeaver se muestra con **dos conexiones distintas**: `TP8-Primario` y `TP8-Réplica`. Así se ve el mismo dato "de un lado al otro".

---

## 3. Estructura del repositorio

```text
TP8/
├── docker-compose.yml
├── README.md
├── scripts/
│   ├── init-primary.sh
│   ├── init.sql
│   └── entrypoint-replica.sh
└── servidor/
    ├── db.py
    ├── main.py
    └── requirements.txt
```

---

## 4. Infraestructura de base de datos distribuida

`docker-compose.yml` define dos contenedores PostgreSQL 16:

| Servicio | Contenedor | Puerto host | Rol | Características |
|---|---|---:|---|---|
| `db-primary` | `tp8-db-primary` | `5432` | Primario | Acepta lectura/escritura, `wal_level=replica`, `max_wal_senders=10`, healthcheck con `pg_isready` |
| `db-replica` | `tp8-db-replica` | `5433` | Secundario | Clonado con `pg_basebackup -R`, `hot_standby=on`, solo lectura |

Volúmenes: `tp8_pg_primary` y `tp8_pg_replica`, para que los datos sobrevivan reinicios.

Scripts:

| Archivo | Función |
|---|---|
| `scripts/init-primary.sh` | Crea el rol interno `replicator` y habilita `host replication` en `pg_hba.conf` |
| `scripts/init.sql` | Crea la tabla `eventos` y dos registros semilla |
| `scripts/entrypoint-replica.sh` | Clona el primario con `pg_basebackup` antes de que arranque el entrypoint oficial de PostgreSQL |

Tabla `eventos`:

```sql
CREATE TABLE eventos (
    id BIGSERIAL PRIMARY KEY,
    clave TEXT NOT NULL UNIQUE,
    valor TEXT NOT NULL,
    creado_en TIMESTAMPTZ NOT NULL DEFAULT now()
);
```

---

## 5. Backend y conexión entre capas

El backend es FastAPI y usa `psycopg[binary]==3.2.3`. La capa de acceso está en `servidor/db.py`, con dos URLs:

| Variable | Valor por defecto | Uso |
|---|---|---|
| `POSTGRES_WRITE_URL` | `postgresql://tp8:tp8@localhost:5432/tp8` | Escrituras |
| `POSTGRES_READ_URL` | `postgresql://tp8:tp8@localhost:5433/tp8` | Lecturas |
| `POSTGRES_PROMOTED_URL` | `postgresql://tp8:tp8@localhost:5433/tp8` | Nodo promovido tras failover |

Endpoints:

| Método | Ruta | Comportamiento |
|---|---|---|
| `GET` | `/` | Información general del servicio |
| `GET` | `/health` | Modo failover y nodos activos |
| `POST` | `/eventos` | Inserta en el primario; `201`, `409` si clave duplicada, `503` si el primario cayó |
| `GET` | `/eventos` | Lee de la réplica; responde con `leido_desde`, `total` y lista |
| `GET` | `/eventos/{id}` | Lee un evento desde la réplica; `404` si no existe |
| `GET` | `/debug/nodos` | Muestra disponibilidad, `pg_is_in_recovery()` y cantidad de filas por nodo |
| `POST` | `/debug/failover/activar` | Reorienta lecturas y escrituras al nodo promovido en `:5433` |
| `POST` | `/debug/failover/resetear` | Vuelve al estado normal: escribir en `:5432`, leer en `:5433` |

Documentación interactiva: `http://localhost:8001/docs`.

---

## 6. Ver la base de datos con DBeaver

DBeaver no está incluido en el repositorio. El instalador está en:

```text
C:\Users\Estudiante\Downloads\dbeaver-ce-26.2.5.2-windows-x86_64.exe
```

Ya quedó instalado en modo usuario en:

```text
C:\Users\Estudiante\AppData\Local\DBeaver\dbeaver.exe
```

Versión verificada: `26.2.5.2`. Para reinstalarlo en silencio:

```powershell
& "C:\Users\Estudiante\Downloads\dbeaver-ce-26.2.5.2-windows-x86_64.exe" /S /currentuser
```

Si DBeaver pide descargar el driver PostgreSQL al abrir la conexión, aceptar.

### 6.1 Conexiones

Las dos conexiones PostgreSQL ya quedan preconfiguradas en el workspace de DBeaver:

| Nombre de conexión | Host | Puerto | Base | Usuario | Password |
|---|---|---:|---|---|---|
| `TP8-Primario` | `localhost` | `5432` | `tp8` | `tp8` | `tp8` |
| `TP8-Réplica` | `localhost` | `5433` | `tp8` | `tp8` | `tp8` |

Están definidas en:

```text
C:\Users\Estudiante\AppData\Roaming\DBeaverData\workspace6\General\.dbeaver\data-sources.json
```

Si se reinstala DBeaver o se elige otro workspace, crearlas con los datos de la tabla. En `TP8-Réplica`, marcar la conexión como **solo lectura** para no intentar escrituras accidentales. Ambas muestran la base `tp8`; se distinguen por el nombre de la conexión y el puerto.

### 6.2 Ver la propagación

En la conexión `TP8-Primario`, abrir un editor SQL y ejecutar:

```sql
SELECT pg_is_in_recovery(), count(*) FROM eventos;

INSERT INTO eventos (clave, valor)
VALUES ('demo-dbeaver', 'escrito en el primario');

SELECT id, clave, valor, creado_en FROM eventos ORDER BY id DESC LIMIT 5;
```

En la conexión `TP8-Réplica`, presionar **F5** para refrescar y ejecutar:

```sql
SELECT pg_is_in_recovery(), count(*) FROM eventos;

SELECT id, clave, valor, creado_en FROM eventos ORDER BY id DESC LIMIT 5;
```

Resultado esperado:

- Primario: `pg_is_in_recovery()` = `false`.
- Réplica: `pg_is_in_recovery()` = `true`.
- El registro `demo-dbeaver` aparece en las dos conexiones.

También se puede insertar desde el backend y refrescar DBeaver:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/eventos" -ContentType "application/json" -Body '{"clave":"demo-backend","valor":"visible en dbeaver"}'
```

---

## 7. Pruebas de propagación, consistencia y tolerancia a fallas

### 7.1 Propagación

Ejecutar:

```powershell
Invoke-RestMethod -Uri "http://localhost:8001/eventos"
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/eventos" -ContentType "application/json" -Body '{"clave":"demo-propagacion","valor":"llega a la replica"}'
Invoke-RestMethod -Uri "http://localhost:8001/eventos"
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

Salida esperada: el `GET /eventos` responde `"leido_desde":":5433 replica"` y el `count(*)` de la réplica aumenta. La consistencia es eventual: la propagación ocurre vía WAL y puede demorar unos milisegundos.

### 7.2 Caída del primario y failover manual

```powershell
docker compose stop db-primary
```

Comprobar que la escritura falla y la lectura sigue funcionando:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/eventos" -ContentType "application/json" -Body '{"clave":"durante-caida","valor":"debe fallar"}'
Invoke-RestMethod -Uri "http://localhost:8001/eventos"
```

Resultado esperado: `POST` responde `503` con `Nodo de escritura no disponible`; `GET` responde `200` desde la réplica.

Promover la réplica. El contenedor corre como usuario `postgres`, por eso se usa `-u postgres`:

```powershell
docker exec -u postgres tp8-db-replica pg_ctl promote -D /var/lib/postgresql/data -w
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery();"
```

Resultado esperado: `pg_is_in_recovery()` pasa de `t` a `f`.

> Si trabajás desde **Git Bash** en la terminal de Visual Studio Code, no uses el bloque anterior tal cual: Git Bash puede convertir `/var/lib/postgresql/data` en una ruta de Windows y aparece el error `directory does not exist`. En ese caso usá el bloque de la sección **10.8.6**, que incluye `MSYS_NO_PATHCONV=1`.

Reorientar el backend:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/debug/failover/activar"
Invoke-RestMethod -Uri "http://localhost:8001/health"
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/eventos" -ContentType "application/json" -Body '{"clave":"escritura-post-promocion","valor":"backend reorientado"}'
Invoke-RestMethod -Uri "http://localhost:8001/eventos"
Invoke-RestMethod -Uri "http://localhost:8001/debug/nodos"
```

Resultado esperado: `leido_desde` y `nodo_escritura` pasan a `:5433 promovida`, y el nuevo `POST` responde `201`.

En DBeaver, la conexión `TP8-Primario` queda en error, mientras `TP8-Réplica` sigue mostrando los datos y `pg_is_in_recovery()` devuelve `false`.

### 7.3 Restaurar el escenario inicial

Después de la prueba de failover, volver al clúster limpio:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/debug/failover/resetear"
docker compose down
docker volume rm -f tp8_pg_primary tp8_pg_replica
docker compose up -d
```

---

## 8. Evidencias de ejecución

Las capturas deben guardarse en `TP8/evidencias/`:

| Archivo | Contenido |
|---|---|
| `01-cluster.png` | DBeaver con `TP8-Primario` y `TP8-Réplica` + salida de `docker compose ps` |
| `02-propagacion.png` | `POST /eventos` exitoso en el primario y el mismo registro en la réplica al refrescar DBeaver |
| `03-failover.png` | `docker compose stop db-primary`, `POST` con `503`, `pg_ctl promote` y `pg_is_in_recovery()` en `false` |

---

## 9. Control de excepciones

| Situación | Respuesta |
|---|---|
| Payload inválido, por ejemplo `clave` vacía | `400 Bad Request`, `detail: "Payload invalido"` |
| `GET /eventos/{id}` inexistente | `404 Not Found`, `detail: "Evento no encontrado"` |
| Clave duplicada en `POST /eventos` | `409 Conflict`, `detail: "La clave ya existe"` |
| Primario caído antes de promover | `503 Service Unavailable`, `detail: "Nodo de escritura no disponible..."` |
| Error no controlado | `500 Internal Server Error`, `detail: "Error interno del servidor"` |

El backend registra la excepción no controlada por consola con `[ERROR] MÉTODO /ruta: mensaje`.

---

## 10. Instrucciones de ejecución paso a paso

### 10.1 Requisitos

- Docker Desktop con Compose v5 o superior.
- Python 3.13.
- DBeaver Community (opcional, para visualizar la base).

Verificar:

```powershell
docker --version
docker compose version
python --version
```

### 10.2 Abrir el proyecto en VS Code

1. Abrir VS Code.
2. `Archivo > Abrir carpeta...` y elegir `C:\Users\Estudiante\ambiente_distribuidos\TP8`.
3. Abrir la terminal integrada con **Ctrl +** la tecla `` ` ``, o `Terminal > Nueva terminal`.
4. Elegir PowerShell.

### 10.3 Instalar dependencias del backend

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8\servidor
python -m pip install -r requirements.txt
```

### 10.4 Levantar el clúster

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8
docker compose up -d
docker compose ps
```

Esperar a que `db-primary` figure como `healthy` y a que `db-replica` esté `Up`.

Verificación rápida:

```powershell
docker exec tp8-db-primary psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

### 10.5 Levantar el backend

En otra terminal de PowerShell:

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8\servidor
python -m uvicorn main:app --reload --host 127.0.0.1 --port 8001
```

Dejar esa terminal abierta. Abrir `http://localhost:8001/docs` para ver la API.

### 10.6 Abrir DBeaver

Instalar DBeaver desde el `.exe` indicado en la sección 6 y abrir las dos conexiones preconfiguradas de la tabla de la sección 6.1.

### 10.7 Detener todo

```powershell
docker compose stop
```

Detener el backend con **Ctrl+C** en su terminal.

### 10.8 Trabajar desde Git Bash en Visual Studio Code

Esta es la secuencia detallada y numerada para levantar, cargar datos, caer el primario y volver, usando **Git Bash** en la terminal integrada de Visual Studio Code.

Git Bash no usa rutas `C:\...`, sino rutas `/c/...`. Si tu terminal muestra `$`, estás en Git Bash; si muestra `PS>`, usá las secciones anteriores en PowerShell.

#### Resumen numerado de los pasos

1. **Docker + carpeta**: abrir Docker Desktop desde Windows. En **Terminal 1 (Git Bash)** pararse en `/c/Users/Estudiante/ambiente_distribuidos/TP8` y ejecutar `docker compose up -d` + `docker compose ps`.
2. **Backend + carpeta**: en **Terminal 2 (Git Bash)** pararse en `/c/Users/Estudiante/ambiente_distribuidos/TP8/servidor` y ejecutar `python -m uvicorn main:app --reload --host 127.0.0.1 --port 8001`. Dejarla abierta.
3. **Cualquier carpeta**: verificar `health`, cargar datos con `curl -X POST http://127.0.0.1:8001/eventos` y comprobar en la réplica con `docker exec tp8-db-replica psql ...`.
4. **Terminal 1**: parar el primario con `docker compose stop db-primary`.
5. **Cualquier carpeta**: verificar `HTTP 503` al escribir y `200` al leer desde `:5433 replica`.
6. **Terminal 1**: promover la réplica con `MSYS_NO_PATHCONV=1 docker exec -u postgres tp8-db-replica pg_ctl promote -D /var/lib/postgresql/data -w`.
7. **Cualquier carpeta**: activar failover, verificar `health` y volver a escribir con `curl -X POST http://127.0.0.1:8001/eventos`.
8. **Terminal 1**: volver a levantar el primario o restaurar el escenario limpio con `resetear`, `down`, `volume rm` y `up -d`.
9. **DBeaver**: reconectar si hace falta, refrescar con **F5** y ejecutar el `SELECT`.
10. **Apagar**: **Ctrl+C** en la Terminal 2 y `docker compose stop` en la Terminal 1.

> Para los `curl`, la carpeta no importa: podés estar en `~`. Para `docker compose`, pararse en `/c/Users/Estudiante/ambiente_distribuidos/TP8`. Para el backend, pararse en `/c/Users/Estudiante/ambiente_distribuidos/TP8/servidor`.

#### 10.8.1 Paso 1 — Terminal 1: levantar Docker y las dos bases

```bash
cd /c/Users/Estudiante/ambiente_distribuidos/TP8
docker compose up -d
docker compose ps
```

Esperado: `tp8-db-primary` con `Up (healthy)` y `tp8-db-replica` con `Up`.

Si Docker Desktop está cerrado, abrirlo desde Windows antes de ejecutar `docker compose`.

Verificación de nodos:

```bash
docker exec tp8-db-primary psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

Esperado en un clúster limpio:

```text
f|2
t|2
```

#### 10.8.2 Paso 2 — Terminal 2: levantar el backend

Abrir otra terminal Git Bash con el botón `+` y dejarla abierta:

```bash
cd /c/Users/Estudiante/ambiente_distribuidos/TP8/servidor
python -m uvicorn main:app --reload --host 127.0.0.1 --port 8001
```

Si es la primera vez:

```bash
python -m pip install -r requirements.txt
```

Verificación:

```bash
curl -s http://127.0.0.1:8001/health | python -m json.tool
```

Esperado: `"modo_failover": false`.

#### 10.8.3 Paso 3 — Cargar datos desde el backend con Git Bash

No hace falta estar en una carpeta especial para este `curl`; se puede ejecutar desde `~` o desde cualquier directorio.

```bash
CLAVE="demo-gitbash-$(date +%s)"

curl -s -X POST http://127.0.0.1:8001/eventos \
  -H "Content-Type: application/json" \
  -d "{\"clave\":\"$CLAVE\",\"valor\":\"cargado desde Git Bash\"}" | python -m json.tool
```

La clave con `$(date +%s)` evita el error `409 La clave ya existe` al repetir la prueba.

Verificar:

```bash
curl -s http://127.0.0.1:8001/eventos | python -m json.tool
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

Esperado: `"leido_desde": ":5433 replica"` y el `count(*)` aumentado.

#### 10.8.4 Paso 4 — Parar solo el primario

Desde la Terminal 1:

```bash
docker compose stop db-primary
```

Esto simula la caída del nodo de escritura. **No borra datos** y **no apaga la réplica**.

No usar `docker compose down` para esta prueba: `down` apaga y elimina ambos contenedores.

#### 10.8.5 Paso 5 — Verificar `503` y que la lectura sigue

```bash
curl -s -X POST http://127.0.0.1:8001/eventos \
  -H "Content-Type: application/json" \
  -d '{"clave":"durante-caida","valor":"debe fallar"}' \
  -w "\nHTTP %{http_code}\n"

curl -s http://127.0.0.1:8001/eventos | python -m json.tool
```

Esperado: `POST` con `HTTP 503` y `GET` con `"leido_desde": ":5433 replica"`.

#### 10.8.6 Paso 6 — Promover la réplica desde Git Bash

En Git Bash, `/var/lib/postgresql/data` puede ser convertido por MSYS en una ruta de Windows. Por eso se usa `MSYS_NO_PATHCONV=1`:

```bash
MSYS_NO_PATHCONV=1 docker exec -u postgres tp8-db-replica pg_ctl promote -D /var/lib/postgresql/data -w
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery();"
```

Esperado: `server promoted` y luego `f`.

Si aparece `directory does not exist`, significa que faltó `MSYS_NO_PATHCONV=1` o que se ejecutó desde PowerShell con una ruta incorrecta.

#### 10.8.7 Paso 7 — Reorientar el backend y volver a escribir

```bash
curl -s -X POST http://127.0.0.1:8001/debug/failover/activar | python -m json.tool
curl -s http://127.0.0.1:8001/health | python -m json.tool
curl -s http://127.0.0.1:8001/debug/nodos | python -m json.tool

curl -s -X POST http://127.0.0.1:8001/eventos \
  -H "Content-Type: application/json" \
  -d '{"clave":"post-failover-1","valor":"escritura en la promovida"}' | python -m json.tool

curl -s http://127.0.0.1:8001/eventos | python -m json.tool
```

Esperado: `modo_failover: true`, `nodo_escritura` y `nodo_lectura` en `:5433 promovida`, `POST` con `201` y `"leido_desde": ":5433 promovida"`.

En DBeaver, `TP8-Primario` queda en error y `TP8-Réplica` sigue funcionando.

#### 10.8.8 Paso 8 — Volver a levantar el primario

Hay dos casos distintos:

**Caso A: el primario solo se paró, pero NO se promovió la réplica**

```bash
docker compose start db-primary
docker compose ps
docker exec tp8-db-primary psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

Esperado: primario `Up`, `f|N` y réplica `t|N`. La replicación se reanuda normalmente.

**Caso B: ya se promovió la réplica y se activó el failover**

`docker compose start db-primary` solo enciende el viejo primario, pero **no lo convierte automáticamente en réplica**. Para volver al escenario limpio:

```bash
curl -s -X POST http://127.0.0.1:8001/debug/failover/resetear | python -m json.tool
docker compose down
docker volume rm -f tp8_pg_primary tp8_pg_replica
docker compose up -d
```

Esto borra los datos de prueba y recrea los volúmenes con los dos registros semilla.

#### 10.8.9 Paso 9 — Reconectar DBeaver desde Git Bash

Si DBeaver está cerrado o las conexiones muestran una X:

```bash
"/c/Users/Estudiante/AppData/Local/DBeaver/dbeaver.exe" \
  -con "id=tp8-primario|create=false|connect=true|openConsole=true" \
  -con "id=tp8-replica|create=false|connect=true|openConsole=true"
```

Si ya está abierto, alcanza con presionar **F5** sobre cada conexión.

#### 10.8.10 Paso 10 — Detener todo desde Git Bash

```bash
docker compose stop
```

Detener el backend con **Ctrl+C** en la Terminal 2.

---

## 11. Comandos para levantar el TP8 (resumen)

Los bloques de esta sección están en PowerShell. Si trabajás desde **Git Bash** en la terminal de Visual Studio Code, usar la sección **10.8**, que tiene las rutas `/c/...`, el `curl` con JSON y el promote con `MSYS_NO_PATHCONV=1`.

Ruta base en PowerShell: `C:\Users\Estudiante\ambiente_distribuidos\TP8`.
Ruta base en Git Bash: `/c/Users/Estudiante/ambiente_distribuidos/TP8`.

### 11.1 Dependencias del backend

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8\servidor
python -m pip install -r requirements.txt
```

### 11.2 Clúster PostgreSQL

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8
docker compose up -d
docker compose ps
```

### 11.3 Verificación de nodos

```powershell
docker exec tp8-db-primary psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
docker exec tp8-db-replica psql -U tp8 -d tp8 -tAc "select pg_is_in_recovery(), count(*) from eventos;"
```

### 11.4 Backend FastAPI

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8\servidor
python -m uvicorn main:app --reload --host 127.0.0.1 --port 8001
```

Abrir `http://localhost:8001/`.

### 11.5 Probar escritura y lectura

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/eventos" -ContentType "application/json" -Body '{"clave":"demo-rapido","valor":"escritura en primario"}'
Invoke-RestMethod -Uri "http://localhost:8001/eventos"
Invoke-RestMethod -Uri "http://localhost:8001/debug/nodos"
```

### 11.6 Failover manual

```powershell
docker compose stop db-primary
docker exec -u postgres tp8-db-replica pg_ctl promote -D /var/lib/postgresql/data -w
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/debug/failover/activar"
Invoke-RestMethod -Uri "http://localhost:8001/health"
```

### 11.7 Volver al estado inicial

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8001/debug/failover/resetear"
docker compose down
docker volume rm -f tp8_pg_primary tp8_pg_replica
docker compose up -d
```

### 11.8 Atajo para levantar clúster y backend

```powershell
cd C:\Users\Estudiante\ambiente_distribuidos\TP8; docker compose up -d; Start-Process python -ArgumentList '-m','uvicorn','main:app','--reload','--host','127.0.0.1','--port','8001' -WorkingDirectory 'C:\Users\Estudiante\ambiente_distribuidos\TP8\servidor'; Start-Sleep 5; start http://localhost:8001/docs
```

capturas : <img width="819" height="557" alt="captp8" src="https://github.com/user-attachments/assets/c1e3e873-37ba-45db-a1ac-d1e88ffa6189" />

<img width="819" height="460" alt="tp8_2" src="https://github.com/user-attachments/assets/bdf8dabf-9f2e-48c7-9bd3-57e9a27b99f2" />
<img width="819" height="460" alt="captp8_3" src="https://github.com/user-attachments/assets/5345e9be-d4b2-43d9-a44e-712e0ca683aa" />
<img width="819" height="460" alt="tp8_4" src="https://github.com/user-attachments/assets/0a644a00-1254-451e-a11a-549d9906aafd" />

<img width="819" height="549" alt="p8_5" src="https://github.com/user-attachments/assets/ddf34c75-51a8-4fbb-9fd6-298078f1137c" />


