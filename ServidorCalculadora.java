import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServidorCalculadora {
    private static final int PUERTO = 5500;

    public static void main(String[] args) {
        System.out.println("=== Servidor Calculadora Iniciado ===");
        
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Esperando conexiones en el puerto " + PUERTO + "...");

            while (true) {
                // Acepta la conexión entrante de un cliente (Bloqueante)
                try (Socket socketCliente = serverSocket.accept();
                     BufferedReader entrada = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
                     PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)) {

                    System.out.println("Cliente conectado desde: " + socketCliente.getInetAddress().getHostAddress());

                    // Leer la trama enviada por el cliente (ejemplo: "15;+;30")
                    String trama = entrada.readLine();
                    if (trama != null) {
                        System.out.println("Petición recibida: " + trama);
                        String resultado = procesarOperacion(trama);
                        
                        // Enviar respuesta al cliente
                        salida.println(resultado);
                        System.out.println("Resultado enviado: " + resultado);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando petición del cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el puerto " + PUERTO + ": " + e.getMessage());
        }
    }

    private static String procesarOperacion(String trama) {
        try {
            String[] partes = trama.split(";");
            if (partes.length != 3) {
                return "ERROR: Formato de trama invalido (Usar: num1;operador;num2)";
            }

            int num1 = Integer.parseInt(partes[0].trim());
            String operador = partes[1].trim();
            int num2 = Integer.parseInt(partes[2].trim());

            switch (operador) {
                case "+":
                    return String.valueOf(num1 + num2);
                case "-":
                    return String.valueOf(num1 - num2);
                case "*":
                    return String.valueOf(num1 * num2);
                case "/":
                    if (num2 == 0) {
                        return "ERROR: Division por cero";
                    }
                    return String.valueOf((double) num1 / num2);
                default:
                    return "ERROR: Operador invalido (" + operador + ")";
            }
        } catch (NumberFormatException e) {
            return "ERROR: Los operandos deben ser enteros validos";
        }
    }
}