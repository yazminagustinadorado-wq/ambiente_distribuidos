# Trabajo Practico N° 9: Seguridad en Aplicaciones Distribuidas (JWT y TLS/SSL)

**Asignatura:** Desarrollo de Aplicaciones para Ambientes Distribuidos
**Docente:** Lic. Gabriel Artaza
**Modalidad:** Individual / Equipos de Trabajo
**Unidad Temática:** Unidad 4 - Seguridad en Aplicaciones Distribuidas

## 1. Objetivo General

Diseñar, implementar y verificar una capa de seguridad end-to-end en una aplicación distribuida. Integrar cifrado en tránsito (TLS/SSL) para garantizar la privacidad de los datos en red y un esquema de autenticación y autorización sin estado mediante JSON Web Tokens (JWT) para proteger los endpoints y servicios.

## 2. Tecnologías y Lenguajes de Programación

- Backend: Python, FastAPI, Pydantic, Uvicorn, PyJWT.
- Certificados: TLS/SSL local auto firmado (OpenSSL / mkcert).
- Cliente: Python, Requests.

## 3. Consignas y Pasos a Desarrollar

1. Configuración del Canal Seguro (TLS/SSL en Tránsito): generación de certificado SSL/TLS local y configuración del servidor para responder únicamente por HTTPS.
2. Servicio de Autenticación y Emisión de Tokens (JWT): endpoint `/login` que recibe credenciales y responde con un token JWT firmado digitalmente; payload con claims (sub, rol, exp).
3. Middleware de Autorización: validación del header `Authorization: Bearer`, control de acceso con respuestas 401 y 403.
4. Pruebas de Tampering (Alteración de Payload): alteración del payload sin re-firmar y verificación del rechazo.

## 4. Arquitectura

Cliente -> Canal HTTPS (TLS/SSL) -> Servidor -> JWT -> Autorización

## 5. Estructura del Repositorio

```
/TP9
+-- /servidor
¦   +-- main.py
¦   +-- requirements.txt
+-- /cliente
¦   +-- main.py
+-- README.md
```

## 6. Instrucciones de Ejecución

```bash
pip install -r servidor/requirements.txt
uvicorn main:app --host 127.0.0.1 --port 8443 --ssl-certfile cert.pem --ssl-keyfile key.pem
python cliente/main.py
```

## 7. Criterios de Evaluación y Demostración en Vivo

- Evaluación en repositorio: modularidad del código, manejo de excepciones de red, cifrado del canal y cumplimiento de las pruebas exigidas.
- Demostración funcional en vivo durante la finalización del cursado.
