from datetime import datetime, timezone
from pathlib import Path
from typing import List

from fastapi import FastAPI, HTTPException, Request
from fastapi.encoders import jsonable_encoder
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel, Field

app = FastAPI(
    title="TP7 - Servicio de Ventas",
    version="2.0.0",
)

CLIENTE_DIR = Path(__file__).resolve().parents[1] / "cliente"


class Recurso(BaseModel):
    id: int
    nombre: str
    detalle: str


class Producto(BaseModel):
    id: int = Field(gt=0)
    nombre: str = Field(min_length=1)
    precio: float = Field(gt=0)
    stock: int = Field(ge=0)


class VentaCreate(BaseModel):
    producto_id: int = Field(gt=0)
    cantidad: int = Field(gt=0)


class Venta(BaseModel):
    id: int
    producto_id: int
    producto_nombre: str
    cantidad: int
    total: float
    fecha: str


RECURSOS_DB: List[dict] = [
    {"id": 1, "nombre": "Inventario", "detalle": "Gestion de inventario"},
    {"id": 2, "nombre": "Usuarios", "detalle": "Gestion de usuarios"},
]

PRODUCTOS_DB: List[dict] = [
    {"id": 1, "nombre": "Notebook Pro 14", "precio": 1250.0, "stock": 12},
    {"id": 2, "nombre": "Mouse Inalambrico", "precio": 35.5, "stock": 40},
    {"id": 3, "nombre": "Teclado Mecanico", "precio": 89.9, "stock": 15},
    {"id": 4, "nombre": "Monitor 27'' 4K", "precio": 480.0, "stock": 3},
]

VENTAS_DB: List[dict] = []


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


def _next_id(items: List[dict]) -> int:
    return max((item["id"] for item in items), default=0) + 1


def _buscar_producto(producto_id: int) -> dict | None:
    return next((p for p in PRODUCTOS_DB if p["id"] == producto_id), None)


@app.get("/health")
def health():
    return {
        "status": "ok",
        "productos": len(PRODUCTOS_DB),
        "ventas": len(VENTAS_DB),
    }


@app.get("/productos")
def listar_productos():
    return PRODUCTOS_DB


@app.post("/productos", status_code=201)
def crear_producto(producto: Producto):
    if _buscar_producto(producto.id):
        raise HTTPException(status_code=409, detail="Ya existe un producto con ese id")

    datos = producto.model_dump()
    PRODUCTOS_DB.append(datos)
    return datos


@app.get("/productos/{producto_id}")
def obtener_producto(producto_id: int):
    producto = _buscar_producto(producto_id)
    if not producto:
        raise HTTPException(status_code=404, detail="Producto no encontrado")
    return producto


@app.get("/ventas")
def listar_ventas():
    return VENTAS_DB


@app.post("/ventas", status_code=201)
def crear_venta(venta: VentaCreate):
    producto = _buscar_producto(venta.producto_id)
    if not producto:
        raise HTTPException(status_code=404, detail="Producto no encontrado")

    if venta.cantidad > producto["stock"]:
        raise HTTPException(
            status_code=409,
            detail=f"Stock insuficiente. Disponible: {producto['stock']}",
        )

    registrada = {
        "id": _next_id(VENTAS_DB),
        "producto_id": venta.producto_id,
        "producto_nombre": producto["nombre"],
        "cantidad": venta.cantidad,
        "total": round(producto["precio"] * venta.cantidad, 2),
        "fecha": datetime.now(timezone.utc).isoformat(),
    }

    producto["stock"] -= venta.cantidad
    VENTAS_DB.append(registrada)
    return registrada


@app.get("/ventas/{venta_id}")
def obtener_venta(venta_id: int):
    venta = next((v for v in VENTAS_DB if v["id"] == venta_id), None)
    if not venta:
        raise HTTPException(status_code=404, detail="Venta no encontrada")
    return venta


@app.get("/recursos")
def listar_recursos():
    return RECURSOS_DB


@app.get("/recursos/{id}")
def obtener_recurso(id: int):
    recurso = next((r for r in RECURSOS_DB if r["id"] == id), None)
    if not recurso:
        raise HTTPException(status_code=404, detail="Recurso no encontrado")
    return recurso


@app.post("/recursos", status_code=201)
def crear_recurso(recurso: Recurso):
    if recurso.id < 0:
        raise HTTPException(status_code=400, detail="El id del recurso debe ser valido")
    if any(r["id"] == recurso.id for r in RECURSOS_DB):
        raise HTTPException(status_code=409, detail="Ya existe un recurso con ese id")

    datos = recurso.model_dump()
    RECURSOS_DB.append(datos)
    return {"mensaje": "Recurso creado", "recurso": datos}


@app.get("/debug/error500", include_in_schema=False)
def provocar_error_500():
    raise RuntimeError("Error 500 simulado para la demostracion del TP7")


app.mount("/", StaticFiles(directory=CLIENTE_DIR, html=True), name="cliente")
