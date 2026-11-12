# Trabajo Practico N° 7: Integración entre Capas y Consumo de APIs / Servicios Distribuidos

**Asignatura:** Desarrollo de Aplicaciones para Ambientes Distribuidos
**Docente:** Lic. Gabriel Artaza
**Modalidad:** Individual
**Formato de evaluacion:** Repositorio + Demostracion / Ejecucion en vivo al cierre del cursado

---

## 1. Portada e Identificacion

Nombre y apellido del estudiante: Yazmin Agustina Dorado

Nombre de la materia: Desarrollo de Aplicaciones para Ambientes Distribuidos

Lenguajes y frameworks utilizados: Python, FastAPI, Pydantic, Uvicorn, Requests

---

## 2. Objetivos del Trabajo Practico

1. Disenar e Implementar un Servicio Distribuido: backend/API con modelo de comunicacion web moderno.
2. Desarrollar una Aplicacion Cliente Independiente: cliente desacoplado del backend que se conecte a traves de la red.
3. Comprender la Abstraccion entre Capas: serializacion, deserializacion y manejo de errores en la capa de transporte.
4. Consolidar Practicas Professionales de Entrega: control de versiones, documentacion tecnica y evidencias.

---

## 3. Marco Tecnologico y Lenguajes Permitidos

Para la solucion del TP7 existe libertad de seleccion tecnologica y de lenguaje.
Opcion REST: Java, JavaScript / TypeScript, Python, C#.
Opcion gRPC: Node.js, Python, Go, Java o C#.

---

## 4. Consignas

Consigna 1: Diseno del Dominio y Contrato del Servicio.
Consigna 2: Implementacion de la Aplicacion Cliente.
Consigna 3: Control de Excepciones y Respuestas de Error.

---

## 5. Arquitectura

Cliente -> Canal de Comunicacion HTTP/1.1 o HTTP/2 -> Servidor de Servicios.

---

## 6. Estructura del Repositorio

/

└── /TP7

    /servidor

    /cliente

    /README.md

---

## 7. Evidencias de Ejecucion

Captura 1 (Invocacion Exitosa): se debe adjuntar la captura de la peticion enviada por el cliente y la respuesta correcta devuelta por el servicio.
Captura 2 (Estructura de Datos): se debe adjuntar la captura de la carga util recibida en formato JSON.
Captura 3 (Manejo de Errores): se debe adjuntar la captura del comportamiento del sistema ante una peticion erronea o un fallo simulado.

---

## 8. Instrucciones de Ejecucion

Pasos breves para levantar el servidor y ejecutar el cliente.


---

## 9. Construccion

Se implemento el TP7 con Python, FastAPI, Pydantic, Uvicorn y Requests.