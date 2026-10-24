# Trabajo Práctico N° 5: Concurrencia y Sincronización con el Modelo de Actores

**Asignatura:** Desarrollo de Aplicaciones para Ambientes Distribuidos  
**Docente:** Lic. Gabriel Artaza  
**Modalidad:** Individual[cite: 1, 2]  

---

## 1. Descripción del Proyecto
Este trabajo práctico implementa un sistema concurrente basado en el **Modelo de Actores** utilizando **Java** y la librería **Apache Pekko**. El objetivo es resolver problemas de concurrencia mediante el paso de mensajes asincrónicos, eliminando por completo la memoria compartida y el uso de bloqueos (`synchronized` o `ReentrantLock`)[cite: 1].

### Componentes principales:
* **`LecturaMetrica`**: Mensaje inmutable que transporta el ID del sensor y el valor de la métrica[cite: 1].
* **`ActorProcesador`**: Nodo central con un buzón (*Mailbox*) FIFO que procesa las lecturas de forma secuencial y mantiene un estado privado aislado (acumulador, contador y promedio histórico)[cite: 1].
* **`Main`**: Inicializa el sistema (*Spawn*) y lanza una ráfaga concurrente de más de 500 mensajes (*Send*) simulando múltiples sensores[cite: 1, 2].

---

## 2. Diagrama de Secuencia / Flujo

```mermaid
sequenceDiagram
    autonumber
    participant Main
    participant ActorSensor
    participant Mailbox (Cola FIFO)
    participant ActorProcesador

    Main->>ActorProcesador: Spawn (Crear Actor y Mailbox)
    loop Ráfaga de 500 mensajes concurrentes
        ActorSensor->>Mailbox (Cola FIFO): Send (Envía LecturaMetrica inmutable)
    end
    loop Procesamiento secuencial sin bloqueos
        Mailbox (Cola FIFO)->>ActorProcesador: Descola mensaje
        Note over ActorProcesador: State Change: Actualiza contador,<br/>acumulador y recalcula promedio
    end


<img width="723" height="225" alt="Captura de pantalla 2026-10-23 204421" src="https://github.com/user-attachments/assets/6f97b64c-c5a1-45f0-9cea-633ae6dc9046" />
