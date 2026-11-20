import datetime
from typing import List, Optional

import jwt
import uvicorn
from fastapi import Depends, FastAPI, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel

app = FastAPI(
    title="TP9 - Seguridad en Aplicaciones Distribuidas",
    version="1.0.0",
)

SECRET_KEY = "tp9_jwt_secret_key_cambia_en_produccion"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRES_MINUTES = 30
SSL_HOST = "127.0.0.1"
SSL_PORT = 8443
SSL_CERTFILE = "cert.pem"
SSL_KEYFILE = "key.pem"

USERS = {
    "admin": {"password": "admin123", "rol": "admin"},
    "profesor": {"password": "profesor123", "rol": "profesor"},
}

RECURSOS = [
    {"id": 1, "nombre": "Inventario", "detalle": "Gestion de inventario"},
    {"id": 2, "nombre": "Usuarios", "detalle": "Gestion de usuarios"},
]


class LoginRequest(BaseModel):
    username: str
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int


class Recurso(BaseModel):
    id: int
    nombre: str
    detalle: str


bearer_scheme = HTTPBearer(auto_error=False)


def create_token(username: str, rol: str) -> str:
    now = datetime.datetime.utcnow()
    payload = {
        "sub": username,
        "rol": rol,
        "iat": now,
        "exp": now + datetime.timedelta(minutes=ACCESS_TOKEN_EXPIRES_MINUTES),
    }
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token expirado")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token invalido o manipulado")


def get_current_user(credentials: Optional[HTTPAuthorizationCredentials] = Depends(bearer_scheme)) -> dict:
    if credentials is None or credentials.scheme.lower() != "bearer":
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Bearer token requerido")
    return decode_token(credentials.credentials)


@app.post("/login", response_model=TokenResponse)
def login(payload: LoginRequest) -> TokenResponse:
    user = USERS.get(payload.username)
    if user is None or user.get("password") != payload.password:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Credenciales invalidas")
    token = create_token(payload.username, user["rol"])
    return TokenResponse(access_token=token, expires_in=ACCESS_TOKEN_EXPIRES_MINUTES)


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/recursos", response_model=List[Recurso])
def listar_recursos(user: dict = Depends(get_current_user)) -> List[Recurso]:
    if user.get("rol") != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="El rol del usuario no tiene permisos para esta operacion")
    return [Recurso(**recurso) for recurso in RECURSOS]


if __name__ == "__main__":
    uvicorn.run(app, host=SSL_HOST, port=SSL_PORT, ssl_certfile=SSL_CERTFILE, ssl_keyfile=SSL_KEYFILE)
