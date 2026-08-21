# Trabajo Práctico N° 1 - Calculadora Distribuida TCP
## Alumno: Agustina Dorado

# Ejercicio 2: Análisis Teórico-Práctico

## 1. ¿Qué sucede con el cliente si el servidor no está ejecutándose al intentar conectar?
El cliente no encuentra al servidor en la red y la conexión falla de inmediato.

Excepción de Java:
`java.net.ConnectException: Connection refused`

## 2. ¿Qué línea de tu código bloquea la ejecución hasta que ocurre un evento de red?
* En el Servidor: La línea `serverSocket.accept()` (Se queda esperando hasta que un cliente intente conectarse).
* En el Cliente y Servidor: La línea `entrada.readLine()` (Se queda esperando hasta que el otro lado envíe un mensaje de texto).

## 3. ¿Qué cambios se necesitan para ejecutar el Cliente y el Servidor en dos computadoras distintas por Wi-Fi?
1. Cambiar la IP en el Cliente: Reemplazar "localhost" por la dirección IP de la notebook del servidor (ejemplo: "192.168.1.15").
2. Averiguar la IP: El compañero del servidor debe abrir la terminal y escribir `ipconfig` para ver su IP de Wi-Fi.
3. Desactivar/Permitir el Firewall: En la PC del servidor hay que dar permiso al puerto (ej: 5500) en el Firewall de Windows para que no bloquee la conexión entrante.
4. Estar en la misma red: Ambas computadoras deben estar conectadas al mismo Wi-Fi.