import java.io.*;
import java.net.*;

public class ServidorSimulado {
    private static final int PUERTO = 5500;

    public static void main(String[] args) {
        System.out.println("Servidor de Pruebas iniciado en el puerto " + PUERTO);
        int contadorPeticiones = 0;

        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    contadorPeticiones++;
                    System.out.println("\n[Servidor] Conexión entrante #" + contadorPeticiones);

                    if (contadorPeticiones % 3 != 0) {
                        System.out.println("[Servidor] SIMULANDO FALLO TRANSITORIO -> Cerrando socket abruptamente...");
                        clientSocket.close();
                        continue;
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                    String peticion = in.readLine();
                    System.out.println("[Servidor] Petición recibida: " + peticion);
                    out.println("200 OK - Resultado procesado: 45");
                    System.out.println("[Servidor] Respuesta enviada con éxito.");
                } catch (IOException e) {
                    System.out.println("[Servidor] Error con cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
}