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

<img width="1335" height="627" alt="Captura de pantalla 2026-11-19 220306" src="https://github.com/user-attachments/assets/8fcb1588-ccca-4a3c-8566-f79f6b985c3c" />
<img width="1237" height="256" alt="Captura de pantalla 2026-11-19 223931" src="https://github.com/user-attachments/assets/eac2f649-75d5-4089-90be-7d1786d6bd29" />
<img width="609" height="328" alt="Captura de pantalla 2026-11-19 225015" src="https://github.com/user-attachments/assets/33c79c86-a665-4da4-8539-3aecf3b8ac62" />
<img width="1281" height="421" alt="Captura de pantalla 2026-11-19 225705" src="https://github.com/user-attachments/assets/cae3aaa0-81c7-4b15-bbb6-046b0ca494ac" />

<img width="1291" height="296" alt="Captura de pantalla 2026-11-19 230301" src="https://github.com/user-attachments/assets/b518ce82-c1fd-44ad-8ffe-96e696dd42be" />
<img width="910" height="175" alt="Captura de pantalla 2026-11-19 231629" src="https://github.com/user-attachments/assets/df1e9aef-2714-449b-b41f-e418affcb784" />


