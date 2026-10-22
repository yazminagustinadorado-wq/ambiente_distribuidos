# Trabajo Práctico N° 4: Representación de Datos y Paso de Mensajes

**Alumno/a:** Yazmin Agustina Dorado  
**Docente:** Lic. Gabriel Artaza  

---

## 1. Objetivo del Trabajo Práctico
Comprender e implementar el empaquetado, serialización/deserialización (`marshalling`/`unmarshalling`) y transmisión de estructuras de datos complejas entre procesos a través de un canal de red Socket TCP.

---

## 2. Descripción de la Solución
El proyecto fue desarrollado en **Java** y consta de los siguientes módulos:

* **`Transaccion.java`**: Define la estructura de la transacción con campos `id` (int), `origen` (String), `monto` (double) y `timestamp` (long).
* **`ParserMensajes.java`**: Implementa la serialización y deserialización en dos formatos:
  * **Texto (JSON):** Convierte el objeto a una cadena JSON estandarizada.
  * **Binario Directo:** Empaqueta los tipos primitivos en un arreglo de bytes utilizando `DataOutputStream`.
* **`ServidorTCP.java`**: Servidor Socket TCP que escucha peticiones en el puerto 5000 y realiza el `unmarshalling` del objeto recibido.
* **`ClienteTCP.java`**: Cliente Socket TCP que transmite una ráfaga de 1000 transacciones simuladas (primero en JSON y luego en Binario) y evalúa el desempeño midiendo el tamaño en bytes y tiempo transcurrido.

---

## 3. Tabla Comparativa de Resultados (Overhead)

Prueba realizada con una ráfaga de **1000 transacciones**:

| Formato de Intercambio | Carga Útil Transmitida (Bytes) | Tiempo Total (ms) |
| :--- | :--- | :--- |
| **JSON (Texto)** | ~69,112 bytes | ~1,500 ms |
| **Binario Directo** | **30,000 bytes** | **1,483 ms** |

### Conclusión
El formato **Binario Directo** ocupó un **56,58% menos de espacio en red** que el formato JSON, demostrando una reducción significativa del consumo de ancho de banda y menor sobrecarga (overhead) en la transmisión de datos.

---

## 4. Captura de Pantalla del Software Funcionando




![Software Funcionando](captura_tp4.png)
