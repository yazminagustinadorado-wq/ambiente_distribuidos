# Trabajo Practico N° 6: Middleware y Objetos Remotos (RMI / RPC)

**Asignatura:** Desarrollo de Aplicaciones para Ambientes Distribuidos  
**Unidad:** 3 - Middleware y Objetos Remotos (RMI / RPC)  
**Tema:** Invocacion Remota de Metodos y Abstraccion de la Comunicacion  
**Modalidad:** Individual / Parejas  

---

## 1. Descripcion del Proyecto

Este trabajo practico implementa un sistema distribuido en Java basado en Java RMI (Remote Method Invocation) que delega tareas computacionalas, validaciones de integridad y filtrado de contenido a un servidor remoto. El objetivo principal es comprender el funcionamiento del middleware en la abstraccion de la comunicacion de red, aplicando el patron Stub / Skeleton, la transparencia de localizacion y la gestion de excepciones remotas.

### Componentes principales:
* ServicioDistribuido: Interfaz remota que declara los metodos computacionales expuestos al cliente.
* ServicioDistribuidoImpl: Implementacion remota con la logica de negocio.
* ServidorRMI: Inicializa el registro de nombres y publica el servicio.
* ClienteRMI: Obtiene la referencia al objeto remoto y expone un menu interactivo.

---

## 2. Consigna Practica - Ejercicio 1: Servidor de Procesamiento Remoto Distribuido

### 2.1. Definicion de la Interfaz Remota

Se debe crear una interfaz remota que declare al menos tres metodos computacionales:

1. Procesamiento Matematico: un metodo que reciba una lista o arreglo de numeros y devuelva estadisticas (promedio, maximo, minimo y desviacion estandar).
2. Validacion de Criterios: un metodo de verificacion de integridad de datos (ej. validar si un formato CUIT o DNI o una cadena de datos es valido).
3. Filtro de Contenido: un metodo que reciba un arreglo de cadenas de texto y devuelva solo aquellas que coincidan con un patron de busqueda.

### 2.2. Implementacion y Publicacion

1. Implementar la interfaz remota asegurando la separacion clara de la logica de negocio.
2. Inicializar el registro de nombres (RMI Registry) y publicar el servicio bajo un identificador accesible.
3. Incluir trazabilidad en consola informando cuando un cliente se conecta y que operaciones se ejecutan.

### 2.3. Invocacion Transparente

1. Crear un cliente por consola que obtenga la referencia al objeto remoto mediante el registro de nombres.
2. Presentar un menu interactivo para que el usuario ingrese datos y ejecute las funciones del servidor de manera transparente.

---

## 3. Ejercicio 2: Cuestionario Teorico y Analisis de Arquitectura

### 3.1. Abstraccion del Middleware: Sockets Puros vs. Java RMI o gRPC

Con sockets puros, el desarrollador es responsable de la apertura y cierre de la conexion TCP, la definicion e interpretacion del protocolo a nivel de aplicacion, la serializacion y deserializacion manual de mensajes y el manejo de concurrencia mediante hilos.

Con Java RMI o gRPC, el programador invoca metodos remotos exactamente como si fueran llamadas locales, ocultando por completo la complejidad de la infraestructura de red subyacente.

Las tareas complejas que abstrae el middleware:
- Gestion de red: creacion, mantenimiento y cierre transparente de conexiones TCP.
- Marshalling y unmarshalling: conversion automatica de argumentos y retornos a secuencias de bytes para su transmision.
- Servicio de nombres (Naming / Registry): vinculacion y resolucion de objetos remotos mediante identificadores logicos.
- Gestion de concurrencia: administracion multihilo de solicitudes entrantes en el servidor.
- Propagacion de excepciones: captura de errores en el servidor y relezado transparente en la JVM del cliente.

### 3.2. Ciclo de Vida y Patron Stub / Skeleton

- Stub (Proxy en el cliente): objeto representante del servicio remoto dentro de la JVM del cliente. Intercepta la invocacion, realiza el marshalling, transmite la peticion por la red, espera la respuesta, realiza el unmarshalling y devuelve el resultado al cliente.
- Skeleton / Dispatcher (en el servidor): recibe las peticiones binarias entrantes, deserializa, identifica el metodo correspondiente, ejecuta la llamada sobre el objeto de negocio real y realiza el marshalling de la respuesta o excepcion para enviarla de regreso al stub.

### 3.3. Manejo de Fallos Parciales

Si el servidor se interrumpe abruptamente durante la ejecucion de un metodo, el cliente queda bloqueado en la invocacion sincrona hasta que expira el timeout o se detecta la ruptura del socket TCP.

Excepciones especificas:
- java.rmi.RemoteException: excepcion base de RMI ante fallos de comunicacion.
- java.rmi.ConnectException: se lanza cuando no es posible conectar con la direccion o puerto del servidor.
- java.rmi.UnmarshalException: ocurre si la conexion se interrumpe a mitad de la lectura de datos.
- java.rmi.NoSuchObjectException: se lanza cuando el objeto remoto no se encuentra en el registro o fue desregistrado.

En gRPC: StatusRuntimeException con estados UNAVAILABLE o DEADLINE_EXCEEDED.

Gestion en entornos productivos:
- Reintentos con backoff exponencial para operaciones idempotentes.
- Circuit breaker: suspender temporalmente los reintentos hacia un nodo caido.
- Configuracion de timeouts: limites explicitos de respuesta y conexion a nivel de socket.
- Mecanismos de fallback: respuestas alternativas desde cache o valores por defecto seguros ante la indisponibilidad del servicio.


