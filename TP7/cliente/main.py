import requests

BASE_URL = "http://localhost:8000"
TIMEOUT = 5


def _request(method, path, payload=None):
    url = BASE_URL + path
    try:
        response = requests.request(method, url, json=payload, timeout=TIMEOUT)
    except requests.RequestException as exc:
        raise RuntimeError(f"No se pudo conectar con {url}: {exc}") from exc

    try:
        body = response.json()
    except ValueError:
        body = response.text

    if not response.ok:
        detalle = body.get("detail") if isinstance(body, dict) else body
        raise RuntimeError(f"{method} {url} -> {response.status_code}: {detalle}")

    return body


def main():
    print("=== Cliente CLI TP7 - Ventas ===")

    print("\n[GET /health]")
    print(_request("GET", "/health"))

    print("\n[GET /productos]")
    productos = _request("GET", "/productos")
    for producto in productos:
        print(f"  #{producto['id']} {producto['nombre']} | ${producto['precio']} | stock {producto['stock']}")

    print("\n[POST /ventas]")
    venta = _request("POST", "/ventas", {"producto_id": 2, "cantidad": 3})
    print(f"  Venta #{venta['id']} registrada por ${venta['total']}")

    print("\n[GET /ventas]")
    for registrada in _request("GET", "/ventas"):
        print(f"  #{registrada['id']} {registrada['producto_nombre']} x{registrada['cantidad']} = ${registrada['total']}")

    print("\n[GET /ventas/999999]")
    try:
        _request("GET", "/ventas/999999")
    except RuntimeError as exc:
        print(f"  Error controlado: {exc}")

    print("\n[POST /ventas con cantidad invalida]")
    try:
        _request("POST", "/ventas", {"producto_id": 2, "cantidad": 0})
    except RuntimeError as exc:
        print(f"  Error controlado: {exc}")


if __name__ == "__main__":
    main()
