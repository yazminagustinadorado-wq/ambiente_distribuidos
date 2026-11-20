import base64
import json

import requests

BASE_URL = "https://localhost:8443"
USERNAME = "admin"
PASSWORD = "admin123"
CERT_PATH = r"C:\Users\yazmi\ambiente_distribuidos\TP9\servidor\ca.pem"


def login() -> str:
    respuesta = requests.post(
        f"{BASE_URL}/login",
        json={"username": USERNAME, "password": PASSWORD},
        verify=CERT_PATH,
    )
    respuesta.raise_for_status()
    return respuesta.json()["access_token"]


def obtener_recursos(token: str):
    headers = {"Authorization": f"Bearer {token}"}
    return requests.get(f"{BASE_URL}/recursos", headers=headers, verify=CERT_PATH)


def crear_token_tampered(token: str) -> str:
    header = token.split(".")[0]
    payload_b64 = token.split(".")[1]
    signature = token.split(".")[2]
    padding = "=" * (-len(payload_b64) % 4)
    payload = json.loads(base64.urlsafe_b64decode(payload_b64 + padding))
    payload["rol"] = "profesor"
    nuevo_payload = base64.urlsafe_b64encode(json.dumps(payload).encode()).decode().rstrip("=")
    return f"{header}.{nuevo_payload}.{signature}"


def main() -> None:
    token = login()
    respuesta = obtener_recursos(token)
    print("GET /recursos con token:", respuesta.status_code, respuesta.json())

    sin_token = requests.get(f"{BASE_URL}/recursos", verify=CERT_PATH)
    print("GET /recursos sin token:", sin_token.status_code)

    token_tampered = crear_token_tampered(token)
    respuesta_tampered = obtener_recursos(token_tampered)
    print("GET /recursos con token manipulado:", respuesta_tampered.status_code, respuesta_tampered.json())


if __name__ == "__main__":
    main()
