from datetime import datetime
from typing import List

from fastapi import FastAPI, HTTPException, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from psycopg import OperationalError
from psycopg.errors import UniqueViolation
from pydantic import BaseModel, Field

from db import execute, fetchall, fetchone, read_url, state, write_url

app = FastAPI(
    title="TP8 - Bases de Datos Distribuidas",
    version="1.0.0",
)


class EventoCreate(BaseModel):
    clave: str = Field(min_length=1, max_length=80)
    valor: str = Field(min_length=1)


class Evento(BaseModel):
    id: int
    clave: str
    valor: str
    creado_en: datetime


class EventoResponse(BaseModel):
    leido_desde: str
    total: int
    eventos: List[Evento]


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=400,
        content={
            "detail": "Payload invalido",
            "errors": jsonable_encoder(exc.errors()),
        },
    )


@app.exception_handler(Exception)
async def unhandled_exception_handler(request: Request, exc: Exception):
    print(f"[ERROR] {request.method} {request.url.path}: {exc}")
    return JSONResponse(
        status_code=500,
        content={"detail": "Error interno del servidor"},
    )


@app.get("/")
def raiz():
    return {
        "tp": 8,
        "dominio": "eventos",
        "docs": "/docs",
        "eventos": "/eventos",
        "nodos": "/debug/nodos",
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "modo_failover": state["failover"],
        "nodo_escritura": ":5433 promovida" if state["failover"] else ":5432 primario",
        "nodo_lectura": ":5433 promovida" if state["failover"] else ":5433 replica",
    }


@app.post("/eventos", status_code=201, response_model=Evento)
def crear_evento(payload: EventoCreate):
    try:
        return execute(
            write_url(),
            "INSERT INTO eventos (clave, valor) VALUES (%s, %s) "
            "RETURNING id, clave, valor, creado_en",
            (payload.clave, payload.valor),
        )
    except UniqueViolation as exc:
        raise HTTPException(status_code=409, detail="La clave ya existe") from exc
    except OperationalError as exc:
        raise HTTPException(
            status_code=503,
            detail="Nodo de escritura no disponible. Promover la replica y activar /debug/failover/activar",
        ) from exc


@app.get("/eventos", response_model=EventoResponse)
def listar_eventos():
    try:
        rows = fetchall(
            read_url(),
            "SELECT id, clave, valor, creado_en FROM eventos ORDER BY id DESC LIMIT 50",
        )
        total = fetchone(read_url(), "SELECT count(*) AS valor FROM eventos")["valor"]
    except OperationalError as exc:
        raise HTTPException(status_code=503, detail="Nodo de lectura no disponible") from exc
    return {
        "leido_desde": ":5433 promovida" if state["failover"] else ":5433 replica",
        "total": total,
        "eventos": rows,
    }


@app.get("/eventos/{evento_id}", response_model=Evento)
def obtener_evento(evento_id: int):
    row = fetchone(
        read_url(),
        "SELECT id, clave, valor, creado_en FROM eventos WHERE id = %s",
        (evento_id,),
    )
    if row is None:
        raise HTTPException(status_code=404, detail="Evento no encontrado")
    return row


def node_info(url, nombre):
    info = {
        "nodo": nombre,
        "url": url,
        "escritura_activa": url == write_url(),
        "lectura_activa": url == read_url(),
    }
    try:
        info["disponible"] = True
        info["en_recuperacion"] = fetchone(url, "SELECT pg_is_in_recovery() AS valor")["valor"]
        info["total_eventos"] = fetchone(url, "SELECT count(*) AS valor FROM eventos")["valor"]
    except OperationalError:
        info["disponible"] = False
    return info


@app.get("/debug/nodos")
def debug_nodos():
    from db import PROMOTED_URL, READ_URL, WRITE_URL

    return {
        "primario": node_info(WRITE_URL, "primario :5432"),
        "replica": node_info(READ_URL, "replica :5433"),
        "promovida": node_info(PROMOTED_URL, "promovida :5433"),
    }


@app.post("/debug/failover/activar")
def activar_failover():
    try:
        fetchone(read_url(), "SELECT 1 AS valor")
    except OperationalError as exc:
        raise HTTPException(
            status_code=503,
            detail="La replica promovida no responde en :5433",
        ) from exc
    state["failover"] = True
    return {"failover": True, "escritura": write_url(), "lectura": read_url()}


@app.post("/debug/failover/resetear")
def resetear_failover():
    state["failover"] = False
    return {"failover": False, "escritura": write_url(), "lectura": read_url()}
