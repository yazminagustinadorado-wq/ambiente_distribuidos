import requests

BASE_URL = "http://localhost:8000"

def obtener_recursos():
    respuesta = requests.get(BASE_URL + "/recursos")
    if respuesta.status_code == 200:
        return respuesta.json()
    raise Exception("Error al obtener recursos" + str(respuesta.status_code))

def obtener_recurso(id):
    respuesta = requests.get(BASE_URL + "/recursos/" + str(id))
    if respuesta.status_code == 200:
        return respuesta.json()
    if respuesta.status_code == 404:
        raise Exception("Recurso no encontrado")
    raise Exception("Error al obtener recurso" + str(respuesta.status_code))

def crear_recurso(recurso):
    respuesta = requests.post(BASE_URL + "/recursos", json=recurso)
    if respuesta.status_code == 200 or respuesta.status_code == 201:
        return respuesta.json()
    if respuesta.status_code == 400:
        raise Exception("Solicitud invalida")
    raise Exception("Error al crear recurso" + str(respuesta.status_code))

if __name__ == "__main__":
    datos = obtener_recursos()
    print("Recursos:", datos)
    nuevo = {"id": 3, "nombre": "Reservas", "detalle": "Gestion de reservas"}
    print("Crear:", crear_recurso(nuevo))