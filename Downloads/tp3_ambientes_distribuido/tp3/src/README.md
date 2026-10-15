# Trabajo Práctico N° 3: Sockets TCP/UDP y Concurrencia en Java

**Asignatura:** Desarrollo de Aplicaciones para Ambientes Distribuidos  
**Docente:** Lic. Gabriel Artaza  
**Alumna:** Yazmin Agustina Dorado  

---

## 📋 Descripción del Proyecto
Este trabajo práctico implementa aplicaciones en red utilizando la API `java.net` de Java, abarcando dos paradigmas fundamentales de comunicación:
1. **TCP (Orientado a conexión):** Servidor de chat multihilo concurrente con capacidad de *broadcast* para múltiples clientes simultáneos.
2. **UDP (Sin conexión):** Sistema de telemetría y alertas con recepción de datagramas y manejo de resiliencia mediante `SocketTimeoutException`.

---

## ⚙️ Instrucciones de Compilación y Ejecución

### 1. Compilación General
Se recomienda compilar especificando la versión de compatibilidad si es necesario:
```bash
javac --release 21 src/ejercicio1/*.java
javac --release 21 src/ejercicio2/*.java

capturas : <img width="1018" height="245" alt="Captura de pantalla 2026-10-13 190611" src="https://github.com/user-attachments/assets/11320524-f42a-4dae-ac0c-667738a27ff5" />

capturas: <img width="1035" height="243" alt="Captura de pantalla 2026-10-13 190646" src="https://github.com/user-attachments/assets/70444f1b-4070-471e-a399-3584f85cfdf6" />

capturas: <img width="853" height="231" alt="Captura de pantalla 2026-10-15 134222" src="https://github.com/user-attachments/assets/16d19fea-2edf-49e2-a98e-7463b1bf7c59" />

capturas: <img width="828" height="207" alt="Captura de pantalla 2026-10-15 134300" src="https://github.com/user-attachments/assets/bfe61449-dfb9-4381-af8c-7c716741ddd8" />



