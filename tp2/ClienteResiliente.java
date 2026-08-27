import java.io.*;
import java.net.*;
import java.util.Random;

public class ClienteResiliente {
    private static final String HOST = "localhost";
    private static final int PUERTO = 5500;
    private static final int MAX_REINTENTOS = 5;
    private static final long TIEMPO_BASE_MS = 1000;

    public static void main(String[] args) {
        Random random = new Random();
        boolean exito = false;
        int intentoActual = 0;

        long tiempoInicioTotal = System.currentTimeMillis();

        System.out.println("=== CLIENTE RESILIENTE INICIADO ===");

        while (intentoActual < MAX_REINTENTOS && !exito) {
            intentoActual++;
            System.out.println("\n[Cliente] Intentando conectar (Intento " + intentoActual + " de " + MAX_REINTENTOS + ")...");

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(HOST, PUERTO), 3000);

                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("15;+;30");

                String respuesta = in.readLine();

                if (respuesta != null) {
                    System.out.println("[Cliente] Respuesta del servidor: " + respuesta);
                    exito = true;
                } else {
                    throw new IOException("El servidor cerró la conexión sin responder.");
                }

            } catch (IOException e) {
                System.err.println("[Cliente] Fallo en el intento " + intentoActual + ": " + e.getMessage());

                if (intentoActual < MAX_REINTENTOS) {
                    long backoffExponencial = (long) (TIEMPO_BASE_MS * Math.pow(2, intentoActual - 1));
                    int jitter = random.nextInt(501);
                    long tiempoEsperado = backoffExponencial + jitter;

                    System.out.printf("[Cliente] Reintentando en %d ms (Backoff: %d ms | Jitter aleatorio: %d ms)...\n",
                            tiempoEsperado, backoffExponencial, jitter);

                    try {
                        Thread.sleep(tiempoEsperado);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        System.err.println("Espera interrumpida.");
                        break;
                    }
                }
            }
        }

        long tiempoFinTotal = System.currentTimeMillis();
        long tiempoTotalTranscurrido = tiempoFinTotal - tiempoInicioTotal;

        System.out.println("\n=============================================");
        System.out.println("         MÉTRICAS DE RESILIENCIA            ");
        System.out.println("=============================================");
        System.out.println("• Estado final de la petición: " + (exito ? "ÉXITO" : "FALLO DEFINITIVO"));
        System.out.println("• Cantidad de intentos realizados: " + intentoActual);
        System.out.println("• Tiempo total transcurrido: " + tiempoTotalTranscurrido + " ms (" + String.format("%.2f", tiempoTotalTranscurrido / 1000.0) + " segundos)");
        System.out.println("=============================================\n");
    }
}