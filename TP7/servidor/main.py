from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(
    title="TP7 - Servicio de APIs",
    version="1.0.0",
)

class Recurso(BaseModel):
    id: int
    nombre: str
    detalle: str

# Lista simulada de recursos en el servidor
RECURSOS_DB = [
    {"id": 1, "nombre": "Inventario", "detalle": "Gestion de inventario"},
    {"id": 2, "nombre": "Usuarios", "detalle": "Gestion de usuarios"},
]

@app.get("/recursos")
def listar_recursos():
    return RECURSOS_DB

@app.get("/recursos/{id}")
def obtener_recurso(id: int):
    # Buscamos el recurso por su ID
    recurso = next((r for r in RECURSOS_DB if r["id"] == id), None)
    
    if not recurso:
        # Lanza un HTTP 404 Not Found si no se encuentra el recurso
        raise HTTPException(status_code=404, detail="Recurso no encontrado")
        
    return recurso

@app.post("/recursos")
def crear_recurso(recurso: Recurso):
    if recurso.id < 0:
        raise HTTPException(status_code=400, detail="El id del recurso debe ser valido")
    
    # Agregamos el recurso a la lista simulada
    RECURSOS_DB.append(recurso.dict())
    return {"mensaje": "Recurso creado", "recurso": recurso}